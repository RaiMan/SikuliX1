package org.sikuli.script.support.recorder.actions;

import org.sikuli.script.Pattern;
import org.sikuli.script.support.generators.ICodeGenerator;

public class ClickAction implements IRecordedAction {
  private Pattern pattern;

  public ClickAction(Pattern pattern) {
    this.pattern = pattern;
  }

  @Override
  public String generate(ICodeGenerator generator) {
    return generator.click(pattern);
  }
}
