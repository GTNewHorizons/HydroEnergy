package com.sinthoras.hydroenergy.mixins.early;

import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.culling.Frustrum;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;
import com.sinthoras.hydroenergy.client.renderer.HETessalator;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {

    @Inject(
            method = "renderWorld",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/RenderGlobal;renderEntities(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/client/renderer/culling/ICamera;F)V",
                    shift = At.Shift.AFTER),
            require = 1)
    private void renderEntities(float p_78471_1_, long p_78471_2_, CallbackInfo ci,
            @Local(name = "frustrum") Frustrum frustrum) {
        HETessalator.render(frustrum);
    }
}
