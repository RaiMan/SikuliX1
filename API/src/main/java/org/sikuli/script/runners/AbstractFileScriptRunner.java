package org.sikuli.script.runners;

import java.io.File;

import org.sikuli.script.ImagePath;
import org.sikuli.script.support.IScriptRunner;

public abstract class AbstractFileScriptRunner extends AbstractScriptRunner {

  protected void prepareFileLocation(File scriptFile, IScriptRunner.Options options) {
    if (!options.isRunningInIDE() && scriptFile.exists()) {
        ImagePath.setBundleFolder(scriptFile.getParentFile());
    }
  }
}
