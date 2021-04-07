/*
 * Copyright (c) 2010-2021, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.idesupport;

import org.sikuli.ide.SikulixIDE;
import org.sikuli.script.support.Commons;

import java.awt.*;
import java.net.URL;

public class IDEDialogStartUp extends SXDialog {

  public IDEDialogStartUp(Rectangle ideWindow) {
    super();
    if (asSingleton()) {
      setMargin(50);

      final String titleText = String.format("---  SikuliX-IDE  ---  %s  ---  starting on Java %s  ---",
          Commons.getSXVersion(), Commons.getJavaVersion());
      URL image = SikulixIDE.class.getResource("/icons/sikulix-red-x.png");

      appendY(new ImageItem(image).align(ALIGN.CENTER));
      appendY(new LineItem());
      appendY(new TextItem(titleText).setActive());
      appendY(new LineItem());
      appendY(new TextItem("Press any key to continue"));

      packLines(pane, lines);
      popup(ideWindow.getLocation());
    }
  }

  void keyHandler() {
    SikulixIDE.doShow();
  }
}
