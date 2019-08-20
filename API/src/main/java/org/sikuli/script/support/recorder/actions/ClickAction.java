/*
 * Copyright (c) 2010-2018, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script.support.recorder.actions;

import org.sikuli.script.Pattern;
import org.sikuli.script.support.generators.ICodeGenerator;

public class ClickAction implements IRecordedAction {
  private Pattern pattern;
  private String[] modifiers;

  public ClickAction(Pattern pattern, String[] modifiers) {
    this.pattern = pattern;
    this.modifiers = modifiers;
  }

  @Override
  public String generate(ICodeGenerator generator) {
    return generator.click(pattern, modifiers);
  }

  public Pattern getPattern() {
    return pattern;
  }

  public void setPattern(Pattern pattern) {
    this.pattern = pattern;
  }

  public String[] getModifiers() {
    return modifiers;
  }

  public void setModifiers(String[] modifiers) {
    this.modifiers = modifiers;
  }
}
