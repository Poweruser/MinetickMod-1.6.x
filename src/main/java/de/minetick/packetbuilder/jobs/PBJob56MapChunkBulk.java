package de.minetick.packetbuilder.jobs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.server.Chunk;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.Packet56MapChunkBulk;
import net.minecraft.server.PlayerConnection;
import net.minecraft.server.TileEntity;
import de.minetick.PlayerChunkSendQueue;
import de.minetick.packetbuilder.PacketBuilderBuffer;
import de.minetick.packetbuilder.PacketBuilderJobInterface;
import de.minetick.packetbuilder.PacketBuilderThreadPool;

public class PBJob56MapChunkBulk implements PacketBuilderJobInterface {

    private PlayerConnection connection;
    private List<Chunk> chunks;
    private PlayerChunkSendQueue chunkQueue;
    
    public PBJob56MapChunkBulk(PlayerConnection connection, List<Chunk> chunks, PlayerChunkSendQueue chunkQueue) {
        this.connection = connection;
        this.chunks = chunks;
        this.chunkQueue = chunkQueue;
    }
    
    @Override
    public void buildAndSendPacket(PacketBuilderBuffer pbb, Object checkAndSendLock) {
        Packet56MapChunkBulk packet = new Packet56MapChunkBulk(pbb, this.chunks);
        boolean allStillListed = true;
        synchronized(checkAndSendLock) {
            Iterator<Chunk> iter = this.chunks.iterator();
            while(iter.hasNext()) {
                Chunk c = iter.next();
                if(this.chunkQueue != null && this.connection != null) {
                    if(!this.chunkQueue.isOnServer(c.x, c.z)) {
                        allStillListed = false;
                        iter.remove();
                    }
                }
            }
            if(allStillListed) {
                this.connection.sendPacket(packet);
                ArrayList arraylist1 = new ArrayList();
                for(Chunk c : this.chunks) {
                    arraylist1.addAll(c.tileEntities.values());
                }
                Iterator iterator2 = arraylist1.iterator();
                EntityPlayer entityplayer = this.connection.player;
                while (iterator2.hasNext()) {
                    TileEntity tileentity = (TileEntity) iterator2.next();

                    entityplayer.b(tileentity);
                }
                entityplayer.addChunksToTrackPlayerIn(this.chunks);
            }
        }
        if(!allStillListed && !this.chunks.isEmpty()) {
            PacketBuilderThreadPool.addJobStatic(new PBJob56MapChunkBulk(this.connection, this.chunks, this.chunkQueue));
        }
        this.chunks = null;
        this.connection = null;
        this.chunkQueue = null;
    }
}
