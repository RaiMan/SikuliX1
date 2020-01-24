/*
 * Copyright (c) 2010-2019, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.util;

import org.sikuli.script.support.RunTime;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public class Crawler {

  private static final String pyMethod = "" +
      ".. py:class:: %s\n" +
      "\n" +
      "\t.. py:method:: %s\n" +
      "\n" +
      "\t\t%s\n" +
      "\n" +
      "%s" +
      "\t\t:return: %s\n";

  private static String pyMethodParams = "\t\t:param %s: %s\n";

  private static String createPyMethod(String clazz, String method, String text,
                                       Map<String, String> params, String returns) {
    String docMethod;
    String docParams = "";
    for (String pName : params.keySet()) {
      docParams += String.format(pyMethodParams, pName, params.get(pName));
    }
    docMethod = String.format(pyMethod, clazz, method, docParams, returns);
    return docMethod;
  }

  private static void p(String message, Object... args) {
    if (message.isEmpty()) message = "%s";
    System.out.println(String.format(message, args));
  }

  static boolean onlyMissing = false;

  static Map<String, List<String>> functions;
  static String[] strings;
  static String packg = "org.sikuli.";
  static String thisPackg = packg;
  static File sourceBase = new File(RunTime.get().fWorkDir, "/src/main/java");

  static int lineNumber = 0;

  public static void main(String[] args) {
    if (args.length == 0) return;
    for (String arg : args) {
      if (arg.equals("?")) {
        onlyMissing = true;
        continue;
      }
      if (arg.startsWith("#")) {
        break;
      }
      if (arg.endsWith(".")) {
        thisPackg = packg + arg;
        continue;
      }
      String className = thisPackg + arg;
      String fpSource = sourceBase.getAbsolutePath() + "/" + className.replace(".", "/") + ".java";
      File fSource = new File(fpSource);
      if (!fSource.exists()) {
        p("ERROR: not exists %s", fSource);
        System.exit(1);
      }
//      crawlClass(className);
//      printFunctions();
      commentReset();
      try {
        Stream<String> lines = Files.lines(Paths.get(fSource.getAbsolutePath()));
        lines.forEach(line -> processLine(line));
      } catch (IOException e) {
        e.printStackTrace();
      }
      onlyMissing = false;
    }
  }

  private static List<List<String>> functionDocs = new ArrayList<>();

  static void processLine(String line) {
    lineNumber++;
    line = line.trim();
    if (line.isEmpty() || line.startsWith("@")) {
      return;
    }
    if (line.startsWith("//")) {
      if (line.startsWith("//<editor-fold")) {
        p("(%d) %s", lineNumber, line);
      }
      return;
    }
    if (line.startsWith("/**")) {
      if (inComment) {
        commentReset();
      } else {
        inComment = true;
      }
      return;
    }
    if (line.startsWith("public") && line.endsWith("{")) {
      if (!line.contains("class")) {
        line = line.substring(7, line.length() - 1).trim();
        String[] parts = line.split("\\s");
        String function = parts[1];
        if (function.startsWith("<")) function = line.split("> ")[1].split("\\s")[1];
        function = line.substring(line.indexOf(function));
        String docs = lastComment;
        boolean hasDocs = true;
        if (docs.isEmpty()) {
          docs = "\n* no javadoc *";
          hasDocs = false;
        }
        if (hasDocs) {
          List<String> item = new ArrayList<>();
          item.add(function);
          item.add(docs);
          functionDocs.add(item);
        }
        if (!onlyMissing || (onlyMissing && !hasDocs)) {
          if (hasDocs) {
            p("%s\n(%d) %s", docs, lineNumber, function);
          } else
            p("(%d) ??? %s", lineNumber, function);
        }
        commentResetAll();
        return;
      }
    }
    if (inComment && line.startsWith("*")) {
      if (line.startsWith("*/")) {
        commentReset();
      } else {
        commentAddLine(line);
      }
      return;
    }
    commentResetAll();
  }

  static boolean inComment = false;
  static String currentComment = "";
  static String lastComment = "";

  static void commentReset() {
    inComment = false;
    lastComment = currentComment;
    currentComment = "";
  }

  static void commentResetAll() {
    inComment = false;
    lastComment = "";
    currentComment = "";
  }

  static void commentAddLine(String line) {
    currentComment = currentComment + "\n" + line;
  }

  static void crawlClass(String className) {
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
        signature = signature.substring("public".length() + 1).replace("org.sikuli.script.", "");
        ;
        strings = signature.split("\\(");
        signature = strings[0] + "(" + strings[1];
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
        if (signature.startsWith("static")) {
          signature = signature.substring(7);
          isStatic = true;
        }
        if (signature.startsWith("<")) {
          strings = signature.split("> ");
          signature = signature.substring(strings[0].length() + 2);
        }
        strings = signature.split(" ");
        String returns = strings[0];
        strings = strings[1].split("\\(");
        String function = strings[0];
        String functionName = function;
        String getterSetter = "";
        if (function.startsWith("get") || function.startsWith("set")) {
          getterSetter = function.substring(0, 3);
          function = function.substring(3);
        }
        signature = signature.replace(functionName, "");
        if (signature.contains("throws")) {
          strings = signature.split(" throws ");
          hasThrows = true;
          signature = strings[0];
        }
        strings = signature.split(" ");
        signature = getterSetter + strings[1] + " -> " + strings[0] + (hasThrows ? "!" : "");
        if (isStatic) function = " " + function;
        if (!getterSetter.isEmpty()) function = "+" + function;
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
          String name = function.substring(1);
          String makeStatic = "";
          if (name.startsWith(" ")) {
            makeStatic = " ";
            name = function.substring(2);
          }
          notBeans.add(makeStatic + (nGet > 0 ? "get" : "set") + name);
        }
      }
      if (notBeans.size() > 0) {
        for (String function : notBeans) {
          List<String> newSignatures = new ArrayList<>();
          int from = 3;
          String prefix = "+";
          if (function.startsWith(" ")) {
            from = 4;
            prefix += " ";
          }
          List<String> listSignatures = functions.remove(prefix + function.substring(from));
          for (String sig : listSignatures) {
            sig = sig.replace("get", "").replace("set", "");
            newSignatures.add(sig);
          }
          functions.put(function, newSignatures);
        }
      }
      functions = sortMap(functions);
    } catch (Exception e) {
      e.printStackTrace();
      p("Error: class %s (%s)", className, orgSignature);
    }
  }

  static void printFunctions() {
    for (String function : functions.keySet()) {
      String name = function;
      if (function.startsWith("+")) {
        name = "+" + function.substring(1, 2).toLowerCase() + function.substring(2);
      }
      p("%-20s : %s", name, functions.get(function));
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
