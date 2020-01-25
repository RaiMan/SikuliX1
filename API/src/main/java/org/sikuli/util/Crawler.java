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

  //<editor-fold desc="00 housekeeping">
  private static void p(String message, Object... args) {
    if (args.length == 0) {
      return;
    }
    if (message.isEmpty()) {
      message = "%s";
    }
    System.out.println(String.format(message, args));
  }

  static String className = "";

  static boolean shouldReflect = false;
  static boolean onlyMissing = false;
  static boolean noComments = false;
  static boolean rawPublic = false;
  static boolean shouldCreate = false;

  static String packg = "org.sikuli.";
  static String thisPackg = packg;
  static File sourceBase = new File(RunTime.get().fWorkDir, "/src/main/java");
  //</editor-fold>

  public static void main(String[] args) {
    if (args.length == 0) return;
    for (String arg : args) {
      if (arg.equals("*")) {
        shouldCreate = true;
        continue;
      }
      if (arg.equals("!")) {
        rawPublic = true;
        continue;
      }
      if (arg.equals("+")) {
        shouldReflect = true;
        continue;
      }
      if (arg.equals("?")) {
        onlyMissing = true;
        continue;
      }
      if (arg.equals("-")) {
        noComments = true;
        continue;
      }
      if (arg.startsWith("#")) {
        break;
      }
      if (arg.endsWith(".")) {
        thisPackg = packg + arg;
        continue;
      }
      if (rawPublic) {
        shouldReflect = false;
        onlyMissing = false;
        noComments = false;
      }
      className = thisPackg + arg;
      String fpSource = sourceBase.getAbsolutePath() + "/" + className.replace(".", "/") + ".java";
      File fSource = new File(fpSource);
      if (!fSource.exists()) {
        p("ERROR: not exists %s", fSource);
        System.exit(1);
      }
      if (shouldReflect) {
        crawlLines(fSource, true);
        crawlClass(className);
        printFunctions();
      }
      if (rawPublic || noComments || onlyMissing || shouldCreate) {
        commentReset();
        crawlLines(fSource, false);
        if (shouldCreate) {
          createPyMethods();
        }
      }
      onlyMissing = false;
      noComments = false;
      shouldReflect = false;
    }
  }

  //<editor-fold desc="10 reflect class">
  static List<Map<String, List<String>>> classes = new ArrayList<>();
  static Map<String, List<String>> functions;
  static String[] strings;

  static void crawlClass(String className) {
    String orgSignature = "";
    String signature = "";
    for (String clazz : classNames) {
      functions = new HashMap<>();
      classes.add(functions);
      try {
        Class<?> aClass;
        if (className.endsWith(clazz)) {
          aClass = Class.forName(className);
        } else {
          aClass = Class.forName(className + "$" + clazz);
          className = className + "." + clazz;
        }
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
          if (isStatic) function = "!" + function;
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
            if (name.startsWith("!")) {
              makeStatic = "!";
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
            if (function.startsWith("!")) {
              from = 4;
              prefix += "!";
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
  }

  static void printFunctions() {
    for (Map<String, List<String>> cFunctions : classes) {
      for (String function : cFunctions.keySet()) {
        String name = function;
        if (function.startsWith("+")) {
          name = "+" + function.substring(1, 2).toLowerCase() + function.substring(2);
        }
        p("%-20s : %s", name, functions.get(function));
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
  //</editor-fold>

  //<editor-fold desc="20 process docs">
  static String NL = "";
  static int lineNumber = 0;
  static List<String> classNames = new ArrayList<>();

  static void crawlLines(File fSource, boolean onlyClasses) {
    try {
      Stream<String> lines = Files.lines(Paths.get(fSource.getAbsolutePath()));
      lines.forEach(line -> processLine(line, onlyClasses));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  static void processLine(String line, boolean onlyClasses) {
    lineNumber++;
    line = line.trim();
    if (line.isEmpty() || line.startsWith("@")) {
      return;
    }
    if (!onlyClasses && line.startsWith("//")) {
      if (!onlyMissing && line.startsWith("//<editor-fold")) {
        if (noComments) {
          NL = "";
        }
        p("%s(%d) %s", NL, lineNumber, line);
        NL = "";
      }
      return;
    }
    if (line.startsWith("public") && (rawPublic || onlyClasses)) {
      if (onlyClasses && !line.contains("class ")) {
        return;
      }
      classNames.add(line.substring(line.indexOf("class ") + 6).split("\\s")[0]);
      if (rawPublic) {
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
        boolean isStatic = false;
        if (line.startsWith("static")) {
          isStatic = true;
          line = line.substring(7);
        }
        String function = line;
        if (line.startsWith("<")) {
          int end = line.indexOf(">");
          if (end < 0) {
            p("ERROR: unbalanced <>: %s", line);
            System.exit(1);
          }
          function = line.substring(end + 2);
        }
        if (isStatic) function = "!" + function;
        String docs = lastComment;
        boolean hasDocs = true;
        if (docs.isEmpty()) {
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
            if (noComments) {
              p("(%d) %s", lineNumber, function);
            } else {
              p("%s\n(%d) %s", docs, lineNumber, function);
            }
          } else
            p("(%d) ?doc? %s", lineNumber, function);
        }
      }
      NL = "\n";
      commentResetAll();
      return;
    }
    if (inComment && line.startsWith("*")) {
      if (line.startsWith("*/")) {
        commentReset();
      } else {
        if (line.startsWith("*")) line = line.substring(1);
        commentAddLine(line);
      }
    }

  }

  //<editor-fold desc="comments">
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
    if (currentComment.isEmpty()) {
      line = "---\n" + line;
    }
    currentComment = currentComment + "\n" + line;
  }
  //</editor-fold>

  private static List<List<String>> functionDocs = new ArrayList<>();

  //<editor-fold desc="pymethod">
  private static final String pyMethod = "" +
      ".. py:class:: %s\n" +
      "\n" +
      "\t.. py:method:: %s\n" +
      "\n" +
      "\t\t%s\n" +
      "\n" +
      "%s" +
      "\t\t:return: %s\n";
  //</editor-fold>

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

  private static void createPyMethods() {
    String clazz = className.substring(className.lastIndexOf(".") + 1);
    String[] docItems = null;
    for (List<String> functionDoc : functionDocs) {
      String function = functionDoc.get(0);
      String docReturn = "";
      Map<String, String> docParams = new LinkedHashMap<>();
      String doc = functionDoc.get(1);
      docItems = doc.split("\\n @");
      if (docItems.length > 1) {
        String pyDoc = docItems[0].substring(4);
        for (int n = 1; n < docItems.length; n++) {
          String item = docItems[n];
          if (item.startsWith("return ")) {
            docReturn = item.substring(7);
          } else if (item.startsWith("param ")) {
            String[] strings = item.substring(6).split("\\s");
            docParams.put(strings[0], item.substring(6 + strings[0].length() + 1));
          }
        }
        p("");
      }
    }
  }
  //</editor-fold>
}
