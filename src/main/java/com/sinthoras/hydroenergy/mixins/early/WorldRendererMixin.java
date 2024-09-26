package com.sinthoras.hydroenergy.mixins.early;

import net.minecraft.client.renderer.WorldRenderer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.sinthoras.hydroenergy.client.renderer.HETessalator;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

    @Shadow
    public int posX;

    @Shadow
    public int posY;

    @Shadow
    public int posZ;

    // Update render cache about changes in rendered chunks
    @Inject(method = "setPosition", at = @At("HEAD"), locals = LocalCapture.CAPTURE_FAILEXCEPTION, require = 1)
    private void onSetPosition(int blockX, int blockY, int blockZ, CallbackInfo callbackInfo) {
        if (blockX != posX || blockY != posY || blockZ != posZ) {
            HETessalator.onRenderChunkUpdate(posX, posZ, blockX, blockY, blockZ);
        }
    }
}
