package miku.npop.modLauncher;

import cpw.mods.modlauncher.serviceapi.ILaunchPluginService;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.EnumSet;

import static java.lang.reflect.Modifier.*;

public class NPOPPlugin implements ILaunchPluginService {
    public static final NPOPPlugin INSTANCE = new NPOPPlugin();

    private NPOPPlugin() {
    }

    @Override
    public String name() {
        return "NPOP-Plus";
    }

    @Override
    public EnumSet<Phase> handlesClass(Type classType, boolean isEmpty) {
        return EnumSet.of(Phase.AFTER);
    }

    @Override
    public boolean processClass(Phase phase, ClassNode cn, Type classType, String reason) {
        if (phase.equals(Phase.BEFORE) || !reason.equals("classloading")) {
            return false;
        }

        if (isInterface(cn.access)) {
            return false;
        }

        boolean res = false;

        for (FieldNode fn : cn.fields) {
            if (fn.name.equals("$VALUES")) continue;


            if (isPrivate(fn.access)) {
                fn.access &= ~Opcodes.ACC_PRIVATE;
                fn.access |= Opcodes.ACC_PUBLIC;
                res = true;
            }
            if (isProtected(fn.access)) {
                fn.access &= ~Opcodes.ACC_PROTECTED;
                fn.access |= Opcodes.ACC_PUBLIC;
                res = true;
            }
            if (isFinal(fn.access)) {
                fn.access &= ~Opcodes.ACC_FINAL;
                res = true;
            }

        }

        for (MethodNode mn : cn.methods) {
            if (mn.name.equals("<clinit>")) continue;

            if (isPrivate(mn.access)) {
                mn.access &= ~Opcodes.ACC_PRIVATE;
                mn.access |= Opcodes.ACC_PUBLIC;
                res = true;
            }
            if (isProtected(mn.access)) {
                mn.access &= ~Opcodes.ACC_PROTECTED;
                mn.access |= Opcodes.ACC_PUBLIC;
                res = true;
            }
            if (isFinal(mn.access)) {
                mn.access &= ~Opcodes.ACC_FINAL;
                res = true;
            }
        }

        return res;
    }

}
