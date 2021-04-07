/*
 * Copyright (c) 2010-2021, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.idesupport;

import org.sikuli.ide.SikulixIDE;
import org.sikuli.script.support.Commons;

import java.net.URL;

//import java.net.URL;

public class IDEAbout extends SXDialog {

  public IDEAbout(String res) {
    super(res, SikulixIDE.class);
  }

  public IDEAbout() {
    super();
    asSingleton();
    setDialogSize(300, 500);
    setMargin(10);
    setAlign(ALIGN.CENTER);
    setFontSize(16);
    setSpaceBefore(10);

    appendY(new ImageItem(SikulixIDE.class.getResource("/icons/sikulix-red-x.png")).resize(200));
    appendY(new LineItem());
    appendY(new TextItem("SikuliX IDE").bold());
    appendY(new TextItem(Commons.getSXVersion()).bold());
    appendY(new TextItem("(" + Commons.getSXBuild() + ")").fontSize(12));
    appendY(new TextItem("Java " + Commons.getJavaVersion()).bold());
    appendY(new LineItem());
    appendY(new LinkItem("sikulix.com", "https://sikulix.github.io"));
    appendY(new LineItem());
    appendY(new TextItem("Press any key or Click here").fontSize(12).setActive());

    packLines(pane, lines);
    popup(SikulixIDE.getWindowCenter());
  }
}
