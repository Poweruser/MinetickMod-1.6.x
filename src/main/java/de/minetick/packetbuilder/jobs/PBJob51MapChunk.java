package de.minetick.packetbuilder.jobs;

import java.util.ArrayList;

import net.minecraft.server.Chunk;
import net.minecraft.server.NetworkManager;
import net.minecraft.server.Packet;
import net.minecraft.server.Packet51MapChunk;
import net.minecraft.server.PlayerConnection;
import net.minecraft.server.TileEntity;
import de.minetick.PlayerChunkSendQueue;
import de.minetick.packetbuilder.PacketBuilderBuffer;
import de.minetick.packetbuilder.PacketBuilderJobInterface;
import de.minetick.packetbuilder.PacketBuilderThread;

public class PBJob51MapChunk implements PacketBuilderJobInterface {

    private PlayerConnection connection;
    private NetworkManager networkManager;
    private PlayerChunkSendQueue chunkQueue;
    private PlayerConnection[] connections;
    private NetworkManager[] networkManagers;
    private PlayerChunkSendQueue[] chunkQueues;
    private Chunk chunk;
    private boolean flag;
    private int i;
    private boolean sendTileEntities;
    ArrayList<NetworkManager> validOnes = new ArrayList<NetworkManager>();
    
    private boolean multipleConnections;
    
    public PBJob51MapChunk(PlayerConnection connection, PlayerChunkSendQueue chunkQueue, Chunk chunk, boolean flag, int i) {
        this.multipleConnections = false;
        this.connection = connection;
        if(this.connection != null && this.connection.networkManager instanceof NetworkManager) {
            this.networkManager = (NetworkManager) this.connection.networkManager;
        }
        this.chunkQueue = chunkQueue;
        this.chunk = chunk;
        this.flag = flag;
        this.i = i;
        this.sendTileEntities = false;
    }
    
    public PBJob51MapChunk(PlayerConnection[] connections, PlayerChunkSendQueue[] chunkQueues, Chunk chunk, boolean flag, int i, boolean sendTileEntities) {
        this.multipleConnections = true;
        this.connections = connections;
        this.networkManagers = new NetworkManager[this.connections.length];
        for(int a = 0; a < this.connections.length; a++) {
            if(this.connections[a] != null && this.connections[a].networkManager instanceof NetworkManager) {
                this.networkManagers[a] = (NetworkManager) this.connections[a].networkManager;
            }
        }
        this.chunkQueues = chunkQueues;
        this.chunk = chunk;
        this.flag = flag;
        this.i = i;
        this.sendTileEntities = sendTileEntities;
    }
    
    @Override
    public void buildAndSendPacket(PacketBuilderBuffer pbb, Object checkAndSendLock) {
        boolean packetSent = false;
        Packet51MapChunk packet = new Packet51MapChunk(pbb, chunk, flag, i);
        if(this.multipleConnections) {
            ArrayList tileentities = null;
            // TODO: Im currently not sure if synchronizing is still required here, needs to be checked
            synchronized(checkAndSendLock) {
                if(this.sendTileEntities) {
                    tileentities = new ArrayList();
                    tileentities.addAll(chunk.tileEntities.values());
                }
                for(int a = 0; a < this.connections.length; a++) {
                    if(this.chunkQueues[a] != null && this.connections[a] != null && this.networkManagers[a] != null) {
                        if(this.chunkQueues[a].isOnServer(chunk.x, chunk.z)) {
                            this.validOnes.add(this.networkManagers[a]);
                            this.connections[a] = null;
                            this.chunkQueues[a] = null;
                            this.networkManagers[a] = null;
                        }
                    }
                }
            }
            if(this.validOnes.size() > 0) {
                packet.setPendingUses(this.validOnes.size());
                packetSent = true;
                for(NetworkManager n: this.validOnes) {
                    n.queueChunks(packet);
                    if(this.sendTileEntities) {
                        for(int i = 0; i < tileentities.size(); i++) {
                            TileEntity te = (TileEntity) (tileentities.get(i));
                            Packet p = te.getUpdatePacket();
                            if(p != null) {
                                n.queueChunks(p);
                            }
                        }
                    }
                }
            }
            this.connections = null;
            this.chunkQueues = null;
            this.networkManagers = null;
        } else {
            if(this.chunkQueue != null &&  this.connection != null && this.networkManager != null) {
                synchronized(checkAndSendLock) {
                    if(!this.chunkQueue.isOnServer(chunk.x, chunk.z)) {
                        packetSent = true;
                        packet.setPendingUses(1);
                        this.networkManager.queueChunks(packet);
                    }
                }
            }
            this.connection = null;
            this.chunkQueue = null;
        }
        if(!packetSent) {
            packet.discard();
        }
        this.clear();
    }

    public void clear() {
        this.validOnes.clear();
        this.validOnes = null;
        chunk = null;
        if(this.connections != null) {
            for(int i = 0; i < this.connections.length; i++) {
                this.connections[i] = null;
            }
            this.connections = null;
        }
        if(this.chunkQueues != null) {
            for(int i = 0; i < this.chunkQueues.length; i++) {
                this.chunkQueues[i] = null;
            }
            this.chunkQueues = null;
        }
        this.connection = null;
        this.chunkQueue = null;
        this.networkManager = null;
    }
}
