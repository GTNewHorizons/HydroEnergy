package com.sinthoras.hydroenergy.hooks;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldEvent;

import com.sinthoras.hydroenergy.client.light.HELightManager;
import com.sinthoras.hydroenergy.client.renderer.HEProgram;
import com.sinthoras.hydroenergy.client.renderer.HETessalator;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class HEHooksEVENT_BUS {

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onEvent(RenderWorldEvent.Pre event) {
        HELightManager
                .onPreRender(event.renderer.worldObj, event.renderer.posX, event.renderer.posY, event.renderer.posZ);
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onEvent(RenderWorldEvent.Post event) {
        HETessalator.onPostRender(event.renderer.posX, event.renderer.posY, event.renderer.posZ);
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onEvent(RenderGameOverlayEvent.Text event) {
        if (Minecraft.getMinecraft().gameSettings.showDebugInfo) {
            event.right.add("HydroEnergy GPU RAM: " + (HETessalator.getGpuMemoryUsage() >> 20) + "MB"); // Byte / 1024 /
                                                                                                        // 1024
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEvent(EntityViewRenderEvent.FogColors event) {
        HEProgram.setFogColor(event.red, event.green, event.blue);
    }
}
