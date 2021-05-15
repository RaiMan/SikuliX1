package org.sikuli.script.support.gui;

import org.sikuli.script.support.Commons;

import java.awt.*;

public class SXDialogPaneImageMenu extends SXDialog {
  public SXDialogPaneImageMenu(Point where, Object... parms) {
    super("sxidepaneimagemenu", where, parms);
  }

  public void rename() {
    Commons.error("PaneImage: not implemented: rename");
  }
}
