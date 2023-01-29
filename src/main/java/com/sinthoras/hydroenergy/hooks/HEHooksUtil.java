package com.sinthoras.hydroenergy.hooks;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.Vec3;

import com.sinthoras.hydroenergy.blocks.HEWater;
import com.sinthoras.hydroenergy.config.HEConfig;

public class HEHooksUtil {

    public static Block getBlockForActiveRenderInfo(Block block, Vec3 eyePosition) {
        if (block instanceof HEWater) {
            // This constant is so magic i'm gonna die!
            // Without this constant there is a gap between rendered water and all under water effects
            return (((HEWater) block).getWaterLevel() + 0.120f) < eyePosition.yCoord ? Blocks.air : block;
        } else {
            return block;
        }
    }

    public static Block getBlockForWorldAndEntity(Block block, int blockY) {
        if (block instanceof HEWater) {
            return (Math.floor(((HEWater) block).getWaterLevel() - HEConfig.clippingOffset)) < blockY ? Blocks.air
                    : block;
        } else {
            return block;
        }
    }
}
