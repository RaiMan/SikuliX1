/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script.support.recorder.actions;

import org.sikuli.script.Pattern;

public abstract class PatternAction implements IRecordedAction {
  private Pattern pattern;

  public PatternAction(Pattern pattern) {
    super();
    this.pattern = pattern;
  }

  public Pattern getPattern() {
    return pattern;
  }

  public void setPattern(Pattern pattern) {
    this.pattern = pattern;
  }
}
