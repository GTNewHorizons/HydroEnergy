package com.sinthoras.hydroenergy.mixins.minecraft;

import com.sinthoras.hydroenergy.client.light.HELightSMPHooks;
import com.sinthoras.hydroenergy.hooks.HEHooksUtil;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.BlockSnapshot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value = World.class)
public class WorldMixin {

    // Redirect getBlock to check for custom water
    @Redirect(method = "handleMaterialAcceleration",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getBlock(III)Lnet/minecraft/block/Block;"),
            require = 1)
    private Block onHandleMaterialAccelerationGetBlock(World world, int blockX, int blockY, int blockZ) {
        return HEHooksUtil.getBlockForWorldAndEntity(world.getBlock(blockY, blockY, blockZ), blockY);
    }

    // Redirect getBlock to check for custom water
    @Redirect(method = "isAnyLiquid",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getBlock(III)Lnet/minecraft/block/Block;"),
            require = 1)
    private Block onIsAnyLiquidGetBlock(World world, int blockX, int blockY, int blockZ) {
        return HEHooksUtil.getBlockForWorldAndEntity(world.getBlock(blockY, blockY, blockZ), blockY);
    }

    // Notify water cache about block change
    @Inject(method = "setBlock(IIILnet/minecraft/block/Block;II)Z",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/Chunk;func_150807_a(IIILnet/minecraft/block/Block;I)Z", shift = At.Shift.AFTER),
            locals = LocalCapture.CAPTURE_FAILEXCEPTION,
            require = 1)
    private void onSetBlock(int blockX, int blockY, int blockZ, Block newBlock, int unused1, int unused2, CallbackInfoReturnable<Boolean> callbackInfoReturnable, Chunk chunk, Block oldBlock, BlockSnapshot blockSnapshot) {
        HELightSMPHooks.onSetBlock((World) ((Object) this), blockX, blockY, blockZ, newBlock, oldBlock);
    }
}
