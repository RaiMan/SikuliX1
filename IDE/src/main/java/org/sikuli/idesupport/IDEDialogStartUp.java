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
    asSingleton();
    setMargin(50);

    final String titleText = String.format("---  SikuliX-IDE  ---  %s  ---  starting on Java %s  ---",
        Commons.getSXVersion(), Commons.getJavaVersion());
    URL image = SikulixIDE.class.getResource("/icons/sikulix-red-x.png");
    appendY(new LineItem(300));
//    appendY(new ImageItem(image).align(ALIGN.CENTER).padT((100)));
//    appendY(new TextItem(titleText).padT(50));

    packLines(pane, lines);
    popup(ideWindow.getLocation());
  }

  void keyHandler() {
    SikulixIDE.getWindow().setVisible(true);
  }
}
