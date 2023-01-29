package com.sinthoras.hydroenergy.api;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraftforge.client.event.EntityViewRenderEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HEGetMaterialUtil {

    private static Logger LOG = LogManager.getLogger("HE API");

    public static Material getMaterialWrapper(Block block, int blockY) {
        if (block instanceof IHEHasCustomMaterialCalculation) {
            return ((IHEHasCustomMaterialCalculation) block).getMaterial(blockY);
        } else {
            return block.getMaterial();
        }
    }

    public static Material getMaterialWrapper(Block block, double blockY) {
        if (block instanceof IHEHasCustomMaterialCalculation) {
            LOG.info("triggered");
            return ((IHEHasCustomMaterialCalculation) block).getMaterial(blockY);
        } else {
            return block.getMaterial();
        }
    }

    public static Material getMaterialWrapper(EntityViewRenderEvent.FogColors event) {
        if (event.block instanceof IHEHasCustomMaterialCalculation) {
            return ((IHEHasCustomMaterialCalculation) event.block)
                    .getMaterial(event.entity.posY + event.entity.getEyeHeight());
        } else {
            return event.block.getMaterial();
        }
    }
}
