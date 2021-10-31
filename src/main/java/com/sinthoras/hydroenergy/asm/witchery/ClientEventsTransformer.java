package com.sinthoras.hydroenergy.asm.witchery;

import com.sinthoras.hydroenergy.asm.HEClasses;
import com.sinthoras.hydroenergy.asm.HEUtil;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import org.objectweb.asm.tree.*;

import java.util.List;

import static org.objectweb.asm.Opcodes.*;

public class ClientEventsTransformer implements IClassTransformer {

    public static final String fullClassName = "com.emoniph.witchery.client.ClientEvents";

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
     * block1.getMaterial() == Material.water
     * with
     * HEGetMaterialUtil.getMaterialWrapper(block1, entityplayer.posY + entityplayer.getEyeHeight()) == Material.water
     */
    public static byte[] transform(byte[] basicClass, boolean isObfuscated) {
        final ClassNode classNode = HEUtil.convertByteArrayToClassNode(basicClass);

        final String METHOD_getMaterialHEWrapper = "getMaterialHEWrapper";
        final String METHOD_getMaterialHEWrapper_DESC = "(L" + HEClasses.Block + ";L" + HEClasses.EntityLivingBase + ";)L" + HEClasses.Material + ";";
        final boolean isApiUsed = null != HEUtil.getMethod(classNode, METHOD_getMaterialHEWrapper, METHOD_getMaterialHEWrapper_DESC);
        if(isApiUsed) {
            HEUtil.info("Witchery(" + fullClassName + ") is using HydroEnergy API. No injection necessary.");
            return basicClass;
        }
        HEUtil.info("Witchery(" + fullClassName + ") does not use HydroEnergy API. Fallback to injection.");

        final String MARKER_method = "getFOVModifier";
        final String MARKER_method_DESC = "(FL" + HEClasses.EntityRenderer + ";L" + HEClasses.Minecraft + ";)F";
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
        if(instructions.size() != 1) {
            HEUtil.info("Could not find instruction " + MARKER_instruction_OWNER + "." + MARKER_instruction + ":" + MARKER_instruction_DESC + " in said method. You will experience visual bugs.");
            return basicClass;
        }
        HEUtil.info("Found instruction " + MARKER_instruction_OWNER + "." + MARKER_instruction + ":" + MARKER_instruction_DESC + " in said method.");

        final String REPLACED_method = "getMaterialWrapper";
        final String REPLACED_method_DESC = "(L" + HEClasses.Block + ";D)L" + HEClasses.Material + ";";
        final String FIELD_posY = isObfuscated ? "field_70163_u" : "posY";
        final String FIELD_posY_DESC = "D";
        final String METHOD_getEyeHeight = isObfuscated ? "func_70047_e" : "getEyeHeight";
        final String METHOD_getEyeHeight_DESC = "()F";
        InsnList instructionToInsert = new InsnList();
        instructionToInsert.add(new VarInsnNode(ALOAD, 6));
        instructionToInsert.add(new VarInsnNode(ALOAD, 4));
        instructionToInsert.add(new FieldInsnNode(GETFIELD, HEClasses.Entity, FIELD_posY, FIELD_posY_DESC));
        instructionToInsert.add(new VarInsnNode(ALOAD, 4));
        instructionToInsert.add(new MethodInsnNode(INVOKEVIRTUAL,
                HEClasses.EntityLivingBase,
                METHOD_getEyeHeight,
                METHOD_getEyeHeight_DESC,
                false));
        instructionToInsert.add(new InsnNode(F2D));
        instructionToInsert.add(new InsnNode(DADD));
        instructionToInsert.add(new MethodInsnNode(INVOKESTATIC,
                HEClasses.HEGetMaterialUtil,
                REPLACED_method,
                REPLACED_method_DESC,
                false));
        // Add instruction after target instruction
        targetMethod.instructions.insert(instructions.get(0), instructionToInsert);
        // Remove ALOAD before target instruction
        targetMethod.instructions.remove(instructions.get(0).getPrevious());
        // Remove target instruction itself
        targetMethod.instructions.remove(instructions.get(0));
        HEUtil.info("Fixed mod-interop with Witchery.");

        return HEUtil.convertClassNodeToByteArray(classNode);
    }
}
