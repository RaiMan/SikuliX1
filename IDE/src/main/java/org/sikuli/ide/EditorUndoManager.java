/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.ide;

import javax.swing.event.DocumentEvent;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;
import java.util.ArrayList;

import org.sikuli.basics.Debug;

class MyCompoundEdit extends CompoundEdit {
   boolean isUnDone=false;
   public int getLength() {
      return edits.size();
   }

	 @Override
   public void undo() throws CannotUndoException {
      super.undo();
      isUnDone=true;
   }
	 @Override
   public void redo() throws CannotUndoException {
      super.redo();
      isUnDone=false;
   }
	 @Override
   public boolean canUndo() {
      return edits.size()>0 && !isUnDone;
   }

	 @Override
   public boolean canRedo() {
      return edits.size()>0 && isUnDone;
   }

}

public class EditorUndoManager extends AbstractUndoableEdit
                  implements UndoableEditListener {
  private static final String me = "EditorUndoManager: ";
   String lastEditName=null;
   ArrayList<MyCompoundEdit> edits=new ArrayList<MyCompoundEdit>();
   MyCompoundEdit current;
   int pointer=-1;

	@Override
   public void undoableEditHappened(UndoableEditEvent e) {
      UndoableEdit edit=e.getEdit();
      if (edit instanceof AbstractDocument.DefaultDocumentEvent) {
         AbstractDocument.DefaultDocumentEvent event=(AbstractDocument.DefaultDocumentEvent)edit;
         int start=event.getOffset();
         int len=event.getLength();
         Debug.log(9, "undoableEditHappened " + start + "," + len);
         boolean isNeedStart=false;
         if(event.getType().equals(DocumentEvent.EventType.CHANGE) ||
            event.getType().equals(DocumentEvent.EventType.INSERT) ){
            try {
               String text=event.getDocument().getText(start, len);
               if (text.contains("\n"))
                  isNeedStart=true;
            } catch (BadLocationException e1) {
              Debug.error(me + "undoableEditHappened: Problem getting text\n%s", e1.getMessage());
            }
         }

         if (current==null) {
            isNeedStart=true;
         }
         else if (lastEditName==null || !lastEditName.equals(edit.getPresentationName())) {
            isNeedStart=true;
         }

         while (pointer<edits.size()-1) {
            edits.remove(edits.size()-1);
            isNeedStart=true;
         }
         if (isNeedStart) {
            createCompoundEdit();
         }

         current.addEdit(edit);
         lastEditName=edit.getPresentationName();

         refreshControls();
      }
   }

   public void createCompoundEdit() {
      if (current==null) {
         current= new MyCompoundEdit();
      }
      else if (current.getLength()>0) {
         current= new MyCompoundEdit();
      }

      edits.add(current);
      pointer++;
   }

	@Override
   public void undo() throws CannotUndoException {
      if (!canUndo()) {
         throw new CannotUndoException();
      }

      MyCompoundEdit u=edits.get(pointer);
      u.undo();
      pointer--;

      refreshControls();
   }

	@Override
   public void redo() throws CannotUndoException {
      if (!canRedo()) {
         throw new CannotUndoException();
      }

      pointer++;
      MyCompoundEdit u=edits.get(pointer);
      u.redo();

      refreshControls();
   }

	@Override
   public boolean canUndo() {
      return pointer>=0;
   }

	@Override
   public boolean canRedo() {
      return edits.size()>0 && pointer<edits.size()-1;
   }

   public void refreshControls() {
      SikuliIDE.getInstance().updateUndoRedoStates();
   }
}
