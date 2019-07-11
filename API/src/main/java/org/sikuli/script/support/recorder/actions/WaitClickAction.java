package org.sikuli.script.support.recorder.actions;

import org.sikuli.script.support.generators.ICodeGenerator;

public class WaitClickAction implements IRecordedAction {
  private String image;

  public WaitClickAction(String image) {
    this.image = image;
  }

  @Override
  public String generate(ICodeGenerator generator) {
    return "wait(\"" + image + "\").click()";
  }
}