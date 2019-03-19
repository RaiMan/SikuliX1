/*
 * Copyright (c) 2010-2018, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script.runners;

import java.io.File;
import java.net.URI;
import java.util.Map;

import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;
import org.sikuli.script.RunTime;

public class AppleScriptRunner extends AbstractScriptRunner {
     
  public static final String NAME = "AppleScript";
  public static final String TYPE = "text/applescript";
  public static final String[] EXTENSIONS = new String[] {"script"};
  
  public static final String SILENT_OPTION = "silent";
  
  private static final int LVL = 3;
  private static final RunTime RUN_TIME = RunTime.get();
        
  @Override
  protected int doEvalScript(String script, Map<String,Object> options) {
    String osascriptShebang = "#!/usr/bin/osascript\n";
    script = osascriptShebang + script;
    File aFile = FileManager.createTempFile("script");
    aFile.setExecutable(true);
    FileManager.writeStringToFile(script, aFile);
   
    int retcode = runScript(aFile.getAbsolutePath(), null, options);
    
    if (retcode != 0) {
      if (options != null && Boolean.TRUE.equals(options.get(SILENT_OPTION))) {
        log(LVL, "AppleScript:\n%s\nreturned:\n%s", script, RUN_TIME.getLastCommandResult());
      } else {
        log(-1, "AppleScript:\n%s\nreturned:\n%s", script, RUN_TIME.getLastCommandResult());
      }
    }
    return retcode;
  }
  
  @Override
  protected int doRunScript(String scriptFile, String[] scriptArgs, Map<String,Object> options) {
    String prefix = options != null && Boolean.TRUE.equals(options.get(SILENT_OPTION)) ? "!" : ""; 
        
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
