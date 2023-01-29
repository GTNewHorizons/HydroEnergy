package com.sinthoras.hydroenergy.network.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class HEHydroDamConfigContainer extends Container {

    private int waterId;

    public HEHydroDamConfigContainer(int waterId) {
        this.waterId = waterId;
    }

    @Override
    public boolean canInteractWith(EntityPlayer entityPlayer) {
        return true;
    }

    @SideOnly(Side.CLIENT)
    public int getWaterId() {
        return waterId;
    }
}
