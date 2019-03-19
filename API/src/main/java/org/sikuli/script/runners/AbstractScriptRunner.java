/*
 * Copyright (c) 2010-2018, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script.runners;

import java.io.File;
import java.io.PipedInputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.sikuli.basics.Debug;
import org.sikuli.script.IScriptRunner;
import org.sikuli.script.SikuliXception;

public abstract class AbstractScriptRunner implements IScriptRunner {

  boolean ready = false;
  boolean redirected = false;
  
  protected void log(int level, String message, Object... args) {
    Debug.logx(level, getName() + ": " + message, args);
  }
  
  private void logNotSupported(String method) {
    Debug.log(-1, "%s does not (yet) support %s",getName(), method);
  }
  
  @Override
  public final void init(String[] args) throws SikuliXception {
    synchronized(this) {
      if(!ready) {
        try {          
          doInit(args);
          ready = true;
        } catch (Exception e) {
          throw new SikuliXception("Cannot initialize Script runner " + this.getName(), e);        
        }
      }
    }
  }
  
  public final boolean isReady() {
    synchronized(this) {
      return ready;
    }
  }
    
  @Override
  public final boolean hasExtension(String ending) {
    for (String suf : getExtensions()) {
      if (suf.equals(ending.toLowerCase())) {
        return true;
      }
    }
    return false;
  }
  
  public boolean canHandle(String identifier) {
    return identifier.toLowerCase().startsWith(getName().toLowerCase() + "*") || 
           getType().equals(identifier) ||
           hasExtension(identifier) ||
           (new File(identifier).exists() && hasExtension(FilenameUtils.getExtension(identifier)));
  };
  
  protected void doInit(String[] args) throws Exception{
    // noop if not implemented
  };
  
  @Override
  public final boolean redirect(PipedInputStream stdout, PipedInputStream stderr) {
    synchronized(this) {
      boolean ret = false;
      
      if (!redirected) {
        init(null);
        ret = doRedirect(stdout, stderr);
        redirected = true;
      }
      
      return ret;
    }
  }
  
  protected boolean doRedirect(PipedInputStream stdout, PipedInputStream stderr) {
    // noop if not implemented
    return false;
  }
  
  @Override
  public int runScript(String scriptfile, String[] scriptArgs, Map<String, Object> options) {  
    logNotSupported("runScript");
    return -1;
  }

  @Override
  public int evalScript(String script, Map<String, Object> options) {
    logNotSupported("evalScript");
    return -1;
  }

  @Override
  public void runLines(String lines, Map<String, Object> options) {
    logNotSupported("runLines");
  }

  @Override
  public int runTest(URI scriptfile, URI imagedirectory, String[] scriptArgs, Map<String, Object> options) {
    logNotSupported("runTest");
    return -1;
  }

  @Override
  public int runInteractive(String[] scriptArgs) {
    logNotSupported("runInteractive");
    return -1;
  }

  @Override
  public String getCommandLineHelp() {
    logNotSupported("getCommandLineHelp");
    return null;
  }

  @Override
  public String getInteractiveHelp() {
    logNotSupported("getInteractiveHelp");
    return null;
  }

  @Override
  public boolean isSupported() {  
    return false;
  }
 
  @Override
  public final void close() {
    synchronized(this) {
      ready = false;
      doClose();      
    }   
  }
  
  protected void doClose() {
    // noop if not implemented
  } 
  
  @Override 
  public final void reset() {
    synchronized(this) {            
      try {      
        close();
        init(null);
        log(3, "reset requested (experimental: please report oddities)");
      } catch(Exception e) {     
        log(-1, "reset requested but did not work. Please report this case." +
                "Do not run scripts anymore and restart the IDE after having saved your work");
      }
    }   
  }
    
  @Override
  public void execBefore(String[] stmts) {
    logNotSupported("execBefore");
  }

  @Override
  public void execAfter(String[] stmts) {
    logNotSupported("execBefore");
  }
  
  @Override
  public boolean canBeDefault() {
     return false; 
  }  
}
