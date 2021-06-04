package org.sikuli.support.gui;

import org.sikuli.ide.SikulixIDE;

import java.awt.*;

public class SXDialogIDE extends SXDialog {

  public SXDialogIDE(String res, Point where, Object... parms) {
    super(res, where, parms);
  }

  public void runBackToIDE() {
    SikulixIDE.doShow();
  }
}
