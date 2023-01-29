package com.sinthoras.hydroenergy.hooks;

import com.sinthoras.hydroenergy.HE;
import com.sinthoras.hydroenergy.client.renderer.HEProgram;
import com.sinthoras.hydroenergy.client.renderer.HEWaterRenderer;

import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerAboutToStartEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppedEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;

public class HEHooksClient extends HEHooksShared {

    @Override
    // load "Do your mod setup. Build whatever data structures you care about. Register recipes."
    public void fmlLifeCycleEvent(FMLPreInitializationEvent event) {
        super.fmlLifeCycleEvent(event);

        RenderingRegistry.registerBlockHandler(HEWaterRenderer.instance);
    }

    @Override
    // load "Do your mod setup. Build whatever data structures you care about. Register recipes."
    public void fmlLifeCycleEvent(FMLInitializationEvent event) {
        super.fmlLifeCycleEvent(event);
        HE.logicalClientLoaded = true;

        HEProgram.init();
    }

    @Override
    // postInit "Handle interaction with other mods, complete your setup based on this."
    public void fmlLifeCycleEvent(FMLPostInitializationEvent event) {
        super.fmlLifeCycleEvent(event);
    }

    @Override
    public void fmlLifeCycleEvent(FMLServerAboutToStartEvent event) {
        super.fmlLifeCycleEvent(event);
    }

    @Override
    public void fmlLifeCycleEvent(FMLServerStartingEvent event) {
        super.fmlLifeCycleEvent(event);
    }

    @Override
    public void fmlLifeCycleEvent(FMLServerStartedEvent event) {
        super.fmlLifeCycleEvent(event);
    }

    @Override
    public void fmlLifeCycleEvent(FMLServerStoppingEvent event) {
        super.fmlLifeCycleEvent(event);
    }

    @Override
    public void fmlLifeCycleEvent(FMLServerStoppedEvent event) {
        super.fmlLifeCycleEvent(event);
    }
}
