/*
 * Copyright (c) 2010-2021, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.idesupport;

import org.sikuli.ide.SikulixIDE;

public class IDEDialogStartUp extends SXDialog {

  public IDEDialogStartUp() {
    super("/Settings/sikulixstartup.txt", getIDEWindowTop(), POSITION.CENTERED);
  }
}
