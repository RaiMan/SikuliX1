/*
 * Copyright (c) 2010-2018, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script.support;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;
import org.sikuli.script.ImagePath;
import org.sikuli.script.runners.JavaScriptRunner;
import org.sikuli.script.runners.JythonRunner;
import org.sikuli.script.runners.JRubyRunner;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.predicate.Predicates;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.RoutingHandler;
import io.undertow.server.ServerConnection;
import io.undertow.server.ServerConnection.CloseListener;
import io.undertow.server.handlers.ExceptionHandler;
import io.undertow.server.handlers.RequestLimitingHandler;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.server.handlers.resource.ResourceHandler;
import io.undertow.server.handlers.resource.ResourceManager;
import io.undertow.util.Headers;
import io.undertow.util.Methods;
import io.undertow.util.StatusCodes;
import io.undertow.util.URLUtils;

/**
 * EXPERIMENTAL --- NOT official API<br>
 * not as is in version 2
 */
public class SikulixServer {
  static {
    // change Undertow-Logging(org.jboss.logging) from log4j to slf4j
    System.setProperty("org.jboss.logging.provider", "slf4j");
  }

  private static Undertow server = null;
  private static boolean isHandling = false;
  private static boolean shouldStop = false;
  private static Object lock = new Object();

  private static int logLevel = RunTime.getDebugLevelStart();

  private static void dolog(int lvl, String message, Object... args) {
    if (Debug.isBeQuiet()) {
      return;
    }
    if (lvl <= logLevel) {
      System.out.println((lvl < 0 ? "[error] " : (lvl > 0 ? "[debug] " : "[info] ")) +
              String.format("SikulixServer: " + message, args));
    }
  }

  private static void dolog(String message, Object... args) {
    dolog(0, message, args);
  }

  static File isRunning = null;
  static FileOutputStream isRunningFile = null;

  private static File asFolder(String option) {
    if (null == option) {
      return null;
    }
    File folder = new File(option);
    if (!folder.isAbsolute()) {
      folder = new File(RunTime.get().fWorkDir, option);
    }
    if (folder.isDirectory() && folder.exists()) {
      return folder;
    }
    return null;
  }

  private static File asFile(String option) {
    if (null == option) {
      return null;
    }
    if (null == asFolder(option)) {
      File file = new File(option);
      if (!file.isAbsolute()) {
        file = new File(RunTime.get().fWorkDir, option);
      }
      if (file.exists()) {
        return file;
      }
    }
    return null;
  }

  private static Map<String, File> groups = new HashMap<>();
  private static final String DEFAULT_GROUP = "DEFAULT_GROUP";

  private static void makeGroups(String option) {
    File folder;
    groups.put(DEFAULT_GROUP, RunTime.get().fWorkDir);
    if (null != (folder = asFolder(option))) {
      groups.put(DEFAULT_GROUP, folder);
      return;
    }
    File folders;
    if (null != (folders = asFile(option))) {
      if (FilenameUtils.getExtension(folders.getPath()).isEmpty() ||
              FilenameUtils.getExtension(folders.getPath()).equals("txt")) {
        dolog(3,"option -g txt-file: %s", folders);
        String[] items = FileManager.readFileToStringArray(folders);
        Pattern pattern = Pattern.compile("(.*?)[ :=](.*)");
        boolean isFirst = true;
        for (String item : items) {
          item = item.trim();
          if (item.startsWith("#") || item.startsWith("//")) {
            continue;
          }
          String grp = "";
          String fldr = "";
          Matcher matcher = pattern.matcher(item);
          if (matcher.matches()) {
            grp = matcher.group(1).trim();
            fldr = matcher.group(2).trim();
            if (fldr.startsWith(":") | fldr.startsWith("=")) {
              fldr = fldr.substring(1).trim();
            }
          } else if (isFirst) {
            fldr = item;
            grp = DEFAULT_GROUP;
          }
          if (null != asFolder(fldr)) {
            if (isFirst) {
              groups.put(DEFAULT_GROUP, asFolder(fldr));
              isFirst = false;
            }
            if (!groups.containsKey(grp)) {
              groups.put(grp, asFolder(fldr));
              dolog(3, "group: %s folder: %s", grp, asFolder(fldr));
            }
          }
        }
      } else if (FilenameUtils.getExtension(folders.getPath()).equals("json")) {
        //TODO evaluate folders.json
        dolog(-1, "(to be implemented) option -g json-file: %s", folders);
      }
    }
  }

  private static List<String> allowedIPs = new ArrayList<>();
  private static final String DEFAULT_ALLOWED_IP = "localhost";

  private static void makeAllowedIPs(String option) {
    allowedIPs.add(DEFAULT_ALLOWED_IP);
    File allowedIPsFile;
    if (null != (allowedIPsFile = asFile(option))) {
      if (FilenameUtils.getExtension(allowedIPsFile.getPath()).isEmpty() ||
              FilenameUtils.getExtension(allowedIPsFile.getPath()).equals("txt")) {
        dolog(3,"option -x txt-file: %s", allowedIPsFile);
        String[] items = FileManager.readFileToStringArray(allowedIPsFile);
        for (String item : items) {
          allowedIPs.add(item);
          dolog(3, "allowed: %s", item);
        }
      }
    } else if (null != option){
      allowedIPs.add(option);
      dolog(3, "allowed: %s", option);
    }
  }

  public static boolean run() {
    String userArgs = "";
    for (String userArg : RunTime.getUserArgs()) {
      userArgs += userArg + " ";
    }
    if (!userArgs.isEmpty()) {
      userArgs = "\nWith User parameters: " + userArgs;
    }
    int port = RunTime.getServerPort();
    String theIP = RunTime.getServerIP();
    String groupsOption = RunTime.getServerGroups();
    makeGroups(groupsOption);
    dolog(3,"DEFAULT_GROUP: %s", groups.get(DEFAULT_GROUP));
    String extraOption = RunTime.getServerExtra();
    makeAllowedIPs(extraOption);
    try {
      String theServer = String.format("%s %d", theIP, port);
      isRunning = new File(RunTime.get().fSikulixStore, "SikulixServer.txt");
      try {
        isRunning.createNewFile();
        isRunningFile = new FileOutputStream(isRunning);
        if (null == isRunningFile.getChannel().tryLock()) {
          dolog(-1, "Terminating on FatalError: already running");
          return false;
        }
        isRunningFile.write(theServer.getBytes());
      } catch (Exception ex) {
        dolog(-1, "Terminating on FatalError: cannot access to lock for/n" + isRunning);
        return false;
      }
      try {
        if (port > 0) {
          dolog(3, "Starting: trying port: %d %s", port, userArgs);
          server = createServer(port, theIP);
          server.start();
          Debug.on(3);
          dolog("at %s:%d in %s", theIP, port, groups.get(DEFAULT_GROUP));
        }
      } catch (Exception ex) {
        dolog(-1, "Starting: " + ex.getMessage());
      }
      if (server == null) {
        dolog(-1, "could not be started");
        return false;
      }
      Runtime.getRuntime().addShutdownHook(new Thread() {
        @Override
        public void run() {
          dolog(3, "final cleanup");
          if (isRunning != null) {
            try {
              isRunningFile.close();
            } catch (IOException ex) {
            }
            isRunning.delete();
          }
        }
      });
      synchronized (lock) {
        while (!shouldStop) {
          lock.wait();
        }
      }
    } catch (Exception e) {
    }
    if (!isHandling) {
      dolog(-1, "start handling not possible: " + port);
      return false;
    }
    dolog("now stopped on port: " + port);
    return true;
  }

  private static Undertow createServer(int port, String ipAddr) {
    RoutingHandler commands = Handlers.routing()
            .add(Methods.GET, "/stop*", Predicates.prefix("stop"),
                    new StopCommandHttpHandler())
            .add(Methods.GET, "/exit*", Predicates.prefix("exit"),
                    new ExitCommandHttpHandler())
            .add(Methods.GET, "/start*", Predicates.prefixes("startp", "startr", "start"),
                    new StartCommandHttpHandler())
            .add(Methods.GET, "/scripts*", Predicates.prefix("scripts"),
                    new ScriptsCommandHttpHandler())
            .add(Methods.GET, "/images*", Predicates.prefix("images"),
                    new ImagesCommandHttpHandler())
            .add(Methods.GET, "/run*", Predicates.prefix("run"),
                    new RunCommandHttpHandler())
            .setFallbackHandler(new AbstractCommandHttpHandler() {
              @Override
              public void handleRequest(HttpServerExchange exchange) throws Exception {
                sendResponse(exchange, false, StatusCodes.BAD_REQUEST,
                        "invalid command: " + exchange.getRelativePath());
              }
            });
    CommandRootHttpHandler cmdRoot = new CommandRootHttpHandler(new PreRoutingHttpHandler(
            new RequestLimitingHandler(1, commands)));
    cmdRoot.addExceptionHandler(Throwable.class, new AbstractCommandHttpHandler() {
      @Override
      public void handleRequest(HttpServerExchange exchange) throws Exception {
        Throwable ex = exchange.getAttachment(ExceptionHandler.THROWABLE);
        SikulixServer.dolog(-1, "while processing: Exception:\n" + ex.getMessage());
        sendResponse(exchange, false, StatusCodes.INTERNAL_SERVER_ERROR,
                "server error: " + ex.getMessage());
      }
    });

    ResourceManager resourceManager = new ClassPathResourceManager(RunTime.class.getClassLoader(), "htdocs");
    ResourceHandler resource = new ResourceHandler(resourceManager, cmdRoot);
    resource.addWelcomeFiles("ControlBox.html");

    Undertow server = Undertow.builder()
            .addHttpListener(port, "localhost")
            .addHttpListener(port, ipAddr)
            .setHandler(resource)
            .build();
    return server;
  }

  private static class StopCommandHttpHandler extends AbstractCommandHttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
      sendResponse(exchange, true, StatusCodes.OK, "stopping server");
      exchange.getConnection().addCloseListener(new CloseListener() {
        @Override
        public void closed(ServerConnection connection) {
          synchronized (lock) {
            shouldStop = true;
            lock.notify();
          }
        }
      });
      server.stop();
    }
  }

  private static class ExitCommandHttpHandler extends AbstractCommandHttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
      sendResponse(exchange, true, StatusCodes.OK, "stopping client");
      exchange.getConnection().close();
    }
  }

  private static class StartCommandHttpHandler extends AbstractCommandHttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
      String runType = JavaScriptRunner.NAME;
      String path = exchange.getRelativePath();
      if (path.length() > 6) {
        if ("p".equals(path.substring(6, 7))) {
          runType = JythonRunner.NAME;
        } else if ("r".equals(path.substring(6, 7))) {
          runType = JRubyRunner.NAME;
        }
      }
      boolean success = true;
      int statusCode;
      String message;
      try {
        Runner.getRunner(runType).init(null);
        message = "ready to run: " + runType;
        statusCode = StatusCodes.OK;
      } catch (Exception ex) {
        SikulixServer.dolog("ScriptRunner init not possible: " + ex.getMessage());
        message = "not ready to run: " + runType;
        statusCode = StatusCodes.SERVICE_UNAVAILABLE;
        success = false;
      }
      sendResponse(exchange, success, statusCode, message);
    }
  }

  private static class ScriptsCommandHttpHandler extends AbstractCommandHttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
      String resource = exchange.getQueryParameters().get("*").getFirst();
      boolean success = true;
      int statusCode = StatusCodes.OK;
      String message = null;
      if (resource.isEmpty()) {
        message = "no scriptFolder given ";
        statusCode = StatusCodes.BAD_REQUEST;
        success = false;
      } else {
        setScriptFolder(getFolder(resource));
        if (getScriptFolder().getPath().startsWith("__NET/")) {
          setScriptFolderNet("http://" + getScriptFolder().getPath().substring(6));
          message = "scriptFolder now: " + getScriptFolderNet();
        } else {
          setScriptFolderNet(null);
          message = "scriptFolder now: " + getScriptFolder().getAbsolutePath();
          if (!getScriptFolder().exists()) {
            message = "scriptFolder not found: " + getScriptFolder().getAbsolutePath();
            statusCode = StatusCodes.NOT_FOUND;
            success = false;
          }
        }
      }
      sendResponse(exchange, success, statusCode, message);
    }
  }

  private static class ImagesCommandHttpHandler extends AbstractCommandHttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
      String resource = exchange.getQueryParameters().get("*").getFirst();
      String asImagePath;
      boolean success = true;
      int statusCode = StatusCodes.OK;
      String message = null;
      if (resource.isEmpty()) {
        message = "no imageFolder given ";
        statusCode = StatusCodes.BAD_REQUEST;
        success = false;
      } else {
        setImageFolder(getFolder(resource));
        if (getImageFolder().getPath().startsWith("__NET/")) {
          setImageFolderNet("http://" + getImageFolder().getPath().substring(6));
          message = "imageFolder now: " + getImageFolderNet();
          asImagePath = getImageFolderNet();
        } else {
          String fpGiven = getImageFolder().getAbsolutePath();
          if (!getImageFolder().exists()) {
            setImageFolder(new File(getImageFolder().getAbsolutePath() + ".sikuli"));
            if (!getImageFolder().exists()) {
              message = "imageFolder not found: " + fpGiven;
              statusCode = StatusCodes.NOT_FOUND;
              success = false;
            }
          }
          asImagePath = getImageFolder().getAbsolutePath();
        }
        message = "imageFolder now: " + asImagePath;
        ImagePath.add(asImagePath);
      }
      sendResponse(exchange, success, statusCode, message);
    }
  }

  private static class RunCommandHttpHandler extends AbstractCommandHttpHandler {
    private static final Pattern PATTERN_QUERY_ARGS = Pattern.compile("args=(?<args>[^&]+)");
    private static List<String> SERVER_SUPPORTED_EXTENSIONS = new ArrayList<>();

    {
      SERVER_SUPPORTED_EXTENSIONS.addAll(Arrays.asList(JavaScriptRunner.EXTENSIONS));
      SERVER_SUPPORTED_EXTENSIONS.addAll(Arrays.asList(JythonRunner.EXTENSIONS));
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
      String script = exchange.getQueryParameters().get("*").getFirst();
      File fScript = null;
      boolean success = true;
      int statusCode = StatusCodes.OK;
      String message = null;
      if (getScriptFolderNet() != null) {
        message = "runScript from net not yet supported";
        statusCode = StatusCodes.SERVICE_UNAVAILABLE;
        success = false;
      }
      if (success) {
        Debug.log("Using script folder: " + getScriptFolder());
        fScript = new File(getScriptFolder(), script);
        if (!fScript.exists()) {
          if (script.endsWith(".sikuli")) {
            script = script.replace(".sikuli", "");
          } else {
            script = script + ".sikuli";
          }
          fScript = new File(getScriptFolder(), script);
        }
        File innerScriptFile = Runner.getScriptFile(fScript);
        success = SERVER_SUPPORTED_EXTENSIONS.stream().anyMatch(s ->
                innerScriptFile != null && s.equals(FilenameUtils.getExtension(innerScriptFile.getName()).toLowerCase()));
        if (!success) {
          SikulixServer.dolog("Script folder path: " + fScript.getAbsolutePath());
          message = "runScript: script not found, not valid or not supported "
                  + fScript.getAbsolutePath();
          statusCode = StatusCodes.NOT_FOUND;
        }
      }
      if (success) {
        IScriptRunner.Options runOptions = new IScriptRunner.Options();
        runOptions.setScriptName(fScript.toString());
        ImagePath.setBundlePath(fScript.getAbsolutePath());
        List<String> args = getQueryAndToArgs(exchange);
        int retval = Runner.run(fScript.toString(), args.toArray(new String[args.size()]), runOptions);
        message = "runScript: returned: " + retval;
        if (retval < 0) {
          statusCode = StatusCodes.SERVICE_UNAVAILABLE;
        }
      }
      sendResponse(exchange, success, statusCode, message);
    }

    private List<String> getQueryAndToArgs(final HttpServerExchange exchange) {
      List<String> args = new LinkedList<String>();
      String queryString = exchange.getQueryString();
      if (queryString != null) {
        Matcher matcher = PATTERN_QUERY_ARGS.matcher(queryString);
        if (matcher.find()) {
          StringBuilder buf = new StringBuilder();
          String[] tokens = matcher.group("args").split(";");
          for (String token : tokens) {
            args.add(URLUtils.decode(token, "UTF-8", true, buf));
          }
        }
      }
      return args;
    }
  }

  private static abstract class AbstractCommandHttpHandler implements HttpHandler {
    private static File scriptFolder = null;
    private static String scriptFolderNet = null;
    private static File imageFolder = null;
    private static String imageFolderNet = null;

    protected void setScriptFolder(File scriptFolder) {
      AbstractCommandHttpHandler.scriptFolder = scriptFolder;
    }

    protected File getScriptFolder() {
      return AbstractCommandHttpHandler.scriptFolder;
    }

    protected void setScriptFolderNet(String scriptFolderNet) {
      AbstractCommandHttpHandler.scriptFolderNet = scriptFolderNet;
    }

    protected String getScriptFolderNet() {
      return AbstractCommandHttpHandler.scriptFolderNet;
    }

    protected void setImageFolder(File imageFolder) {
      AbstractCommandHttpHandler.imageFolder = imageFolder;
    }

    protected File getImageFolder() {
      return AbstractCommandHttpHandler.imageFolder;
    }

    protected void setImageFolderNet(String imageFolderNet) {
      AbstractCommandHttpHandler.imageFolderNet = imageFolderNet;
    }

    protected String getImageFolderNet() {
      return AbstractCommandHttpHandler.imageFolderNet;
    }

    protected void sendResponse(HttpServerExchange exchange, boolean success, int stateCode, String message) {
      exchange.setStatusCode(stateCode);
      exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
      String head = exchange.getProtocol() + " " + StatusCodes.getReason(exchange.getStatusCode());
      String body = (success ? "PASS " : "FAIL ") + exchange.getStatusCode() + " " + message;
      exchange.getResponseSender().send(body);

      SikulixServer.dolog("returned:\n" + (head + "\n\n" + body));
    }

    protected File getFolder(String path) {
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
  }

  private static class PreRoutingHttpHandler implements HttpHandler {
    private static final Pattern PATTERN = Pattern.compile("^(?<command>/[^/]+)(?<resource>/.*)*");
    private HttpHandler next;

    public PreRoutingHttpHandler(HttpHandler next) {
      this.next = next;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
      String path = exchange.getRelativePath();
      Matcher matcher = PATTERN.matcher(path);
      if (matcher.find()) {
        String command = matcher.group("command").toLowerCase();
        String resource = Optional.ofNullable(matcher.group("resource")).orElse("");
        path = command + resource;
      }
      exchange.setRelativePath(path);

      next.handleRequest(exchange);
    }
  }

  private static class CommandRootHttpHandler extends ExceptionHandler {
    public CommandRootHttpHandler(HttpHandler handler) {
      super(handler);
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
      isHandling = true;
      SikulixServer.dolog("received request: <%s %s %s> from %s",
              exchange.getRequestMethod(), exchange.getRequestURI(), exchange.getProtocol(),
              exchange.getSourceAddress());
      super.handleRequest(exchange);
    }
  }
}