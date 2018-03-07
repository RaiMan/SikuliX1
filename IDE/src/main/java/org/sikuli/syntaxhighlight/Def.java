/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.syntaxhighlight;

/**
 * @author Tal Liron
 */
public abstract class Def<C>
{
	//
	// Attributes
	//

	public boolean isResolved()
	{
		return resolved;
	}

	public Def<C> getCause( C container )
	{
		return null;
	}

	//
	// Operations
	//

	public boolean resolve( C container ) throws ResolutionException
	{
		return false;
	}

	//
	// Object
	//

	@Override
	public String toString()
	{
		return getClass().getSimpleName();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	protected boolean resolved = false;
}
