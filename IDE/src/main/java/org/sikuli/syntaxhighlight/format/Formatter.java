/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.syntaxhighlight.format;

import java.io.IOException;
import java.io.Writer;

import org.sikuli.syntaxhighlight.Jygments;
import org.sikuli.syntaxhighlight.ResolutionException;
import org.sikuli.syntaxhighlight.grammar.Token;
import org.sikuli.syntaxhighlight.style.Style;

/**
 * @author Tal Liron
 */
public abstract class Formatter
{
	//
	// Static operations
	//

	public static Formatter getByName( String name ) throws ResolutionException
	{
		if( Character.isLowerCase( name.charAt( 0 ) ) )
			name = Character.toUpperCase( name.charAt( 0 ) ) + name.substring( 1 ) + "Formatter";

		Formatter formatter = getByFullName( name );
		if( formatter != null )
			return formatter;
		else
		{
			// Try contrib package
			String pack = Jygments.class.getPackage().getName() + ".contrib";
			formatter = getByFullName( pack + "." + name );
			if( formatter == null )
			{
				// Try this package
				pack = Formatter.class.getPackage().getName();
				formatter = getByFullName( pack + "." + name );
			}
			return formatter;
		}
	}

	public static Formatter getByFullName( String fullName ) throws ResolutionException
	{
		try
		{
			return (Formatter) Jygments.class.getClassLoader().loadClass( fullName ).newInstance();
		}
		catch( InstantiationException x )
		{
		}
		catch( IllegalAccessException x )
		{
		}
		catch( ClassNotFoundException x )
		{
		}

		return null;
	}

	public Formatter( Style style, boolean full, String title, String encoding )
	{
		this.style = style;
		this.title = title != null ? title : "";
		this.encoding = encoding != null ? encoding : "utf8";
	}

	public Style getStyle()
	{
		return style;
	}

	public String getTitle()
	{
		return title;
	}

	public String getEncoding()
	{
		return encoding;
	}

	public abstract void format( Iterable<Token> tokenSource, Writer writer ) throws IOException;

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final Style style;

	private final String title;

	private final String encoding;
}
