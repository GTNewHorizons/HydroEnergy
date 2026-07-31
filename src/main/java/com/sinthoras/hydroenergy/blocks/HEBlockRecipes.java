package com.sinthoras.hydroenergy.blocks;

import static gregtech.api.recipe.RecipeMaps.assemblerRecipes;
import static gregtech.api.util.GTRecipeBuilder.SECONDS;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import com.ruling_0.materiallib.api.Material;
import com.ruling_0.materiallib.api.MaterialLibAPI;
import com.sinthoras.hydroenergy.HE;
import com.sinthoras.hydroenergy.config.HEConfig;

import gregtech.api.GregTechAPI;
import gregtech.api.enums.GTValues;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.TierEU;
import gregtech.api.enums.materials.FluidShapes;
import gregtech.api.enums.materials.Materials;
import gregtech.api.enums.materials.PipeShapes;
import gregtech.api.enums.materials.Shapes;
import gregtech.api.interfaces.IItemContainer;
import gregtech.api.util.GTUtility;

public class HEBlockRecipes {

    public static void registerRecipes() {
        // ULV is disabled!
        IItemContainer[] hulls = { null, // ULV,
                ItemList.Hull_LV, ItemList.Hull_MV, ItemList.Hull_HV, ItemList.Hull_EV, ItemList.Hull_IV,
                ItemList.Hull_LuV, ItemList.Hull_ZPM, ItemList.Hull_UV, ItemList.Hull_MAX, // UHV
                ItemList.Hull_UEV, ItemList.Hull_UIV, ItemList.Hull_UMV, ItemList.Hull_UXV, ItemList.Hull_MAXV };
        IItemContainer[] motors = { null, // ULV,
                ItemList.Electric_Motor_LV, ItemList.Electric_Motor_MV, ItemList.Electric_Motor_HV,
                ItemList.Electric_Motor_EV, ItemList.Electric_Motor_IV, ItemList.Electric_Motor_LuV,
                ItemList.Electric_Motor_ZPM, ItemList.Electric_Motor_UV, ItemList.Electric_Motor_UHV,
                ItemList.Electric_Motor_UEV, ItemList.Electric_Motor_UEV, // UIV
                ItemList.Electric_Motor_UEV, // UMV
                ItemList.Electric_Motor_UEV, // UXV
                ItemList.Electric_Motor_UEV // MAX
        };
        IItemContainer[] pumps = { null, // ULV,
                ItemList.Electric_Pump_LV, ItemList.Electric_Pump_MV, ItemList.Electric_Pump_HV,
                ItemList.Electric_Pump_EV, ItemList.Electric_Pump_IV, ItemList.Electric_Pump_LuV,
                ItemList.Electric_Pump_ZPM, ItemList.Electric_Pump_UV, ItemList.Electric_Pump_UHV,
                ItemList.Electric_Pump_UEV, ItemList.Electric_Pump_UEV, // UIV
                ItemList.Electric_Pump_UEV, // UMV
                ItemList.Electric_Pump_UEV, // UXV
                ItemList.Electric_Pump_UEV, // MAX
        };
        Material[] solderMaterials = { Materials.SolderingAlloy, Materials.Tin, Materials.Lead, };
        Material[] rotorMaterialsPerVoltage = { null, // ULV,
                Materials.Steel, // LV
                Materials.Aluminium, // MV
                Materials.StainlessSteel, // HV
                Materials.Titanium, // EV
                Materials.TungstenSteel, // IV
                Materials.TungstenSteel, // LuV
                Materials.Iridium, // ZPM
                Materials.Osmium, // UV
                Materials.Neutronium, // UHV
                Materials.Neutronium, // UEV
                Materials.Neutronium, // UIV
                Materials.Neutronium, // UMV
                Materials.Neutronium, // UXV
                Materials.Neutronium, // MAX
        };
        Material[] cableMaterialsPerVoltage = { null, // ULV,
                Materials.Tin, // LV
                Materials.Copper, // MV
                Materials.Gold, // HV
                Materials.Aluminium, // EV
                Materials.Platinum, // IV
                Materials.VanadiumGallium, // LuV
                Materials.Naquadah, // ZPM
                Materials.NaquadahAlloy, // UV
                Materials.NaquadahAlloy, // UHV
                Materials.NaquadahAlloy, // UEV
                Materials.NaquadahAlloy, // UIV
                Materials.NaquadahAlloy, // UMV
                Materials.NaquadahAlloy, // UXV
                Materials.NaquadahAlloy, // MAX
        };

        for (int i = 0; i < solderMaterials.length; ++i) {
            FluidStack solder = MaterialLibAPI.getFluidStack(solderMaterials[i], FluidShapes.fluidMolten, 72 << i);

            GTValues.RA.stdBuilder()
                    .itemInputs(
                            ItemList.Casing_SolidSteel.get(1L),
                            new ItemStack(GregTechAPI.sBlockConcretes, 2, 8),
                            ItemList.Cover_Screen.get(1L),
                            ItemList.FluidRegulator_MV.get(2L),
                            GTUtility.getIntegratedCircuit(1))
                    .itemOutputs(HE.hydroDamControllerBlock).fluidInputs(solder).duration(10 * SECONDS)
                    .eut(TierEU.RECIPE_LV).addTo(assemblerRecipes);

            for (int tierId = 1; tierId < HE.hydroPumpBlocks.length; tierId++) {
                if (!(HEConfig.enabledTiers[tierId])) {
                    continue;
                }

                GTValues.RA.stdBuilder()
                        .itemInputs(
                                hulls[tierId].get(1L),
                                MaterialLibAPI.getStack(rotorMaterialsPerVoltage[tierId], Shapes.rotor, 2),
                                motors[tierId].get(1L),
                                pumps[tierId].get(1L),
                                MaterialLibAPI.getStack(cableMaterialsPerVoltage[tierId], PipeShapes.cableGt01, 2),
                                GTUtility.getIntegratedCircuit(1))
                        .itemOutputs(HE.hydroPumpBlocks[tierId]).fluidInputs(solder).duration(10 * SECONDS)
                        .eut(GTValues.VP[tierId - 1]).addTo(assemblerRecipes);

                GTValues.RA.stdBuilder()
                        .itemInputs(
                                hulls[tierId].get(1L),
                                MaterialLibAPI.getStack(rotorMaterialsPerVoltage[tierId], Shapes.rotor, 2),
                                motors[tierId].get(1L),
                                pumps[tierId].get(1L),
                                MaterialLibAPI.getStack(cableMaterialsPerVoltage[tierId], PipeShapes.cableGt01, 2),
                                GTUtility.getIntegratedCircuit(2))
                        .itemOutputs(HE.hydroTurbineBlocks[tierId]).fluidInputs(solder).duration(10 * SECONDS)
                        .eut(GTValues.VP[tierId - 1]).addTo(assemblerRecipes);

            }
        }
    }
}
