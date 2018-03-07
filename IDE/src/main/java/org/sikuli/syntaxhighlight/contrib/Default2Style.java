/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.syntaxhighlight.contrib;

import org.sikuli.syntaxhighlight.ResolutionException;
import org.sikuli.syntaxhighlight.style.Style;

/**
 * @author Tal Liron
 */
public class Default2Style extends Style
{
	public Default2Style()
	{
		super();

		add( "Whitespace", "#bbbbbb" );
		add( "Comment", "italic", "#408080" );
		add( "Comment.Preproc", "noitalic", "#BC7A00" );

		add( "Keyword", "bold", "#008000" );
		add( "Keyword.Pseudo", "nobold" );
		add( "Keyword.Type", "nobold", "#B00040" );

		add( "Operator", "#666666" );
		add( "Operator.Word", "bold", "#AA22FF" );

		add( "Name.Builtin", "#008000" );
		add( "Name.Function", "#0000FF" );
		add( "Name.Class", "bold", "#0000FF" );
		add( "Name.Namespace", "bold", "#0000FF" );
		add( "Name.Exception", "bold", "#D2413A" );
		add( "Name.Variable", "#19177C" );
		add( "Name.Constant", "#880000" );
		add( "Name.Label", "#A0A000" );
		add( "Name.Entity", "bold", "#999999" );
		add( "Name.Attribute", "#7D9029" );
		add( "Name.Tag", "bold", "#008000" );
		add( "Name.Decorator", "#AA22FF" );

		add( "String", "#BA2121" );
		add( "String.Doc", "italic" );
		add( "String.Interpol", "bold", "#BB6688" );
		add( "String.Escape", "bold", "#BB6622" );
		add( "String.Regex", "#BB6688" );
		add( "String.Symbol", "#19177C" );
		add( "String.Other", "#008000" );
		add( "Number", "#666666" );

		add( "Generic.Heading", "bold", "#000080" );
		add( "Generic.Subheading", "bold", "#800080" );
		add( "Generic.Deleted", "#A00000" );
		add( "Generic.Inserted", "#00A000" );
		add( "Generic.Error", "#FF0000" );
		add( "Generic.Emph", "italic" );
		add( "Generic.Strong", "bold" );
		add( "Generic.Prompt", "bold", "#000080" );
		add( "Generic.Output", "#888" );
		add( "Generic.Traceback", "#04D" );

		add( "Error", "border", "#FF0000" );

		try
		{
			resolve();
		}
		catch( ResolutionException x )
		{
			throw new RuntimeException( x );
		}
	}
}
