/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script.support.recorder.actions;

import org.sikuli.script.Pattern;
import org.sikuli.script.support.generators.ICodeGenerator;

public class MouseUpAction extends PatternAction implements IRecordedAction {
  private String[] buttons;

  public MouseUpAction(Pattern pattern, String[] buttons) {
    super(pattern);
    this.buttons = buttons;
  }

  @Override
  public String generate(ICodeGenerator generator) {
    return generator.mouseUp(getPattern(), buttons);
  }
}
