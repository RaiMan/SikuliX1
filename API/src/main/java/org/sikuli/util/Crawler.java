package org.sikuli.util;

import java.lang.reflect.Method;

public class Crawler {

  private static void p(String message, Object... args) {
    if (message.isEmpty()) message = "%s";
    System.out.println(String.format(message, args));
  }

  public static void main(String[] args) {
    if (args.length == 0) return;
    String className = args[0];
    try {
      Class<?> aClass = Class.forName(className);
      p("Class: %s", aClass);
      Method[] declaredMethods = aClass.getDeclaredMethods();
      for (Method m : declaredMethods) {
        String signature = m.toGenericString();
        if (!signature.startsWith("public")) {
          continue;
        }
        signature = signature.replace("public ", "").trim();
        signature = signature.replace("java.lang.", "");
        signature = signature.replace("java.util.", "");
        signature = signature.replace("org.sikuli.script.", "");
        p("", signature);
      }
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
  }
}
