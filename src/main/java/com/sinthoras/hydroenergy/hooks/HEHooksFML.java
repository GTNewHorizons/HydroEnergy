package com.sinthoras.hydroenergy.hooks;

import com.sinthoras.hydroenergy.HE;
import com.sinthoras.hydroenergy.client.HEClient;
import com.sinthoras.hydroenergy.client.light.HELightManager;
import com.sinthoras.hydroenergy.server.HEBlockQueue;
import com.sinthoras.hydroenergy.server.HEServer;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ServerTickEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class HEHooksFML {

    private float waterLevel = 85.0f;
    private int sign = 1;
    private boolean isLoggedIn = false;

    @SubscribeEvent
    public void onEvent(ServerTickEvent event) {
        if (event.phase == ServerTickEvent.Phase.END) {
            HEBlockQueue.onTick();
            if (HE.DEBUGslowFill) {
                waterLevel += sign * 0.005f;
                HEServer.instance.setWaterLevel(0, waterLevel);
            }
            if (waterLevel >= 86.0f) {
                sign = -1;
            }
            if (waterLevel <= 60.0f) {
                sign = 1;
            }
        }
    }

    @SubscribeEvent
    public void onEvent(PlayerLoggedInEvent event) {
        HEServer.instance.synchronizeClient(event);
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onEvent(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        isLoggedIn = true;
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onEvent(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        HEClient.onDisconnect();
        isLoggedIn = false;
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onEvent(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && isLoggedIn) {
            HELightManager.onTick();
        }
    }
}
