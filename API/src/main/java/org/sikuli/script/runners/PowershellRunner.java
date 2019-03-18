package org.sikuli.script.runners;

import java.io.File;
import java.net.URI;
import java.util.Map;

import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;
import org.sikuli.script.RunTime;

public class PowershellRunner extends AbstractScriptRunner {
  
  public static final String NAME = "PowerShell";
  public static final String TYPE = "text/powershell";
  public static final String[] EXTENSIONS = new String[] {"ps1"};
  
  private static final RunTime RUN_TIME = RunTime.get();
  
  private static final String ME = "PowershellRunner: ";
  
  private void log(int level, String message, Object... args) {
    Debug.logx(level, ME + message, args);
  }

  @Override
  public int evalScript(String script, Map<String,Object> options) {
    File aFile = FileManager.createTempFile("ps1");
    FileManager.writeStringToFile(script, aFile);
    return runScript(aFile.toURI(), null, null);
  }
  
  @Override
  public int runScript(URI uScriptfile, String[] scriptArgs, Map<String,Object> options) {    
    File scriptfile = new File(uScriptfile);
    
    String[] psDirect = new String[]{
            "powershell.exe", "-ExecutionPolicy", "UnRestricted",
            "-NonInteractive", "-NoLogo", "-NoProfile", "-WindowStyle", "Hidden",
            "-File", scriptfile.getAbsolutePath()
    };
    String[] psCmdType = new String[]{
            "cmd.exe", "/S", "/C",
            "type " + scriptfile.getAbsolutePath() + " | powershell -noprofile -"
    };
    String retVal = RUN_TIME.runcmd(psCmdType);
    String[] parts = retVal.split("\\s");
    int retcode = -1;
    try {
      retcode = Integer.parseInt(parts[0]);
    } catch (Exception ex) {
    }
    if (retcode != 0) {
      log(-1, "PowerShell:\n%s\nreturned:\n%s", scriptfile, RUN_TIME.getLastCommandResult());
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
    return 0;
  }

  @Override
  public int runInteractive(String[] scriptArgs) {
    // TODO Auto-generated method stub
    return 0;
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
    return RunTime.get().runningWindows;
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
