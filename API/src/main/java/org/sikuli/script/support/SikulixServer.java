/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script.support;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.attribute.RelativePathAttribute;
import io.undertow.predicate.Predicates;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.RoutingHandler;
import io.undertow.server.handlers.ExceptionHandler;
import io.undertow.server.handlers.form.EagerFormParsingHandler;
import io.undertow.server.handlers.form.FormData;
import io.undertow.server.handlers.form.FormDataParser;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.server.handlers.resource.ResourceHandler;
import io.undertow.server.handlers.resource.ResourceManager;
import io.undertow.util.AttachmentKey;
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

  private static void dolog(int lvl, String message, Object... args) {
    if (Debug.isBeQuiet()) {
      return;
    }
    if (lvl <= Debug.getDebugLevel()) {
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
          } catch (NumberFormatException ex) {
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
        } catch (NumberFormatException ex) {
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
        if (port > 0) {
          dolog(3, "Starting: trying with: %s:%d", theIP, port);
          server = createServer(port, theIP);
          server.start();
          dolog("Server is ready at %s:%d in %s\n", theIP, port, groups.get(DEFAULT_GROUP));
        }
      } catch (Exception ex) {
        dolog(-1, "Starting: " + ex);
      }
      if (server == null) {
        dolog(-1, "could not be started");
        return false;
      }
      initScriptRunners();
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
      server.stop();
    } catch (Exception ex) {
    }
    if (!isHandling) {
      dolog(-1, "start handling not possible: " + port);
      return false;
    }
    dolog("now stopped on port: " + port);
    return true;
  }

  private static void initScriptRunners() {
    dolog("Start all ScriptRunner initialization, but requests can be accepted");
    ExecutorService executor = Executors.newFixedThreadPool(1, r -> new Thread(r, "ScriptRunner-Initializer"));
    final int allCount = Runner.getRunners().size();
    AtomicInteger index = new AtomicInteger();
    Runner.getRunners().forEach(runner -> executor.submit(() -> {
      runner.init(null);
      dolog("ScriptRunner-%s initialization done [%02d/%02d]", 
            runner.getName(), index.incrementAndGet(), allCount);
    }));
    executor.shutdown();
  }

  private static Undertow createServer(int port, String ipAddr) {
    ControllerCommand controller = new ControllerCommand();
    TasksCommand tasks = new TasksCommand();
    ScriptsCommand scripts = new ScriptsCommand(tasks);
    GroupsCommand groups = new GroupsCommand(scripts);

    ResourceManager resourceManager = new ClassPathResourceManager(RunTime.class.getClassLoader(), "htdocs");
    ResourceHandler resource = new ResourceHandler(resourceManager, AbstractCommand.getFallbackHandler());
    resource.addWelcomeFiles("ControlBox.html");

    RoutingHandler commands = Handlers.routing()
            .addAll(controller.getRouting())
            .addAll(tasks.getRouting())
            .addAll(scripts.getRouting())
            .addAll(groups.getRouting())
            .setFallbackHandler(resource);
    CommandRootHttpHandler cmdRoot = new CommandRootHttpHandler(commands);
    cmdRoot.addExceptionHandler(Throwable.class, AbstractCommand.getExceptionHttpHandler());

    Undertow server = Undertow.builder()
            .addHttpListener(port, ipAddr)
            .setServerOption(UndertowOptions.RECORD_REQUEST_START_TIME, true)
            .setHandler(cmdRoot)
            .build();
    return server;
  }

  private static class ControllerCommand extends AbstractCommand {
    public ControllerCommand() {
      getRouting()
          .add(Methods.GET, "/stop", stop)
          .add(Methods.POST, "/stop", stop)
          .add(Methods.GET, "/pause", pause)
          .add(Methods.POST, "/pause", pause)
          .add(Methods.GET, "/resume", resume)
          .add(Methods.POST, "/resume", resume);
    }

    private HttpHandler stop = exchange -> {
      sendResponse(exchange, StatusCodes.OK, new SimpleResponse("stopping server"));
      synchronized (lock) {
        shouldStop = true;
        lock.notify();
      }
      getTaskManager().stop();
    };

    private HttpHandler pause = exchange -> {
      if (getTaskManager().pause()) {
        sendResponse(exchange, StatusCodes.OK, new SimpleResponse("pause the script execution after the currently running script ends"));
      } else {
        sendResponse(exchange, StatusCodes.ACCEPTED, new SimpleResponse("the script execution is already paused"));
      }
    };

    private HttpHandler resume = exchange -> {
      if (getTaskManager().resume()) {
        sendResponse(exchange, StatusCodes.OK, new SimpleResponse("resumed the script execution"));
      } else {
        sendResponse(exchange, StatusCodes.ACCEPTED, new SimpleResponse("the script execution is already resumed"));
      }
    };
  }

  private static class TasksCommand extends AbstractCommand {
    public TasksCommand() {
      getRouting()
          .add(Methods.GET, "/tasks", getTasks)
          .add(Methods.GET, "/tasks/{id}", getTask)
          .add(Methods.PUT, "/tasks/{id}/cancel", cancelTask);
    }

    private HttpHandler getTasks = exchange -> {
      sendResponse(exchange, StatusCodes.OK, getFilteredTasks(exchange).values());
    };

    private HttpHandler getTask = exchange -> {
      String id = exchange.getQueryParameters().get("id").getLast();
      Task task = getFilteredTasks(exchange).get(id);
      if (task != null) {
        sendResponse(exchange, StatusCodes.OK, task);
      } else {
        sendResponse(exchange, StatusCodes.NOT_FOUND,
            new ErrorResponse(String.format("not found the task: id='%s'", id)));
      }
    };

    private HttpHandler cancelTask = exchange -> {
      int statusCode = StatusCodes.OK;
      Object responseObject = new SimpleResponse("the task has been canceled");

      boolean success = false;
      String id = exchange.getQueryParameters().get("id").getLast();
      Task task = getFilteredTasks(exchange).get(id);
      if (task != null) {
        success = getTaskManager().cancel(id);
      }
      if (!success) {
        responseObject = new ErrorResponse(String.format("no cancelable task found: id='%s'", id));
        statusCode = StatusCodes.NOT_FOUND;
      }
      sendResponse(exchange, statusCode, responseObject);
    };

    private Map<String, Task> getFilteredTasks(final HttpServerExchange exchange) {
      CommandsAttachment attachment = Optional.ofNullable(exchange.getAttachment(KEY)).orElse(new CommandsAttachment());
      return getTaskManager().getTasks(Optional.ofNullable(attachment.get(GroupsCommand.ATTACHMENTKEY_GROUPNAME)), 
                                       Optional.ofNullable(attachment.get(ScriptsCommand.ATTACHMENTKEY_SCRIPTNAME)));
    }
  }

  private static class ScriptsCommand extends AbstractCommand {
    public static final String ATTACHMENTKEY_SCRIPTNAME = "scriptName";
    private static final Pattern PATTERN_QUERY_ARGS = Pattern.compile("args=(?<args>[^&]+)");
    private AtomicInteger taskId;
    private TasksCommand tasks;

    public ScriptsCommand(TasksCommand tasks) {
      this.taskId = new AtomicInteger();
      this.tasks = tasks;
      this.run.addExceptionHandler(Throwable.class, getExceptionHttpHandler());
      getRouting()
          .add(Methods.GET, "/scripts", 
              getScripts)
          .add(Methods.GET, "/scripts/*",
              Predicates.regex(RelativePathAttribute.INSTANCE, "^/scripts/[^/].*/run$"),
              run)
          .add(Methods.POST, "/scripts/*",
              Predicates.regex(RelativePathAttribute.INSTANCE, "^/scripts/[^/].*/run$"),
              run)
          .add(Methods.POST, "/scripts/*",
              Predicates.regex(RelativePathAttribute.INSTANCE, "^/scripts/[^/].*/task$"),
              task)
          .add(Methods.GET, "/scripts/*",
              Predicates.regex(RelativePathAttribute.INSTANCE, "^/scripts/[^/].*/tasks(/.*)*$"),
              delegate)
          .add(Methods.PUT, "/scripts/*",
              Predicates.regex(RelativePathAttribute.INSTANCE, "^/scripts/[^/].*/tasks(/.*)*$"),
              delegate)
          .add(Methods.GET, "/scripts/*", 
              Predicates.regex(RelativePathAttribute.INSTANCE, "^/scripts/([^/].*)?[^/]$"),
              getScript);
    }

    private HttpHandler getScripts = exchange -> {
      String groupName = getCurrentGroup(exchange);
      sendResponse(exchange, StatusCodes.OK, getScriptsList(groups.get(groupName)));
    };

    private HttpHandler getScript = exchange -> {
      String groupName = getCurrentGroup(exchange);
      String scriptName = exchange.getQueryParameters().get("*").getLast();
      Optional<ObjectNode> result = getScriptInfo(groupName, scriptName);
      if (result.isPresent()) {
        sendResponse(exchange, StatusCodes.OK, result.get());
      } else {
        sendResponse(exchange, StatusCodes.NOT_FOUND, new ErrorResponse(String.format("script not found '%s'", scriptName)));
      }
    };

    private ExceptionHandler run = Handlers.exceptionHandler(exchange -> {
      if (exchange.isInIoThread()) {
        // switching to a worker thread
        exchange.dispatch(this.run);
        return;
      }

      int statusCode = StatusCodes.OK;
      Object responseObject = null;

      if (getTaskManager().isPaused()) {
        responseObject = new ErrorResponse(String.format("the script execution is paused"));
        statusCode = StatusCodes.NOT_ACCEPTABLE;
      } else {
        String id = generateTaskId(exchange);
        String groupName = getCurrentGroup(exchange);
        String scriptName = exchange.getQueryParameters().get("*").getLast().replaceFirst("/run$", "");
        String[] scriptArgs = getScriptArgs(exchange);
  
        Task task = getTaskManager().requestSync(id, groupName, scriptName, scriptArgs);
        int retval = task.exitCode;
        switch(retval) {
          case Runner.FILE_NOT_FOUND:
            responseObject = new ErrorResponse(String.format("script not found '%s'", scriptName));
            statusCode = StatusCodes.NOT_FOUND;
            break;
          case Runner.NOT_SUPPORTED:
            responseObject = new ErrorResponse(String.format("script not supported '%s'", scriptName));
            statusCode = StatusCodes.NOT_FOUND;
            break;
          default:
            if (retval < 0 || 255 < retval) {
              responseObject = new ErrorResponse(String.format("script failed exitCode='%d'", retval));
              statusCode = StatusCodes.SERVICE_UNAVAILABLE;
            } else {
              responseObject = task;
            }
            break;
        }  
      }
      sendResponse(exchange, statusCode, responseObject);
    });

    private HttpHandler task = exchange -> {
      String id = generateTaskId(exchange);
      String groupName = getCurrentGroup(exchange);
      String scriptName = exchange.getQueryParameters().get("*").getLast().replaceFirst("/task$", "");
      String[] scriptArgs = getScriptArgs(exchange);

      Task task = getTaskManager().requestAsync(id, groupName, scriptName, scriptArgs);
      sendResponse(exchange, StatusCodes.OK, task);
    };

    private HttpHandler delegate = exchange -> {
      String param = exchange.getQueryParameters().get("*").getLast();
      String newRelativePath = param.substring(param.lastIndexOf("/tasks"));
      exchange.setRelativePath(newRelativePath);
      String scriptName = param.substring(0, param.lastIndexOf("/tasks"));
      CommandsAttachment attachment = Optional.ofNullable(exchange.getAttachment(KEY)).orElse(new CommandsAttachment());
      attachment.put(ATTACHMENTKEY_SCRIPTNAME, scriptName);
      exchange.putAttachment(KEY, attachment);
      tasks.getRouting().handleRequest(exchange); 
    };

    private List<ObjectNode> getScriptsList(File base) {
      ObjectMapper mapper = getObjectMapper();
      List<ObjectNode> result = new ArrayList<>();
      for (File entry : base.listFiles()) {
        if (entry.isDirectory()) {
          File script = Runner.getScriptFile(entry);
          if (script != null) {
            result.add(mapper.createObjectNode().put("name", entry.getName().replaceFirst("\\.sikuli$", ""))
                                                .put("type", Runner.getRunner(script.getAbsolutePath()).getType()));
          } else {
            List<ObjectNode> sub = getScriptsList(entry);
            sub.forEach(s -> {
              result.add(mapper.createObjectNode().put("name", entry.getName() + "/" + s.get("name").asText())
                                                  .put("type", s.get("type").asText()));
            });
          }
        }
      }
      return result;
    }

    private Optional<ObjectNode> getScriptInfo(String groupName, String scriptName) {
      RunTime.get().fWorkDir = groups.get(groupName);
      String[] scripts = RunTime.resolveRelativeFiles(new String[]{scriptName});
      if (!scripts[0].startsWith("?")) {
        ObjectNode result = getObjectMapper().createObjectNode();
        result.put("name", scriptName)
              .put("group", groupName)
              .put("type", Runner.getRunner(scripts[0]).getType())
              .put("file", scripts[0]);
        return Optional.of(result);
      } else {
        return Optional.empty();
      }
    }

    private String generateTaskId(final HttpServerExchange exchange) {
      return String.valueOf(taskId.incrementAndGet());
    }

    private String getCurrentGroup(final HttpServerExchange exchange) {
      CommandsAttachment attachment = Optional.ofNullable(exchange.getAttachment(KEY)).orElse(new CommandsAttachment());
      return Optional.ofNullable(attachment.get(GroupsCommand.ATTACHMENTKEY_GROUPNAME)).orElse(DEFAULT_GROUP);
    }

    private String[] getScriptArgs(final HttpServerExchange exchange) {
      String[] args = {};
      Optional<String> argsString = Optional.empty();
      String queryString = exchange.getQueryString();
      if (queryString != null) {
        Matcher matcher = PATTERN_QUERY_ARGS.matcher(queryString);
        if (matcher.find()) {
          argsString = Optional.of(matcher.group("args"));
        }
      }
      if (exchange.getRequestMethod().equals(Methods.POST)) {
        FormData form = exchange.getAttachment(FormDataParser.FORM_DATA);
        if (form != null) {
          argsString = Optional.ofNullable(form.getLast("args")).map(fVal -> fVal.getValue());
        }
      }
      if (argsString.isPresent()) {
        StringBuilder buf = new StringBuilder();
        String[] tokens = argsString.get().split(";");
        args = new String[tokens.length];
        for (int i=0; i<tokens.length; i++) {
          args[i] = URLUtils.decode(tokens[i], "UTF-8", true, buf);
        }
      }
      return args;
    }
  }

  private static class GroupsCommand extends AbstractCommand {
    public static final String ATTACHMENTKEY_GROUPNAME = "groupName";
    private ScriptsCommand scripts;

    public GroupsCommand(ScriptsCommand scripts) {
      this.scripts = scripts;
      getRouting()
          .add(Methods.GET, "/groups", getGroups)
          .add(Methods.GET, "/groups/{name}", getSubTree)
          .add(Methods.GET, "/groups/{name}/*", delegate)
          .add(Methods.POST, "/groups/{name}/*", delegate)
          .add(Methods.PUT, "/groups/{name}/*", delegate);
    }

    private HttpHandler getGroups = exchange -> {
      List<ObjectNode> result = groups.entrySet().stream()
          .map(e -> getObjectMapper().createObjectNode().put("name", e.getKey()).put("folder", e.getValue().getAbsolutePath()))
          .collect(Collectors.toList());
      sendResponse(exchange, StatusCodes.OK, result);
    };

    private HttpHandler getSubTree = exchange -> {
      String groupName = exchange.getQueryParameters().get("name").getLast();
      if (groups.containsKey(groupName)) {
        List<ObjectNode> result = getFoldersAndScripts(groups.get(groupName));
        sendResponse(exchange, StatusCodes.OK, result);
      } else {
        sendResponse(exchange, StatusCodes.NOT_FOUND, new ErrorResponse("group not found: " + groupName));
      }
    };

    private HttpHandler delegate = exchange -> {
      String newRelativePath = "/" + exchange.getQueryParameters().get("*").getLast();
      exchange.setRelativePath(newRelativePath);
      String groupName = exchange.getQueryParameters().get("name").getLast();
      if (groups.containsKey(groupName)) {
        CommandsAttachment attachment = Optional.ofNullable(exchange.getAttachment(KEY)).orElse(new CommandsAttachment());
        attachment.put(ATTACHMENTKEY_GROUPNAME, groupName);
        exchange.putAttachment(KEY, attachment);
        scripts.getRouting().handleRequest(exchange); 
      } else {
        sendResponse(exchange, StatusCodes.NOT_FOUND, new ErrorResponse("group not found: " + groupName));
      }
    };

    private List<ObjectNode> getFoldersAndScripts(File base) {
      ObjectMapper mapper = getObjectMapper();
      List<ObjectNode> result = new ArrayList<>();
      for (File entry : base.listFiles()) {
        if (entry.isDirectory()) {
          File script = Runner.getScriptFile(entry);
          if (script != null) {
            result.add(mapper.createObjectNode().put("folder", entry.getName()).put("innerScript", script.getName()));
          } else {
            List<ObjectNode> sub = getFoldersAndScripts(entry);
            if (!sub.isEmpty()) {
              result.add((ObjectNode)mapper.createObjectNode().put("folder", entry.getName()).set("children", mapper.valueToTree(sub)));
            }
          }
        }
      }
      return result;
    }
  }

  private static abstract class AbstractCommand {
    public static final AttachmentKey<CommandsAttachment> KEY = AttachmentKey.create(CommandsAttachment.class);

    private static TaskManager taskManager;
    private static ObjectMapper mapper;
    static {
      taskManager = new TaskManager();
      mapper = new ObjectMapper();
      mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"));
    }

    private static HttpHandler fallbackHandler = exchange -> {
      AbstractCommand.sendResponse(exchange, StatusCodes.BAD_REQUEST,
          new ErrorResponse(String.format("invalid command '%s'", exchange.getRequestPath())));
    };
    private static HttpHandler exceptionHandler = exchange -> {
      Throwable ex = exchange.getAttachment(ExceptionHandler.THROWABLE);
      dolog(-1, "while processing: Exception:\n" + ex);
      ex.printStackTrace();
      AbstractCommand.sendResponse(exchange, StatusCodes.INTERNAL_SERVER_ERROR,
          new ErrorResponse(String.format("server error '%s'", ex)));
    };

    private RoutingHandler routing;

    protected AbstractCommand() {
      routing = Handlers.routing().setFallbackHandler(fallbackHandler);
    }

    protected RoutingHandler getRouting() {
      return routing;
    }

    protected static TaskManager getTaskManager() {
      return taskManager;
    }

    protected static ObjectMapper getObjectMapper() {
      return mapper;
    }

    protected static HttpHandler getFallbackHandler() {
      return fallbackHandler;
    }

    protected static HttpHandler getExceptionHttpHandler() {
      return exceptionHandler;
    }

    protected static void sendResponse(HttpServerExchange exchange, int stateCode, Object responseObject) {
      exchange.setStatusCode(stateCode);
      exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
      String head = exchange.getProtocol() + " " + exchange.getStatusCode() + " " + StatusCodes.getReason(exchange.getStatusCode());
      try {
        String body = mapper.writeValueAsString(responseObject);
        exchange.getResponseSender().send(body);
        dolog("returned for <%s %s %s> from %s:\n%s\n%s",
              exchange.getRequestMethod(), exchange.getRequestURI(), exchange.getProtocol(), exchange.getSourceAddress(),
              head, body);
      } catch (JsonProcessingException ex) {
        dolog(-1, "serialize to json: Exception:\n" + ex);
        ex.printStackTrace();
      }
    }

    protected static class CommandsAttachment extends HashMap<String, String> {
      private static final long serialVersionUID = 2103091341469112744L;
    }
  }

  private static class TaskManager {
    private LinkedHashMap<String, Task> allTasks;
    private LinkedBlockingDeque<Task> queue;
    private boolean shouldStop;
    private boolean shouldPause;
    private Object lock;
    private ExecutorService executor;

    public TaskManager() {
      allTasks = new LinkedHashMap<>();
      queue = new LinkedBlockingDeque<>();
      shouldStop = false;
      shouldPause = false;
      executor = Executors.newSingleThreadExecutor(r -> new Thread(r, "Task Executor"));
      lock = new Object();
      executor.execute(() -> {
        while (!shouldStop) {
          Task task = null;
          try {
            synchronized(lock) {
              while (shouldPause) {
                lock.wait();
              }
            }
            task = queue.take();
            if ("shouldStop".equals(task.id) || "shouldPause".equals(task.id)) {
              // NOOP
            } else {
              synchronized(task) {
                if (task.isWaiting()) {
                  task.updateStatus(Task.Status.RUNNING);
                }
              }
              if (task.isRunning()) {
                  task.runScript();
              }
            }
          } catch (InterruptedException ex) {
            // NOOP
          } catch (Exception ex) {
            SikulixServer.dolog(-1, "ScriptExecutor: Exception: %s", ex);
            ex.printStackTrace();
            if (task != null) {
              task.updateStatus(Task.Status.FAILED);
            }
          } finally {
            if (task != null) {
              synchronized(task) {
                task.notify();
              }
            }
          }
        }
      });
    }

    public Map<String, Task> getTasks(Optional<String> groupName, Optional<String> scriptName) {
      LinkedHashMap<String, Task> result = allTasks.entrySet().stream()
          .filter(e -> {
            if (groupName.isPresent()) {
              if (scriptName.isPresent()) {
                return groupName.get().equals(e.getValue().groupName) && scriptName.get().equals(e.getValue().scriptName);
              } else {
                return groupName.get().equals(e.getValue().groupName);
              }
            } else {
              if (scriptName.isPresent()) {
                return DEFAULT_GROUP.equals(e.getValue().groupName) && scriptName.get().equals(e.getValue().scriptName);
              } else {
                return true;
              }
            }
          })
          .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (oldValue, newValue) -> newValue, LinkedHashMap::new));
      return Collections.unmodifiableMap(result);
    }

    public Task requestSync(final String id, final String groupName, final String scriptName, final String[] scriptArgs) throws Exception {
      return request(id, groupName, scriptName, scriptArgs, false);
    }

    public Task requestAsync(final String id, final String groupName, final String scriptName, final String[] scriptArgs) throws Exception {
      return request(id, groupName, scriptName, scriptArgs, true);
    }

    private Task request(final String id, final String groupName, final String scriptName, 
        final String[] scriptArgs, boolean isAsync) throws Exception {
      Task request = new Task(id, groupName, scriptName, scriptArgs, isAsync);
      synchronized(allTasks) {
        allTasks.put(request.id, request);
        queue.put(request);
      }
      if (!isAsync) {
        synchronized(request) {
          while(request.isWaiting() || request.isRunning()) {
            request.wait();
          }
        }
      }
      return request.clone();
    }

    public boolean cancel(final String id) {
      Task task = allTasks.get(id);
      if (task != null) {
        synchronized (task) {
          if (task.isWaiting()) {
            task.updateStatus(Task.Status.CANCELED);
            task.notify();
            return true;
          } else {
            SikulixServer.dolog(-1, "could not cancel the task: %s", id);
            return false;
          }
        }
      } else {
        SikulixServer.dolog(-1, "the task is not found: %s", id);
        return false;
      }
    }

    public void stop() {
      shouldStop = true;
      queue.addFirst(new Task("shouldStop", null, null, null, true));
      executor.shutdown();
      while(!executor.isTerminated()) {
        try {
          executor.awaitTermination(60, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
          ex.printStackTrace();
        }
      }
    }

    public boolean pause() {
      synchronized(lock) {
        if (shouldPause) {
          return false;
        } else {
          shouldPause = true;
          queue.addFirst(new Task("shouldPause", null, null, null, true));
          return true;
        }
      }
    }

    public boolean resume() {
      synchronized(lock) {
        if (shouldPause) {
          shouldPause = false;
          lock.notify();
          return true;
        } else {
          return false;
        }
      }
    }

    public boolean isPaused() {
      return shouldPause;
    }
  }

  private static class Task implements Cloneable {
    public final String id;
    public final String groupName;
    public final String scriptName;
    public final String[] scriptArgs;
    @SuppressWarnings("unused")
    public final boolean isAsync;
    public Status status;
    public Date startDate;
    public Date endDate;
    public int exitCode;

    private Task(final String id, final String groupName, 
                 final String scriptName, final String[] scriptArgs, final boolean isAsync) {
      this.id = id;
      this.groupName = groupName;
      this.scriptName = scriptName;
      this.scriptArgs = scriptArgs;
      this.isAsync = isAsync;
      this.status = Status.WAITING;
    }

    @JsonIgnore
    public boolean isWaiting() {
      return status == Status.WAITING;
    }

    @JsonIgnore
    public boolean isRunning() {
      return status == Status.RUNNING;
    }

    public void updateStatus(Status status) {
      this.status = status;
    }

    public void runScript() {
      RunTime.get().fWorkDir = groups.get(groupName);
      String[] scripts = RunTime.resolveRelativeFiles(new String[]{scriptName});
      RunTime.setUserArgs(scriptArgs);
      startDate = new Date();
      exitCode = Runner.runScripts(scripts, scriptArgs, new IScriptRunner.Options());
      endDate = new Date();
      if (exitCode < 0 || 255 < exitCode) {
        status = Status.FAILED;
      } else {
        status = Status.FINISHED;
      }
    }

    @Override
    public Task clone() {
      Task clone = null;
      try {
        clone = (Task) super.clone();
        if (startDate != null) {
          clone.startDate = (Date) startDate.clone();
        }
        if (endDate != null) {
          clone.endDate = (Date) endDate.clone();
        }
      } catch (CloneNotSupportedException ex) {
        SikulixServer.dolog(-1, "Task#clone() error: %s", ex);
        ex.printStackTrace();
      }
      return clone;
    }

    private static enum Status{
      WAITING,
      RUNNING,
      FINISHED,
      CANCELED,
      FAILED
    }
  }

  private static class SimpleResponse {
    @SuppressWarnings("unused")
    public String message;
    
    public SimpleResponse(String message) {
      this.message = message;
    }
  }

  private static class ErrorResponse {
    @SuppressWarnings("unused")
    public String error;
    
    public ErrorResponse(String error) {
      this.error = error;
    }
  }

  private static class CommandRootHttpHandler extends ExceptionHandler {
    public CommandRootHttpHandler(HttpHandler handler) {
      super(new EagerFormParsingHandler(handler));
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
      isHandling = true;
      dolog("received request: <%s %s %s> from %s",
            exchange.getRequestMethod(), exchange.getRequestURI(), exchange.getProtocol(),
            exchange.getSourceAddress());
      super.handleRequest(exchange);
    }
  }
}