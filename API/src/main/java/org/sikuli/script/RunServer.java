/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.regex.Matcher;
import javax.script.ScriptEngine;
import org.sikuli.basics.Debug;

/**
 * EXPERIMENTAL --- NOT official API<br>
 *   not as is in version 2
 */
public class RunServer {
  private static ServerSocket server = null;
  private static PrintWriter out = null;
  private static Scanner in = null;
  private static boolean isHandling = false;
  private static boolean shouldStop = false;

//TODO set loglevel at runtime
  private static int logLevel = 0;
  private static void log(int lvl, String message, Object... args) {
    if (lvl < 0 || lvl >= logLevel) {
      System.out.println((lvl < 0 ? "[error] " : "[info] ") +
              String.format("RunServer: " + message, args));
    }
  }
  private static void log(String message, Object... args) {
    log(0, message, args);
  }

	private RunServer() {
	}

  static File isRunning = null;
  static FileOutputStream isRunningFile = null;

  public static boolean run(String[] args) {
		if (args == null) {
			args = new String[0];
		}
    String userArgs = "";
    for (String userArg : RunTime.get().getArgs()) {
      userArgs += userArg + " ";
    }
    if (!userArgs.isEmpty()) {
      userArgs = "\nWith User parameters: " + userArgs;
    }
    int port = getPort(args.length > 0 ? args[0] : null);
    try {
      try {
        if (port > 0) {
					log(3, "Starting: trying port: %d %s", port, userArgs);
          server = new ServerSocket(port);
        }
      } catch (Exception ex) {
        log(-1, "Starting: " + ex.getMessage());
      }
      if (server == null) {
        log(-1, "could not be started");
        return false;
      }
      String theIP = InetAddress.getLocalHost().getHostAddress();
      String theServer = String.format("%s %d", theIP, port);
      isRunning = new File(RunTime.get().fSikulixStore, "RunServer.txt");
      try {
        isRunning.createNewFile();
        isRunningFile = new FileOutputStream(isRunning);
        if (null == isRunningFile.getChannel().tryLock()) {
          log(-1, "Terminating on FatalError: already running");
          return false;
        }
        isRunningFile.write(theServer.getBytes());
      } catch (Exception ex) {
        log(-1, "Terminating on FatalError: cannot access to lock for/n" + isRunning);
        return false;
      }
      Runtime.getRuntime().addShutdownHook(new Thread() {
        @Override
        public void run() {
          log(3, "final cleanup");
          if (isRunning != null) {
            try {
              isRunningFile.close();
            } catch (IOException ex) {
            }
            isRunning.delete();
          }
        }
      });
      while (true) {
        log("now waiting on port: %d at %s", port, theIP);
        Socket socket = server.accept();
        out = new PrintWriter(socket.getOutputStream());
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
      log(-1, "start handling not possible: " + port);
      return false;
    }
    log("now stopped on port: " + port);
    return true;
  }

  private static int getPort(String p) {
    int port;
    int pDefault = 50001;
    if (p != null) {
      try {
        port = Integer.parseInt(p);
      } catch (NumberFormatException ex) {
        log(-1, "given port not useable: %s --- using default", p);
        return pDefault;
      }
    } else {
      return pDefault;
    }
    if (port < 1024) {
      port += pDefault;
    }
    return port;
  }

  static ScriptEngine jsRunner = null;
	static File scriptFolder = null;
	static String scriptFolderNet = null;
	static File imageFolder = null;
	static String imageFolderNet = null;

  private static class HandleClient implements Runnable {

    private volatile boolean keepRunning;
    private boolean shouldKeep = false;
    Thread thread;
    Socket socket;
    Boolean shouldStop = false;

    public HandleClient(Socket sock) {
      init(sock);
    }

    private void init(Socket sock) {
      socket = sock;
      if (in == null || out == null) {
        RunServer.log(-1, "communication not established");
        System.exit(1);
      }
      thread = new Thread(this, "HandleClient");
      keepRunning = true;
      thread.start();
    }

    public boolean getShouldStop() {
      return shouldStop;
    }

    boolean isHTTP = false;
    String request;
    String rCommand;
    String rRessource;
    String rVersion = "HTTP/1.1";
    String rQuery;
    String[] rArgs;
    String rMessage = "";
    String rStatus;
    String rStatusOK = "200 OK";
    String rStatusBadRequest = "400 Bad Request";
    String rStatusNotFound = "404 Not Found";
    String rStatusServerError = "500 Internal Server Error";
    String rStatusServiceNotAvail = "503 Service Unavailable";
		Object evalReturnObject;
    String runTypeJS = "JavaScript";
    String runTypePY = "Python";
    String runTypeRB = "Ruby";
    String runType = runTypeJS;

    @Override
    public void run() {
			Debug.on(3);
      RunServer.log("now handling client: " + socket);
      while (keepRunning) {
        try {
          String inLine = in.nextLine();
          if (inLine != null) {
            if (!isHTTP) {
              RunServer.log("processing: <%s>", inLine);
            }
						boolean success = true;
            if (inLine.startsWith("GET /") && inLine.contains("HTTP/")) {
              isHTTP = true;
							request = inLine;
              continue;
            }
            if (isHTTP) {
              if (!inLine.isEmpty()) {
                continue;
              }
            }
            if (!isHTTP) {
              request = "GET /" + inLine + " HTTP/1.1";
            }
            success = checkRequest(request);
            if (success) {
              // STOP
              if (rCommand.contains("STOP")) {
                rMessage = "stopping server";
                shouldStop = true;
                shouldKeep = false;
              } else if (rCommand.contains("EXIT")) {
                rMessage = "stopping client";
                shouldKeep = false;
              // START
              } else if (rCommand.startsWith("START")) {
                runType = runTypeJS;
                if (rCommand.length() > 5) {
                  if ("P".equals(rCommand.substring(5, 6))) {
                    runType = runTypePY;
                  } else if ("R".equals(rCommand.substring(5, 6))) {
                    runType = runTypeRB;
                  }
                }
                success = startRunner(runType, null, null);
                rMessage = "startRunner for: " + runType;
                if (!success) {
                  rMessage = "startRunner: not possible for: " + runType;
                  rStatus = rStatusServiceNotAvail;
                }
              // SCRIPTS
              } else if (rCommand.startsWith("SCRIPTS")) {
                if (rRessource.isEmpty()) {
                  rMessage = "no scriptFolder given ";
                  rStatus = rStatusBadRequest;
                  success = false;
                } else {
                  scriptFolder = getFolder(rRessource);
                  if (scriptFolder.getPath().startsWith("__NET/")) {
                    scriptFolderNet = "http://" + scriptFolder.getPath().substring(6);
                    rMessage = "scriptFolder now: " + scriptFolderNet;
                  } else {
                    scriptFolderNet = null;
                    rMessage = "scriptFolder now: " + scriptFolder.getAbsolutePath();
                    if (!scriptFolder.exists()) {
                      rMessage = "scriptFolder not found: " + scriptFolder.getAbsolutePath();
                      rStatus = rStatusNotFound;
                      success = false;
                    }
                  }
                }
              // IMAGES
              } else if (rCommand.startsWith("IMAGES")) {
                String asImagePath;
                if (rRessource.isEmpty()) {
                  rMessage = "no imageFolder given ";
                  rStatus = rStatusBadRequest;
                  success = false;
                } else {
                  imageFolder = getFolder(rRessource);
                  if (imageFolder.getPath().startsWith("__NET/")) {
                    imageFolderNet = "http://" + imageFolder.getPath().substring(6);
                    rMessage = "imageFolder now: " + imageFolderNet;
                    asImagePath = imageFolderNet;
                  } else {
                    String fpGiven = imageFolder.getAbsolutePath();
                    if (!imageFolder.exists()) {
                      imageFolder = new File(imageFolder.getAbsolutePath() + ".sikuli");
                      if (!imageFolder.exists()) {
                        rMessage = "imageFolder not found: " + fpGiven;
                        rStatus = rStatusNotFound;
                        success = false;
                      }
                    }
                    asImagePath = imageFolder.getAbsolutePath();
                  }
                  rMessage = "imageFolder now: " + asImagePath;
                  ImagePath.add(asImagePath);
                }
                // RUN
              } else if (rCommand.startsWith("RUN")) {
                String script = rRessource;
                File fScript = null;
                File fScriptScript = null;
                if (scriptFolderNet != null) {
                  rMessage = "runScript from net not yet supported";
                  rStatus = rStatusServiceNotAvail;
                  success = false;
                }
                if (success) {
                  Debug.log("Using script folder: " + RunServer.scriptFolder);
                  fScript = new File(RunServer.scriptFolder, script);
                  if (!fScript.exists()) {
                    if (script.endsWith(".sikuli")) {
                      script = script.replace(".sikuli", "");
                    } else {
                      script = script + ".sikuli";
                    }
                    fScript = new File(scriptFolder, script);
                  }
                  String scriptScript = script.replace(".sikuli", "");
                  fScriptScript = new File(fScript, scriptScript + ".js");
                  success = fScriptScript.exists();
                  if (!success) {
                    fScriptScript = new File(fScript, scriptScript + ".py");
                    success = fScript.exists() && fScriptScript.exists();
                    if (!success) {
                      RunServer.log("Script folder path: " + fScript.getAbsolutePath());
                      RunServer.log("Script file path: " + fScriptScript.getAbsolutePath());
                      rMessage = "runScript: script not found, not valid or not supported "
                              + fScriptScript.toString();
                    }
                    runType = runTypePY;
                  }
                }
                if (success) {
                  ImagePath.setBundlePath(fScript.getAbsolutePath());
                  List<String> args = new ArrayList<String>();

                  if (this.rQuery != null && this.rQuery.length() > 0) {
                    String[] params = this.rQuery.split("[;&]");

                    for (String param : params) {
                      String[] pair = param.split("[=]");

                      if (pair != null && pair.length == 2) {
                        // Needs both a variable name and value, and supports repeated parameters
                        String arg = String.format("--%1$s=%2$s", pair[0], pair[1]);

                        args.add(arg);
                      }
                    }
                  }

                  success = this.startRunner(this.runType, fScript, fScriptScript, args.toArray(new String[0]));
                }
              } else if (rCommand.startsWith("EVAL")) {
                if (jsRunner != null) {
                  String line = rQuery;
                  try {
                    evalReturnObject = jsRunner.eval(line);
                    rMessage = "runStatement: returned: "
                            + (evalReturnObject == null ? "null" : evalReturnObject.toString());
                    success = true;
                  } catch (Exception ex) {
                    rMessage = "runStatement: raised exception on eval: " + ex.toString();
                    success = false;
                  }
                } else {
                  rMessage = "runStatement: not possible --- no runner";
                  rStatus = rStatusServiceNotAvail;
                  success = false;
                }
              }
            }
            String retVal = "";
            if (isHTTP) {
              retVal = "HTTP/1.1 " + rStatus;
              String state = (success ? "PASS " : "FAIL ") + rStatus.substring(0,3) + " ";
              retVal += "\r\n\r\n" + state + rMessage + "\r";
            } else {
              retVal = (success ? "isok:\n" : "fail:\n") + rMessage + "\n###+++###";
            }
            try {
              out.println(retVal);
              out.flush();
              RunServer.log("returned:\n"  + retVal.replace("###+++###", ""));
            } catch (Exception ex) {
              RunServer.log(-1, "write response: Exception:\n" + ex.getMessage());
            }
            stopRunning();
          }
        } catch (Exception ex) {
          RunServer.log(-1, "while processing: Exception:\n" + ex.getMessage());
          shouldKeep = false;
          stopRunning();
        }
      }
      try {
        Thread.sleep(100);
      } catch (InterruptedException ex) {
        shouldKeep = false;
        stopRunning();
      }
    }

    public void stopRunning() {
      if (!shouldKeep) {
        in.close();
        out.close();
        try {
          socket.close();
        } catch (IOException ex) {
          RunServer.log(-1, "fatal: socket not closeable");
          System.exit(1);
        }
        keepRunning = false;
      }
    }

    private File getFolder(String path) {
      File aFolder = new File(path);
      Debug.log("Original path: " + aFolder);
      if (path.toLowerCase().startsWith("/home/")) {
        path = path.substring(6);
        aFolder = new File(RunTime.get().fUserDir, path);
      } else if (path.toLowerCase().startsWith("/net/")) {
        path = "__NET/" + path.substring(5);
        aFolder = new File(path);
      } else if (RunTime.get().runningWindows) {
          Matcher matcher = java.util.regex.Pattern.compile("(?ix: ^ (?: / ([a-z]) [:]? /) (.*) $)").matcher(path);
          // Assume specified drive exists or fallback on the default/required drive
          String newPath = matcher.matches() ? matcher.replaceAll("$1:/$2") : ("c:" + path);
          aFolder = new File(newPath);
      }
      Debug.log("Transformed path: " + aFolder);
      return aFolder;
    }
    
    private boolean checkRequest(String request) {
      shouldKeep = false;
      rCommand = "NOOP";
      rMessage = "invalid: " + request;
      rStatus = rStatusBadRequest;
      String[] parts = request.split("\\s");
      if (parts.length != 3 || !"GET".equals(parts[0]) || !parts[1].startsWith("/")) {
        return false;
      }
      if (!rVersion.equals(parts[2])) {
        return false;
      }
      String cmd = parts[1].substring(1);
      if (cmd.startsWith("X")) {
        cmd = cmd.substring(1);
        shouldKeep = true;
      }
      parts = cmd.split("\\?");
      cmd = parts[0];
      rQuery = "";
      if (parts.length > 1) {
        rQuery = parts[1];
      }
      parts = cmd.split("/");
      if (!"START,STARTP,STOP,EXIT,SCRIPTS,IMAGES,RUN,EVAL,".contains((parts[0]+",").toUpperCase())) {
        rMessage = "invalid command: " + request;
        return false;
      }
      rCommand = parts[0].toUpperCase();
      rMessage = "";
      rStatus = rStatusOK;
      rRessource = "";
      if (parts.length > 1) {
        rRessource = cmd.substring(rCommand.length());
      }
      return true;
    }

    private boolean startRunner(String runType, File fScript, File fScriptScript) {
      return this.startRunner(runType, fScript, fScriptScript, new String[0]);
    }

    private boolean startRunner(String runType, File fScript, File fScriptScript, String[] args) {
      if (runTypeJS.equals(runType)) {
        if (jsRunner == null) {
          try {
            jsRunner = Runner.initjs();
            String prolog = "";
            prolog = Runner.prologjs(prolog);
            prolog = Runner.prologjs(prolog);
            jsRunner.eval(prolog);
          } catch (Exception ex) {
            rMessage = "startRunner JavaScript: not possible";
            rStatus = rStatusServiceNotAvail;
            return false;
          }
        }
        if (fScript == null) {
          return true;
        }
        if (jsRunner != null) {
          try {
            evalReturnObject = jsRunner.eval(new java.io.FileReader(fScriptScript));
            rMessage = "runScript: returned: "
                    + (evalReturnObject == null ? "null" : evalReturnObject.toString());
            return evalReturnObject != null;
          } catch (Exception ex) {
            rMessage = "runScript: script raised exception on run: " + ex.toString();
            return false;
          }
        } else {
          return false;
        }
      } else if (runTypePY.equals(runType)) {
        Integer retval = 0;
        if (!Runner.initpy()) {
          retval = -1;
        }
        if (fScript != null && retval == 0) {
          // Arguments are passed to Python in the long format: --name=value
          evalReturnObject = Runner.run(fScript.getAbsolutePath(), args);
          try {
            retval = Integer.parseInt(evalReturnObject.toString());
            if (retval == -999) {
              retval = 0;
            }
          } catch (Exception ex) {
            retval = 0;
          }
        }
        if (retval < 0) {
          rMessage = "startRunner Python: not possible or crashed with exception";
          rStatus = rStatusServiceNotAvail;
          return false;
        }
        if (fScript != null) {
          rMessage = "runScript: returned: " + retval.toString();
        }
      }
      return true;
    }
  }
}