/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.scriptrunner;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import javax.swing.ImageIcon;
import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;
import org.sikuli.basics.Settings;
import org.sikuli.script.ImagePath;
import org.sikuli.script.RunTime;
import org.sikuli.script.Runner;
import org.sikuli.script.Sikulix;
import org.sikuli.util.JythonHelper;

public class ScriptingSupport {

  public static RunTime runTime = RunTime.get();

	private static final String me = "ScriptingSupport: ";
  private static final int lvl = 3;
  private static void log(int level, String message, Object... args) {
    Debug.logx(level, me + message, args);
  }

  private static Boolean runAsTest;

	public static Map<String, IScriptRunner> scriptRunner = new HashMap<String, IScriptRunner>();
	private static Map<String, IScriptRunner> supportedRunner = new HashMap<String, IScriptRunner>();
  public static boolean systemRedirected = false;

	public static String TypeCommentToken = "---SikuliX---";
	public static String TypeCommentDefault = "# This script uses %s " + TypeCommentToken + "\n";

  private static boolean isRunningInteractive = false;

  private static String[] runScripts = null;
  private static String[] testScripts = null;
  private static int lastReturnCode = 0;

  private static boolean isReady = false;

  private static ServerSocket server = null;
  private static boolean isHandling = false;
  private static boolean shouldStop = false;
  private static ObjectOutputStream out = null;
  private static Scanner in = null;
  private static int runnerID = -1;
  private static Map<String, RemoteRunner> remoteRunners = new HashMap<String, RemoteRunner>();

  //<editor-fold defaultstate="collapsed" desc="remote runner support">
  public static void startRemoteRunner(String[] args) {
    int port = getPort(args.length > 0 ? args[0] : null);
    try {
      try {
        if (port > 0) {
          server = new ServerSocket(port);
        }
      } catch (Exception ex) {
        log(-1, "Remote: at start: " + ex.getMessage());
      }
      if (server == null) {
        log(-1, "Remote: could not be started on port: " + (args.length > 0 ? args[0] : null));
        System.exit(1);
      }
      while (true) {
        log(lvl, "Remote: now waiting on port: %d at %s", port, InetAddress.getLocalHost().getHostAddress());
        Socket socket = server.accept();
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new Scanner(socket.getInputStream());
        HandleClient client = new HandleClient(socket);
        isHandling = true;
        while (true) {
          if (socket.isClosed()) {
            shouldStop = client.getShouldStop();
            break;
          }
          try {
            Thread.sleep(1000);
          } catch (InterruptedException ex) {
          }
        }
        if (shouldStop) {
          break;
        }
      }
    } catch (Exception e) {
    }
    if (!isHandling) {
      log(-1, "Remote: start handling not possible: " + port);
    }
    log(lvl, "Remote: now stopped on port: " + port);
  }

  private static int getPort(String p) {
    int port;
    int pDefault = 50000;
    if (p != null) {
      try {
        port = Integer.parseInt(p);
      } catch (NumberFormatException ex) {
        return -1;
      }
    } else {
      return pDefault;
    }
    if (port < 1024) {
      port += pDefault;
    }
    return port;
  }

  private static class HandleClient implements Runnable {

    private volatile boolean keepRunning;
    Thread thread;
    Socket socket;
    Boolean shouldStop = false;

    public HandleClient(Socket sock) {
      init(sock);
    }

    private void init(Socket sock) {
      socket = sock;
      if (in == null || out == null) {
        ScriptingSupport.log(-1, "communication not established");
        System.exit(1);
      }
      thread = new Thread(this, "HandleClient");
      keepRunning = true;
      thread.start();
    }

    public boolean getShouldStop() {
      return shouldStop;
    }

    @Override
    public void run() {
      String e;
      ScriptingSupport.log(lvl,"now handling client: " + socket);
      while (keepRunning) {
        try {
          e = in.nextLine();
          if (e != null) {
            ScriptingSupport.log(lvl,"processing: " + e);
            if (e.contains("EXIT")) {
              stopRunning();
              in.close();
              out.close();
              if (e.contains("STOP")) {
                ScriptingSupport.log(lvl,"stop server requested");
                shouldStop = true;
              }
              return;
            }
            if (e.toLowerCase().startsWith("run")) {
              int retVal = runScript(e);
              send((new Integer(retVal)));
            } else if (e.toLowerCase().startsWith("system")) {
              getSystem();
            }
          }
        } catch (Exception ex) {
          ScriptingSupport.log(-1, "Exception while processing\n" + ex.getMessage());
          stopRunning();
        }
      }
      try {
        Thread.sleep(100);
      } catch (InterruptedException ex) {
        stopRunning();
      }
    }

    private void getSystem() {
      String os = System.getProperty("os.name").toLowerCase();
      if (os.startsWith("mac")) {
        os = "MAC";
      } else if (os.startsWith("windows")) {
        os = "WINDOWS";
      } else if (os.startsWith("linux")) {
        os = "LINUX";
      } else {
        os = "NOTSUPPORTED";
      }
      GraphicsEnvironment genv = GraphicsEnvironment.getLocalGraphicsEnvironment();
      GraphicsDevice[] gdevs = genv.getScreenDevices();
      send(os + " " + gdevs.length);
    }

    private int runScript(String command) {
      return 0;
    }

    private void send(Object o) {
      try {
        out.writeObject(o);
        out.flush();
        if (o instanceof ImageIcon) {
          ScriptingSupport.log(lvl,"returned: Image(%dx%d)",
                  ((ImageIcon) o).getIconWidth(), ((ImageIcon) o).getIconHeight());
        } else {
          ScriptingSupport.log(lvl,"returned: "  + o);
        }
      } catch (IOException ex) {
        ScriptingSupport.log(-1, "send: writeObject: Exception: " + ex.getMessage());
      }
    }

    public void stopRunning() {
      ScriptingSupport.log(lvl,"stop client handling requested");
      try {
        socket.close();
      } catch (IOException ex) {
        ScriptingSupport.log(-1, "fatal: socket not closeable");
        System.exit(1);
      }
      keepRunning = false;
    }
  }

  public static String getRemoteRunner(String adr, String p) {
    RemoteRunner rr = new RemoteRunner(adr, p);
    String rname = null;
    if (rr.isValid()) {
      rname = getNextRunnerName();
      remoteRunners.put(rname, rr);
    } else {
      log(-1, "getRemoteRunner: adr(%s) port(%s) not available");
    }
    return rname;
  }

  static synchronized String getNextRunnerName() {
    return String.format("remoterunner%d", runnerID++);
  }

  public int runRemote(String rname, String script, String[] args) {
    RemoteRunner rr = remoteRunners.get(rname);
    if (rr == null) {
      log(-1, "runRemote: RemortRunner(%s) not available");
      return rr.runRemote(args);
    }
    return 0;
  }
//</editor-fold>

  public static void init() {
    if (isReady) {
      return;
    }
		log(lvl, "initScriptingSupport: enter");
    if (scriptRunner.isEmpty()) {
      ServiceLoader<IScriptRunner> rloader = ServiceLoader.load(IScriptRunner.class);
      Iterator<IScriptRunner> rIterator = rloader.iterator();
      while (rIterator.hasNext()) {
				IScriptRunner current = null;
				try {
					current = rIterator.next();
				} catch (ServiceConfigurationError e) {
					log(lvl, "initScriptingSupport: warning: %s", e.getMessage());
					continue;
				}
        String name = current.getName();
        if (name != null && !name.startsWith("Not")) {
          scriptRunner.put(name, current);
          current.init(null);
					log(lvl, "initScriptingSupport: added: %s", name);
        }
      }
    }
    if (scriptRunner.isEmpty()) {
      Debug.error("Settings: No scripting support available. Rerun Setup!");
      String em = "Terminating: No scripting support available. Rerun Setup!";
      log(-1, em);
      if (Settings.isRunningIDE) {
        Sikulix.popError(em, "IDE has problems ...");
      }
      System.exit(1);
    } else {
      Runner.RDEFAULT = (String) scriptRunner.keySet().toArray()[0];
      Runner.EDEFAULT = scriptRunner.get(Runner.RDEFAULT).getFileEndings()[0];
      for (IScriptRunner r : scriptRunner.values()) {
        for (String e : r.getFileEndings()) {
          if (!supportedRunner.containsKey(Runner.endingTypes.get(e))) {
            supportedRunner.put(Runner.endingTypes.get(e), r);
          }
        }
      }
    }
		log(lvl, "initScriptingSupport: exit with defaultrunner: %s (%s)", Runner.RDEFAULT, Runner.EDEFAULT);
    isReady = true;
  }

  public static IScriptRunner getRunner(String script, String type) {
    init();
    IScriptRunner currentRunner = null;
    String ending = null;
    if (script != null) {
      for (String suffix : Runner.endingTypes.keySet()) {
        if (script.endsWith(suffix)) {
          ending = suffix;
          break;
        }
      }
    } else if (type != null) {
      currentRunner = scriptRunner.get(type);
      if (currentRunner != null) {
        return currentRunner;
      }
      ending = Runner.typeEndings.get(type);
      if (ending == null) {
        if (Runner.endingTypes.containsKey(type)) {
          ending = type;
        }
      }
    }
    if (ending != null) {
      for (IScriptRunner r : scriptRunner.values()) {
        if (r.hasFileEnding(ending) != null) {
          currentRunner = r;
          break;
        }
      }
    }
    if (currentRunner == null) {
      log(-1, "getRunner: no runner found for:\n%s", (script == null ? type : script));
    }
    return currentRunner;
  }

  public static boolean hasTypeRunner(String type) {
    return supportedRunner.containsKey(type);
  }

  public static void runningInteractive() {
    isRunningInteractive = true;
  }

  public static boolean getRunningInteractive() {
    return isRunningInteractive;
  }

  //<editor-fold defaultstate="collapsed" desc="run scripts">
  private static boolean isRunningScript = false;

  /**
   * INTERNAL USE: run scripts when sikulix.jar is used on commandline with args -r, -t or -i<br>
   * If you want to use it the args content must be according to the Sikulix command line parameter rules<br>
   * use run(script, args) to run one script from a script or Java program
   * @param args parameters given on commandline
   */
  public static void runscript(String[] args) {

    if (isRunningScript) {
      log(-1, "can run only one script at a time!");
      return;
    }

    IScriptRunner currentRunner = null;

    if (args != null && args.length > 1 && args[0].startsWith("-testSetup")) {
      currentRunner = getRunner(null, args[1]);
      if (currentRunner == null) {
        args[0] = null;
      } else {
        String[] stmts = new String[0];
        if (args.length > 2) {
          stmts = new String[args.length - 2];
          for (int i = 0; i < stmts.length; i++) {
            stmts[i] = args[i+2];
          }
        }
        if (0 != currentRunner.runScript(null, null, stmts, null)) {
          args[0] = null;
        }
      }
      isRunningScript = false;
      return;
    }

    runScripts = Runner.evalArgs(args);
    isRunningScript = true;

    if (runTime.runningInteractive) {
      int exitCode = 0;
      if (currentRunner == null) {
        String givenRunnerName = runTime.interactiveRunner;
        if (givenRunnerName == null) {
          currentRunner = getRunner(null, Runner.RDEFAULT);
        } else {
          currentRunner = getRunner(null, givenRunnerName);
        }
      }
      if (currentRunner == null) {
        System.exit(1);
      }
      exitCode = currentRunner.runInteractive(runTime.getSikuliArgs());
      currentRunner.close();
      Sikulix.endNormal(exitCode);
    }

		if (runScripts == null) {
			runTime.terminate(1, "option -r without any script");
		}

    if (runScripts.length > 0) {
			String scriptName = runScripts[0];
			if (scriptName != null && !scriptName.isEmpty() && scriptName.startsWith("git*")) {
				run(scriptName, runTime.getSikuliArgs());
				return;
			}
		}

    if (runScripts != null && runScripts.length > 0) {
      int exitCode = 0;
      runAsTest = runTime.runningTests;
      for (String givenScriptName : runScripts) {
        if (lastReturnCode == -1) {
          log(lvl, "Exit code -1: Terminating multi-script-run");
          break;
        }
        exitCode = new RunBox(givenScriptName, runTime.getArgs(), runAsTest).run();
        lastReturnCode = exitCode;
      }
      System.exit(exitCode);
    }
  }

  /**
   * run a script at scriptPath (.sikuli or .skl)
   * @param scriptPath absolute or relative to working folder
   * @param args parameter given to the script
   * @return exit code
   */
  public static int run(String scriptPath, String[] args) {
    runAsTest = false;
    init();
    String savePath = ImagePath.getBundlePath();
    int retVal = new RunBox(scriptPath, args, runAsTest).run();
    ImagePath.setBundlePath(savePath);
    return retVal;
  }

  /**
   * run a script at scriptPath (.sikuli or .skl)
   * @param scriptPath absolute or relative to working folder
   * @return exit code
   */
  public static int run(String scriptPath) {
    return run(scriptPath, new String[0]);
  }

  public static boolean transferScript(String src, String dest) {
    log(lvl, "transferScript: %s\nto: %s", src, dest);
    FileManager.FileFilter filter = new FileManager.FileFilter() {
      @Override
      public boolean accept(File entry) {
        if (entry.getName().endsWith(".html")) {
          return false;
        } else if (entry.getName().endsWith(".$py.class")) {
          return false;
        } else {
          for (String ending : Runner.endingTypes.keySet()) {
            if (entry.getName().endsWith("." + ending)) {
              return false;
            }
          }
        }
        return true;
      }
    };
    try {
      FileManager.xcopy(src, dest, filter);
    } catch (IOException ex) {
      log(-1, "transferScript: IOError: %s", ex.getMessage(), src, dest);
      return false;
    }
    log(lvl, "transferScript: completed");
    return true;
  }
//</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="helpers">
  public static void setProject() {
    RunBox.setProject();
  }

  public static int getLastReturnCode() {
    return lastReturnCode;
  }

  private static class RunBox {

    boolean asTest = false;
    String[] args = new String[0];

    String givenScriptHost = "";
    String givenScriptFolder = "";
    String givenScriptName = "";
    String givenScriptScript = "";
    String givenScriptType = "sikuli";
    String givenScriptScriptType = Runner.RDEFAULT;
		URL uGivenScript = null;
    URL uGivenScriptFile = null;
    boolean givenScriptExists = true;

    private RunBox(String givenName, String[] givenArgs, boolean isTest) {
      Object[] vars = Runner.runBoxInit(givenName, RunTime.scriptProject, RunTime.uScriptProject);
      givenScriptHost = (String) vars[0];
      givenScriptFolder = (String) vars[1];
      givenScriptName = (String) vars[2];
      givenScriptScript = (String) vars[3];
      givenScriptType = (String) vars[4];
      givenScriptScriptType = (String) vars[5];
      uGivenScript = (URL) vars[6];
      uGivenScriptFile = (URL) vars[7];
      givenScriptExists = (Boolean) vars[8];
      RunTime.scriptProject = (File) vars[9];
      RunTime.uScriptProject = (URL) vars[10];
      args = givenArgs;
      asTest = isTest;
    }

    private static void setProject() {
      if (RunTime.scriptProject == null) {
        RunTime.scriptProject = new File(FileManager.normalizeAbsolute(ImagePath.getBundlePath(), false)).getParentFile();
      }
    }

    private int run() {
      if (Runner.RASCRIPT.equals(givenScriptScriptType)) {
        return Runner.runas(givenScriptScript);
      } else if (Runner.RSSCRIPT.equals(givenScriptScriptType)) {
        return Runner.runps(givenScriptScript);
      } else if (Runner.RRSCRIPT.equals(givenScriptScriptType)) {
        return Runner.runrobot(givenScriptScript);
      }
      int exitCode = -1;
      IScriptRunner currentRunner = null;
      if (givenScriptType == "NET" && givenScriptExists) {
        log(lvl, "running script from net:\n%s", uGivenScript);
        if (Runner.RJSCRIPT.equals(givenScriptScriptType)) {
          exitCode = Runner.runjs(null, uGivenScript, givenScriptScript, args);
        } else {
          ScriptingSupport.init();
					currentRunner = scriptRunner.get(givenScriptScriptType);
          if (null == currentRunner) {
            log(-1, "running from net not supported for %s\n%s", givenScriptScriptType, uGivenScript);
          } else {
            ImagePath.addHTTP(uGivenScript.toExternalForm());
            exitCode = currentRunner.runScript(null, null, new String[] {givenScriptScript}, null);
          }
        }
      } else {
        log(lvl, "givenScriptName:\n%s", givenScriptName);
        if (givenScriptName.endsWith(".skl")) {
          givenScriptName = FileManager.unzipSKL(givenScriptName);
          if (givenScriptName == null) {
            log(-1, "not possible to make .skl runnable");
            return -9999;
          }
        }
        File fScript = Runner.getScriptFile(new File(givenScriptName));
        if (fScript == null) {
          return -9999;
        }
        fScript = new File(FileManager.normalizeAbsolute(fScript.getPath(), true));
        if (givenScriptName.endsWith(".jar") && fScript.getName().endsWith("$py.class")) {
          log(lvl, "Trying to run script: %s/%s", givenScriptName, fScript.getName());
          return JythonHelper.get().runJar(givenScriptName);
        }
        log(lvl, "Trying to run script:\n%s", fScript);
        if (fScript.getName().endsWith(".js")) {
          return Runner.runjs(fScript, null, givenScriptScript, args);
        }
        currentRunner = getRunner(fScript.getName(), null);
        if (currentRunner != null) {
          ImagePath.setBundlePath(fScript.getParent());
          if (asTest) {
            exitCode = currentRunner.runTest(fScript, null, args, null);
          } else {
            exitCode = currentRunner.runScript(fScript, null, args, null);
          }
        }
      }
      if (currentRunner != null) {
        currentRunner.close();
      }
      return exitCode;
    }
  }
}
