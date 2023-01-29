package com.sinthoras.hydroenergy.client;

import com.sinthoras.hydroenergy.HE;
import com.sinthoras.hydroenergy.HEUtil;
import com.sinthoras.hydroenergy.config.HEConfig;
import com.sinthoras.hydroenergy.network.packet.HEPacketConfigRequest;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class HEDam {

    private int waterId;
    private float waterLevel;
    private HE.DamMode mode;
    public int limitUp;
    public int limitDown;
    public int limitEast;
    public int limitWest;
    public int limitSouth;
    public int limitNorth;
    private int blockX;
    private int blockY;
    private int blockZ;

    private HE.DamMode pendingMode;
    private int pendingLimitWest;
    private int pendingLimitDown;
    private int pendingLimitNorth;
    private int pendingLimitEast;
    private int pendingLimitUp;
    private int pendingLimitSouth;

    public HEDam(int waterId) {
        this.waterId = waterId;
    }

    public void onConfigUpdate(int blockX, int blockY, int blockZ, HE.DamMode mode, int limitWest, int limitDown,
            int limitNorth, int limitEast, int limitUp, int limitSouth) {
        this.blockX = blockX;
        this.blockY = blockY;
        this.blockZ = blockZ;

        this.mode = mode;

        this.limitWest = limitWest;
        this.limitDown = limitDown;
        this.limitNorth = limitNorth;
        this.limitEast = limitEast;
        this.limitUp = limitUp;
        this.limitSouth = limitSouth;

        pendingMode = mode;

        pendingLimitWest = limitWest;
        pendingLimitDown = limitDown;
        pendingLimitNorth = limitNorth;
        pendingLimitEast = limitEast;
        pendingLimitUp = limitUp;
        pendingLimitSouth = limitSouth;

        verifyChanges();
    }

    // Clap change requests to config limits
    private void verifyChanges() {
        pendingLimitWest = blockX - HEUtil.clamp(blockX - pendingLimitWest, 0, HEConfig.maxWaterSpreadWest);
        pendingLimitDown = blockY - HEUtil.clamp(blockY - pendingLimitDown, 0, HEConfig.maxWaterSpreadDown);
        pendingLimitNorth = blockZ - HEUtil.clamp(blockZ - pendingLimitNorth, 0, HEConfig.maxWaterSpreadNorth);
        pendingLimitEast = blockX + HEUtil.clamp(pendingLimitEast - blockX, 0, HEConfig.maxWaterSpreadEast);
        pendingLimitUp = blockY + HEUtil.clamp(pendingLimitUp - blockY, 0, HEConfig.maxWaterSpreadUp);
        pendingLimitSouth = blockZ + HEUtil.clamp(pendingLimitSouth - blockZ, 0, HEConfig.maxWaterSpreadSouth);
    }

    public void onWaterUpdate(float waterLevel) {
        this.waterLevel = waterLevel;
    }

    public boolean renderAsDebug() {
        return mode != HE.DamMode.SPREAD;
    }

    public float getWaterLevelForPhysicsAndLighting() {
        if (mode == HE.DamMode.SPREAD) {
            return waterLevel;
        } else {
            return 0.0f;
        }
    }

    public float getWaterLevelForRendering() {
        if (mode == HE.DamMode.SPREAD) {
            return waterLevel;
        } else {
            return 256.0f;
        }
    }

    public void applyChanges() {
        HE.network.sendToServer(
                new HEPacketConfigRequest(
                        waterId,
                        pendingMode,
                        pendingLimitWest,
                        pendingLimitDown,
                        pendingLimitNorth,
                        pendingLimitEast,
                        pendingLimitUp,
                        pendingLimitSouth));
    }

    public void setMode(HE.DamMode mode) {
        this.pendingMode = mode;
    }

    public HE.DamMode getMode() {
        return pendingMode;
    }

    public void setLimitWest(int limitWest) {
        pendingLimitWest = limitWest;
        verifyChanges();
    }

    public void setLimitDown(int limitDown) {
        pendingLimitDown = limitDown;
        verifyChanges();
    }

    public void setLimitNorth(int limitNorth) {
        pendingLimitNorth = limitNorth;
        verifyChanges();
    }

    public void setLimitEast(int limitEast) {
        pendingLimitEast = limitEast;
        verifyChanges();
    }

    public void setLimitUp(int limitUp) {
        pendingLimitUp = limitUp;
        verifyChanges();
    }

    public void setLimitSouth(int limitSouth) {
        pendingLimitSouth = limitSouth;
        verifyChanges();
    }

    public int getLimitWest() {
        return pendingLimitWest;
    }

    public int getLimitDown() {
        return pendingLimitDown;
    }

    public int getLimitNorth() {
        return pendingLimitNorth;
    }

    public int getLimitEast() {
        return pendingLimitEast;
    }

    public int getLimitUp() {
        return pendingLimitUp;
    }

    public int getLimitSouth() {
        return pendingLimitSouth;
    }

    public int getBlockX() {
        return blockX;
    }

    public int getBlockY() {
        return blockY;
    }

    public int getWaterId() {
        return waterId;
    }

    public int getBlockZ() {
        return blockZ;
    }
}
