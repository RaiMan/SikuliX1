package org.sikuli.script.support.gui;

import org.apache.commons.io.FilenameUtils;
import org.sikuli.ide.EditorImageButton;
import org.sikuli.ide.PatternWindow;
import org.sikuli.ide.SikulixIDE;
import org.sikuli.script.Region;
import org.sikuli.script.SX;
import org.sikuli.script.support.Commons;

import java.awt.*;
import java.io.File;

public class SXDialogPaneImageOptimize extends SXDialogIDE {
  public SXDialogPaneImageOptimize(Point where, Object... parms) {
    super("sxidepaneimageoptimize", where, parms);
  }

  public void fullscreen() {
    Commons.error("SXDialogPaneImageOptimize::fullscreen: not implemented");
  }

  public void applyclose() {
    Commons.error("SXDialogPaneImageOptimize::applyclose: not implemented");
  }
}
