/*
 * Copyright (c) 2010-2021, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.idesupport;

import org.sikuli.ide.SikulixIDE;
import org.sikuli.script.support.Commons;

import java.awt.*;
import java.net.URL;

//import java.net.URL;

public class IDEAbout extends SXDialog {

  public IDEAbout(String res) {
    super(res, SikulixIDE.class);
    asSingleton();

    packLines(pane, lines);
    Rectangle rect = SikulixIDE.getWindowRect();
    int x = rect.x + rect.width / 2;
    x -= finalSize.width / 2;
    int y = rect.y + 30;
    popup(new Point(x, y));
  }

  public IDEAbout() {
    this("/Settings/sikulixabout.txt");
  }
}
