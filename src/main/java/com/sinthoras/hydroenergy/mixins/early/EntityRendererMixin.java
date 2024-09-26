package com.sinthoras.hydroenergy.mixins.early;

import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.EntityLivingBase;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.sinthoras.hydroenergy.client.renderer.HETessalator;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {

    // TODO: Simple Inject should be enough. But I remeber for fastcraft/optifine issues so i'll copy the ASM injection
    // first
    // Inject render call for lakes
    @Redirect(
            method = "renderWorld",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/RenderGlobal;renderEntities(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/client/renderer/culling/ICamera;F)V"),
            require = 1)
    private void redirectRenderEntities(RenderGlobal renderGlobal, EntityLivingBase entitylivingbase, ICamera frustrum,
            float p_78471_1_) {
        renderGlobal.renderEntities(entitylivingbase, frustrum, p_78471_1_);
        HETessalator.render(frustrum);
    }
}
