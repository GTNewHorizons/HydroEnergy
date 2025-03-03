package com.sinthoras.hydroenergy.client.gui.widgets;

import java.awt.Color;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.RenderHelper;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.sinthoras.hydroenergy.HE;
import com.sinthoras.hydroenergy.client.HEDam;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class HEWidgetModes extends Gui {

    private static final Color nearBlack = new Color(16, 0, 16, 240);
    private static final Color lightPurple = new Color(80, 0, 255, 80);
    private static final Color darkPurple = new Color(40, 0, 127, 80);

    private final HEDam dam;
    private final int pixelX;
    private final int pixelY;

    private HEButtonTextured.Drain buttonDrain;
    private HEButtonTextured.Debug buttonDebug;
    private HEButtonTextured.Water buttonWater;
    private boolean isEnabled;

    public HEWidgetModes(HEDam dam, int pixelX, int pixelY) {
        this.dam = dam;
        this.pixelX = pixelX;
        this.pixelY = pixelY;
    }

    public void init(List<GuiButton> buttonList) {
        buttonDrain = new HEButtonTextured.Drain(0, pixelX, pixelY);
        buttonDebug = new HEButtonTextured.Debug(1, pixelX + 25, pixelY);
        buttonWater = new HEButtonTextured.Water(2, pixelX + 50, pixelY);

        buttonList.add(buttonDrain);
        buttonList.add(buttonDebug);
        buttonList.add(buttonWater);
    }

    public void actionPerformed(final GuiButton button) {
        if (button == buttonDrain) {
            dam.setMode(HE.DamMode.DRAIN);
            dam.applyChanges();
        } else if (button == buttonDebug) {
            dam.setMode(HE.DamMode.DEBUG);
            dam.applyChanges();
        } else if (button == buttonWater) {
            dam.setMode(HE.DamMode.SPREAD);
            dam.applyChanges();
        }
    }

    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;

        if (isEnabled) {
            HE.DamMode mode = dam.getMode();
            buttonDrain.enabled = mode != HE.DamMode.DRAIN;
            buttonDebug.enabled = mode != HE.DamMode.DEBUG;
            buttonWater.enabled = mode != HE.DamMode.SPREAD;
        } else {
            buttonDrain.enabled = false;
            buttonDebug.enabled = false;
            buttonWater.enabled = false;
        }

        buttonDrain.visible = isEnabled;
        buttonDebug.visible = isEnabled;
        buttonWater.visible = isEnabled;
    }

    // This draw call handles the button manually before the overlay is drawn if a overlay is active
    public void draw(Minecraft minecraft, int mouseX, int mouseY) {
        if (!isEnabled) {
            buttonDrain.visible = true;
            buttonDebug.visible = true;
            buttonWater.visible = true;

            buttonDrain.drawButton(minecraft, mouseX, mouseY);
            buttonDebug.drawButton(minecraft, mouseX, mouseY);
            buttonWater.drawButton(minecraft, mouseX, mouseY);

            buttonDrain.visible = false;
            buttonDebug.visible = false;
            buttonWater.visible = false;
        }
    }

    public void drawTooltip(FontRenderer fontRenderer, int mouseX, int mouseY, int screenWidth) {
        if (isEnabled) {
            if (buttonDrain.isOverButton(mouseX, mouseY)) {
                drawTooltip("Remove water from world", mouseX, mouseY, fontRenderer, screenWidth);
            }
            if (buttonDebug.isOverButton(mouseX, mouseY)) {
                drawTooltip("Spread and show all water", mouseX, mouseY, fontRenderer, screenWidth);
            }
            if (buttonWater.isOverButton(mouseX, mouseY)) {
                drawTooltip("Spread and show water", mouseX, mouseY, fontRenderer, screenWidth);
            }
        }
    }

    private void drawTooltip(String s, int mouseX, int mouseY, FontRenderer fontRenderer, int screenWidth) {
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        mouseX += 12;
        mouseY += 12;
        int stringWidth = fontRenderer.getStringWidth(s);

        // Make sure the end does not leave the image
        mouseX -= Math.max(0, mouseX + stringWidth + 3 - screenWidth);

        this.drawGradientRect(
                mouseX - 3,
                mouseY - 4,
                mouseX + stringWidth + 3,
                mouseY - 3,
                nearBlack.getRGB(),
                nearBlack.getRGB());
        this.drawGradientRect(
                mouseX - 3,
                mouseY + 11,
                mouseX + stringWidth + 3,
                mouseY + 12,
                nearBlack.getRGB(),
                nearBlack.getRGB());
        this.drawGradientRect(
                mouseX - 3,
                mouseY - 3,
                mouseX + stringWidth + 3,
                mouseY + 11,
                nearBlack.getRGB(),
                nearBlack.getRGB());
        this.drawGradientRect(mouseX - 4, mouseY - 3, mouseX - 3, mouseY + 11, nearBlack.getRGB(), nearBlack.getRGB());
        this.drawGradientRect(
                mouseX + stringWidth + 3,
                mouseY - 3,
                mouseX + stringWidth + 4,
                mouseY + 11,
                nearBlack.getRGB(),
                nearBlack.getRGB());
        this.drawGradientRect(
                mouseX - 3,
                mouseY - 2,
                mouseX - 2,
                mouseY + 10,
                lightPurple.getRGB(),
                darkPurple.getRGB());
        this.drawGradientRect(
                mouseX + stringWidth + 2,
                mouseY - 2,
                mouseX + stringWidth + 3,
                mouseY + 10,
                lightPurple.getRGB(),
                darkPurple.getRGB());
        this.drawGradientRect(
                mouseX - 3,
                mouseY - 3,
                mouseX + stringWidth + 3,
                mouseY - 2,
                lightPurple.getRGB(),
                lightPurple.getRGB());
        this.drawGradientRect(
                mouseX - 3,
                mouseY + 10,
                mouseX + stringWidth + 3,
                mouseY + 11,
                darkPurple.getRGB(),
                darkPurple.getRGB());

        fontRenderer.drawStringWithShadow(s, mouseX, mouseY, Color.WHITE.getRGB());

        // Reset color
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        RenderHelper.enableStandardItemLighting();
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
    }
}
