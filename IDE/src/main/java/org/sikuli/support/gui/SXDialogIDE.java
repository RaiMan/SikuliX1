package org.sikuli.support.gui;

import org.sikuli.ide.SikulixIDE;

import java.awt.*;

public class SXDialogIDE extends SXDialog {

  SikulixIDE sxide;

  public SXDialogIDE(String res, Point where, Object... parms) {
    super(res, where, parms);
    sxide = SikulixIDE.get();
  }

  public void runBackToIDE() {
    SikulixIDE.doShow();
  }

  public Rectangle getIdeWindow() {
    return sxide.getBounds();
  }
}
