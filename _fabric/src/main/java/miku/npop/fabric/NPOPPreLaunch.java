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
import java.util.Vector;

public class NPOPPreLaunch implements PreLaunchEntrypoint {
    static {
        System.out.println("NPOP loading in fabric.");

        boolean flag = System.getProperty("NPOP-Plus-Attach-Debug") != null;

        if (!Utils.isMacOS()) {
            System.out.println("Try to get the InstrumentationImpl.");
            try {
                Instrumentation instrumentation;
                if (Utils.isWindows()) {
                    Field loadedLibraryNames_field = ClassLoader.class.getDeclaredField("loadedLibraryNames");
                    loadedLibraryNames_field.setAccessible(true);
                    ((Vector<String>) loadedLibraryNames_field.get(null)).removeIf(s -> s.contains("attach"));
                    instrumentation = (Instrumentation) WindowsHack.Hack();
                    instrumentation.addTransformer(PreMain.AT);
                } else {
                    System.out.println("Guess you are on Linux.");
                    instrumentation = (Instrumentation) LinuxHack.hack();
                    instrumentation.addTransformer(PreMain.AT);
                }
                System.out.println("Successfully get the InstrumentationImpl and add our transformer.");
                flag = true;
            } catch (Throwable t) {
                System.out.println("Failed to get InstrumentationImpl. Fallback to attach api.");
            }
        } else {
            System.out.println("We are on MacOS. Trying the attach api.");
            flag = true;
        }

        if (flag) {
            try {
                System.loadLibrary("attach");
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
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void onPreLaunch() {
    }
}
