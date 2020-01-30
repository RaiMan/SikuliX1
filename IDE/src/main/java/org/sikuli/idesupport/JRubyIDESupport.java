/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.idesupport;

import java.util.Set;

import org.sikuli.script.runners.JRubyRunner;
import org.sikuli.script.support.generators.ICodeGenerator;
import org.sikuli.script.support.generators.JythonCodeGenerator;

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
