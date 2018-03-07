/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintStream;

import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;
import org.sikuli.script.RunTime;

/**
 * INTERNAL USE: all things needed with Linux at setup or runtime
 */
public class LinuxSupport {

  static final RunTime runTime = RunTime.get();

  //<editor-fold defaultstate="collapsed" desc="new logging concept">
  private static final String me = "LinuxSupport: ";
  private static int lvl = 3;
  private static boolean isCopiedProvided = false;
  private static boolean haveBuilt = false;
  public static boolean shouldUseProvided = false;

  //  private static String osArch;
  public static void log(int level, String message, Object... args) {
    Debug.logx(level, me + message, args);
  }

  private static void logp(String message, Object... args) {
    Debug.logx(-3, message, args);
  }
//</editor-fold>

  static File fWorkDir = null;
  private static final String buildFolderSrc = "Build/Source";
  private static final String buildFolderInclude = "Build/Include";
  private static final String buildFolderTarget = "Build/Target";
  static File fLibs = runTime.fLibsFolder;
  public static final String slibVision = "VisionProxy";
  public static final String libVision = "lib" + slibVision + ".so";
  public static final String libGrabKey = "libJXGrabKey.so";
  static boolean libSearched = false;

  private static String libOpenCVcore = "";
  private static String libOpenCVimgproc = "";
  private static String libOpenCVhighgui = "";
  private static String libTesseract = "";
  private static boolean opencvAvail = true;
  private static boolean tessAvail = true;

  public static boolean existsLibs() {
    return new File(runTime.fLibsProvided, libVision).exists() ||
            new File(runTime.fLibsProvided, libGrabKey).exists();
  }

  public static boolean copyProvidedLibs(File fLibsFolder) {
    String[] toCopy = runTime.fLibsProvided.list(new FilenameFilter() {
      @Override
      public boolean accept(File folder, String name) {
        if (name.endsWith(".so")) {
          return true;
        }
        return false;
      }
    });
    boolean success = false;
    if (toCopy != null) {
      for (String aLib : toCopy) {
        success |= FileManager.xcopy(new File(runTime.fLibsProvided, aLib),
                new File(fLibsFolder, aLib));
      }
    }
    return success;
  }

  public static boolean checkAllLibs() {
    boolean success = false;
    if (!isCopiedProvided && !runTime.useLibsProvided) {
      success = true;
      String[] allLibs = runTime.fLibsProvided.list(new FilenameFilter() {
        @Override
        public boolean accept(File folder, String name) {
          if (name.toLowerCase().endsWith(".so")) {
            return true;
          }
          return false;
        }
      });
      if (allLibs != null) {
        for (String sLib : allLibs) {
          File fSrc = new File(runTime.fLibsProvided, sLib);
          File fTgt = new File(runTime.fLibsFolder, sLib);
          success &= FileManager.xcopy(fSrc, fTgt);
          log(3, "Copy provided lib: %s (%s)", sLib, (success ? "ok" : "did not work"));
        }
      }
      isCopiedProvided = true;
    } else if (!haveBuilt) {
      haveBuilt = true;
      success = haveToBuild();
    }
    shouldUseProvided = success;
    return success;
  }

  public static boolean haveToBuild() {
    boolean success = true;
    log(3, "we have to build libVisionProxy.so");
    success &= checkNeeded();
    if (success) {
      success &= buildVision();
    }
    return  success;
  }

  private static boolean checkNeeded() {
    String cmdRet;
    boolean checkSuccess = true;
    log(lvl, "checking: availability of OpenCV and Tesseract");
    log(lvl, "checking: scanning loader cache (ldconfig -p)");
    cmdRet = runTime.runcmd("ldconfig -p");
    if (cmdRet.contains(runTime.runCmdError)) {
      log(-1, "checking: ldconfig returns error:\ns", cmdRet);
      checkSuccess = false;
    } else {
      String[] libs = cmdRet.split("\n");
      for (String libx : libs) {
        libx = libx.trim();
        if (!libx.startsWith("lib")) {
          continue;
        }
        if (libx.startsWith("libopencv_core.so.")) {
          libOpenCVcore = libx.split("=>")[1].trim();
        } else if (libx.startsWith("libopencv_highgui.so.")) {
          libOpenCVhighgui = libx.split("=>")[1].trim();
        } else if (libx.startsWith("libopencv_imgproc.so.")) {
          libOpenCVimgproc = libx.split("=>")[1].trim();
        } else if (libx.startsWith("libtesseract.so.")) {
          libTesseract = libx.split("=>")[1].trim();
        }
      }
      if (libOpenCVcore.isEmpty() || libOpenCVhighgui.isEmpty() || libOpenCVimgproc.isEmpty()) {
        log(-1, "checking: OpenCV not in loader cache (see doc-note on OpenCV)");
        opencvAvail = checkSuccess = false;
      } else {
        log(lvl, "checking: found OpenCV libs:\n%s\n%s\n%s",
                libOpenCVcore, libOpenCVhighgui, libOpenCVimgproc);
      }
      if (libTesseract.isEmpty()) {
        log(-1, "checking: Tesseract not in loader cache (see doc-note on Tesseract)");
        tessAvail = checkSuccess = false;
      } else {
        log(lvl, "checking: found Tesseract lib:\n%s", libTesseract);
      }
    }
    // checking wmctrl, xdotool
//    cmdRet = runTime.runcmd("wmctrl -m");
//    if (cmdRet.contains(runTime.runCmdError)) {
//      log(-1, "checking: wmctrl not available or not working");
//    } else {
//      log(lvl, "checking: wmctrl seems to be available");
//    }
//    cmdRet = runTime.runcmd("xdotool version");
//    if (cmdRet.contains(runTime.runCmdError)) {
//      log(-1, "checking: xdotool not available or not working");
//    } else {
//      log(lvl, "checking: xdotool seems to be available");
//    }
    return checkSuccess;
  }

  private static boolean runLdd(File lib) {
    // ldd -r lib
    // undefined symbol: _ZN2cv3MatC1ERKS0_RKNS_5Rect_IiEE	(./libVisionProxy.so)
    String cmdRet = runTime.runcmd("ldd -r " + lib);
    String[] retLines;
    boolean success = true;
    retLines = cmdRet.split("continued: build on the fly on Linux at runtime: if bundled do not work, looking for provided - if these do not work, try to build. setup not ready yet. \n");
    String libName = lib.getName();
    String libsMissing = "";
    for (String line : retLines) {
      if (line.contains("undefined symbol:") && line.contains(libName)) {
        line = line.split("symbol:")[1].trim().split("\\s")[0];
        libsMissing += line + ":";
      }
    }
    if (libsMissing.isEmpty()) {
      log(lvl, "checking: should work: %s", libName);
    } else {
      log(-1, "checking: might not work, has undefined symbols: %s", libName);
      log(lvl, "%s", libsMissing);
      success = false;
    }
    return success;
  }

  public static boolean buildVision() {
    File fLibsSaveDir = runTime.fLibsProvided;
    fWorkDir = fLibsSaveDir.getParentFile();
    fWorkDir.mkdirs();
    fLibsSaveDir.mkdir();
    File fTarget = new File(fWorkDir, buildFolderTarget);
    File fSource = new File(fWorkDir, buildFolderSrc);
    File fInclude = new File(fWorkDir, buildFolderInclude);
    fInclude.mkdirs();

    File[] javas = new File[]{null, null};
    javas[0] = new File(System.getProperty("java.home"));
    String jhome = System.getenv("JAVA_HOME");
    if (jhome != null) {
      javas[1] = new File(jhome);
    }

    log(lvl, "buildVision: starting inline build: libVisionProxy.so");
    log(lvl, "buildVision: java.home from java props: %s", javas[0]);
    log(lvl, "buildVision: JAVA_HOME from environment: %s", javas[1]);

    File javaHome = null;
    for (File jh : javas) {
      if (jh == null) {
        continue;
      }
      if (!new File(jh, "bin/javac").exists()) {
        jh = jh.getParentFile();
      }
      if (!new File(jh, "bin/javac").exists()) {
        jh = null;
      }
      if (jh != null) {
        if (new File(jh, "include/jni.h").exists()) {
          javaHome = jh;
          break;
        }
      }
    }
    if (javaHome == null) {
      log(-1, "buildVision: no valid Java JDK available nor found");
      return false;
    }
    log(lvl, "buildVision: JDK: found at: %s", javaHome);

    File cmdFile = new File(fWorkDir, "runBuild");
    String libVisionPath = new File(fTarget, libVision).getAbsolutePath();
    String sRunBuild = runTime.extractResourceToString("/Support/Linux", "runBuild", "");

    sRunBuild = sRunBuild.replace("#jdkdir#", "jdkdir=" + javaHome.getAbsolutePath());

    String inclUsr = "/usr/include";
    String inclUsrLocal = "/usr/local/include";
    boolean exportIncludeOpenCV = false;
    boolean exportIncludeTesseract = false;

    String inclLib = "opencv2";
    if (!new File(inclUsr, inclLib).exists() && !new File(inclUsrLocal, inclLib).exists()) {
      log(lvl, "buildVision: opencv-include: not found - using the bundled include files");
      exportIncludeOpenCV = true;
    }

    inclLib = "tesseract";
    if (!new File(inclUsr, inclLib).exists() && !new File(inclUsrLocal, inclLib).exists()) {
      log(lvl, "buildVision: tesseract-include: not found - using the bundled include files");
      exportIncludeTesseract = true;
    }

    boolean success = (null != runTime.extractResourcesToFolder("/srcnativelibs/Vision", fSource, null));
    if (!success) {
      log(-1, "buildVision: cannot export bundled sources");
    }
    if (exportIncludeOpenCV) {
      if (null == runTime.extractResourcesToFolder("/srcnativelibs/Include/OpenCV", fInclude, null)) {
        log(-1, "buildVision: cannot export opencv includes");
        success = false;
      }
    }
    if (exportIncludeTesseract) {
      if (null == runTime.extractResourcesToFolder("/srcnativelibs/Include/Tesseract", fInclude, null)) {
        log(-1, "buildVision: cannot export tesseract includes");
        success = false;
      }
    }

    if (success && (exportIncludeOpenCV || exportIncludeTesseract)) {
      sRunBuild = sRunBuild.replace("#extrainclude#", "extrainclude=$work/Include");
    }

    if (success) {
      sRunBuild = sRunBuild.replace("#work#", "work=" + fTarget.getParentFile().getAbsolutePath());
      sRunBuild = sRunBuild.replace("#opencvcore#", "opencvcore=" + libOpenCVcore);
      sRunBuild = sRunBuild.replace("#opencvimgproc#", "opencvimgproc=" + libOpenCVimgproc);
      sRunBuild = sRunBuild.replace("#opencvhighgui#", "opencvhighgui=" + libOpenCVhighgui);
      sRunBuild = sRunBuild.replace("#tesseractlib#", "tesseractlib=" + libTesseract);
    }

    log(lvl, "**** content of build script:\n(stored at: %s)\n%s\n**** content end",
            cmdFile.getAbsolutePath(), sRunBuild);
    try {
      PrintStream psCmdFile = new PrintStream(cmdFile);
      psCmdFile.print(sRunBuild);
      psCmdFile.close();
    } catch (Exception ex) {
      log(-1, "buildVision: problem writing command file:\n%s", cmdFile);
      return false;
    }
    cmdFile.setExecutable(true);

    if (success && opencvAvail && tessAvail) {
      log(lvl, "buildVision: running build script");
      String cmdRet = runTime.runcmd(cmdFile.getAbsolutePath());
      if (cmdRet.contains(runTime.runCmdError)) {
        log(-1, "buildVision: build script returns error:\n%s", cmdRet);
        return false;
      } else {
        log(lvl, "buildVision: checking created libVisionProxy.so");
        if (!runLdd(new File(libVisionPath))) {
          log(-1, "------- output of the build run\n%s", cmdRet);
          return false;
        }
      }
    } else {
      log(-1, "buildVision: corrrect the reported problems and try again");
      return false;
    }
    File providedLib = new File(fLibsSaveDir, libVision);
    if (!FileManager.xcopy(new File(libVisionPath), providedLib)) {
      log(-1, "buildVision: could not save:\n%s", providedLib);
      return false;
    }
    if (runTime.fLibsFolder.exists()) {
      copyProvidedLibs(runTime.fLibsFolder);
    }
    log(lvl, "buildVision: ending inline build: success:\n%s", providedLib);
    return true;
  }

  //TODO needed??? processLibs
//<editor-fold defaultstate="collapsed" desc="obsolete???">
  private static final String[] libsExport = new String[]{null, null};
  private static final String[] libsCheck = new String[]{null, null};

  private static boolean processLibs(String fpLibsJar) {
//    boolean shouldBuildVisionNow = false;
//    if (!fLibs.exists()) {
//      fLibs.mkdirs();
//    }
//    if (fLibs.exists()) {
//      if (fpLibsJar != null) {
//        runTime.extractResourcesToFolderFromJar(fpLibsJar, runTime.fpJarLibs, fLibs, null);
//      } else {
//        runTime.extractResourcesToFolder(runTime.fpJarLibs, fWorkDir, null);
//      }
//      libsCheck[0] = new File(fLibs, libVision).getAbsolutePath();
//      libsCheck[1] = new File(fLibs, libGrabKey).getAbsolutePath();
//      File fLibCheck;
//      for (int i = 0; i < libsCheck.length; i++) {
//        fLibCheck = new File(libsCheck[i]);
//        if (fLibCheck.exists()) {
//          if (!checklibs(fLibCheck)) {
////TODO why? JXGrabKey unresolved: pthread
//            if (i == 0) {
//              if (libsExport[i] == null) {
//                log(-1, "provided %s might not be useable on this Linux - see log", fLibCheck.getName());
//              } else {
//                log(-1, "bundled %s might not be useable on this Linux - see log", fLibCheck.getName());
//              }
//              shouldBuildVisionNow = true;
//            }
//          }
//        } else {
//          log(-1, "check not possible for\n%s", fLibCheck);
//        }
//      }
//    } else {
//      log(-1, "check useability of libs: problems with libs folder\n%s", fLibs);
//    }
//    return shouldBuildVisionNow;
    return true;
  }

  private static boolean checklibs(File lib) {
    String cmdRet;
    String[] retLines;
    boolean checkSuccess = true;

    if (!libSearched) {
      checkSuccess = checkNeeded();
      libSearched = true;
    }

    log(lvl, "checking\n%s", lib);
    // readelf -d lib
    // 0x0000000000000001 (NEEDED)             Shared library: [libtesseract.so.3]
    cmdRet = runTime.runcmd("readelf -d " + lib);
    if (cmdRet.contains(runTime.runCmdError)) {
      log(-1, "checking: readelf returns error:\ns", cmdRet);
      checkSuccess = false;
    } else {
      retLines = cmdRet.split("\n");
      String libsNeeded = "";
      for (String line : retLines) {
        if (line.contains("(NEEDED)")) {
          line = line.split("\\[")[1].replace("]", "");
          libsNeeded += line + ":";
        }
      }
      log(lvl, libsNeeded);
    }

    if (!runLdd(lib)) {
      checkSuccess = false;
    }

//    return false; // for testing
    return checkSuccess;
  }
//</editor-fold>

}
