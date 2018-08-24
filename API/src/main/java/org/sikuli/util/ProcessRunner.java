/*
 * Copyright (c) 2010-2018, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.util;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProcessRunner {

  private static void p(String form, Object... args) {
    System.out.println(String.format(form, args));
  }

  public static String run(String... args) throws IOException, InterruptedException {
    String result = "";
    String work = null;
    if (args.length > 0) {
      ProcessBuilder app = new ProcessBuilder();
      List<String> cmd = new ArrayList<String>();
      Map<String, String> processEnv = app.environment();
      for (String arg : args) {
        if (arg.startsWith("work=")) {
          work = arg.substring(5);
          continue;
        }
        if (arg.startsWith("javahome=")) {
          processEnv.put("JAVA_HOME", arg.substring(9));
          continue;
        }
        if (arg.startsWith("?")) {
          final String fName = arg.substring(1);
          String folder = null;
          if (null == work) {
            folder = System.getProperty("user.dir");
          } else {
            folder = work;
          }
          String[] fList = new File(folder).list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
              if (name.startsWith(fName)) return true;
              return false;
            }
          });
          if (fList.length > 0) {
            arg = fList[0];
          }
        }
        cmd.add(arg);
      }
      app.directory(work == null ? null : new File(work));
      app.redirectErrorStream(true);
      app.command(cmd);
      Process process = app.start();
      InputStreamReader reader = new InputStreamReader(process.getInputStream());
      BufferedReader processOut = new BufferedReader(reader);
      String line = processOut.readLine();
      while (null != line) {
        result += line + "\n";
        line = processOut.readLine();
      }
      process.waitFor();
      int exitValue = process.exitValue();
      if (exitValue > 0) {
        result += "error";
      } else {
        result = "success\n" + result;
      }
    }
    return result;
  }

  public static void detach(String... args) {
    List<String> cmd = new ArrayList<String>();
    for (String arg : args) {
      cmd.add(arg);
    }
    detach(cmd);
  }

  public static void detach(List<String> cmd) {
    if (cmd.size() > 0) {
      ProcessBuilder app = new ProcessBuilder();
      Map<String, String> processEnv = app.environment();
      app.command(cmd);
      app.redirectErrorStream(true);
      app.redirectInput(ProcessBuilder.Redirect.INHERIT);
      app.redirectOutput(ProcessBuilder.Redirect.INHERIT);
      Process process = null;
      try {
        process = app.start();
      } catch (Exception e) {
        p("[Error] ProcessRunner: start: %s", e.getMessage());
      }
//      try {
//        if (process != null) {
//          InputStreamReader reader = new InputStreamReader(process.getInputStream());
//          BufferedReader processOut = new BufferedReader(reader);
//          String line = processOut.readLine();
//          while (null != line) {
//            System.out.println(line);
//            line = processOut.readLine();
//          }
//        }
//      } catch (IOException e) {
//        p("[Error] ProcessRunner: read: %s", e.getMessage());
//      }
      try {
        if (process != null) {
          process.waitFor();
        }
      } catch (InterruptedException e) {
        p("[Error] ProcessRunner: waitFor: %s", e.getMessage());
      }
    }
  }
}

