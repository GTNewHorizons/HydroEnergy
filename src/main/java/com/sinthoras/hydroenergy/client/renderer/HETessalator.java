package com.sinthoras.hydroenergy.client.renderer;

import java.nio.FloatBuffer;
import java.util.Stack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.culling.Frustrum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.client.MinecraftForgeClient;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GLContext;

import com.sinthoras.hydroenergy.HE;
import com.sinthoras.hydroenergy.HEUtil;
import com.sinthoras.hydroenergy.config.HEConfig;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class HETessalator {

    private static final int maxRenderDistance = (int) GameSettings.Options.RENDER_DISTANCE.getValueMax();
    private static final int maxRenderChunksX = 2 * maxRenderDistance + 1;
    private static final int maxRenderChunksZ = maxRenderChunksX;
    private static final HERenderChunk[] renderChunks = new HERenderChunk[maxRenderChunksX * maxRenderChunksZ];
    private static final Stack<HERenderChunk> availableRenderChunks = new Stack<>();

    private static final Stack<HEBufferIds> availableBuffers = new Stack<>();
    private static final FloatBuffer vboBuffer = GLAllocation.createDirectFloatBuffer(7 * HE.blockPerSubChunk);
    private static int numWaterBlocks = 0;

    private static int getChunkIndex(int chunkX, int chunkZ) {
        return HEUtil.nonNegativeModulo(chunkX, maxRenderChunksX)
                + HEUtil.nonNegativeModulo(chunkZ, maxRenderChunksZ) * maxRenderChunksX;
    }

    public static void onPostRender(int blockX, int blockY, int blockZ) {
        final int chunkX = HEUtil.coordBlockToChunk(blockX);
        final int chunkY = HEUtil.coordBlockToChunk(blockY);
        final int chunkZ = HEUtil.coordBlockToChunk(blockZ);
        HERenderSubChunk renderSubChunk = renderChunks[getChunkIndex(chunkX, chunkZ)].renderSubChunks[chunkY];

        if (numWaterBlocks != 0) {
            if (renderSubChunk.vaoId == GL31.GL_INVALID_INDEX) {
                if (availableBuffers.empty()) {
                    renderSubChunk.vaoId = GL30.glGenVertexArrays();
                    renderSubChunk.vboId = GL15.glGenBuffers();

                    GL30.glBindVertexArray(renderSubChunk.vaoId);

                    GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, renderSubChunk.vboId);
                    GL15.glBufferData(
                            GL15.GL_ARRAY_BUFFER,
                            (long) vboBuffer.capacity() * Float.BYTES,
                            GL15.GL_STATIC_DRAW);

                    GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 7 * Float.BYTES, 0);
                    GL20.glEnableVertexAttribArray(0);

                    GL20.glVertexAttribPointer(1, 1, GL11.GL_FLOAT, false, 7 * Float.BYTES, 3 * Float.BYTES);
                    GL20.glEnableVertexAttribArray(1);

                    GL20.glVertexAttribPointer(2, 1, GL11.GL_FLOAT, false, 7 * Float.BYTES, 4 * Float.BYTES);
                    GL20.glEnableVertexAttribArray(2);

                    GL20.glVertexAttribPointer(3, 1, GL11.GL_FLOAT, false, 7 * Float.BYTES, 5 * Float.BYTES);
                    GL20.glEnableVertexAttribArray(3);

                    GL20.glVertexAttribPointer(4, 1, GL11.GL_FLOAT, false, 7 * Float.BYTES, 6 * Float.BYTES);
                    GL20.glEnableVertexAttribArray(4);

                    GL30.glBindVertexArray(0);
                } else {
                    HEBufferIds ids = availableBuffers.pop();
                    renderSubChunk.vaoId = ids.vaoId;
                    renderSubChunk.vboId = ids.vboId;
                }
            }

            vboBuffer.flip();
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, renderSubChunk.vboId);
            GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, vboBuffer);
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

            renderSubChunk.numWaterBlocks = numWaterBlocks;

            // reset tesselator
            vboBuffer.clear();
            numWaterBlocks = 0;
        } else if (renderSubChunk.vaoId != GL31.GL_INVALID_INDEX) {
            HEBufferIds ids = new HEBufferIds();
            ids.vaoId = renderSubChunk.vaoId;
            ids.vboId = renderSubChunk.vboId;
            availableBuffers.push(ids);
            renderSubChunk.vaoId = GL31.GL_INVALID_INDEX;
            renderSubChunk.vboId = GL31.GL_INVALID_INDEX;
            renderSubChunk.numWaterBlocks = 0;
        }
    }

    public static void addBlock(int blockX, int blockY, int blockZ, int waterId, int worldColorModifier,
            boolean[] shouldSideBeRendered) {
        int renderSides = 0;
        for (int i = 0; i < shouldSideBeRendered.length; i++) {
            if (shouldSideBeRendered[i]) {
                renderSides |= 1 << i;
            }
        }

        vboBuffer.put(blockX);
        vboBuffer.put(blockY);
        vboBuffer.put(blockZ);

        final int lightXMinus = 15, lightXPlus = 15, lightYMinus = 15, lightYPlus = 15, lightZMinus = 15,
                lightZPlus = 15;
        final int light0 = (lightXMinus << 16) | (lightXPlus << 8) | lightYMinus;
        final int light1 = (lightYPlus << 16) | (lightZMinus << 8) | lightZPlus;
        vboBuffer.put(light0);
        vboBuffer.put(light1);

        final int info = (waterId << 6) | renderSides;
        vboBuffer.put(info);

        vboBuffer.put(worldColorModifier);

        numWaterBlocks++;
    }

    public static void render(ICamera camera) {
        if (!GLContext.getCapabilities().OpenGL30 || HEConfig.useLimitedRendering) {
            return;
        }
        if (MinecraftForgeClient.getRenderPass() == HE.waterBlocks[0].getRenderBlockPass()) {
            final Frustrum frustrum = (Frustrum) camera;
            final float cameraBlockX = (float) frustrum.xPosition;
            final float cameraBlockY = (float) frustrum.yPosition;
            final float cameraBlockZ = (float) frustrum.zPosition;

            GL11.glEnable(GL11.GL_BLEND);

            HEProgram.bind();

            HEProgram.setViewProjection(cameraBlockX, cameraBlockY, cameraBlockZ);
            HEProgram.setCameraPosition(cameraBlockX, cameraBlockY, cameraBlockZ);
            HEProgram.setWaterLevels();
            HEProgram.setDebugStates();
            HEProgram.setWaterUV();
            HEProgram.setFog();
            HEProgram.bindLightLookupTable();
            HEProgram.bindAtlasTexture();

            HESortedRenderList.setup(
                    HEUtil.coordBlockToChunk((int) cameraBlockX),
                    HEUtil.coordBlockToChunk((int) cameraBlockY),
                    HEUtil.coordBlockToChunk((int) cameraBlockZ));

            final World world = Minecraft.getMinecraft().theWorld;
            final int centerChunkX = HEUtil.coordBlockToChunk((int) cameraBlockX);
            final int centerChunkZ = HEUtil.coordBlockToChunk((int) cameraBlockZ);
            final int renderDistanceChunks = Minecraft.getMinecraft().renderGlobal.renderDistanceChunks;
            for (int offsetChunkX = -renderDistanceChunks; offsetChunkX < renderDistanceChunks; offsetChunkX++) {
                for (int offsetChunkZ = -renderDistanceChunks; offsetChunkZ < renderDistanceChunks; offsetChunkZ++) {
                    final int chunkX = centerChunkX + offsetChunkX;
                    final int chunkZ = centerChunkZ + offsetChunkZ;
                    final HERenderChunk renderChunks = HETessalator.renderChunks[getChunkIndex(chunkX, chunkZ)];
                    if (renderChunks != null) {
                        final Chunk vanillaChunk = world.getChunkFromChunkCoords(chunkX, chunkZ);
                        for (int chunkY = 0; chunkY < HE.chunkHeight; chunkY++) {
                            final int blockX = HEUtil.coordChunkToBlock(chunkX);
                            final int blockY = HEUtil.coordChunkToBlock(chunkY);
                            final int blockZ = HEUtil.coordChunkToBlock(chunkZ);
                            final HERenderSubChunk renderSubChunk = renderChunks.renderSubChunks[chunkY];
                            final AxisAlignedBB chunkBB = AxisAlignedBB.getBoundingBox(
                                    blockX,
                                    blockY,
                                    blockZ,
                                    blockX + HE.chunkWidth,
                                    blockY + HE.chunkHeight,
                                    blockZ + HE.chunkDepth);
                            if (renderSubChunk.vaoId != GL31.GL_INVALID_INDEX
                                    && !vanillaChunk.getAreLevelsEmpty(blockY, blockY + 15)
                                    && frustrum.isBoundingBoxInFrustum(chunkBB)) {
                                HESortedRenderList.add(
                                        renderSubChunk.vaoId,
                                        renderSubChunk.numWaterBlocks,
                                        chunkX,
                                        chunkY,
                                        chunkZ);
                            }
                        }
                    }
                }
            }

            HESortedRenderList.render();

            HEProgram.unbind();

            GL11.glDisable(GL11.GL_BLEND);
        }
    }

    // One can argue to use ChunkEvent.Load and ChunkEvent.Unload for this stuff,
    // but those are not in the GL thread and cause issues with cleanup etc
    public static void onRenderChunkUpdate(int oldBlockX, int oldBlockZ, int blockX, int blockY, int blockZ) {
        // Just execute once per vertical SubChunk-stack (aka chunk)
        if (blockY == 0) {
            final int oldChunkX = HEUtil.coordBlockToChunk(oldBlockX);
            final int oldChunkZ = HEUtil.coordBlockToChunk(oldBlockZ);
            final int chunkX = HEUtil.coordBlockToChunk(blockX);
            final int chunkZ = HEUtil.coordBlockToChunk(blockZ);

            final int oldChunkIndex = getChunkIndex(oldChunkX, oldChunkZ);
            final HERenderChunk oldRenderChunk = renderChunks[oldChunkIndex];
            if (oldRenderChunk != null && oldRenderChunk.chunkX == oldChunkX && oldRenderChunk.chunkZ == oldChunkZ) {
                renderChunks[oldChunkIndex] = null;
                availableRenderChunks.push(oldRenderChunk);
            }

            final HERenderChunk renderChunk = availableRenderChunks.isEmpty() ? new HERenderChunk()
                    : availableRenderChunks.pop();
            renderChunk.chunkX = chunkX;
            renderChunk.chunkZ = chunkZ;

            for (HERenderSubChunk renderSubChunk : renderChunk.renderSubChunks) {
                if (renderSubChunk.vaoId != GL31.GL_INVALID_INDEX) {
                    HEBufferIds ids = new HEBufferIds();
                    ids.vaoId = renderSubChunk.vaoId;
                    ids.vboId = renderSubChunk.vboId;
                    availableBuffers.push(ids);
                    renderSubChunk.vaoId = GL31.GL_INVALID_INDEX;
                    renderSubChunk.vboId = GL31.GL_INVALID_INDEX;
                    renderSubChunk.numWaterBlocks = 0;
                }
            }

            final int newChunkIndex = getChunkIndex(chunkX, chunkZ);
            if (renderChunks[newChunkIndex] != null) {
                availableRenderChunks.push(renderChunks[newChunkIndex]);
            }
            renderChunks[newChunkIndex] = renderChunk;
        }
    }

    public static int getGpuMemoryUsage() {
        int subChunkCounter = 0;
        for (HERenderChunk renderChunks : renderChunks) {
            if (renderChunks != null) {
                for (HERenderSubChunk renderSubChunk : renderChunks.renderSubChunks) {
                    if (renderSubChunk.vaoId != GL31.GL_INVALID_INDEX) {
                        subChunkCounter++;
                    }
                }
            }
        }
        return subChunkCounter * vboBuffer.capacity();
    }
}

@SideOnly(Side.CLIENT)
class HEBufferIds {

    public int vaoId;
    public int vboId;
}

@SideOnly(Side.CLIENT)
class HERenderChunk {

    public final HERenderSubChunk[] renderSubChunks;
    public int chunkX = 0;
    public int chunkZ = 0;

    public HERenderChunk() {
        renderSubChunks = new HERenderSubChunk[HE.numChunksY];
        for (int i = 0; i < HE.numChunksY; i++) {
            renderSubChunks[i] = new HERenderSubChunk();
        }
    }
}

@SideOnly(Side.CLIENT)
class HERenderSubChunk {

    public int vaoId = GL31.GL_INVALID_INDEX;
    public int vboId = GL31.GL_INVALID_INDEX;
    public int numWaterBlocks = 0;
}
