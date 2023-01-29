package com.sinthoras.hydroenergy.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import com.sinthoras.hydroenergy.HE;
import com.sinthoras.hydroenergy.HETags;
import com.sinthoras.hydroenergy.HEUtil;
import com.sinthoras.hydroenergy.api.IHEHasCustomMaterialCalculation;
import com.sinthoras.hydroenergy.client.HEClient;
import com.sinthoras.hydroenergy.config.HEConfig;
import com.sinthoras.hydroenergy.server.HEServer;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;

public class HEWater extends BlockFluidBase implements IHEHasCustomMaterialCalculation {

    private int waterId;

    public HEWater(int waterId) {
        super(FluidRegistry.WATER, Material.water);
        this.waterId = waterId;
        setHardness(100.0F);
        setLightOpacity(0);
        setBlockName("water");
        setBlockTextureName(HETags.MODID + ":" + HE.dummyTexture);
    }

    @Override
    public FluidStack drain(World world, int blockX, int blockY, int blockZ, boolean doDrain) {
        return null;
    }

    @Override
    public boolean canDrain(World world, int blockX, int blockY, int blockZ) {
        return false;
    }

    @Override
    public int getQuantaValue(IBlockAccess world, int blockX, int blockY, int blockZ) {
        float waterLevelInBlock = getWaterLevel() - blockY;
        waterLevelInBlock = HEUtil.clamp(waterLevelInBlock, 0.0f, 1.0f);
        return Math.round(waterLevelInBlock * 8);
    }

    @Override
    public Vec3 getFlowVector(IBlockAccess world, int x, int y, int z) {
        return Vec3.createVectorHelper(0.0D, 0.0D, 0.0D);
    }

    @Override
    public boolean canCollideCheck(int meta, boolean fullHit) {
        return false;
    }

    @Override
    public int getLightOpacity() {
        if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
            return HE.waterOpacity;
        } else {
            return 0;
        }
    }

    @Override
    public int getMaxRenderHeightMeta() {
        return 0;
    }

    public float getWaterLevel() {
        if (HE.logicalClientLoaded) {
            return HEClient.getDam(waterId).getWaterLevelForPhysicsAndLighting();
        } else {
            return HEServer.instance.getWaterLevel(getWaterId());
        }
    }

    public boolean canFlowInto(IBlockAccess world, int blockX, int blockY, int blockZ) {
        Block block = world.getBlock(blockX, blockY, blockZ);
        return block.getMaterial() == Material.air
                || (canDisplace(world, blockX, blockY, blockZ) && !(block instanceof BlockLiquid))
                || (block.getMaterial() == Material.water && !(block instanceof HEWater));
    }

    public int getWaterId() {
        return waterId;
    }

    @Override
    public String getUnlocalizedName() {
        return super.getUnlocalizedName() + waterId;
    }

    public Material getMaterial(int blockY) {
        return (Math.floor(getWaterLevel() - HEConfig.clippingOffset)) < blockY ? Material.air : Material.water;
    }

    public Material getMaterial(double blockY) {
        // This constant is so magic I'm gonna die!
        // Without this constant there is a gap between rendered water and all underwater effects
        return (getWaterLevel() + 0.120f) < blockY ? Material.air : Material.water;
    }
}
