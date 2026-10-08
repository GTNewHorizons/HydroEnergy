package com.sinthoras.hydroenergy.compat;

import net.minecraft.world.World;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

import com.cardinalstar.cubicchunks.api.ICube;
import com.cardinalstar.cubicchunks.world.ICubicWorld;
import com.cardinalstar.cubicchunks.world.cube.ICubeProvider;

public final class HECubicChunks {

    private HECubicChunks() {}

    public static boolean isCubic(World world) {
        return world instanceof ICubicWorld && world.getChunkProvider() instanceof ICubeProvider;
    }

    public static int minHeight(World world) {
        return ((ICubicWorld) world).getMinHeight();
    }

    public static int maxHeight(World world) {
        return ((ICubicWorld) world).getMaxHeight();
    }

    public static boolean isLoaded(World world, int x, int y, int z) {
        ICube cube = ((ICubicWorld) world).getCubeCache().getLoadedCube(x, y, z);
        return cube != null && cube.isCubeLoaded();
    }

    public static ExtendedBlockStorage getStorage(World world, int x, int y, int z) {
        ICube cube = ((ICubicWorld) world).getCubeCache().getLoadedCube(x, y, z);
        return cube == null ? null : cube.getStorage();
    }
}
