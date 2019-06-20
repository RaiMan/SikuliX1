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
import org.apache.commons.lang3.tuple.Pair;
import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;
import org.sikuli.script.runners.RobotRunner;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.attribute.RelativePathAttribute;
import io.undertow.predicate.Predicates;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.RoutingHandler;
import io.undertow.server.ServerConnection;
import io.undertow.server.ServerConnection.CloseListener;
import io.undertow.server.handlers.ExceptionHandler;
import io.undertow.server.handlers.RequestLimit;
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

  private static String serverIPdefault = "0.0.0.0";
  private static String serverIP = serverIPdefault;

  private static int serverPortdefault = 50001;
  private static int serverPort = serverPortdefault;

  private static List<Pair<String, Integer>> serverListenAt = new ArrayList<>();

  private static String serverOption1 = "";
  private static String serverOption2 = "";

  private static void evalServerOptions(String[] listenAt) {
    if (null != listenAt && listenAt.length > 0) {
      serverOption1 = listenAt[0];
      serverOption2 = "";
      if (listenAt.length == 1) {
        if (serverOption1.startsWith("[") && serverOption1.contains("]:")) {
          listenAt = serverOption1.split("]:");
          listenAt[0] = listenAt[0].substring(1);
        } else if (serverOption1.contains(":")) {
          listenAt = serverOption1.split(":");
          if (listenAt.length > 2) {
            listenAt = new String[]{serverOption1, "" + serverPort};
          }
        } else {
          try {
            serverPort = Integer.parseInt(serverOption1);
          } catch (NumberFormatException e) {
            serverIP = serverOption1;
          }
        }
      }
      if (listenAt.length > 1) {
        serverOption1 = listenAt[0].trim();
        serverOption2 = listenAt[1].trim();
        if (!serverOption1.isEmpty()) {
          serverIP = serverOption1;
        }
        try {
          serverPort = Integer.parseInt(serverOption2);
        } catch (NumberFormatException e) {
          serverOption2 = "?" + serverOption2;
        }
      }
      if (serverOption1.isEmpty()) {
        serverOption1 = "?";
      }
    }
  }

  private static Map<String, File> groups = new HashMap<>();
  private static final String DEFAULT_GROUP = "DEFAULT_GROUP";

  private static void makeGroups(String option) {
    File folder;
    groups.put(DEFAULT_GROUP, RunTime.get().fWorkDir);
    if (null != (folder = RunTime.asFolder(option))) {
      groups.put(DEFAULT_GROUP, folder);
      return;
    }
    File folders;
    if (null != (folders = RunTime.asFile(option))) {
      if (FilenameUtils.getExtension(folders.getPath()).isEmpty() ||
              FilenameUtils.getExtension(folders.getPath()).equals("txt")) {
        dolog(3, "option -g txt-file: %s", folders);
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
          if (null != RunTime.asFolder(fldr)) {
            if (isFirst) {
              groups.put(DEFAULT_GROUP, RunTime.asFolder(fldr));
              isFirst = false;
            }
            if (!groups.containsKey(grp)) {
              groups.put(grp, RunTime.asFolder(fldr));
              dolog(3, "group: %s folder: %s", grp, RunTime.asFolder(fldr));
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
    if (null != (allowedIPsFile = RunTime.asFile(option))) {
      if (FilenameUtils.getExtension(allowedIPsFile.getPath()).isEmpty() ||
              FilenameUtils.getExtension(allowedIPsFile.getPath()).equals("txt")) {
        dolog(3, "option -x txt-file: %s", allowedIPsFile);
        String[] items = FileManager.readFileToStringArray(allowedIPsFile);
        for (String item : items) {
          allowedIPs.add(item);
          dolog(3, "allowed: %s", item);
        }
      }
    } else if (null != option) {
      allowedIPs.add(option);
      dolog(3, "allowed: %s", option);
    }
  }

  public static boolean run() {
    // evaluate startup option -s
    evalServerOptions(RunTime.getServerOptions());
    if (null != RunTime.asFile(serverIP)) {
      RunTime.startLog(3, "server (-s): %s is a file", serverIP);
      String[] serverOptions = FileManager.readFileToString(RunTime.asFile(serverIP)).split("\n");
      serverIP = serverIPdefault;
      for (String line : serverOptions) {
        serverIP = serverIPdefault;
        serverPort = serverPortdefault;
        line = line.trim();
        evalServerOptions(new String[]{line});
        serverListenAt.add(Pair.of(serverIP, serverPort));
        RunTime.startLog(3, "server (-s): from file: %s:%d", serverIP, serverPort);
      }
    } else {
      RunTime.startLog(3, "server (-s): %s:%s -> %s:%d", serverOption1, serverOption2, serverIP, serverPort);
    }
    int port = serverPort;
    String theIP = serverIP;

    // evaluate startup option -g
    String groupsOption = RunTime.getServerGroups();
    makeGroups(groupsOption);
    dolog(3, "DEFAULT_GROUP: %s", groups.get(DEFAULT_GROUP));

    // evaluate startup option -x
    String extraOption = RunTime.getServerExtra();
    makeAllowedIPs(extraOption);

    // start the server
    String theServer = String.format("%s %d", theIP, port);
    isRunning = new File(RunTime.get().fSikulixStore, "SikulixServer.txt");
    try {
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
        Runner.getRunners().stream()
            .filter(runner -> !runner.getName().equals(RobotRunner.NAME))  //TODO Delete this line when RobotRunner.init() call failure is resolved
            .forEach(runner -> runner.init(null));
      } catch (Exception ex) {
        dolog(-1, "ScriptRunner init not possible: " + ex.getMessage());
        return false;
      }
      try {
        if (port > 0) {
          dolog(3, "Starting: trying with: %s:%d", theIP, port);
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
    StopCommand stop = new StopCommand();
    ScriptsCommand scripts = new ScriptsCommand();

    RoutingHandler commands = Handlers.routing()
            .addAll(stop.getRouting())
            .addAll(scripts.getRouting())
            .setFallbackHandler(AbstractCommand.getFallbackHandler());
    CommandRootHttpHandler cmdRoot = new CommandRootHttpHandler(commands);
    cmdRoot.addExceptionHandler(Throwable.class, AbstractCommand.getExceptionHttpHandler());

    ResourceManager resourceManager = new ClassPathResourceManager(RunTime.class.getClassLoader(), "htdocs");
    ResourceHandler resource = new ResourceHandler(resourceManager, cmdRoot);
    resource.addWelcomeFiles("ControlBox.html");

    Undertow server = Undertow.builder()
            .addHttpListener(port, ipAddr)
            .setHandler(resource)
            .build();
    return server;
  }

  private static class StopCommand extends AbstractCommand {
    public StopCommand() {
      getRouting()
          .add(Methods.GET, "/stop",
               toRequestLimitingHandler(stop))
          .add(Methods.POST, "/stop",
               toRequestLimitingHandler(stop));
    }

    private HttpHandler stop = exchange -> {
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
    };
  }

  private static class ScriptsCommand extends AbstractCommand {
    private static final Pattern PATTERN_QUERY_ARGS = Pattern.compile("args=(?<args>[^&]+)");

    public ScriptsCommand() {
      getRouting()
          .add(Methods.GET, "/scripts/*/run",
              Predicates.regex(RelativePathAttribute.INSTANCE, "^/scripts/[^/].*/run$"),
              toRequestLimitingHandler(run))
          .add(Methods.POST, "/scripts/*/run",
              Predicates.regex(RelativePathAttribute.INSTANCE, "^/scripts/[^/].*/run$"),
              toRequestLimitingHandler(run));
    }

    private HttpHandler run = exchange -> {
      String script = exchange.getQueryParameters().get("*").getFirst().replaceFirst("/run$", "");
      File fScript = new File(getCurrentGroup(), script);
      int statusCode = StatusCodes.OK;
      String message = null;
      List<String> args = getQueryAndToArgs(exchange);
      int retval = Runner.executeScript(fScript.getPath(), args.toArray(new String[args.size()]));
      switch(retval) {
        case Runner.FILE_NOT_FOUND:
          //TODO handle: the given script file does not exist
          message = "runScript: script not found " + fScript.getAbsolutePath();
          statusCode = StatusCodes.NOT_FOUND;
          break;
        case Runner.NOT_SUPPORTED:
          //TODO handle: there is no runner available for the given script file
          message = "runScript: script not supported " + fScript.getAbsolutePath();
          statusCode = StatusCodes.NOT_FOUND;
          break;
        default:
          //TODO all other retvals are returned by the user script and should be reported.
          if (retval < 0) {
            statusCode = StatusCodes.SERVICE_UNAVAILABLE;
          }
          message = "runScript: returned: " + retval;
          break;
      }
      sendResponse(exchange, statusCode==StatusCodes.OK, statusCode, message);
    };

    private String getCurrentGroup() {
      //TODO evaluate and return the current group's folder
      return groups.get(DEFAULT_GROUP).getAbsolutePath();
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

  private static abstract class AbstractCommand {
    private static RequestLimit sharedRequestLimit = new RequestLimit(1);
    private static HttpHandler fallbackHandler = exchange -> {
      AbstractCommand.sendResponse(exchange, false, StatusCodes.BAD_REQUEST,
      "invalid command: " + exchange.getRelativePath());
    };
    private static HttpHandler exceptionHandler = exchange -> {
      Throwable ex = exchange.getAttachment(ExceptionHandler.THROWABLE);
        SikulixServer.dolog(-1, "while processing: Exception:\n" + ex.getMessage());
        AbstractCommand.sendResponse(exchange, false, StatusCodes.INTERNAL_SERVER_ERROR,
            "server error: " + ex.getMessage());
    };
    private RoutingHandler routing;

    protected AbstractCommand() {
      routing = Handlers.routing();
    }

    protected RoutingHandler getRouting() {
      return routing;
    }

    protected RequestLimitingHandler toRequestLimitingHandler(HttpHandler handler) {
      return new RequestLimitingHandler(sharedRequestLimit, handler);
    }

    protected static HttpHandler getFallbackHandler() {
      return fallbackHandler;
    }

    protected static HttpHandler getExceptionHttpHandler() {
      return exceptionHandler;
    }

    protected static void sendResponse(HttpServerExchange exchange, boolean success, int stateCode, String message) {
      exchange.setStatusCode(stateCode);
      exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
      String head = exchange.getProtocol() + " " + StatusCodes.getReason(exchange.getStatusCode());
      String body = (success ? "PASS " : "FAIL ") + exchange.getStatusCode() + " " + message;
      exchange.getResponseSender().send(body);

      SikulixServer.dolog("returned:\n" + (head + "\n\n" + body));
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