package de.minetick;

public enum BiomeBaseIDEnum {
    OCEAN(0),
    PLAINS(1),

    DESERT(2),

    EXTREME_HILLS(3),
    FOREST(4),
    TAIGA(5),

    SWAMPLAND(6),
    RIVER(7),

    HELL(8),
    SKY(9),

    FROZEN_OCEAN(10),
    FROZEN_RIVER(11),
    ICE_PLAINS(12),
    ICE_MOUNTAINS(13),

    MUSHROOM_ISLAND(14),
    MUSHROOM_SHORE(15),

    BEACH(16),
    DESERT_HILLS(17),
    FOREST_HILLS(18),
    TAIGA_HILLS(19),

    SMALL_MOUNTAINS(20),

    JUNGLE(21),
    JUNGLE_HILLS(22);

    private final int id;

    private BiomeBaseIDEnum(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }
}
