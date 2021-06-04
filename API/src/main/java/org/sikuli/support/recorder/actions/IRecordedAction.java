/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.support.recorder.actions;

import org.sikuli.support.recorder.generators.ICodeGenerator;

public interface IRecordedAction {
  String generate(ICodeGenerator generator);
}
