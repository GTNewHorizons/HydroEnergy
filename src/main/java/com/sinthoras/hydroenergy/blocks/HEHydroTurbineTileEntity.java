package com.sinthoras.hydroenergy.blocks;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import com.sinthoras.hydroenergy.HE;
import com.sinthoras.hydroenergy.config.HEConfig;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.enums.GTValues;
import gregtech.api.enums.Textures;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import tectech.thing.metaTileEntity.multi.base.render.TTRenderedExtendedFacingTexture;

public abstract class HEHydroTurbineTileEntity extends HETieredTileEntity {

    public static class LV extends HEHydroTurbineTileEntity {

        private static final int tierId = 1;

        public LV() {
            super(tierId);
        }

        public LV(int id) {
            super(id, tierId);
        }

        @Override
        protected int getTier() {
            return tierId;
        }

        @Override
        public IMetaTileEntity newMetaEntity(IGregTechTileEntity tileEntity) {
            return new LV();
        }
    }

    public static class MV extends HEHydroTurbineTileEntity {

        private static final int tierId = 2;

        public MV() {
            super(tierId);
        }

        public MV(int id) {
            super(id, tierId);
        }

        @Override
        protected int getTier() {
            return tierId;
        }

        @Override
        public IMetaTileEntity newMetaEntity(IGregTechTileEntity tileEntity) {
            return new MV();
        }
    }

    public static class HV extends HEHydroTurbineTileEntity {

        private static final int tierId = 3;

        public HV() {
            super(tierId);
        }

        public HV(int id) {
            super(id, tierId);
        }

        @Override
        protected int getTier() {
            return tierId;
        }

        @Override
        public IMetaTileEntity newMetaEntity(IGregTechTileEntity tileEntity) {
            return new HV();
        }
    }

    public static class EV extends HEHydroTurbineTileEntity {

        private static final int tierId = 4;

        public EV() {
            super(tierId);
        }

        public EV(int id) {
            super(id, tierId);
        }

        @Override
        protected int getTier() {
            return tierId;
        }

        @Override
        public IMetaTileEntity newMetaEntity(IGregTechTileEntity tileEntity) {
            return new EV();
        }
    }

    public static class IV extends HEHydroTurbineTileEntity {

        private static final int tierId = 5;

        public IV() {
            super(tierId);
        }

        public IV(int id) {
            super(id, tierId);
        }

        @Override
        protected int getTier() {
            return tierId;
        }

        @Override
        public IMetaTileEntity newMetaEntity(IGregTechTileEntity tileEntity) {
            return new IV();
        }
    }

    public static class LuV extends HEHydroTurbineTileEntity {

        private static final int tierId = 6;

        public LuV() {
            super(tierId);
        }

        public LuV(int id) {
            super(id, tierId);
        }

        @Override
        protected int getTier() {
            return tierId;
        }

        @Override
        public IMetaTileEntity newMetaEntity(IGregTechTileEntity tileEntity) {
            return new LuV();
        }
    }

    public static class ZPM extends HEHydroTurbineTileEntity {

        private static final int tierId = 7;

        public ZPM() {
            super(tierId);
        }

        public ZPM(int id) {
            super(id, tierId);
        }

        @Override
        protected int getTier() {
            return tierId;
        }

        @Override
        public IMetaTileEntity newMetaEntity(IGregTechTileEntity tileEntity) {
            return new ZPM();
        }
    }

    public static class UV extends HEHydroTurbineTileEntity {

        private static final int tierId = 8;

        public UV() {
            super(tierId);
        }

        public UV(int id) {
            super(id, tierId);
        }

        @Override
        protected int getTier() {
            return tierId;
        }

        @Override
        public IMetaTileEntity newMetaEntity(IGregTechTileEntity tileEntity) {
            return new UV();
        }
    }

    public static class UHV extends HEHydroTurbineTileEntity {

        private static final int tierId = 9;

        public UHV() {
            super(tierId);
        }

        public UHV(int id) {
            super(id, tierId);
        }

        @Override
        protected int getTier() {
            return tierId;
        }

        @Override
        public IMetaTileEntity newMetaEntity(IGregTechTileEntity tileEntity) {
            return new UHV();
        }
    }

    public static class UEV extends HEHydroTurbineTileEntity {

        private static final int tierId = 10;

        public UEV() {
            super(tierId);
        }

        public UEV(int id) {
            super(id, tierId);
        }

        @Override
        protected int getTier() {
            return tierId;
        }

        @Override
        public IMetaTileEntity newMetaEntity(IGregTechTileEntity tileEntity) {
            return new UEV();
        }
    }

    public static class UIV extends HEHydroTurbineTileEntity {

        private static final int tierId = 11;

        public UIV() {
            super(tierId);
        }

        public UIV(int id) {
            super(id, tierId);
        }

        @Override
        protected int getTier() {
            return tierId;
        }

        @Override
        public IMetaTileEntity newMetaEntity(IGregTechTileEntity tileEntity) {
            return new UIV();
        }
    }

    public static class UMV extends HEHydroTurbineTileEntity {

        private static final int tierId = 12;

        public UMV() {
            super(tierId);
        }

        public UMV(int id) {
            super(id, tierId);
        }

        @Override
        protected int getTier() {
            return tierId;
        }

        @Override
        public IMetaTileEntity newMetaEntity(IGregTechTileEntity tileEntity) {
            return new UMV();
        }
    }

    public static class UXV extends HEHydroTurbineTileEntity {

        private static final int tierId = 13;

        public UXV() {
            super(tierId);
        }

        public UXV(int id) {
            super(id, tierId);
        }

        @Override
        protected int getTier() {
            return tierId;
        }

        @Override
        public IMetaTileEntity newMetaEntity(IGregTechTileEntity tileEntity) {
            return new UXV();
        }
    }

    public static class MAX extends HEHydroTurbineTileEntity {

        private static final int tierId = 14;

        public MAX() {
            super(tierId);
        }

        public MAX(int id) {
            super(id, tierId);
        }

        @Override
        protected int getTier() {
            return tierId;
        }

        @Override
        public IMetaTileEntity newMetaEntity(IGregTechTileEntity tileEntity) {
            return new MAX();
        }
    }

    private static Textures.BlockIcons.CustomIcon textureScreenTurbineON;
    private static Textures.BlockIcons.CustomIcon textureScreenTurbineOFF;
    private static Textures.BlockIcons.CustomIcon textureScreenArrowDownAnimated;

    private final int blockTextureIndex = getCasingTextureId();

    public HEHydroTurbineTileEntity(int tierId) {
        super("he_turbine_" + GTValues.VN[tierId].toLowerCase());
    }

    public HEHydroTurbineTileEntity(int blockId, int tierId) {
        super(
                blockId + tierId,
                "he_turbine_" + GTValues.VN[tierId].toLowerCase(),
                "Hydro Turbine (" + GTValues.VN[tierId] + ")");
    }

    @Override
    public void onTick() {
        final int consumableWaterPerTick = (int) (getVoltage() * HEConfig.milliBucketPerEU);
        int consumedWater = 0;
        for (FluidStack fluidStack : getStoredFluids()) {
            if (fluidStack.getFluidID() == HE.pressurizedWater.getID()) {
                final int consumableWater = Math.max(0, consumableWaterPerTick - consumedWater);
                final int processedWater = Math.min(fluidStack.amount, consumableWater);
                fluidStack.amount -= processedWater;
                consumedWater += processedWater;
            }
        }
        float producedEU = consumedWater * HEConfig.euPerMilliBucket;
        producedEU *= getEfficiencyModifier();
        producedEU *= (float) getCurrentEfficiency(null) / 100_00.0f;
        addEnergyOutput_EM((int) producedEU, 1);

        addOutput(new FluidStack(FluidRegistry.WATER, consumedWater));
    }

    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister blockIconRegister) {
        textureScreenTurbineOFF = new Textures.BlockIcons.CustomIcon("iconsets/he_turbine");
        textureScreenTurbineON = new Textures.BlockIcons.CustomIcon("iconsets/he_turbine_active");
        textureScreenArrowDownAnimated = new Textures.BlockIcons.CustomIcon("iconsets/he_arrow_down_animated");
        super.registerIcons(blockIconRegister);
    }

    @Override
    public ITexture[] getTexture(IGregTechTileEntity baseMetaTileEntity, ForgeDirection side, ForgeDirection facing,
            int colorIndex, boolean isActive, boolean hasRedstoneSignal) {
        if (side == facing) {
            if (isActive) {
                return new ITexture[] {
                        Textures.BlockIcons.casingTexturePages[blockTextureIndex >> 6][blockTextureIndex & 0x3f],
                        new TTRenderedExtendedFacingTexture(textureScreenTurbineON),
                        new TTRenderedExtendedFacingTexture(textureScreenArrowDownAnimated) };
            } else {
                return new ITexture[] {
                        Textures.BlockIcons.casingTexturePages[blockTextureIndex >> 6][blockTextureIndex & 0x3f],
                        new TTRenderedExtendedFacingTexture(textureScreenTurbineOFF) };
            }
        } else {
            return new ITexture[] {
                    Textures.BlockIcons.casingTexturePages[blockTextureIndex >> 6][blockTextureIndex & 0x3f] };
        }
    }

    @Override
    public String[] getDescription() {
        return new String[] { "Hydro Turbine Controller", "Controller Block for the Hydro Turbine",
                "Consumes pressurize water to produce EU", "Input is pressurized water from Hydro Dams",
                "Requires a Dynamo, Input, Output and Maintenance Hatch anywhere!",
                "Produces up to " + getMilliBucketsPerTick() + "mB Water per Tick",
                "Efficiency: " + getEfficiencyModifierInPercent(), HE.blueprintHintTecTech,
                "Use Redstone to automate!" };
    }

    @Override
    public String[] getStructureDescription(ItemStack itemStack) {
        return new String[] { "1 Dynamo Hatch", "1 Fluid Input Hatch", "1 Fluid Output Hatch", "1 Maintenance Hatch",
                "Fill the rest with " + getCasingName(), };
    }

    @Override
    protected long getEnergyConsumption() {
        return getVoltage();
    }

    @Override
    public boolean isPowerPassButtonEnabled() {
        return false;
    }

    @Override
    public boolean isSafeVoidButtonEnabled() {
        return false;
    }

    @Override
    public boolean isAllowedToWorkButtonEnabled() {
        return false;
    }
}
