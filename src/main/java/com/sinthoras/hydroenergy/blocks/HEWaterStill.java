package com.sinthoras.hydroenergy.blocks;

import net.minecraft.block.Block;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import com.sinthoras.hydroenergy.client.renderer.HEWaterRenderer;
import com.sinthoras.hydroenergy.server.HEBlockQueue;
import com.sinthoras.hydroenergy.server.HEServer;

public class HEWaterStill extends HEWater {

    public HEWaterStill(int waterId) {
        super(waterId);
        setTickRandomly(false);
    }

    @Override
    public int getRenderType() {
        return HEWaterRenderer.instance.getRenderId();
    }

    @Override
    public boolean shouldSideBeRendered(IBlockAccess world, int blockX, int blockY, int blockZ, int side) {
        Block block = world.getBlock(blockX, blockY, blockZ);
        if (block != this) {
            return !block.isOpaqueCube();
        }
        return false;
    }

    @Override
    public void onNeighborBlockChange(World world, int blockX, int blockY, int blockZ, Block block) {
        spread(world, blockX, blockY, blockZ);
    }

    @Override
    public void onBlockAdded(World world, int blockX, int blockY, int blockZ) {}

    private void spread(World world, int blockX, int blockY, int blockZ) {
        final int waterId = getWaterId();
        boolean canSpread = HEServer.instance.canSpread(waterId);
        if (canSpread && blockY < HEServer.instance.getWaterLimitUp(waterId)) {
            HEBlockQueue.enqueueBlock(world, blockX, blockY + 1, blockZ, waterId);
        }

        if (canSpread && blockY > HEServer.instance.getWaterLimitDown(waterId)) {
            HEBlockQueue.enqueueBlock(world, blockX, blockY - 1, blockZ, waterId);
        }

        if (canSpread && blockX < HEServer.instance.getWaterLimitEast(waterId)) {
            HEBlockQueue.enqueueBlock(world, blockX + 1, blockY, blockZ, waterId);
        }

        if (canSpread && blockX > HEServer.instance.getWaterLimitWest(waterId)) {
            HEBlockQueue.enqueueBlock(world, blockX - 1, blockY, blockZ, waterId);
        }

        if (canSpread && blockZ < HEServer.instance.getWaterLimitSouth(waterId)) {
            HEBlockQueue.enqueueBlock(world, blockX, blockY, blockZ + 1, waterId);
        }

        if (canSpread && blockZ > HEServer.instance.getWaterLimitNorth(waterId)) {
            HEBlockQueue.enqueueBlock(world, blockX, blockY, blockZ - 1, waterId);
        }

        if (!canSpread || HEServer.instance.isBlockOutOfBounds(waterId, blockX, blockY, blockZ)) {
            HEBlockQueue.enqueueBlock(world, blockX, blockY, blockZ, waterId);
        }
    }
}
