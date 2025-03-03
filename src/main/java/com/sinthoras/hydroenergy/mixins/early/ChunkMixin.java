package com.sinthoras.hydroenergy.mixins.early;

import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

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
            require = 1,
            locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private void onSetExtBlocklight(CallbackInfo callbackInfo, int maxFilledChunkY, int blockX, int blockZ,
            int lightValue, int blockY, int lightOpacity, ExtendedBlockStorage extendedBlockStorage) {
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
            locals = LocalCapture.CAPTURE_FAILEXCEPTION,
            require = 0,
            expect = 0)
    private void onRelightBlockSetExtBlockLightA(int blockX, int unusedBlockY, int blockZ, CallbackInfo callbackInfo,
            int highestBlockY, int i1, int j1, int k1, int blockY, ExtendedBlockStorage extendedBlockStorage) {
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
            locals = LocalCapture.CAPTURE_FAILEXCEPTION,
            require = 0,
            expect = 0)
    private void onRelightBlockSetExtBlockLightB(int blockX, int unusedBlockY, int blockZ, CallbackInfo callbackInfo,
            int highestBlockY, int i1, int j1, int k1, int blockY) {
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
            locals = LocalCapture.CAPTURE_FAILEXCEPTION,
            require = 0,
            expect = 0)
    private void onRelightBlockSetExtBlockLightC(int blockX, int unusedBlockY, int blockZ, CallbackInfo callbackInfo,
            int highestBlockY, int blockY) {
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
            CallbackInfo callbackInfo) {
        HELightSMPHooks.onLightUpdate((Chunk) ((Object) this), blockX, blockY, blockZ);
    }
}
