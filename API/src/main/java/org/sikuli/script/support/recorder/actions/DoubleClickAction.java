/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script.support.recorder.actions;

import org.sikuli.script.Pattern;
import org.sikuli.script.support.generators.ICodeGenerator;

public class DoubleClickAction extends ClickAction {

  public DoubleClickAction(Pattern pattern, String[] modifiers) {
    super(pattern, modifiers);
  }

  @Override
  public String generate(ICodeGenerator generator) {
    return generator.doubleClick(getPattern(), getModifiers());
  }
}
