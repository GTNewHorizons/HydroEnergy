package com.sinthoras.hydroenergy.client.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.sinthoras.hydroenergy.blocks.HEHydroDamTileEntity;
import com.sinthoras.hydroenergy.client.HEClient;
import com.sinthoras.hydroenergy.network.container.HEHydroDamConfigContainer;

import cpw.mods.fml.common.network.IGuiHandler;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;

public class HEGuiHandler implements IGuiHandler {

    public static final int HydroDamConfigurationGuiId = 0;

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int blockX, int blockY, int blockZ) {
        if (id == HydroDamConfigurationGuiId) {
            TileEntity tileEntity = world.getTileEntity(blockX, blockY, blockZ);
            if (tileEntity instanceof IGregTechTileEntity) {
                IMetaTileEntity metaTileEntity = ((IGregTechTileEntity) tileEntity).getMetaTileEntity();
                if (metaTileEntity instanceof HEHydroDamTileEntity) {
                    return new HEHydroDamConfigContainer(((HEHydroDamTileEntity) metaTileEntity).getWaterId());
                }
            }
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int blockX, int blockY, int blockZ) {
        if (world.isRemote) {
            if (id == HydroDamConfigurationGuiId) {
                TileEntity tileEntity = world.getTileEntity(blockX, blockY, blockZ);
                if (tileEntity instanceof IGregTechTileEntity) {
                    IMetaTileEntity metaTileEntity = ((IGregTechTileEntity) tileEntity).getMetaTileEntity();
                    if (metaTileEntity instanceof HEHydroDamTileEntity) {
                        return new HEHydroDamConfigGuiContainer(
                                new HEHydroDamConfigContainer(HEClient.getWaterId(blockX, blockY, blockZ)));
                    }
                }
            }
        } else {
            return getServerGuiElement(id, player, world, blockX, blockY, blockZ);
        }
        return null;
    }
}
