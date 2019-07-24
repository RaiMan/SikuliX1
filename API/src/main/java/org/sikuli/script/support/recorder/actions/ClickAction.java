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

  protected Pattern getPattern() {
    return pattern;
  }

  protected String[] getModifiers() {
    return modifiers;
  }  
}
