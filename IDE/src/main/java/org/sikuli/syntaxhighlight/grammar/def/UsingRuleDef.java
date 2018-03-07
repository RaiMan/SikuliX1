/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.syntaxhighlight.grammar.def;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.sikuli.syntaxhighlight.ResolutionException;
import org.sikuli.syntaxhighlight.grammar.Grammar;
import org.sikuli.syntaxhighlight.grammar.Lexer;
import org.sikuli.syntaxhighlight.grammar.State;
import org.sikuli.syntaxhighlight.grammar.UsingRule;

/**
 * @author Tal Liron
 */
public class UsingRuleDef extends StateDef
{
	//
	// Construction
	//

	public UsingRuleDef( String stateName, String pattern, String usingLexerName )
	{
		super( stateName );
		this.pattern = pattern;
		this.usingLexerName = usingLexerName;
	}

	//
	// Attributes
	//

	public String getPattern()
	{
		return pattern;
	}

	public String getUsingLexerName()
	{
		return usingLexerName;
	}

	//
	// Def
	//

	@Override
	public boolean resolve( Grammar grammar ) throws ResolutionException
	{
		Pattern pattern;
		try
		{
			pattern = Pattern.compile( this.pattern, Pattern.MULTILINE | Pattern.DOTALL );
		}
		catch( PatternSyntaxException x )
		{
			throw new ResolutionException( "RegEx syntax error: " + this.pattern, x );
		}

		Lexer usingLexer = Lexer.getByName( usingLexerName );
		UsingRule rule = new UsingRule( pattern, usingLexer );
		State state = grammar.getState( stateName );
		state.addRule( rule );

		resolved = true;
		return true;
	}

	//
	// Object
	//

	@Override
	public String toString()
	{
		return super.toString() + ", " + pattern + ", " + usingLexerName;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final String pattern;

	private final String usingLexerName;
}
