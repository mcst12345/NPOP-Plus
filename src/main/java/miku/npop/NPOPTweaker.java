package miku.npop;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.util.List;

public class NPOPTweaker implements ITweaker {
    public static boolean shouldLoad = true;

    static {
        if (shouldLoad) {
            boolean flag = false;

            System.out.println("NPOP loading as tweaker.");

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
                    String JavaHome = JAVA.substring(0, JAVA.length() - 3) + "bin" + File.separator + "java";
                    if (Utils.isWindows()) {
                        JavaHome = JavaHome + ".exe";
                    }
                    JavaHome = "\"" + JavaHome + "\"";
                    run.insert(0, JavaHome + " ");
                } else {
                    String tmp = JAVA + File.separator + "bin" + File.separator + "java";
                    if (Utils.isWindows()) {
                        tmp = tmp + ".exe";
                    }
                    tmp = "\"" + tmp + "\"";
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
                System.out.println("Failed to load agent. Fall back to original tweaker.");
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
