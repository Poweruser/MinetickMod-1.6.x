package de.minetick;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import de.minetick.packetbuilder.PacketBuilderThreadPool;
import de.minetick.packetbuilder.jobs.PBJob56MapChunkBulk;

import net.minecraft.server.Chunk;
import net.minecraft.server.ChunkCoordIntPair;
import net.minecraft.server.NetworkManager.SendQueueFillLevel;
import net.minecraft.server.PlayerChunk;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.INetworkManager;
import net.minecraft.server.PlayerChunkMap;
import net.minecraft.server.TileEntity;
import net.minecraft.server.WorldData;
import net.minecraft.server.WorldServer;
import net.minecraft.server.WorldType;

public class PlayerChunkManager {

    private List<EntityPlayer> shuffleList = Collections.synchronizedList(new LinkedList<EntityPlayer>());
    private boolean skipChunkGeneration = false;
    private int chunkCreated = 0;
    private WorldServer world;
    private PlayerChunkMap pcm;

    private Map<String, PlayerChunkBuffer> playerBuff = new HashMap<String, PlayerChunkBuffer>();

    public PlayerChunkManager(WorldServer world, PlayerChunkMap pcm) {
        this.world = world;
        this.pcm = pcm;
    }

    public PlayerChunkMap getPlayerChunkMap() {
        return this.pcm;
    }

    private String getMapKey(EntityPlayer entity) {
        return entity.getBukkitEntity().getName();
    }
    
    public PlayerChunkBuffer getChunkBuffer(EntityPlayer entityplayer) {
        return this.playerBuff.get(this.getMapKey(entityplayer));
    }

    public PlayerChunkBuffer addPlayer(EntityPlayer entityplayer) {
        String mapkey = this.getMapKey(entityplayer);
        PlayerChunkBuffer buff = this.playerBuff.get(mapkey);
        if(buff == null) {
            buff = new PlayerChunkBuffer(this, entityplayer);
            this.playerBuff.put(mapkey, buff);
        } else {
            buff.clear();
        }
        this.shuffleList.add(entityplayer);
        return buff;
    }

    public void removePlayer(EntityPlayer entityplayer) {
        this.shuffleList.remove(entityplayer);
        String mapkey = this.getMapKey(entityplayer);
        PlayerChunkBuffer buff = this.playerBuff.get(mapkey);
        if(buff != null) {
            buff.clear();
        }
        this.playerBuff.remove(mapkey);
    }

    public boolean skipChunkGeneration() {
        return this.skipChunkGeneration;
    }

    public void skipChunkGeneration(boolean skip) {
        this.skipChunkGeneration = skip;
    }

    public boolean alreadyEnqueued(EntityPlayer entityplayer, ChunkCoordIntPair ccip) {
        PlayerChunkBuffer buff = this.playerBuff.get(this.getMapKey(entityplayer));
        if(buff == null) {
            return false;
        }
        if(buff.contains(ccip)) {
            return true;
        }
        return false;
    }

    public int updatePlayers(boolean allowGeneration) {
        int allGenerated = 0;
        EntityPlayer[] array = this.shuffleList.toArray(new EntityPlayer[0]);
        for(int i = 0; i < array.length; i++) {
            EntityPlayer entityplayer = array[i];
            PlayerChunkBuffer buff = this.playerBuff.get(this.getMapKey(entityplayer));
            if(buff == null) {
                continue;
            }
            buff.resetCounters();
            buff.updatePos(entityplayer);
            int playerChunkPosX = (int) entityplayer.locX >> 4;
            int playerChunkPosZ = (int) entityplayer.locZ >> 4;
            buff.getPlayerChunkSendQueue().checkServerDataSize(playerChunkPosX, playerChunkPosZ, this.getPlayerChunkMap().getViewDistance(), entityplayer);

            // High priority chunks
            PriorityQueue<ChunkCoordIntPair> queue = buff.getHighPriorityQueue();
            while(queue.size() > 0) {
                ChunkCoordIntPair ccip = queue.poll();
                if(buff.getPlayerChunkSendQueue().isOnServer(ccip) && !buff.getPlayerChunkSendQueue().alreadyLoaded(ccip)) {
                    PlayerChunk c = this.pcm.a(ccip.x, ccip.z, true);
                    c.a(entityplayer);
                    if(buff.getPlayerChunkSendQueue().queueForSend(c, entityplayer)) {
                        buff.loadedChunks++;
                    }
                }
                buff.remove(ccip);
            }

            // Low priority chunks
            queue = buff.getLowPriorityQueue();
            while(queue.size() > 0 && buff.loadedChunks < 80 && buff.skippedChunks < 1) {
                ChunkCoordIntPair ccip = queue.poll();
                if(buff.getPlayerChunkSendQueue().isOnServer(ccip) && !buff.getPlayerChunkSendQueue().alreadyLoaded(ccip)) {
                    if(allowGeneration && !this.skipChunkGeneration && allGenerated <= (this.world.getWorldData().getType().equals(WorldType.FLAT) ? 1 : 0)) {
                        PlayerChunk c = this.pcm.a(ccip.x, ccip.z, false);
                        if(c == null) {
                            c = this.pcm.a(ccip.x, ccip.z, true);
                            c.a(entityplayer);
                            if(c.isNewChunk()) {
                                buff.generatedChunks++;
                                allGenerated++;
                            } else {
                                buff.loadedChunks++;
                            }
                        } else {
                            c = this.pcm.a(ccip.x, ccip.z, false);
                            c.a(entityplayer);
                            buff.enlistedChunks++;
                        }
                        buff.getPlayerChunkSendQueue().queueForSend(c, entityplayer);
                        buff.remove(ccip);
                    } else {
                        buff.skippedChunks++;
                    }
                } else {
                    buff.remove(ccip);
                }
            }
            if(buff.generatedChunks > 0 || buff.loadedChunks > 0 || buff.enlistedChunks > 0) {
                buff.getPlayerChunkSendQueue().sort(entityplayer);
            }
            if(buff.generatedChunks > 0) {
                this.shuffleList.remove(entityplayer);
                this.shuffleList.add(entityplayer);
            }
            PlayerChunkSendQueue chunkQueue = buff.getPlayerChunkSendQueue();

            int packetCount = 1, chunksPerPacket = 4;
            SendQueueFillLevel level = entityplayer.playerConnection.getSendQueueFillLevel();
            if(level.equals(SendQueueFillLevel.VERYLOW)) {
                packetCount = 4;
            } else if(level.equals(SendQueueFillLevel.LOW)) {
                packetCount = 2;
            } else if(level.equals(SendQueueFillLevel.HIGH)) {
                chunksPerPacket = 2;
            } else if(level.equals(SendQueueFillLevel.FULL)) {
                packetCount = 0;
            }

            // Poweruser start - moved here from EntityPlayer.l_()
            for(int w = 0; w < packetCount; w++) {
            ArrayList<Chunk> arraylist = new ArrayList<Chunk>();
            //ArrayList arraylist1 = new ArrayList();
            int skipped = 0;
            while(chunkQueue.hasChunksQueued() && arraylist.size() < chunksPerPacket && skipped < 4) {
                ChunkCoordIntPair chunkcoordintpair = chunkQueue.peekFirst(); // Poweruser
                if (chunkcoordintpair != null) {
                    if(this.world.isLoaded(chunkcoordintpair.x << 4, 0, chunkcoordintpair.z << 4)) {
                        // CraftBukkit start - Get tile entities directly from the chunk instead of the world
                        Chunk chunk = this.world.getChunkAt(chunkcoordintpair.x, chunkcoordintpair.z);
                        arraylist.add(chunk);
                        //arraylist1.addAll(chunk.tileEntities.values());
                        // CraftBukkit end
                        chunkQueue.removeFirst(); // Poweruser
                    } else {
                        chunkQueue.skipFirst(); // Poweruser
                        skipped++;
                        //break;
                    }
                }
            }
            if (!arraylist.isEmpty()) {
                //this.playerConnection.sendPacket(new Packet56MapChunkBulk(arraylist));
                PacketBuilderThreadPool.addJobStatic(new PBJob56MapChunkBulk(entityplayer.playerConnection, arraylist, chunkQueue)); // Poweruser
                /*
                Iterator iterator2 = arraylist1.iterator();

                while (iterator2.hasNext()) {
                    TileEntity tileentity = (TileEntity) iterator2.next();

                    entityplayer.b(tileentity);
                }

                iterator2 = arraylist.iterator();

                while (iterator2.hasNext()) {
                    Chunk chunk = (Chunk) iterator2.next();

                    entityplayer.p().getTracker().a(entityplayer, chunk);
                }
                */
            }
            }
            // Poweruser end
        }
        return allGenerated;
    }

    public static ChunkPosEnum isWithinRadius(int positionx, int positionz, int centerx, int centerz, int radius) {
        int distancex = positionx - centerx;
        int distancez = positionz - centerz;

        boolean within = distancex >= -radius && distancex <= radius ? distancez >= -radius && distancez <= radius : false;
        if(within) {
            return ChunkPosEnum.INSIDE;
        } else {
            return ChunkPosEnum.OUTSIDE;
        }
    }

    public enum ChunkPosEnum {
        INSIDE,
        OUTSIDE;
    }

    public boolean doAllCornersOfPlayerAreaExist(int x, int z, int radius) {
        boolean exists = this.world.chunkExists(x - radius, z - radius);
        exists = exists && this.world.chunkExists(x + radius, z + radius);
        if(!exists) { return false; }
        exists = exists && this.world.chunkExists(x - radius, z + radius);
        return exists && this.world.chunkExists(x + radius, z - radius);
    }
}
