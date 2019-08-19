/*
 * Copyright (c) 2010-2019, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script.runners;

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
import org.sikuli.script.ImagePath;
import org.sikuli.script.support.RunTime;
import org.sikuli.script.support.Runner;

/**
 * EXPERIMENTAL --- NOT official API<br>
 *   not as is in version 2
 */
public class ServerRunner extends AbstractScriptRunner {

  public static final String NAME = "Server";
  public static final String TYPE = "text/server";
  public static final String[] EXTENSIONS = new String[0];

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public String[] getExtensions() {
    return EXTENSIONS;
  }

  @Override
  public String getType() {
    return TYPE;
  }

  private static ServerSocket server = null;
  private static PrintWriter out = null;
  private static Scanner in = null;
  private static boolean isHandling = false;
  private static boolean shouldStop = false;

//TODO set loglevel at runtime
  private static int logLevel = 0;
  private static void dolog(int lvl, String message, Object... args) {
    if (Debug.isBeQuiet()) {
      return;
    }
    if (lvl < 0 || lvl >= logLevel) {
      System.out.println((lvl < 0 ? "[error] " : "[info] ") +
              String.format("RunServer: " + message, args));
    }
  }

  private static void dolog(String message, Object... args) {
    dolog(0, message, args);
  }
