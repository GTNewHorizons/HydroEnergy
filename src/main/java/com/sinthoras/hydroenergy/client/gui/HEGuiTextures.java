package com.sinthoras.hydroenergy.client.gui;

import com.cleanroommc.modularui.drawable.UITexture;
import com.sinthoras.hydroenergy.HETags;

public final class HEGuiTextures {

    public static void init() {}

    public static final UITexture OVERLAY_BUTTON_DRAIN = UITexture.builder()
            .location(HETags.MODID, "textures/gui/he_water_config_drain.png").fullImage().canApplyTheme().build();

    public static final UITexture OVERLAY_BUTTON_DEBUG = UITexture.builder()
            .location(HETags.MODID, "textures/gui/he_water_config_debug.png").fullImage().canApplyTheme().build();

    public static final UITexture OVERLAY_BUTTON_SPREAD = UITexture.builder()
            .location(HETags.MODID, "textures/gui/he_water_config_spread.png").fullImage().canApplyTheme().build();
}
