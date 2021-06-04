/*
 * Copyright (c) 2010-2021, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.support.ide.syntaxhighlight.grammar;

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
