package com.sinthoras.hydroenergy.client.renderer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import com.sinthoras.hydroenergy.HE;

public class HESortedRenderList {

    // 541 is the maximum number of chunks with the same distance to the center chunk
    private static int[][] bucketsVaoIds = new int[HE.maxRenderDist * 3 + 1][541];
    private static int[][] bucketsNumWaterBlocks = new int[HE.maxRenderDist * 3 + 1][541];
    private static int[] bucketsIds = new int[HE.maxRenderDist * 3 + 1];

    private static int centerX = 0;
    private static int centerY = 0;
    private static int centerZ = 0;

    public static void setup(int chunkX, int chunkY, int chunkZ) {
        centerX = chunkX;
        centerY = chunkY;
        centerZ = chunkZ;
        for (int i = 0; i < bucketsIds.length; i++) {
            bucketsIds[i] = 0;
        }
    }

    public static void add(int vaoId, int numWaterBlocks, int chunkX, int chunkY, int chunkZ) {
        int distance = Math.abs(chunkX - centerX) + Math.abs(chunkY - centerY) + Math.abs(chunkZ - centerZ);
        bucketsVaoIds[distance][bucketsIds[distance]] = vaoId;
        bucketsNumWaterBlocks[distance][bucketsIds[distance]] = numWaterBlocks;
        bucketsIds[distance]++;
    }

    public static void render() {
        for (int distance = bucketsIds.length - 1; distance >= 0; distance--) {
            final int numVaos = bucketsIds[distance];
            if (numVaos == 0) {
                continue;
            }
            int[] vaoIds = bucketsVaoIds[distance];
            int[] numWaterBlocks = bucketsNumWaterBlocks[distance];
            for (int n = 0; n < numVaos; n++) {
                GL30.glBindVertexArray(vaoIds[n]);

                HEProgram.setCullFronts();
                GL11.glDrawArrays(GL11.GL_POINTS, 0, numWaterBlocks[n]);

                HEProgram.setCullBacks();
                GL11.glDrawArrays(GL11.GL_POINTS, 0, numWaterBlocks[n]);

                GL30.glBindVertexArray(0);
            }
        }
    }
}
