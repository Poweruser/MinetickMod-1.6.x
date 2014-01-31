package net.minecraft.server;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import de.minetick.MinetickMod;
import de.minetick.packetbuilder.PacketBuilderBuffer;

public class Packet51MapChunk extends Packet {

    public int a;
    public int b;
    public int c;
    public int d;
    private byte[] buffer;
    private byte[] inflatedBuffer;
    public boolean e;
    private int size;
    //private static byte[] buildBuffer = new byte[196864];
    // Poweruser start
    private static final ThreadLocal<byte[]> localBuildBuffer = new ThreadLocal<byte[]>() {
        @Override
        protected byte[] initialValue() {
            return new byte[196864];
        }
    };
    // Poweruser end

    public Packet51MapChunk() {
        this.lowPriority = true;
    }

    //public Packet51MapChunk(Chunk chunk, boolean flag, int i) {
    // Poweruser start
    private AtomicInteger pendingUses;
    private static int targetCompressionLevel = MinetickMod.defaultPacketCompression;

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

    private PacketBuilderBuffer pbb;

    public void discard() {
        if(this.pbb != null) {
            if(this.buffer != null) {
                this.pbb.offerSendBuffer(this.buffer);
                this.buffer = null;
            }
            this.pbb = null;
        }
    }

    public Packet51MapChunk(PacketBuilderBuffer pbb, Chunk chunk, boolean flag, int i) {
        this.pbb = pbb;
    // Poweruser end
        this.lowPriority = true;
        this.a = chunk.x;
        this.b = chunk.z;
        this.e = flag;
        ChunkMap chunkmap = a(chunk, flag, i);
        //Deflater deflater = new Deflater(-1);
        Deflater deflater = new Deflater(targetCompressionLevel); // Poweruser

        this.d = chunkmap.c;
        this.c = chunkmap.b;

        try {
            this.inflatedBuffer = chunkmap.a;
            deflater.setInput(chunkmap.a, 0, chunkmap.a.length);
            deflater.finish();
            //this.buffer = new byte[chunkmap.a.length];
            this.buffer = this.pbb.requestSendBuffer(chunkmap.a.length); // Poweruser
            this.size = deflater.deflate(this.buffer);
        } finally {
            deflater.end();
        }
    }

    public void a(DataInput datainput) throws IOException {
        this.a = datainput.readInt();
        this.b = datainput.readInt();
        this.e = datainput.readBoolean();
        this.c = datainput.readShort();
        this.d = datainput.readShort();
        this.size = datainput.readInt();
        /*
        if (buildBuffer.length < this.size) {
            buildBuffer = new byte[this.size];
        }
        */
        // Poweruser start
        if(localBuildBuffer.get().length < this.size) {
            localBuildBuffer.set(new byte[this.size]);
        }
        // Poweruser end

        //datainput.readFully(buildBuffer, 0, this.size);
        datainput.readFully(localBuildBuffer.get(), 0, this.size); // Poweruser
        int i = 0;

        int j;

        for (j = 0; j < 16; ++j) {
            i += this.c >> j & 1;
        }

        j = 12288 * i;
        if (this.e) {
            j += 256;
        }

        this.inflatedBuffer = new byte[j];
        Inflater inflater = new Inflater();

        //inflater.setInput(buildBuffer, 0, this.size);
        inflater.setInput(localBuildBuffer.get(), 0, this.size); // Poweruser
        try {
            inflater.inflate(this.inflatedBuffer);
        } catch (DataFormatException dataformatexception) {
            throw new IOException("Bad compressed data format");
        } finally {
            inflater.end();
        }
    }

    public void a(DataOutput dataoutput) throws IOException {
        dataoutput.writeInt(this.a);
        dataoutput.writeInt(this.b);
        dataoutput.writeBoolean(this.e);
        dataoutput.writeShort((short) (this.c & '\uffff'));
        dataoutput.writeShort((short) (this.d & '\uffff'));
        dataoutput.writeInt(this.size);
        dataoutput.write(this.buffer, 0, this.size);

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
        return 17 + this.size;
    }

    public static ChunkMap a(Chunk chunk, boolean flag, int i) {
        int j = 0;
        ChunkSection[] achunksection = chunk.i();
        int k = 0;
        ChunkMap chunkmap = new ChunkMap();
        //byte[] abyte = buildBuffer;
        byte[] abyte = localBuildBuffer.get(); // Poweruser

        if (flag) {
            chunk.seenByPlayer = true;
        }

        int l;

        for (l = 0; l < achunksection.length; ++l) {
            if (achunksection[l] != null && (!flag || !achunksection[l].isEmpty()) && (i & 1 << l) != 0) {
                chunkmap.b |= 1 << l;
                if (achunksection[l].getExtendedIdArray() != null) {
                    chunkmap.c |= 1 << l;
                    ++k;
                }
            }
        }

        for (l = 0; l < achunksection.length; ++l) {
            if (achunksection[l] != null && (!flag || !achunksection[l].isEmpty()) && (i & 1 << l) != 0) {
                byte[] abyte1 = achunksection[l].getIdArray();

                System.arraycopy(abyte1, 0, abyte, j, abyte1.length);
                j += abyte1.length;
            }
        }

        NibbleArray nibblearray;

        for (l = 0; l < achunksection.length; ++l) {
            if (achunksection[l] != null && (!flag || !achunksection[l].isEmpty()) && (i & 1 << l) != 0) {
                nibblearray = achunksection[l].getDataArray();
                System.arraycopy(nibblearray.a, 0, abyte, j, nibblearray.a.length);
                j += nibblearray.a.length;
            }
        }

        for (l = 0; l < achunksection.length; ++l) {
            if (achunksection[l] != null && (!flag || !achunksection[l].isEmpty()) && (i & 1 << l) != 0) {
                nibblearray = achunksection[l].getEmittedLightArray();
                System.arraycopy(nibblearray.a, 0, abyte, j, nibblearray.a.length);
                j += nibblearray.a.length;
            }
        }

        if (!chunk.world.worldProvider.g) {
            for (l = 0; l < achunksection.length; ++l) {
                if (achunksection[l] != null && (!flag || !achunksection[l].isEmpty()) && (i & 1 << l) != 0) {
                    nibblearray = achunksection[l].getSkyLightArray();
                    System.arraycopy(nibblearray.a, 0, abyte, j, nibblearray.a.length);
                    j += nibblearray.a.length;
                }
            }
        }

        if (k > 0) {
            for (l = 0; l < achunksection.length; ++l) {
                if (achunksection[l] != null && (!flag || !achunksection[l].isEmpty()) && achunksection[l].getExtendedIdArray() != null && (i & 1 << l) != 0) {
                    nibblearray = achunksection[l].getExtendedIdArray();
                    System.arraycopy(nibblearray.a, 0, abyte, j, nibblearray.a.length);
                    j += nibblearray.a.length;
                }
            }
        }

        if (flag) {
            byte[] abyte2 = chunk.m();

            System.arraycopy(abyte2, 0, abyte, j, abyte2.length);
            j += abyte2.length;
        }

        chunkmap.a = new byte[j];
        System.arraycopy(abyte, 0, chunkmap.a, 0, j);
        chunk.world.antiXRay.orebfuscate(chunkmap.a, chunkmap.a.length, chunk, chunkmap.b); // Poweruser
        return chunkmap;
    }
}
