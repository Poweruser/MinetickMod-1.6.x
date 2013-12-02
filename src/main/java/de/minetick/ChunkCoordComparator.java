package de.minetick;

import java.util.Comparator;

import net.minecraft.server.EntityPlayer;
import net.minecraft.server.ChunkCoordIntPair;

public class ChunkCoordComparator implements Comparator<ChunkCoordIntPair> {
    private int x;
    private int z;

    public void setPos(EntityPlayer entityplayer) {
        x = (int) entityplayer.locX >> 4;
        z = (int) entityplayer.locZ >> 4;
    }

    public ChunkCoordComparator (EntityPlayer entityplayer) {
        this.setPos(entityplayer);
    }

    @Override
    public int compare(ChunkCoordIntPair a, ChunkCoordIntPair b) {
        if (a.equals(b)) {
            return 0;
        }

        // Subtract current position to set center point
        int ax = a.x - this.x;
        int az = a.z - this.z;
        int bx = b.x - this.x;
        int bz = b.z - this.z;

        int result = ((ax - bx) * (ax + bx)) + ((az - bz) * (az + bz));
        if (result != 0) {
            return result;
        }

        if (ax < 0) {
            if (bx < 0) {
                return bz - az;
            } else {
                return -1;
            }
        } else {
            if (bx < 0) {
                return 1;
            } else {
                return az - bz;
            }
        }
    }

}
