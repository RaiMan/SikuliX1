package org.sikuli.script.support.recorder.actions;

import org.sikuli.script.Pattern;
import org.sikuli.script.support.generators.ICodeGenerator;

public class MouseDownAction implements IRecordedAction {
  private Pattern pattern;
  private String[] buttons;

  public MouseDownAction(Pattern pattern, String[] buttons) {
    this.pattern = pattern;
    this.buttons = buttons;
  }

  @Override
  public String generate(ICodeGenerator generator) {
    return generator.mouseDown(pattern, buttons);
  }
}
