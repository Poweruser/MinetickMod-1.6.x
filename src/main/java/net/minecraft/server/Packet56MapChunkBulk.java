package net.minecraft.server;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import de.minetick.MinetickMod;
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
    private AtomicInteger pendingUses;
    public static int targetCompressionLevel = MinetickMod.defaultPacketCompression;

    public static void changeCompressionLevel(int level) {
        if(level < Deflater.BEST_SPEED || level > Deflater.BEST_COMPRESSION) {
            targetCompressionLevel = Deflater.DEFAULT_COMPRESSION;
        } else {
            targetCompressionLevel = level;
        }
    }

    public void setPendingUses(int uses) {
        this.pendingUses = new AtomicInteger(uses);
    }

    public void discard() {
        if(this.pbb != null) {
            if(this.buffer != null) {
                this.pbb.offerSendBuffer(this.buffer);
                this.buffer = null;
            }
            this.pbb = null;
        }
    }
    // Poweruser end

    /*
    // CraftBukkit start
    static final ThreadLocal<Deflater> localDeflater = new ThreadLocal<Deflater>() {
        @Override
        protected Deflater initialValue() {
            // Don't use higher compression level, slows things down too much
            /*
             * Default was 6, but as compression is run in seperate threads now
             * a higher compression can be afforded
             * /
            return new Deflater(9);
        }
    };
    // CraftBukkit end
    */

    public Packet56MapChunkBulk() {}

    //public Packet56MapChunkBulk(List list) {
    // Poweruser start
    public Packet56MapChunkBulk(PacketBuilderBuffer pbb, List list) {
        this.pbb = pbb;
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
            ChunkMap chunkmap = Packet51MapChunk.a(chunk, true, '\uffff');

            /*
            if (buildBuffer.length < j + chunkmap.a.length) {
                byte[] abyte = new byte[j + chunkmap.a.length];

                System.arraycopy(buildBuffer, 0, abyte, 0, buildBuffer.length);
                buildBuffer = abyte;
            }

            System.arraycopy(chunkmap.a, 0, buildBuffer, j, chunkmap.a.length);
            */

            j += chunkmap.a.length;
            this.c[k] = chunk.x;
            this.d[k] = chunk.z;
            this.a[k] = chunkmap.b;
            this.b[k] = chunkmap.c;
            this.inflatedBuffers[k] = chunkmap.a;
        }

        // Poweruser start - we know the total size now (j), lets build the buffer and copy over the builderBuffers of each chunk
        byte[] completeBuildBuffer = new byte[j];
        int startIndex = 0;
        for(int a = 0; a < i; a++) {
            System.arraycopy(this.inflatedBuffers[a], 0, completeBuildBuffer, startIndex, this.inflatedBuffers[a].length);
            startIndex += this.inflatedBuffers[a].length;
            this.inflatedBuffers[a] = null;
        }
        Deflater deflater = new Deflater(targetCompressionLevel);
        try {
            deflater.setInput(completeBuildBuffer);
            deflater.finish();
            this.buffer = this.pbb.requestSendBuffer(completeBuildBuffer.length + 100);
            this.size = deflater.deflate(this.buffer);
        } finally {
            deflater.end();
        }
        // Poweruser end

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

    /*
    // Add compression method
    public void compress() {
        if (this.buffer != null) {
            return;
        }

        Deflater deflater = localDeflater.get();
        deflater.reset();
        deflater.setInput(this.buildBuffer);
        deflater.finish();

        this.buffer = new byte[this.buildBuffer.length + 100];
        this.size = deflater.deflate(this.buffer);
    }
    // CraftBukkit end
    */

    public void a(DataInput datainput) throws IOException { // CraftBukkit - throws IOException
        short short1 = datainput.readShort();

        this.size = datainput.readInt();
        this.h = datainput.readBoolean();
        this.c = new int[short1];
        this.d = new int[short1];
        this.a = new int[short1];
        this.b = new int[short1];
        this.inflatedBuffers = new byte[short1][];
        if (buildBuffer.length < this.size) {
            buildBuffer = new byte[this.size];
        }

        datainput.readFully(buildBuffer, 0, this.size);
        byte[] abyte = new byte[196864 * short1];
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
        if(this.pendingUses.decrementAndGet() == 0) {
            this.discard();
        }
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
