package net.minecraft.server;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import de.minetick.packetbuilder.PacketBuilderBuffer;

public class Packet56MapChunkBulk extends Packet {

    private int[] c;
    private int[] d;
    public int[] a;
    public int[] b;
    private byte[] buffer;
    private byte[][] inflatedBuffers;
    private int size;
    private boolean h;
    //private byte[] buildBuffer = new byte[0]; // CraftBukkit - remove static
    // Poweruser start
    private byte[] buildBuffer;
    private PacketBuilderBuffer pbb;
    // Poweruser end

    // CraftBukkit start
    static final ThreadLocal<Deflater> localDeflater = new ThreadLocal<Deflater>() {
        @Override
        protected Deflater initialValue() {
            // Don't use higher compression level, slows things down too much
            /*
             * Default was 6, but as compression is run in seperate threads now
             * a higher compression can be afforded
             */
            return new Deflater(8);
        }
    };
    // CraftBukkit end

    public Packet56MapChunkBulk() {}

    //public Packet56MapChunkBulk(List list) {
    // Poweruser start
    public Packet56MapChunkBulk(PacketBuilderBuffer pbb, List list) {
        this.pbb = pbb;
        this.buildBuffer = this.pbb.requestBuildBuffer(196864);
    // Poweruser end
        int i = list.size();

        this.c = new int[i];
        this.d = new int[i];
        this.a = new int[i];
        this.b = new int[i];
        this.inflatedBuffers = new byte[i][];
        this.h = !list.isEmpty() && !((Chunk) list.get(0)).world.worldProvider.g;
        int j = 0;

        for (int k = 0; k < i; ++k) {
            Chunk chunk = (Chunk) list.get(k);
            ChunkMap chunkmap = Packet51MapChunk.a(this.pbb, chunk, true, '\uffff');

            if (buildBuffer.length < j + chunkmap.a.length) {
                /*
                byte[] abyte = new byte[j + chunkmap.a.length];

                System.arraycopy(buildBuffer, 0, abyte, 0, buildBuffer.length);
                buildBuffer = abyte;
                */
                // Poweruser start
                this.pbb.offerBuildBuffer(this.buildBuffer);
                this.buildBuffer = this.pbb.requestBuildBufferAndCopy(j + chunkmap.a.length, this.buildBuffer);
                // Poweruser end
            }

            System.arraycopy(chunkmap.a, 0, buildBuffer, j, chunkmap.a.length);
            j += chunkmap.a.length;
            this.c[k] = chunk.x;
            this.d[k] = chunk.z;
            this.a[k] = chunkmap.b;
            this.b[k] = chunkmap.c;
            this.inflatedBuffers[k] = chunkmap.a;
        }

        this.compress(); // Poweruser

        /* CraftBukkit start - Moved to compress()
        Deflater deflater = new Deflater(-1);

        try {
            deflater.setInput(buildBuffer, 0, j);
            deflater.finish();
            this.buffer = new byte[j];
            this.size = deflater.deflate(this.buffer);
        } finally {
            deflater.end();
        }
        */
    }

    // Add compression method
    public void compress() {
        if (this.buffer != null) {
            return;
        }

        Deflater deflater = localDeflater.get();
        deflater.reset();
        deflater.setInput(this.buildBuffer);
        deflater.finish();

        //this.buffer = new byte[this.buildBuffer.length + 100];
        this.buffer = this.pbb.requestSendBuffer(this.buildBuffer.length + 100); // Poweruser
        this.size = deflater.deflate(this.buffer);
        // Poweruser start
        this.pbb.offerBuildBuffer(this.buildBuffer);
        this.buildBuffer = null;
        // Poweruser end
    }
    // CraftBukkit end

    public void a(DataInput datainput) throws IOException { // CraftBukkit - throws IOException
        short short1 = datainput.readShort();

        this.size = datainput.readInt();
        this.h = datainput.readBoolean();
        this.c = new int[short1];
        this.d = new int[short1];
        this.a = new int[short1];
        this.b = new int[short1];
        this.inflatedBuffers = new byte[short1][];
        boolean newBuildBuffer = false; // Poweruser
        //if (buildBuffer.length < this.size) {
        if(buildBuffer == null || buildBuffer.length < this.size) { // Poweruser
            //buildBuffer = new byte[this.size];
            // Poweruser start
            buildBuffer = this.pbb.requestBuildBuffer(this.size);
            newBuildBuffer = true;
            // Poweruser end
        }

        datainput.readFully(buildBuffer, 0, this.size);
        //byte[] abyte = new byte[196864 * short1];
        byte[] abyte = this.pbb.requestBuildBuffer(196864 * short1); // Poweruser
        Inflater inflater = new Inflater();

        inflater.setInput(buildBuffer, 0, this.size);

        try {
            inflater.inflate(abyte);
        } catch (DataFormatException dataformatexception) {
            throw new IOException("Bad compressed data format");
        } finally {
            inflater.end();
        }

        int i = 0;

        for (int j = 0; j < short1; ++j) {
            this.c[j] = datainput.readInt();
            this.d[j] = datainput.readInt();
            this.a[j] = datainput.readShort();
            this.b[j] = datainput.readShort();
            int k = 0;
            int l = 0;

            int i1;

            for (i1 = 0; i1 < 16; ++i1) {
                k += this.a[j] >> i1 & 1;
                l += this.b[j] >> i1 & 1;
            }

            i1 = 2048 * 4 * k + 256;
            i1 += 2048 * l;
            if (this.h) {
                i1 += 2048 * k;
            }

            this.inflatedBuffers[j] = new byte[i1];
            System.arraycopy(abyte, i, this.inflatedBuffers[j], 0, i1);
            i += i1;
        }
        // Poweruser start
        this.pbb.offerBuildBuffer(abyte);
        abyte = null;
        if(newBuildBuffer) {
            this.pbb.offerBuildBuffer(buildBuffer);
            buildBuffer = null;
        }
    }

    public void a(DataOutput dataoutput) throws IOException { // CraftBukkit - throws IOException
        //compress(); // CraftBukkit  // Poweruser - moved back to the constructor
        dataoutput.writeShort(this.c.length);
        dataoutput.writeInt(this.size);
        dataoutput.writeBoolean(this.h);
        dataoutput.write(this.buffer, 0, this.size);

        for (int i = 0; i < this.c.length; ++i) {
            dataoutput.writeInt(this.c[i]);
            dataoutput.writeInt(this.d[i]);
            dataoutput.writeShort((short) (this.a[i] & '\uffff'));
            dataoutput.writeShort((short) (this.b[i] & '\uffff'));
        }

        // Poweruser start
        this.pbb.offerSendBuffer(this.buffer);
        this.pbb = null;
        // Poweruser end
    }

    public void handle(Connection connection) {
        connection.a(this);
    }

    public int a() {
        return 6 + this.size + 12 * this.d();
    }

    public int d() {
        return this.c.length;
    }
}
