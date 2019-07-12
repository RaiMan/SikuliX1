package org.sikuli.script.support.recorder.actions;

import org.sikuli.script.support.generators.ICodeGenerator;

public interface IRecordedAction {
  String generate(ICodeGenerator generator);
}
