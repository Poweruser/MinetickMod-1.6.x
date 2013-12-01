package net.minecraft.server;

public class ChunkMap {

    public byte[] a;
    public int b;
    public int c;

    // Poweruser start
    private byte[] buildBuffer = new byte[196864];

    public byte[] getBuildBuffer() {
        return this.buildBuffer;
    }
    // Poweruser end

    public ChunkMap() {}
}
