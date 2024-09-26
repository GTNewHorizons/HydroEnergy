package com.sinthoras.hydroenergy.mixins.early;

import net.minecraft.client.multiplayer.ChunkProviderClient;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.sinthoras.hydroenergy.client.light.HELightManager;

@Mixin(ChunkProviderClient.class)
public class ChunkProviderClientMixin {

    // Notify light cache of unload event
    @Inject(method = "unloadChunk", at = @At("RETURN"), require = 1)
    private void onUnloadChunk(int chunkX, int chunkZ, CallbackInfo callbackInfo) {
        HELightManager.onChunkUnload(chunkX, chunkZ);
    }
}
