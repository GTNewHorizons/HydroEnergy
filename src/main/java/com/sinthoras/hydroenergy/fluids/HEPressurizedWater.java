package com.sinthoras.hydroenergy.fluids;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import com.sinthoras.hydroenergy.HETags;

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

    public void setPressure(FluidStack fluidStack, float pressure) {
        if (fluidStack.tag == null) {
            fluidStack.tag = new NBTTagCompound();
        }
        fluidStack.tag.setFloat(HETags.pressure, pressure);
    }

    public float getPressure(FluidStack fluidStack) {
        if (fluidStack.tag != null && fluidStack.tag.hasKey(HETags.pressure)) {
            return fluidStack.tag.getFloat(HETags.pressure);
        } else {
            return -1;
        }
    }
}
