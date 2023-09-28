package miku.npop;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;

public class PreMain {

    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println(inst.hashCode());
        System.out.println(inst.getObjectSize(inst));
        try {
            Class<?> clazz = Class.forName("sun.instrument.InstrumentationImpl");
            Field field = clazz.getDeclaredField("mNativeAgent");
            System.out.println(Utils.getUnsafe().getLongVolatile(inst, Utils.getUnsafe().objectFieldOffset(field)));
        } catch (NoSuchFieldException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        System.out.println("NPOP loading as JavaAgent.");
        inst.addTransformer(new AccessTransformer(), true);
        System.out.println("Success.");
    }
}
