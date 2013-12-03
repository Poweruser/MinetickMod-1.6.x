package net.minecraft.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;



public class WorldGenVillageStartPiece extends WorldGenVillageWell {

    public final WorldChunkManager a;
    public final boolean b;
    public final int c;
    public WorldGenVillagePieceWeight d;
    public List e;
    public List i = new ArrayList();
    public List j = new ArrayList();

    public WorldGenVillageStartPiece(WorldChunkManager worldchunkmanager, int i, Random random, int j, int k, List list, int l) {
        super((WorldGenVillageStartPiece) null, 0, random, j, k);
        this.a = worldchunkmanager;
        this.e = list;
        this.c = l;
        BiomeBase biomebase = worldchunkmanager.getBiome(j, k);

        BiomeBaseDB biomeBaseDB = worldchunkmanager.getBiomeBaseObj();

        //this.b = biomebase == BiomeBase.DESERT || biomebase == BiomeBase.DESERT_HILLS;
        this.b = biomebase.equals(biomeBaseDB.DESERT) || biomebase.equals(biomeBaseDB.DESERT_HILLS); // Poweruser
        this.k = this;
    }

    public WorldChunkManager d() {
        return this.a;
    }
}
