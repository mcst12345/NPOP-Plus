package miku.npop;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;

public class PreMain {
    public static final ClassFileTransformer AT = new AccessTransformer();

    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("NPOP loading as JavaAgent.");
        inst.addTransformer(AT, true);
        System.out.println("Success.");
    }
}
