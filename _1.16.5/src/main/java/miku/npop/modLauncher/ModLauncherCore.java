package miku.npop.modLauncher;

import cpw.mods.modlauncher.LaunchPluginHandler;
import cpw.mods.modlauncher.Launcher;
import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.ITransformationService;
import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.serviceapi.ILaunchPluginService;
import joptsimple.OptionSpecBuilder;
import miku.npop.FileUtils;
import miku.npop.Utils;

import javax.annotation.Nonnull;
import java.io.*;
import java.lang.management.ManagementFactory;
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
        boolean flag = false;
        try {
            File file = new File("Agent.jar");
            if (!file.exists()) {
                if (Utils.isWindows()) {
                    try (InputStream is = Utils.class.getResourceAsStream("/AgentWindows")) {
                        assert is != null;
                        FileUtils.copyInputStreamToFile(is, file);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else if (Utils.isMacOS()) {
                    try (InputStream is = Utils.class.getResourceAsStream("/AgentMacOS")) {
                        assert is != null;
                        FileUtils.copyInputStreamToFile(is, file);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    System.out.println("Guess you are on Linux.");
                    try (InputStream is = Utils.class.getResourceAsStream("/AgentLinux")) {
                        assert is != null;
                        FileUtils.copyInputStreamToFile(is, file);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];

            StringBuilder run = new StringBuilder();

            run.insert(0, file.getAbsolutePath()).insert(0, "-jar ");
            String JAVA = System.getProperty("java.home");
            System.out.println("java.home:" + JAVA);
            if (JAVA.endsWith("jre")) {
                JAVA = JAVA.substring(0, JAVA.length() - 3);
                File jdk = new File(JAVA + "bin" + File.separator + "java");
                assert jdk.exists();
                String tmp = JAVA + "bin" + File.separator + "java";
                if (Utils.isWindows()) {
                    tmp = tmp + ".exe";
                }
                run.insert(0, tmp + " ");
            } else {
                String tmp = JAVA + File.separator + "bin" + File.separator + "java";
                if (Utils.isWindows()) {
                    tmp = tmp + ".exe";
                }
                run.insert(0, tmp + " ");
            }

            run.append(" ").append(pid).append(" ").append(file.getAbsolutePath());

            System.out.println("Running agent.");
            System.out.println("Command:" + run);

            if (Utils.isWindows()) {
                ProcessBuilder process = new ProcessBuilder("cmd /c " + run);
                process.redirectErrorStream(true);
                Process mc = process.start();
                BufferedReader inStreamReader = new BufferedReader(new InputStreamReader(mc.getInputStream()));
                String line;
                while ((line = inStreamReader.readLine()) != null) {
                    System.out.println(line);
                }

            } else {
                Process mc = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", String.valueOf(run)}, null, null);
                InputStream is = mc.getInputStream();
                String line;

                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
                mc.waitFor();
                is.close();
                reader.close();
            }
        } catch (Throwable t) {
            t.printStackTrace();
            System.out.println("Failed to load agent. Fall back to original ModLaunchCore.");
            flag = true;
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
