package com.sinthoras.hydroenergy.client.gui;

import java.awt.Color;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import com.sinthoras.hydroenergy.HE;
import com.sinthoras.hydroenergy.HETags;
import com.sinthoras.hydroenergy.client.HEClient;
import com.sinthoras.hydroenergy.client.HEDam;
import com.sinthoras.hydroenergy.client.gui.widgets.HEWidgetModes;
import com.sinthoras.hydroenergy.network.container.HEHydroDamConfigContainer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class HEHydroDamConfigGuiContainer extends GuiContainer {

    public static final ResourceLocation backgroundTextureLocation = new ResourceLocation(
            HETags.MODID,
            HE.damBackgroundLocation);

    private HEDam dam;

    private HEWidgetModes widgetModes;

    private GuiButton changeWest;
    private GuiButton changeEast;
    private GuiButton changeDown;
    private GuiButton changeUp;
    private GuiButton changeNorth;
    private GuiButton changeSouth;

    private HEPopupLimitGui[] limitGuis = new HEPopupLimitGui[6];

    private int centerX = 0;
    private int centerY = 0;

    private static final Color lineGrey = new Color(155, 155, 155);
    private static final Color textGrey = new Color(104, 104, 104);

    public HEHydroDamConfigGuiContainer(HEHydroDamConfigContainer hydroDamConfigContainer) {
        super(hydroDamConfigContainer);
        xSize = 256;
        ySize = 176;
        dam = HEClient.getDam(hydroDamConfigContainer.getWaterId());
    }

    @Override
    public void initGui() {
        super.initGui();

        centerX = guiLeft + xSize / 2;
        centerY = guiTop + ySize / 2;
        int popupLeft = centerX - HEPopupLimitGui.xSize / 2;
        int popupTop = centerY - HEPopupLimitGui.ySize / 2;

        int id = 0;
        int pixelX = guiLeft + 10;
        int pixelY = guiTop + 32;
        int width = 42;
        int height = 20;
        changeWest = new GuiButton(id, pixelX, pixelY, width, height, "Change");
        buttonList.add(changeWest);
        limitGuis[0] = new HEPopupLimitGui("Western limit (-X)", popupLeft, popupTop, dam.getLimitWest());

        pixelY += 30;
        changeDown = new GuiButton(id, pixelX, pixelY, width, height, "Change");
        buttonList.add(changeDown);
        limitGuis[1] = new HEPopupLimitGui("Lower limit (-Y)", popupLeft, popupTop, dam.getLimitDown());

        pixelY += 30;
        changeNorth = new GuiButton(id, pixelX, pixelY, width, height, "Change");
        buttonList.add(changeNorth);
        limitGuis[2] = new HEPopupLimitGui("Northern limit (-Z)", popupLeft, popupTop, dam.getLimitNorth());

        pixelX = guiLeft + xSize - width - 10;
        pixelY = guiTop + 32;
        changeEast = new GuiButton(id, pixelX, pixelY, width, height, "Change");
        buttonList.add(changeEast);
        limitGuis[3] = new HEPopupLimitGui("Eastern limit (+X)", popupLeft, popupTop, dam.getLimitEast());

        pixelY += 30;
        changeUp = new GuiButton(id, pixelX, pixelY, width, height, "Change");
        buttonList.add(changeUp);
        limitGuis[4] = new HEPopupLimitGui("Upper limit (+Y)", popupLeft, popupTop, dam.getLimitUp());

        pixelY += 30;
        changeSouth = new GuiButton(id, pixelX, pixelY, width, height, "Change");
        buttonList.add(changeSouth);
        limitGuis[5] = new HEPopupLimitGui("Southern limit (+Z)", popupLeft, popupTop, dam.getLimitSouth());

        widgetModes = new HEWidgetModes(dam, guiLeft + xSize - 75, guiTop + 5);
        widgetModes.init(buttonList);

        for (HEPopupLimitGui limitGui : limitGuis) {
            limitGui.init(0, buttonList);
        }
    }

    @Override
    protected void actionPerformed(final GuiButton button) {
        if (button == changeWest) {
            widgetModes.setEnabled(false);
            limitGuis[0].show();
        } else if (button == changeDown) {
            widgetModes.setEnabled(false);
            limitGuis[1].show();
        } else if (button == changeNorth) {
            widgetModes.setEnabled(false);
            limitGuis[2].show();
        } else if (button == changeEast) {
            widgetModes.setEnabled(false);
            limitGuis[3].show();
        } else if (button == changeUp) {
            widgetModes.setEnabled(false);
            limitGuis[4].show();
        } else if (button == changeSouth) {
            widgetModes.setEnabled(false);
            limitGuis[5].show();
        }
        for (HEPopupLimitGui limitGui : limitGuis) {
            limitGui.actionPerformed(button);
        }
        widgetModes.actionPerformed(button);
    }

    private void updateValues() {
        if (limitGuis[0].getValueChangedAndReset()) {
            dam.setLimitWest(limitGuis[0].getValue());
            dam.applyChanges();
        }
        if (limitGuis[1].getValueChangedAndReset()) {
            dam.setLimitDown(limitGuis[1].getValue());
            dam.applyChanges();
        }
        if (limitGuis[2].getValueChangedAndReset()) {
            dam.setLimitNorth(limitGuis[2].getValue());
            dam.applyChanges();
        }
        if (limitGuis[3].getValueChangedAndReset()) {
            dam.setLimitEast(limitGuis[3].getValue());
            dam.applyChanges();
        }
        if (limitGuis[4].getValueChangedAndReset()) {
            dam.setLimitUp(limitGuis[4].getValue());
            dam.applyChanges();
        }
        if (limitGuis[5].getValueChangedAndReset()) {
            dam.setLimitSouth(limitGuis[5].getValue());
            dam.applyChanges();
        }

        limitGuis[0].updateOriginalValue(dam.getLimitWest());
        limitGuis[1].updateOriginalValue(dam.getLimitDown());
        limitGuis[2].updateOriginalValue(dam.getLimitNorth());
        limitGuis[3].updateOriginalValue(dam.getLimitEast());
        limitGuis[4].updateOriginalValue(dam.getLimitUp());
        limitGuis[5].updateOriginalValue(dam.getLimitSouth());
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        updateValues();

        Minecraft minecraft = Minecraft.getMinecraft();
        FontRenderer fontRenderer = minecraft.fontRenderer;

        GL11.glColor4f(1F, 1F, 1F, 1F);
        minecraft.getTextureManager().bindTexture(backgroundTextureLocation);
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, xSize, ySize);

        {
            // Title
            fontRenderer.drawString("Hydro Dam", guiLeft + 15, guiTop + 6, Color.BLACK.getRGB());
            fontRenderer.drawString("Water Spread Limits", guiLeft + 15, guiTop + 18, textGrey.getRGB());

            // Reset color
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

            // Draw title bar buttons if popup is open
            widgetModes.draw(minecraft, mouseX, mouseY);
        }

        {
            // Disable buttons if a popup is open and render them before the popup is rendered
            boolean enableElements = !isAnyLimitGuiOpen();
            widgetModes.setEnabled(enableElements);
            changeWest.enabled = enableElements;
            changeDown.enabled = enableElements;
            changeNorth.enabled = enableElements;
            changeEast.enabled = enableElements;
            changeUp.enabled = enableElements;
            changeSouth.enabled = enableElements;

            if (!enableElements) {
                changeWest.visible = true;
                changeDown.visible = true;
                changeNorth.visible = true;
                changeEast.visible = true;
                changeUp.visible = true;
                changeSouth.visible = true;

                changeWest.drawButton(minecraft, mouseX, mouseY);
                changeDown.drawButton(minecraft, mouseX, mouseY);
                changeNorth.drawButton(minecraft, mouseX, mouseY);
                changeEast.drawButton(minecraft, mouseX, mouseY);
                changeUp.drawButton(minecraft, mouseX, mouseY);
                changeSouth.drawButton(minecraft, mouseX, mouseY);
            }

            changeWest.visible = enableElements;
            changeDown.visible = enableElements;
            changeNorth.visible = enableElements;
            changeEast.visible = enableElements;
            changeUp.visible = enableElements;
            changeSouth.visible = enableElements;
        }

        {
            String limitWest = "" + dam.getLimitWest();
            String limitDown = "" + dam.getLimitDown();
            String limitNorth = "" + dam.getLimitNorth();
            String limitEast = "" + dam.getLimitEast();
            String limitUp = "" + dam.getLimitUp();
            String limitSouth = "" + dam.getLimitSouth();

            int constStringWidthHalf = fontRenderer.getStringWidth(" < X < ") / 2;
            fontRenderer.drawString(" < X < ", centerX - constStringWidthHalf, guiTop + 38, Color.BLACK.getRGB());
            fontRenderer.drawString(" < Y < ", centerX - constStringWidthHalf, guiTop + 68, Color.BLACK.getRGB());
            fontRenderer.drawString(" < Z < ", centerX - constStringWidthHalf, guiTop + 98, Color.BLACK.getRGB());

            int stringWidth = fontRenderer.getStringWidth(limitWest);
            drawHorizontalLine(
                    guiLeft + 57,
                    centerX - constStringWidthHalf - stringWidth - 5,
                    guiTop + 41,
                    lineGrey.getRGB());
            fontRenderer.drawString(
                    limitWest,
                    centerX - constStringWidthHalf - stringWidth,
                    guiTop + 38,
                    Color.BLACK.getRGB());

            stringWidth = fontRenderer.getStringWidth(limitDown);
            drawHorizontalLine(
                    guiLeft + 57,
                    centerX - constStringWidthHalf - stringWidth - 5,
                    guiTop + 71,
                    lineGrey.getRGB());
            fontRenderer.drawString(
                    limitDown,
                    centerX - constStringWidthHalf - stringWidth,
                    guiTop + 68,
                    Color.BLACK.getRGB());

            stringWidth = fontRenderer.getStringWidth(limitNorth);
            drawHorizontalLine(
                    guiLeft + 57,
                    centerX - constStringWidthHalf - stringWidth - 5,
                    guiTop + 101,
                    lineGrey.getRGB());
            fontRenderer.drawString(
                    limitNorth,
                    centerX - constStringWidthHalf - stringWidth,
                    guiTop + 98,
                    Color.BLACK.getRGB());

            stringWidth = fontRenderer.getStringWidth(limitEast);
            drawHorizontalLine(
                    centerX + constStringWidthHalf + stringWidth + 5,
                    guiLeft + xSize - 57,
                    guiTop + 41,
                    lineGrey.getRGB());
            fontRenderer.drawString(limitEast, centerX + constStringWidthHalf, guiTop + 38, Color.BLACK.getRGB());

            stringWidth = fontRenderer.getStringWidth(limitUp);
            drawHorizontalLine(
                    centerX + constStringWidthHalf + stringWidth + 5,
                    guiLeft + xSize - 57,
                    guiTop + 71,
                    lineGrey.getRGB());
            fontRenderer.drawString(limitUp, centerX + constStringWidthHalf, guiTop + 68, Color.BLACK.getRGB());

            stringWidth = fontRenderer.getStringWidth(limitSouth);
            drawHorizontalLine(
                    centerX + constStringWidthHalf + stringWidth + 5,
                    guiLeft + xSize - 57,
                    guiTop + 101,
                    lineGrey.getRGB());
            fontRenderer.drawString(limitSouth, centerX + constStringWidthHalf, guiTop + 98, Color.BLACK.getRGB());

            // Reset color
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        }

        for (HEPopupLimitGui limitGui : limitGuis) {
            limitGui.draw(mouseX, mouseY);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        // There is a translation in GL before this call. Revert it for the duration of this method
        GL11.glTranslatef((float) -this.guiLeft, (float) -this.guiTop, 0.0f);
        widgetModes.drawTooltip(Minecraft.getMinecraft().fontRenderer, mouseX, mouseY, width);
        GL11.glTranslatef((float) this.guiLeft, (float) this.guiTop, 0.0f);
    }

    @Override
    protected void mouseClicked(int pixelX, int pixelY, int mouseButtonId) {
        super.mouseClicked(pixelX, pixelY, mouseButtonId);
        for (HEPopupLimitGui limitGui : limitGuis) {
            limitGui.mouseClicked(pixelX, pixelY, mouseButtonId);
        }
    }

    @Override
    protected void keyTyped(char c, int keyCode) {
        if (!passKeyTypesToLimitGuis(c, keyCode)) {
            super.keyTyped(c, keyCode);
        }
    }

    private boolean passKeyTypesToLimitGuis(char c, int keyCode) {
        for (HEPopupLimitGui limitGui : limitGuis) {
            if (limitGui.keyTyped(c, keyCode)) {
                return true;
            }
        }
        return false;
    }

    private boolean isAnyLimitGuiOpen() {
        for (HEPopupLimitGui limitGui : limitGuis) {
            if (limitGui.visible) {
                return true;
            }
        }
        return false;
    }
}
