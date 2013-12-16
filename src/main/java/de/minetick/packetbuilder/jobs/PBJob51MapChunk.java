package de.minetick.packetbuilder.jobs;

import net.minecraft.server.Chunk;
import net.minecraft.server.Packet51MapChunk;
import net.minecraft.server.PlayerConnection;
import de.minetick.PlayerChunkSendQueue;
import de.minetick.packetbuilder.PacketBuilderBuffer;
import de.minetick.packetbuilder.PacketBuilderJobInterface;
import de.minetick.packetbuilder.PacketBuilderThread;

public class PBJob51MapChunk implements PacketBuilderJobInterface {

    private PlayerConnection connection;
    private PlayerChunkSendQueue chunkQueue;
    private PlayerConnection[] connections;
    private PlayerChunkSendQueue[] chunkQueues;
    private Chunk chunk;
    private boolean flag;
    private int i;
    
    private boolean multipleConnections;
    
    public PBJob51MapChunk(PlayerConnection connection, PlayerChunkSendQueue chunkQueue, Chunk chunk, boolean flag, int i) {
        this.multipleConnections = false;
        this.connection = connection;
        this.chunkQueue = chunkQueue;
        this.chunk = chunk;
        this.flag = flag;
        this.i = i;
    }
    
    public PBJob51MapChunk(PlayerConnection[] connections, PlayerChunkSendQueue[] chunkQueues, Chunk chunk, boolean flag, int i) {
        this.multipleConnections = true;
        this.connections = connections;
        this.chunkQueues = chunkQueues;
        this.chunk = chunk;
        this.flag = flag;
        this.i = i;
    }
    
    @Override
    public void buildAndSendPacket(PacketBuilderBuffer pbb, Object checkAndSendLock) {
        Packet51MapChunk packet = new Packet51MapChunk(pbb, chunk, flag, i);
        if(this.multipleConnections) {
            synchronized(checkAndSendLock) {
                for(int a = 0; a < this.connections.length; a++) {
                    if(this.chunkQueues[a] != null && this.connections[a] != null) {
                        if(this.chunkQueues[a].isOnServer(chunk.x, chunk.z)) {
                            this.connections[a].sendPacket(packet);
                            this.connections[a] = null;
                            this.chunkQueues[a] = null;
                        }
                    }
                }
            }
            this.connections = null;
            this.chunkQueues = null;
        } else {
            if(this.chunkQueue != null &&  this.connection != null) {
                synchronized(checkAndSendLock) {
                    if(!this.chunkQueue.isOnServer(chunk.x, chunk.z)) {
                        this.connection.sendPacket(packet);
                    }
                }
            }
            this.connection = null;
            this.chunkQueue = null;
        }
        chunk = null;
    }
}
