/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.syntaxhighlight.grammar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sikuli.syntaxhighlight.Def;
import org.sikuli.syntaxhighlight.NestedDef;
import org.sikuli.syntaxhighlight.ResolutionException;

/**
 * @author Tal Liron
 */
public class Grammar extends NestedDef<Grammar>
{
	//
	// Attributes
	//

	public State getState( String stateName )
	{
		State state = statesByName.get( stateName );
		if( state == null )
		{
			state = new State( stateName );
			statesByName.put( stateName, state );
			addDef( state );
		}
		return state;
	}

	public State resolveState( String stateName ) throws ResolutionException
	{
		if( stateName.startsWith( "#pop" ) )
		{
			int depth = 1;
			if( stateName.length() > 4 )
			{
				String depthString = stateName.substring( 5 );
				try
				{
					depth = Integer.parseInt( depthString );
				}
				catch( NumberFormatException x )
				{
					throw new ResolutionException( x );
				}
			}
			return new RelativeState( false, depth );
		}
		else if( stateName.startsWith( "#push" ) )
		{
			int depth = 1;
			if( stateName.length() > 5 )
			{
				String depthString = stateName.substring( 6 );
				try
				{
					depth = Integer.parseInt( depthString );
				}
				catch( NumberFormatException x )
				{
					throw new ResolutionException( x );
				}
			}
			return new RelativeState( true, depth );
		}

		State state = getState( stateName );
		if( state.isResolved() )
			return state;
		else
			return null;
	}

	public List<State> resolveStates( List<String> stateNames ) throws ResolutionException
	{
		ArrayList<State> states = new ArrayList<State>();
		for( String stateName : stateNames )
		{
			String[] combinedStateName = stateName.split( "\\+" );
			if( combinedStateName.length > 1 )
			{
				State combinedState = null;

				for( String singleStateName : combinedStateName )
				{
					State state = resolveState( singleStateName );
					if( state == null )
						return null;

					if( combinedState == null )
						combinedState = state;
					else
						combinedState = new State( combinedState, state );
				}

				states.add( combinedState );
			}
			else
			{
				State state = resolveState( stateName );
				if( state == null )
					return null;

				states.add( state );
			}
		}

		return states;
	}

	//
	// Operations
	//

	public void resolve() throws ResolutionException
	{
		resolve( this );

		// Are we resolved?
		for( Map.Entry<String, State> entry : statesByName.entrySet() )
		{
			if( !entry.getValue().isResolved() )
			{
				String message = "Unresolved state: " + entry.getKey();
				Def<Grammar> cause = entry.getValue().getCause( this );
				while( cause != null )
				{
					message += ", cause: " + cause;
					cause = cause.getCause( this );
				}
				throw new ResolutionException( message );
			}
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final Map<String, State> statesByName = new HashMap<String, State>();
}
