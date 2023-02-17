/*
 * Copyright (c) 2010-2022, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.util;

import org.sikuli.script.SikuliXception;
import org.sikuli.script.support.Commons;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.*;
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
    System.out.println(String.format(message, args)); //OK
  }

  static String className = "";
  static String classOuter = "";

  static boolean shouldReflect = false;
  static boolean onlyMissing = false;
  static boolean noComments = false;
  static boolean rawPublic = false;
  static boolean shouldCreate = false;

  static String packg = "org.sikuli.";
  static String thisPackg = packg;
  static File sourceBase = new File(Commons.getWorkDir(), "/src/main/java");
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
      classOuter = arg;
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
  static String[] strings;

  static void crawlClass(String className) {
    String orgSignature = "";
    String signature = "";
    boolean isInner = false;
    Map<String, List<String>> functions;
    for (String clazz : classNames) {
      functions = new HashMap<>(); 
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
        ArrayList<String> classInfo = new ArrayList<>();
        classInfo.add(String.format("%s (%s)", className, superClass));
        if (isInner) {
          classInfo.add(classOuter);
        }
        functions.put("#name", classInfo);
        Method[] declaredMethods = aClass.getDeclaredMethods();
        Constructor<?>[] declaredConstructors = aClass.getDeclaredConstructors();
        Field[] declaredFields = aClass.getDeclaredFields();
        for (Constructor constructor : declaredConstructors) {
          int modifiers = constructor.getModifiers();
          if ((modifiers & Modifier.PUBLIC) == 0) continue;
          String constrName = constructor.getName().replace(thisPackg, "");
          if (isInner) {
            constrName = constrName.split("\\$")[1];
          }
          String params = "()";
          String hasThrows = "";
          if (constructor.getExceptionTypes().length > 0) {
            hasThrows = String.format("!%s", (Object[]) constructor.getExceptionTypes());
          }
          if (constructor.getParameterCount() > 0) {
            throw new SikuliXception("Constructor Params ?? " + constructor.toGenericString());
          }
          signature = constrName + params + hasThrows;
          if (functions.containsKey(" ")) {
            functions.get(" ").add(signature);
          } else {
            ArrayList<String> signatures = new ArrayList<>();
            signatures.add(signature);
            functions.put(" ", signatures);
          }
        }
        for (Method method : declaredMethods) {
          int modifiers = method.getModifiers();
          if ((modifiers & Modifier.PUBLIC) == 0) {
            continue;
          }
          boolean isStatic = (modifiers & Modifier.STATIC) == 1 ? true : false;
          String methName = method.getName().replace(thisPackg, "");
          if (isInner) {
            methName = methName.split("\\$")[1];
          }
          String parameter = "()";
          String hasThrows = "";
          if (method.getExceptionTypes().length > 0) {
            hasThrows = String.format(" !%s", (Object[]) method.getExceptionTypes());
          }
          if (method.getParameterCount() > 0) {
            String genericString = method.toGenericString();
            Type[] gpTypes = method.getGenericParameterTypes();
            String[] parameters = new String[gpTypes.length];
            int n = 0;
            for (Type param : gpTypes) {
              parameters[n] = param.getTypeName();
              n++;
            }
            parameter = String.format("%s", (Object[]) gpTypes);
          }
          String returns = method.getReturnType().toString();
          if ("void".equals(returns)) {
            signature = parameter + " void" + hasThrows;
          } else {
            signature = String.format("%s -> %s%s", returns, parameter, hasThrows);
          }

          String getterSetter = "";
          String function = methName;
          if (function.startsWith("get") || function.startsWith("set")) {
            getterSetter = function.substring(0, 3);
            function = function.substring(3);
          }
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
        classes.add(sortMap(functions));
      } catch (Exception e) {
        p("Error: class %s (%s)", className, orgSignature);
      }
      isInner = true;
    }
  }

  static void printFunctions() {
    for (Map<String, List<String>> clazz : classes) {
      p("%-20s : %s", "----- Class -----", clazz.get("#name"));
      for (String function : clazz.keySet()) {
        if (function.startsWith("#")) {
          continue;
        }
        String name = function;
        if (function.startsWith("+")) {
          name = "+" + function.substring(1, 2).toLowerCase() + function.substring(2);
        }
        p("%-20s : %s", name, clazz.get(function));
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
