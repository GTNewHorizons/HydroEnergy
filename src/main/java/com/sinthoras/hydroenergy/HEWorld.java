package com.sinthoras.hydroenergy;

import net.minecraft.world.World;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

import com.sinthoras.hydroenergy.compat.HECubicChunks;

import cpw.mods.fml.common.Loader;

/** Loaded-only section access. The optional integration is never linked without CubicChunks. */
public final class HEWorld {

    private static final boolean CUBIC_CHUNKS = Loader.isModLoaded("cubicchunks");

    private HEWorld() {}

    public static boolean isCubic(World world) {
        return world != null && CUBIC_CHUNKS && HECubicChunks.isCubic(world);
    }

    public static int minHeight(World world) {
        return isCubic(world) ? HECubicChunks.minHeight(world) : 0;
    }

    public static int maxHeight(World world) {
        return isCubic(world) ? HECubicChunks.maxHeight(world) : 256;
    }

    public static boolean isLoaded(World world, int x, int y, int z) {
        if (y < (minHeight(world) >> 4) || y > ((maxHeight(world) - 1) >> 4)) return false;
        return isCubic(world) ? HECubicChunks.isLoaded(world, x, y, z) : world.getChunkProvider().chunkExists(x, z);
    }

    public static ExtendedBlockStorage getStorage(World world, int x, int y, int z) {
        if (!isLoaded(world, x, y, z)) return null;
        return isCubic(world) ? HECubicChunks.getStorage(world, x, y, z)
                : world.getChunkFromChunkCoords(x, z).getBlockStorageArray()[y];
    }
}
