package com.sinthoras.hydroenergy.blocks;

import static com.gtnewhorizon.structurelib.structure.StructureUtility.*;
import static gregtech.api.util.GTStructureUtility.ofHatchAdder;
import static gregtech.api.util.GTUtility.filterValidMTEs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
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
import com.gtnewhorizons.modularui.api.math.Alignment;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.common.widget.DynamicPositionedColumn;
import com.gtnewhorizons.modularui.common.widget.FakeSyncWidget;
import com.gtnewhorizons.modularui.common.widget.SlotWidget;
import com.gtnewhorizons.modularui.common.widget.TextWidget;
import com.sinthoras.hydroenergy.HE;
import com.sinthoras.hydroenergy.HETags;
import com.sinthoras.hydroenergy.HEUtil;
import com.sinthoras.hydroenergy.client.gui.HEGuiHandler;
import com.sinthoras.hydroenergy.config.HEConfig;
import com.sinthoras.hydroenergy.server.HEServer;

import cpw.mods.fml.common.network.internal.FMLNetworkHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.GregTechAPI;
import gregtech.api.enums.GTValues;
import gregtech.api.enums.Textures;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatchOutput;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.recipe.check.CheckRecipeResultRegistry;
import gregtech.api.util.GTUtility;
import tectech.thing.metaTileEntity.multi.base.TTMultiblockBase;
import tectech.thing.metaTileEntity.multi.base.render.TTRenderedExtendedFacingTexture;

public class HEHydroDamTileEntity extends TTMultiblockBase implements IConstructable {

    private static Textures.BlockIcons.CustomIcon Screen;
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
    protected boolean checkMachine_EM(IGregTechTileEntity gregTechTileEntity, ItemStack itemStack) {
        return structureCheck_EM(HETags.structurePieceMain, 2, 3, 0);
    }

    @Override
    public void construct(ItemStack itemStack, boolean hintsOnly) {
        structureBuild_EM(HETags.structurePieceMain, 2, 3, 0, itemStack, hintsOnly);
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

    private int getVoltageTier() {
        boolean configCircuitIsPresent = mInventory != null && mInventory[1] != null
                && mInventory[1].getItem() == GTUtility.getIntegratedCircuit(0).getItem();
        return configCircuitIsPresent ? HEUtil.clamp(mInventory[1].getItemDamage(), 1, GTValues.V.length - 1) : 1;
    }

    @Override
    public boolean onRunningTick(ItemStack stack) {
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
            if (hatch.isFluidLocked() && hatch.getLockedFluidName() != null
                    && !hatch.getLockedFluidName().equals(fluidStack.getUnlocalizedName())) {
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
    public void onScrewdriverRightClick(ForgeDirection side, EntityPlayer player, float blockX, float blockY,
            float blockZ, ItemStack tool) {
        if (!player.isSneaking()) {
            if (getBaseMetaTileEntity().isServerSide()) {
                FMLNetworkHandler.openGui(
                        player,
                        HETags.MODID,
                        HEGuiHandler.HydroDamConfigurationGuiId,
                        getBaseMetaTileEntity().getWorld(),
                        getBaseMetaTileEntity().getXCoord(),
                        getBaseMetaTileEntity().getYCoord(),
                        getBaseMetaTileEntity().getZCoord());
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister blockIconRegister) {
        Screen = new Textures.BlockIcons.CustomIcon("iconsets/he_dam");
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

    private static final String[] mouseOverDescription = new String[] { "Hydro Dam Controller",
            "Controller Block for the Hydro Dam", "Input is pressurized water from Hydro Pumps",
            "Output is pressurized water for Hydro Turbines", "Requires an Input and Output Hatch on the front!",
            HE.blueprintHintTecTech };

    @Override
    public String[] getDescription() {
        return mouseOverDescription;
    }

    private static final String[] chatDescription = new String[] { "1 Fluid Input Hatch", "1 Fluid Output Hatch",
            "Fill the rest with Light Concrete", "No Maintenance Hatch required!" };

    @Override
    public String[] getStructureDescription(ItemStack itemStack) {
        return chatDescription;
    }

    public long getEuStored() {
        return euStored;
    }

    public long getEuCapacity() {
        return euCapacityGui;
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

    @Override
    public void addUIWidgets(ModularWindow.Builder builder, UIBuildContext buildContext) {
        super.addUIWidgets(builder, buildContext);

        builder.widget(new FakeSyncWidget.IntegerSyncer(() -> waterId, val -> waterId = val))
                .widget(
                        TextWidget.dynamicString(() -> "Hydro Dam (" + GTValues.VN[getVoltageTier()] + ")")
                                .setSynced(false).setDefaultColor(COLOR_TEXT_WHITE.get()).setPos(7, 8))
                .widget(
                        new TextWidget(GTUtility.trans("142", "Running perfectly."))
                                .setDefaultColor(COLOR_TEXT_WHITE.get()).setPos(7, 16))
                .widget(TextWidget.dynamicString(() -> {
                    if (getFillMultiplier() > 1.0f) {
                        return "Please upgrade circuit config (>" + getVoltageTier() + ").";
                    } else {
                        return "";
                    }
                }).setSynced(false).setDefaultColor(COLOR_TEXT_GRAY.get()).setPos(7, 84))
                .widget(
                        TextWidget.dynamicString(() -> euStored + " EU / " + euCapacity + " EU").setSynced(false)
                                .setDefaultColor(COLOR_TEXT_WHITE.get()).setPos(99 - 3 - 26, 35).attachSyncer(
                                        new FakeSyncWidget.LongSyncer(() -> euStored, val -> euStored = val),
                                        builder,
                                        (widget, val) -> {
                                            if (widget.isClient()) {
                                                FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
                                                widget.setPos(
                                                        99 - fontRenderer.getStringWidth("/") / 2
                                                                - fontRenderer.getStringWidth(euStored + " EU "),
                                                        35);
                                            }
                                        }))
                .widget(new FakeSyncWidget.LongSyncer(() -> euCapacity, val -> euCapacity = val))
                .widget(TextWidget.dynamicString(() -> {
                    float fillMultiplier = getFillMultiplier();
                    if (fillMultiplier > 1.0f) {
                        return ">100.00%";
                    } else {
                        return String.format("%.2f", fillMultiplier * 100.0f) + "%";
                    }
                }).setSynced(false).setDefaultColor(COLOR_TEXT_WHITE.get()).setPos(99 - 50, 45 + 5).setSize(50 * 2, 9))
                .widget(
                        TextWidget.dynamicString(() -> "IN: " + euPerTickIn + " EU/t").setSynced(false)
                                .setDefaultColor(COLOR_TEXT_WHITE.get()).setPos(7, 45 + 20))
                .widget(new FakeSyncWidget.IntegerSyncer(() -> euPerTickIn, val -> euPerTickIn = val))
                .widget(
                        TextWidget.dynamicString(() -> "OUT: " + euPerTickOut + " EU/t").setSynced(false)
                                .setDefaultColor(COLOR_TEXT_WHITE.get()).setTextAlignment(Alignment.CenterRight)
                                .setPos(7 + 184 - 100, 45 + 20).setSize(100, 9))
                .widget(new FakeSyncWidget.IntegerSyncer(() -> euPerTickOut, val -> euPerTickOut = val))
                .widget(TextWidget.dynamicString(() -> {
                    if (getFillMultiplier() > 1.0f) {
                        return "Please upgrade circuit config (>" + getVoltageTier() + ").";
                    } else {
                        return "Click me with a screwdriver.";
                    }
                }).setSynced(false).setDefaultColor(COLOR_TEXT_GRAY.get()).setPos(7, 84));
    }

    @Override
    protected void drawTexts(DynamicPositionedColumn screenElements, SlotWidget inventorySlot) {}

    private float getFillMultiplier() {
        return euCapacity == 0.0f ? 0.0f : ((float) euStored) / ((float) euCapacity);
    }
}
