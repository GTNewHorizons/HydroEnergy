package com.sinthoras.hydroenergy.mixinplugin;

import java.util.ArrayList;
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
        List<String> mixins = new ArrayList<>();
        if (FMLLaunchHandler.side().isClient()) {
            mixins.add("GT_PollutionRendererMixin");
            if (loadedMods.contains("cubicchunks")) mixins.add("CubeMixin");
        }
        return mixins;
    }

}
