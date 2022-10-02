package com.sinthoras.hydroenergy.server.mytown2;

import mytown.entities.TownBlock;
import mytown.new_datasource.MyTownUniverse;

public class HEMyTown2IntegrationImplementation extends HEMyTown2Integration {

    public boolean hasPlayerModificationRightsForChunk(String residentName, int dimension, int chunkX, int chunkZ) {
        TownBlock chunk = MyTownUniverse.instance.blocks.get(dimension, chunkX, chunkZ);
        if (chunk == null) {
            return true;
        }
        return chunk.getTown().residentsMap.contains(residentName);
    }
}
