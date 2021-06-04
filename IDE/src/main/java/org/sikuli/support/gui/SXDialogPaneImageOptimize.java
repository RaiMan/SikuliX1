package org.sikuli.support.gui;

import org.sikuli.support.Commons;

import java.awt.*;

public class SXDialogPaneImageOptimize extends SXDialogIDE {
  public SXDialogPaneImageOptimize(Point where, Object... parms) {
    super("sxidepaneimageoptimize", where, parms);
  }

  public void fullscreen() {
    String feature = "fullscreen";
    toggleText(feature, "(⬆)", "(_)");
    Commons.error("SXDialogPaneImageOptimize::fullscreen: not implemented");
  }

  public void apply() {
    String feature = "apply";
    toggleText(feature, "(✔)", "(!!)");
    Commons.error("SXDialogPaneImageOptimize::applyclose: not implemented");
  }
}
