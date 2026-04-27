package com.sinthoras.hydroenergy.mixins.late;

import static com.sinthoras.hydroenergy.api.HEGetMaterialUtil.getMaterialWrapper;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraftforge.client.event.EntityViewRenderEvent;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import gregtech.common.pollution.PollutionRenderer;

@Mixin(value = PollutionRenderer.class)
public class GT_PollutionRendererMixin {

    // Redirect getMaterial to check for custom water
    @Redirect(
            method = "manipulateColor",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/Block;getMaterial()Lnet/minecraft/block/material/Material;",
                    ordinal = 0),
            require = 1)
    private Material redirectMaterialIsWaterCheck(Block instance, EntityViewRenderEvent.FogColors event) {
        return getMaterialWrapper(instance, event.entity.posY + event.entity.getEyeHeight());
    }
}
