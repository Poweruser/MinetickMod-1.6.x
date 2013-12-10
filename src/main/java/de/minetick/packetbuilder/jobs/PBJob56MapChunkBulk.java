package de.minetick.packetbuilder.jobs;

import java.util.List;

import net.minecraft.server.Chunk;
import net.minecraft.server.Packet56MapChunkBulk;
import net.minecraft.server.PlayerConnection;
import de.minetick.packetbuilder.PacketBuilderJobInterface;

public class PBJob56MapChunkBulk implements PacketBuilderJobInterface {

    private PlayerConnection connection;
    private List<Chunk> chunks;
    
    public PBJob56MapChunkBulk(PlayerConnection connection, List<Chunk> chunks) {
        this.connection = connection;
        this.chunks = chunks;
    }
    
    @Override
    public void buildAndSendPacket() {
        this.connection.sendPacket(new Packet56MapChunkBulk(this.chunks));
        this.chunks = null;
        this.connection = null;
    }
}
