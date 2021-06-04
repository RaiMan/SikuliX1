/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.support.ide;

import org.sikuli.support.runner.JRubyRunner;
import org.sikuli.support.recorder.generators.ICodeGenerator;
import org.sikuli.support.recorder.generators.JythonCodeGenerator;

/**
 * all methods from/for IDE, that are JRuby specific
 */
public class JRubyIDESupport implements IIDESupport {

	@Override
	public String[] getTypes() {
		return new String[]{JRubyRunner.TYPE};
	}

	@Override
	public IIndentationLogic getIndentationLogic() {
    return null;
	}

	@Override
	public String normalizePartialScript(String script) {
		//TODO run partial script: normalize lines for Ruby
		return script;
	}

	@Override
  public ICodeGenerator getCodeGenerator() {
	  return new JythonCodeGenerator();
  }
}
