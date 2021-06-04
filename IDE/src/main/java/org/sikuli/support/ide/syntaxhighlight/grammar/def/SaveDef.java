/*
 * Copyright (c) 2010-2021, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.support.ide.syntaxhighlight.grammar.def;

import org.sikuli.support.ide.syntaxhighlight.ResolutionException;
import org.sikuli.support.ide.syntaxhighlight.grammar.Grammar;
import org.sikuli.support.ide.syntaxhighlight.grammar.Rule;
import org.sikuli.support.ide.syntaxhighlight.grammar.SaveRule;
import org.sikuli.support.ide.syntaxhighlight.grammar.State;

/**
 * @author Tal Liron
 */
public class SaveDef extends StateDef
{
	public SaveDef( String stateName, String savedStateName )
	{
		super( stateName );
		this.savedStateName = savedStateName;
	}

	//
	// Def
	//

	@Override
	public boolean resolve( Grammar grammar ) throws ResolutionException
	{
		State state = grammar.getState( stateName );
		State savedState = grammar.getState( savedStateName );

		// Only include a resolved state
		if( savedState.isResolved() )
		{
			if( placeHolder != null )
			{
				int location = state.getRules().indexOf( placeHolder );
				state.getRules().remove( placeHolder );
				state.addRuleAt( location, new SaveRule( savedState ) );
			}
			else
				state.addRule( new SaveRule( savedState ) );

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

	//
	// Object
	//

	@Override
	public String toString()
	{
		return super.toString() + " " + stateName + ", " + savedStateName;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final String savedStateName;
}
