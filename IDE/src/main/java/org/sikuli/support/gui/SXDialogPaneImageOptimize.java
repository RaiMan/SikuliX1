package org.sikuli.support.gui;

import org.sikuli.support.Commons;
import org.sikuli.support.devices.ScreenDevice;

import java.awt.*;
import java.awt.image.BufferedImage;

public class SXDialogPaneImageOptimize extends SXDialogIDE {
  public SXDialogPaneImageOptimize(Point where, Object... parms) {
    super("sxidepaneimageoptimize", where, parms);
    parent = (SXDialog) parms[parms.length - 1];
  }

  SXDialog parent = null;

  public void fullscreen() {
    ImageItem paneshot = (ImageItem) getItem("paneshot");
    BufferedImage shot = (BufferedImage) parent.globalStore.get("screenshot");
    Point topLeft = parent.getLocation();
    Rectangle rect = ScreenDevice.getScreenDeviceForPoint(topLeft).asRectangle();
    rect.x += 50;
    rect.y += 50;
    rect.width -= 100;
    rect.height -= 100;
    paneshot.set(adjustTo(rect, shot));
    where(rect.getLocation());
    reRun();
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
