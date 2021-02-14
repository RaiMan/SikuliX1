/*
 * Copyright (c) 2010-2021, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.idesupport;

import org.sikuli.script.runners.JRubyRunner;

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
		throw new UnsupportedOperationException("Not supported yet.");
		//To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public String normalizePartialScript(String script) {
		//TODO run partial script: normalize lines for Ruby
		return script;
	}

}
