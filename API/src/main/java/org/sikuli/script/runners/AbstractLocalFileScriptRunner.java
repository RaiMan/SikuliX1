/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script.runners;

import java.io.File;

import org.sikuli.script.ImagePath;
import org.sikuli.script.support.IScriptRunner;

public abstract class AbstractLocalFileScriptRunner extends AbstractScriptRunner {

  protected void prepareFileLocation(File scriptFile, IScriptRunner.Options options) {
    if (!options.isRunningInIDE() && scriptFile.exists()) {
        ImagePath.setBundleFolder(scriptFile.getParentFile());
    }
  }

  @Override
  public boolean canHandle(String identifier) {
    if (identifier != null) {
      /*
       * Test if we have a network protocol in front of
       * the identifier. In such a case we cannot handle
       * the identifier directly
       */
      int protoSepIndex = identifier.indexOf("://");
      if (protoSepIndex > 0 && protoSepIndex <= 5) {
        return false;
      }

      return super.canHandle(identifier);
    }

    return false;
  }
}
