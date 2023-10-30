package com.sinthoras.hydroenergy.hooks;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.FluidRegistry;

import com.sinthoras.hydroenergy.HE;
import com.sinthoras.hydroenergy.HETags;
import com.sinthoras.hydroenergy.blocks.HEBlockRecipes;
import com.sinthoras.hydroenergy.blocks.HEHydroDamTileEntity;
import com.sinthoras.hydroenergy.blocks.HEHydroPumpTileEntity;
import com.sinthoras.hydroenergy.blocks.HEHydroTurbineTileEntity;
import com.sinthoras.hydroenergy.blocks.HEWaterStill;
import com.sinthoras.hydroenergy.config.HEConfig;
import com.sinthoras.hydroenergy.network.packet.HEPacketChunkUpdate;
import com.sinthoras.hydroenergy.network.packet.HEPacketConfigRequest;
import com.sinthoras.hydroenergy.network.packet.HEPacketConfigUpdate;
import com.sinthoras.hydroenergy.network.packet.HEPacketSynchronize;
import com.sinthoras.hydroenergy.network.packet.HEPacketWaterUpdate;
import com.sinthoras.hydroenergy.server.HEServer;
import com.sinthoras.hydroenergy.server.commands.HECommandDebug;
import com.sinthoras.hydroenergy.server.commands.HECommandListControllers;
import com.sinthoras.hydroenergy.server.commands.HECommandSetWater;

import codechicken.nei.api.API;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerAboutToStartEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppedEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;

public class HEHooksShared {

    // preInit "Run before anything else. Read your config, create blocks, items,
    // etc, and register them with the GameRegistry."
    public void fmlLifeCycleEvent(FMLPreInitializationEvent event) {
        HEConfig.syncronizeConfiguration(event.getSuggestedConfigurationFile());

        HE.network = NetworkRegistry.INSTANCE.newSimpleChannel(HETags.MODID);
        int networkId = 0;
        HE.network.registerMessage(
                HEPacketWaterUpdate.Handler.class,
                HEPacketWaterUpdate.class,
                networkId++,
                Side.CLIENT);
        HE.network.registerMessage(
                HEPacketSynchronize.Handler.class,
                HEPacketSynchronize.class,
                networkId++,
                Side.CLIENT);
        HE.network.registerMessage(
                HEPacketConfigUpdate.Handler.class,
                HEPacketConfigUpdate.class,
                networkId++,
                Side.CLIENT);
        HE.network.registerMessage(
                HEPacketConfigRequest.Handler.class,
                HEPacketConfigRequest.class,
                networkId++,
                Side.SERVER);
        HE.network.registerMessage(
                HEPacketChunkUpdate.Handler.class,
                HEPacketChunkUpdate.class,
                networkId++,
                Side.CLIENT);

        HE.info("The subsequent " + HEConfig.maxDams + " liquid errors are intendend. Please ignore...");

        for (int waterId = 0; waterId < HEConfig.maxDams; waterId++) {
            HE.waterBlocks[waterId] = new HEWaterStill(waterId);
            GameRegistry.registerBlock(HE.waterBlocks[waterId], HE.waterBlocks[waterId].getUnlocalizedName());
            HE.waterBlockIds[waterId] = Block.blockRegistry.getIDForObject(HE.waterBlocks[waterId]);
            API.hideItem(new ItemStack(HE.waterBlocks[waterId]));
        }

        FluidRegistry.registerFluid(HE.pressurizedWater);
    }

    // load "Do your mod setup. Build whatever data structures you care about. Register recipes."
    public void fmlLifeCycleEvent(FMLInitializationEvent event) {
        FMLCommonHandler.instance().bus().register(new HEHooksFML());
        MinecraftForge.EVENT_BUS.register(new HEHooksEVENT_BUS());

        HE.hydroDamControllerBlock = new HEHydroDamTileEntity(HEConfig.blockIdOffset, "he_dam", "Hydro Dam")
                .getStackForm(1L);

        // Instantiate all variants in case a server has a different configuration the block is still available
        HE.hydroPumpBlocks[1] = new HEHydroPumpTileEntity.LV(HEConfig.blockIdOffset + 1).getStackForm(1L);
        HE.hydroTurbineBlocks[1] = new HEHydroTurbineTileEntity.LV(HEConfig.blockIdOffset + 17).getStackForm(1L);
        HE.hydroPumpBlocks[2] = new HEHydroPumpTileEntity.MV(HEConfig.blockIdOffset + 1).getStackForm(1L);
        HE.hydroTurbineBlocks[2] = new HEHydroTurbineTileEntity.MV(HEConfig.blockIdOffset + 17).getStackForm(1L);
        HE.hydroPumpBlocks[3] = new HEHydroPumpTileEntity.HV(HEConfig.blockIdOffset + 1).getStackForm(1L);
        HE.hydroTurbineBlocks[3] = new HEHydroTurbineTileEntity.HV(HEConfig.blockIdOffset + 17).getStackForm(1L);
        HE.hydroPumpBlocks[4] = new HEHydroPumpTileEntity.EV(HEConfig.blockIdOffset + 1).getStackForm(1L);
        HE.hydroTurbineBlocks[4] = new HEHydroTurbineTileEntity.EV(HEConfig.blockIdOffset + 17).getStackForm(1L);
        HE.hydroPumpBlocks[5] = new HEHydroPumpTileEntity.IV(HEConfig.blockIdOffset + 1).getStackForm(1L);
        HE.hydroTurbineBlocks[5] = new HEHydroTurbineTileEntity.IV(HEConfig.blockIdOffset + 17).getStackForm(1L);
        HE.hydroPumpBlocks[6] = new HEHydroPumpTileEntity.LuV(HEConfig.blockIdOffset + 1).getStackForm(1L);
        HE.hydroTurbineBlocks[6] = new HEHydroTurbineTileEntity.LuV(HEConfig.blockIdOffset + 17).getStackForm(1L);
        HE.hydroPumpBlocks[7] = new HEHydroPumpTileEntity.ZPM(HEConfig.blockIdOffset + 1).getStackForm(1L);
        HE.hydroTurbineBlocks[7] = new HEHydroTurbineTileEntity.ZPM(HEConfig.blockIdOffset + 17).getStackForm(1L);
        HE.hydroPumpBlocks[8] = new HEHydroPumpTileEntity.UV(HEConfig.blockIdOffset + 1).getStackForm(1L);
        HE.hydroTurbineBlocks[8] = new HEHydroTurbineTileEntity.UV(HEConfig.blockIdOffset + 17).getStackForm(1L);
        HE.hydroPumpBlocks[9] = new HEHydroPumpTileEntity.UHV(HEConfig.blockIdOffset + 1).getStackForm(1L);
        HE.hydroTurbineBlocks[9] = new HEHydroTurbineTileEntity.UHV(HEConfig.blockIdOffset + 17).getStackForm(1L);
        HE.hydroPumpBlocks[10] = new HEHydroPumpTileEntity.UEV(HEConfig.blockIdOffset + 1).getStackForm(1L);
        HE.hydroTurbineBlocks[10] = new HEHydroTurbineTileEntity.UEV(HEConfig.blockIdOffset + 17).getStackForm(1L);
        HE.hydroPumpBlocks[11] = new HEHydroPumpTileEntity.UIV(HEConfig.blockIdOffset + 1).getStackForm(1L);
        HE.hydroTurbineBlocks[11] = new HEHydroTurbineTileEntity.UIV(HEConfig.blockIdOffset + 17).getStackForm(1L);
        HE.hydroPumpBlocks[12] = new HEHydroPumpTileEntity.UMV(HEConfig.blockIdOffset + 1).getStackForm(1L);
        HE.hydroTurbineBlocks[12] = new HEHydroTurbineTileEntity.UMV(HEConfig.blockIdOffset + 17).getStackForm(1L);
        HE.hydroPumpBlocks[13] = new HEHydroPumpTileEntity.UXV(HEConfig.blockIdOffset + 1).getStackForm(1L);
        HE.hydroTurbineBlocks[13] = new HEHydroTurbineTileEntity.UXV(HEConfig.blockIdOffset + 17).getStackForm(1L);
        HE.hydroPumpBlocks[14] = new HEHydroPumpTileEntity.MAX(HEConfig.blockIdOffset + 1).getStackForm(1L);
        HE.hydroTurbineBlocks[14] = new HEHydroTurbineTileEntity.MAX(HEConfig.blockIdOffset + 17).getStackForm(1L);

        // Hide blocks in NEI if not enabled
        for (int tierId = 0; tierId < HEConfig.enabledTiers.length; tierId++) {
            if (HEConfig.enabledTiers[tierId] == false) {
                if (HE.hydroPumpBlocks[tierId] != null) {
                    API.hideItem(HE.hydroPumpBlocks[tierId]);
                }
                if (HE.hydroTurbineBlocks[tierId] != null) {
                    API.hideItem(HE.hydroTurbineBlocks[tierId]);
                }
            }
        }
    }

    // postInit "Handle interaction with other mods, complete your setup based on this."
    public void fmlLifeCycleEvent(FMLPostInitializationEvent event) {
        NetworkRegistry.INSTANCE.registerGuiHandler(HETags.MODID, HE.guiHandler);
        HEBlockRecipes.registerRecipes();
    }

    public void fmlLifeCycleEvent(FMLServerAboutToStartEvent event) {}

    // register server commands in this event handler
    public void fmlLifeCycleEvent(FMLServerStartingEvent event) {
        event.registerServerCommand(new HECommandSetWater());
        event.registerServerCommand(new HECommandDebug());
        event.registerServerCommand(new HECommandListControllers());
        if (event.getSide() == Side.SERVER || event.getServer().isSinglePlayer()) {
            HEServer.instance = HEServer.load(event.getServer().worldServers[0]);
        }
    }

    public void fmlLifeCycleEvent(FMLServerStartedEvent event) {}

    public void fmlLifeCycleEvent(FMLServerStoppingEvent event) {}

    public void fmlLifeCycleEvent(FMLServerStoppedEvent event) {}
}
