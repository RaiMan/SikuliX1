package org.sikuli.script.support.gui;

import org.sikuli.script.support.Commons;

import java.awt.*;

public class SXDialogPaneImage extends SXDialog {
  public SXDialogPaneImage(String res, Point where, Object... parms) {
    super(res, where, parms);
  }

  public void rename() {
    Commons.error("PaneImage: not implemented: rename");
  }
}
