/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.syntaxhighlight.grammar;

/**
 * @author Tal Liron
 */
public class SaveRule extends Rule
{
	//
	// Construction
	//

	public SaveRule( State state )
	{
		super();
		this.state = state;
	}

	//
	// Attributes
	//

	public State getState()
	{
		return state;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final State state;
}
