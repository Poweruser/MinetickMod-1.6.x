package de.minetick;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.PriorityQueue;

import de.minetick.PlayerChunkManager.ChunkPosEnum;

import net.minecraft.server.EntityPlayer;
import net.minecraft.server.ChunkCoordIntPair;

public class PlayerChunkBuffer {
    private LinkedHashSet<ChunkCoordIntPair> uncertainBorderChunks;
    private LinkedHashSet<ChunkCoordIntPair> lowPriorityBuffer;
    private LinkedHashSet<ChunkCoordIntPair> highPriorityBuffer;
    public PriorityQueue<ChunkCoordIntPair> pq;
    public ChunkCoordComparator comp;
    public int generatedChunks = 0;
    public int loadedChunks = 0;
    public int skippedChunks = 0;
    public int enlistedChunks = 0;
    private PlayerChunkManager playerChunkManager;

    public PlayerChunkBuffer(PlayerChunkManager playerChunkManager, EntityPlayer ent) {
        this.playerChunkManager = playerChunkManager;
        this.lowPriorityBuffer = new LinkedHashSet<ChunkCoordIntPair>();
        this.highPriorityBuffer = new LinkedHashSet<ChunkCoordIntPair>();
        this.uncertainBorderChunks = new LinkedHashSet<ChunkCoordIntPair>();
        this.comp = new ChunkCoordComparator(ent);
        this.pq = new PriorityQueue<ChunkCoordIntPair>(750, this.comp);
    }

    public Comparator<ChunkCoordIntPair> updatePos(EntityPlayer ent) {
        this.comp.setPos(ent);
        return this.comp;
    }

    public void clear() {
        this.lowPriorityBuffer.clear();
        this.highPriorityBuffer.clear();
        this.pq.clear();
    }
    
    public boolean isEmpty() {
        return this.lowPriorityBuffer.isEmpty() && this.highPriorityBuffer.isEmpty();
    }
    
    public LinkedHashSet<ChunkCoordIntPair> getLowPriorityBuffer() {
        return this.lowPriorityBuffer;
    }

    public LinkedHashSet<ChunkCoordIntPair> getHighPriorityBuffer() {
        return this.highPriorityBuffer;
    }

    public void addLowPriorityChunk(ChunkCoordIntPair ccip) {
        this.lowPriorityBuffer.add(ccip);
    }
    
    public void addHighPriorityChunk(ChunkCoordIntPair ccip) {
        this.highPriorityBuffer.add(ccip);
    }
    
    public void addBorderChunk(ChunkCoordIntPair ccip) {
        this.uncertainBorderChunks.add(ccip);
    }

    public void remove(ChunkCoordIntPair ccip) {
        this.lowPriorityBuffer.remove(ccip);
        this.highPriorityBuffer.remove(ccip);
    }
    
    public boolean contains(ChunkCoordIntPair ccip) {
        return this.lowPriorityBuffer.contains(ccip) || this.highPriorityBuffer.contains(ccip);
    }

    public PriorityQueue<ChunkCoordIntPair> getLowPriorityQueue() {
        this.pq.clear();
        this.pq.addAll(this.lowPriorityBuffer);
        return this.pq;
    }

    public PriorityQueue<ChunkCoordIntPair> getHighPriorityQueue() {
        this.pq.clear();
        this.pq.addAll(this.highPriorityBuffer);
        return this.pq;
    }

    public void resetCounters() {
        this.generatedChunks = 0;
        this.enlistedChunks = 0;
        this.skippedChunks = 0;
        this.loadedChunks = 0;
    }

    public void checkBorderChunks(int x, int z, int radius) {
        Iterator<ChunkCoordIntPair> iter = this.uncertainBorderChunks.iterator();
        while(iter.hasNext()) {
            ChunkCoordIntPair ccip = iter.next();
            ChunkPosEnum pos = this.playerChunkManager.isWithinRadius(ccip.x, ccip.z, x, z, radius);
            if(pos.equals(ChunkPosEnum.OUTSIDE)) {
                iter.remove();
            } else if(pos.equals(ChunkPosEnum.INSIDE)) {
                this.addLowPriorityChunk(ccip);
                iter.remove();
            }
        }
    }
}
