package net.minecraft.server;

import net.minecraft.server.*;

public class BiomeBaseDB {
    public final BiomeBase[] biomes = new BiomeBase[256];

    public final BiomeBase OCEAN = new BiomeOcean(0).b(112).a("Ocean").b(-1.0F, 0.4F);
    public final BiomeBase PLAINS = new BiomePlains(1).b(9286496).a("Plains").a(0.8F, 0.4F);
    public final BiomeBase DESERT = new BiomeDesert(2).b(16421912).a("Desert").m().a(2.0F, 0.0F).b(0.1F, 0.2F);

    public final BiomeBase EXTREME_HILLS = new BiomeBigHills(3).b(6316128).a("Extreme Hills").b(0.3F, 1.5F).a(0.2F, 0.3F);
    public final BiomeBase FOREST = new BiomeForest(4).b(353825).a("Forest").a(5159473).a(0.7F, 0.8F);
    public final BiomeBase TAIGA = new BiomeTaiga(5).b(747097).a("Taiga").a(5159473).b().a(0.05F, 0.8F).b(0.1F, 0.4F);

    public final BiomeBase SWAMPLAND = new BiomeSwamp(6).b(522674).a("Swampland").a(9154376).b(-0.2F, 0.1F).a(0.8F, 0.9F);
    public final BiomeBase RIVER = new BiomeRiver(7).b(255).a("River").b(-0.5F, 0.0F);

    public final BiomeBase HELL = new BiomeHell(8).b(16711680).a("Hell").m().a(2.0F, 0.0F);
    public final BiomeBase SKY = new BiomeTheEnd(9).b(8421631).a("Sky").m();

    public final BiomeBase FROZEN_OCEAN = new BiomeOcean(10).b(9474208).a("FrozenOcean").b().b(-1.0F, 0.5F).a(0.0F, 0.5F);
    public final BiomeBase FROZEN_RIVER = new BiomeRiver(11).b(10526975).a("FrozenRiver").b().b(-0.5F, 0.0F).a(0.0F, 0.5F);
    public final BiomeBase ICE_PLAINS = new BiomeIcePlains(12).b(16777215).a("Ice Plains").b().a(0.0F, 0.5F);
    public final BiomeBase ICE_MOUNTAINS = new BiomeIcePlains(13).b(10526880).a("Ice Mountains").b().b(0.3F, 1.3F).a(0.0F, 0.5F);

    public final BiomeBase MUSHROOM_ISLAND = new BiomeMushrooms(14).b(16711935).a("MushroomIsland").a(0.9F, 1.0F).b(0.2F, 1.0F);
    public final BiomeBase MUSHROOM_SHORE = new BiomeMushrooms(15).b(10486015).a("MushroomIslandShore").a(0.9F, 1.0F).b(-1.0F, 0.1F);

    public final BiomeBase BEACH = new BiomeBeach(16).b(16440917).a("Beach").a(0.8F, 0.4F).b(0.0F, 0.1F);
    public final BiomeBase DESERT_HILLS = new BiomeDesert(17).b(13786898).a("DesertHills").m().a(2.0F, 0.0F).b(0.3F, 0.8F);
    public final BiomeBase FOREST_HILLS = new BiomeForest(18).b(2250012).a("ForestHills").a(5159473).a(0.7F, 0.8F).b(0.3F, 0.7F);
    public final BiomeBase TAIGA_HILLS = new BiomeTaiga(19).b(1456435).a("TaigaHills").b().a(5159473).a(0.05F, 0.8F).b(0.3F, 0.8F);

    public final BiomeBase SMALL_MOUNTAINS = new BiomeBigHills(20).b(7501978).a("Extreme Hills Edge").b(0.2F, 0.8F).a(0.2F, 0.3F);

    public final BiomeBase JUNGLE = new BiomeJungle(21).b(5470985).a("Jungle").a(5470985).a(1.2F, 0.9F).b(0.2F, 0.4F);
    public final BiomeBase JUNGLE_HILLS = new BiomeJungle(22).b(2900485).a("JungleHills").a(5470985).a(1.2F, 0.9F).b(1.8F, 0.5F);

    public BiomeBaseDB() {
        this.biomes[this.OCEAN.id] = this.OCEAN;
        this.biomes[this.PLAINS.id] = this.PLAINS;
        this.biomes[this.DESERT.id] = this.DESERT;
        this.biomes[this.EXTREME_HILLS.id] = this.EXTREME_HILLS;
        this.biomes[this.FOREST.id] = this.FOREST;
        this.biomes[this.TAIGA.id] = this.TAIGA;
        this.biomes[this.SWAMPLAND.id] = this.SWAMPLAND;
        this.biomes[this.RIVER.id] = this.RIVER;
        this.biomes[this.HELL.id] = this.HELL;
        this.biomes[this.SKY.id] = this.SKY;
        this.biomes[this.FROZEN_OCEAN.id] = this.FROZEN_OCEAN;
        this.biomes[this.FROZEN_RIVER.id] = this.FROZEN_RIVER;
        this.biomes[this.ICE_PLAINS.id] = this.ICE_PLAINS;
        this.biomes[this.ICE_MOUNTAINS.id] = this.ICE_MOUNTAINS;
        this.biomes[this.MUSHROOM_ISLAND.id] = this.MUSHROOM_ISLAND;
        this.biomes[this.MUSHROOM_SHORE.id] = this.MUSHROOM_SHORE;
        this.biomes[this.BEACH.id] = this.BEACH;
        this.biomes[this.DESERT_HILLS.id] = this.DESERT_HILLS;
        this.biomes[this.FOREST_HILLS.id] = this.FOREST_HILLS;
        this.biomes[this.TAIGA_HILLS.id] = this.TAIGA_HILLS;
        this.biomes[this.SMALL_MOUNTAINS.id] = this.SMALL_MOUNTAINS;
        this.biomes[this.JUNGLE.id] = this.JUNGLE;
        this.biomes[this.JUNGLE_HILLS.id] = this.JUNGLE_HILLS;
    }
}
