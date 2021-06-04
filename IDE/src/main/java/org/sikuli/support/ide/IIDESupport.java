/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.support.ide;

import org.sikuli.support.recorder.generators.ICodeGenerator;

public interface IIDESupport {

  @SuppressWarnings("serial")
  public static class IncompleteStringException extends Exception {
    int lineNumber;

    public IncompleteStringException(int lineNumber) {
      super();
      this.lineNumber = lineNumber;
    }

    public int getLineNumber() {
      return lineNumber;
    }
  }

	String[] getTypes();

	IIndentationLogic getIndentationLogic();

	String normalizePartialScript(String script);

	public ICodeGenerator getCodeGenerator();
}
