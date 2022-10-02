package com.sinthoras.hydroenergy.client.gui;

import com.github.technus.tectech.thing.metaTileEntity.multi.base.GT_GUIContainer_MultiMachineEM;
import com.sinthoras.hydroenergy.HEUtil;
import com.sinthoras.hydroenergy.blocks.HEHydroDamTileEntity;
import com.sinthoras.hydroenergy.network.container.HEHydroDamEuContainer;
import gregtech.api.enums.GT_Values;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.util.GT_Utility;
import java.awt.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraftforge.fluids.FluidRegistry;
import org.lwjgl.opengl.GL11;

public class HEHydroDamEuGuiContainer extends GT_GUIContainer_MultiMachineEM {

    private static final Color textColor = new Color(250, 250, 255);
    private static final Color textHintColor = new Color(110, 110, 120);

    private HEHydroDamEuContainer hydroDamContainer;

    public HEHydroDamEuGuiContainer(
            InventoryPlayer inventoryPlayer,
            IGregTechTileEntity hydroDamMetaTileEntity,
            String aName,
            String textureFile) {
        super(
                new HEHydroDamEuContainer(inventoryPlayer, hydroDamMetaTileEntity),
                aName,
                textureFile,
                false,
                false,
                false);
        hydroDamContainer = (HEHydroDamEuContainer) mContainer;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        ItemStack[] inventory = ((HEHydroDamTileEntity) hydroDamContainer.mTileEntity.getMetaTileEntity()).mInventory;
        boolean configCircuitIsPresent = inventory != null
                && inventory[1] != null
                && inventory[1].getItem() == GT_Utility.getIntegratedCircuit(0).getItem();
        int voltageTier =
                configCircuitIsPresent ? HEUtil.clamp(inventory[1].getItemDamage(), 1, GT_Values.VN.length - 1) : 1;

        long euCapacity = hydroDamContainer.getEuCapacity();
        long euStored = hydroDamContainer.getEuStored();
        long euPerTickIn = hydroDamContainer.getEuPerTickIn();
        long euPerTickOut = hydroDamContainer.getEuPerTickOut();
        float fillMultiplier = euCapacity == 0.0f ? 0.0f : ((float) euStored) / ((float) euCapacity);

        fontRendererObj.drawString("Hydro Dam (" + GT_Values.VN[voltageTier] + ")", 7, 8, textColor.getRGB());
        fontRendererObj.drawString("Running perfectly.", 7, 16, textColor.getRGB());
        if (fillMultiplier > 1.0f) {
            fontRendererObj.drawString(
                    "Please upgrade circuit config (>" + voltageTier + ").", 7, 84, textHintColor.getRGB());
        } else {
            fontRendererObj.drawString("Click me with a screwdriver.", 7, 84, textHintColor.getRGB());
        }

        int slashWidth = fontRendererObj.getStringWidth("/");
        int storedWidth = fontRendererObj.getStringWidth("" + euStored + " EU ");
        fontRendererObj.drawString(
                "" + euStored + " EU / " + euCapacity + " EU",
                99 - slashWidth / 2 - storedWidth,
                35,
                textColor.getRGB());

        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
        IIcon iconStill = FluidRegistry.WATER.getStillIcon();
        GL11.glColor4f(0.7f, 0.7f, 0.7f, 1.0f);
        drawTexturedBar(7, 45, 184, 16, iconStill, Math.min(fillMultiplier, 1.0f));

        String relativeInfo;
        if (fillMultiplier > 1.0f) {
            relativeInfo = ">100.00%";
        } else {
            relativeInfo = String.format("%.2f", fillMultiplier * 100.0f) + "%";
        }
        int relativeInfoWidth = fontRendererObj.getStringWidth(relativeInfo);
        fontRendererObj.drawString(relativeInfo, 99 - relativeInfoWidth / 2, 45 + 5, textColor.getRGB());

        String in = "IN: " + euPerTickIn + " EU/t";
        String out = "OUT: " + euPerTickOut + " EU/t";
        fontRendererObj.drawString(in, 7, 45 + 20, textColor.getRGB());
        int constStringWidth = fontRendererObj.getStringWidth(out);
        fontRendererObj.drawString(out, 7 + 184 - constStringWidth, 45 + 20, textColor.getRGB());
    }

    private void drawTexturedBar(int pixelX, int pixelY, int width, int height, IIcon icon, float progress) {
        int pixelProgress = Math.round(width * progress);
        int iconHeight = icon.getIconHeight();
        int completeTextures = pixelProgress / iconHeight;
        for (int i = 0; i < completeTextures; i++) {
            int tmpX = pixelX + i * iconHeight;
            drawTexturedModelRectFromIcon(tmpX, pixelY, icon, iconHeight, height);
        }
        int remainder = pixelProgress % iconHeight;
        if (remainder > 0) {
            int tmpX = pixelX + completeTextures * iconHeight;
            drawTexturedModelRectFromIcon(tmpX, pixelY, icon, remainder, height);
        }
    }
}
