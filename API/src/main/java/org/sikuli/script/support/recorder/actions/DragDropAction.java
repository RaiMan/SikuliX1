/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script.support.recorder.actions;

import org.sikuli.script.Pattern;
import org.sikuli.script.support.generators.ICodeGenerator;

public class DragDropAction implements IRecordedAction {
  private Pattern sourcePattern;
  private Pattern targetPattern;

  public DragDropAction(Pattern sourcePattern, Pattern targetPattern) {
    this.sourcePattern = sourcePattern;
    this.targetPattern = targetPattern;
  }

  @Override
  public String generate(ICodeGenerator generator) {
    return generator.dragDrop(sourcePattern, targetPattern);
  }
}
