/*
 * Copyright (c) 2010-2018, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.idesupport;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.swing.ImageIcon;

import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;
import org.sikuli.script.IScriptRunner;
import org.sikuli.script.ImagePath;
import org.sikuli.script.RunTime;
import org.sikuli.script.Runner;
import org.sikuli.script.Sikulix;
import org.sikuli.script.runners.*;

public class ScriptingSupport {

  private static final String me = "ScriptingSupport: ";
  private static final int lvl = 3;
  private static void log(int level, String message, Object... args) {
    Debug.logx(level, me + message, args);
  }

  private static final Class<?>[] IDE_RUNNER_CLASSES = new Class<?>[]{JythonRunner.class, PythonRunner.class,
      JRubyRunner.class, JavaScriptRunner.class, TextRunner.class};
  private static final List<IScriptRunner> IDE_RUNNERS = new ArrayList<>();

  public static void init() {
    synchronized(IDE_RUNNERS) {
      if(IDE_RUNNERS.isEmpty()) {
        log(lvl, "enter");

        List<IScriptRunner> runners = Runner.getRunners();

        for (Class<?> runnerClass : IDE_RUNNER_CLASSES) {
          for (IScriptRunner runner : runners) {
            if(runnerClass.equals(runner.getClass())) {
              log(lvl, "added: %s", runner.getName());
              IDE_RUNNERS.add(runner);
              break;
            }
          }
        }

        if (IDE_RUNNERS.isEmpty()) {
          String em = "Terminating: No scripting support available. Rerun Setup!";
          log(-1, em);
          Sikulix.popError(em, "IDE has problems ...");
          System.exit(1);
        }

        IScriptRunner defaultRunner = IDE_RUNNERS.get(0);
        log(lvl, "exit: defaultrunner: %s (%s)", defaultRunner.getName(), defaultRunner.getExtensions()[0]);
      }
    }
  }

  public static String getDefaultExtension() {
    return getDefaultRunner().getExtensions()[0];
  }

  public static IScriptRunner getDefaultRunner() {
    return Runner.getRunner(RunTime.getDefaultRunnerType());
  }

  public static synchronized List<IScriptRunner> getRunners(){
    synchronized(IDE_RUNNERS) {
      init();
      return new ArrayList<IScriptRunner>(IDE_RUNNERS);
    }
  }

  //<editor-fold defaultstate="collapsed" desc="remote runner support">
  private static ServerSocket server = null;
  private static boolean isHandling = false;
  private static boolean shouldStop = false;
  private static ObjectOutputStream out = null;
  private static Scanner in = null;
  private static int runnerID = -1;

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

  static synchronized String getNextRunnerName() {
    return String.format("remoterunner%d", runnerID++);
  }
//</editor-fold>

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
          for (String ending : Runner.getExtensions()) {
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
}
