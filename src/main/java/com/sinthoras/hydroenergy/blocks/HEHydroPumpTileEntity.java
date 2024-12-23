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
import gregtech.api.util.shutdown.ShutDownReasonRegistry;
import tectech.thing.metaTileEntity.multi.base.render.TTRenderedExtendedFacingTexture;

public abstract class HEHydroPumpTileEntity extends HETieredTileEntity {

    public static class LV extends HEHydroPumpTileEntity {

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

    public static class MV extends HEHydroPumpTileEntity {

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

    public static class HV extends HEHydroPumpTileEntity {

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

    public static class EV extends HEHydroPumpTileEntity {

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

    public static class IV extends HEHydroPumpTileEntity {

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

    public static class LuV extends HEHydroPumpTileEntity {

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

    public static class ZPM extends HEHydroPumpTileEntity {

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

    public static class UV extends HEHydroPumpTileEntity {

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

    public static class UHV extends HEHydroPumpTileEntity {

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

    public static class UEV extends HEHydroPumpTileEntity {

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

    public static class UIV extends HEHydroPumpTileEntity {

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

    public static class UMV extends HEHydroPumpTileEntity {

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

    public static class UXV extends HEHydroPumpTileEntity {

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

    public static class MAX extends HEHydroPumpTileEntity {

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

    private static Textures.BlockIcons.CustomIcon textureScreenPumpON;
    private static Textures.BlockIcons.CustomIcon textureScreenPumpOFF;
    private static Textures.BlockIcons.CustomIcon textureScreenArrowUpAnimated;

    private final int blockTextureIndex = getCasingTextureId();

    public HEHydroPumpTileEntity(int tierId) {
        super("he_pump_" + GTValues.VN[tierId].toLowerCase());
    }

    public HEHydroPumpTileEntity(int blockId, int tierId) {
        super(
                blockId + tierId,
                "he_pump_" + GTValues.VN[tierId].toLowerCase(),
                "Hydro Pump (" + GTValues.VN[tierId] + ")");
    }

    @Override
    public void onTick() {
        int requiredWater = (int) (GTValues.V[getTier()] * HEConfig.milliBucketPerEU);
        for (FluidStack fluidStack : getStoredFluids()) {
            if (fluidStack.getFluid().getID() == FluidRegistry.WATER.getID()) {
                final int consumedWater = Math.min(fluidStack.amount, requiredWater);
                requiredWater -= consumedWater;
                fluidStack.amount -= consumedWater;
            }
        }
        if (requiredWater > 0) {
            stopMachine(ShutDownReasonRegistry.NONE);
            return;
        }

        float pumpedWater = getVoltage() * HEConfig.milliBucketPerEU;
        pumpedWater *= getEfficiencyModifier();
        pumpedWater *= ((float) getCurrentEfficiency(null)) / 100_00.0f;
        final FluidStack fluidStack = new FluidStack(HE.pressurizedWater, (int) pumpedWater);
        HE.pressurizedWater.setPressure(fluidStack, getPressure());
        addOutput(fluidStack);
    }

    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister blockIconRegister) {
        textureScreenPumpOFF = new Textures.BlockIcons.CustomIcon("iconsets/he_pump");
        textureScreenPumpON = new Textures.BlockIcons.CustomIcon("iconsets/he_pump_active");
        textureScreenArrowUpAnimated = new Textures.BlockIcons.CustomIcon("iconsets/he_arrow_up_animated");
        super.registerIcons(blockIconRegister);
    }

    public ITexture[] getTexture(IGregTechTileEntity baseMetaTileEntity, ForgeDirection side, ForgeDirection facing,
            int colorIndex, boolean isActive, boolean hasRedstoneSignal) {
        if (side == facing) {
            if (isActive) {
                return new ITexture[] {
                        Textures.BlockIcons.casingTexturePages[blockTextureIndex >> 6][blockTextureIndex & 0x3f],
                        new TTRenderedExtendedFacingTexture(textureScreenPumpON),
                        new TTRenderedExtendedFacingTexture(textureScreenArrowUpAnimated) };
            } else {
                return new ITexture[] {
                        Textures.BlockIcons.casingTexturePages[blockTextureIndex >> 6][blockTextureIndex & 0x3f],
                        new TTRenderedExtendedFacingTexture(textureScreenPumpOFF) };
            }
        } else {
            return new ITexture[] {
                    Textures.BlockIcons.casingTexturePages[blockTextureIndex >> 6][blockTextureIndex & 0x3f] };
        }
    }

    @Override
    public String[] getDescription() {
        return new String[] { "Hydro Pump Controller", "Controller Block for the Hydro Pump",
                "Consumes EU to pressurize water", "Output is pressurized water for Hydro Dams",
                "Requires a Energy, Input, Output and Maintenance Hatch anywhere!",
                "Requires " + getMilliBucketsPerTick() + "mB Water per Tick",
                "Efficiency: " + getEfficiencyModifierInPercent(), HE.blueprintHintTecTech,
                "Use Redstone to automate!" };
    }

    @Override
    public String[] getStructureDescription(ItemStack itemStack) {
        return new String[] { "1 Energy Hatch", "1 Fluid Input Hatch", "1 Fluid Output Hatch", "1 Maintenance Hatch",
                "Fill the rest with " + getCasingName(), };
    }

    @Override
    protected long getEnergyConsumption() {
        return -getVoltage();
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
