/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.syntaxhighlight.grammar;

import java.util.ArrayList;
import java.util.List;

import org.sikuli.syntaxhighlight.NestedDef;

/**
 * @author Tal Liron
 */
public class State extends NestedDef<Grammar>
{
	//
	// Construction
	//

	public State( String name )
	{
		super();
		this.name = name;
	}

	public State( State state1, State state2 )
	{
		this( state1.getName() + "+" + state2.getName() );
		include( state1 );
		include( state2 );
	}

	//
	// Attributes
	//

	public String getName()
	{
		return name;
	}

	public List<Rule> getRules()
	{
		return rules;
	}

	//
	// Operations
	//

	public void addRule( Rule rule )
	{
		rules.add( rule );
	}

	public void addRuleAt( int location, Rule rule )
	{
		rules.add( location, rule );
	}

	public void include( State includedState )
	{
		rules.addAll( includedState.rules );
	}

	public void includeAt( int location, State includedState )
	{
		rules.addAll( location, includedState.rules );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final String name;

	private final List<Rule> rules = new ArrayList<Rule>();
}
