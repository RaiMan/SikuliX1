package org.sikuli.support.ide.autocomplete;

import org.sikuli.basics.Debug;
import org.sikuli.ide.*;
import org.sikuli.support.runner.IRunner;

import javax.swing.text.Caret;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractCompleter implements IAutoCompleter {

  protected void log(int level, String message, Object... args) {
    Debug.logx(level, getName() + "Completer: " + message, args);
  }

  EditorPane pane = null;

  @Override
  public void setPane(EditorPane pane) {
    this.pane = pane;
    init();
  }

  private List<String> commandMenu = null;
  private SikuliIDEPopUpMenu popCompletion = null;

  private void init() {
    if (commandMenu == null) {
      commandMenu = getCommandMenu();
      popCompletion = null; //TODO new SikuliIDEPopUpMenu("POP_COMPLETION", pane, commandMenu);
      if (!popCompletion.isValidMenu()) {
        popCompletion = null;
      }
      doInit();
    }
  }

  void doInit() {

  }

  @Override
  public IRunner getRunner() {
    return pane.getRunner();
  }

  @Override
  public void handle(Caret caret, int start, int pos, String lineText) {
    Point cPoint = caret.getMagicCaretPosition();
    if (null == cPoint) {
      cPoint = new Point(0, 0);
    }
    int lineNumber = pane.getLineNumberAtCaret(caret.getDot());
    String text = lineText.substring(0, pos - start);
    String rest = lineText.substring(pos - start);
    //log(3, "handle %d at (%d,%d): %s", lineNumber, cPoint.x, cPoint.y, text + " # " + rest);
    int yOff = 20;
    if (pos > 0 && pos - start == 0) {
      yOff += yOff;
    }
    if (popCompletion != null) {
      //TODO popCompletion.showCompletion(pane, cPoint.x, cPoint.y + yOff);
    }
  }

  static String _I(String key, Object... args) {
    return "";
  }

  private static final String[][] getMenuCommands() {
    String[][] CommandsOnToolbar = {
            {"----"}, {"Find"},
            {"find"}, {"PATTERN"},
            {"findAll"}, {"PATTERN"},
            {"wait"}, {"PATTERN", "[timeout]"},
            {"waitVanish"}, {"PATTERN", "[timeout]"},
            {"exists"}, {"PATTERN", "[timeout]"},
            {"----"}, {"Mouse"},
            {"click"}, {"PATTERN", "[modifiers]"},
            {"doubleClick"}, {"PATTERN", "[modifiers]"},
            {"rightClick"}, {"PATTERN", "[modifiers]"},
            {"hover"}, {"PATTERN"},
            {"dragDrop"}, {"PATTERN", "PATTERN", "[modifiers]"},
            {"----"}, {"Keyboard"},
            {"type"}, {"_text", "[modifiers]"},
            {"type"}, {"PATTERN", "_text", "[modifiers]"},
            {"paste"}, {"_text", "[modifiers]"},
            {"paste"}, {"PATTERN", "_text", "[modifiers]"},
            {"----"}, {"Observer"},
            {"onAppear"}, {"PATTERN", "_hnd"},
            {"onVanish"}, {"PATTERN", "_hnd"},
            {"onChange"}, {"_hnd"},
            {"observe"}, {"[time]", "[background]"}
    };
    return CommandsOnToolbar;
  }

  private List<String> getCommandMenu() {
    String[][] CommandsOnToolbar = getMenuCommands();
    List<String> menu = new ArrayList<>();
    for (int i = 0; i < CommandsOnToolbar.length; i++) {
      String cmd = CommandsOnToolbar[i++][0];
      String[] params = CommandsOnToolbar[i];
      if (cmd.equals("----")) {
        menu.add("_" + params[0]);
      } else {
        menu.add(getTextRepresentation(cmd, params));
      }
    }
    return menu;
  }

  static String getTextRepresentation(String cmd, String[] params) {
    String ret = "" + cmd + "(";
    int count = 0;
    for (String p : params) {
      ret += p;
      if (++count < params.length) {
        ret += ", ";
      }
    }
    ret += ")";
    return ret;
  }

  private void insertCommand(String cmd, String[] params) {
    int endPos = -1, endPosLen = 0;
    boolean first = true;
    pane.insertString(cmd + "(");
    for (String p : params) {
      if (p.equals("PATTERN")) {
        if (!first) {
          pane.insertString(", ");
        } else {
          first = false;
        }
        continue;
      }
      if (!p.startsWith("[")) {
        if (!first) {
          pane.insertString(", ");
        }
        if (p.startsWith("_")) {
          endPos = pane.getCaretPosition();
          p = p.substring(1);
        }
        endPosLen = p.length();
        pane.insertString(p);
        first = false;
      }
    }
    pane.insertString(")");
    (new SikuliEditorKit.InsertBreakAction()).insertBreak(pane);
    if (endPos >= 0) {
      pane.requestFocus();
      pane.setCaretPosition(endPos);
      pane.setSelectionStart(endPos);
      pane.setSelectionEnd(endPos + endPosLen);
      Debug.log(5, "sel: " + pane.getSelectedText());
    }
    pane.requestFocus();
  }

}
