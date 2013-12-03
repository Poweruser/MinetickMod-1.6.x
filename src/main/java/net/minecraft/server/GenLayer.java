package net.minecraft.server;



public abstract class GenLayer {

    private long b;
    protected GenLayer a;
    private long c;
    private long d;

    // Poweruser start
    protected IntCache intCache;
    protected BiomeBaseDB biomeBaseDB;

    public void setIntCache(IntCache intCache) {
        this.intCache = intCache;
    }

    public void setBiomeBaseDB(BiomeBaseDB biomeBaseDB) {
        this.biomeBaseDB = biomeBaseDB;
    }

    public BiomeBaseDB getBiomeBaseDB() {
        return this.biomeBaseDB;
    }
    // Poweruser end

    //public static GenLayer[] a(long i, WorldType worldtype) {
    public static GenLayer[] a(long i, WorldType worldtype, IntCache intCache, BiomeBaseDB biomebasedb) {  // Poweruser - added parameter intCache
        LayerIsland layerisland = new LayerIsland(1L);
        layerisland.setIntCache(intCache); // Poweruser
        layerisland.setBiomeBaseDB(biomebasedb); // Poweruser
        GenLayerZoomFuzzy genlayerzoomfuzzy = new GenLayerZoomFuzzy(2000L, layerisland);
        genlayerzoomfuzzy.setIntCache(intCache); // Poweruser
        genlayerzoomfuzzy.setBiomeBaseDB(biomebasedb); // Poweruser
        GenLayerIsland genlayerisland = new GenLayerIsland(1L, genlayerzoomfuzzy);
        genlayerisland.setIntCache(intCache); // Poweruser
        genlayerisland.setBiomeBaseDB(biomebasedb); // Poweruser
        GenLayerZoom genlayerzoom = new GenLayerZoom(2001L, genlayerisland);
        genlayerzoom.setIntCache(intCache); // Poweruser
        genlayerzoom.setBiomeBaseDB(biomebasedb); // Poweruser

        genlayerisland = new GenLayerIsland(2L, genlayerzoom);
        genlayerisland.setIntCache(intCache); // Poweruser
        genlayerisland.setBiomeBaseDB(biomebasedb); // Poweruser
        GenLayerIcePlains genlayericeplains = new GenLayerIcePlains(2L, genlayerisland);
        genlayericeplains.setIntCache(intCache); // Poweruser
        genlayericeplains.setBiomeBaseDB(biomebasedb); // Poweruser

        genlayerzoom = new GenLayerZoom(2002L, genlayericeplains);
        genlayerzoom.setIntCache(intCache); // Poweruser
        genlayerzoom.setBiomeBaseDB(biomebasedb); // Poweruser
        genlayerisland = new GenLayerIsland(3L, genlayerzoom);
        genlayerisland.setIntCache(intCache); // Poweruser
        genlayerisland.setBiomeBaseDB(biomebasedb); // Poweruser
        genlayerzoom = new GenLayerZoom(2003L, genlayerisland);
        genlayerzoom.setIntCache(intCache); // Poweruser
        genlayerzoom.setBiomeBaseDB(biomebasedb); // Poweruser
        genlayerisland = new GenLayerIsland(4L, genlayerzoom);
        genlayerisland.setIntCache(intCache); // Poweruser
        genlayerisland.setBiomeBaseDB(biomebasedb); // Poweruser
        GenLayerMushroomIsland genlayermushroomisland = new GenLayerMushroomIsland(5L, genlayerisland);
        genlayermushroomisland.setIntCache(intCache); // Poweruser
        genlayermushroomisland.setBiomeBaseDB(biomebasedb); // Poweruser
        byte b0 = 4;

        if (worldtype == WorldType.LARGE_BIOMES) {
            b0 = 6;
        }

        GenLayer genlayer = GenLayerZoom.a(1000L, genlayermushroomisland, 0);
        genlayer.setIntCache(intCache); // Poweruser
        genlayer.setBiomeBaseDB(biomebasedb); // Poweruser
        GenLayerRiverInit genlayerriverinit = new GenLayerRiverInit(100L, genlayer);
        genlayerriverinit.setIntCache(intCache); // Poweruser
        genlayerriverinit.setBiomeBaseDB(biomebasedb); // Poweruser

        genlayer = GenLayerZoom.a(1000L, genlayerriverinit, b0 + 2);
        genlayer.setIntCache(intCache); // Poweruser
        genlayer.setBiomeBaseDB(biomebasedb); // Poweruser
        GenLayerRiver genlayerriver = new GenLayerRiver(1L, genlayer);
        genlayerriver.setIntCache(intCache); // Poweruser
        genlayerriver.setBiomeBaseDB(biomebasedb); // Poweruser
        GenLayerSmooth genlayersmooth = new GenLayerSmooth(1000L, genlayerriver);
        genlayersmooth.setIntCache(intCache); // Poweruser
        genlayersmooth.setBiomeBaseDB(biomebasedb); // Poweruser
        GenLayer genlayer1 = GenLayerZoom.a(1000L, genlayermushroomisland, 0);
        genlayer1.setIntCache(intCache); // Poweruser
        genlayer1.setBiomeBaseDB(biomebasedb); // Poweruser
        GenLayerBiome genlayerbiome = new GenLayerBiome(200L, genlayer1, worldtype);
        genlayerbiome.setIntCache(intCache); // Poweruser
        genlayerbiome.setBiomeBaseDB(biomebasedb); // Poweruser

        genlayer1 = GenLayerZoom.a(1000L, genlayerbiome, 2);
        genlayer1.setIntCache(intCache); // Poweruser
        genlayer1.setBiomeBaseDB(biomebasedb); // Poweruser
        /*
        Object object = new GenLayerRegionHills(1000L, genlayer1);

        for (int j = 0; j < b0; ++j) {
            object = new GenLayerZoom((long) (1000 + j), (GenLayer) object);
            if (j == 0) {
                object = new GenLayerIsland(3L, (GenLayer) object);
            }

            if (j == 1) {
                object = new GenLayerMushroomShore(1000L, (GenLayer) object);
            }

            if (j == 1) {
                object = new GenLayerSwampRivers(1000L, (GenLayer) object);
            }
        }
        */
        // Poweruser start - casting is not necessary here
        GenLayer object = new GenLayerRegionHills(1000L, genlayer1);
        object.setIntCache(intCache);
        object.setBiomeBaseDB(biomebasedb);

        for (int j = 0; j < b0; ++j) {
            object = new GenLayerZoom((long) (1000 + j), object);
            object.setIntCache(intCache);
            object.setBiomeBaseDB(biomebasedb);
            if (j == 0) {
                object = new GenLayerIsland(3L, object);
                object.setIntCache(intCache);
                object.setBiomeBaseDB(biomebasedb);
            }

            if (j == 1) {
                object = new GenLayerMushroomShore(1000L, object);
                object.setIntCache(intCache);
                object.setBiomeBaseDB(biomebasedb);
            }

            if (j == 1) {
                object = new GenLayerSwampRivers(1000L, object);
                object.setIntCache(intCache);
                object.setBiomeBaseDB(biomebasedb);
            }
        }
        // Poweruser end

        GenLayerSmooth genlayersmooth1 = new GenLayerSmooth(1000L, (GenLayer) object);
        genlayersmooth1.setIntCache(intCache); // Poweruser
        genlayersmooth1.setBiomeBaseDB(biomebasedb); // Poweruser
        GenLayerRiverMix genlayerrivermix = new GenLayerRiverMix(100L, genlayersmooth1, genlayersmooth);
        genlayerrivermix.setIntCache(intCache); // Poweruser
        genlayerrivermix.setBiomeBaseDB(biomebasedb); // Poweruser
        GenLayerZoomVoronoi genlayerzoomvoronoi = new GenLayerZoomVoronoi(10L, genlayerrivermix);
        genlayerzoomvoronoi.setIntCache(intCache); // Poweruser
        genlayerzoomvoronoi.setBiomeBaseDB(biomebasedb); // Poweruser

        genlayerrivermix.a(i);
        genlayerzoomvoronoi.a(i);
        return new GenLayer[] { genlayerrivermix, genlayerzoomvoronoi, genlayerrivermix};
    }

    public GenLayer(long i) {
        this.d = i;
        this.d *= this.d * 6364136223846793005L + 1442695040888963407L;
        this.d += i;
        this.d *= this.d * 6364136223846793005L + 1442695040888963407L;
        this.d += i;
        this.d *= this.d * 6364136223846793005L + 1442695040888963407L;
        this.d += i;
    }

    public void a(long i) {
        this.b = i;
        if (this.a != null) {
            this.a.a(i);
        }

        this.b *= this.b * 6364136223846793005L + 1442695040888963407L;
        this.b += this.d;
        this.b *= this.b * 6364136223846793005L + 1442695040888963407L;
        this.b += this.d;
        this.b *= this.b * 6364136223846793005L + 1442695040888963407L;
        this.b += this.d;
    }

    public void a(long i, long j) {
        this.c = this.b;
        this.c *= this.c * 6364136223846793005L + 1442695040888963407L;
        this.c += i;
        this.c *= this.c * 6364136223846793005L + 1442695040888963407L;
        this.c += j;
        this.c *= this.c * 6364136223846793005L + 1442695040888963407L;
        this.c += i;
        this.c *= this.c * 6364136223846793005L + 1442695040888963407L;
        this.c += j;
    }

    protected int a(int i) {
        int j = (int) ((this.c >> 24) % (long) i);

        if (j < 0) {
            j += i;
        }

        this.c *= this.c * 6364136223846793005L + 1442695040888963407L;
        this.c += this.b;
        return j;
    }

    public abstract int[] a(int i, int j, int k, int l);
}
