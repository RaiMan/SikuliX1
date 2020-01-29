/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script.runners;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.sikuli.basics.FileManager;
import org.sikuli.script.ImagePath;
import org.sikuli.script.support.IScriptRunner;
import org.sikuli.script.support.Runner;
import org.sikuli.util.AbortableScriptRunnerWrapper;

public class NetworkRunner extends AbstractScriptRunner {

  private AbortableScriptRunnerWrapper wrapper = new AbortableScriptRunnerWrapper();

  @Override
  protected int doRunScript(String scriptFile, String[] scriptArgs, IScriptRunner.Options options) {

    String scriptUrl = getScriptURL(scriptFile);

    if (null != scriptUrl) {
      File dir = null;

      try {
        dir = Files.createTempDirectory("sikulix").toFile();
        String localFile = FileManager.downloadURL(scriptUrl, dir.getAbsolutePath());
        if (localFile != null) {

          String identifierParent = scriptUrl.substring(0, scriptUrl.lastIndexOf("/"));

          ImagePath.addHTTP(identifierParent);

          IScriptRunner runner = Runner.getRunner(localFile);
          wrapper.setRunner(runner);
          int retval = runner.runScript(localFile, scriptArgs, options);
          ImagePath.removeHTTP(identifierParent);

          return retval;
        }
      } catch (IOException e) {
        log(-1, "Error creating tmpfile: %s", e.getMessage());
      } finally {
        wrapper.clearRunner();
        if (null != dir) {
          try {
            FileUtils.deleteDirectory(dir);
          } catch (IOException e) {
            log(-1, "Error deleting tmp dir %s: %s", dir, e.getMessage());
          }
        }
      }
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
    if (identifier != null) {
      int protoSepIndex = identifier.indexOf("://");

      if (protoSepIndex > 0 && protoSepIndex <= 5) {
        String[] parts = identifier.split("://");
        if (parts.length > 1 && !parts[1].isEmpty()) {
          return null != getScriptURL(identifier);
        }
      }
    }

    return false;
  }

  public String resolveRelativeFile(String script) {
    return script;
  }

  private String getScriptURL(String scriptFile) {
    URL scriptURL;
    try {
      scriptURL = new URL(scriptFile);

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
        for (String ending : runner.getFileEndings()) {

          String url;

          if (identifier.endsWith(ending)) {
            url = identifier;
          } else {
            url = identifier + "/" + basename + ending;
          }

          if (FileManager.isUrlUseabel(url) > 0) {
            return url;
          }
        }
      }
    } catch (MalformedURLException e) {
      log(-1, "Invalid URL:\n%s", scriptFile);
    }

    return null;
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

  @Override
  public boolean isAbortSupported() {
    return wrapper.isAbortSupported();
  }

  @Override
  protected void doAbort() {
    wrapper.doAbort();
  }
}
