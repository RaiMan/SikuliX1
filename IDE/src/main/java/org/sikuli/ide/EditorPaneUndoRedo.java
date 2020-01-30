/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.ide;

import javax.swing.*;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public class EditorPaneUndoRedo implements UndoableEditListener {

  private static final KeyStroke undoKeystroke = KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.META_DOWN_MASK);
  KeyStroke redoKeystroke = KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.META_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK);

  private UndoManager undoManager = null;
  private UndoAction undoAction = null;
  private RedoAction redoAction = null;
  private EditorPane editorPane = null;

  public EditorPaneUndoRedo(EditorPane pane) {
    undoManager = new UndoManager();
    undoAction = new UndoAction();
    redoAction = new RedoAction();
    editorPane = pane;

//    editorPane.getInputMap().put(undoKeystroke, "undoKeystroke");
//    editorPane.getActionMap().put("undoKeystroke", undoAction);
//
//    editorPane.getInputMap().put(redoKeystroke, "redoKeystroke");
//    editorPane.getActionMap().put("redoKeystroke", redoAction);
  }

  public UndoManager getUndoManager() {
    return undoManager;
  }

  public void undoableEditHappened(UndoableEditEvent e) {
    undoManager.addEdit(e.getEdit());
    undoAction.update();
    redoAction.update();
    SikulixIDE.get().updateUndoRedoStates();
  }

  public UndoAction getUndoAction() {
    return undoAction;
  }

  class UndoAction extends AbstractAction {
    public UndoAction() {
      super("Undo");
      setEnabled(false);
    }

    public void actionPerformed(ActionEvent e) {
      try {
        undoManager.undo();
      } catch (CannotUndoException ex) {
        // TODO deal with this
        //ex.printStackTrace();
      }
      update();
      redoAction.update();
    }

    protected void update() {
      if (undoManager.canUndo()) {
        setEnabled(true);
        putValue(Action.NAME, undoManager.getUndoPresentationName());
      } else {
        setEnabled(false);
        putValue(Action.NAME, "Undo");
      }
    }
  }

  public RedoAction getRedoAction() {
    return redoAction;
  }

  class RedoAction extends AbstractAction {
    public RedoAction() {
      super("Redo");
      setEnabled(false);
    }

    public void actionPerformed(ActionEvent e) {
      try {
        undoManager.redo();
      } catch (CannotRedoException ex) {
        // TODO deal with this
        ex.printStackTrace();
      }
      update();
      undoAction.update();
    }

    protected void update() {
      if (undoManager.canRedo()) {
        setEnabled(true);
        putValue(Action.NAME, undoManager.getRedoPresentationName());
      } else {
        setEnabled(false);
        putValue(Action.NAME, "Redo");
      }
    }
  }

}
