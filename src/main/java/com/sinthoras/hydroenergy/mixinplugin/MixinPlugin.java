package com.sinthoras.hydroenergy.mixinplugin;

import com.google.common.collect.Lists;
import com.sinthoras.hydroenergy.HETags;
import net.minecraft.launchwrapper.Launch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import ru.timeconqueror.spongemixins.MinecraftURLClassPath;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Set;

public class MixinPlugin implements IMixinConfigPlugin {

    private static Logger LOG = LogManager.getLogger(HETags.MODID);

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
        final boolean isDevelopmentEnvironment = (boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");

        List<String> mixins = Lists.newArrayList(
                "minecraft.WorldMixin",
                "minecraft.WorldRendererMixin",
                "minecraft.EntityMixin",
                "minecraft.EntityRendererMixin",
                "minecraft.ChunkMixin",
                "minecraft.ChunkProviderClientMixin",
                "minecraft.ActiveRenderInfoMixin"
        );

        if(isDevelopmentEnvironment || loadJar("gregtech")) {
            LOG.info("Found GregTech! Integrating now...");
            mixins.add("gregtech.GT_PollutionRendererMixin");
        }
        else {
            LOG.info("Could not find GregTech! Skipping integration....");
        }

        return mixins;
    }

    private boolean loadJar(final String jarName) {
        try {
            File jar = MinecraftURLClassPath.getJarInModPath(jarName);
            if(jar == null) {
                LOG.info("Jar not found: " + jarName);
                return false;
            }

            LOG.info("Attempting to add " + jar.toString() + " to the URL Class Path");
            if(!jar.exists()) {
                throw new FileNotFoundException(jar.toString());
            }
            MinecraftURLClassPath.addJar(jar);
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}
