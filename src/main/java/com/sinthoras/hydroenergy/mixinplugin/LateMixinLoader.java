package com.sinthoras.hydroenergy.mixinplugin;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.gtnewhorizon.gtnhmixins.ILateMixinLoader;
import com.gtnewhorizon.gtnhmixins.LateMixin;

import cpw.mods.fml.relauncher.FMLLaunchHandler;

@LateMixin
public class LateMixinLoader implements ILateMixinLoader {

    @Override
    public String getMixinConfig() {
        return "mixins.hydroenergy.late.json";
    }

    @Override
    public List<String> getMixins(Set<String> loadedMods) {
        if (FMLLaunchHandler.side().isClient()) {
            return Collections.singletonList("GT_PollutionRendererMixin");
        }
        return Collections.emptyList();
    }

}
