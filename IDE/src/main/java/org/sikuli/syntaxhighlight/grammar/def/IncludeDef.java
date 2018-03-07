/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.syntaxhighlight.grammar.def;

import org.sikuli.syntaxhighlight.Def;
import org.sikuli.syntaxhighlight.ResolutionException;
import org.sikuli.syntaxhighlight.grammar.Grammar;
import org.sikuli.syntaxhighlight.grammar.Rule;
import org.sikuli.syntaxhighlight.grammar.State;

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
