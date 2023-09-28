package miku.npop.agent;

import java.lang.instrument.Instrumentation;

public class PreMain {
    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("NPOP loading as JavaAgent.");
        inst.addTransformer(new AccessTransformer(), true);
        System.out.println("Success.");
    }
}
