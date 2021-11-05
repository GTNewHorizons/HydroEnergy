package com.sinthoras.hydroenergy.mixinplugin;

import java.util.Arrays;
import java.util.List;

import static com.sinthoras.hydroenergy.mixinplugin.TargetedMod.*;

public enum Mixin {

    //
    // IMPORTANT: Do not make any references to any mod from this file. This file is loaded quite early on and if
    // you refer to other mods you load them as well. The consequence is: You can't inject any previously loaded classes!
    // Exception: Tags.java, as long as it is used for Strings only!
    //

    GT_PollutionRendererMixin("gregtech.GT_PollutionRendererMixin", GREGTECH),

    ActiveRenderInfoMixin("minecraft.ActiveRenderInfoMixin", VANILLA),
    ChunkMixin("minecraft.ChunkMixin", VANILLA),
    ChunkProviderClientMixin("minecraft.ChunkProviderClientMixin", VANILLA),
    EntityMixin("minecraft.EntityMixin", VANILLA),
    EntityRendererMixin("minecraft.EntityRendererMixin", VANILLA),
    WorldMixin("minecraft.WorldMixin", VANILLA),
    WorldRendererMixin("minecraft.WorldRendererMixin", VANILLA);

    public final String mixinClass;
    public final List<TargetedMod> targetedMods;

    Mixin(String mixinClass, TargetedMod... targetedMods) {
        this.mixinClass = mixinClass;
        this.targetedMods = Arrays.asList(targetedMods);
    }
}
