package cpw.mods.modlauncher;

import miku.npop.modLauncher.ModLauncherCore;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import static java.lang.reflect.Modifier.*;

public class NPOPClassTransformer extends ClassTransformer {
    public NPOPClassTransformer(ClassTransformer source) throws IllegalAccessException {
        super((TransformStore) ModLauncherCore.transformers.get(source), (LaunchPluginHandler) ModLauncherCore.pluginHandler.get(source), (TransformingClassLoader) ModLauncherCore.transformingClassLoader.get(source), (TransformerAuditTrail) ModLauncherCore.auditTrail.get(source));
    }

    @Override
    byte[] transform(byte[] inputClass, String className, final String reason){
        byte[] clazz = super.transform(inputClass,className,reason);
        try {
            ClassReader cr = new ClassReader(clazz);
            ClassNode cn = new ClassNode();
            cr.accept(cn,0);
            if(isInterface(cn.access)){
                return clazz;
            }
            for (FieldNode fn : cn.fields) {
                if (fn.name.equals("$VALUES")) continue;

                if (isPrivate(fn.access)) {
                    fn.access &= ~Opcodes.ACC_PRIVATE;
                    fn.access |= Opcodes.ACC_PUBLIC;
                }
                if (isProtected(fn.access)) {
                    fn.access &= ~Opcodes.ACC_PROTECTED;
                    fn.access |= Opcodes.ACC_PUBLIC;
                }
                if (isFinal(fn.access)) {
                    fn.access &= ~Opcodes.ACC_FINAL;
                }

            }

            for (MethodNode mn : cn.methods) {
                if (mn.name.equals("<clinit>")) continue;

                if (isPrivate(mn.access)) {
                    mn.access &= ~Opcodes.ACC_PRIVATE;
                    mn.access |= Opcodes.ACC_PUBLIC;
                }
                if (isProtected(mn.access)) {
                    mn.access &= ~Opcodes.ACC_PROTECTED;
                    mn.access |= Opcodes.ACC_PUBLIC;
                }
                if (isFinal(mn.access)) {
                    mn.access &= ~Opcodes.ACC_FINAL;
                }
            }

            ClassWriter cw = new ClassWriter(0);
            cn.accept(cw);
            return cw.toByteArray();

        } catch (Throwable t){
            return clazz;
        }
    }
}
