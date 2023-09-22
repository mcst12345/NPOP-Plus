package miku.npop;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;

public class PreMain {
    public static final ClassFileTransformer AT = new AccessTransformer();
    public static void premain(String agentArgs, Instrumentation inst){
        inst.addTransformer(AT);
    }
}
