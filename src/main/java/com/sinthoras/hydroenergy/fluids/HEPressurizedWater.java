package com.sinthoras.hydroenergy.fluids;

import com.sinthoras.hydroenergy.HETags;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

public class HEPressurizedWater extends Fluid {

    public HEPressurizedWater() {
        super("pressurized_water");
        setUnlocalizedName("Pressurized Water");
    }

    @Override
    public int getColor() {
        return FluidRegistry.WATER.getColor();
    }

    @Override
    public IIcon getStillIcon() {
        return FluidRegistry.WATER.getStillIcon();
    }

    @Override
    public IIcon getFlowingIcon() {
        return FluidRegistry.WATER.getFlowingIcon();
    }

    public void setPressure(FluidStack fluidStack, int pressure) {
        if(fluidStack.tag == null) {
            fluidStack.tag = new NBTTagCompound();
        }
        fluidStack.tag.setInteger(HETags.pressure, pressure);
    }

    public int getPressure(FluidStack fluidStack) {
        if(fluidStack.tag != null && fluidStack.tag.hasKey(HETags.pressure)) {
            return fluidStack.tag.getInteger(HETags.pressure);
        }
        else {
            return -1;
        }
    }
}
