/*
 * Copyright (c) 2010-2021, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.support.ide.syntaxhighlight.contrib;

import java.io.IOException;
import java.io.Writer;

import org.sikuli.support.ide.syntaxhighlight.format.Formatter;
import org.sikuli.support.ide.syntaxhighlight.grammar.Token;
import org.sikuli.support.ide.syntaxhighlight.style.Style;

/**
 * @author Tal Liron
 */
public class DebugFormatter extends Formatter
{
	//
	// Construction
	//

	public DebugFormatter()
	{
		this( null, false, null, null );
	}

	public DebugFormatter( Style style, boolean full, String title, String encoding )
	{
		super( style, full, title, encoding );
	}

	//
	// Formatter
	//

	@Override
	public void format( Iterable<Token> tokenSource, Writer writer ) throws IOException
	{
		for( Token token : tokenSource )
			writer.write( token.getPos() + " " + token.getType() + ": " + token.getValue() + "\n" );
		writer.flush();
	}
}
