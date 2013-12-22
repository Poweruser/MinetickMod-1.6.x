package de.minetick;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;

import org.bukkit.craftbukkit.util.LongHash;

import de.minetick.PlayerChunkManager.ChunkPosEnum;

import net.minecraft.server.ChunkCoordIntPair;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.PlayerChunk;

public class PlayerChunkSendQueue {

    private LinkedHashSet<Long> serverData; // what it should be
    private LinkedHashSet<ChunkCoordIntPair> clientData; // sent Data
    private LinkedList<ChunkCoordIntPair> queue; // waiting to be sent
    private Object lock = new Object();
    private PlayerChunkManager pcm;
    private EntityPlayer player;

    public PlayerChunkSendQueue(PlayerChunkManager pcm, EntityPlayer entityplayer) {
        this.pcm = pcm;
        this.serverData = new LinkedHashSet<Long>();
        this.clientData = new LinkedHashSet<ChunkCoordIntPair>();
        this.queue = new LinkedList<ChunkCoordIntPair>();
        this.player = entityplayer;
    }
    
    public void sort(EntityPlayer entityplayer) {
        synchronized(this.lock) {
            ChunkCoordComparator comp = new ChunkCoordComparator(entityplayer);
            Collections.sort(this.queue, comp);
            Collections.sort(this.player.chunkCoordIntPairQueue, comp);
        }
    }
    
    public boolean hasChunksQueued() {
        return this.queue.size() > 0;
    }
    
    public boolean queueForSend(PlayerChunk playerchunk, EntityPlayer entityplayer) {
        boolean a = false, b = false, c = false;
        ChunkCoordIntPair ccip = PlayerChunk.a(playerchunk);
        synchronized(this.lock) {
            a = this.clientData.contains(ccip);
            b = this.serverData.contains(LongHash.toLong(ccip.x, ccip.z));
            c = this.queue.contains(ccip);
            if(!a && b && !c) {
                this.queue.add(ccip);
                this.player.chunkCoordIntPairQueue.add(ccip);
                return true;
            }
        }
        if(a || !b || c) {
            if(!b) {
                if(c || a) {
                    playerchunk.b(entityplayer);
                    this.removeFromClient(ccip);
                }
            }            
        }
        return false;
    }
    
    public void addToServer(int x, int z) {
        synchronized(this.lock) {
            this.serverData.add(LongHash.toLong(x, z));
        }
    }
    
    public void checkServerDataSize(int x, int z, int viewDistance, EntityPlayer entityplayer) {
        int a = viewDistance * 2 + 1;
        if(this.serverData.size() > a*a) {
            synchronized(this.lock) {
                Iterator<Long> iter = this.serverData.iterator();
                while(iter.hasNext()) {
                    Long l = iter.next();
                    int i = LongHash.lsw(l.longValue());
                    int j = LongHash.msw(l.longValue());
                    ChunkPosEnum pos = PlayerChunkManager.isWithinRadius(i, j, x, z, viewDistance);
                    if(pos.equals(ChunkPosEnum.OUTSIDE)) {
                        PlayerChunk c = this.pcm.getPlayerChunkMap().a(i, j, false);
                        if(c != null) {
                            c.b(entityplayer);
                            this.removeFromClient(PlayerChunk.a(c));
                        }
                        iter.remove(); // remove from server
                    }
                }
            }
        }
    }
    
    public void removeFromServer(int x, int z) {
        synchronized(this.lock) {
            this.serverData.remove(LongHash.toLong(x, z));
        }
    }
    
    public void removeFromClient(ChunkCoordIntPair ccip) {
        synchronized(this.lock) {
            this.clientData.remove(ccip);
            this.queue.remove(ccip);
            this.player.chunkCoordIntPairQueue.remove(ccip);
        }
    }

    public void removeFromQueue(ChunkCoordIntPair ccip) {
        synchronized(this.lock) {
            this.queue.remove(ccip);
            this.player.chunkCoordIntPairQueue.remove(ccip);
        }
    }

    public ChunkCoordIntPair peekFirst() {
        ChunkCoordIntPair cc = null;
        synchronized(this.lock) {
            boolean foundOne = false;
            while(!foundOne && !this.queue.isEmpty()) {
                cc = this.queue.peekFirst();
                if(!this.serverData.contains(LongHash.toLong(cc.x, cc.z)) || this.clientData.contains(cc)) {
                    this.queue.removeFirst();
                    this.player.chunkCoordIntPairQueue.remove(cc);
                    cc = null;
                } else {
                    foundOne = true;
                }
            }
        }
        return cc;
    }
    
    public void removeFirst() {
        synchronized(this.lock) {
            ChunkCoordIntPair ccip = this.queue.removeFirst();
            this.clientData.add(ccip);
            this.player.chunkCoordIntPairQueue.remove(ccip);
        }
    }
    
    public void skipFirst() {
        synchronized(this.lock) {
            ChunkCoordIntPair ccip = this.queue.removeFirst();
            this.player.chunkCoordIntPairQueue.remove(ccip);
            if(this.isOnServer(ccip) && !this.isChunkSent(ccip)) {
                this.queue.addLast(ccip);
                this.player.chunkCoordIntPairQueue.add(ccip);
            }
        }
    }
    
    public void clear() {
        synchronized(this.lock) {
            this.clientData.clear();
            this.queue.clear();
            this.player.chunkCoordIntPairQueue.clear();
        }
    }

    public boolean isChunkSent(ChunkCoordIntPair ccip) {
            return this.clientData.contains(ccip);
    }

    public boolean isAboutToSend(ChunkCoordIntPair location) {
            return this.queue.contains(location);
    }
    
    public boolean alreadyLoaded(ChunkCoordIntPair ccip) {
            return this.isChunkSent(ccip) || this.isAboutToSend(ccip);
    }
    
    public boolean isOnServer(ChunkCoordIntPair ccip) {
            return this.serverData.contains(LongHash.toLong(ccip.x, ccip.z));
    }

    public boolean isOnServer(int x, int z) {
        return this.serverData.contains(LongHash.toLong(x, z));
    }

    public int size() {
        return this.queue.size();
    }
}
