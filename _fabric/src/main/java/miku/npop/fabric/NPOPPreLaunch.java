package miku.npop.fabric;

import miku.npop.FileUtils;
import miku.npop.Utils;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

import java.io.*;
import java.lang.management.ManagementFactory;

public class NPOPPreLaunch implements PreLaunchEntrypoint {
    static {
        System.out.println("NPOP loading in fabric.");

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

        String pid;
        try {
            pid = String.valueOf(ProcessHandle.current().pid());
        } catch (Throwable t) {
            pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
        }

        StringBuilder run = new StringBuilder();

        run.insert(0, file.getAbsolutePath()).insert(0, "-jar ");
        String JAVA = System.getProperty("java.home");
        System.out.println("java.home:" + JAVA);
        if (JAVA.endsWith("jre")) {
            String JavaHome = JAVA.substring(0, JAVA.length() - 3) + "bin" + File.separator + "java";
            if (Utils.isWindows()) {
                JavaHome = JavaHome + ".exe";
            }
            File jdk = new File(JavaHome);
            run.insert(0, jdk + " ");
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

        try {
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
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onPreLaunch() {
    }

}
