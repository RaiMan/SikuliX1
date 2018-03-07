/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.syntaxhighlight;

/**
 * @author Tal Liron
 */
public class ResolutionException extends Exception
{
	//
	// Construction
	//

	public ResolutionException( String message )
	{
		super( message );
	}

	public ResolutionException( String message, Throwable cause )
	{
		super( message, cause );
	}

	public ResolutionException( Throwable cause )
	{
		super( cause );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final long serialVersionUID = 1L;
}
