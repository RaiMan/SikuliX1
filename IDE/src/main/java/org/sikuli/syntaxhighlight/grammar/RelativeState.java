/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.syntaxhighlight.grammar;

/**
 * @author Tal Liron
 */
public class RelativeState extends State
{
	//
	// Construction
	//

	public RelativeState( boolean push, int depth )
	{
		super( push ? "#push" : "#pop" + ( depth > 1 ? ":" + depth : "" ) );
		this.push = push;
		this.depth = depth;
	}

	//
	// Attributes
	//

	public boolean isPush()
	{
		return push;
	}

	public int getDepth()
	{
		return depth;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final boolean push;

	private final int depth;
}
