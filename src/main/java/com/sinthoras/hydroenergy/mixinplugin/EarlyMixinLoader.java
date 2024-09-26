package com.sinthoras.hydroenergy.mixinplugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.gtnewhorizon.gtnhmixins.IEarlyMixinLoader;

import cpw.mods.fml.relauncher.FMLLaunchHandler;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

@IFMLLoadingPlugin.MCVersion("1.7.10")
public class EarlyMixinLoader implements IFMLLoadingPlugin, IEarlyMixinLoader {

    @Override
    public String[] getASMTransformerClass() {
        return null;
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {}

    @Override
    public String getAccessTransformerClass() {
        return null;
    }

    @Override
    public String getMixinConfig() {
        return "mixins.hydroenergy.early.json";
    }

    @Override
    public List<String> getMixins(Set<String> loadedCoreMods) {
        final List<String> mixins = new ArrayList<>();
        if (FMLLaunchHandler.side().isClient()) {
            mixins.add("ActiveRenderInfoMixin");
            mixins.add("ChunkClientMixin");
            mixins.add("ChunkProviderClientMixin");
            mixins.add("EntityRendererMixin");
            mixins.add("WorldRendererMixin");
            mixins.add("WorldMixinClient");
        }
        mixins.add("ChunkMixin");
        mixins.add("EntityMixin");
        mixins.add("WorldMixin");
        return mixins;
    }
}
