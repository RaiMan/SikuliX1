/*
 * Copyright (c) 2010-2021, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.support.ide.syntaxhighlight;

import java.io.IOException;
import java.io.Writer;

import org.sikuli.support.ide.syntaxhighlight.format.Formatter;
import org.sikuli.support.ide.syntaxhighlight.grammar.Lexer;
import org.sikuli.support.ide.syntaxhighlight.grammar.Token;

/**
 * @author Tal Liron
 */
public abstract class Jygments
{
	//
	// Static operations
	//

	public static Iterable<Token> lex( String code, Lexer lexer )
	{
		return lexer.getTokens( code );
	}

	public static void format( Iterable<Token> tokens, Formatter formatter, Writer writer ) throws IOException
	{
		formatter.format( tokens, writer );
	}

	public static void highlight( String code, Lexer lexer, Formatter formatter, Writer writer ) throws IOException
	{
		format( lex( code, lexer ), formatter, writer );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private Jygments()
	{
	}
}
