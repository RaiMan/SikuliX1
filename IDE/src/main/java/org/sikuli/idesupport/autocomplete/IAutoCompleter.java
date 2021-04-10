package org.sikuli.idesupport.autocomplete;

import org.sikuli.ide.EditorPane;
import org.sikuli.script.runnerSupport.IScriptRunner;

import javax.swing.text.Caret;

public interface IAutoCompleter {

  String getName();

  void setPane(EditorPane pane);

  IScriptRunner getRunner();

  void handle(Caret caret, int line, int pos, String lineText);

}
