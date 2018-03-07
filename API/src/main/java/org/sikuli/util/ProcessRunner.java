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
      process.waitFor();
      String line = processOut.readLine();
      while (null != line) {
        result += line + "\n";
        line = processOut.readLine();
      }
      int exitValue = process.exitValue();
      if (exitValue > 0) {
        result += "error";
      } else {
        result = "success\n" + result;
      }
    }
    return result;
  }
}
