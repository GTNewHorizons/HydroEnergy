package com.sinthoras.hydroenergy.mixins.early;

import net.minecraft.block.Block;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.sinthoras.hydroenergy.hooks.HEHooksUtil;

@Mixin(value = World.class)
public class WorldMixin {

    // Redirect getBlock to check for custom water
    @Redirect(
            method = "handleMaterialAcceleration",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getBlock(III)Lnet/minecraft/block/Block;"),
            require = 1)
    private Block onHandleMaterialAccelerationGetBlock(World world, int blockX, int blockY, int blockZ) {
        return HEHooksUtil.getBlockForWorldAndEntity(world.getBlock(blockX, blockY, blockZ), blockY);
    }

    // Redirect getBlock to check for custom water
    @Redirect(
            method = "isAnyLiquid",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getBlock(III)Lnet/minecraft/block/Block;"),
            require = 1)
    private Block onIsAnyLiquidGetBlock(World world, int blockX, int blockY, int blockZ) {
        return HEHooksUtil.getBlockForWorldAndEntity(world.getBlock(blockX, blockY, blockZ), blockY);
    }
}
