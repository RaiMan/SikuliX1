/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.idesupport;

import org.sikuli.idesupport.IIndentationLogic;

public interface IIDESupport {

	String[] getTypes();

	IIndentationLogic getIndentationLogic();

	String normalizePartialScript(String script);

	}
