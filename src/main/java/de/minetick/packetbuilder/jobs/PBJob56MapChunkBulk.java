package de.minetick.packetbuilder.jobs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.server.Chunk;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.NetworkManager;
import net.minecraft.server.Packet;
import net.minecraft.server.Packet56MapChunkBulk;
import net.minecraft.server.PlayerConnection;
import net.minecraft.server.TileEntity;
import de.minetick.PlayerChunkManager;
import de.minetick.PlayerChunkSendQueue;
import de.minetick.PlayerChunkManager.ChunkPosEnum;
import de.minetick.packetbuilder.PacketBuilderBuffer;
import de.minetick.packetbuilder.PacketBuilderJobInterface;
import de.minetick.packetbuilder.PacketBuilderThreadPool;

public class PBJob56MapChunkBulk implements PacketBuilderJobInterface {

    private PlayerConnection connection;
    private NetworkManager networkManager = null;
    private List<Chunk> chunks;
    private PlayerChunkSendQueue chunkQueue;
    
    public PBJob56MapChunkBulk(PlayerConnection connection, List<Chunk> chunks, PlayerChunkSendQueue chunkQueue) {
        this.connection = connection;
        if(this.connection != null && this.connection.networkManager instanceof NetworkManager) {
            this.networkManager = (NetworkManager) this.connection.networkManager;
        }
        this.chunks = chunks;
        this.chunkQueue = chunkQueue;
    }
    
    @Override
    public void buildAndSendPacket(PacketBuilderBuffer pbb) {
        if(this.networkManager == null || this.chunkQueue == null && this.connection == null) {
            if(this.chunks != null) {
                this.chunks.clear();
            }
            this.clear();
            return;
        }
        boolean highPriority = false;
        int playerX = (int) this.connection.player.locX >> 4;
        int playerZ = (int) this.connection.player.locZ >> 4;
        Packet56MapChunkBulk packet = new Packet56MapChunkBulk(pbb, this.chunks);
        boolean allStillListed = true;
        int[] vec = PlayerChunkManager.get2DDirectionVector(this.connection.player);
        Iterator<Chunk> iter = this.chunks.iterator();
        while(iter.hasNext()) {
            Chunk c = iter.next();
            if(!this.chunkQueue.isOnServer(c.x, c.z)) {
                if(!highPriority && PlayerChunkManager.isWithinRadius(c.x, c.z, playerX + vec[0], playerZ + vec[1], 1).equals(ChunkPosEnum.INSIDE)) {
                    highPriority = true;
                }
                allStillListed = false;
                iter.remove();
            }
        }
        if(allStillListed) {
            packet.setPendingUses(1);
            if(highPriority) {
                this.connection.sendPacket(packet);
            } else {
                this.networkManager.queueChunks(packet);
            }
            ArrayList<TileEntity> arraylist1 = new ArrayList<TileEntity>();
            for(Chunk c : this.chunks) {
                arraylist1.addAll(c.tileEntities.values());
            }
            Iterator<TileEntity> iterator2 = arraylist1.iterator();
            EntityPlayer entityplayer = this.connection.player;
            while (iterator2.hasNext()) {
                TileEntity tileentity = (TileEntity) iterator2.next();
                if (tileentity != null) {
                    Packet p = tileentity.getUpdatePacket();
                    if(p != null) {
                        this.networkManager.queueChunks(p);
                    }
                }
            }
            entityplayer.chunksForTracking.addAll(this.chunks);
            this.chunks.clear();
        } else {
            packet.discard();
        }
        if(!allStillListed && !this.chunks.isEmpty()) {
            PacketBuilderThreadPool.addJobStatic(new PBJob56MapChunkBulk(this.connection, this.chunks, this.chunkQueue));
        }
        this.clear();
    }

    public void clear() {
        this.chunks = null;
        this.connection = null;
        this.chunkQueue = null;
        this.networkManager = null;
    }
}
