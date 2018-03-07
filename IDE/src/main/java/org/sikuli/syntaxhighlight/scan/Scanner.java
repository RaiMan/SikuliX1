/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.syntaxhighlight.scan;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Tal Liron
 */
public abstract class Scanner
{
	//
	// Construction
	//

	public Scanner( String text, int flags )
	{
		this.data = text;
		this.flags = flags;
		dataLength = data.length();
	}

	//
	// Attributes
	//

	public int getStartPos()
	{
		return startPos;
	}

	public int getPos()
	{
		return pos;
	}

	public String getLast()
	{
		return last;
	}

	public String getMatch()
	{
		return match;
	}

	public boolean isEos()
	{
		return pos >= dataLength;
	}

	public Matcher check( String pattern ) throws EndOfText
	{
		if( isEos() )
			throw new EndOfText();
		Pattern re = patternCache.get( pattern );
		if( re == null )
		{
			re = Pattern.compile( pattern, flags );
			patternCache.put( pattern, re );
		}
		return re.matcher( data.substring( pos ) );
	}

	public boolean test( String pattern ) throws EndOfText
	{
		return check( pattern ).matches();
	}

	public boolean scan( String pattern ) throws EndOfText
	{
		if( isEos() )
			throw new EndOfText();
		Pattern re = patternCache.get( pattern );
		if( re == null )
		{
			re = Pattern.compile( pattern, flags );
			patternCache.put( pattern, re );
		}
		last = match;
		Matcher matcher = re.matcher( data.substring( pos ) );
		if( !matcher.matches() )
			return false;
		startPos = matcher.start();
		pos = matcher.end();
		match = matcher.group();
		return true;
	}

	public boolean getChar() throws EndOfText
	{
		return scan( "." );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final Map<String, Pattern> patternCache = new HashMap<String, Pattern>();

	private final int flags;

	private final int dataLength;

	private String data;

	private int startPos, pos;

	private String match, last;
}
