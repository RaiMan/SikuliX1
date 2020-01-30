/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script.support.recorder.actions;

import org.sikuli.script.Pattern;
import org.sikuli.script.support.generators.ICodeGenerator;

public class MouseWheelAction extends PatternAction implements IRecordedAction {
  private int direction;
  private int steps;
  private String[] modifiers;
  private long stepDelay;

  public MouseWheelAction(Pattern pattern, int direction, int steps, String[] modifiers, long stepDelay) {
    super(pattern);
    this.direction = direction;
    this.steps = steps;
    this.modifiers = modifiers;
    this.stepDelay = stepDelay;
  }

  @Override
  public String generate(ICodeGenerator generator) {
    return generator.wheel(getPattern(), direction, steps, modifiers, stepDelay);
  }
}
