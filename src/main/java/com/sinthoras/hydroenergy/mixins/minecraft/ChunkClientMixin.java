package com.sinthoras.hydroenergy.mixins.minecraft;

import net.minecraft.world.chunk.Chunk;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.sinthoras.hydroenergy.client.light.HELightManager;

@Mixin(Chunk.class)
public class ChunkClientMixin {

    // Capture whole chunk update from server to check for custom water blocks and cache them
    @Inject(
            method = "fillChunk",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/chunk/Chunk;generateHeightMap()V",
                    shift = At.Shift.AFTER),
            require = 1)
    private void onFillChunk(byte[] incomingData, int unusedFlags1, int unusedFlags2, boolean unusedFlag,
            CallbackInfo callbackInfo) {
        HELightManager.onChunkDataLoad((Chunk) ((Object) this));
    }
}
