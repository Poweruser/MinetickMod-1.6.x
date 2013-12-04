package de.minetick;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import net.minecraft.server.ChunkCoordIntPair;
import net.minecraft.server.PlayerChunk;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.PlayerChunkMap;
import net.minecraft.server.WorldData;
import net.minecraft.server.WorldServer;
import net.minecraft.server.WorldType;

public class PlayerChunkManager {

    private List<EntityPlayer> shuffleList = Collections.synchronizedList(new LinkedList<EntityPlayer>());
    private boolean skipChunkGeneration = false;
    private int chunkCreated = 0;
    private boolean dirtyFlag = false;
    private WorldServer world;
    private PlayerChunkMap pcm;
    
    private Map<String, PlayerChunkBuffer> playerBuff = new HashMap<String, PlayerChunkBuffer>();

    public PlayerChunkManager(WorldServer world, PlayerChunkMap pcm) {
        this.world = world;
        this.pcm = pcm;
    }

    private String getMapKey(EntityPlayer entity) {
        return entity.getBukkitEntity().getName();
    }
    
    public PlayerChunkBuffer getChunkBuffer(EntityPlayer entityplayer) {
        return this.playerBuff.get(this.getMapKey(entityplayer));
    }

    public boolean hasChunksWaiting() {
        if(this.dirtyFlag) {
            this.dirtyFlag = false;
            return true;
        }
        if(this.shuffleList.size() == 0) {
            return false;
        }
        for(PlayerChunkBuffer pcb : this.playerBuff.values()) {
            if(!pcb.isEmpty()) {
                return true;
            }
        }
        return false;
    }
    
    public PlayerChunkBuffer addPlayer(EntityPlayer entityplayer) {
        String mapkey = this.getMapKey(entityplayer);
        PlayerChunkBuffer buff = this.playerBuff.get(mapkey);
        if(buff == null) {
            buff = new PlayerChunkBuffer(entityplayer);
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
            PriorityQueue<ChunkCoordIntPair> queue = buff.getCurrentQueue();
            while(queue.size() > 0 && buff.loadedChunks < 40 && buff.skippedChunks < 1) {
                ChunkCoordIntPair p = queue.poll();
                boolean exists = this.world.chunkExists(p.x, p.z);
                if(!exists && (!allowGeneration || this.skipChunkGeneration || allGenerated > (this.world.getWorldData().getType().equals(WorldType.FLAT) ? 1 : 0))) {
                    buff.skippedChunks++;
                } else {
                    int playerChunkPosX, playerChunkPosZ;
                    PlayerChunk c = this.pcm.a(p.x, p.z, false);
                    if(c == null) {
                        c = this.pcm.a(p.x, p.z, true);
                        playerChunkPosX = (int) entityplayer.locX >> 4;
                        playerChunkPosZ = (int) entityplayer.locZ >> 4;
                        if(this.isWithinRadius(p.x, p.z, playerChunkPosX, playerChunkPosZ, this.pcm.getViewDistance())) {
                            c.a(entityplayer);
                        }
                        if(c.isNewChunk()) {
                            buff.generatedChunks++;
                            allGenerated++;
                        } else {
                            buff.loadedChunks++;
                        }
                    } else {
                        playerChunkPosX = (int) entityplayer.locX >> 4;
                        playerChunkPosZ = (int) entityplayer.locZ >> 4;
                        if(this.isWithinRadius(p.x, p.z, playerChunkPosX, playerChunkPosZ, this.pcm.getViewDistance())) {
                            c.a(entityplayer);
                        }
                        buff.enlistedChunks++;
                    }
                    buff.remove(p);
                }
            }
            if(buff.generatedChunks > 0 || buff.loadedChunks > 0 || buff.enlistedChunks > 0) {
                Collections.sort(entityplayer.chunkCoordIntPairQueue, buff.comp);
            }
            if(buff.generatedChunks > 0) {
                this.shuffleList.remove(entityplayer);
                this.shuffleList.add(entityplayer);
            }
        }
        return allGenerated;
    }

    private boolean isWithinRadius(int positionx, int positionz, int centerx, int centerz, int radius) {
        int distancex = positionx - centerx;
        int distancez = positionz - centerz;

        return distancex >= -radius && distancex <= radius ? distancez >= -radius && distancez <= radius : false;
    }
}
