package com.sinthoras.hydroenergy.server;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;

import com.sinthoras.hydroenergy.HE;
import com.sinthoras.hydroenergy.HETags;
import com.sinthoras.hydroenergy.HEUtil;
import com.sinthoras.hydroenergy.config.HEConfig;
import com.sinthoras.hydroenergy.network.packet.HEPacketConfigUpdate;
import com.sinthoras.hydroenergy.network.packet.HEPacketWaterUpdate;

public class HEDam {

    // NBT variables
    private float waterLevel;
    private boolean isPlaced;
    private HE.DamMode mode = HE.DamMode.DRAIN;
    public int limitUp;
    public int limitDown;
    public int limitEast;
    public int limitWest;
    public int limitSouth;
    public int limitNorth;
    private int[] blocksPerY = new int[256];
    private int blockX;
    private int blockY;
    private int blockZ;
    private int dimensionId;
    private int waterBlockX;
    private int waterBlockY;
    private int waterBlockZ;
    private String ownerName = "";

    private final int waterId;
    private final long[] euCapacityUpToY = new long[256];
    private long timestampLastUpdate = 0;

    public HEDam(int waterId) {
        this.waterId = waterId;
    }

    public void readFromNBTFull(NBTTagCompound compound) {
        waterLevel = compound.getFloat(HETags.waterLevel);
        boolean drainState = compound.getBoolean(HETags.drainState);
        isPlaced = compound.getBoolean(HETags.isPlaced);
        limitUp = compound.getInteger(HETags.limitUp);
        limitDown = compound.getInteger(HETags.limitDown);
        limitEast = compound.getInteger(HETags.limitEast);
        limitWest = compound.getInteger(HETags.limitWest);
        limitSouth = compound.getInteger(HETags.limitSouth);
        limitNorth = compound.getInteger(HETags.limitNorth);
        blocksPerY = compound.getIntArray(HETags.blocksPerY);
        blockX = compound.getInteger(HETags.blockX);
        blockY = compound.getInteger(HETags.blockY);
        blockZ = compound.getInteger(HETags.blockZ);
        dimensionId = compound.getInteger(HETags.dimensionId);
        waterBlockX = compound.getInteger(HETags.waterBlockX);
        waterBlockY = compound.getInteger(HETags.waterBlockY);
        waterBlockZ = compound.getInteger(HETags.waterBlockZ);
        ownerName = compound.getString(HETags.ownerName);

        if (!isPlaced || drainState) {
            mode = HE.DamMode.DRAIN;
        } else {
            mode = HE.DamMode.SPREAD;
        }
    }

    public void writeToNBTFull(NBTTagCompound compound) {
        compound.setFloat(HETags.waterLevel, waterLevel);
        compound.setBoolean(HETags.drainState, mode == HE.DamMode.DRAIN);
        compound.setBoolean(HETags.isPlaced, isPlaced);
        compound.setInteger(HETags.limitUp, limitUp);
        compound.setInteger(HETags.limitDown, limitDown);
        compound.setInteger(HETags.limitEast, limitEast);
        compound.setInteger(HETags.limitWest, limitWest);
        compound.setInteger(HETags.limitSouth, limitSouth);
        compound.setInteger(HETags.limitNorth, limitNorth);
        compound.setIntArray(HETags.blocksPerY, blocksPerY);
        compound.setInteger(HETags.blockX, blockX);
        compound.setInteger(HETags.blockY, blockY);
        compound.setInteger(HETags.blockZ, blockZ);
        compound.setInteger(HETags.dimensionId, dimensionId);
        compound.setInteger(HETags.waterBlockX, waterBlockX);
        compound.setInteger(HETags.waterBlockY, waterBlockY);
        compound.setInteger(HETags.waterBlockZ, waterBlockZ);
        compound.setString(HETags.ownerName, ownerName);
    }

    public void setMode(HE.DamMode mode) {
        if (mode != this.mode) {
            this.mode = mode;
            sendConfigUpdate();
        }
    }

    public HE.DamMode getMode() {
        return mode;
    }

    public boolean setWaterLevel(float waterLevel) {
        this.waterLevel = waterLevel;
        long timestamp = System.currentTimeMillis();
        if (timestamp - timestampLastUpdate >= HEConfig.minimalWaterUpdateInterval) {
            timestampLastUpdate = timestamp;
            sendWaterUpdate();
            return true;
        }
        return false;
    }

    public boolean setLimitWest(int limitWest) {
        if (limitWest != this.limitWest) {
            this.limitWest = limitWest;
            sendConfigUpdate();
            return true;
        }
        return false;
    }

    public int getLimitWest() {
        return limitWest;
    }

    public boolean setLimitDown(int limitDown) {
        if (limitDown != this.limitDown) {
            this.limitDown = limitDown;
            sendConfigUpdate();
            return true;
        }
        return false;
    }

    public int getLimitDown() {
        return limitDown;
    }

    public boolean setLimitNorth(int limitNorth) {
        if (limitNorth != this.limitNorth) {
            this.limitNorth = limitNorth;
            sendConfigUpdate();
            return true;
        }
        return false;
    }

    public int getLimitNorth() {
        return limitNorth;
    }

    public boolean setLimitEast(int limitEast) {
        if (limitEast != this.limitEast) {
            this.limitEast = limitEast;
            sendConfigUpdate();
            return true;
        }
        return false;
    }

    public int getLimitEast() {
        return limitEast;
    }

    public boolean setLimitUp(int limitUp) {
        if (limitUp != this.limitUp) {
            this.limitUp = limitUp;
            sendConfigUpdate();
            return true;
        }
        return false;
    }

    public int getLimitUp() {
        return limitUp;
    }

    public boolean setLimitSouth(int limitSouth) {
        if (limitSouth != this.limitSouth) {
            this.limitSouth = limitSouth;
            sendConfigUpdate();
            return true;
        }
        return false;
    }

    public int getLimitSouth() {
        return limitSouth;
    }

    public void sendWaterUpdate() {
        HEPacketWaterUpdate message = new HEPacketWaterUpdate(waterId, waterLevel);
        HE.network.sendToAll(message);
    }

    public void sendConfigUpdate() {
        HEPacketConfigUpdate message = new HEPacketConfigUpdate(
                waterId,
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
        HE.network.sendToAll(message);
    }

    public void breakController() {
        isPlaced = false;
        sendConfigUpdate();
    }

    public void placeController(String ownerName, int dimensionId, int blockX, int blockY, int blockZ, int waterBlockX,
            int waterBlockY, int waterBlockZ) {
        isPlaced = true;
        mode = HE.DamMode.DRAIN;
        limitEast = blockX + 20;
        limitWest = blockX - 20;
        limitUp = blockY + 10;
        limitDown = blockY;
        limitSouth = blockZ + 20;
        limitNorth = blockZ - 20;
        waterLevel = blockY;
        blocksPerY = new int[256];
        this.blockX = blockX;
        this.blockY = blockY;
        this.blockZ = blockZ;
        this.dimensionId = dimensionId;
        this.waterBlockX = waterBlockX;
        this.waterBlockY = waterBlockY;
        this.waterBlockZ = waterBlockZ;
        this.ownerName = ownerName;

        sendConfigUpdate();
    }

    public void onWaterRemoved(int blockY) {
        blocksPerY[blockY]--;
    }

    public void onWaterPlaced(int blockY) {
        blocksPerY[blockY]++;
    }

    public float getWaterLevel() {
        return waterLevel;
    }

    public boolean isPlaced() {
        return isPlaced;
    }

    public int getBlocksOnY(int blockY) {
        return blocksPerY[blockY];
    }

    public int getBlockX() {
        return blockX;
    }

    public int getBlockY() {
        return blockY;
    }

    public int getBlockZ() {
        return blockZ;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public boolean onConfigRequest(HE.DamMode mode, int limitWest, int limitDown, int limitNorth, int limitEast,
            int limitUp, int limitSouth) {
        // Clap change requests to server limits before processing
        limitWest = blockX - HEUtil.clamp(blockX - limitWest, 0, HEConfig.maxWaterSpreadWest);
        limitDown = blockY - HEUtil.clamp(blockY - limitDown, 0, HEConfig.maxWaterSpreadDown);
        limitNorth = blockZ - HEUtil.clamp(blockZ - limitNorth, 0, HEConfig.maxWaterSpreadNorth);
        limitEast = blockX + HEUtil.clamp(limitEast - blockX, 0, HEConfig.maxWaterSpreadEast);
        limitUp = blockY + HEUtil.clamp(limitUp - blockY, 0, HEConfig.maxWaterSpreadUp);
        limitSouth = blockZ + HEUtil.clamp(limitSouth - blockZ, 0, HEConfig.maxWaterSpreadSouth);

        if (this.mode != mode || this.limitWest != limitWest
                || this.limitDown != limitDown
                || this.limitNorth != limitNorth
                || this.limitEast != limitEast
                || this.limitUp != limitUp
                || this.limitSouth != limitSouth) {
            this.mode = mode;
            this.limitWest = limitWest;
            this.limitDown = limitDown;
            this.limitNorth = limitNorth;
            this.limitEast = limitEast;
            this.limitUp = limitUp;
            this.limitSouth = limitSouth;
            sendConfigUpdate();
            HEBlockQueue.enqueueBlock(
                    MinecraftServer.getServer().worldServerForDimension(dimensionId).provider.worldObj,
                    waterBlockX,
                    waterBlockY,
                    waterBlockZ,
                    waterId);
            return true;
        }
        return false;
    }

    public boolean canSpread() {
        return mode != HE.DamMode.DRAIN && isPlaced;
    }

    public String getShortDescription() {
        return "HEController @(" + blockX + ", " + blockY + ", " + blockZ + ")";
    }

    public long getEuCapacity() {
        long euCapacity = 0;
        for (int blockY = this.blockY; blockY < HE.numChunksY * HE.chunkHeight; blockY++) {
            euCapacity += blocksPerY[blockY] * HE.bucketToMilliBucket
                    * HEConfig.euPerMilliBucket
                    * (blockY - this.blockY + 1);
            euCapacityUpToY[blockY] = euCapacity;
        }
        return euCapacity;
    }

    // This method must be called after getEuCapacity (cause euCapacityUpToY[])
    public long getEuCapacityAt(int blockY) {
        return euCapacityUpToY[blockY];
    }

    // This method must be called after getEuCapacity (cause euCapacityUpToY[])
    public void setWaterLevel(long euStored) {
        for (int blockY = this.blockY; blockY < HE.numChunksY * HE.chunkHeight; blockY++) {
            if (euStored < euCapacityUpToY[blockY]) {
                float energyCapacityAtY = blocksPerY[blockY] * HE.bucketToMilliBucket
                        * HEConfig.euPerMilliBucket
                        * (blockY - this.blockY + 1);
                float decimals = 1.0f + ((float) euStored - (float) euCapacityUpToY[blockY]) / energyCapacityAtY;
                setWaterLevel(blockY + decimals);
                return;
            }
        }
    }

    public int getRainedOnBlocks() {
        for (int blockY = 255; blockY >= 0; blockY--) {
            if (blocksPerY[blockY] > 0) {
                return blocksPerY[blockY];
            }
        }
        return 0;
    }
}
