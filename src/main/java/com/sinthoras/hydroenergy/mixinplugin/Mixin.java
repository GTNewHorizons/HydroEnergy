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

    GT_PollutionRendererMixin("gregtech.GT_PollutionRendererMixin", true, GREGTECH),

    ActiveRenderInfoMixin("minecraft.ActiveRenderInfoMixin", true, VANILLA),
    ChunkMixin("minecraft.ChunkMixin", false, VANILLA),
    ChunkProviderClientMixin("minecraft.ChunkProviderClientMixin", true, VANILLA),
    EntityMixin("minecraft.EntityMixin", false, VANILLA),
    EntityRendererMixin("minecraft.EntityRendererMixin", true, VANILLA),
    WorldMixin("minecraft.WorldMixin", false, VANILLA),
    WorldRendererMixin("minecraft.WorldRendererMixin", true, VANILLA);

    public final String mixinClass;
    public final List<TargetedMod> targetedMods;
    // Injecting into @SideOnly(Side.Client) classes will crash the server. Flag them as clientSideOnly!
    public final boolean clientSideOnly;

    Mixin(String mixinClass, boolean clientSideOnly, TargetedMod... targetedMods) {
        this.mixinClass = mixinClass;
        this.targetedMods = Arrays.asList(targetedMods);
        this.clientSideOnly = clientSideOnly;
    }
}
