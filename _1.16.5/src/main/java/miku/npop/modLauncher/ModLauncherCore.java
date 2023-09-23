package miku.npop.modLauncher;

import cpw.mods.modlauncher.LaunchPluginHandler;
import cpw.mods.modlauncher.Launcher;
import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.ITransformationService;
import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.serviceapi.ILaunchPluginService;
import joptsimple.OptionSpecBuilder;
import miku.npop.AccessTransformer;
import miku.npop.Utils;
import miku.npop.hack.LinuxHack;
import miku.npop.hack.WindowsHack;

import javax.annotation.Nonnull;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class ModLauncherCore implements ITransformationService {
    static {
        System.out.println("NPOP-Plus loading in ModLauncher mode.");
        boolean flag = System.getProperty("NPOP-ModLauncher-Debug") != null;
        if (!flag) {
            if (!Utils.isMacOS()) {
                System.out.println("Try to get the InstrumentationImpl.");
                try {
                    Instrumentation instrumentation;
                    if (Utils.isWindows()) {
                        Field loadedLibraryNames_field = ClassLoader.class.getDeclaredField("loadedLibraryNames");
                        loadedLibraryNames_field.setAccessible(true);
                        ((Vector<String>) loadedLibraryNames_field.get(null)).removeIf(s -> s.contains("attach"));
                        instrumentation = (Instrumentation) WindowsHack.Hack();
                        instrumentation.addTransformer(new AccessTransformer());
                    } else {
                        System.out.println("Guess you are on Linux.");
                        instrumentation = (Instrumentation) LinuxHack.hack();
                        instrumentation.addTransformer(new AccessTransformer());
                    }
                    System.out.println("Successfully get the InstrumentationImpl and add our transformer.");
                } catch (Throwable t) {
                    System.out.println("Failed to get InstrumentationImpl. Fallback to forgeCoreMod.");
                    flag = true;
                }
            } else {
                System.out.println("We are on MacOS. Fallback to forgeCoreMod.");
                flag = true;
            }
        }


        if (flag) {
            System.out.println("Replacing launchPlugins map.");
            try {
                Field launchPluginsField = Launcher.class.getDeclaredField("launchPlugins");
                launchPluginsField.setAccessible(true);
                Object launchPlugins = launchPluginsField.get(Launcher.INSTANCE);
                launchPluginsField = LaunchPluginHandler.class.getDeclaredField("plugins");
                launchPluginsField.setAccessible(true);
                NPOPPluginMap<String, ILaunchPluginService> fucked = new NPOPPluginMap<>();
                fucked.putAll(((Map<String, ILaunchPluginService>) launchPluginsField.get(launchPlugins)));
                launchPluginsField.set(launchPlugins, fucked);
                System.out.println("Success. NPOP-Plus is loaded.");
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }
    @Nonnull
    @Override
    public String name() {
        return "NoPrivateOrProtected-Plus";
    }

    @Override
    public void arguments(@Nonnull BiFunction<String, String, OptionSpecBuilder> argumentBuilder) {
        ITransformationService.super.arguments(argumentBuilder);
    }

    @Override
    public void argumentValues(@Nonnull OptionResult option) {
        ITransformationService.super.argumentValues(option);
    }

    @Override
    public void initialize(@Nonnull IEnvironment environment) {

    }

    @Override
    public void beginScanning(@Nonnull IEnvironment environment) {

    }

    @Override
    public List<Map.Entry<String, Path>> runScan(@Nonnull IEnvironment environment) {
        return ITransformationService.super.runScan(environment);
    }

    @Override
    public void onLoad(@Nonnull IEnvironment env,@Nonnull Set<String> otherServices) {

    }

    @Nonnull
    @Override
    public List<ITransformer> transformers() {
        return Collections.emptyList();
    }

    @Override
    public Map.Entry<Set<String>, Supplier<Function<String, Optional<URL>>>> additionalClassesLocator() {
        return ITransformationService.super.additionalClassesLocator();
    }

    @Override
    public Map.Entry<Set<String>, Supplier<Function<String, Optional<URL>>>> additionalResourcesLocator() {
        return ITransformationService.super.additionalResourcesLocator();
    }

    //From ClassLoaderUtils by LXGaming.
    //Licensed under Apache2.0.
    private static Field getField(Field[] fields, @Nonnull String... names) throws NoSuchFieldException {
        for (Field field : fields) {
            for (String name : names) {
                if (field.getName().equals(name)) {
                    return field;
                }
            }
        }

        throw new NoSuchFieldException(String.join(", ", names));
    }
}
