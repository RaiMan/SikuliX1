/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.idesupport;

import java.util.Set;

import org.sikuli.idesupport.IIndentationLogic;
import org.sikuli.script.support.generators.ICodeGenerator;

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
