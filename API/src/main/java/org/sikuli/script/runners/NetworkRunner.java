/*
 * Copyright (c) 2010-2018, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script.runners;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Map;

import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;
import org.sikuli.script.Sikulix;

public class NetworkRunner extends AbstractScriptRunner {

  @Override
  public int runScript(String scriptFile, String[] scriptArgs, Map<String,Object> options) {
      return -1;
    
//    String givenScriptFolder = scriptfile.getPath().substring(1);
//        
//    
//    if (givenScriptHost.contains("github.com")) {
//      givenScriptHost = "https://raw.githubusercontent.com";
//      givenScriptFolder = givenScriptFolder.replace("tree/", "");
//    } else {
//      givenScriptHost = "http://" + givenScriptHost;
//    }
//    if (givenScriptName.endsWith(".zip")) {
//      scriptLocation = givenScriptHost + givenScriptFolder + givenScriptName;
//      if (0 < FileManager.isUrlUseabel(scriptLocation)) {
//        Sikulix.terminate(999, ".zip from net not yet supported\n%s", scriptLocation);
//      }
//    } else {
//      for (String suffix : endingTypes.keySet()) {
//        String dlsuffix = "";
//        if (suffix != "js") {
//          dlsuffix = ".txt";
//        }
//        givenScriptScript = givenScriptName + "/" + givenScriptName + "." + suffix;
//        scriptLocation = givenScriptHost + "/" + givenScriptFolder + givenScriptScript;
//        givenScriptScriptType = runnerTypes.get(suffix);
//        if (0 < FileManager.isUrlUseabel(scriptLocation)) {
//          content = FileManager.downloadURLtoString(scriptLocation);
//          break;
//        } else if (!dlsuffix.isEmpty()) {
//          givenScriptScript = givenScriptName + "/" + givenScriptName + "." + suffix + dlsuffix;
//          scriptLocation = givenScriptHost + "/" + givenScriptFolder + givenScriptScript;
//          if (0 < FileManager.isUrlUseabel(scriptLocation)) {
//            content = FileManager.downloadURLtoString(scriptLocation);
//            break;
//          }
//        }
//        scriptLocation = givenScriptHost + "/" + givenScriptFolder + givenScriptName;
//      }
//      if (content != null && !content.isEmpty()) {
//        givenScriptType = "NET";
//        givenScriptScript = content;
//        givenScriptExists = true;
//        try {
//          uGivenScript = new URL(givenScriptHost + "/" + givenScriptFolder + givenScriptName);
//        } catch (Exception ex) {
//          givenScriptExists = false;
//        }
//      } else {
//        givenScriptExists = false;
//      }
//    }
//    if (!givenScriptExists) {
//      log(-1, "given script location not supported or not valid:\n%s", scriptLocation);
//    } else {
//      String header = "# ";
//      String trailer = "\n";
//      if (RJSCRIPT.equals(givenScriptScriptType)) {
//        header = "/*\n";
//        trailer = "*/\n";
//      }
//      header += scriptLocation + "\n";
//      if (Debug.is() > 2) {
//        FileManager.writeStringToFile(header + trailer + content,
//                new File(runTime.fSikulixStore, "LastScriptFromNet.txt"));
//      }
//    }
  }

  @Override
  public boolean isSupported() {   
    return true;
  }

  @Override
  public String getName() {
    return "NetworkRunner";
  }
  
  @Override 
  public boolean canHandle(String identifier) {       
    if (identifier != null && identifier.indexOf(":") > 5){
      String[] parts = identifier.split(":");
      if (parts.length > 1 && !parts[1].isEmpty()) {
        return true;
      }
    }
    
    return false;
  }

  @Override
  public String[] getExtensions() {
    // TODO Auto-generated method stub
    return new String[0];
  }

  @Override
  public String getType() {    
    return "NET";
  }
}
