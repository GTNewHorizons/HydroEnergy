package com.sinthoras.hydroenergy.client.gui;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;

import com.sinthoras.hydroenergy.HE;
import com.sinthoras.hydroenergy.HETags;
import com.sinthoras.hydroenergy.client.gui.widgets.HENumberInput;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class HEPopupLimitGui extends Gui {

    private static ResourceLocation limitBackgroundTextureLocation = new ResourceLocation(
            HETags.MODID,
            HE.damLimitBackgroundLocation);

    private GuiButton minus1000;
    private GuiButton minus100;
    private GuiButton minus10;
    private GuiButton minus1;
    private GuiButton plus1;
    private GuiButton plus10;
    private GuiButton plus100;
    private GuiButton plus1000;
    private GuiButton ok;
    private GuiButton cancel;
    private HENumberInput textField;

    public static final int xSize = 168;
    public static final int ySize = 140;

    public boolean visible;

    private int guiLeft;
    private int guiTop;
    private boolean valueChanged;
    private int originalValue;
    private int currentValue;
    private int lowerLimit = Integer.MIN_VALUE;
    private int upperLimit = Integer.MAX_VALUE;
    private String title = "";

    public HEPopupLimitGui(String title, int guiLeft, int guiTop, int originalValue) {
        this.title = title;
        this.guiLeft = guiLeft;
        this.guiTop = guiTop;
        this.originalValue = originalValue;
        this.currentValue = originalValue;
        this.visible = false;
        this.valueChanged = false;
    }

    public void init(int id, List<GuiButton> buttonList) {
        int pixelX = guiLeft + 10;
        int height = 20;
        int pixelY = guiTop + 20 + 3 * height;
        int width = 39;

        minus1000 = new GuiButton(id, pixelX, pixelY, width, height, "-1000");
        buttonList.add(minus1000);

        pixelX += width + 10;
        width = 32;
        minus100 = new GuiButton(id, pixelX, pixelY, width, height, "-100");
        buttonList.add(minus100);

        pixelX += width + 10;
        width = 27;
        minus10 = new GuiButton(id, pixelX, pixelY, width, height, "-10");
        buttonList.add(minus10);

        pixelX += width + 10;
        width = 20;
        minus1 = new GuiButton(id, pixelX, pixelY, width, height, "-1");
        buttonList.add(minus1);

        pixelX = guiLeft + 10;
        pixelY = guiTop + 20;
        width = 39;
        plus1000 = new GuiButton(id, pixelX, pixelY, width, height, "+1000");
        buttonList.add(plus1000);

        pixelX += width + 10;
        width = 32;
        plus100 = new GuiButton(id, pixelX, pixelY, width, height, "+100");
        buttonList.add(plus100);

        pixelX += width + 10;
        width = 27;
        plus10 = new GuiButton(id, pixelX, pixelY, width, height, "+10");
        buttonList.add(plus10);

        pixelX += width + 10;
        width = 20;
        plus1 = new GuiButton(id, pixelX, pixelY, width, height, "+1");
        buttonList.add(plus1);

        pixelX = guiLeft + 10;
        pixelY = guiTop + 5 * height + 10;
        width = 42;
        ok = new GuiButton(id, pixelX, pixelY, width, height, "OK");
        buttonList.add(ok);

        pixelX += 49 + 42 + 37 + 20 - 42;
        width = 42;
        cancel = new GuiButton(id, pixelX, pixelY, width, height, "Cancel");
        buttonList.add(cancel);

        pixelX = guiLeft + 10;
        pixelY = guiTop + 2 * height + 10;
        width = xSize - 20;
        textField = new HENumberInput(Minecraft.getMinecraft().fontRenderer, pixelX, pixelY, width, height);
        textField.setEnableBackgroundDrawing(true);
        textField.setMaxStringLength(16);
        textField.setTextColor(0xFFFFFFFF);
        textField.setText("" + originalValue);
        textField.minValue = lowerLimit;
        textField.maxValue = upperLimit;

        setVisibility(false);
    }

    public boolean getValueChangedAndReset() {
        boolean tmp = valueChanged;
        valueChanged = false;
        return tmp;
    }

    public void updateOriginalValue(int value) {
        originalValue = value;
    }

    public int getValue() {
        return currentValue;
    }

    public void show() {
        textField.setText("" + originalValue);
        setVisibility(true);
    }

    private void confirm() {
        valueChanged = originalValue != currentValue;
        originalValue = currentValue;
        setVisibility(false);
    }

    private void cancel() {
        currentValue = originalValue;
        setVisibility(false);
    }

    private void setVisibility(boolean value) {
        visible = value;

        textField.setVisible(value);
        textField.setFocused(value);

        ok.visible = value;
        cancel.visible = value;

        plus1.visible = value;
        plus10.visible = value;
        plus100.visible = value;
        plus1000.visible = value;
        minus1000.visible = value;
        minus100.visible = value;
        minus10.visible = value;
        minus1.visible = value;
    }

    private void add(int value) {
        textField.add(value);
    }

    public void mouseClicked(int pixelX, int pixelY, int mouseButtonId) {
        if (visible) {
            textField.mouseClicked(pixelX, pixelY, mouseButtonId);
        }
    }

    public boolean keyTyped(char c, int keyCode) {
        if (visible) {
            if (keyCode == 1 || keyCode == Minecraft.getMinecraft().gameSettings.keyBindInventory.getKeyCode()) {
                cancel();
            } else {
                textField.textboxKeyTyped(c, keyCode);
                currentValue = textField.getValue();
            }
            return true;
        }
        return false;
    }

    public void actionPerformed(final GuiButton button) {
        if (visible) {
            if (button == ok) {
                confirm();
            } else if (button == cancel) {
                cancel();
            } else if (button == minus1000) {
                add(-1000);
            } else if (button == minus100) {
                add(-100);
            } else if (button == minus10) {
                add(-10);
            } else if (button == minus1) {
                add(-1);
            } else if (button == plus1) {
                add(1);
            } else if (button == plus10) {
                add(10);
            } else if (button == plus100) {
                add(100);
            } else if (button == plus1000) {
                add(1000);
            }
        }
        currentValue = textField.getValue();
    }

    public void draw(int mouseX, int mouseY) {
        if (visible) {
            Minecraft minecraft = Minecraft.getMinecraft();

            minecraft.getTextureManager().bindTexture(limitBackgroundTextureLocation);
            drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, xSize, ySize);

            int width = minecraft.fontRenderer.getStringWidth(title);
            minecraft.fontRenderer.drawString(title, guiLeft + xSize / 2 - width / 2, guiTop + 6, 0x000000);

            textField.drawTextBox();
        }
    }
}
