/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.syntaxhighlight.grammar;

/**
 * @author Tal Liron
 */
public class Token
{
	//
	// Construction
	//

	public Token( int pos, TokenType tokenType, String value )
	{
		this.pos = pos;
		this.tokenType = tokenType;
		this.value = value;
	}

	//
	// Attributes
	//

	public int getPos()
	{
		return pos;
	}

	public TokenType getType()
	{
		return tokenType;
	}

	public String getValue()
	{
		return value;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final int pos;

	private final TokenType tokenType;

	private final String value;
}
