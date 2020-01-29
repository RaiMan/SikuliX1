/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script;

import java.io.File;
import java.net.URL;

import org.sikuli.script.runnerSupport.JythonSupport;
import org.sikuli.script.support.RunTime;

/**
 * Can be used in pure Jython environments to add the Sikuli Python API to sys.path<br>
 * Usage: (before any Sikuli features are used)<br>
 * import org.sikuli.script.SikulixForJython<br>
 * from sikuli import *
 */
public class SikulixForJython {

  private static SikulixForJython instance = null;
  private static final int lvl = 3;

  static {
    staticInit();
  }

  static void staticInit() {
    JythonSupport helper = JythonSupport.get();
    helper.log(lvl, "SikulixForJython: init: starting");
    RunTime runTime = RunTime.get();
    String sikuliStuff = "sikuli/Sikuli";
    File fSikuliStuff = helper.existsSysPathModule(sikuliStuff);
    String libSikuli = "/Lib/" + sikuliStuff + ".py";
    String fpSikuliStuff;
    if (null == fSikuliStuff) {
      URL uSikuliStuff = runTime.resourceLocation(libSikuli);
      if (uSikuliStuff == null) {
        runTime.dumpClassPath();
        //helper.terminate(999, "no suitable sikulix...jar on classpath");
        throw new SikuliXception(String.format("fatal: " + "Jython: " + "no suitable sikulix...jar on classpath"));
      }
      fpSikuliStuff = runTime.fSikulixLib.getAbsolutePath();
      if (!helper.hasSysPath(fpSikuliStuff)) {
        helper.log(lvl, "sikuli/*.py not found on current Jython::sys.path");
        helper.addSysPath(fpSikuliStuff);
        if (!helper.hasSysPath(fpSikuliStuff)) {
          //helper.terminate(999, "not possible to add to Jython::sys.path: %s", fpSikuliStuff);
          throw new SikuliXception(String.format("fatal: " + "Jython: " +
                  "not possible to add to Jython::sys.path: %s", fpSikuliStuff));
        }
        helper.log(lvl, "added as Jython::sys.path[0]:\n%s", fpSikuliStuff);
      } else {
        helper.log(lvl, "sikuli/*.py is on Jython::sys.path at:\n%s", fpSikuliStuff);
      }
    }
    helper.addSitePackages();
//TODO default ImagePath???
/*
    List<String> sysArgv = helper.getSysArgv();
    if (sysArgv.size() > 0) {
      String path = sysArgv.get(0);
      Settings.BundlePath = new File(path).getParent();
      helper.log(lvl, "Default ImagePath: %s", Settings.BundlePath);
    }
*/
    helper.log(lvl, "SikulixForJython: init: success");
  }

  SikulixForJython() {
  }

  public static SikulixForJython get() {
    if (null == instance) {
      instance = new SikulixForJython();
    }
    return instance;
  }
}
