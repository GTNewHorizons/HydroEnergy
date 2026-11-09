package com.sinthoras.hydroenergy.blocks;

import static com.gtnewhorizon.structurelib.structure.StructureUtility.*;
import static gregtech.api.util.GTStructureUtility.ofHatchAdder;
import static gregtech.api.util.GTUtility.filterValidMTEs;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;

import com.gtnewhorizon.structurelib.alignment.constructable.IConstructable;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;
import com.sinthoras.hydroenergy.HE;
import com.sinthoras.hydroenergy.HETags;
import com.sinthoras.hydroenergy.HEUtil;
import com.sinthoras.hydroenergy.client.gui.HEHydroDamTileEntityGui;
import com.sinthoras.hydroenergy.config.HEConfig;
import com.sinthoras.hydroenergy.server.HEServer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.GregTechAPI;
import gregtech.api.enums.GTValues;
import gregtech.api.enums.Textures;
import gregtech.api.interfaces.IIconContainer;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatchOutput;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.recipe.check.CheckRecipeResultRegistry;
import gregtech.api.structure.error.StructureError;
import gregtech.api.util.GTUtility;
import gregtech.api.util.MultiblockTooltipBuilder;
import gregtech.common.gui.modularui.multiblock.base.MTEMultiBlockBaseGui;
import tectech.thing.metaTileEntity.multi.base.TTMultiblockBase;
import tectech.thing.metaTileEntity.multi.base.render.TTRenderedExtendedFacingTexture;

public class HEHydroDamTileEntity extends TTMultiblockBase implements IConstructable {

    private static IIconContainer Screen;
    private static final int steelTextureIndex = 16;
    private static final int concreteBlockMeta = 8;
    private int waterId = -1;
    private long euStored = 0;
    private long euCapacity = 0;
    private long euCapacityGui = 0;
    private int euPerTickIn = 0;
    private int euPerTickOut = 0;
    private final HEUtil.AveragedRingBuffer euPerTickOutAverage = new HEUtil.AveragedRingBuffer(64);
    private final HEUtil.AveragedRingBuffer euPerTickInAverage = new HEUtil.AveragedRingBuffer(64);

    private static final IStructureDefinition<HEHydroDamTileEntity> multiblockDefinition = StructureDefinition
            .<HEHydroDamTileEntity>builder()
            .addShape(
                    HETags.structurePieceMain,
                    transpose(
                            new String[][] { { "HHHHH", "CCCCC", "CCCCC", "CCCCC", "CCCCC" },
                                    { "HHHHH", "C   C", "C   C", "C   C", "C   C" },
                                    { "HHHHH", "C   C", "C   C", "C   C", "C   C" },
                                    { "HH~HH", "C   C", "C   C", "C   C", "C   C" },
                                    { "HHHHH", "CCCCC", "CCCCC", "CCCCC", "CCCCC" } }))
            .addElement(
                    'H',
                    ofChain(
                            ofHatchAdder(
                                    HEHydroDamTileEntity::addClassicToMachineList,
                                    steelTextureIndex,
                                    GregTechAPI.sBlockConcretes,
                                    concreteBlockMeta),
                            ofBlockAnyMeta(GregTechAPI.sBlockConcretes, concreteBlockMeta)))
            .addElement('C', ofBlockAnyMeta(GregTechAPI.sBlockConcretes, concreteBlockMeta)).build();

    public HEHydroDamTileEntity(String name) {
        super(name);

        // Disable maintenance requirements at block placement
        mWrench = true;
        mScrewdriver = true;
        mSoftMallet = true;
        mHardHammer = true;
        mSolderingTool = true;
        mCrowbar = true;
    }

    public HEHydroDamTileEntity(int id, String name, String nameRegional) {
        super(id, name, nameRegional);
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity tileEntity) {
        return new HEHydroDamTileEntity(mName);
    }

    @Override
    public void checkMachine(IGregTechTileEntity aBaseMetaTileEntity, ItemStack aStack, List<StructureError> errors) {
        checkPiece(HETags.structurePieceMain, 2, 3, 0, errors);
    }

    @Override
    public void construct(ItemStack itemStack, boolean hintsOnly) {
        buildPiece(HETags.structurePieceMain, itemStack, hintsOnly, 2, 3, 0);
    }

    @Override
    public IStructureDefinition<HEHydroDamTileEntity> getStructure_EM() {
        return multiblockDefinition;
    }

    @Override
    protected @NotNull CheckRecipeResult checkProcessing_EM() {
        mMaxProgresstime = 1;
        return CheckRecipeResultRegistry.SUCCESSFUL;
    }

    private float getMaxGuiPressure() {
        return HEConfig.pressureIncreasePerTier * getVoltageTier();
    }

    public int getVoltageTier() {
        boolean configCircuitIsPresent = mInventory != null && mInventory[1] != null
                && mInventory[1].getItem() == GTUtility.getIntegratedCircuit(0).getItem();
        return configCircuitIsPresent ? HEUtil.clamp(mInventory[1].getItemDamage(), 1, GTValues.V.length - 1) : 1;
    }

    @Override
    public boolean onRunningTick(ItemStack stack) {
        if (waterId < 0 || waterId >= HEConfig.maxDams) return false; // Do nothing if waterID is invalid
        mProgresstime = 0;
        euPerTickIn = 0;
        euPerTickOut = 0;

        euCapacity = HEServer.instance.getEuCapacity(waterId);
        euStored = HEUtil.clamp(euStored, 0, euCapacity);
        euCapacityGui = HEServer.instance
                .getEuCapacityAt(waterId, (int) (getBaseMetaTileEntity().getYCoord() + getMaxGuiPressure()));

        final int waterLevelOverController = (int) (HEServer.instance.getWaterLevel(waterId)
                - getBaseMetaTileEntity().getYCoord());
        getStoredFluids().forEach(fluidStack -> {
            if (fluidStack.getFluidID() == HE.pressurizedWater.getID()
                    && HE.pressurizedWater.getPressure(fluidStack) >= waterLevelOverController) {
                final long availableEnergy = (long) (fluidStack.amount * HEConfig.euPerMilliBucket);
                final long storableEnergy = Math.min(euCapacity - euStored, availableEnergy);
                fluidStack.amount -= storableEnergy;
                euStored += storableEnergy;
                euPerTickIn += storableEnergy;
            }
        });

        final int availableOutput = (int) Math.min(HEConfig.damDrainPerSecond, euStored);
        final int availableOutputAsWater = (int) (availableOutput * HEConfig.milliBucketPerEU);
        if (availableOutput > 0) {
            final int distributedFluid = distributeFluid(new FluidStack(HE.pressurizedWater, availableOutputAsWater));
            final long distributedEu = (long) (distributedFluid * HEConfig.euPerMilliBucket);
            euStored -= distributedEu;
            euPerTickOut += distributedEu;
        }

        if (getBaseMetaTileEntity().getWorld().isRaining()) {
            final long rainingEuGeneration = (long) (HEServer.instance.getRainedOnBlocks(waterId)
                    * HEConfig.waterBonusPerSurfaceBlockPerRainTick);
            final long addedEu = Math.min(euCapacity - euStored, rainingEuGeneration);
            euStored += addedEu;
            euPerTickIn += addedEu;
        }

        euPerTickInAverage.addValue(euPerTickIn);
        euPerTickOutAverage.addValue(euPerTickOut);

        HEServer.instance.setWaterLevel(waterId, euStored);
        return true;
    }

    private int distributeFluid(FluidStack fluidStack) {
        final int availableFluid = fluidStack.amount;
        for (MTEHatchOutput hatch : filterValidMTEs(mOutputHatches)) {
            if (!hatch.outputsLiquids()) {
                continue;
            }
            if (hatch.isFluidLocked() && hatch.getLockedFluid() != null
                    && !hatch.getLockedFluid().equals(fluidStack.getFluid())) {
                continue;
            }

            FluidStack currentFluid = hatch.getFillableStack();
            if (currentFluid == null || currentFluid.getFluid().getID() <= 0) {
                currentFluid = new FluidStack(HE.pressurizedWater, 0);
            }
            if (currentFluid.getFluid().getID() == HE.pressurizedWater.getID()) {
                final int availableSpace = hatch.getCapacity() - currentFluid.amount;
                final int placedFluid = Math.min(availableSpace, fluidStack.amount);
                fluidStack.amount -= placedFluid;
                currentFluid.amount += placedFluid;
                hatch.setFillableStack(currentFluid);
            }
        }
        return availableFluid - fluidStack.amount;
    }

    @Override
    public boolean onRightclick(IGregTechTileEntity aBaseMetaTileEntity, EntityPlayer aPlayer) {
        if (waterId < 0 || waterId >= HEConfig.maxDams) {
            HE.error(HE.ERROR_serverIdsOutOfBounds);
            return false;
        }

        return super.onRightclick(aBaseMetaTileEntity, aPlayer);
    }

    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister blockIconRegister) {
        Screen = Textures.BlockIcons.custom("iconsets/he_dam");
        super.registerIcons(blockIconRegister);
    }

    @Override
    public ITexture[] getTexture(IGregTechTileEntity baseMetaTileEntity, ForgeDirection side, ForgeDirection facing,
            int colorIndex, boolean isActive, boolean hasRedstoneSignal) {
        if (side == facing) {
            return new ITexture[] { Textures.BlockIcons.getCasingTextureForId(steelTextureIndex),
                    new TTRenderedExtendedFacingTexture(Screen) };
        } else {
            return new ITexture[] { Textures.BlockIcons.getCasingTextureForId(steelTextureIndex) };
        }
    }

    @Override
    public boolean doRandomMaintenanceDamage() {
        // Disable maintenance events
        return true;
    }

    @Override
    public void onFirstTick_EM(IGregTechTileEntity baseMetaTileEntity) {
        if (waterId == -1 && getBaseMetaTileEntity().isServerSide()) {
            ForgeDirection direction = getExtendedFacing().getDirection();
            final int offsetX;
            final int offsetY = 1;
            final int offsetZ;
            if (direction == ForgeDirection.WEST) {
                offsetX = 2;
                offsetZ = 0;
            } else if (direction == ForgeDirection.NORTH) {
                offsetX = 0;
                offsetZ = 2;
            } else if (direction == ForgeDirection.EAST) {
                offsetX = -2;
                offsetZ = 0;
            } else {
                offsetX = 0;
                offsetZ = -2;
            }
            waterId = HEServer.instance.onPlacecontroller(
                    getBaseMetaTileEntity().getOwnerName(),
                    getBaseMetaTileEntity().getWorld().provider.dimensionId,
                    getBaseMetaTileEntity().getXCoord(),
                    getBaseMetaTileEntity().getYCoord(),
                    getBaseMetaTileEntity().getZCoord(),
                    getBaseMetaTileEntity().getXCoord() + offsetX,
                    getBaseMetaTileEntity().getYCoord() + offsetY,
                    getBaseMetaTileEntity().getZCoord() + offsetZ);
            markDirty();
        }
        super.onFirstTick_EM(baseMetaTileEntity);
    }

    @Override
    public void onRemoval() {
        if (getBaseMetaTileEntity().isServerSide()) {
            HEServer.instance.onBreakController(waterId);
        }
        super.onRemoval();
    }

    @Override
    public void saveNBTData(NBTTagCompound compound) {
        super.saveNBTData(compound);
        compound.setInteger(HETags.waterId, waterId);
        compound.setLong(HETags.waterStored, euStored);
        compound.setLong(HETags.waterCapacity, euCapacity);
    }

    @Override
    public void loadNBTData(NBTTagCompound compound) {
        super.loadNBTData(compound);
        waterId = compound.getInteger(HETags.waterId);
        euStored = compound.getLong(HETags.waterStored);
        euCapacity = compound.getLong(HETags.waterCapacity);
    }

    @Override
    protected MultiblockTooltipBuilder createTooltip() {
        final MultiblockTooltipBuilder tt = new MultiblockTooltipBuilder();
        tt.addMachineType("Hydro Dam");
        tt.addInfo("Input is pressurized water from Hydro Pumps");
        tt.addInfo("Output is pressurized water for Hydro Turbines");
        tt.addInfo("Requires an Input and Output Hatch on the front!");
        tt.beginStructureBlock(5, 5, 5, true);
        tt.addController("Front Center");
        tt.addInputHatch("Any Casing", 1);
        tt.addOutputHatch("Any Casing", 1);
        tt.addStructureInfo("Fill the rest with Light Concrete");
        tt.addStructureInfo("No Maintenance Hatch required!");
        tt.toolTipFinisher();
        return tt;
    }

    public long getEuStored() {
        return euStored;
    }

    public void setEuStored(long euStored) {
        this.euStored = euStored;
    }

    public long getEuCapacity() {
        return euCapacityGui;
    }

    public void setEuCapacity(long euCapacityGui) {
        this.euCapacityGui = euCapacityGui;
    }

    public int getEuPerTickIn() {
        return (int) euPerTickInAverage.getAverage();
    }

    public int getEuPerTickOut() {
        return (int) euPerTickOutAverage.getAverage();
    }

    public int getWaterId() {
        return waterId;
    }

    public void setWaterId(int waterId) {
        this.waterId = waterId;
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
        return true;
    }

    public float getFillMultiplier() {
        return euCapacityGui == 0.0f ? 0.0f : (float) euStored / euCapacityGui;
    }

    @Override
    public boolean supportsPowerPanel() {
        return false;
    }

    @Override
    public boolean showRecipeTextInGUI() {
        return false;
    }

    @Override
    public boolean shouldDisplayCheckRecipeResult() {
        return false;
    }

    @Override
    public boolean supportsMaintenanceIssueHoverable() {
        return false;
    }

    @Override
    public boolean supportsSingleRecipeLocking() {
        return false;
    }

    @Override
    protected boolean useMui2() {
        return true;
    }

    @Override
    protected @NotNull MTEMultiBlockBaseGui<?> getGui() {
        return new HEHydroDamTileEntityGui(this);
    }

}
