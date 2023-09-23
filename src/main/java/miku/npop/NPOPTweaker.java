package miku.npop;

import miku.npop.hack.LinuxHack;
import miku.npop.hack.WindowsHack;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import sun.instrument.InstrumentationImpl;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Vector;

public class NPOPTweaker implements ITweaker {
    static {
        boolean flag = false;

        System.out.println("NPOP loading as tweaker.");

        if (!Utils.isMacOS()) {
            System.out.println("Try to get the InstrumentationImpl.");
            try {
                InstrumentationImpl instrumentation;
                if (Utils.isWindows()) {
                    Field loadedLibraryNames_field = ClassLoader.class.getDeclaredField("loadedLibraryNames");
                    loadedLibraryNames_field.setAccessible(true);
                    ((Vector<String>) loadedLibraryNames_field.get(null)).removeIf(s -> s.contains("attach"));
                    instrumentation = (InstrumentationImpl) WindowsHack.Hack();
                    instrumentation.addTransformer(PreMain.AT);
                } else {
                    System.out.println("Guess you are on Linux.");
                    instrumentation = (InstrumentationImpl) LinuxHack.hack();
                    instrumentation.addTransformer(PreMain.AT);
                }
                System.out.println("Successfully get the InstrumentationImpl and add our transformer.");
                flag = true;
            } catch (Throwable t) {
                System.out.println("Failed to get InstrumentationImpl. Fallback to forgeCoreMod.");
            }
        } else {
            System.out.println("We are on MacOS. Fallback to forgeCoreMod.");
            flag = true;
        }

        if (flag) {
            System.out.println("Adding transformer to LaunchClassLoader.");
            try {
                Field transformers = LaunchClassLoader.class.getDeclaredField("transformers");
                transformers.setAccessible(true);
                ((List<IClassTransformer>) transformers.get(Launch.classLoader)).add(FMLAccessTransformer.INSTANCE);
                transformers.setAccessible(false);
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }

            System.out.println("Success.");
        }
    }

    @Override
    public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) {

    }

    @Override
    public void injectIntoClassLoader(LaunchClassLoader classLoader) {

    }

    @Override
    public String getLaunchTarget() {
        return null;
    }

    @Override
    public String[] getLaunchArguments() {
        return new String[0];
    }
}