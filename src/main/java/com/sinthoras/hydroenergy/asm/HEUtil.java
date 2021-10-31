package com.sinthoras.hydroenergy.asm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

import java.util.ArrayList;
import java.util.List;

public class HEUtil {

    public static void info(String message) {

    }

    private static ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);

    public static MethodNode getMethod(ClassNode owner, String name, String description) {
        for(MethodNode method : owner.methods) {
            if(method.name.equals(name) && method.desc.equals(description)) {
                return method;
            }
        }
        return null;
    }

    public static ClassNode convertByteArrayToClassNode(byte[] basicClass) {
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(basicClass);
        classReader.accept(classNode, 0);
        return classNode;
    }

    public static byte[] convertClassNodeToByteArray(ClassNode classNode) {
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        classNode.accept(classWriter);
        return classWriter.toByteArray();
    }

    public static List<MethodInsnNode> getInstructions(MethodNode method, boolean isStatic, String owner, String name, String description) {
        List<MethodInsnNode> list = new ArrayList<MethodInsnNode>();
        for(AbstractInsnNode instruction : method.instructions.toArray()) {
            if(instruction.getOpcode() == INVOKESTATIC || instruction.getOpcode() == INVOKEVIRTUAL) {
                MethodInsnNode instructionMethod = (MethodInsnNode)instruction;
                if(instructionMethod.owner.equals(owner) && instructionMethod.name.equals(name) && instructionMethod.desc.equals(description)) {
                    if((isStatic && instructionMethod.getOpcode() == INVOKESTATIC) || (!isStatic && instructionMethod.getOpcode() == INVOKEVIRTUAL)) {
                        list.add(instructionMethod);
                    }
                }
            }
        }
        return list;
    }
}
