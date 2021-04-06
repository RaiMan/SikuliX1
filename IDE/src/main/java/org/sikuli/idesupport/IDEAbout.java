/*
 * Copyright (c) 2010-2021, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.idesupport;

import org.sikuli.ide.SikulixIDE;
import org.sikuli.script.support.Commons;

import java.awt.*;
import java.net.URL;

public class IDEAbout extends SXDialog {

  public IDEAbout() {
    super();
    asSingleton();
    setDialogSize(300, 500);
    setMargin(10);
    setAlign(ALIGN.CENTER);
    setFontSize(16);
    setLineSpace(10);

    URL image = SikulixIDE.class.getResource("/icons/sikulix-red-x.png");
    //appendY(new ImageItem(image).resize(200));
    appendY(new LineItem(200));
    //appendY(new TextItem("SikuliX IDE"));
    //appendY(new TextItem(Commons.getSXVersion()));
    //appendY(new TextItem("Java " + Commons.getJavaVersion()));

    packLines(pane, lines);
    Dimension size = getDialogSize();
    popup(SikulixIDE.getWindowCenter());
  }
}
