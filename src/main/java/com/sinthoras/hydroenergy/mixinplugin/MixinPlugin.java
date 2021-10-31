package com.sinthoras.hydroenergy.mixinplugin;

import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MixinPlugin implements IMixinConfigPlugin {

    @Override
    public void onLoad(String mixinPackage) {

    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        List<String> mixins = new ArrayList<>();

        // TODO: Filter client/server and debug + jar loading issues
        mixins.add("minecraft.WorldMixin");
        mixins.add("minecraft.WorldRendererMixin");
        mixins.add("minecraft.EntityMixin");
        mixins.add("minecraft.EntityRendererMixin");
        mixins.add("minecraft.ChunkMixin");
        mixins.add("minecraft.ChunkProviderClientMixin");
        mixins.add("minecraft.ActiveRenderInfoMixin");

        mixins.add("gregtech.GT_PollutionRendererMixin");

        return mixins;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}
