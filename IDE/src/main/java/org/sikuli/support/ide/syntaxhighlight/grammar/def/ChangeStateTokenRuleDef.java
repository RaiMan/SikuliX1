/*
 * Copyright (c) 2010-2021, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.support.ide.syntaxhighlight.grammar.def;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.sikuli.support.ide.syntaxhighlight.ResolutionException;
import org.sikuli.support.ide.syntaxhighlight.grammar.Grammar;
import org.sikuli.support.ide.syntaxhighlight.grammar.Rule;
import org.sikuli.support.ide.syntaxhighlight.grammar.State;
import org.sikuli.support.ide.syntaxhighlight.grammar.TokenRule;
import org.sikuli.support.ide.syntaxhighlight.grammar.TokenType;

/**
 * @author Tal Liron
 */
public class ChangeStateTokenRuleDef extends TokenRuleDef
{
	//
	// Construction
	//

	public ChangeStateTokenRuleDef( String stateName, String pattern, int flags, List<String> tokenTypeNames, List<String> nextStateNames )
	{
		super( stateName, pattern, flags, tokenTypeNames );
		this.nextStateNames = nextStateNames;
	}

	public ChangeStateTokenRuleDef( String stateName, String pattern, int flags, String[] tokenTypeNames, String... nextStateNames )
	{
		super( stateName, pattern, flags, tokenTypeNames );
		ArrayList<String> list = new ArrayList<>(nextStateNames.length);
		for( String nextStateName : nextStateNames )
			list.add( nextStateName );
		this.nextStateNames = list;
	}

	//
	// Def
	//

	@Override
	public boolean resolve( Grammar grammar ) throws ResolutionException
	{
		if( grammar.resolveStates( nextStateNames ) != null )
			return super.resolve( grammar );
		else
		{
			if( placeHolder == null )
			{
				placeHolder = new Rule();
				State state = grammar.getState( stateName );
				state.addRule( placeHolder );
			}
			return false;
		}
	}

	//
	// TokenRuleDef
	//

	@Override
	protected TokenRule createTokenRule( Pattern pattern, List<TokenType> tokenTypes, Grammar grammar ) throws ResolutionException
	{
		return new TokenRule( pattern, tokenTypes, grammar.resolveStates( nextStateNames ) );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final List<String> nextStateNames;
}
