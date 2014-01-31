package de.minetick;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import net.minecraft.server.EntityLiving;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Packet51MapChunk;
import net.minecraft.server.Packet56MapChunkBulk;

import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.EntityType;

import de.minetick.antixray.AntiXRay;
import de.minetick.modcommands.AntiXRayCommand;
import de.minetick.modcommands.PacketCompressionCommand;
import de.minetick.modcommands.PacketsPerTickCommand;
import de.minetick.modcommands.TPSCommand;
import de.minetick.modcommands.ThreadPoolsCommand;
import de.minetick.modcommands.WorldStatsCommand;
import de.minetick.packetbuilder.PacketBuilderThreadPool;
import de.minetick.profiler.Profiler;

public class MinetickMod {

    private PacketBuilderThreadPool packetBuilderPool;
    private int availableProcessors;
    private int antixrayPoolSize;
    private int packetbuilderPoolSize;
    private Profiler profiler;
    private ThreadPool threadPool;
    private int timerDelay = 45;
    private ScheduledExecutorService timerService = Executors.newScheduledThreadPool(2);
    private ScheduledFuture<Object> tickTimerTask;
    private TickTimer tickTimerObject;
    private TickCounter tickCounterObject;
    private List<Integer> ticksPerSecond;
    private int ticksCounter = 0;
    private HashSet<String> notGeneratingWorlds;
    private int maxEntityLifeTime = 10;
    private HashSet<EntityType> entitiesToDelete;
    private Logger logger = Logger.getLogger("Minecraft");

    private static boolean initDone = false;
    private static MinetickMod instance;
    public static final int defaultPacketCompression = 7;

    public MinetickMod() {
        this.availableProcessors = Runtime.getRuntime().availableProcessors();
        this.tickTimerObject = new TickTimer();
        this.tickCounterObject = new TickCounter();
        this.ticksPerSecond = Collections.synchronizedList(new LinkedList<Integer>());
        this.timerService.scheduleAtFixedRate(this.tickCounterObject, 1, 1, TimeUnit.SECONDS);
        this.notGeneratingWorlds = new HashSet<String>();
        this.entitiesToDelete = new HashSet<EntityType>();
        instance = this;
    }
    
    public void init() {
        if(!initDone) {
            initDone = true;
            CraftServer craftserver = MinecraftServer.getServer().server;
            craftserver.getCommandMap().register("tps", "MinetickMod", new TPSCommand("tps"));
            craftserver.getCommandMap().register("antixray", "MinetickMod", new AntiXRayCommand("antixray"));
            craftserver.getCommandMap().register("threadpools", "MinetickMod", new ThreadPoolsCommand("threadpools"));
            craftserver.getCommandMap().register("packetspertick", "MinetickMod", new PacketsPerTickCommand("packetspertick"));
            craftserver.getCommandMap().register("packetcompression", "MinetickMod", new PacketCompressionCommand("packetcompression"));
            craftserver.getCommandMap().register("worldstats", "MinetickMod", new WorldStatsCommand("worldstats"));
    
            this.profiler = new Profiler(craftserver.getMinetickModProfilerLogInterval(),
                                         craftserver.getMinetickModProfilerWriteEnabled(),
                                         craftserver.getMinetickModProfilerWriteInterval());
            this.threadPool = new ThreadPool(this.profiler);
            AntiXRay.setWorldsFromConfig(craftserver.getMinetickModOrebfuscatedWorlds());
            int axrps = craftserver.getMinetickModAntiXRayPoolSize();
            if(axrps <= 0 || axrps > 64) {
                axrps = this.availableProcessors;
            }
            this.antixrayPoolSize = axrps;
            AntiXRay.adjustThreadPoolSize(this.antixrayPoolSize);
            int pbps = craftserver.getMinetickModPacketBuilderPoolSize();
            if(pbps <= 0 || pbps > 64) {
                pbps = this.availableProcessors;
            }
            this.packetbuilderPoolSize = pbps;
            this.packetBuilderPool = new PacketBuilderThreadPool(this.packetbuilderPoolSize);
            int level = craftserver.getMinetickModCompressionLevel();
            if(level < 1 || level > 9) {
                level = defaultPacketCompression;
            }
            Packet51MapChunk.changeCompressionLevel(level);
            Packet56MapChunkBulk.changeCompressionLevel(level);
            int packets = craftserver.getMinetickModPacketsPerTick();
            if(packets < 1 || packets > 20) {
                packets = 1;
            }
            PlayerChunkManager.packetsPerTick = packets;
            List<String> worlds = craftserver.getMinetickModNotGeneratingWorlds();
            for(String w: worlds) {
                this.notGeneratingWorlds.add(w.toLowerCase());
            }
            this.maxEntityLifeTime = craftserver.getMinetickModMaxEntityLifeTime();
            List<String> entitiesToDelete = craftserver.getMinetickModEntitiesWithLimitedLifeTime();
            for(String name: entitiesToDelete) {
                try {
                    EntityType type = EntityType.valueOf(name.toUpperCase());
                    this.entitiesToDelete.add(type);
                } catch (IllegalArgumentException e) {
                    logger.warning("[MinetickMod] Settings: Skipping \"" + name + "\", as it is not a constant in org.bukkit.entity.EntityType!");
                }
            }
        }
    }
    
    public Profiler getProfiler() {
        return this.profiler;
    }
    
    public ThreadPool getThreadPool() {
        return this.threadPool;
    }
    
    public void startTickTimerTask() {
        this.tickTimerTask = instance.timerService.schedule(this.tickTimerObject, this.timerDelay, TimeUnit.MILLISECONDS);
    }

    public void cancelTimerTask(boolean flag) {
        this.tickTimerTask.cancel(false);
    }

    public void increaseTickCounter() {
        this.ticksCounter++;
    }

    public static Integer[] getTicksPerSecond() {
        return instance.ticksPerSecond.toArray(new Integer[0]);
    }

    public static boolean doesWorldNotGenerateChunks(String worldName) {
        return instance.notGeneratingWorlds.contains(worldName.toLowerCase());
    }

    public void shutdown() {
        this.threadPool.shutdown();
        this.timerService.shutdown();
        PacketBuilderThreadPool.shutdownStatic();
        AntiXRay.shutdown();
    }

    public void checkTickTime(long tickTime) {         
        if(tickTime > 45000000L) {
            if(this.timerDelay > 40) {
                this.timerDelay--;
            }
        } else if(this.timerDelay < 45) {
            this.timerDelay++;
        }
    }

    private class TickCounter implements Runnable {
        @Override
        public void run() {
            ticksPerSecond.add(ticksCounter);
            ticksCounter = 0;
            if(ticksPerSecond.size() > 30) {
                ticksPerSecond.remove(0);
            }
        }
    }

    private class TickTimer implements Callable<Object> {
        public Object call() {
            MinecraftServer.getServer().cancelHeavyCalculationsForAllWorlds(true);
            return null;
        }
    }

    public static int getMaxEntityLifeTime() {
        return instance.maxEntityLifeTime;
    }

    public static boolean isEntityAllowedToBeDeleted(EntityLiving entity) {
        return !entity.isImportantEntity() && instance.entitiesToDelete.contains(entity.getBukkitEntity().getType());
    }
}
