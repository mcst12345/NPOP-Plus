package miku.npop.fabric;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import miku.npop.PreMain;
import miku.npop.Utils;
import miku.npop.hack.LinuxHack;
import miku.npop.hack.WindowsHack;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.util.Set;

public class NPOPPreLaunch implements PreLaunchEntrypoint {
    static {
        System.out.println("NPOP loading in fabric.");

        boolean flag = System.getProperty("NPOP-Plus-Attach-Debug") != null;

        if (!flag) {
            if (!Utils.isMacOS()) {
                System.out.println("Try to get the InstrumentationImpl.");
                try {
                    Field module = Class.class.getDeclaredField("module");
                    long offset = Utils.getUnsafe().objectFieldOffset(module);
                    Instrumentation instrumentation;
                    if (Utils.isWindows()) {
                        Class<?> clazz = Class.forName("jdk.internal.loader.NativeLibraries");
                        Field loadedLibraryNamesField = clazz.getDeclaredField("loadedLibraryNames");
                        ((Set<String>) Utils.getUnsafe().staticFieldBase(loadedLibraryNamesField)).removeIf(s -> s.contains("attach"));
                        Utils.getUnsafe().putObject(WindowsHack.class, offset, Object.class.getModule());
                        instrumentation = (Instrumentation) WindowsHack.Hack();

                        instrumentation.addTransformer(PreMain.AT);
                    } else {
                        System.out.println("Guess you are on Linux.");
                        Utils.getUnsafe().putObject(LinuxHack.class, offset, Object.class.getModule());
                        instrumentation = (Instrumentation) LinuxHack.hack();
                        instrumentation.addTransformer(PreMain.AT);
                    }
                    System.out.println("Successfully get the InstrumentationImpl and add our transformer.");
                } catch (Throwable t) {
                    System.out.println("Failed to get InstrumentationImpl. Fallback to attach api.");
                    flag = true;
                }
            } else {
                System.out.println("We are on MacOS. Trying the attach api.");
                flag = true;
            }
        }

        if (flag) {

            try {
                Class<?> clazz = Class.forName("jdk.internal.loader.NativeLibraries");
                Field loadedLibraryNamesField = clazz.getDeclaredField("loadedLibraryNames");
                ((Set<String>) Utils.getUnsafe().staticFieldBase(loadedLibraryNamesField)).removeIf(s -> s.contains("attach"));
                System.loadLibrary("attach");
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            } catch (Throwable ignored) {
            }
            String pid;
            try {
                pid = String.valueOf(ProcessHandle.current().pid());
            } catch (Throwable t) {
                pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
            }

            try {
                VirtualMachine vm = VirtualMachine.attach(pid);
                vm.loadAgent(PreMain.class.getProtectionDomain().getCodeSource().getLocation().getPath());
            } catch (AttachNotSupportedException | IOException | AgentLoadException | AgentInitializationException e) {
                System.out.println("Attaching failed! Try to add \"-Djdk.attach.allowAttachSelf=true\" to your launch arguments.");
                System.out.println("If this doesn't work,try to run NPOP-Plus in javaagent mode.");
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void onPreLaunch() {
    }
}
