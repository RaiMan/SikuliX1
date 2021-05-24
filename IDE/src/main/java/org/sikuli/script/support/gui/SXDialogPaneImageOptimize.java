package org.sikuli.script.support.gui;

import org.apache.commons.io.FilenameUtils;
import org.sikuli.ide.EditorImageButton;
import org.sikuli.ide.PatternWindow;
import org.sikuli.ide.SikulixIDE;
import org.sikuli.script.Region;
import org.sikuli.script.SX;

import java.awt.*;
import java.io.File;

public class SXDialogPaneImageOptimize extends SXDialog {
  public SXDialogPaneImageOptimize(Point where, Object... parms) {
    super("sxidepaneimage", where, parms);
  }

  public void rename() {
    closeCancel();
    final String image = FilenameUtils.getBaseName(((File) getOptions().get("image")).getAbsolutePath());
    final Region showAt = new Region(getLocation().x, getLocation().y, 1, 1);
    final String name = SX.input("New name for image " + image, "ImageButton :: rename", true);
    EditorImageButton.renameImage(name, getOptions());
  }

  public void optimize() {
    closeCancel();
    Rectangle ideWindow = SikulixIDE.get().getBounds();
    new PatternWindow(getOptions().get("parm1"));
  }

  public void pattern() {
    closeCancel();
    new PatternWindow(getOptions().get("parm1"));
  }
}
