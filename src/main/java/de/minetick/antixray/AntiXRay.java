package de.minetick.antixray;

import java.util.Random;

import net.minecraft.server.Block;
import net.minecraft.server.Chunk;
import net.minecraft.server.WorldServer;

public class AntiXRay {
    
    private WorldServer worldServer;
    private static boolean blocksToHide[] = new boolean[65536];
    private static int[][] additionalUpdatePositions = new int[][]{ { 2,0, 0},{ 2,0, 1},{ 2,0,-1},
                                                                  {-2,0, 0},{-2,0, 1},{-2,0,-1},
                                                                  { 0,0,-2},{ 1,0,-2},{-1,0,-2},
                                                                  { 0,0, 2},{ 1,0, 2},{-1,0, 2},
                                                                  { 0,-2,0},{ 0,2, 0}};
    private Random random = new Random();
    
    public AntiXRay(WorldServer world) {
        this.worldServer = world;
        if(this.worldServer.dimension == 0) {
            blocksToHide[14] = true;
            blocksToHide[15] = true;
            //blocksToHide[16] = true;
            //blocksToHide[21] = true;
            blocksToHide[56] = true;
            //blocksToHide[73] = true;
            blocksToHide[129] = true;
        }
    }

    public void issueBlockUpdates(int x, int y, int z) {
        for(int i = x - 1; i <= x + 1; i++) {
            for(int j = y - 1; j <= y + 1; j++) {
                for(int k = z - 1; k <= z + 1; k++) {
                    if(j >= 0 &&  j < 256 && !(i == x && j == y && k == z)) {
                        this.worldServer.notify(i, j, k);
                    }
                }
            }
        }
        int xp, yp, zp;
        for(int i = 0; i < additionalUpdatePositions.length; i++) {
            xp = additionalUpdatePositions[i][0] + x;
            yp = additionalUpdatePositions[i][1] + y;
            zp = additionalUpdatePositions[i][2] + z; 
            if(yp >= 0 && yp < 256) {
                this.worldServer.notify(xp, yp, zp);
            }
        }
    }
    
    public void orebfuscate(byte[] buildBuffer, int dataLength, Chunk chunk, int chunkSectionsBitMask) {
        int index = 0;
        // just the lower 4 sections should be enough, thats up to height 64
        for(int sectionID = 0; sectionID < 4; sectionID++) {
            if((chunkSectionsBitMask & (1 << sectionID)) != 0) {
                for(int y = 0; y < 16; y++) { 
                    for(int z = 0; z < 16; z++) {
                        if(z == 0 || z == 15) {
                            index += 16;
                            continue;
                        }
                        for(int x = 0; x < 16; x++) {
                            // work within the chunk only, not on its borders
                            if(x != 0 && x != 15) {
                                if(index >= dataLength) {
                                    return;
                                }
                                int blockID = buildBuffer[index] & 255;
                                if(blocksToHide[blockID]) {
                                    if(this.hasOnlySolidBlockNeighbours(chunk, sectionID, x, y, z)) {
                                        buildBuffer[index] = 1;
                                    }
                                } else if(blockID == Block.STONE.id || blockID == Block.DIRT.id) {
                                    double r = this.random.nextDouble();
                                    if(r < 0.10D) {
                                        if(this.hasOnlySolidBlockNeighbours(chunk, sectionID, x, y, z)) {
                                            if(r < 0.02D) {
                                                buildBuffer[index] = 56;
                                            } else if(r < 0.04D) {
                                                buildBuffer[index] = 15;
                                            } else if(r < 0.06D) {
                                                buildBuffer[index] = 14;
                                            } else if(r < 0.08D) {
                                                buildBuffer[index] = 74;
                                            } else if(r < 0.1D) {
                                                buildBuffer[index] = 16;
                                            }
                                        }
                                    }
                                }
                            }
                            index++;
                        }
                    }
                }
            }
        }
    }

    private boolean hasOnlySolidBlockNeighbours(Chunk chunk, int section, int x, int y, int z) {
        return this.checkForSolidBlocks(chunk, section, x, y, z, 1);
    }
    
    private boolean checkForSolidBlocks(Chunk chunk, int section, int x, int y, int z, int distance) {
        boolean allSolid = true;
        int blockID = chunk.getTypeIdWithinSection(section, x + distance, y, z);
        allSolid = allSolid && Block.l(blockID);
        blockID = chunk.getTypeIdWithinSection(section, x - distance, y, z);
        allSolid = allSolid && Block.l(blockID);        
        blockID = chunk.getTypeIdWithinSection(section, x, y, z + distance);
        allSolid = allSolid && Block.l(blockID);
        blockID = chunk.getTypeIdWithinSection(section, x, y, z - distance);
        allSolid = allSolid && Block.l(blockID);
        if(!allSolid) { return allSolid; }

        int below = y - distance;
        int belowSection = section;
        if(below < 0) {
            below += 16;
            belowSection--;
        }
        if(belowSection >= 0) {
            blockID = chunk.getTypeIdWithinSection(belowSection, x, below, z);
            allSolid = allSolid && Block.l(blockID);
        }

        int above = y + distance;
        int aboveSection = section;
        if(above > 15) {
            above -= 16;
            aboveSection++;
        }
        if(aboveSection < 16) {
            blockID = chunk.getTypeIdWithinSection(aboveSection, x, above, z);
            allSolid = allSolid && Block.l(blockID);
        }
        return allSolid;
    }
}
