package com.sinthoras.hydroenergy.asm.biomesoplenty;

import com.sinthoras.hydroenergy.asm.HEClasses;
import com.sinthoras.hydroenergy.asm.HEUtil;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

import java.util.List;

public class FogHandlerTransformer implements IClassTransformer {

    public static final String fullClassName = "biomesoplenty.client.fog.FogHandler";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        final boolean isDeobfuscated = (boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");
        if(transformedName.equals(fullClassName)) {
            return transform(basicClass, !isDeobfuscated);
        }
        else {
            return basicClass;
        }
    }

    /* Replace
     * blockAtEyes.getMaterial() == Material.water
     * with
     * HEGetMaterialUtil.getMaterialWrapper(event) == Material.water
     */
    public static byte[] transform(byte[] basicClass, boolean isObfuscated) {
        final ClassNode classNode = HEUtil.convertByteArrayToClassNode(basicClass);

        final String METHOD_getMaterialHEWrapper = "getMaterialHEWrapper";
        final String METHOD_getMaterialHEWrapper_DESC = "(L" + HEClasses.FogColors + ";)L" + HEClasses.Material + ";";
        final boolean isApiUsed = null != HEUtil.getMethod(classNode, METHOD_getMaterialHEWrapper, METHOD_getMaterialHEWrapper_DESC);
        if(isApiUsed) {
            HEUtil.info("Biomes 'o Plenty(" + fullClassName + ") is using HydroEnergy API. No injection necessary.");
            return basicClass;
        }
        HEUtil.info("Biomes 'o Plenty(" + fullClassName + ") does not use HydroEnergy API. Fallback to injection.");

        final String MARKER_method = "onGetFogColour";
        final String MARKER_method_DESC = "(L" + HEClasses.FogColors + ";)V";
        final MethodNode targetMethod = HEUtil.getMethod(classNode, MARKER_method, MARKER_method_DESC);
        if(targetMethod == null) {
            HEUtil.info("Could not find method " + MARKER_method + ":" + MARKER_method_DESC + " in " + fullClassName + ". You will experience severe visual bugs.");
            return basicClass;
        }
        HEUtil.info("Found method " + MARKER_method + ":" + MARKER_method_DESC + " in " + fullClassName);

        final boolean isStatic = false;
        final String MARKER_instruction_OWNER = HEClasses.Block;
        final String MARKER_instruction = isObfuscated ? "func_149688_o" : "getMaterial";
        final String MARKER_instruction_DESC = "()L" + HEClasses.Material + ";";
        List<MethodInsnNode> instructions = HEUtil.getInstructions(targetMethod, isStatic, MARKER_instruction_OWNER, MARKER_instruction, MARKER_instruction_DESC);
        if(instructions.size() != 2) {
            HEUtil.info("Could not find instruction " + MARKER_instruction_OWNER + "." + MARKER_instruction + ":" + MARKER_instruction_DESC + " twice in said method. You will experience severe visual bugs.");
            return basicClass;
        }
        HEUtil.info("Found instruction " + MARKER_instruction_OWNER + "." + MARKER_instruction + ":" + MARKER_instruction_DESC + " twice in said method.");

        final String REPLACED_method = "getMaterialWrapper";
        final String REPLACED_method_DESC = "(L" + HEClasses.FogColors + ";)L" + HEClasses.Material + ";";
        InsnList instructionToInsert = new InsnList();
        instructionToInsert.add(new VarInsnNode(ALOAD, 1));
        instructionToInsert.add(new MethodInsnNode(INVOKESTATIC,
                HEClasses.HEGetMaterialUtil,
                REPLACED_method,
                REPLACED_method_DESC,
                false));
        // Add instruction after target instruction
        targetMethod.instructions.insert(instructions.get(1), instructionToInsert);
        // Remove ALOAD before target instruction
        targetMethod.instructions.remove(instructions.get(1).getPrevious());
        // Remove target instruction itself
        targetMethod.instructions.remove(instructions.get(1));
        HEUtil.info("Fixed mod-interop with Biomes 'o Plenty.");

        return HEUtil.convertClassNodeToByteArray(classNode);
    }
}
