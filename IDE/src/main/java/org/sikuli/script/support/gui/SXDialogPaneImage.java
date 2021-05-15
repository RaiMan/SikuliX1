package org.sikuli.script.support.gui;

import org.sikuli.ide.PatternWindow;
import org.sikuli.script.support.Commons;

import java.awt.*;

public class SXDialogPaneImage extends SXDialog {
  public SXDialogPaneImage(Point where, Object... parms) {
    super("sxidepaneimage", where, parms);
  }

  public void rename() {
    closeCancel();
    new PatternWindow(getOptions().get("parm1"));
  }
}
