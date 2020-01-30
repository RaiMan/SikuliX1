/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script.support.recorder.actions;

import org.sikuli.script.Pattern;
import org.sikuli.script.support.generators.ICodeGenerator;

public class WaitAction extends PatternAction implements IRecordedAction {
  private Integer seconds;
  IRecordedAction matchAction;

  public WaitAction(Pattern pattern, Integer seconds, IRecordedAction matchAction) {
    super(pattern);
    this.seconds = seconds;
    this.matchAction = matchAction;
  }

  @Override
  public String generate(ICodeGenerator generator) {
    return generator.wait(getPattern(), seconds, matchAction);
  }
}