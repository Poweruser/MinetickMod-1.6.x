package net.minecraft.server;

import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class BlockSand extends Block {

    //public static boolean instaFall;
    // Poweruser start
    private static Set<World> instantFall = Collections.synchronizedSet(new HashSet<World>());

    public static void setInstantFall(World world) {
        instantFall.add(world);
    }

    public static void unSetInstantFall(World world) {
        instantFall.remove(world);
    }

    public static boolean isInstantFallOn(World world) {
        return instantFall.contains(world);
    }
    // Poweruser end

    public BlockSand(int i) {
        super(i, Material.SAND);
        this.a(CreativeModeTab.b);
    }

    public BlockSand(int i, Material material) {
        super(i, material);
    }

    public void onPlace(World world, int i, int j, int k) {
        world.a(i, j, k, this.id, this.a(world));
    }

    public void doPhysics(World world, int i, int j, int k, int l) {
        world.a(i, j, k, this.id, this.a(world));
    }

    public void a(World world, int i, int j, int k, Random random) {
        if (!world.isStatic) {
            this.k(world, i, j, k);
        }
    }

    private void k(World world, int i, int j, int k) {
        if (canFall(world, i, j - 1, k) && j >= 0) {
            byte b0 = 32;

            //if (!instaFall && world.e(i - b0, j - b0, k - b0, i + b0, j + b0, k + b0)) {
            if (!isInstantFallOn(world) && world.e(i - b0, j - b0, k - b0, i + b0, j + b0, k + b0)) { // Poweruser
                if (!world.isStatic) {
                    EntityFallingBlock entityfallingblock = new EntityFallingBlock(world, (double) ((float) i + 0.5F), (double) ((float) j + 0.5F), (double) ((float) k + 0.5F), this.id, world.getData(i, j, k));

                    this.a(entityfallingblock);
                    world.addEntity(entityfallingblock);
                }
            } else {
                world.setAir(i, j, k);

                while (canFall(world, i, j - 1, k) && j > 0) {
                    --j;
                }

                if (j > 0) {
                    world.setTypeIdUpdate(i, j, k, this.id);
                }
            }
        }
    }

    protected void a(EntityFallingBlock entityfallingblock) {}

    public int a(World world) {
        return 2;
    }

    public static boolean canFall(World world, int i, int j, int k) {
        int l = world.getTypeId(i, j, k);

        if (l == 0) {
            return true;
        } else if (l == Block.FIRE.id) {
            return true;
        } else {
            Material material = Block.byId[l].material;

            return material == Material.WATER ? true : material == Material.LAVA;
        }
    }

    public void a_(World world, int i, int j, int k, int l) {}
}
