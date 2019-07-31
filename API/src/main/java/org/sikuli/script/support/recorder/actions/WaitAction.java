/*
 * Copyright (c) 2010-2018, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script.support.recorder.actions;

import org.sikuli.script.Pattern;
import org.sikuli.script.support.generators.ICodeGenerator;

public class WaitAction implements IRecordedAction {
  private Pattern pattern;
  private Integer seconds;
  IRecordedAction matchAction;

  public WaitAction(Pattern pattern, Integer seconds, IRecordedAction matchAction) {
    this.pattern = pattern;
    this.seconds = seconds;
    this.matchAction = matchAction;
  }

  @Override
  public String generate(ICodeGenerator generator) {
    return generator.wait(pattern, seconds, matchAction);
  }
}