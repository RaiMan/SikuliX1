/*
 * Copyright (c) 2010-2021, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.idesupport;

import org.sikuli.ide.SikulixIDE;
import org.sikuli.script.support.gui.SXDialog;

public class IDEAbout extends SXDialog {

  public IDEAbout() {
    super("/Settings/sikulixabout.txt", SikulixIDE.getWindowTop(), POSITION.CENTERED);
  }
}
