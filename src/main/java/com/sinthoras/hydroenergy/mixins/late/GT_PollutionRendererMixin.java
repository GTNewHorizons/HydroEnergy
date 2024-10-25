package com.sinthoras.hydroenergy.mixins.late;

import static com.sinthoras.hydroenergy.api.HEGetMaterialUtil.getMaterialWrapper;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraftforge.client.event.EntityViewRenderEvent;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import gregtech.common.pollution.PollutionRenderer;

@Mixin(value = PollutionRenderer.class)
public class GT_PollutionRendererMixin {

    private EntityViewRenderEvent.FogColors event;

    // Grab eye position for subsequent getMaterial call
    @Inject(
            method = "manipulateColor",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/Block;getMaterial()Lnet/minecraft/block/material/Material;",
                    shift = At.Shift.BEFORE,
                    ordinal = 0),
            require = 1)
    private void beforeMaterialIsWaterCheck(EntityViewRenderEvent.FogColors event, CallbackInfo callbackInfo) {
        this.event = event;
    }

    // Redirect getMaterial to check for custom water
    @Redirect(
            method = "manipulateColor",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/Block;getMaterial()Lnet/minecraft/block/material/Material;",
                    ordinal = 0),
            require = 1)
    private Material redirectMaterialIsWaterCheck(Block instance) {
        return getMaterialWrapper(instance, event.entity.posY + event.entity.getEyeHeight());
    }
}
