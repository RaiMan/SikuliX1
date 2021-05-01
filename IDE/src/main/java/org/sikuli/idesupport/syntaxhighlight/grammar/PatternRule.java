/*
 * Copyright (c) 2010-2021, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.idesupport.syntaxhighlight.grammar;

import java.util.regex.Pattern;

/**
 * @author Tal Liron
 */
public abstract class PatternRule extends Rule
{
	//
	// Construction
	//

	public PatternRule( Pattern pattern )
	{
		this.pattern = pattern;
	}

	//
	// Attributes
	//

	public Pattern getPattern()
	{
		return pattern;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final Pattern pattern;
}
