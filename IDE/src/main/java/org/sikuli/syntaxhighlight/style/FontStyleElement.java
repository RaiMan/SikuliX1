/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.syntaxhighlight.style;

/**
 * @author Tal Liron
 */
public class FontStyleElement extends StyleElement
{
	//
	// Constants
	//

	public static final FontStyleElement Roman = create( "roman" );

	public static final FontStyleElement Sans = create( "sans" );

	public static final FontStyleElement Mono = create( "mono" );

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	protected FontStyleElement( String name )
	{
		super( name );
	}

	private static FontStyleElement create( String name )
	{
		FontStyleElement fontStyleElement = new FontStyleElement( name );
		add( fontStyleElement );
		return fontStyleElement;
	}
}
