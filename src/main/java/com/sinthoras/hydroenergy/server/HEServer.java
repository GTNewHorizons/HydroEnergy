package com.sinthoras.hydroenergy.server;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;

import com.sinthoras.hydroenergy.HE;
import com.sinthoras.hydroenergy.HETags;
import com.sinthoras.hydroenergy.config.HEConfig;
import com.sinthoras.hydroenergy.network.packet.HEPacketSynchronize;

import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;

public class HEServer extends WorldSavedData {

    private HEDam[] dams;

    public HEServer() {
        super(HETags.MODID);
    }

    public HEServer(String name) {
        super(name);
        dams = new HEDam[HEConfig.maxDams];
        for (int waterId = 0; waterId < HEConfig.maxDams; waterId++) {
            dams[waterId] = new HEDam(waterId);
        }
    }

    public static HEServer instance;

    public static HEServer load(World world) {
        HEServer instance = (HEServer) world.mapStorage.loadData(HEServer.class, HETags.MODID);
        if (instance == null) {
            instance = new HEServer(HETags.MODID);
            world.mapStorage.setData(HETags.MODID, instance);
            HE.info("Initializing dam data");
        } else {
            HE.info("Dam data loaded");
        }
        return instance;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        dams = new HEDam[HEConfig.maxDams];
        for (int waterId = 0; waterId < HEConfig.maxDams; waterId++) {
            dams[waterId] = new HEDam(waterId);
            dams[waterId].readFromNBTFull(compound.getCompoundTag(HETags.dam + waterId));
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        for (int waterId = 0; waterId < HEConfig.maxDams; waterId++) {
            NBTTagCompound damCompound = new NBTTagCompound();
            dams[waterId].writeToNBTFull(damCompound);
            compound.setTag(HETags.dam + waterId, damCompound);
        }
    }

    public boolean canControllerBePlaced() {
        for (HEDam dam : dams) {
            if (!dam.isPlaced()) {
                return true;
            }
        }
        return false;
    }

    public void onBreakController(int waterId) {
        dams[waterId].breakController();
        markDirty();
    }

    public int onPlacecontroller(String ownerName, int dimensionId, int blockX, int blockY, int blockZ, int waterBlockX,
            int waterBlockY, int waterBlockZ) {
        for (int waterId = 0; waterId < HEConfig.maxDams; waterId++) {
            if (!dams[waterId].isPlaced()) {
                dams[waterId].placeController(
                        ownerName,
                        dimensionId,
                        blockX,
                        blockY,
                        blockZ,
                        waterBlockX,
                        waterBlockY,
                        waterBlockZ);
                markDirty();
                return waterId;
            }
        }
        return -1;
    }

    public void onWaterRemoved(int waterId, int blockY) {
        dams[waterId].onWaterRemoved(blockY);
        markDirty();
    }

    public void onWaterPlaced(int waterId, int blockY) {
        dams[waterId].onWaterPlaced(blockY);
        markDirty();
    }

    public float getWaterLevel(int waterId) {
        return dams[waterId].getWaterLevel();
    }

    public void setWaterLevel(int waterId, float waterLevel) {
        if (dams[waterId].setWaterLevel(waterLevel)) {
            markDirty();
        }
    }

    public void setMode(int waterId, HE.DamMode mode) {
        dams[waterId].setMode(mode);
    }

    public int getWaterLimitWest(int waterId) {
        return dams[waterId].getLimitWest();
    }

    public int getWaterLimitDown(int waterId) {
        return dams[waterId].getLimitDown();
    }

    public int getWaterLimitNorth(int waterId) {
        return dams[waterId].getLimitNorth();
    }

    public int getWaterLimitEast(int waterId) {
        return dams[waterId].getLimitEast();
    }

    public int getWaterLimitUp(int waterId) {
        return dams[waterId].getLimitUp();
    }

    public int getWaterLimitSouth(int waterId) {
        return dams[waterId].getLimitSouth();
    }

    public boolean isBlockOutOfBounds(int waterId, int blockX, int blockY, int blockZ) {
        HEDam dam = dams[waterId];
        return blockX > dam.limitEast || blockX < dam.limitWest
                || blockY > dam.limitUp
                || blockY < dam.limitDown
                || blockZ > dam.limitSouth
                || blockZ < dam.limitNorth;
    }

    public void setWaterLimitWest(int waterId, int limitWest) {
        if (dams[waterId].setLimitWest(limitWest)) {
            markDirty();
        }
    }

    public void setWaterLimitDown(int waterId, int limitDown) {
        if (dams[waterId].setLimitDown(limitDown)) {
            markDirty();
        }
    }

    public void setWaterLimitNorth(int waterId, int limitNorth) {
        if (dams[waterId].setLimitNorth(limitNorth)) {
            markDirty();
        }
    }

    public void setWaterLimitEast(int waterId, int limitEast) {
        if (dams[waterId].setLimitEast(limitEast)) {
            markDirty();
        }
    }

    public void setWaterLimitUp(int waterId, int limitUp) {
        if (dams[waterId].setLimitUp(limitUp)) {
            markDirty();
        }
    }

    public void setWaterLimitSouth(int waterId, int limitSouth) {
        if (dams[waterId].setLimitSouth(limitSouth)) {
            markDirty();
        }
    }

    public boolean canSpread(int waterId) {
        return dams[waterId].canSpread();
    }

    public void synchronizeClient(PlayerLoggedInEvent event) {
        HEPacketSynchronize message = new HEPacketSynchronize();
        for (int waterId = 0; waterId < HEConfig.maxDams; waterId++) {
            message.blocksX[waterId] = dams[waterId].getBlockX();
            message.blocksY[waterId] = dams[waterId].getBlockY();
            message.blocksZ[waterId] = dams[waterId].getBlockZ();
            message.waterLevels[waterId] = dams[waterId].getWaterLevel();
            message.modes[waterId] = dams[waterId].getMode();
            message.limitsWest[waterId] = dams[waterId].limitWest;
            message.limitsDown[waterId] = dams[waterId].limitDown;
            message.limitsNorth[waterId] = dams[waterId].limitNorth;
            message.limitsEast[waterId] = dams[waterId].limitEast;
            message.limitsUp[waterId] = dams[waterId].limitUp;
            message.limitsSouth[waterId] = dams[waterId].limitSouth;
        }
        message.enabledTiers = HEConfig.enabledTiers;
        HE.network.sendTo(message, (EntityPlayerMP) event.player);
        HE.debug("Sent synchronize packet to player " + event.player.getDisplayName());
    }

    public void onConfigRequest(int waterId, HE.DamMode mode, int limitWest, int limitDown, int limitNorth,
            int limitEast, int limitUp, int limitSouth) {
        HE.debug("Received dam config change request");
        if (waterId < 0 || waterId >= HEConfig.maxDams) {
            return;
        }
        if (dams[waterId].onConfigRequest(mode, limitWest, limitDown, limitNorth, limitEast, limitUp, limitSouth)) {
            markDirty();
        }
    }

    public int getWaterId(int blockX, int blockY, int blockZ) {
        for (int waterId = 0; waterId < HEConfig.maxDams; waterId++) {
            if (blockX == dams[waterId].getBlockX() && blockY == dams[waterId].getBlockY()
                    && blockZ == dams[waterId].getBlockZ()) {
                return waterId;
            }
        }
        return -1;
    }

    public List<String> getControllerCoordinates() {
        List<String> list = new ArrayList<String>();
        for (HEDam dam : dams) {
            if (dam.isPlaced()) {
                list.add(dam.getShortDescription());
            }
        }
        return list;
    }

    public long getEuCapacity(int waterId) {
        return dams[waterId].getEuCapacity();
    }

    // This method must be called after getEuCapacity
    public long getEuCapacityAt(int waterId, int blockY) {
        return dams[waterId].getEuCapacityAt(blockY);
    }

    // This method must be called after getEuCapacity
    public void setWaterLevel(int waterId, long euStored) {
        dams[waterId].setWaterLevel(euStored);
    }

    public int getRainedOnBlocks(int waterId) {
        return dams[waterId].getRainedOnBlocks();
    }

    public String getOwnerName(int waterId) {
        return dams[waterId].getOwnerName();
    }
}
