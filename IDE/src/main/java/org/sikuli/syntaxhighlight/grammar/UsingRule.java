/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.syntaxhighlight.grammar;

import java.util.regex.Pattern;

/**
 * @author Tal Liron
 */
public class UsingRule extends PatternRule
{
	//
	// Construction
	//

	public UsingRule( Pattern pattern, Lexer lexer )
	{
		super( pattern );
		this.lexer = lexer;
	}

	//
	// Attributes
	//

	public Lexer getLexer()
	{
		return lexer;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final Lexer lexer;
}
