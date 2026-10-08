package com.sinthoras.hydroenergy.mixins.late;

import net.minecraft.world.EnumSkyBlock;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.cardinalstar.cubicchunks.world.cube.Cube;
import com.sinthoras.hydroenergy.client.light.HELightManager;

/** Cube packets bypass Chunk.fillChunk, including replacements of already loaded cubes. */
@Mixin(value = Cube.class, remap = false)
public class CubeMixin {

    @Inject(method = "markForRenderUpdate", at = @At("HEAD"), require = 1)
    private void onCubeData(CallbackInfo ci) {
        Cube cube = (Cube) (Object) this;
        if (cube.getWorld().isRemote) {
            HELightManager.onSectionDataLoad(cube.getWorld(), cube.getX(), cube.getY(), cube.getZ());
        }
    }

    @Inject(method = "onCubeUnload", at = @At("HEAD"), require = 1)
    private void onCubeUnload(CallbackInfo ci) {
        Cube cube = (Cube) (Object) this;
        if (cube.getWorld().isRemote) {
            HELightManager.onSectionUnload(cube.getX(), cube.getY(), cube.getZ());
        }
    }

    @Inject(method = "setLightFor", at = @At("RETURN"), require = 1)
    private void onCubeLight(EnumSkyBlock type, int x, int y, int z, int light, CallbackInfo ci) {
        Cube cube = (Cube) (Object) this;
        if (cube.getWorld().isRemote && type == EnumSkyBlock.Sky) {
            HELightManager.onSectionLightUpdate(
                    cube.getWorld(),
                    (cube.getX() << 4) + (x & 15),
                    (cube.getY() << 4) + (y & 15),
                    (cube.getZ() << 4) + (z & 15));
        }
    }
}
