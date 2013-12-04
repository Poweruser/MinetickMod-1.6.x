package de.minetick;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.PriorityQueue;

import net.minecraft.server.EntityPlayer;
import net.minecraft.server.ChunkCoordIntPair;

public class PlayerChunkBuffer {
    private LinkedHashSet<ChunkCoordIntPair> playerChunkBuffer;
    public PriorityQueue<ChunkCoordIntPair> pq;
    public ChunkCoordComparator comp;
    public int generatedChunks = 0;
    public int loadedChunks = 0;
    public int skippedChunks = 0;
    public int enlistedChunks = 0;

    public PlayerChunkBuffer(EntityPlayer ent) {
        this.playerChunkBuffer = new LinkedHashSet<ChunkCoordIntPair>();
        this.comp = new ChunkCoordComparator(ent);
        this.pq = new PriorityQueue<ChunkCoordIntPair>(750, this.comp);
    }

    public Comparator<ChunkCoordIntPair> updatePos(EntityPlayer ent) {
        this.comp.setPos(ent);
        return this.comp;
    }

    public void clear() {
        this.playerChunkBuffer.clear();
        this.pq.clear();
    }

    public boolean isEmpty() {
        return this.playerChunkBuffer.isEmpty();
    }

    public LinkedHashSet<ChunkCoordIntPair> getPlayerChunkBuffer() {
        return this.playerChunkBuffer;
    }

    public void add(ChunkCoordIntPair ccip) {
        this.playerChunkBuffer.add(ccip);
    }

    public void remove(ChunkCoordIntPair ccip) {
        this.playerChunkBuffer.remove(ccip);
    }

    public boolean contains(ChunkCoordIntPair ccip) {
        return this.playerChunkBuffer.contains(ccip);
    }

    public PriorityQueue<ChunkCoordIntPair> getCurrentQueue() {
        this.pq.clear();
        this.pq.addAll(this.playerChunkBuffer);
        return this.pq;
    }

    public void resetCounters() {
        this.generatedChunks = 0;
        this.enlistedChunks = 0;
        this.skippedChunks = 0;
        this.loadedChunks = 0;
    }
}
