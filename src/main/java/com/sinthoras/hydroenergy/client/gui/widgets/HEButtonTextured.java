package com.sinthoras.hydroenergy.client.gui.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

import org.lwjgl.opengl.GL11;

import com.sinthoras.hydroenergy.client.gui.HEHydroDamConfigGuiContainer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class HEButtonTextured extends GuiButton {

    public static class Drain extends HEButtonTextured {

        public Drain(int id, int pixelX, int pixelY) {
            super(id, pixelX, pixelY, 0);
        }
    }

    public static class Debug extends HEButtonTextured {

        public Debug(int id, int pixelX, int pixelY) {
            super(id, pixelX, pixelY, 20);
        }
    }

    public static class Water extends HEButtonTextured {

        public Water(int id, int pixelX, int pixelY) {
            super(id, pixelX, pixelY, 40);
        }
    }

    private int texU = 0;

    public HEButtonTextured(int id, int pixelX, int pixelY, int texU) {
        super(id, pixelX, pixelY, 20, 20, "");
        this.texU = texU;
    }

    public void drawButton(Minecraft minecraft, int mouseX, int mouseY) {
        if (this.visible) {
            minecraft.getTextureManager().bindTexture(HEHydroDamConfigGuiContainer.backgroundTextureLocation);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

            if (enabled) {
                int texV = 176;
                if (isOverButton(mouseX, mouseY)) {
                    texV += this.height;
                }
                drawTexturedModalRect(xPosition, yPosition, texU, texV, width, height);
            } else {
                drawTexturedModalRect(xPosition, yPosition, texU, 216, width, height);
            }
        }
    }

    public boolean isOverButton(int mouseX, int mouseY) {
        return mouseX >= xPosition && mouseY >= yPosition && mouseX < xPosition + width && mouseY < yPosition + height;
    }
}
