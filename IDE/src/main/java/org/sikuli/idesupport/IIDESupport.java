/*
 * Copyright (c) 2010-2018, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.idesupport;

import org.sikuli.idesupport.IIndentationLogic;
import org.sikuli.script.support.generators.ICodeGenerator;

public interface IIDESupport {

	String[] getTypes();

	IIndentationLogic getIndentationLogic();

	String normalizePartialScript(String script);

	public ICodeGenerator getCodeGenerator();

}
