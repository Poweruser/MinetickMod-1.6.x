package net.minecraft.server;

public class GenLayerRiverMix extends GenLayer {

    private GenLayer b;
    private GenLayer c;

    public GenLayerRiverMix(long i, GenLayer genlayer, GenLayer genlayer1) {
        super(i);
        this.b = genlayer;
        this.c = genlayer1;
    }

    public void a(long i) {
        this.b.a(i);
        this.c.a(i);
        super.a(i);
    }

    public int[] a(int i, int j, int k, int l) {
        int[] aint = this.b.a(i, j, k, l);
        int[] aint1 = this.c.a(i, j, k, l);
        //int[] aint2 = IntCache.a(k * l);
        int[] aint2 = this.intCache.a(k * l); // Poweruser

        for (int i1 = 0; i1 < k * l; ++i1) {
            /*
            if (aint[i1] == BiomeBase.OCEAN.id) {
                aint2[i1] = aint[i1];
            } else if (aint1[i1] >= 0) {
                if (aint[i1] == BiomeBase.ICE_PLAINS.id) {
                    aint2[i1] = BiomeBase.FROZEN_RIVER.id;
                } else if (aint[i1] != BiomeBase.MUSHROOM_ISLAND.id && aint[i1] != BiomeBase.MUSHROOM_SHORE.id) {
                    aint2[i1] = aint1[i1];
                } else {
                    aint2[i1] = BiomeBase.MUSHROOM_SHORE.id;
                }
            } else {
                aint2[i1] = aint[i1];
            }
            */
            // Poweruser start
            if (aint[i1] == this.biomeBaseDB.OCEAN.id) {
                aint2[i1] = aint[i1];
            } else if (aint1[i1] >= 0) {
                if (aint[i1] == this.biomeBaseDB.ICE_PLAINS.id) {
                    aint2[i1] = this.biomeBaseDB.FROZEN_RIVER.id;
                } else if (aint[i1] != this.biomeBaseDB.MUSHROOM_ISLAND.id && aint[i1] != this.biomeBaseDB.MUSHROOM_SHORE.id) {
                    aint2[i1] = aint1[i1];
                } else {
                    aint2[i1] = this.biomeBaseDB.MUSHROOM_SHORE.id;
                }
            } else {
                aint2[i1] = aint[i1];
            }
            // Poweruser end
        }

        return aint2;
    }
}
