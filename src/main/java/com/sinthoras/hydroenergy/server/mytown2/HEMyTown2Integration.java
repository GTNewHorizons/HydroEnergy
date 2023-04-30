package com.sinthoras.hydroenergy.server.mytown2;

import com.sinthoras.hydroenergy.HE;
import com.sinthoras.hydroenergy.HETags;

import cpw.mods.fml.common.Loader;

public abstract class HEMyTown2Integration {

    private static HEMyTown2Integration instance = null;

    public static HEMyTown2Integration getInstance() {
        if (instance == null) {
            if (Loader.isModLoaded(HETags.MyTown2_MODID)) {
                try {
                    instance = Class.forName("com.sinthoras.hydroenergy.server.HEMyTown2Implementation")
                            .asSubclass(HEMyTown2Integration.class).newInstance();
                } catch (Exception e) {
                    HE.warn("Could not initialize MyTown2 Integration. Pretending MyTown2 is not loaded!");
                    instance = new HEMyTown2IntegrationDummy();
                }
            } else {
                instance = new HEMyTown2IntegrationDummy();
            }
        }
        return instance;
    }

    public abstract boolean hasPlayerModificationRightsForChunk(String residentName, int dimension, int chunkX,
            int chunkZ);
}
