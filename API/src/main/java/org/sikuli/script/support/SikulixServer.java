/*
 * Copyright (c) 2010-2018, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script.support;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
import io.undertow.server.ServerConnection;
import io.undertow.server.ServerConnection.CloseListener;
import io.undertow.server.handlers.ExceptionHandler;
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

  //TODO should be RunTime.getDebugLevelStart()
  private static int logLevel = 3;

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
        Runner.getRunners().stream().forEach(runner -> runner.init(null));
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
    TasksCommand tasks = new TasksCommand();
    ScriptsCommand scripts = new ScriptsCommand(tasks);
    GroupsCommand groups = new GroupsCommand(scripts);

    ResourceManager resourceManager = new ClassPathResourceManager(RunTime.class.getClassLoader(), "htdocs");
    ResourceHandler resource = new ResourceHandler(resourceManager, AbstractCommand.getFallbackHandler());
    resource.addWelcomeFiles("ControlBox.html");

    RoutingHandler commands = Handlers.routing()
            .addAll(stop.getRouting())
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

  private static class StopCommand extends AbstractCommand {
    public StopCommand() {
      getRouting()
          .add(Methods.GET, "/stop", stop)
          .add(Methods.POST, "/stop", stop);
    }

    private HttpHandler stop = exchange -> {
      sendResponse(exchange, StatusCodes.OK, new SimpleResponse("stopping server"));
      getTaskManager().stop();
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

  private static class TasksCommand extends AbstractCommand {
    public TasksCommand() {
      getRouting()
          .add(Methods.GET, "/tasks", getTasks)
          .add(Methods.GET, "/tasks/{id}", getTask)
          .add(Methods.PUT, "/tasks/{id}/cancel", cancelTask);
    }

    private HttpHandler getTasks = exchange -> {
      sendResponse(exchange, StatusCodes.OK, getTaskManager().getAllTasks());
    };

    private HttpHandler getTask = exchange -> {
      String id = exchange.getQueryParameters().get("id").getLast();
      Optional<Task> result = getTaskManager().getTask(id);
      if(result.isPresent()) {
        result.ifPresent(task -> sendResponse(exchange, StatusCodes.OK, task));
      } else {
        sendResponse(exchange, StatusCodes.NOT_FOUND,
            new ErrorResponse(String.format("not found the task: id='%s'", id)));
      }
    };

    private HttpHandler cancelTask = exchange -> {
      int statusCode = StatusCodes.OK;
      Object responseObject = new SimpleResponse("the task has been canceled");

      String id = exchange.getQueryParameters().get("id").getLast();
      boolean success = getTaskManager().cancel(id);
      if (!success) {
        responseObject = new ErrorResponse(String.format("no cancelable task found: id='%s'", id));
        statusCode = StatusCodes.NOT_FOUND;
      }
      sendResponse(exchange, statusCode, responseObject);
    };
  }

  private static class ScriptsCommand extends AbstractCommand {
    private static final Pattern PATTERN_QUERY_ARGS = Pattern.compile("args=(?<args>[^&]+)");
    private TasksCommand tasks;

    public ScriptsCommand(TasksCommand tasks) {
      this.tasks = tasks;
      getRouting()
          .add(Methods.GET, "/scripts/*",
              Predicates.regex(RelativePathAttribute.INSTANCE, "^/scripts/[^/].*/run$"),
              run)
          .add(Methods.POST, "/scripts/*",
              Predicates.regex(RelativePathAttribute.INSTANCE, "^/scripts/[^/].*/run$"),
              run)
          .add(Methods.POST, "/scripts/*",
              Predicates.regex(RelativePathAttribute.INSTANCE, "^/scripts/[^/].*/task$"),
              task);
    }

    private HttpHandler run = exchange -> {
      exchange.dispatch(() -> {
        int statusCode = StatusCodes.OK;
        Object responseObject = null;

        String id = generateTaskId(exchange);
        String groupName = getCurrentGroup(exchange);
        String scriptName = exchange.getQueryParameters().get("*").getLast().replaceFirst("/run$", "");
        String[] scriptArgs = getQueryAndToArgs(exchange);

        Task task = null;
        try {
          task = getTaskManager().requestSync(id, groupName, scriptName, scriptArgs);
        } catch(Exception ex) {
          responseObject = new ErrorResponse(String.format("exception occurred '%s'", ex.getMessage()));
          statusCode = StatusCodes.SERVICE_UNAVAILABLE;
        }
        if (task != null) {
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
    };

    private HttpHandler task = exchange -> {
      int statusCode = StatusCodes.OK;
      Object responseObject = null;

      String id = generateTaskId(exchange);
      String groupName = getCurrentGroup(exchange);
      String scriptName = exchange.getQueryParameters().get("*").getLast().replaceFirst("/task$", "");
      String[] scriptArgs = getQueryAndToArgs(exchange);

      Task task = null;
      try {
        task = getTaskManager().requestAsync(id, groupName, scriptName, scriptArgs);
      } catch(Exception ex) {
        responseObject = new ErrorResponse(String.format("runScript: exception occurred '%s'", ex.getMessage()));
        statusCode = StatusCodes.SERVICE_UNAVAILABLE;
      }
      if (task != null) {
        responseObject = task;
      }
      sendResponse(exchange, statusCode, responseObject);
    };

    private String generateTaskId(final HttpServerExchange exchange) {
      return String.format("%03d-%x", exchange.getIoThread().getNumber(), exchange.getRequestStartTime());
    }

    private String getCurrentGroup(final HttpServerExchange exchange) {
      CommandsAttachment attachment = Optional.ofNullable(exchange.getAttachment(KEY)).orElse(new CommandsAttachment());
      return Optional.ofNullable(attachment.get(GroupsCommand.ATTACHMENTKEY_GROUPNAME)).orElse(DEFAULT_GROUP);
    }

    private String[] getQueryAndToArgs(final HttpServerExchange exchange) {
      String[] args = {};
      String queryString = exchange.getQueryString();
      if (queryString != null) {
        Matcher matcher = PATTERN_QUERY_ARGS.matcher(queryString);
        if (matcher.find()) {
          StringBuilder buf = new StringBuilder();
          String[] tokens = matcher.group("args").split(";");
          args = new String[tokens.length];
          for (int i=0; i<tokens.length; i++) {
            args[i] = URLUtils.decode(tokens[i], "UTF-8", true, buf);
          }
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
          .add(Methods.POST, "/groups/{name}/*", delegate);
    }

    private HttpHandler getGroups = exchange -> {
      //TODO implement : Returns a list of available groups.
      sendResponse(exchange, StatusCodes.OK, "a list of available groups");
    };

    private HttpHandler getSubTree = exchange -> {
      //TODO implement : Returns the subtree (folders and contained scripts) in the group.
      sendResponse(exchange, StatusCodes.OK, "the subtree in the group");
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
        sendResponse(exchange, StatusCodes.NOT_FOUND, new ErrorResponse("group not found : " + groupName));
      }
    };
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
      SikulixServer.dolog(-1, "while processing: Exception:\n" + ex.getMessage());
      AbstractCommand.sendResponse(exchange, StatusCodes.INTERNAL_SERVER_ERROR,
          new ErrorResponse(String.format("server error '%s'", ex.getMessage())));
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

    protected static HttpHandler getFallbackHandler() {
      return fallbackHandler;
    }

    protected static HttpHandler getExceptionHttpHandler() {
      return exceptionHandler;
    }

    protected static void sendResponse(HttpServerExchange exchange, int stateCode, Object responseObject) {
      exchange.setStatusCode(stateCode);
      exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
      String head = exchange.getProtocol() + " " + StatusCodes.getReason(exchange.getStatusCode());
      try {
        String body = mapper.writeValueAsString(responseObject);
        exchange.getResponseSender().send(body);
        SikulixServer.dolog("returned:\n" + head + "\n" + body);
      } catch (JsonProcessingException e) {
        dolog(-1, "serialize to json: Exception:\n" + e.getMessage());
        e.printStackTrace();
      }
    }

    protected static class CommandsAttachment extends HashMap<String, String> {
      private static final long serialVersionUID = 2103091341469112744L;
    }
  }

  private static class TaskManager {
    private LinkedHashMap<String, Task> allTasks;
    private BlockingQueue<Task> queue;
    private boolean shouldStop;
    private ExecutorService executor;

    public TaskManager() {
      allTasks = new LinkedHashMap<>();
      queue = new LinkedBlockingQueue<Task>();
      shouldStop = false;
      executor = Executors.newSingleThreadExecutor();
      executor.execute(() -> {
        while (!shouldStop) {
          Task task = null;
          try {
            task = queue.take();
            synchronized(task) {
              if (task.isWaiting()) {
                task.updateStatus(Task.Status.RUNNING);
              }
            }
            if (task.isRunning()) {
              task.runScript();
            }
          } catch (InterruptedException e) {
            // NOOP
          } catch (Exception e) {
            dolog(-1, "ScriptExecutor: Exception: %s", e.getMessage());
            e.printStackTrace();
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

    public Collection<Task> getAllTasks() {
      return Collections.unmodifiableCollection(allTasks.values());
    }

    public Optional<Task> getTask(final String id) {
      Task task = allTasks.get(id);
      return Optional.ofNullable(task != null ? task.clone() : null);
    }

    public Task requestSync(final String id, final String groupName, final String scriptName, final String[] scriptArgs) throws Exception {
      return request(id, groupName, scriptName, scriptArgs, false);
    }

    public Task requestAsync(final String id, final String groupName, final String scriptName, final String[] scriptArgs) throws Exception {
      return request(id, groupName, scriptName, scriptArgs, true);
    }

    private Task request(final String id, final String groupName, final String scriptName, 
        final String[] scriptArgs, boolean isAsync) throws Exception {
      Task request = new Task(id, groupName, scriptName, scriptArgs);
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
            dolog(-1, "could not cancel the task : %s", id);
            return false;
          }
        }
      } else {
        dolog(-1, "the task is not found : %s", id);
        return false;
      }
    }

    public void stop() {
      shouldStop = true;
    }
  }

  private static class Task implements Cloneable {
    public final String id;
    public final String groupName;
    public final String scriptName;
    public final String[] scriptArgs;
    public Status status;
    public Date startDate;
    public Date endDate;
    public int exitCode;

    private Task(final String id, final String groupName, final String scriptName, final String[] scriptArgs) {
      this.id = id;
      this.groupName = groupName;
      this.scriptName = scriptName;
      this.scriptArgs = scriptArgs;
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
      } catch (CloneNotSupportedException e) {
        dolog(-1, "Task#clone() error: %s", e.getMessage());
        e.printStackTrace();
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