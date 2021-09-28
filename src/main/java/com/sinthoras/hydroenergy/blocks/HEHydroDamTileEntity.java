package com.sinthoras.hydroenergy.blocks;

import com.github.technus.tectech.thing.metaTileEntity.multi.base.GT_MetaTileEntity_MultiblockBase_EM;
import com.github.technus.tectech.thing.metaTileEntity.multi.base.render.TT_RenderedExtendedFacingTexture;
import com.gtnewhorizon.structurelib.alignment.constructable.IConstructable;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;
import com.sinthoras.hydroenergy.HE;
import com.sinthoras.hydroenergy.HETags;
import com.sinthoras.hydroenergy.HEUtil;
import com.sinthoras.hydroenergy.client.gui.HEGuiHandler;
import com.sinthoras.hydroenergy.client.gui.HEHydroDamEuGuiContainer;
import com.sinthoras.hydroenergy.config.HEConfig;
import com.sinthoras.hydroenergy.network.container.HEHydroDamEuContainer;
import com.sinthoras.hydroenergy.server.HEServer;
import cpw.mods.fml.common.network.internal.FMLNetworkHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.GregTech_API;
import gregtech.api.enums.GT_Values;
import gregtech.api.enums.Textures;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch_Output;
import gregtech.api.util.GT_Utility;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;

import static com.gtnewhorizon.structurelib.structure.StructureUtility.*;
import static gregtech.api.util.GT_StructureUtility.ofHatchAdder;

public class HEHydroDamTileEntity extends GT_MetaTileEntity_MultiblockBase_EM implements IConstructable {

    private static Textures.BlockIcons.CustomIcon Screen;
    private final static int steelTextureIndex = 16;
    private final static int concreteBlockMeta = 8;
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
        .addShape(HETags.structurePieceMain,
            transpose(new String[][]{
                {"HHHHH", "CCCCC", "CCCCC", "CCCCC", "CCCCC"},
                {"HHHHH", "C   C", "C   C", "C   C", "C   C"},
                {"HHHHH", "C   C", "C   C", "C   C", "C   C"},
                {"HH~HH", "C   C", "C   C", "C   C", "C   C"},
                {"HHHHH", "CCCCC", "CCCCC", "CCCCC", "CCCCC"}
            })
        ).addElement(
            'H',
            ofChain(
                ofHatchAdder(
                    HEHydroDamTileEntity::addClassicToMachineList, steelTextureIndex,
                    GregTech_API.sBlockConcretes, concreteBlockMeta
                ),
                ofBlockAnyMeta(
                    GregTech_API.sBlockConcretes, concreteBlockMeta
                )
            )
        ).addElement(
            'C',
                    ofBlockAnyMeta(
                GregTech_API.sBlockConcretes, concreteBlockMeta
            )
        ).build();

    public HEHydroDamTileEntity(String name) {
        super(name);

        // Disable maintenance requirements at block placement
        mWrench = true;
        mScrewdriver = true;
        mSoftHammer = true;
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
        structureBuild_EM(HETags.structurePieceMain, 2,3,0, hintsOnly, itemStack);
    }

    @Override
    public IStructureDefinition<HEHydroDamTileEntity> getStructure_EM() {
        return multiblockDefinition;
    }

    @Override
    public boolean checkRecipe_EM(ItemStack stack) {
        mMaxProgresstime = 1;
        return true;
    }

    private float getMaxGuiPressure() {
        boolean configCircuitIsPresent = mInventory != null && mInventory[1] != null && mInventory[1].getItem() == GT_Utility.getIntegratedCircuit(0).getItem();
        int voltageTier = configCircuitIsPresent ? HEUtil.clamp(mInventory[1].getItemDamage(), 1, GT_Values.V.length) : 1;
        return HEConfig.pressureIncreasePerTier * voltageTier;
    }

    @Override
    public boolean onRunningTick(ItemStack stack) {
        mProgresstime = 0;
        euPerTickIn = 0;
        euPerTickOut = 0;

        euCapacity = HEServer.instance.getEuCapacity(waterId);
        euStored = HEUtil.clamp(euStored, 0, euCapacity);
        euCapacityGui = HEServer.instance.getEuCapacityAt(waterId, (int)(getBaseMetaTileEntity().getYCoord() + getMaxGuiPressure()));

        final int waterLevelOverController = (int) (HEServer.instance.getWaterLevel(waterId) - getBaseMetaTileEntity().getYCoord());
        getStoredFluids().forEach(fluidStack -> {
            if(fluidStack.getFluidID() == HE.pressurizedWater.getID()
                    && HE.pressurizedWater.getPressure(fluidStack) >= waterLevelOverController) {
                final long avaiableEnergy = (long)(fluidStack.amount * HEConfig.euPerMilliBucket);
                final long storableEnergy = Math.min(euCapacity - euStored, avaiableEnergy);
                fluidStack.amount -= storableEnergy;
                euStored += storableEnergy;
                euPerTickIn += storableEnergy;
            }
        });

        final int availableOutput = (int)Math.min(HEConfig.damDrainPerSecond, euStored);
        final int availableOutputAsWater = (int)(availableOutput * HEConfig.milliBucketPerEU);
        if(availableOutput > 0) {
            final int distributedFluid = distributeFluid(new FluidStack(HE.pressurizedWater, availableOutputAsWater));
            final long distributedEu = (long)(distributedFluid * HEConfig.euPerMilliBucket);
            euStored -= distributedEu;
            euPerTickOut += distributedEu;
        }

        if(getBaseMetaTileEntity().getWorld().isRaining()) {
            final long raingEuGeneration = (long)(HEServer.instance.getRainedOnBlocks(waterId) * HEConfig.waterBonusPerSurfaceBlockPerRainTick);
            final long addedEu = Math.min(euCapacity - euStored, raingEuGeneration);
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
        for (GT_MetaTileEntity_Hatch_Output hatch : mOutputHatches) {
            if (!isValidMetaTileEntity(hatch)) {
                continue;
            }
            if (!hatch.outputsLiquids()) {
                continue;
            }
            if (hatch.isFluidLocked() && hatch.getLockedFluidName() != null && !hatch.getLockedFluidName().equals(fluidStack.getUnlocalizedName())) {
                continue;
            }

            FluidStack currentFluid = hatch.getFillableStack();
            if (currentFluid == null || currentFluid.getFluid().getID() <= 0) {
                currentFluid = new FluidStack(HE.pressurizedWater, 0);
            }
            if(currentFluid.getFluid().getID() == HE.pressurizedWater.getID()) {
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
    public Object getServerGUI(int id, InventoryPlayer playerInventory, IGregTechTileEntity baseMetaTileEntity) {
        return new HEHydroDamEuContainer(playerInventory, baseMetaTileEntity);
    }

    @Override
    public Object getClientGUI(int id, InventoryPlayer playerInventory, IGregTechTileEntity baseMetaTileEntity) {
        return new HEHydroDamEuGuiContainer(playerInventory, baseMetaTileEntity, getLocalName(), "EMDisplay.png");
    }

    @Override
    public void onScrewdriverRightClick(byte side, EntityPlayer player, float blockX, float blockY, float blockZ) {
        if(!player.isSneaking()) {
            if (getBaseMetaTileEntity().isServerSide()) {
                FMLNetworkHandler.openGui(player, HETags.MODID, HEGuiHandler.HydroDamConfigurationGuiId, getBaseMetaTileEntity().getWorld(),
                        getBaseMetaTileEntity().getXCoord(), getBaseMetaTileEntity().getYCoord(), getBaseMetaTileEntity().getZCoord());
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister blockIconRegister) {
        Screen = new Textures.BlockIcons.CustomIcon("iconsets/he_dam");
        super.registerIcons(blockIconRegister);
    }

    @Override
    public ITexture[] getTexture(IGregTechTileEntity baseMetaTileEntity, byte side, byte facing, byte colorIndex, boolean isActive, boolean hasRedstoneSignal) {
        if(side == facing) {
            return new ITexture[] {
                    Textures.BlockIcons.getCasingTextureForId(steelTextureIndex),
                    new TT_RenderedExtendedFacingTexture(Screen)
            };
        }
        else {
            return new ITexture[] {
                    Textures.BlockIcons.getCasingTextureForId(steelTextureIndex)
            };
        }
    }

    @Override
    public boolean doRandomMaintenanceDamage() {
        // Disable maintenance events
        return true;
    }

    @Override
    public void onFirstTick_EM(IGregTechTileEntity baseMetaTileEntity) {
        if(waterId == -1 && getBaseMetaTileEntity().isServerSide()) {
            ForgeDirection direction = getExtendedFacing().getDirection();
            final int offsetX;
            final int offsetY = 1;
            final int offsetZ;
            if(direction == ForgeDirection.WEST) {
                offsetX = 2;
                offsetZ = 0;
            }
            else if(direction == ForgeDirection.NORTH) {
                offsetX = 0;
                offsetZ = 2;
            }
            else if(direction == ForgeDirection.EAST) {
                offsetX = -2;
                offsetZ = 0;
            }
            else {
                offsetX = 0;
                offsetZ = -2;
            }
            waterId = HEServer.instance.onPlacecontroller(getBaseMetaTileEntity().getOwnerName(),
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
        if(getBaseMetaTileEntity().isServerSide()) {
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

    private final static String[] mouseOverDescription = new String[] {
            "Hydro Dam Controller",
            "Controller Block for the Hydro Dam",
            "Input is pressurized water from Hydro Pumps",
            "Output is pressurized water for Hydro Turbines",
            "Requires an Input and Output Hatch on the front!",
            HE.blueprintHintTecTech
    };

    @Override
    public String[] getDescription() {
        return mouseOverDescription;
    }

    private static final String[] chatDescription = new String[] {
            "1 Fluid Input Hatch",
            "1 Fluid Output Hatch",
            "Fill the rest with Light Concrete",
            "No Maintenance Hatch required!"
    };

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
        return (int)euPerTickInAverage.getAverage();
    }

    public int getEuPerTickOut() {
        return (int)euPerTickOutAverage.getAverage();
    }

    public int getWaterId() {
        return waterId;
    }
}
