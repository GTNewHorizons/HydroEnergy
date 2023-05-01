package com.sinthoras.hydroenergy.client.renderer;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.RenderBlockFluid;

import org.lwjgl.opengl.GLContext;

import com.sinthoras.hydroenergy.HEUtil;
import com.sinthoras.hydroenergy.blocks.HEWater;
import com.sinthoras.hydroenergy.blocks.HEWaterStill;
import com.sinthoras.hydroenergy.client.HEClient;
import com.sinthoras.hydroenergy.config.HEConfig;

import cpw.mods.fml.client.registry.RenderingRegistry;

public class HEWaterRenderer extends RenderBlockFluid {

    public static HEWaterRenderer instance = new HEWaterRenderer();
    private final int renderID = RenderingRegistry.getNextAvailableRenderId();

    @Override
    public float getFluidHeightForRender(IBlockAccess world, int blockX, int blockY, int blockZ, BlockFluidBase block) {
        HEWaterStill water = (HEWaterStill) block;
        float val = water.getWaterLevel() - blockY;
        return HEUtil.clamp(val, 0.0f, 1.0f);
    }

    @Override
    public int getRenderId() {
        return renderID;
    }

    // Static instances to save time on memory allocation
    private static final Block[] neighbors = new Block[6];
    private static final boolean[] shouldSidesBeRendered = new boolean[6];

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int blockX, int blockY, int blockZ, Block block, int modelId,
            RenderBlocks renderer) {
        if (!(block instanceof HEWater)) {
            return false;
        }

        if (GLContext.getCapabilities().OpenGL32 && !HEConfig.useLimitedRendering) {
            neighbors[0] = world.getBlock(blockX - 1, blockY, blockZ);
            neighbors[1] = world.getBlock(blockX + 1, blockY, blockZ);
            neighbors[2] = world.getBlock(blockX, blockY - 1, blockZ);
            neighbors[3] = world.getBlock(blockX, blockY + 1, blockZ);
            neighbors[4] = world.getBlock(blockX, blockY, blockZ - 1);
            neighbors[5] = world.getBlock(blockX, blockY, blockZ + 1);

            shouldSidesBeRendered[0] = !neighbors[0].isOpaqueCube() && neighbors[0] != block;
            shouldSidesBeRendered[1] = !neighbors[1].isOpaqueCube() && neighbors[1] != block;
            shouldSidesBeRendered[2] = neighbors[2] != block;
            shouldSidesBeRendered[3] = !neighbors[3].isOpaqueCube() && neighbors[3] != block;
            shouldSidesBeRendered[4] = !neighbors[4].isOpaqueCube() && neighbors[4] != block;
            shouldSidesBeRendered[5] = !neighbors[5].isOpaqueCube() && neighbors[5] != block;

            int worldColorModifier = block.colorMultiplier(world, blockX, blockY, blockZ);
            HETessalator.addBlock(
                    blockX,
                    blockY,
                    blockZ,
                    ((HEWater) block).getWaterId(),
                    worldColorModifier,
                    shouldSidesBeRendered);

            return false;
        } else {
            HEWater water = (HEWater) block;
            float renderedWaterLevel = HEClient.getDam(water.getWaterId()).getWaterLevelForRendering();
            if (renderedWaterLevel < blockY) {
                return false;
            }

            Tessellator tessellator = Tessellator.instance;
            int color = block.colorMultiplier(world, blockX, blockY, blockZ);
            float red = (color >> 16 & 255) / 255.0F;
            float green = (color >> 8 & 255) / 255.0F;
            float blue = (color & 255) / 255.0F;

            Block waterBlock = Block.getBlockFromName("water");
            BlockFluidBase theFluid = (BlockFluidBase) block;
            int densityDir = -1;
            int bMeta = world.getBlockMetadata(blockX, blockY, blockZ);

            boolean renderTop = renderedWaterLevel > blockY && renderedWaterLevel <= blockY + 1;

            boolean renderBottom = block.shouldSideBeRendered(world, blockX, blockY + densityDir, blockZ, 0)
                    && world.getBlock(blockX, blockY + densityDir, blockZ) != theFluid;

            boolean[] renderSides = new boolean[] { block.shouldSideBeRendered(world, blockX, blockY, blockZ - 1, 2),
                    block.shouldSideBeRendered(world, blockX, blockY, blockZ + 1, 3),
                    block.shouldSideBeRendered(world, blockX - 1, blockY, blockZ, 4),
                    block.shouldSideBeRendered(world, blockX + 1, blockY, blockZ, 5) };

            if (!renderTop && !renderBottom
                    && !renderSides[0]
                    && !renderSides[1]
                    && !renderSides[2]
                    && !renderSides[3]) {
                return false;
            } else {
                boolean rendered = false;
                double heightNW, heightSW, heightSE, heightNE;
                float flow = getFluidHeightForRender(world, blockX, blockY, blockZ, theFluid);

                if (flow > 0) {
                    heightNW = flow;
                    heightSW = flow;
                    heightSE = flow;
                    heightNE = flow;
                } else {
                    return false;
                }

                final float LIGHT_Y_NEG = 0.5F;
                final float LIGHT_Y_POS = 1.0F;
                final float LIGHT_XZ_NEG = 0.8F;
                final float LIGHT_XZ_POS = 0.6F;
                final double RENDER_OFFSET = 0.0010000000474974513D;

                if (renderer.renderAllFaces || renderTop) {
                    rendered = true;
                    IIcon iconStill = waterBlock.getIcon(1, bMeta);

                    heightNW -= RENDER_OFFSET;
                    heightSW -= RENDER_OFFSET;
                    heightSE -= RENDER_OFFSET;
                    heightNE -= RENDER_OFFSET;

                    double u1, u2, u3, u4, v1, v2, v3, v4;

                    u2 = iconStill.getInterpolatedU(0.0D);
                    v2 = iconStill.getInterpolatedV(0.0D);
                    u1 = u2;
                    v1 = iconStill.getInterpolatedV(16.0D);
                    u4 = iconStill.getInterpolatedU(16.0D);
                    v4 = v1;
                    u3 = u4;
                    v3 = v2;

                    tessellator.setBrightness(block.getMixedBrightnessForBlock(world, blockX, blockY, blockZ));
                    tessellator.setColorOpaque_F(LIGHT_Y_POS * red, LIGHT_Y_POS * green, LIGHT_Y_POS * blue);

                    tessellator.addVertexWithUV(blockX, blockY + heightNW, blockZ, u2, v2);
                    tessellator.addVertexWithUV(blockX, blockY + heightSW, blockZ + 1, u1, v1);
                    tessellator.addVertexWithUV(blockX + 1, blockY + heightSE, blockZ + 1, u4, v4);
                    tessellator.addVertexWithUV(blockX + 1, blockY + heightNE, blockZ, u3, v3);

                    tessellator.addVertexWithUV(blockX, blockY + heightNW, blockZ, u2, v2);
                    tessellator.addVertexWithUV(blockX + 1, blockY + heightNE, blockZ, u3, v3);
                    tessellator.addVertexWithUV(blockX + 1, blockY + heightSE, blockZ + 1, u4, v4);
                    tessellator.addVertexWithUV(blockX, blockY + heightSW, blockZ + 1, u1, v1);
                }

                if (renderer.renderAllFaces || renderBottom) {
                    rendered = true;
                    tessellator.setBrightness(block.getMixedBrightnessForBlock(world, blockX, blockY - 1, blockZ));
                    tessellator.setColorOpaque_F(LIGHT_Y_NEG * red, LIGHT_Y_NEG * green, LIGHT_Y_NEG * blue);
                    renderer.renderFaceYNeg(
                            block,
                            blockX,
                            blockY + RENDER_OFFSET,
                            blockZ,
                            waterBlock.getIcon(0, bMeta));
                }

                for (int side = 0; side < 4; ++side) {
                    int x2 = blockX;
                    int z2 = blockZ;

                    switch (side) {
                        case 0:
                            --z2;
                            break;
                        case 1:
                            ++z2;
                            break;
                        case 2:
                            --x2;
                            break;
                        case 3:
                            ++x2;
                            break;
                    }

                    IIcon iconFlow = waterBlock.getIcon(side + 2, bMeta);
                    if (renderer.renderAllFaces || renderSides[side]) {
                        rendered = true;

                        double ty1;
                        double tx1;
                        double ty2;
                        double tx2;
                        double tz1;
                        double tz2;

                        if (side == 0) {
                            ty1 = heightNW;
                            ty2 = heightNE;
                            tx1 = blockX;
                            tx2 = blockX + 1;
                            tz1 = blockZ + RENDER_OFFSET;
                            tz2 = blockZ + RENDER_OFFSET;
                        } else if (side == 1) {
                            ty1 = heightSE;
                            ty2 = heightSW;
                            tx1 = blockX + 1;
                            tx2 = blockX;
                            tz1 = blockZ + 1 - RENDER_OFFSET;
                            tz2 = blockZ + 1 - RENDER_OFFSET;
                        } else if (side == 2) {
                            ty1 = heightSW;
                            ty2 = heightNW;
                            tx1 = blockX + RENDER_OFFSET;
                            tx2 = blockX + RENDER_OFFSET;
                            tz1 = blockZ + 1;
                            tz2 = blockZ;
                        } else {
                            ty1 = heightNE;
                            ty2 = heightSE;
                            tx1 = blockX + 1 - RENDER_OFFSET;
                            tx2 = blockX + 1 - RENDER_OFFSET;
                            tz1 = blockZ;
                            tz2 = blockZ + 1;
                        }

                        float u1Flow = iconFlow.getInterpolatedU(0.0D);
                        float u2Flow = iconFlow.getInterpolatedU(8.0D);
                        float v1Flow = iconFlow.getInterpolatedV((1.0D - ty1) * 16.0D * 0.5D);
                        float v2Flow = iconFlow.getInterpolatedV((1.0D - ty2) * 16.0D * 0.5D);
                        float v3Flow = iconFlow.getInterpolatedV(8.0D);
                        tessellator.setBrightness(block.getMixedBrightnessForBlock(world, x2, blockY, z2));
                        float sideLighting = 1.0F;

                        if (side < 2) {
                            sideLighting = LIGHT_XZ_NEG;
                        } else {
                            sideLighting = LIGHT_XZ_POS;
                        }

                        tessellator.setColorOpaque_F(
                                LIGHT_Y_POS * sideLighting * red,
                                LIGHT_Y_POS * sideLighting * green,
                                LIGHT_Y_POS * sideLighting * blue);

                        tessellator.addVertexWithUV(tx1, blockY + ty1, tz1, u1Flow, v1Flow);
                        tessellator.addVertexWithUV(tx2, blockY + ty2, tz2, u2Flow, v2Flow);
                        tessellator.addVertexWithUV(tx2, blockY, tz2, u2Flow, v3Flow);
                        tessellator.addVertexWithUV(tx1, blockY, tz1, u1Flow, v3Flow);

                        tessellator.addVertexWithUV(tx1, blockY + ty1, tz1, u1Flow, v1Flow);
                        tessellator.addVertexWithUV(tx1, blockY, tz1, u1Flow, v3Flow);
                        tessellator.addVertexWithUV(tx2, blockY, tz2, u2Flow, v3Flow);
                        tessellator.addVertexWithUV(tx2, blockY + ty2, tz2, u2Flow, v2Flow);
                    }
                }
                renderer.renderMinY = 0;
                renderer.renderMaxY = 1;
                return rendered;
            }
        }
    }
}
