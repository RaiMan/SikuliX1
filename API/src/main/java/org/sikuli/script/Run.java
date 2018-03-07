/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 * EXPERIMENTAL --- NOT official API<br>
 *   not in version 2
 */
public class Run {

  private static Scanner in = null;
  private static PrintWriter out = null;
  private static Socket socket = null;
  private static boolean socketValid = false;
  private static String ip = null;
  private static int port = -1;
  private static boolean keepAlive = false;
  private static boolean disconnecting = false;

  private static void log(String message, Object... args) {
    System.out.println(String.format("[debug] Run: " + message, args));
  }

  private static void error(String message, Object... args) {
    System.out.println(String.format("[error] Run: " + message, args));
  }

  private Run() {
  }

  public static void main(String[] args) {
    String adr = "";
    String p = "-1";
    if (!init(adr, p)) {
      error("not possible");
    }
  }

  public static boolean connect() {
    keepAlive = true;
    return init();
  }

  private static Boolean init() {
    if (socketValid) {
      if (!close()) {
        return false;
      }
    }
    return init(ip, "" + port);
  }

  private static Boolean init(String adr, String p) {
    socketValid = true;
    ip = getAddress(adr);
    port = getPort(p);
    if (ip == null || port < 0) {
      error("target not valid: " + adr + " / " + p);
      System.exit(1);
    }
    try {
      socket = new Socket(ip, port);
    } catch (Exception ex) {
      error("no connection: " + adr + " / " + p);
      socketValid = false;
    }
    try {
      if (socketValid) {
        out = new PrintWriter(socket.getOutputStream());
        in = new Scanner(socket.getInputStream());
        log("connection at: " + socket);
      }
    } catch (Exception ex) {
      error("problem starting connection:\n", ex.getMessage());
      socketValid = false;
    }
    return socketValid;
  }

  public static String getAddress(String adr) {
    try {
      if (adr == null || adr.isEmpty()) {
        return InetAddress.getLocalHost().getHostAddress();
      }
      return InetAddress.getByName(adr).getHostAddress();
    } catch (UnknownHostException ex) {
      return null;
    }
  }

  public static int getPort(String p) {
    int port;
    int pDefault = 50001;
    if (p == null || p.isEmpty()) {
       return pDefault;
   } else {
      try {
        port = Integer.parseInt(p);
      } catch (NumberFormatException ex) {
        return pDefault;
      }
    }
    if (port < 0) {
      port = pDefault;
    }
    if (port < 1024) {
      port += pDefault;
    }
    return port;
  }

  public static boolean isValid() {
    return (socketValid && socket != null);
  }

  public static String send(String command) {
    if (keepAlive) {
      command = "X" + command;
    } else {
      init();
    }
    if (!isValid()) {
      error("connection not valid - send not possible");
      return null;
    }
    String res;
    try {
      out.println(command);
      out.flush();
      log("send: " + command);
      res = in.nextLine();
      while (in.hasNextLine()) {
        String line = in.nextLine();
        if (line.contains("###+++###")) {
          break;
        }
        res += "\n" + line;
      }
    } catch (Exception ex) {
      error("error while processing:\n" + ex.getMessage());
      res = "fail: reason unknown";
    }
    if (!keepAlive && !disconnecting) {
      close();
    }
    return res;
  }

  public static boolean close() {
    return close(false);
  }

  public static boolean stop() {
    return close(true);
  }

  private static boolean close(boolean stopServer) {
    disconnecting = true;
    if (stopServer) {
      send("STOP");
    } else if (keepAlive) {
      send("EXIT");
    }
    if (socket != null) {
      try {
        in.close();
        out.close();
        socket.close();
      } catch (IOException ex) {
        log("error on close: %s\n" + ex.getMessage(), socket);
        socket = null;
      }
    }
    in = null;
    out = null;
    socketValid = false;
    if (socket == null) {
      return false;
    }
    socket = null;
    keepAlive = false;
    return true;
  }

  public static String show() {
    return String.format("%s:%d %s", ip, port, isValid());
  }
}
