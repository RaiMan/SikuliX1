/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script;

import java.io.File;
import java.net.URL;

import org.sikuli.basics.FileManager;
import org.sikuli.util.JythonHelper;

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
    JythonHelper helper = JythonHelper.get();
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
        helper.terminate(1, "no suitable sikulix...jar on classpath");
      }
      fpSikuliStuff = runTime.fSikulixLib.getAbsolutePath();
      if (!helper.hasSysPath(fpSikuliStuff)) {
        helper.log(lvl, "sikuli/*.py not found on current Jython::sys.path");
        helper.addSysPath(fpSikuliStuff);
        if (!helper.hasSysPath(fpSikuliStuff)) {
          helper.terminate(1, "not possible to add to Jython::sys.path:\n%s", fpSikuliStuff);
        }
        helper.log(lvl, "added as Jython::sys.path[0]:\n%s", fpSikuliStuff);
      } else {
        helper.log(lvl, "sikuli/*.py is on Jython::sys.path at:\n%s", fpSikuliStuff);
      }
    }
    helper.addSitePackages();
    helper.log(lvl, "SikulixForJython: init: success");
  }

  private SikulixForJython() {
  }

  public static SikulixForJython get() {
    if (null == instance) {
      instance = new SikulixForJython();
    }
    return instance;
  }
}
