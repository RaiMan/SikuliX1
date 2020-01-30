/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script.support.recorder.actions;

import org.sikuli.script.Pattern;
import org.sikuli.script.support.generators.ICodeGenerator;

public class MouseMoveAction extends PatternAction implements IRecordedAction {
  public MouseMoveAction(Pattern pattern) {
    super(pattern);
  }

  @Override
  public String generate(ICodeGenerator generator) {
    return generator.mouseMove(getPattern());
  }
}
