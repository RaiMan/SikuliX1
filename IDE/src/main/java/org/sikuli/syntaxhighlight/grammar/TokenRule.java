/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.syntaxhighlight.grammar;

import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Tal Liron
 */
public class TokenRule extends PatternRule
{
	//
	// Construction
	//

	public TokenRule( Pattern pattern, List<TokenType> tokenTypes )
	{
		this( pattern, tokenTypes, (List<State>) null );
	}

	public TokenRule( Pattern pattern, List<TokenType> tokenTypes, List<State> nextStates )
	{
		super( pattern );
		this.nextStates = nextStates;
		this.tokenTypes = tokenTypes;
	}

	//
	// Attributes
	//

	public List<TokenType> getTokenTypes()
	{
		return tokenTypes;
	}

	public List<State> getNextStates()
	{
		return nextStates;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final List<TokenType> tokenTypes;

	private final List<State> nextStates;
}
