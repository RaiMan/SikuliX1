/*
 * Copyright (c) 2010-2018, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script.runners;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;
import org.sikuli.script.IScriptRunner;
import org.sikuli.script.ImagePath;
import org.sikuli.script.RunTime;
import org.sikuli.script.Runner;
import org.sikuli.script.Sikulix;

public class NetworkRunner extends AbstractScriptRunner {

  @Override
  protected int doRunScript(String scriptFile, String[] scriptArgs, Map<String, Object> options) {

    try {

      URL scriptURL = new URL(scriptFile);

      String path = scriptURL.getPath();

      if (path.endsWith("/")) {
        path = path.substring(0, path.length() - 1);
      }

      String basename = FilenameUtils.getBaseName(path);

      String host = scriptURL.getHost();

      if (host.contains("github.com")) {
        host = "https://raw.githubusercontent.com";
        path = path.replace("tree/", "");
      } else {
        host = scriptFile.substring(0, scriptFile.indexOf(path));               
      }

      String identifier = host + path;
      
      for (IScriptRunner runner : Runner.getRunners()) {

        if (!SikulixRunner.NAME.equals(runner.getName())) { // SikuliX bundles can't be downloaded directly 
          for (String extension : runner.getExtensions()) {

            String url;

            if (identifier.endsWith("." + extension)) {
              url = identifier;
            } else {
              url = identifier + "/" + basename + "." + extension;
            }
                        
            if (FileManager.isUrlUseabel(url) > 0) {
              String content = FileManager.downloadURLtoString(url);

              if (content != null && !content.isEmpty()) {

                String identifierParent = url.substring(0, url.lastIndexOf("/"));

                ImagePath.addHTTP(identifierParent);
                int retval = runner.evalScript(content, null);
                ImagePath.removeHTTP(identifierParent);

                if (Debug.is() > 2) {
                  FileManager.writeStringToFile(content,
                      new File(RunTime.get().fSikulixStore, "LastScriptFromNet.txt"));
                }

                return retval;
              }
            }
          }
        }
      }
    } catch (MalformedURLException e) {
      log(-1, "Invalid URL:\n%s", scriptFile);
      return -1;
    }

    log(-1, "given script location not supported or not valid:\n%s", scriptFile);
    return -1;

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
    if (identifier != null && identifier.indexOf("://") <= 5) {
      String[] parts = identifier.split("://");
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
