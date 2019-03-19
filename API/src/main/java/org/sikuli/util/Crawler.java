/*
 * Copyright (c) 2010-2018, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class Crawler {

  private static void p(String message, Object... args) {
    if (message.isEmpty()) message = "%s";
    System.out.println(String.format(message, args));
  }

  public static void main(String[] args) {
    if (args.length == 0) return;
    Map<String, List<String>> functions;
    String[] strings;
    String packg = "org.sikuli.";
    String thisPackg = packg;
    for (String arg : args) {
      if (arg.startsWith("#")) {
        break;
      }
      if (arg.endsWith(".")) {
        thisPackg = packg + arg;
        continue;
      }
      String className = thisPackg + arg;
      functions = new HashMap<>();
      String orgSignature = "";
      String signature = "";
      try {
        Class<?> aClass = Class.forName(className);
        String cName = aClass.getSimpleName();
        String superClass = aClass.getSuperclass().getSimpleName();
        p("------- Class: %s (%s)", className, superClass);
        Method[] declaredMethods = aClass.getDeclaredMethods();
        Constructor<?>[] declaredConstructors = aClass.getDeclaredConstructors();
        Field[] declaredFields = aClass.getDeclaredFields();
        for (Constructor c : declaredConstructors) {
          signature = c.toGenericString();
          if (!signature.startsWith("public")) {
            continue;
          }
          strings = signature.split("\\(");
          signature = "(" + strings[1];
          signature = signature.replace("org.sikuli.script.", "");
          if (functions.containsKey(" ")) {
            functions.get(" ").add(signature);
          } else {
            ArrayList<String> signatures = new ArrayList<>();
            signatures.add(signature);
            functions.put(" ", signatures);
          }
        }
        for (Method m : declaredMethods) {
          signature = m.toGenericString();
          orgSignature = signature;
          if (!signature.startsWith("public")) {
            continue;
          }
          boolean isStatic = false;
          boolean hasThrows = false;
          signature = signature.replace("public ", "").trim();
          signature = signature.replace("java.lang.", "");
          signature = signature.replace("java.util.", "");
          signature = signature.replace("org.sikuli.script.", "");
          signature = signature.replace(cName + ".", "");
          if (signature.startsWith("<")) {
            strings = signature.split("> ");
            signature = signature.substring(strings[0].length() + 2);
          }
          if (signature.startsWith("static")) {
            signature = signature.substring(7);
            isStatic = true;
          }
          strings = signature.split(" ");
          strings = strings[1].split("\\(");
          String function = strings[0];
          String functionName = function;
          if (function.startsWith("get") || function.startsWith("set")) {
            functionName = function.substring(3);
            function = "+" + functionName;
          }
          signature = signature.replace(functionName, "");
          if (signature.contains("throws")) {
            strings = signature.split(" throws ");
            hasThrows = true;
            signature = strings[0];
          }
          strings = signature.split(" ");
          signature = strings[1] + " -> " + strings[0] + (hasThrows ? "!" : "");
          if (isStatic) function = " " + function;
          if (functions.containsKey(function)) {
            functions.get(function).add(signature);
          } else {
            ArrayList<String> signatures = new ArrayList<>();
            signatures.add(signature);
            functions.put(function, signatures);
          }
        }
        List<String> notBeans = new ArrayList<>();
        for (String function : functions.keySet()) {
          if (function.startsWith("+")) {
            String functionName = function.substring(1);
            int nGet = 0;
            int nSet = 0;
            for (String sig : functions.get(function)) {
              if (sig.startsWith("get")) {
                nGet++;
              } else if (sig.startsWith("set")) {
                nSet++;
              }
            }
            if (nSet > 0 && nGet > 0) continue;
            notBeans.add((nGet > 0 ? "get" : "set") + function.substring(1));
          }
        }
        if (notBeans.size() > 0) {
          for (String function: notBeans) {
            List<String> newSignatures = new ArrayList<>();
            List<String> listSignatures = functions.remove("+" + function.substring(3));
            for (String sig : listSignatures) {
              sig = sig.replace("get", "").replace("set", "");
              newSignatures.add(sig);
            }
            functions.put(function, newSignatures);
          }
        }
        Map<String, List<String>> sortedFunctions = sortMap(functions);
        for (String function : sortedFunctions.keySet()) {
          p("%-20s : %s", function, sortedFunctions.get(function));
        }
      } catch (Exception e) {
        e.printStackTrace();
        p("Error: class %s (%s)", className, orgSignature);
      }
    }
  }

  static Map<String, List<String>> sortMap(Map<String, List<String>> map) {
    List<Map.Entry<String, List<String>>> entries
            = new ArrayList<>(map.entrySet());
    Collections.sort(entries, new Comparator<Map.Entry<String, List<String>>>() {
      @Override
      public int compare(
              Map.Entry<String, List<String>> o1, Map.Entry<String, List<String>> o2) {
        return o1.getKey().toLowerCase().compareTo(o2.getKey().toLowerCase());
      }
    });
    Map<String, List<String>> sortedMap = new LinkedHashMap<>();
    for (Map.Entry<String, List<String>> entry : entries) {
      sortedMap.put(entry.getKey(), entry.getValue());
    }
    return sortedMap;
  }
}
