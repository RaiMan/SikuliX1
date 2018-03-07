/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.syntaxhighlight.grammar.def;

import org.sikuli.syntaxhighlight.Def;
import org.sikuli.syntaxhighlight.grammar.Grammar;
import org.sikuli.syntaxhighlight.grammar.Rule;

public abstract class StateDef extends Def<Grammar>
{
	//
	// Construction
	//

	public StateDef( String stateName )
	{
		this.stateName = stateName;
	}

	//
	// Attributes
	//

	public String getStateName()
	{
		return stateName;
	}

	//
	// Object
	//

	@Override
	public String toString()
	{
		return super.toString() + " " + stateName;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	protected final String stateName;

	protected Rule placeHolder = null;
}
