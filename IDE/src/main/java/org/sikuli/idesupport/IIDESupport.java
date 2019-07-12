/*
 * Copyright (c) 2010-2018, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.idesupport;

import org.sikuli.idesupport.IIndentationLogic;
import org.sikuli.script.support.generators.ICodeGenerator;

public interface IIDESupport {

	public String[] getTypes();

	public IIndentationLogic getIndentationLogic();

	public ICodeGenerator getCodeGenerator();

}
