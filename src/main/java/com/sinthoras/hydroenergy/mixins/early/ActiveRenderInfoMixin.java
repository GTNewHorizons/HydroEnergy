package com.sinthoras.hydroenergy.mixins.early;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.sugar.Local;
import com.sinthoras.hydroenergy.hooks.HEHooksUtil;

@Mixin(ActiveRenderInfo.class)
public class ActiveRenderInfoMixin {

    @Unique
    private static Vec3 he$eyePosition;

    // Grab eye position for subsequent redirect
    @Inject(
            method = "getBlockAtEntityViewpoint",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;getBlock(III)Lnet/minecraft/block/Block;",
                    ordinal = 0),
            require = 1)
    private static void onGetEyePosition(World world, EntityLivingBase entity, float interpolationFactor,
            CallbackInfoReturnable<Block> cir, @Local Vec3 eyePosition) {
        ActiveRenderInfoMixin.he$eyePosition = eyePosition;
    }

    // Redirect getBlock to check for custom water
    @Redirect(
            method = "getBlockAtEntityViewpoint",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;getBlock(III)Lnet/minecraft/block/Block;",
                    ordinal = 0),
            require = 1)
    private static Block redirectGetBlock(World world, int blockX, int blockY, int blockZ) {
        return HEHooksUtil.getBlockForActiveRenderInfo(world.getBlock(blockX, blockY, blockZ), he$eyePosition);
    }
}
