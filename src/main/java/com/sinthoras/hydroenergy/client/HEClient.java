package com.sinthoras.hydroenergy.client;

import net.minecraft.client.Minecraft;

import com.sinthoras.hydroenergy.HE;
import com.sinthoras.hydroenergy.config.HEConfig;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class HEClient {

    private static HEDam[] dams = new HEDam[HEConfig.maxDams];

    static {
        for (int waterId = 0; waterId < HEConfig.maxDams; waterId++) {
            dams[waterId] = new HEDam(waterId);
        }
    }

    public static void onWaterUpdate(int waterId, float waterLevel) {
        if (waterId < 0 || waterId >= HEConfig.maxDams) {
            HE.error(HE.ERROR_serverIdsOutOfBounds);
            return;
        }
        dams[waterId].onWaterUpdate(waterLevel);
    }

    public static void onConfigUpdate(int waterId, int blockX, int blockY, int blockZ, HE.DamMode mode, int limitWest,
            int limitDown, int limitNorth, int limitEast, int limitUp, int limitSouth) {
        HE.debug("Received dam config update");
        if (waterId < 0 || waterId >= HEConfig.maxDams) {
            HE.error(HE.ERROR_serverIdsOutOfBounds);
            return;
        }
        dams[waterId].onConfigUpdate(
                blockX,
                blockY,
                blockZ,
                mode,
                limitWest,
                limitDown,
                limitNorth,
                limitEast,
                limitUp,
                limitSouth);
    }

    public static float[] getDebugStatesAsFactors() {
        float[] debugFactors = new float[HEConfig.maxDams];
        for (int waterId = 0; waterId < HEConfig.maxDams; waterId++) {
            debugFactors[waterId] = dams[waterId].renderAsDebug() ? 1.0f : 0.0f;
        }
        return debugFactors;
    }

    public static float[] getAllWaterLevelsForRendering() {
        float[] waterLevels = new float[HEConfig.maxDams];
        for (int waterId = 0; waterId < HEConfig.maxDams; waterId++) {
            waterLevels[waterId] = dams[waterId].getWaterLevelForRendering();
        }
        return waterLevels;
    }

    public static float[] getAllWaterLevelForPhysicsAndLighting() {
        float[] waterLevels = new float[HEConfig.maxDams];
        for (int waterId = 0; waterId < HEConfig.maxDams; waterId++) {
            waterLevels[waterId] = dams[waterId].getWaterLevelForPhysicsAndLighting();
        }
        return waterLevels;
    }

    public static void onSynchronize(int[] blocksX, int[] blocksY, int[] blocksZ, float[] waterLevels,
            HE.DamMode[] modes, int[] limitsWest, int[] limitsDown, int[] limitsNorth, int[] limitsEast, int[] limitsUp,
            int[] limitsSouth, boolean[] enabledTiers) {
        if (HEConfig.maxDams < waterLevels.length) {
            HE.error(HE.ERROR_serverIdsOutOfBounds);
        }
        for (int waterId = 0; waterId < HEConfig.maxDams; waterId++) {
            dams[waterId].onConfigUpdate(
                    blocksX[waterId],
                    blocksY[waterId],
                    blocksZ[waterId],
                    modes[waterId],
                    limitsWest[waterId],
                    limitsDown[waterId],
                    limitsNorth[waterId],
                    limitsEast[waterId],
                    limitsUp[waterId],
                    limitsSouth[waterId]);
            dams[waterId].onWaterUpdate(waterLevels[waterId]);
        }
        HE.debug("Received synchronize packet from server");

        if (HEConfig.enabledTiers.length != enabledTiers.length) {
            Minecraft.getMinecraft().thePlayer.sendChatMessage(HE.WARN_clientConfigMissmatchDetected);
            return;
        }
        for (int tierId = 0; tierId < enabledTiers.length; tierId++) {
            if (HEConfig.enabledTiers[tierId] != enabledTiers[tierId]) {
                Minecraft.getMinecraft().thePlayer.sendChatMessage(HE.WARN_clientConfigMissmatchDetected);
                return;
            }
        }
    }

    public static HEDam getDam(int waterId) {
        return dams[waterId];
    }

    public static void onDisconnect() {
        dams = new HEDam[HEConfig.maxDams];
        for (int waterId = 0; waterId < HEConfig.maxDams; waterId++) {
            dams[waterId] = new HEDam(waterId);
        }
    }

    public static int getWaterId(int blockX, int blockY, int blockZ) {
        for (HEDam dam : dams) {
            if (dam.getBlockX() == blockX && dam.getBlockY() == blockY && dam.getBlockZ() == blockZ) {
                return dam.getWaterId();
            }
        }
        return -1;
    }
}
