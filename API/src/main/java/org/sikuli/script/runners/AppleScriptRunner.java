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
  static final String ME = "Runner: ";
  
  private static void log(int level, String message, Object... args) {
    Debug.logx(level, ME + message, args);
  }
      
  @Override
  public int evalScript(String script, Map<String,Object> options) {
    String osascriptShebang = "#!/usr/bin/osascript\n";
    script = osascriptShebang + script;
    File aFile = FileManager.createTempFile("script");
    aFile.setExecutable(true);
    FileManager.writeStringToFile(script, aFile);
   
    int retcode = runScript(URI.create(aFile.getAbsolutePath()), null, options);
    
    if (retcode != 0) {
      if (Boolean.TRUE.equals(options.get(SILENT_OPTION))) {
        log(LVL, "AppleScript:\n%s\nreturned:\n%s", script, RUN_TIME.getLastCommandResult());
      } else {
        log(-1, "AppleScript:\n%s\nreturned:\n%s", script, RUN_TIME.getLastCommandResult());
      }
    }
    return retcode;
  }
  
  @Override
  public int runScript(URI scriptfile, String[] scriptArgs, Map<String,Object> options) {
    String prefix = options.containsKey("silent") ? "!" : ""; 
        
    String retVal = RUN_TIME.runcmd(new String[]{prefix + new File(scriptfile).getAbsolutePath()});
    String[] parts = retVal.split("\n");
    int retcode = -1;
    try {
      retcode = Integer.parseInt(parts[0]);
    } catch (Exception ex) {
    }
    return retcode;
  }

  @Override
  public void runLines(String lines, Map<String,Object> options) {
    // TODO Auto-generated method stub

  }

  @Override
  public int runTest(URI scriptfile, URI imagedirectory, String[] scriptArgs, Map<String,Object> options) {
    // TODO Auto-generated method stub
    return -1;
  }

  @Override
  public int runInteractive(String[] scriptArgs) {
    // TODO Auto-generated method stub
    return -1;
  }

  @Override
  public String getCommandLineHelp() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getInteractiveHelp() {
    // TODO Auto-generated method stub
    return null;
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
 
  @Override
  public void close() {
    // TODO Auto-generated method stub

  }

  @Override
  public boolean doSomethingSpecial(String action, Object[] args) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void execBefore(String[] stmts) {
    // TODO Auto-generated method stub

  }

  @Override
  public void execAfter(String[] stmts) {
    // TODO Auto-generated method stub

  }

  @Override
  protected void doInit(String[] args) {
    // TODO Auto-generated method stub

  }

}
