package de.minetick.packetbuilder.jobs;

import java.util.ArrayList;

import net.minecraft.server.Chunk;
import net.minecraft.server.Packet51MapChunk;
import net.minecraft.server.PlayerConnection;
import de.minetick.packetbuilder.PacketBuilderJobInterface;

public class PBJob51MapChunk implements PacketBuilderJobInterface {

    private PlayerConnection connection;
    private ArrayList<PlayerConnection> connections;
    private Chunk chunk;
    private boolean flag;
    private int i;
    
    private boolean multipleConnections;
    
    public PBJob51MapChunk(PlayerConnection connection, Chunk chunk, boolean flag, int i) {
        this.multipleConnections = false;
        this.connection = connection;
        this.chunk = chunk;
        this.flag = flag;
        this.i = i;
    }
    
    public PBJob51MapChunk(ArrayList<PlayerConnection> connections, Chunk chunk, boolean flag, int i) {
        this.multipleConnections = true;
        this.connections = connections;
        this.chunk = chunk;
        this.flag = flag;
        this.i = i;
    }
    
    @Override
    public void buildAndSendPacket() {
        Packet51MapChunk packet = new Packet51MapChunk(chunk, flag, i);
        if(this.multipleConnections) {
            for(int a = 0; a < this.connections.size(); a++) {
                this.connections.get(a).sendPacket(packet);
            }
            this.connections.clear();
        } else {
            this.connection.sendPacket(packet);
            this.connection = null;
        }
        chunk = null;
        
        
        
        
    }

}
