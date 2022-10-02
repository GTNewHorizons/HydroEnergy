package com.sinthoras.hydroenergy.network.container;

import com.github.technus.tectech.thing.metaTileEntity.multi.base.GT_Container_MultiMachineEM;
import com.sinthoras.hydroenergy.HE;
import com.sinthoras.hydroenergy.blocks.HEHydroDamTileEntity;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import java.nio.ByteBuffer;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;

public class HEHydroDamEuContainer extends GT_Container_MultiMachineEM {

    private long euStored;
    private long euCapacity;
    private int euPerTickIn;
    private int euPerTickOut;
    private long timestamp = 0;

    private ByteBuffer buffer;

    private class Buffer {

        public static final int euStoredOffset = 0 * Long.BYTES;
        public static final int euCapacityOffset = 1 * Long.BYTES;
        public static final int euPerTickInOffset = 2 * Long.BYTES;
        public static final int euPerTickOutOffset = 2 * Long.BYTES + Integer.BYTES;
    }

    private static final int parameterIdOffset = 21;

    public HEHydroDamEuContainer(InventoryPlayer inventoryPlayer, IGregTechTileEntity hydroDamMetaTileEntity) {
        super(inventoryPlayer, hydroDamMetaTileEntity, false, false, false);
    }

    @Override
    public void addCraftingToCrafters(ICrafting clientHandle) {
        buffer.putLong(Buffer.euStoredOffset, euStored);
        sendStateUpdate(clientHandle, Buffer.euStoredOffset);

        buffer.putLong(Buffer.euCapacityOffset, euCapacity);
        sendStateUpdate(clientHandle, Buffer.euCapacityOffset);

        buffer.putInt(Buffer.euPerTickInOffset, euPerTickIn);
        sendStateUpdate(clientHandle, Buffer.euPerTickInOffset);

        buffer.putInt(Buffer.euPerTickOutOffset, euPerTickOut);
        sendStateUpdate(clientHandle, Buffer.euPerTickOutOffset);

        super.addCraftingToCrafters(clientHandle);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        // Initize buffer here because this method is called from the super constructor and therefore, is run before
        // this class' contructor is run
        if (buffer == null) {
            buffer = ByteBuffer.allocate(2 * Long.BYTES + 2 * Integer.BYTES);
        }

        if (mTileEntity.isServerSide()) {
            HEHydroDamTileEntity hydroDamTileEntity = (HEHydroDamTileEntity) mTileEntity.getMetaTileEntity();
            long currentTime = System.currentTimeMillis();
            if (!mTileEntity.getWorld().isRemote && currentTime > HE.controllerGuiUpdateDelay + timestamp) {
                boolean updateEuCapacity = false;
                long currentEuCapacity = hydroDamTileEntity.getEuCapacity();
                if (euCapacity != currentEuCapacity) {
                    euCapacity = currentEuCapacity;
                    buffer.putLong(Buffer.euCapacityOffset, euCapacity);
                    updateEuCapacity = true;
                }

                boolean updateEuStored = false;
                long currentEuStored = hydroDamTileEntity.getEuStored();
                if (euStored != currentEuStored) {
                    euStored = currentEuStored;
                    buffer.putLong(Buffer.euStoredOffset, euStored);
                    updateEuStored = true;
                }

                boolean updateEuPerTickIn = false;
                int currentEuPerTickIn = hydroDamTileEntity.getEuPerTickIn();
                if (euPerTickIn != currentEuPerTickIn) {
                    euPerTickIn = currentEuPerTickIn;
                    buffer.putInt(Buffer.euPerTickInOffset, euPerTickIn);
                    updateEuPerTickIn = true;
                }

                boolean updateEuPerTickOut = false;
                int currentEuPerTickOut = hydroDamTileEntity.getEuPerTickOut();
                if (euPerTickOut != currentEuPerTickOut) {
                    euPerTickOut = currentEuPerTickOut;
                    buffer.putInt(Buffer.euPerTickOutOffset, euPerTickOut);
                    updateEuPerTickOut = true;
                }

                for (ICrafting clientHandle : (List<ICrafting>) crafters) {
                    if (updateEuCapacity) {
                        sendStateUpdate(clientHandle, Buffer.euCapacityOffset);
                    }

                    if (updateEuStored) {
                        sendStateUpdate(clientHandle, Buffer.euStoredOffset);
                    }

                    if (updateEuPerTickIn) {
                        sendStateUpdate(clientHandle, Buffer.euPerTickInOffset);
                    }

                    if (updateEuPerTickOut) {
                        sendStateUpdate(clientHandle, Buffer.euPerTickOutOffset);
                    }
                }

                if (updateEuCapacity || updateEuStored || updateEuPerTickIn || updateEuPerTickOut) {
                    timestamp = currentTime;
                }
            }
        }
    }

    private void sendStateUpdate(ICrafting clientHandle, int bufferOffset) {
        final int bytes;
        if (bufferOffset >= Buffer.euPerTickInOffset) {
            bytes = Integer.BYTES;
        } else {
            bytes = Long.BYTES;
        }
        for (int i = 0; i < bytes; i++) {
            int index = bufferOffset + i;
            clientHandle.sendProgressBarUpdate(this, index + parameterIdOffset, buffer.get(index));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }

    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int index, int value) {
        super.updateProgressBar(index, value);
        index = index - parameterIdOffset;
        if (index >= 0 && index < buffer.capacity()) {
            buffer.put(index, (byte) value);
        }
    }

    @SideOnly(Side.CLIENT)
    public long getEuStored() {
        return buffer.getLong(Buffer.euStoredOffset);
    }

    @SideOnly(Side.CLIENT)
    public long getEuCapacity() {
        return buffer.getLong(Buffer.euCapacityOffset);
    }

    @SideOnly(Side.CLIENT)
    public int getEuPerTickIn() {
        return buffer.getInt(Buffer.euPerTickInOffset);
    }

    @SideOnly(Side.CLIENT)
    public int getEuPerTickOut() {
        return buffer.getInt(Buffer.euPerTickOutOffset);
    }
}
