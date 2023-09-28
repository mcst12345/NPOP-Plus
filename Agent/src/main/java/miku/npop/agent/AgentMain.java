package miku.npop.agent;

import java.lang.instrument.Instrumentation;

public class AgentMain {
    public static void agentmain(String agentArgs, Instrumentation inst) {
        System.out.println("NPOP loading as JavaAgent.");
        inst.addTransformer(new AccessTransformer(), true);
        System.out.println("Success.");
    }
}
