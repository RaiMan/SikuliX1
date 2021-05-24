package org.sikuli.script.support.gui;

import org.apache.commons.io.FilenameUtils;
import org.sikuli.ide.EditorImageButton;
import org.sikuli.ide.PatternWindow;
import org.sikuli.script.Region;
import org.sikuli.script.SX;
import org.sikuli.script.Sikulix;
import org.sikuli.script.support.Commons;

import java.awt.*;
import java.io.File;

public class SXDialogPaneImage extends SXDialog {
  public SXDialogPaneImage(Point where, Object... parms) {
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
    new PatternWindow(getOptions().get("parm1"));
  }

  public void pattern() {
    closeCancel();
    new PatternWindow(getOptions().get("parm1"));
  }
}
