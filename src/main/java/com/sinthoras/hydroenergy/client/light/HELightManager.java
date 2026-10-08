package com.sinthoras.hydroenergy.client.light;

import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

import com.sinthoras.hydroenergy.HE;
import com.sinthoras.hydroenergy.HESectionPos;
import com.sinthoras.hydroenergy.HEWorld;
import com.sinthoras.hydroenergy.blocks.HEWater;
import com.sinthoras.hydroenergy.client.HEClient;
import com.sinthoras.hydroenergy.config.HEConfig;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class HELightManager {

    private static final float[] waterLevelOfLastUpdate = new float[HEConfig.maxDams];
    private static final long[] timestampsNextUpdate = new long[HEConfig.maxDams];
    private static final Map<HESectionPos, LightSection> sections = new HashMap<>();

    public static void clear() {
        sections.clear();
        Arrays.fill(waterLevelOfLastUpdate, 0);
        Arrays.fill(timestampsNextUpdate, 0);
    }

    public static void onChunkUnload(int x, int z) {
        sections.keySet().removeIf(pos -> pos.x == x && pos.z == z);
    }

    public static void onSectionUnload(int x, int y, int z) {
        sections.remove(new HESectionPos(x, y, z));
    }

    public static void onChunkDataLoad(Chunk chunk) {
        if (HEWorld.isCubic(chunk.worldObj)) return; // Cubes arrive independently of their column.
        for (int y = 0; y < chunk.getBlockStorageArray().length; y++) {
            onSectionDataLoad(chunk.worldObj, chunk.xPosition, y, chunk.zPosition);
        }
    }

    public static void onSectionDataLoad(World world, int x, int y, int z) {
        if (!world.isRemote || world != Minecraft.getMinecraft().theWorld) return;
        HESectionPos pos = new HESectionPos(x, y, z);
        ExtendedBlockStorage storage = HEWorld.getStorage(world, x, y, z);
        LightSection section = null;
        if (storage != null && !storage.isEmpty()) {
            for (int i = 0; i < 4096; i++) {
                Block block = storage.getBlockByExtId(i & 15, i >> 8, (i >> 4) & 15);
                if (block instanceof HEWater) {
                    if (section == null) section = new LightSection();
                    section.set(i, ((HEWater) block).getWaterId());
                }
            }
        }
        if (section == null) sections.remove(pos);
        else {
            sections.put(pos, section);
            patch(world, pos, section);
        }
    }

    public static void onSetBlock(int x, int y, int z, Block block, Block oldBlock) {
        if (!(block instanceof HEWater) && !(oldBlock instanceof HEWater)) return;
        HESectionPos pos = new HESectionPos(x >> 4, y >> 4, z >> 4);
        LightSection section = sections.get(pos);
        int address = ((y & 15) << 8) | ((z & 15) << 4) | (x & 15);
        if (block instanceof HEWater) {
            if (section == null) {
                section = new LightSection();
                sections.put(pos, section);
            }
            section.set(address, ((HEWater) block).getWaterId());
        } else if (section != null) {
            section.water.clear(address);
            if (section.water.isEmpty()) sections.remove(pos);
        }
        if (block instanceof HEWater) onSectionLightUpdate(Minecraft.getMinecraft().theWorld, x, y, z);
    }

    public static void onLightUpdate(Chunk chunk, int x, int y, int z) {
        onSectionLightUpdate(chunk.worldObj, (chunk.xPosition << 4) + (x & 15), y, (chunk.zPosition << 4) + (z & 15));
    }

    public static void onSectionLightUpdate(World world, int x, int y, int z) {
        if (world == null || !world.isRemote || world.provider.hasNoSky) return;
        ExtendedBlockStorage storage = HEWorld.getStorage(world, x >> 4, y >> 4, z >> 4);
        if (storage == null || storage.getSkylightArray() == null) return;
        Block block = storage.getBlockByExtId(x & 15, y & 15, z & 15);
        if (block instanceof HEWater) {
            storage.getSkylightArray().set(x & 15, y & 15, z & 15, light(y, ((HEWater) block).getWaterId()));
        }
    }

    public static void onPreRender(World world, int x, int y, int z) {
        // Packet and tick hooks patch the cache on the client thread; asynchronous renderers only read lighting.
        if (!Minecraft.getMinecraft().func_152345_ab()) return;
        HESectionPos pos = new HESectionPos(x >> 4, y >> 4, z >> 4);
        LightSection section = sections.get(pos);
        if (section != null) patch(world, pos, section);
    }

    public static void onTick() {
        long now = System.currentTimeMillis();
        for (int id = 0; id < HEConfig.maxDams; id++) {
            float level = HEClient.getDam(id).getWaterLevelForPhysicsAndLighting();
            if (Math.abs(waterLevelOfLastUpdate[id] - level) > (0.5f / HE.waterOpacity)
                    && now >= timestampsNextUpdate[id]) {
                timestampsNextUpdate[id] = now;
                triggerLightingUpdate(id, level, waterLevelOfLastUpdate[id]);
                waterLevelOfLastUpdate[id] = level;
            }
        }
    }

    public static void triggerLightingUpdate(int id, float level, float oldLevel) {
        World world = Minecraft.getMinecraft().theWorld;
        if (world == null) return;
        Set<HESectionPos> rerender = new HashSet<>();
        for (Map.Entry<HESectionPos, LightSection> entry : sections.entrySet()) {
            HESectionPos pos = entry.getKey();
            LightSection section = entry.getValue();
            int bottom = pos.y << 4;
            if (bottom > level && bottom > oldLevel) continue;
            if (bottom + 16 + HE.underWaterSkylightDepth < level && bottom + 16 + HE.underWaterSkylightDepth < oldLevel)
                continue;
            if (!section.contains(id) || !HEWorld.isLoaded(world, pos.x, pos.y, pos.z)) continue;
            patch(world, pos, section);
            rerender.add(pos);
            section.addTouchingSections(rerender, pos, id);
        }
        if (Minecraft.getMinecraft().renderGlobal == null) return;
        for (HESectionPos pos : rerender) {
            if (!HEWorld.isLoaded(world, pos.x, pos.y, pos.z)) continue;
            try {
                Minecraft.getMinecraft().renderGlobal.markBlocksForUpdate(
                        pos.x << 4,
                        pos.y << 4,
                        pos.z << 4,
                        (pos.x << 4) + 15,
                        (pos.y << 4) + 15,
                        (pos.z << 4) + 15);
            } catch (NullPointerException ignored) {
                // Vanilla's renderer array may not yet be initialized during a world transition.
            }
            timestampsNextUpdate[id] += HEConfig.minLightUpdateTimePerSubChunk;
        }
    }

    private static int light(int y, int id) {
        float depth = Math.min(y - HEClient.getDam(id).getWaterLevelForPhysicsAndLighting(), 0);
        return Math.max(0, (int) (15 + depth * HE.waterOpacity));
    }

    private static void patch(World world, HESectionPos pos, LightSection section) {
        if (world == null || world.provider.hasNoSky) return;
        ExtendedBlockStorage storage = HEWorld.getStorage(world, pos.x, pos.y, pos.z);
        if (storage == null) return;
        NibbleArray sky = storage.getSkylightArray();
        if (sky == null) {
            sky = new NibbleArray(4096, 4);
            storage.setSkylightArray(sky);
        }
        for (int i = section.water.nextSetBit(0); i >= 0; i = section.water.nextSetBit(i + 1)) {
            sky.set(i & 15, i >> 8, (i >> 4) & 15, light((pos.y << 4) + (i >> 8), section.dams[i]));
        }
    }

    private static final class LightSection {

        private final BitSet water = new BitSet(4096);
        private final byte[] dams = new byte[4096];

        private void set(int address, int id) {
            water.set(address);
            dams[address] = (byte) id;
        }

        private void addTouchingSections(Set<HESectionPos> targets, HESectionPos pos, int id) {
            int faces = 0;
            for (int i = water.nextSetBit(0); i >= 0; i = water.nextSetBit(i + 1)) {
                if (dams[i] != id) continue;
                int x = i & 15, y = i >> 8, z = (i >> 4) & 15;
                if (x == 0) faces |= 1;
                if (x == 15) faces |= 2;
                if (y == 0) faces |= 4;
                if (y == 15) faces |= 8;
                if (z == 0) faces |= 16;
                if (z == 15) faces |= 32;
            }
            if ((faces & 1) != 0) targets.add(new HESectionPos(pos.x - 1, pos.y, pos.z));
            if ((faces & 2) != 0) targets.add(new HESectionPos(pos.x + 1, pos.y, pos.z));
            if ((faces & 4) != 0) targets.add(new HESectionPos(pos.x, pos.y - 1, pos.z));
            if ((faces & 8) != 0) targets.add(new HESectionPos(pos.x, pos.y + 1, pos.z));
            if ((faces & 16) != 0) targets.add(new HESectionPos(pos.x, pos.y, pos.z - 1));
            if ((faces & 32) != 0) targets.add(new HESectionPos(pos.x, pos.y, pos.z + 1));
        }

        private boolean contains(int id) {
            for (int i = water.nextSetBit(0); i >= 0; i = water.nextSetBit(i + 1)) {
                if (dams[i] == id) return true;
            }
            return false;
        }
    }
}
