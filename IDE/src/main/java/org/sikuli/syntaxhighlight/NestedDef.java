/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.syntaxhighlight;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tal Liron
 */
public class NestedDef<C> extends Def<C>
{
	//
	// Operations
	//

	public void addDef( Def<C> def )
	{
		defs.add( def );
	}

	//
	// Def
	//

	@Override
	public boolean resolve( C container ) throws ResolutionException
	{
		// Keep resolving until done
		boolean didSomething = false, keepGoing = true;
		while( keepGoing )
		{
			keepGoing = false;
			for( Def<C> def : new ArrayList<Def<C>>( defs ) )
			{
				if( !def.isResolved() )
				{
					if( def.resolve( container ) )
					{
						keepGoing = true;
						didSomething = true;
					}
				}
			}
		}

		// Are we resolved?
		resolved = true;
		for( Def<C> def : defs )
		{
			if( !def.isResolved() )
			{
				resolved = false;
				break;
			}
		}

		return didSomething;
	}

	@Override
	public Def<C> getCause( C container )
	{
		for( Def<C> def : defs )
			if( !def.isResolved() )
				return def;
		return null;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final List<Def<C>> defs = new ArrayList<Def<C>>();
}
