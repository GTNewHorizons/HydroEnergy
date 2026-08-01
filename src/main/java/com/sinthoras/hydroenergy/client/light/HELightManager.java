package com.sinthoras.hydroenergy.client.light;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Deque;
import java.util.HashMap;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

import com.sinthoras.hydroenergy.HE;
import com.sinthoras.hydroenergy.HEUtil;
import com.sinthoras.hydroenergy.blocks.HEWater;
import com.sinthoras.hydroenergy.client.HEClient;
import com.sinthoras.hydroenergy.config.HEConfig;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class HELightManager {

    private static final float[] waterLevelOfLastUpdate = new float[HEConfig.maxDams];
    private static final long[] timestampsNextUpdate = new long[HEConfig.maxDams];

    private static final HashMap<Long, HELightChunk> chunks = new HashMap<Long, HELightChunk>();
    // Small bounded pool of recycled HELightChunks. A single slot was enough to save most of
    // the allocations, but a tiny queue keeps the reuse rate up when several chunks flip at
    // the same time. Chunks that do not fit in the pool are simply garbage collected.
    private static final int maxAvailableBuffers = 16;
    private static final Deque<HELightChunk> availableBuffers = new ArrayDeque<>();

    public static void onChunkUnload(int chunkX, int chunkZ) {
        long key = HEUtil.chunkCoordsToKey(chunkX, chunkZ);
        HELightChunk lightChunk = chunks.remove(key);
        if (lightChunk != null) {
            recycle(lightChunk);
        }
    }

    public static void onChunkDataLoad(Chunk chunk) {
        int chunkX = chunk.xPosition;
        int chunkZ = chunk.zPosition;
        long key = HEUtil.chunkCoordsToKey(chunkX, chunkZ);

        HELightChunk lightChunk = chunks.remove(key);
        if (lightChunk == null) {
            lightChunk = getBuffer();
        }
        lightChunk.reset();

        lightChunk.parseChunk(chunk);

        if (!lightChunk.hasWater()) {
            recycle(lightChunk);
            return;
        }

        chunks.put(key, lightChunk);

        for (int chunkY = 0; chunkY < HE.numChunksY; chunkY++) {
            lightChunk.patchSubChunk(chunk, chunkY);
            if (lightChunk.hasWaterInSubchunk(HEUtil.chunkYToFlag(chunkY))) {
                markChunkForRerender(Minecraft.getMinecraft().renderGlobal, chunkX, chunkY, chunkZ);
            }
        }
    }

    public static void onSetBlock(int blockX, int blockY, int blockZ, Block block, Block oldBlock) {
        boolean isWater = block instanceof HEWater;
        boolean wasWater = oldBlock instanceof HEWater;
        if (!isWater && !wasWater) {
            return;
        }

        int chunkX = HEUtil.coordBlockToChunk(blockX);
        int chunkZ = HEUtil.coordBlockToChunk(blockZ);
        long key = HEUtil.chunkCoordsToKey(chunkX, chunkZ);
        HELightChunk lightChunk = chunks.get(key);

        if (wasWater) {
            if (lightChunk != null) {
                lightChunk.removeWaterBlock(blockX, blockY, blockZ, ((HEWater) oldBlock).getWaterId());
                if (!lightChunk.hasWater()) {
                    chunks.remove(key);
                    recycle(lightChunk);
                    lightChunk = null;
                }
            }
        }

        if (isWater) {
            if (lightChunk == null) {
                lightChunk = getBuffer();
                chunks.put(key, lightChunk);
            }
            lightChunk.addWaterBlock(blockX, blockY, blockZ, ((HEWater) block).getWaterId());
        }
    }

    public static void onLightUpdate(Chunk chunk, int blockX, int blockY, int blockZ) {
        if (chunk.getBlock(blockX, blockY, blockZ) instanceof HEWater) {
            long key = HEUtil.chunkCoordsToKey(chunk.xPosition, chunk.zPosition);
            HELightChunk lightChunk = chunks.get(key);
            if (lightChunk != null) {
                lightChunk.patchBlock(chunk, blockX, blockY, blockZ);
            }
        }
    }

    public static void onPreRender(World world, int blockX, int blockY, int blockZ) {
        int chunkX = HEUtil.coordBlockToChunk(blockX);
        int chunkY = HEUtil.coordBlockToChunk(blockY);
        int chunkZ = HEUtil.coordBlockToChunk(blockZ);
        long key = HEUtil.chunkCoordsToKey(chunkX, chunkZ);
        HELightChunk lightChunk = chunks.get(key);
        if (lightChunk != null) {
            lightChunk.patchSubChunk(world.getChunkFromChunkCoords(chunkX, chunkZ), chunkY);
        }
    }

    private static HELightChunk getBuffer() {
        HELightChunk lightChunk = availableBuffers.pollFirst();
        return lightChunk == null ? new HELightChunk() : lightChunk;
    }

    private static void recycle(HELightChunk lightChunk) {
        if (availableBuffers.size() >= maxAvailableBuffers) {
            return;
        }
        lightChunk.reset();
        availableBuffers.addFirst(lightChunk);
    }

    // If any waterLevel changed enough and the last update was long enough ago chunks will be redrawn.
    public static void onTick() {
        final long currentTime = System.currentTimeMillis();
        for (int waterId = 0; waterId < HEConfig.maxDams; waterId++) {
            final float currentWaterLevel = HEClient.getDam(waterId).getWaterLevelForPhysicsAndLighting();
            if (Math.abs(waterLevelOfLastUpdate[waterId] - currentWaterLevel) > (0.5f / HE.waterOpacity)
                    && currentTime - timestampsNextUpdate[waterId] >= 0) {
                timestampsNextUpdate[waterId] = currentTime;
                triggerLightingUpdate(waterId, currentWaterLevel, waterLevelOfLastUpdate[waterId]);
                waterLevelOfLastUpdate[waterId] = currentWaterLevel;
            }
        }
    }

    // Crawls through all chunks and if the chunk has water from the right dam and is impacted by the waterLevel change
    // it queues them up for redrawing. It also redraws neighboring non-water chunks if they touch a waterBlock and
    // therefore are impaced by the light value change.
    public static void triggerLightingUpdate(int waterId, float waterLevel, float oldWaterLevel) {
        RenderGlobal renderGlobal = Minecraft.getMinecraft().renderGlobal;
        for (long key : chunks.keySet()) {
            HELightChunk chunk = chunks.get(key);
            if (chunk.hasUpdateForDam(waterId)) {
                int chunkX = (int) (key >> 32);
                int chunkZ = (int) key;

                long keyWest = HEUtil.chunkCoordsToKey(chunkX - 1, chunkZ);
                long keyNorth = HEUtil.chunkCoordsToKey(chunkX, chunkZ - 1);
                long keyEast = HEUtil.chunkCoordsToKey(chunkX + 1, chunkZ);
                long keySouth = HEUtil.chunkCoordsToKey(chunkX, chunkZ + 1);
                HELightChunk neighborChunkWest = chunks.get(keyWest);
                HELightChunk neighborChunkNorth = chunks.get(keyNorth);
                HELightChunk neighborChunkEast = chunks.get(keyEast);
                HELightChunk neighborChunkSouth = chunks.get(keySouth);

                for (int chunkY = 0; chunkY < HE.numChunksY; chunkY++) {
                    int blockY = HEUtil.coordChunkToBlock(chunkY);
                    boolean chunkTooLow = blockY + HE.chunkHeight + HE.underWaterSkylightDepth < waterLevel
                            && blockY + HE.chunkHeight + HE.underWaterSkylightDepth < oldWaterLevel;
                    boolean chunkTooHigh = blockY > waterLevel && blockY > oldWaterLevel;
                    if (!chunkTooLow && !chunkTooHigh) {
                        short flagChunkY = HEUtil.chunkYToFlag(chunkY);
                        if (chunk.hasWaterInSubchunk(flagChunkY)) {
                            chunk.subChunkMustBePatched(flagChunkY);
                            markChunkForRerender(renderGlobal, chunkX, chunkY, chunkZ);
                            timestampsNextUpdate[waterId] += HEConfig.minLightUpdateTimePerSubChunk;

                            // Handle neighbors that don't have water, but touch it
                            // Technically, a chunk like this could be surrounded by chunks with water and receive
                            // multiple
                            // updates, but this scenario is rather unlikely and therefore, not worth checking for.
                            if ((neighborChunkWest == null || !neighborChunkWest.hasWaterInSubchunk(flagChunkY))
                                    && chunk.requiresPatchingWest(flagChunkY)) {
                                markChunkForRerender(renderGlobal, chunkX - 1, chunkY, chunkZ);
                                timestampsNextUpdate[waterId] += HEConfig.minLightUpdateTimePerSubChunk;
                            }
                            if ((neighborChunkNorth == null || !neighborChunkNorth.hasWaterInSubchunk(flagChunkY))
                                    && chunk.requiresPatchingNorth(flagChunkY)) {
                                markChunkForRerender(renderGlobal, chunkX, chunkY, chunkZ - 1);
                                timestampsNextUpdate[waterId] += HEConfig.minLightUpdateTimePerSubChunk;
                            }
                            if ((neighborChunkEast == null || !neighborChunkEast.hasWaterInSubchunk(flagChunkY))
                                    && chunk.requiresPatchingEast(flagChunkY)) {
                                markChunkForRerender(renderGlobal, chunkX + 1, chunkY, chunkZ);
                                timestampsNextUpdate[waterId] += HEConfig.minLightUpdateTimePerSubChunk;
                            }
                            if ((neighborChunkSouth == null || !neighborChunkSouth.hasWaterInSubchunk(flagChunkY))
                                    && chunk.requiresPatchingSouth(flagChunkY)) {
                                markChunkForRerender(renderGlobal, chunkX, chunkY, chunkZ + 1);
                                timestampsNextUpdate[waterId] += HEConfig.minLightUpdateTimePerSubChunk;
                            }
                        }
                    }
                }
            }
        }
    }

    private static void markChunkForRerender(RenderGlobal renderGlobal, int chunkX, int chunkY, int chunkZ) {
        if (renderGlobal == null) {
            return;
        }
        try {
            int blockX = HEUtil.coordChunkToBlock(chunkX);
            int blockY = HEUtil.coordChunkToBlock(chunkY);
            int blockZ = HEUtil.coordChunkToBlock(chunkZ);
            renderGlobal.markBlocksForUpdate(
                    blockX,
                    blockY,
                    blockZ,
                    blockX + HE.chunkWidth - 1,
                    blockY + HE.chunkHeight - 1,
                    blockZ + HE.chunkDepth - 1);
        } catch (NullPointerException e) {
            // RenderGlobal not fully initialized yet (worldRenderers is null), ignore
        }
    }
}

@SideOnly(Side.CLIENT)
class HELightChunk {

    public BitSet[] lightFlags;
    public short subChunkHasWaterFlags;
    public short requiresPatching;
    public short neighborRequiresPatchingWest;
    public short neighborRequiresPatchingNorth;
    public short neighborRequiresPatchingEast;
    public short neighborRequiresPatchingSouth;
    private int damFlags;
    private final int[] damBlockCounts = new int[HEConfig.maxDams];
    // Holds corresponding waterId for X/Z combination. I don't expect people to stack
    // multiple on top of each other. If they do the light calculation will be incorrect.
    // Acceptable to save quite some RAM.
    public int[][] waterIds;

    public HELightChunk() {
        lightFlags = new BitSet[HE.numChunksY];
        for (int chunkY = 0; chunkY < HE.numChunksY; chunkY++) {
            lightFlags[chunkY] = new BitSet(HE.blockPerSubChunk);
        }

        waterIds = new int[HE.chunkWidth][HE.chunkDepth];
        subChunkHasWaterFlags = 0;
        requiresPatching = 0;
        damFlags = 0;

        // If a block at the chunk border is from water it means that the neighbors need to be handled as well
        neighborRequiresPatchingWest = 0;
        neighborRequiresPatchingNorth = 0;
        neighborRequiresPatchingEast = 0;
        neighborRequiresPatchingSouth = 0;
    }

    public void reset() {
        for (int chunkY = 0; chunkY < HE.numChunksY; chunkY++) {
            lightFlags[chunkY].clear();
        }
        subChunkHasWaterFlags = 0;
        requiresPatching = 0;
        damFlags = 0;
        Arrays.fill(damBlockCounts, 0);
        neighborRequiresPatchingWest = 0;
        neighborRequiresPatchingNorth = 0;
        neighborRequiresPatchingEast = 0;
        neighborRequiresPatchingSouth = 0;
        // waterIds does not need to be reset since it is only accessed
        // whenever data is found and for that to happen there must be a
        // valid value in it again
    }

    // This method checks for each block in the chunk what block it is
    // with the logic from ExtendedBlockStorage.getBlockByExtId(blockX, blockY, blockZ)
    // and a waterId LUT (getWaterIdFromBlockId)
    public void parseChunk(Chunk chunk) {
        ExtendedBlockStorage[] chunkStorage = chunk.getBlockStorageArray();
        for (int chunkY = 0; chunkY < HE.numChunksY; chunkY++) {
            ExtendedBlockStorage subChunkStorage = chunkStorage[chunkY];
            if (subChunkStorage != null) {
                BitSet flags = lightFlags[chunkY];

                int[] bucketsBlockX = new int[HE.chunkWidth];
                int[] bucketsBlockZ = new int[HE.chunkDepth];
                short flagChunkY = HEUtil.chunkYToFlag(chunkY);

                for (int blockX = 0; blockX < HE.chunkWidth; blockX++) {
                    for (int blockY = 0; blockY < HE.chunkHeight; blockY++) {
                        for (int blockZ = 0; blockZ < HE.chunkDepth; blockZ++) {
                            Block block = subChunkStorage.getBlockByExtId(blockX, blockY, blockZ);
                            if (block instanceof HEWater) {
                                int waterId = ((HEWater) block).getWaterId();
                                bucketsBlockX[blockX]++;
                                bucketsBlockZ[blockZ]++;
                                flags.set((blockX << 8) | (blockY << 4) | blockZ);
                                waterIds[blockX][blockZ] = waterId;
                                this.subChunkHasWaterFlags |= flagChunkY;
                                damBlockCounts[waterId]++;
                                damFlags |= 1 << waterId;
                            }
                        }
                    }
                }

                neighborRequiresPatchingWest |= bucketsBlockX[0] > 0 ? flagChunkY : 0;
                neighborRequiresPatchingNorth |= bucketsBlockZ[0] > 0 ? flagChunkY : 0;
                neighborRequiresPatchingEast |= bucketsBlockX[15] > 0 ? flagChunkY : 0;
                neighborRequiresPatchingSouth |= bucketsBlockZ[15] > 0 ? flagChunkY : 0;
            }
        }
        requiresPatching = subChunkHasWaterFlags;
    }

    public void removeWaterBlock(int blockX, int blockY, int blockZ, int waterId) {
        int chunkY = blockY >> 4;
        BitSet flags = lightFlags[chunkY];
        blockX = blockX & 15;
        blockY = blockY & 15;
        blockZ = blockZ & 15;
        flags.clear((blockX << 8) | (blockY << 4) | blockZ);
        if (flags.isEmpty()) {
            subChunkHasWaterFlags &= ~HEUtil.chunkYToFlag(chunkY);
        }
        if (damBlockCounts[waterId] > 0) {
            damBlockCounts[waterId]--;
        }
        if (damBlockCounts[waterId] == 0) {
            damFlags &= ~(1 << waterId);
        }
    }

    public void addWaterBlock(int blockX, int blockY, int blockZ, int waterId) {
        int chunkY = HEUtil.coordBlockToChunk(blockY);
        BitSet flags = lightFlags[chunkY];
        this.subChunkHasWaterFlags |= HEUtil.chunkYToFlag(chunkY);
        blockX = blockX & 15;
        blockY = blockY & 15;
        blockZ = blockZ & 15;
        flags.set((blockX << 8) | (blockY << 4) | blockZ);
        waterIds[blockX][blockZ] = waterId;
        if (damBlockCounts[waterId]++ == 0) {
            damFlags |= 1 << waterId;
        }
    }

    public boolean hasWater() {
        return subChunkHasWaterFlags != 0;
    }

    public void patchBlock(Chunk chunk, int blockX, int blockY, int blockZ) {
        int chunkY = HEUtil.coordBlockToChunk(blockY);
        int waterId = waterIds[blockX][blockZ];
        float blockDiff = Math.min(blockY - HEClient.getDam(waterId).getWaterLevelForPhysicsAndLighting(), 0);
        int lightVal = (int) (15 + blockDiff * HE.waterOpacity);
        lightVal = Math.max(lightVal, 0);
        ExtendedBlockStorage[] chunkStorage = chunk.getBlockStorageArray();
        NibbleArray skyLightArray = chunkStorage[chunkY].getSkylightArray();
        if (skyLightArray == null) {
            skyLightArray = new NibbleArray(HE.blockPerSubChunk, 4);
            chunkStorage[chunkY].setSkylightArray(skyLightArray);
        }
        skyLightArray.set(blockX, blockY & 15, blockZ, lightVal);
    }

    public void patchSubChunk(Chunk chunk, int chunkY) {
        short flagChunkY = HEUtil.chunkYToFlag(chunkY);
        if (hasWaterInSubchunk(flagChunkY) && subChunkRequiresPatching(flagChunkY)) {
            float[] waterLevels = HEClient.getAllWaterLevelForPhysicsAndLighting();
            BitSet flags = lightFlags[chunkY];
            ExtendedBlockStorage[] chunkStorage = chunk.getBlockStorageArray();
            NibbleArray skyLightArray = chunkStorage[chunkY].getSkylightArray();
            if (skyLightArray == null) {
                skyLightArray = new NibbleArray(HE.blockPerSubChunk, 4);
                chunkStorage[chunkY].setSkylightArray(skyLightArray);
            }
            for (int linearCoord = flags.nextSetBit(0); linearCoord
                    != -1; linearCoord = flags.nextSetBit(linearCoord + 1)) {
                int blockX = linearCoord >> 8;
                int blockY = (linearCoord >> 4) & 15;
                int blockZ = linearCoord & 15;
                int waterId = waterIds[blockX][blockZ];
                float blockDiff = Math.min(HEUtil.coordChunkToBlock(chunkY) + blockY - waterLevels[waterId], 0);
                int lightVal = (int) (15 + blockDiff * HE.waterOpacity);
                lightVal = Math.max(lightVal, 0);
                skyLightArray.set(blockX, blockY, blockZ, lightVal);
            }
            subChunkWasPatched(flagChunkY);
        }
    }

    private void subChunkWasPatched(int flagChunkY) {
        requiresPatching &= ~flagChunkY;
    }

    public void subChunkMustBePatched(int flagChunkY) {
        requiresPatching |= flagChunkY;
    }

    private boolean subChunkRequiresPatching(int flagChunkY) {
        return (requiresPatching & flagChunkY) > 0;
    }

    public boolean hasWaterInSubchunk(int flagChunkY) {
        return (subChunkHasWaterFlags & flagChunkY) > 0;
    }

    public boolean hasUpdateForDam(int waterId) {
        return (damFlags & (1 << waterId)) != 0;
    }

    public boolean requiresPatchingWest(int flagChunkY) {
        return (neighborRequiresPatchingWest & flagChunkY) > 0;
    }

    public boolean requiresPatchingNorth(int flagChunkY) {
        return (neighborRequiresPatchingNorth & flagChunkY) > 0;
    }

    public boolean requiresPatchingEast(int flagChunkY) {
        return (neighborRequiresPatchingEast & flagChunkY) > 0;
    }

    public boolean requiresPatchingSouth(int flagChunkY) {
        return (neighborRequiresPatchingSouth & flagChunkY) > 0;
    }
}
