/*
 * Copyright (c) 2010-2021, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.support.ide.syntaxhighlight.grammar.def;

import org.sikuli.support.ide.syntaxhighlight.Def;
import org.sikuli.support.ide.syntaxhighlight.ResolutionException;
import org.sikuli.support.ide.syntaxhighlight.grammar.Grammar;
import org.sikuli.support.ide.syntaxhighlight.grammar.Rule;
import org.sikuli.support.ide.syntaxhighlight.grammar.State;

/**
 * @author Tal Liron
 */
public class IncludeDef extends StateDef
{
	public IncludeDef( String stateName, String includedStateName )
	{
		super( stateName );
		this.includedStateName = includedStateName;
	}

	//
	// Def
	//

	@Override
	public boolean resolve( Grammar grammar ) throws ResolutionException
	{
		State state = grammar.getState( stateName );
		State includedState = grammar.getState( includedStateName );

		// Only include a resolved state
		if( includedState.isResolved() )
		{
			if( placeHolder != null )
			{
				int location = state.getRules().indexOf( placeHolder );
				state.getRules().remove( placeHolder );
				state.includeAt( location, includedState );
			}
			else
				state.include( includedState );

			resolved = true;
			return true;
		}
		else if( placeHolder == null )
		{
			// Remember location
			placeHolder = new Rule();
			state.addRule( placeHolder );
		}

		return false;
	}

	@Override
	public Def<Grammar> getCause( Grammar grammar )
	{
		return grammar.getState( includedStateName ).getCause( grammar );
	}

	//
	// Object
	//

	@Override
	public String toString()
	{
		return super.toString() + " " + stateName + ", " + includedStateName;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final String includedStateName;
}
