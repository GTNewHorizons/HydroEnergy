package com.sinthoras.hydroenergy.mixins.early;

import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.chunk.Chunk;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;
import com.sinthoras.hydroenergy.client.light.HELightSMPHooks;

@Mixin(value = Chunk.class, priority = 1100)
public class ChunkMixin {

    // Correct vanilla light calculation if necessary
    @Inject(
            method = "generateSkylightMap",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/chunk/storage/ExtendedBlockStorage;setExtSkylightValue(IIII)V",
                    shift = At.Shift.AFTER),
            require = 1)
    private void onSetExtBlocklight(CallbackInfo ci, @Local(ordinal = 1) int blockX, @Local(ordinal = 2) int blockZ,
            @Local(ordinal = 4) int blockY) {
        HELightSMPHooks.onLightUpdate((Chunk) ((Object) this), blockX, blockY, blockZ);
    }

    // Correct vanilla light calculation if necessary
    @Inject(
            method = "relightBlock",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/chunk/storage/ExtendedBlockStorage;setExtSkylightValue(IIII)V",
                    shift = At.Shift.AFTER,
                    ordinal = 0),
            require = 0,
            expect = 0)
    private void onRelightBlockSetExtBlockLightA(int blockX, int unusedBlockY, int blockZ, CallbackInfo ci,
            @Local(ordinal = 7) int blockY) {
        HELightSMPHooks.onLightUpdate((Chunk) ((Object) this), blockX, blockY, blockZ);
    }

    // Correct vanilla light calculation if necessary
    @Inject(
            method = "relightBlock",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/chunk/storage/ExtendedBlockStorage;setExtSkylightValue(IIII)V",
                    shift = At.Shift.AFTER,
                    ordinal = 1),
            require = 0,
            expect = 0)
    private void onRelightBlockSetExtBlockLightB(int blockX, int unusedBlockY, int blockZ, CallbackInfo ci,
            @Local(ordinal = 7) int blockY) {
        HELightSMPHooks.onLightUpdate((Chunk) ((Object) this), blockX, blockY, blockZ);
    }

    // Correct vanilla light calculation if necessary
    @Inject(
            method = "relightBlock",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/chunk/storage/ExtendedBlockStorage;setExtSkylightValue(IIII)V",
                    shift = At.Shift.AFTER,
                    ordinal = 2),
            require = 0,
            expect = 0)
    private void onRelightBlockSetExtBlockLightC(int blockX, int unusedBlockY, int blockZ, CallbackInfo ci,
            @Local(ordinal = 4) int blockY) {
        HELightSMPHooks.onLightUpdate((Chunk) ((Object) this), blockX, blockY, blockZ);
    }

    // Correct vanilla light calculation if necessary
    @Inject(
            method = "setLightValue",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/chunk/storage/ExtendedBlockStorage;setExtSkylightValue(IIII)V",
                    shift = At.Shift.AFTER),
            require = 1)
    private void onSetLightValue(EnumSkyBlock enumSkyBlock, int blockX, int blockY, int blockZ, int lightValue,
            CallbackInfo ci) {
        HELightSMPHooks.onLightUpdate((Chunk) ((Object) this), blockX, blockY, blockZ);
    }
}
