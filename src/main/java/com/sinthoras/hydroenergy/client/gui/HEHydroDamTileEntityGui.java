package com.sinthoras.hydroenergy.client.gui;

import static com.gtnewhorizon.gtnhlib.util.numberformatting.NumberFormatUtil.formatNumber;

import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.ForgeDirection;

import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.value.sync.EnumSyncValue;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.LongSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.ListWidget;
import com.cleanroommc.modularui.widgets.ToggleButton;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;
import com.sinthoras.hydroenergy.HE;
import com.sinthoras.hydroenergy.blocks.HEHydroDamTileEntity;
import com.sinthoras.hydroenergy.client.HEClient;
import com.sinthoras.hydroenergy.client.HEDam;
import com.sinthoras.hydroenergy.config.HEConfig;
import com.sinthoras.hydroenergy.server.HEServer;

import gregtech.api.modularui2.GTGuiTextures;
import gregtech.api.util.GTUtility;
import gregtech.common.gui.modularui.multiblock.base.TTMultiblockBaseGui;
import gregtech.common.modularui2.sync.LinkedBoolValue;

public class HEHydroDamTileEntityGui extends TTMultiblockBaseGui<HEHydroDamTileEntity> {

    public HEHydroDamTileEntityGui(HEHydroDamTileEntity multiblock) {
        super(multiblock);
    }

    @Override
    protected ListWidget<IWidget, ?> createTerminalTextWidget(PanelSyncManager syncManager, ModularPanel parent) {
        LongSyncValue euStoredSyncer = syncManager.findSyncHandler("euStored", LongSyncValue.class);
        LongSyncValue euCapacitySyncer = syncManager.findSyncHandler("euCapacity", LongSyncValue.class);
        IntSyncValue inAvgSyncer = syncManager.findSyncHandler("inAvg", IntSyncValue.class);
        IntSyncValue outAvgSyncer = syncManager.findSyncHandler("outAvg", IntSyncValue.class);

        return super.createTerminalTextWidget(syncManager, parent).child(
                Flow.col().coverChildrenHeight().fullWidth().childPadding(6).marginRight(4)
                        .crossAxisAlignment(Alignment.CrossAxis.START)
                        .child(
                                IKey.dynamic(
                                        () -> EnumChatFormatting.WHITE + StatCollector.translateToLocalFormatted(
                                                "dam.gui.text.tier",
                                                GTUtility.getColoredTierNameFromTier(
                                                        (byte) multiblock.getVoltageTier())))
                                        .asWidget())
                        .child(
                                IKey.dynamic(
                                        () -> StatCollector.translateToLocalFormatted(
                                                "dam.gui.text.stored",
                                                formatNumber(euStoredSyncer.getLongValue()),
                                                formatNumber(euCapacitySyncer.getLongValue())))
                                        .color(Color.WHITE.main).asWidget().fullWidth())
                        .child(IKey.dynamic(() -> {
                            float fillMultiplier = multiblock.getFillMultiplier();
                            if (fillMultiplier > 1) {
                                return ">100.00%";
                            } else {
                                return String.format("%.2f", fillMultiplier * 100) + "%";
                            }
                        }).color(Color.WHITE.main).asWidget().fullWidth())
                        .child(
                                Flow.row().coverChildrenHeight().fullWidth()
                                        .mainAxisAlignment(Alignment.MainAxis.SPACE_BETWEEN)
                                        .child(
                                                IKey.dynamic(
                                                        () -> StatCollector.translateToLocalFormatted(
                                                                "dam.gui.text.in",
                                                                formatNumber(inAvgSyncer.getIntValue())))
                                                        .color(Color.WHITE.main).asWidget())
                                        .child(
                                                IKey.dynamic(
                                                        () -> StatCollector.translateToLocalFormatted(
                                                                "dam.gui.text.out",
                                                                formatNumber(outAvgSyncer.getIntValue())))
                                                        .color(Color.WHITE.main).asWidget()))
                        .child(
                                IKey.dynamic(
                                        () -> StatCollector.translateToLocalFormatted(
                                                "dam.gui.text.upgrade",
                                                multiblock.getVoltageTier()))
                                        .asWidget().setEnabledIf(s -> multiblock.getFillMultiplier() > 1)));
    }

    @Override
    protected Flow createRightPanelGapRow(ModularPanel parent, PanelSyncManager syncManager) {
        IPanelHandler configPanel = syncManager.syncedPanel(
                "heDam_config",
                true,
                (p_syncManager, syncHandler) -> createConfigPanel(parent, syncManager, p_syncManager));

        return super.createRightPanelGapRow(parent, syncManager).child(
                new ButtonWidget<>().onMousePressed(i -> configPanel.togglePanel())
                        .addTooltipLine(IKey.lang("dam.gui.tooltip.configure_dam"))
                        .backgroundOverlay(GTGuiTextures.OVERLAY_BUTTON_SCREWDRIVER));
    }

    public ModularPanel createConfigPanel(ModularPanel parent, PanelSyncManager syncManager,
            PanelSyncManager p_syncManager) {
        IntSyncValue waterIdSyncer = syncManager.findSyncHandler("waterId", IntSyncValue.class);
        // this needs to be here for proper functionality
        EnumSyncValue<HE.DamMode, ?> modeSyncer = new EnumSyncValue<>(
                HE.DamMode.class,
                () -> HEClient.getDam(waterIdSyncer.getIntValue()).getMode(),
                val -> {
                    HEDam dam = HEClient.getDam(waterIdSyncer.getIntValue());
                    dam.setMode(val);
                    dam.applyChanges();
                },
                () -> HEServer.instance.getMode(waterIdSyncer.getIntValue()),
                val -> HEServer.instance.setMode(waterIdSyncer.getIntValue(), val)).allowC2S();
        p_syncManager.syncValue("mode", modeSyncer);

        ModularPanel panel = new ModularPanel("heDam_config") {

            @Override
            public boolean isDraggable() {
                return false;
            }

            @Override
            public boolean disablePanelsBelow() {
                return true;
            }
        }.width(200).coverChildrenHeight().relative(parent).padding(6);

        Flow mainColumn = Flow.column().coverChildrenHeight().fullWidth().childPadding(8);

        Flow topRow = Flow.row().coverChildrenHeight().fullWidth().mainAxisAlignment(Alignment.MainAxis.SPACE_BETWEEN);
        topRow.child(IKey.lang("dam.gui.config.text.spread_limit").asWidget());

        // mode buttons
        Flow buttonRow = Flow.row().coverChildren().childPadding(2);
        buttonRow.child(
                new ToggleButton().value(LinkedBoolValue.of(modeSyncer, HE.DamMode.DRAIN))
                        .backgroundOverlay(HEGuiTextures.OVERLAY_BUTTON_DRAIN)
                        .addTooltipLine(StatCollector.translateToLocal("dam.gui.config.tooltip.drain")));
        buttonRow.child(
                new ToggleButton().value(LinkedBoolValue.of(modeSyncer, HE.DamMode.DEBUG))
                        .backgroundOverlay(HEGuiTextures.OVERLAY_BUTTON_DEBUG)
                        .addTooltipLine(StatCollector.translateToLocal("dam.gui.config.tooltip.debug")));
        buttonRow.child(
                new ToggleButton().value(LinkedBoolValue.of(modeSyncer, HE.DamMode.SPREAD))
                        .backgroundOverlay(HEGuiTextures.OVERLAY_BUTTON_SPREAD)
                        .addTooltipLine(StatCollector.translateToLocal("dam.gui.config.tooltip.spread")));
        topRow.child(buttonRow);

        mainColumn.child(topRow);
        // do this for x,y,z order
        mainColumn.child(createLimitRow(waterIdSyncer, 2));
        mainColumn.child(createLimitRow(waterIdSyncer, 0));
        mainColumn.child(createLimitRow(waterIdSyncer, 1));

        return panel.child(mainColumn);
    }

    private Flow createLimitRow(IntSyncValue waterIdSyncer, int index) {
        String direction = StatCollector.translateToLocal("dam.gui.config.text.direction." + index);

        Flow limitRow = Flow.row().fullWidth().coverChildrenHeight().childPadding(2);

        limitRow.child(createLimitEditor(waterIdSyncer, direction, 2 * index));
        limitRow.child(
                IKey.dynamic(() -> StatCollector.translateToLocalFormatted("dam.gui.config.text.interval", direction))
                        .asWidget());
        limitRow.child(createLimitEditor(waterIdSyncer, direction, 2 * index + 1));

        return limitRow;
    }

    private TextFieldWidget createLimitEditor(IntSyncValue waterIdSyncer, String direction, int index) {
        ForgeDirection forgeDirection = ForgeDirection.getOrientation(index);

        TextFieldWidget textFieldWidget = new TextFieldWidget().value(createLimitSyncer(waterIdSyncer, forgeDirection))
                .expanded().formatAsInteger(true)
                .setTextAlignment(index % 2 == 0 ? Alignment.CenterRight : Alignment.CenterLeft).addTooltipLine(
                        IKey.lang(
                                "dam.gui.config.text.limit",
                                StatCollector.translateToLocal("dam.gui.config.text.limit." + index),
                                index % 2 == 0 ? "-" : "+",
                                direction));

        if (baseMetaTileEntity.isClientSide()) {
            int[] minMax = getLimitMinMax(waterIdSyncer, forgeDirection);
            textFieldWidget.numbersInt(minMax[0], minMax[1]);
        } else textFieldWidget.numbersInt();

        return textFieldWidget;
    }

    private int[] getLimitMinMax(IntSyncValue waterIdSyncer, ForgeDirection direction) {
        int[] minMax = new int[2];
        HEDam dam = HEClient.getDam(waterIdSyncer.getIntValue());

        switch (direction) {
            case DOWN -> {
                minMax[0] = Math.max(dam.getBlockY() - HEConfig.maxWaterSpreadDown, 0);
                minMax[1] = dam.getBlockY();
            }
            case UP -> {
                minMax[0] = dam.getBlockY();
                minMax[1] = Math.min(dam.getBlockY() + HEConfig.maxWaterSpreadUp, 255);
            }
            case NORTH -> {
                minMax[0] = dam.getBlockZ() - HEConfig.maxWaterSpreadNorth;
                minMax[1] = dam.getBlockZ();
            }
            case SOUTH -> {
                minMax[0] = dam.getBlockZ();
                minMax[1] = dam.getBlockZ() + HEConfig.maxWaterSpreadSouth;
            }
            case WEST -> {
                minMax[0] = dam.getBlockX() - HEConfig.maxWaterSpreadWest;
                minMax[1] = dam.getBlockX();
            }
            case EAST -> {
                minMax[0] = dam.getBlockX();
                minMax[1] = dam.getBlockX() + HEConfig.maxWaterSpreadEast;
            }
            default -> throw new IllegalArgumentException("Invalid direction!");
        }

        return minMax;
    }

    private IntSyncValue createLimitSyncer(IntSyncValue waterIdSyncer, ForgeDirection direction) {
        return (switch (direction) {
            case DOWN -> new IntSyncValue(() -> HEClient.getDam(waterIdSyncer.getIntValue()).getLimitDown(), val -> {
                HEDam dam = HEClient.getDam(waterIdSyncer.getIntValue());
                dam.setLimitDown(val);
                dam.applyChanges();
            },
                    () -> HEServer.instance.getWaterLimitDown(waterIdSyncer.getIntValue()),
                    val -> HEServer.instance.setWaterLimitDown(waterIdSyncer.getIntValue(), val));
            case UP -> new IntSyncValue(() -> HEClient.getDam(waterIdSyncer.getIntValue()).getLimitUp(), val -> {
                HEDam dam = HEClient.getDam(waterIdSyncer.getIntValue());
                dam.setLimitUp(val);
                dam.applyChanges();
            },
                    () -> HEServer.instance.getWaterLimitUp(waterIdSyncer.getIntValue()),
                    val -> HEServer.instance.setWaterLimitUp(waterIdSyncer.getIntValue(), val));
            case NORTH -> new IntSyncValue(() -> HEClient.getDam(waterIdSyncer.getIntValue()).getLimitNorth(), val -> {
                HEDam dam = HEClient.getDam(waterIdSyncer.getIntValue());
                dam.setLimitNorth(val);
                dam.applyChanges();
            },
                    () -> HEServer.instance.getWaterLimitNorth(waterIdSyncer.getIntValue()),
                    val -> HEServer.instance.setWaterLimitNorth(waterIdSyncer.getIntValue(), val));
            case SOUTH -> new IntSyncValue(() -> HEClient.getDam(waterIdSyncer.getIntValue()).getLimitSouth(), val -> {
                HEDam dam = HEClient.getDam(waterIdSyncer.getIntValue());
                dam.setLimitSouth(val);
                dam.applyChanges();
            },
                    () -> HEServer.instance.getWaterLimitSouth(waterIdSyncer.getIntValue()),
                    val -> HEServer.instance.setWaterLimitSouth(waterIdSyncer.getIntValue(), val));
            case WEST -> new IntSyncValue(() -> HEClient.getDam(waterIdSyncer.getIntValue()).getLimitWest(), val -> {
                HEDam dam = HEClient.getDam(waterIdSyncer.getIntValue());
                dam.setLimitWest(val);
                dam.applyChanges();
            },
                    () -> HEServer.instance.getWaterLimitWest(waterIdSyncer.getIntValue()),
                    val -> HEServer.instance.setWaterLimitWest(waterIdSyncer.getIntValue(), val));
            case EAST -> new IntSyncValue(() -> HEClient.getDam(waterIdSyncer.getIntValue()).getLimitEast(), val -> {
                HEDam dam = HEClient.getDam(waterIdSyncer.getIntValue());
                dam.setLimitEast(val);
                dam.applyChanges();
            },
                    () -> HEServer.instance.getWaterLimitEast(waterIdSyncer.getIntValue()),
                    val -> HEServer.instance.setWaterLimitEast(waterIdSyncer.getIntValue(), val));
            default -> throw new IllegalArgumentException("Invalid direction!");
        }).allowC2S();
    }

    @Override
    protected void registerSyncValues(PanelSyncManager syncManager) {
        super.registerSyncValues(syncManager);

        syncManager.syncValue("euStored", new LongSyncValue(multiblock::getEuStored, multiblock::setEuStored));
        syncManager.syncValue("euCapacity", new LongSyncValue(multiblock::getEuCapacity, multiblock::setEuCapacity));
        syncManager.syncValue("inAvg", new IntSyncValue(multiblock::getEuPerTickIn));
        syncManager.syncValue("outAvg", new IntSyncValue(multiblock::getEuPerTickOut));
        syncManager.syncValue("waterId", new IntSyncValue(multiblock::getWaterId, multiblock::setWaterId));
    }
}
