package org.sikuli.support.gui;

import java.awt.*;

public class SXDialogPaneImagePattern extends SXDialogIDE {
  public SXDialogPaneImagePattern(Point where, Object... parms) {
    super("sxidepaneimagepattern", where, parms);
  }

  public void optimize() {
    closeCancel();
  }
}
