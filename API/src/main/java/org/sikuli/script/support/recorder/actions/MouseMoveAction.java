package org.sikuli.script.support.recorder.actions;

import org.sikuli.script.Pattern;
import org.sikuli.script.support.generators.ICodeGenerator;

public class MouseMoveAction implements IRecordedAction {
  private Pattern pattern;

  public MouseMoveAction(Pattern pattern) {
    this.pattern = pattern;
  }

  @Override
  public String generate(ICodeGenerator generator) {
    return generator.mouseMove(pattern);
  }
}
