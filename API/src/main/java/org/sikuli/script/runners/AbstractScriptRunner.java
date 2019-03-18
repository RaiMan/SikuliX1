package org.sikuli.script.runners;

import java.io.File;
import java.io.PipedInputStream;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.sikuli.script.IScriptRunner;
import org.sikuli.script.SikuliXception;

public abstract class AbstractScriptRunner implements IScriptRunner {

  boolean ready = false;
  boolean redirected = false;
  
  @Override
  public final synchronized void init(String[] args) throws SikuliXception {
    if(!ready) {
      try {
        ready = false;
        doInit(args);
        ready = true;
      } catch (Exception e) {
        throw new SikuliXception("Cannot initialize Script runner " + this.getName(), e);        
      }
    }
  }
  
  public final synchronized boolean isReady() {
    return ready;
  }
    
  @Override
  public final String hasExtension(String ending) {
    for (String suf : getExtensions()) {
      if (suf.equals(ending.toLowerCase())) {
        return suf;
      }
    }
    return null;
  }
  
  public boolean canHandle(String identifier) {
    return identifier.startsWith(getName()) || getType().equals(identifier) ||  hasExtension(identifier) != null;
  };
  
  protected abstract void doInit(String[] args) throws Exception;
  
  @Override
  public final synchronized boolean redirect(PipedInputStream[] pin) {    
    boolean ret = false;
    
    if (!redirected) {
      init(null);
      ret = doRedirect(pin);
      redirected = true;
    }
    
    return ret;
  }
  
  protected boolean doRedirect(PipedInputStream[] pin) {
    return false;
  }

  
  
    
}
