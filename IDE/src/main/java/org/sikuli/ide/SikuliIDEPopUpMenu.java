/*
 * Copyright (c) 2010-2018, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.ide;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;

import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;
import org.sikuli.script.*;
import org.sikuli.script.Sikulix;
import org.sikuli.idesupport.ScriptingSupport;

public class SikuliIDEPopUpMenu extends JPopupMenu {

  private static String me = "SikuliIDEPopUpMenu: ";
  private static int lvl = 3;

  private static void log(int level, String message, Object... args) {
    Debug.logx(level, me + message, args);
  }

  private String popType;
  private boolean validMenu = true;

  public static final String POP_TAB = "POP_TAB";
  private CloseableTabbedPane refTab;
  public static final String POP_IMAGE = "POP_IMAGE";
  private EditorPane refEditorPane = null;
  public static final String POP_LINE = "POP_LINE";
  private EditorLineNumberView refLineNumberView = null;

  private static String[] selOptionsTypes = null;

  private MouseEvent mouseTrigger;
  private int menuCount = 0;
  private Map<String, Integer> menus = new HashMap<String, Integer>();

  /**
   * Get the value of isValidMenu
   *
   * @return the value of isValidMenu
   */
  public boolean isValidMenu() {
    return validMenu;
  }

  public SikuliIDEPopUpMenu(String pType, Object ref) {
    popType = pType;
    init(ref);
  }

  private void init(Object ref) {
    if (popType.equals(POP_TAB)) {
      refTab = (CloseableTabbedPane) ref;
      popTabMenu();
//    } else if (popType.equals(POP_IMAGE)) {
//      refEditorPane = (EditorPane) ref;
//      popImageMenu();
    } else if (popType.equals(POP_LINE)) {
      refLineNumberView = (EditorLineNumberView) ref;
      refEditorPane = ((EditorLineNumberView) ref).getEditorPane();
      popLineMenu();
    } else {
      validMenu = false;
    }
    if (!validMenu) {
      return;
    }
  }

  public void doShow(CloseableTabbedPane comp, MouseEvent me) {
    mouseTrigger = me;
    show(comp, me.getX(), me.getY());
  }

  private void fireIDEFileMenu(String name) throws NoSuchMethodException {
    fireIDEMenu(SikulixIDE.getInstance().getFileMenu(), name);
  }

  private void fireIDERunMenu(String name) throws NoSuchMethodException {
    fireIDEMenu(SikulixIDE.getInstance().getRunMenu(), name);
  }

  private void fireIDEMenu(JMenu menu, String name) throws NoSuchMethodException {
    JMenuItem jmi;
    String jmiName = null;
    for (int i = 0; i < menu.getItemCount(); i++) {
      jmi = menu.getItem(i);
      if (jmi == null || jmi.getName() == null) {
        continue;
      }
      jmiName = jmi.getName();
      if (jmiName.equals(name)) {
        jmi.doClick();
      }
    }
    if (jmiName == null) {
      log(-1, "IDEFileMenu not found: " + name);
    }
  }

  private void fireInsertTabAndLoad(int tabIndex) {
    SikulixIDE.FileAction insertNewTab = SikulixIDE.getInstance().getFileAction(tabIndex);
    insertNewTab.doInsert(null);
  }

  private JMenuItem createMenuItem(JMenuItem item, ActionListener listener) {
    item.addActionListener(listener);
    return item;
  }

  private JMenuItem createMenuItem(String name, ActionListener listener) {
    return createMenuItem(new JMenuItem(name), listener);
  }

  private void createMenuSeperator() {
    menuCount++;
    addSeparator();
  }

  private void setMenuText(int index, String text) {
    ((JMenuItem) getComponent(index)).setText(text);
  }

  private String getMenuText(int index) {
    return ((JMenuItem) getComponent(index)).getText();
  }

  private void setMenuEnabled(int index, boolean enabled) {
    ((JMenuItem) getComponent(index)).setEnabled(enabled);
  }

  class MenuAction implements ActionListener {

    protected Method actMethod = null;
    protected String action;
    protected int menuPos;

    public MenuAction() {
    }

    public MenuAction(String item) throws NoSuchMethodException {
      Class[] paramsWithEvent = new Class[1];
      try {
        paramsWithEvent[0] = Class.forName("java.awt.event.ActionEvent");
        actMethod = this.getClass().getMethod(item, paramsWithEvent);
        action = item;
        menuPos = menuCount++;
        menus.put(item, menuPos);
      } catch (ClassNotFoundException cnfe) {
        log(-1, "Can't find menu action: %s\n" + cnfe, item);
      }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      if (actMethod != null) {
        try {
          log(lvl, "PopMenuAction." + action);
          Object[] params = new Object[1];
          params[0] = e;
          actMethod.invoke(this, params);
        } catch (Exception ex) {
          log(-1, "Problem when trying to invoke menu action %s\nError: %s",
                  action, ex.getMessage());
        }
      }
    }
  }

  private void popTabMenu() {
    try {
      add(createMenuItem("About", new PopTabAction(PopTabAction.ABOUT)));
      createMenuSeperator();
      add(createMenuItem("Set Type", new PopTabAction(PopTabAction.SET_TYPE)));
      createMenuSeperator();
      add(createMenuItem("Move Tab", new PopTabAction(PopTabAction.MOVE_TAB)));
      add(createMenuItem("Duplicate", new PopTabAction(PopTabAction.DUPLICATE)));
      add(createMenuItem("Open", new PopTabAction(PopTabAction.OPEN)));
      add(createMenuItem("Open left", new PopTabAction(PopTabAction.OPENL)));
      createMenuSeperator();
      add(createMenuItem("Save", new PopTabAction(PopTabAction.SAVE)));
      add(createMenuItem("SaveAs", new PopTabAction(PopTabAction.SAVE_AS)));
      createMenuSeperator();
      add(createMenuItem("Run", new PopTabAction(PopTabAction.RUN)));
      add(createMenuItem("Run Slowly", new PopTabAction(PopTabAction.RUN_SLOW)));
      createMenuSeperator();
      add(createMenuItem("Reset", new PopTabAction(PopTabAction.RESET)));

    } catch (NoSuchMethodException ex) {
      validMenu = false;
    }
  }

  class PopTabAction extends MenuAction {

    static final String ABOUT = "doAbout";
    static final String SET_TYPE = "doSetType";
    static final String MOVE_TAB = "doMoveTab";
    static final String DUPLICATE = "doDuplicate";
    static final String OPEN = "doOpen";
    static final String OPENL = "doOpenLeft";
    static final String SAVE = "doSave";
    static final String SAVE_AS = "doSaveAs";
    static final String RUN = "doRun";
    static final String RUN_SLOW = "doRunSlow";
    static final String RESET = "doReset";

    public PopTabAction() {
      super();
    }

    public PopTabAction(String item) throws NoSuchMethodException {
      super(item);
    }

    public void doAbout(ActionEvent ae) {
      Debug.log(3, "doAbout: selected");
      EditorPane cp = SikulixIDE.getInstance().getCurrentCodePane();
      String srcBundle = cp.getSrcBundle();
      String bundlePath = cp.getBundlePath();
      String currentFilename = cp.getCurrentFilename();
      String currentSrcDir = cp.getCurrentSrcDir();
      if (!cp.hasEditingFile()) {
        (new Thread() {
          @Override
          public void run() {
            Region at = Mouse.at().offset(100,52).grow(10);
            ((RobotDesktop) at.getScreen().getRobot()).putMouse(at.getCenter().x, at.getCenter().y + 20);
            Do.popup( "Script not yet saved","IDE: About: script info", "", false, 2, at);
          }
        }).start();
      } else {
        if (cp.isPython) {
          (new Thread() {
            @Override
            public void run() {
              String msg = String.format("Python script: %s\nFolder: %s\nImages: %s",
                      cp.getCurrentShortFilename(), cp.getSrcBundle(), cp.getImagePath());
              Region at = Mouse.at().offset(200,78).grow(10);
              ((RobotDesktop) at.getScreen().getRobot()).putMouse(at.getCenter().x, at.getCenter().y + 20);
              Do.popup( msg,"IDE: About: script info", "", false,10, at);
            }
          }).start();
        } else if (!cp.isText){
          (new Thread() {
            @Override
            public void run() {
              String msg = String.format("SikuliX script: %s\nin Folder: %s",
                      cp.getCurrentShortFilename(), new File(cp.getBundlePath()).getParent());
              Region at = Mouse.at().offset(100,65).grow(10);
              ((RobotDesktop) at.getScreen().getRobot()).putMouse(at.getCenter().x, at.getCenter().y + 20);
              Do.popup( msg,"IDE: About: script info", "", false, 10, at);
            }
          }).start();
        }
      }
    }

    public void doSetType(ActionEvent ae) {
      //TODO use a popUpSelect for more language options
      Debug.log(3, "doSetType: selected");
      String error = "";
      EditorPane cp = SikulixIDE.getInstance().getCurrentCodePane();
      if (selOptionsTypes == null) {
        String types = "";
        for (IScriptRunner runner : ScriptingSupport.getRunners()) {
          types += runner.getType().replaceFirst(".*?\\/", "") + " ";
        }
        if (!types.isEmpty()) {
          types = types.trim();
          selOptionsTypes = types.split(" ");
        }
      }
      if (null == selOptionsTypes) {
        return;
      }
      String currentType = cp.getSikuliContentType();
      Location mouseAt = new Location(mouseTrigger.getXOnScreen(), mouseTrigger.getYOnScreen());
      Sikulix.popat(mouseAt.offset(100, 85));
      String targetType = Sikulix.popSelect("Select the Content Type ...",
              selOptionsTypes, currentType.replaceFirst(".*?\\/", ""));
      if (targetType == null) {
        targetType = currentType;
      } else {
        targetType = "text/" + targetType;
      }
      if (currentType.equals(targetType)) {
        SikulixIDE.getStatusbar().setCurrentContentType(currentType);
        return;
      }
      String targetEnding = Runner.getExtension(targetType);
      if (cp.getText().length() > 0) {
        if (!Sikulix.popAsk(String.format(
                "Switch to %s requested, but tab is not empty!\n"
                        + "Click YES, to discard content and switch\n"
                        + "Click NO to cancel this action and keep content.",
                targetType))) {
          error = ": with errors";
        }
      }
      if (error.isEmpty()) {
        cp.reInit(targetEnding);
        cp.setText("");
        error = ": (" + targetType + ")";
      }
      String msg = "doSetType: completed" + error;
      SikulixIDE.getStatusbar().setMessage(msg);
      SikulixIDE.getStatusbar().setCurrentContentType(targetType);
      Debug.log(3, msg);
    }

    public void doMoveTab(ActionEvent ae) throws NoSuchMethodException {
      if (ae.getActionCommand().contains("Insert")) {
        log(lvl, "doMoveTab: entered at insert");
        doLoad(refTab.getSelectedIndex() + 1);
        resetMenuAfterMoveTab();
        return;
      }
      log(lvl, "doMoveTab: entered at move");
      refTab.resetLastClosed();
      if (SikulixIDE.getInstance().getCurrentCodePane().isSourceBundleTemp()) {
        log(-1, "Untitled tab cannot be moved");
        return;
      }
//      fireIDEFileMenu("SAVE");
      boolean success = refTab.fireCloseTab(mouseTrigger, refTab.getSelectedIndex());
      if (success && refTab.getLastClosed() != null) {
        refTab.isLastClosedByMove = true;
        setMenuText(menus.get(MOVE_TAB), "Insert Right");
        setMenuText(menus.get(OPENL), "Insert Left");
        log(lvl, "doMoveTab: preparation success");
      } else {
        log(-1, "doMoveTab: preperation aborted");
      }
    }

    private void checkAndResetMoveTab() throws NoSuchMethodException {
      if (refTab.isLastClosedByMove) {
        log(-1, "doMoveTab: is prepared and will be aborted");
        int currentTab = refTab.getSelectedIndex();
        doLoad(refTab.getSelectedIndex() + 1);
        refTab.setSelectedIndex(currentTab);
      }
      resetMenuAfterMoveTab();
    }

    private void resetMenuAfterMoveTab() {
      setMenuText(menus.get(MOVE_TAB), "Move Tab");
      setMenuText(menus.get(OPENL), "Open left");
      refTab.resetLastClosed();
    }

    public void doDuplicate(ActionEvent ae) throws NoSuchMethodException {
      log(lvl, "doDuplicate: entered");
      EditorPane ep = SikulixIDE.getInstance().getCurrentCodePane();
      checkAndResetMoveTab();
      fireIDEFileMenu("SAVE");
      if (ep.isSourceBundleTemp()) {
        log(-1, "Untitled tab cannot be duplicated");
        return;
      }
      String bundleOld = ep.getBundlePath();
      fireIDEFileMenu("SAVE_AS");
      if (FileManager.pathEquals(bundleOld, ep.getBundlePath())) {
        log(-1, "duplicate must use different project name");
        return;
      }
      setMenuText(menus.get(OPENL), "Insert left");
      doOpenLeft(null);
    }

    private boolean doLoad(int tabIndex) {
      boolean success = true;
      fireInsertTabAndLoad(tabIndex);
      return success;
    }

    public void doOpen(ActionEvent ae) throws NoSuchMethodException {
      log(lvl, "doOpen: entered");
      checkAndResetMoveTab();
      doLoad(refTab.getSelectedIndex() + 1);
    }

    public void doOpenLeft(ActionEvent ae) throws NoSuchMethodException {
      if (getMenuText(5).contains("Insert")) {
        log(lvl, "doOpenLeft: entered at insert left");
        doLoad(refTab.getSelectedIndex());
        resetMenuAfterMoveTab();
        return;
      }
      log(lvl, "doOpenLeft: entered");
      doLoad(refTab.getSelectedIndex());
    }

    public void doSave(ActionEvent ae) throws NoSuchMethodException {
      log(lvl, "doSave: entered");
      fireIDEFileMenu("SAVE");
    }

    public void doSaveAs(ActionEvent ae) throws NoSuchMethodException {
      log(lvl, "doSaveAs: entered");
      fireIDEFileMenu("SAVE_AS");
    }

    public void doRun(ActionEvent ae) throws NoSuchMethodException {
      log(lvl, "doRun: entered");
      fireIDERunMenu("RUN");
    }

    public void doRunSlow(ActionEvent ae) throws NoSuchMethodException {
      log(lvl, "doRunSlow: entered");
      fireIDERunMenu("RUN_SLOWLY");
    }

    public void doReset(ActionEvent ae) throws NoSuchMethodException {
      log(lvl, "Reset: entered");
      SikulixIDE.getInstance().clearMessageArea();
      checkAndResetMoveTab();
      ImagePath.reset();
      EditorPane cp = SikulixIDE.getInstance().getCurrentCodePane();
      cp.paneReset();
      cp.reparse();
      if (cp.isShouldReparse()) {
        cp.reparse();
      }
      cp.getRunner().reset();
    }
  }

  private void popImageMenu() {
    try {
      add(createMenuItem("copy", new PopImageAction(PopImageAction.COPY)));
    } catch (NoSuchMethodException ex) {
      validMenu = false;
    }
  }

  class PopImageAction extends MenuAction {

    static final String COPY = "doCopy";

    public PopImageAction() {
      super();
    }

    public PopImageAction(String item) throws NoSuchMethodException {
      super(item);
    }

    public void doCopy(ActionEvent ae) {
      //int caretPosition = refEditorPane.getCaretPosition();
      Element elementLine = refEditorPane.getLineAtCaret(-1);
      int elementCount = elementLine.getElementCount();
      int startOffset = elementLine.getStartOffset();
      int endOffset = elementLine.getEndOffset();
      String lineText = "###notvalid###";
      try {
        lineText = elementLine.getDocument().getText(startOffset, endOffset - startOffset);
      } catch (BadLocationException e) {
      }
      log(lvl, "Image: copy from: %s", lineText);
    }
  }

  private void popLineMenu() {
    try {
      add(createMenuItem("run line", new PopLineAction(PopLineAction.RUNLINE)));
      add(createMenuItem("run to line", new PopLineAction(PopLineAction.RUNTO)));
      add(createMenuItem("run from line", new PopLineAction(PopLineAction.RUNFROM)));
      add(createMenuItem("run selection", new PopLineAction(PopLineAction.RUNSEL)));
    } catch (NoSuchMethodException ex) {
      validMenu = false;
    }
  }

  class PopLineAction extends MenuAction {

    static final String RUNLINE = "doRunLine";
    static final String RUNTO = "doRunTo";
    static final String RUNFROM = "doRunFrom";
    static final String RUNSEL = "doRunSel";

    public PopLineAction() {
      super();
    }

    public PopLineAction(String item) throws NoSuchMethodException {
      super(item);
    }

    public void doRunLine(ActionEvent ae) {
      int current = refLineNumberView.getCurrentLine();
      log(lvl, "doRunLine: %d", current);
      refEditorPane.runLines(refEditorPane.getLines(current, null));
    }

    public void doRunTo(ActionEvent ae) {
      int current = refLineNumberView.getCurrentLine();
      log(lvl, "doRunToLine: %d", current);
      refEditorPane.runLines(refEditorPane.getLines(current, false));
    }

    public void doRunFrom(ActionEvent ae) {
      int current = refLineNumberView.getCurrentLine();
      log(lvl, "doRunFromLine: %d", current);
      String lines = refEditorPane.getLines(current, true);
      refEditorPane.runLines(lines);
    }

    public void doRunSel(ActionEvent ae) {
      refEditorPane.runSelection();
    }
  }
}
