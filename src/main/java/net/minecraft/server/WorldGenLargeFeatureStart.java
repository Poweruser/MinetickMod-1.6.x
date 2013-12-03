package net.minecraft.server;

import java.util.Random;



public class WorldGenLargeFeatureStart extends StructureStart {

    public WorldGenLargeFeatureStart(World world, Random random, int i, int j) {
        BiomeBase biomebase = world.getBiome(i * 16 + 8, j * 16 + 8);

        BiomeBaseDB biomeBaseDB = world.getBiomeBaseDB(); // Poweruser

        //if (biomebase != BiomeBase.JUNGLE && biomebase != BiomeBase.JUNGLE_HILLS) {
        //    if (biomebase == BiomeBase.SWAMPLAND) {
        if (!biomebase.equals(biomeBaseDB.JUNGLE) && !biomebase.equals(biomeBaseDB.JUNGLE_HILLS)) { // Poweruser
            if (biomebase.equals(biomeBaseDB.SWAMPLAND)) {
                WorldGenWitchHut worldgenwitchhut = new WorldGenWitchHut(random, i * 16, j * 16);

                this.a.add(worldgenwitchhut);
            } else {
                WorldGenPyramidPiece worldgenpyramidpiece = new WorldGenPyramidPiece(random, i * 16, j * 16);

                this.a.add(worldgenpyramidpiece);
            }
        } else {
            WorldGenJungleTemple worldgenjungletemple = new WorldGenJungleTemple(random, i * 16, j * 16);

            this.a.add(worldgenjungletemple);
        }

        this.c();
    }
}
