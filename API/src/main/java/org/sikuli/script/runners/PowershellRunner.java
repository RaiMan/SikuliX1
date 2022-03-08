/*
 * Copyright (c) 2010-2022, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script.runners;

import org.sikuli.script.runnerSupport.IScriptRunner;
import org.sikuli.script.support.Commons;

import java.io.File;

public class PowershellRunner extends ProcessRunner {

  public static final String NAME = "PowerShell";
  public static final String TYPE = "text/powershell";
  public static final String[] EXTENSIONS = new String[] {"ps1"};

  @Override
  protected int doRunScript(String scriptFile, String[] scriptArgs, IScriptRunner.Options options) {
    File fScriptFile = new File(scriptFile);

//    String[] psDirect = new String[]{
//            "powershell.exe", "-ExecutionPolicy", "UnRestricted",
//            "-NonInteractive", "-NoLogo", "-NoProfile", "-WindowStyle", "Hidden",
//            "-File", fScriptFile.getAbsolutePath()
//    };

    String cmd = "cmd.exe";
    String[] args = new String[] {
      "/S", "/C", "type \"" + fScriptFile.getAbsolutePath() + "\" | powershell -noprofile -"
    };

    return super.doRunScript(cmd, args, options);
  }

  @Override
  public boolean isSupported() {
    return Commons.runningWindows();
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public String[] getExtensions() {
    return EXTENSIONS.clone();
  }

  @Override
  public String getType() {
    return TYPE;
  }
}
