package se.vidstige.jadb;

import java.io.IOException;
import java.util.Map;

/**
 * Launches the ADB server
 */
public class AdbServerLauncher {
    private final String executable;
    private Subprocess subprocess;

    /**
     * Creates a new launcher loading ADB from the environment.
     * 
     * @param subprocess the sub-process.
     * @param environment the environment to use to locate the ADB executable.
     */
    public AdbServerLauncher(Subprocess subprocess, Map<String, String> environment) {
        this(subprocess, findAdbExecutable(environment));
    }

    /**
     * Creates a new launcher with the specified ADB.
     *
     * @param subprocess the sub-process.
     * @param executable the location of the ADB executable.
     */
    public AdbServerLauncher(Subprocess subprocess, String executable) {
        this.subprocess = subprocess;
        this.executable = executable;
    }

    private static String findAdbExecutable(Map<String, String> environment) {
        String androidHome = environment.get("ANDROID_HOME");
        if (androidHome == null || androidHome.equals("")) {
            return "adb";
        }
        return androidHome + "/platform-tools/adb";
    }

    public void launch() throws IOException, InterruptedException {
        Process p = subprocess.execute(new String[]{executable, "start-server"});
        p.waitFor();
        int exitValue = p.exitValue();
        if (exitValue != 0) throw new IOException("adb exited with exit code: " + exitValue);
    }
}
