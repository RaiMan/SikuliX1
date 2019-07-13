package org.sikuli.script.support.recorder.actions;

import org.sikuli.script.Pattern;
import org.sikuli.script.support.generators.ICodeGenerator;

public class WaitClickAction implements IRecordedAction {
  private Pattern pattern;
  private int seconds;

  public WaitClickAction(Pattern pattern, int seconds) {
    this.pattern = pattern;
    this.seconds = seconds;
  }

  @Override
  public String generate(ICodeGenerator generator) {
    return generator.waitClick(pattern, seconds);
  }
}