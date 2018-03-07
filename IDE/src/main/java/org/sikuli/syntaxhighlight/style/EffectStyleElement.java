/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.syntaxhighlight.style;

/**
 * @author Tal Liron
 */
public class EffectStyleElement extends StyleElement
{
	//
	// Constants
	//

	public static final EffectStyleElement Bold = create( "bold" );

	public static final EffectStyleElement NoBold = create( "nobold" );

	public static final EffectStyleElement Italic = create( "italic" );

	public static final EffectStyleElement NoItalic = create( "noitalic" );

	public static final EffectStyleElement Underline = create( "underline" );

	public static final EffectStyleElement NoUnderline = create( "nounderline" );

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	protected EffectStyleElement( String name )
	{
		super( name );
	}

	private static EffectStyleElement create( String name )
	{
		EffectStyleElement fontStyleElement = new EffectStyleElement( name );
		add( fontStyleElement );
		return fontStyleElement;
	}
}
