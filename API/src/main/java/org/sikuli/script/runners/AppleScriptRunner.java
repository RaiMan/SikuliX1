/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script.runners;

import java.io.File;

import org.sikuli.basics.FileManager;
import org.sikuli.script.support.IScriptRunner;
import org.sikuli.script.support.RunTime;

public class AppleScriptRunner extends AbstractLocalFileScriptRunner {

  public static final String NAME = "AppleScript";
  public static final String TYPE = "text/applescript";
  public static final String[] EXTENSIONS = new String[] {"script"};

  private static final int LVL = 3;
  private static final RunTime RUN_TIME = RunTime.get();

  @Override
  protected int doEvalScript(String script, IScriptRunner.Options options) {
    String osascriptShebang = "#!/usr/bin/osascript\n";
    script = osascriptShebang + script;
    File aFile = FileManager.createTempFile("script");
    aFile.setExecutable(true);
    FileManager.writeStringToFile(script, aFile);

    int retcode = runScript(aFile.getAbsolutePath(), null, options);

    if (retcode != 0) {
      if (options != null && options.isSilent()) {
        log(LVL, "AppleScript:\n%s\nreturned:\n%s", script, RUN_TIME.getLastCommandResult());
      } else {
        log(-1, "AppleScript:\n%s\nreturned:\n%s", script, RUN_TIME.getLastCommandResult());
      }
    }
    return retcode;
  }

  @Override
  protected int doRunScript(String scriptFile, String[] scriptArgs, IScriptRunner.Options options) {
    String prefix = options != null && options.isSilent() ? "!" : "";

    String retVal = RUN_TIME.runcmd(new String[]{prefix + new File(scriptFile).getAbsolutePath()});
    String[] parts = retVal.split("\n");
    int retcode = -1;
    try {
      retcode = Integer.parseInt(parts[0]);
    } catch (Exception ex) {
    }
    return retcode;
  }

  @Override
  public boolean isSupported() {
    return RunTime.get().runningMac;
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
