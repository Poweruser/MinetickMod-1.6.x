package de.minetick.antixray;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.bukkit.World.Environment;

import de.minetick.MinetickMod;
import de.minetick.MinetickThreadFactory;

import net.minecraft.server.Block;
import net.minecraft.server.Chunk;
import net.minecraft.server.WorldServer;

public class AntiXRay {
    
    private boolean enabled = false;
    private WorldServer worldServer;
    private static boolean blocksToHide[] = new boolean[65536];
    private static int[][] additionalUpdatePositions = new int[][]{ { 2,0, 0},{ 2,0, 1},{ 2,0,-1},
                                                                  {-2,0, 0},{-2,0, 1},{-2,0,-1},
                                                                  { 0,0,-2},{ 1,0,-2},{-1,0,-2},
                                                                  { 0,0, 2},{ 1,0, 2},{-1,0, 2},
                                                                  { 0,-2,0},{ 0,2, 0},{ 3,0, 0},
                                                                  {-3, 0,0},{ 0,0, 3},{ 0,0,-3}};
    private static List<String> configWorlds = new LinkedList<String>();
    private Random random = new Random();
    private static ScheduledExecutorService scheduledExecutorService;

    public static void setWorldsFromConfig(List<String> list) {
        configWorlds.clear();
        configWorlds.addAll(list);
    }

    public static void adjustThreadPoolSize(int size) {
        ScheduledExecutorService service = new ScheduledThreadPoolExecutor (
                size, new MinetickThreadFactory(Thread.MIN_PRIORITY, "MinetickMod_AntiXRay"));
        ScheduledExecutorService oldOne = scheduledExecutorService;
        scheduledExecutorService = service;
        if(oldOne != null) {
            oldOne.shutdown();
        }
    }

    public boolean isNether() {
        return this.worldServer.getWorld().getEnvironment().equals(Environment.NETHER);
    }

    public boolean isOverworld() {
        return this.worldServer.getWorld().getEnvironment().equals(Environment.NORMAL);
    }

    public AntiXRay(WorldServer world) {
        this.worldServer = world;
        this.enabled = false;
        for(String w: configWorlds) {
            if(w.equalsIgnoreCase(this.worldServer.getWorld().getName())) {
                this.enabled = true;
            }
        }
        if(this.isOverworld()) {
            blocksToHide[7] = true;
            blocksToHide[13] = true;
            blocksToHide[14] = true;
            blocksToHide[15] = true;
            blocksToHide[16] = true;
            blocksToHide[21] = true;
            blocksToHide[56] = true;
            blocksToHide[73] = true;
            blocksToHide[129] = true;
        } else if(this.isNether()) {
            blocksToHide[153] = true;
        }
    }

    public void issueBlockUpdates(int x, int y, int z) {
        for(int i = x - 1; i <= x + 1; i++) {
            for(int j = y - 1; j <= y + 1; j++) {
                for(int k = z - 1; k <= z + 1; k++) {
                    if(j >= 0 &&  j < 256 && !(i == x && j == y && k == z)) {
                        this.worldServer.notify(i, j, k);
                    }
                }
            }
        }
        int xp, yp, zp;
        for(int i = 0; i < additionalUpdatePositions.length; i++) {
            xp = additionalUpdatePositions[i][0] + x;
            yp = additionalUpdatePositions[i][1] + y;
            zp = additionalUpdatePositions[i][2] + z; 
            if(yp >= 0 && yp < 256) {
                this.worldServer.notify(xp, yp, zp);
            }
        }
    }
    
    public void orebfuscate(byte[] buildBuffer, int dataLength, Chunk chunk, int chunkSectionsBitMask) {

        // just the lower 4 sections should be enough, thats up to height 64
        int sectionsCount = 4;
        Future<Boolean>[] tasks = new Future[sectionsCount];
        int sectionStart = 0;
        for(int sectionID = 0; sectionID < sectionsCount; sectionID++) {
            if((chunkSectionsBitMask & (1 << sectionID)) != 0) {
                tasks[sectionID] = this.scheduledExecutorService.submit(new SectionChecker(sectionID, buildBuffer, dataLength, chunk, sectionStart));
                sectionStart += 4096;
            }
        }
        for(int i = 0; i < tasks.length; i++) {
            if(tasks[i] != null) {
                try {
                    tasks[i].get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean hasOnlySolidBlockNeighbours(Chunk chunk, int section, int x, int y, int z, int range) {
        boolean result = true;
        int i = range;
        try {
            while(result && i > 0) {
                result = this.checkForSolidBlocks(chunk, section, x, y, z, i);
                i--;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private boolean checkForSolidBlocks(Chunk chunk, int section, int x, int y, int z, int distance) {
        boolean allSolid = true;
        int blockID;

        boolean within = (z - distance >= 0 && z + distance <= 15 && x - distance >= 0 && x + distance <= 15);
        if(!within) {
            allSolid = allSolid && this.checkBlockOfOtherPosition(chunk, section, x, y, z, distance, 0);
            allSolid = allSolid && this.checkBlockOfOtherPosition(chunk, section, x, y, z, -distance, 0);
            allSolid = allSolid && this.checkBlockOfOtherPosition(chunk, section, x, y, z, 0, distance);
            allSolid = allSolid && this.checkBlockOfOtherPosition(chunk, section, x, y, z, 0, -distance);
        } else {
            blockID = chunk.getTypeIdWithinSection(section, x + distance, y, z);
            allSolid = allSolid && Block.l(blockID);
            blockID = chunk.getTypeIdWithinSection(section, x - distance, y, z);
            allSolid = allSolid && Block.l(blockID);
            blockID = chunk.getTypeIdWithinSection(section, x, y, z + distance);
            allSolid = allSolid && Block.l(blockID);
            blockID = chunk.getTypeIdWithinSection(section, x, y, z - distance);
            allSolid = allSolid && Block.l(blockID);
        }

        if(!allSolid) { return allSolid; }

        int below = y - distance;
        int belowSection = section;
        if(below < 0) {
            below += 16;
            belowSection--;
        }
        if(belowSection >= 0) {
            blockID = chunk.getTypeIdWithinSection(belowSection, x, below, z);
            allSolid = allSolid && Block.l(blockID);
        }

        int above = y + distance;
        int aboveSection = section;
        if(above > 15) {
            above -= 16;
            aboveSection++;
        }
        if(aboveSection < 16) {
            blockID = chunk.getTypeIdWithinSection(aboveSection, x, above, z);
            allSolid = allSolid && Block.l(blockID);
        }
        return allSolid;
    }

    private boolean checkBlockOfOtherPosition(Chunk c, int section, int x, int y, int z, int diffX, int diffZ) {
        int newX = x + diffX;
        int newZ = z + diffZ;
        int absX = (c.x << 4) + newX;
        int absZ = (c.z << 4) + newZ;
        int ox = absX >> 4;
        int oz = absZ >> 4;
        int blockID = 0;
        if(ox == c.x && oz == c.z) {
            blockID = c.getTypeIdWithinSection(section, newX, y, newZ);
        } else {
            if(this.worldServer.isLoaded(absX, y, absZ)) {
                blockID = this.worldServer.getTypeId(absX, section*16 + y, absZ);
            } else {
                return false;
            }
        }
        return Block.l(blockID);
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void enable() {
        this.enabled = true;
    }

    public void disable() {
        this.enabled = false;
    }

    private class SectionChecker implements Callable<Boolean> {

        private int sectionID;
        private int sectionStart;
        private byte[] buildBuffer;
        private int dataLength;
        private Chunk chunk;

        public SectionChecker(int sectionID, byte[] buildBuffer, int dataLength, Chunk chunk, int sectionStart) {
            this.sectionID = sectionID;
            this.buildBuffer = buildBuffer;
            this.sectionStart = sectionStart;
            this.dataLength = dataLength;
            this.chunk = chunk;
        }

        private Boolean cleanup() {
            this.chunk = null;
            this.buildBuffer = null;
            return true;
        }

        @Override
        public Boolean call() throws Exception {
            int index = this.sectionStart;
            for(int y = 0; y < 16; y++) {
                for(int z = 0; z < 16; z++) {
                    for(int x = 0; x < 16; x++) {
                        if(index >= dataLength) {
                            //System.out.println("out of range: " + index + " > " + dataLength);
                            return this.cleanup();
                        }
                        int blockID = buildBuffer[index] & 255;
                        if(isOverworld()) {
                            if(blocksToHide[blockID]) {
                                if(hasOnlySolidBlockNeighbours(chunk, sectionID, x, y, z, 1)) {
                                    buildBuffer[index] = 1; // stone
                                }
                            } else if(isEnabled()) {
                                if(isOverworld() && blockID == Block.STONE.id) {
                                    double r = random.nextDouble();
                                    if(r < 0.15D) {
                                        if(hasOnlySolidBlockNeighbours(chunk, sectionID, x, y, z, 2)) {
                                            if(r < 0.03D) {
                                                buildBuffer[index] = 56; // diamond ore
                                            } else if(r < 0.06D) {
                                                buildBuffer[index] = 15; // iron ore
                                            } else if(r < 0.09D) {
                                                buildBuffer[index] = 14; // gold ore
                                            } else if(r < 0.12D) {
                                                buildBuffer[index] = 74; // redstone or
                                            } else if(r < 0.15D) {
                                                buildBuffer[index] = 48; // mossy cobble
                                            }
                                        }
                                    }
                                }
                            }
                        } else if(isNether()) {
                            if(blocksToHide[blockID] && hasOnlySolidBlockNeighbours(chunk, sectionID, x, y, z, 1)) {
                                buildBuffer[index] = 87; // nether rack
                            }
                        }
                        index++;
                    }
                }
            }
            return this.cleanup();
        }
    }

    public static void shutdown() {
        scheduledExecutorService.shutdown();
    }
}
