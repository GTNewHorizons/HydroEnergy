package com.sinthoras.hydroenergy.asm;

import com.sinthoras.hydroenergy.client.renderer.HETessalator;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.EntityLivingBase;

public class HEStaticInjectors {

    public static void renderEntities(RenderGlobal renderGlobal, EntityLivingBase entitylivingbase, ICamera frustrum, float p_78471_1_) {
        renderGlobal.renderEntities(entitylivingbase, frustrum, p_78471_1_);
        HETessalator.render(frustrum);
    }

}
