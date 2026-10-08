package com.sinthoras.hydroenergy.server;

import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

import com.sinthoras.hydroenergy.HE;
import com.sinthoras.hydroenergy.HETags;
import com.sinthoras.hydroenergy.HEUtil;
import com.sinthoras.hydroenergy.HEWorld;
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
    private final NavigableMap<Integer, Integer> blocksPerY = new TreeMap<>();
    private int blockX;
    private int blockY;
    private int blockZ;
    private int dimensionId;
    private int waterBlockX;
    private int waterBlockY;
    private int waterBlockZ;
    private String ownerName = "";

    private final int waterId;
    private final NavigableMap<Integer, Long> euCapacityUpToY = new TreeMap<>();
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
        blocksPerY.clear();
        boolean sparse = compound.hasKey("waterHeights", 11);
        int[] counts = compound
                .getIntArray(sparse && compound.hasKey("waterCounts", 11) ? "waterCounts" : HETags.blocksPerY);
        int[] heights = compound.getIntArray("waterHeights");
        for (int i = 0; i < counts.length && (!sparse || i < heights.length); i++) {
            if (counts[i] > 0) blocksPerY.put(sparse ? heights[i] : i, counts[i]);
        }
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
        int[] heights = new int[blocksPerY.size()];
        int[] counts = new int[blocksPerY.size()];
        int index = 0;
        for (Map.Entry<Integer, Integer> entry : blocksPerY.entrySet()) {
            heights[index] = entry.getKey();
            counts[index++] = entry.getValue();
        }
        compound.setIntArray("waterHeights", heights);
        compound.setIntArray("waterCounts", counts);
        // Keep the old tag valid for ordinary-height worlds opened with an older HydroEnergy build.
        int[] legacyCounts = new int[256];
        for (Map.Entry<Integer, Integer> entry : blocksPerY.subMap(0, true, 256, false).entrySet()) {
            legacyCounts[entry.getKey()] = entry.getValue();
        }
        compound.setIntArray(HETags.blocksPerY, legacyCounts);
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
        blocksPerY.clear();
        euCapacityUpToY.clear();
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
        int count = getBlocksOnY(blockY);
        if (count <= 1) blocksPerY.remove(blockY);
        else blocksPerY.put(blockY, count - 1);
    }

    public void onWaterPlaced(int blockY) {
        blocksPerY.put(blockY, getBlocksOnY(blockY) + 1);
    }

    public float getWaterLevel() {
        return waterLevel;
    }

    public boolean isPlaced() {
        return isPlaced;
    }

    public int getBlocksOnY(int blockY) {
        return blocksPerY.getOrDefault(blockY, 0);
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
        World world = MinecraftServer.getServer().worldServerForDimension(dimensionId);
        if (world == null) return false;
        // Clamp change requests to server and world limits before processing
        limitWest = blockX - HEUtil.clamp(blockX - limitWest, 0, HEConfig.maxWaterSpreadWest);
        limitDown = Math.max(
                HEWorld.minHeight(world),
                blockY - HEUtil.clamp(blockY - limitDown, 0, HEConfig.maxWaterSpreadDown));
        limitNorth = blockZ - HEUtil.clamp(blockZ - limitNorth, 0, HEConfig.maxWaterSpreadNorth);
        limitEast = blockX + HEUtil.clamp(limitEast - blockX, 0, HEConfig.maxWaterSpreadEast);
        limitUp = Math.min(
                HEWorld.maxHeight(world) - 1,
                blockY + HEUtil.clamp(limitUp - blockY, 0, HEConfig.maxWaterSpreadUp));
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
            HEBlockQueue.enqueueBlock(world, waterBlockX, waterBlockY, waterBlockZ, waterId);
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
        euCapacityUpToY.clear();
        for (Map.Entry<Integer, Integer> entry : blocksPerY.tailMap(blockY, true).entrySet()) {
            euCapacity += energyAt(entry.getKey(), entry.getValue());
            euCapacityUpToY.put(entry.getKey(), euCapacity);
        }
        return euCapacity;
    }

    private double energyAt(int y, int count) {
        return (double) count * HE.bucketToMilliBucket * HEConfig.euPerMilliBucket * (y - blockY + 1L);
    }

    // Call getEuCapacity first to refresh the cumulative capacities.
    public long getEuCapacityAt(int blockY) {
        Map.Entry<Integer, Long> entry = euCapacityUpToY.floorEntry(blockY);
        return entry == null ? 0 : entry.getValue();
    }

    public void setWaterLevel(long euStored) {
        long previousCapacity = 0;
        for (Map.Entry<Integer, Long> entry : euCapacityUpToY.entrySet()) {
            if (euStored < entry.getValue()) {
                double fraction = (euStored - previousCapacity)
                        / energyAt(entry.getKey(), getBlocksOnY(entry.getKey()));
                setWaterLevel((float) (entry.getKey() + Math.max(0, fraction)));
                return;
            }
            previousCapacity = entry.getValue();
        }
        setWaterLevel(euCapacityUpToY.isEmpty() ? (float) blockY : euCapacityUpToY.lastKey() + 1.0f);
    }

    public int getRainedOnBlocks() {
        return blocksPerY.isEmpty() ? 0 : blocksPerY.lastEntry().getValue();
    }
}
