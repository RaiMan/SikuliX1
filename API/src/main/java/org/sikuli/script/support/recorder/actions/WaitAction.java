package org.sikuli.script.support.recorder.actions;

import org.sikuli.script.Pattern;
import org.sikuli.script.support.generators.ICodeGenerator;

public class WaitAction implements IRecordedAction {
  private Pattern pattern;
  private int seconds;
  IRecordedAction matchAction;  

  public WaitAction(Pattern pattern, int seconds, IRecordedAction matchAction) {
    this.pattern = pattern;
    this.seconds = seconds;
    this.matchAction = matchAction;
  }

  @Override
  public String generate(ICodeGenerator generator) {
    return generator.wait(pattern, seconds, matchAction);
  }
}