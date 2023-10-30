package com.sinthoras.hydroenergy;

import com.sinthoras.hydroenergy.hooks.HEHooksClient;
import com.sinthoras.hydroenergy.hooks.HEHooksShared;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerAboutToStartEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppedEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;

@Mod(
        modid = HETags.MODID,
        version = HETags.VERSION,
        name = HETags.MODNAME,
        dependencies = HETags.DEPENDENCIES,
        acceptedMinecraftVersions = "[1.7.10]")
public class HEMod {

    @SidedProxy(
            clientSide = HETags.GROUPNAME + ".hooks.HEHooksClient",
            serverSide = HETags.GROUPNAME + ".hooks.HEHooksShared")
    public static HEHooksShared proxy;

    @Mod.EventHandler
    // preInit "Run before anything else. Read your config, create blocks, items,
    // etc. and register them with the GameRegistry."
    public void fmlLifeCycleEvent(FMLPreInitializationEvent event) {
        HE.debug("Registered sided proxy for: " + (proxy instanceof HEHooksClient ? "Client" : "Dedicated server"));
        HE.debug("preInit()" + event.getModMetadata().name);
        proxy.fmlLifeCycleEvent(event);
    }

    @Mod.EventHandler
    // load "Do your mod setup. Build whatever data structures you care about. Register recipes."
    public void fmlLifeCycleEvent(FMLInitializationEvent event) {
        HE.debug("init()");
        proxy.fmlLifeCycleEvent(event);
    }

    @Mod.EventHandler
    // postInit "Handle interaction with other mods, complete your setup based on this."
    public void fmlLifeCycle(FMLPostInitializationEvent event) {
        HE.debug("postInit()");
        proxy.fmlLifeCycleEvent(event);
    }

    @Mod.EventHandler
    public void fmlLifeCycle(FMLServerAboutToStartEvent event) {
        HE.debug("Server about to start");
        proxy.fmlLifeCycleEvent(event);
    }

    @Mod.EventHandler
    // register server commands in this event handler
    public void fmlLifeCycle(FMLServerStartingEvent event) {
        HE.debug("Server starting");
        proxy.fmlLifeCycleEvent(event);
    }

    @Mod.EventHandler
    public void fmlLifeCycle(FMLServerStartedEvent event) {
        HE.debug("Server started");
        proxy.fmlLifeCycleEvent(event);
    }

    @Mod.EventHandler
    public void fmlLifeCycle(FMLServerStoppingEvent event) {
        HE.debug("Server stopping");
        proxy.fmlLifeCycleEvent(event);
    }

    @Mod.EventHandler
    public void fmlLifeCycle(FMLServerStoppedEvent event) {
        HE.debug("Server stopped");
        proxy.fmlLifeCycleEvent(event);
    }
}
