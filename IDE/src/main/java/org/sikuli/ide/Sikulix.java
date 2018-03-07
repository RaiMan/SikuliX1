/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.ide;

import org.sikuli.scriptrunner.ScriptingSupport;

import javax.swing.*;
import java.security.CodeSource;

public class Sikulix {

  public static void main(String[] args) {
    String jarName = "";

    CodeSource codeSrc = SikuliIDE.class.getProtectionDomain().getCodeSource();
    if (codeSrc != null && codeSrc.getLocation() != null) {
      jarName = codeSrc.getLocation().getPath();
    }

    if (jarName.contains("sikulixsetupIDE")) {
      JOptionPane.showMessageDialog(null, "Not useable!\nRun setup first!",
              "sikulixsetupIDE", JOptionPane.ERROR_MESSAGE);
      System.exit(0);
    }

    if (args.length > 0 && args[0].startsWith("-test")) {
      ScriptingSupport.runscript(args);
      if (null == args[0]) {
        System.exit(1);
      }
      System.exit(0);
    }

    SikuliIDE.run(args);
  }
}
