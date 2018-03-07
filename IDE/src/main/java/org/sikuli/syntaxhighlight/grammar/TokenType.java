/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.syntaxhighlight.grammar;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Tal Liron
 */
public class TokenType
{
	//
	// Constants
	//

	public static final TokenType Token = create( "Token", "" );

	// Main

	public static final TokenType Whitespace = create( "Whitespace", "w", Token );

	public static final TokenType Text = create( "Text", "", Token );

	public static final TokenType Error = create( "Error", "err", Token );

	public static final TokenType Other = create( "Other", "x", Token );

	// Keywords

	public static final TokenType Keyword = create( "Keyword", "k", Text );

	public static final TokenType Keyword_Constant = create( "Keyword.Constant", "kc", Keyword );

	public static final TokenType Keyword_Declaration = create( "Keyword.Declaration", "kd", Keyword );

	public static final TokenType Keyword_Namespace = create( "Keyword.Namespace", "kn", Keyword );

	public static final TokenType Keyword_Pseudo = create( "Keyword.Pseudo", "kp", Keyword );

	public static final TokenType Keyword_Reserved = create( "Keyword.Reserved", "kr", Keyword );

	public static final TokenType Keyword_Type = create( "Keyword.Type", "kt", Keyword );

	// Names

	public static final TokenType Name = create( "Name", "n", Text );

	public static final TokenType Name_Attribute = create( "Name.Attribute", "na", Name );

	public static final TokenType Name_Builtin = create( "Name.Builtin", "nb", Name );

	public static final TokenType Name_Builtin_Pseudo = create( "Name.Builtin.Pseudo", "bp", Name_Builtin );

	public static final TokenType Name_Class = create( "Name.Class", "nc", Name );

	public static final TokenType Name_Constant = create( "Name.Constant", "no", Name );

	public static final TokenType Name_Decorator = create( "Name.Decorator", "nd", Name );

	public static final TokenType Name_Entity = create( "Name.Entity", "ni", Name );

	public static final TokenType Name_Exception = create( "Name.Exception", "ne", Name );

	public static final TokenType Name_Function = create( "Name.Function", "nf", Name );

	public static final TokenType Name_Property = create( "Name.Property", "py", Name );

	public static final TokenType Name_Label = create( "Name.Label", "nl", Name );

	public static final TokenType Name_Namespace = create( "Name.Namespace", "nn", Name );

	public static final TokenType Name_Other = create( "Name.Other", "nx", Name );

	public static final TokenType Name_Tag = create( "Name.Tag", "nt", Name );

	public static final TokenType Name_Variable = create( "Name.Variable", "nv", Name );

	public static final TokenType Name_Variable_Class = create( "Name.Variable.Class", "vc", Name_Variable );

	public static final TokenType Name_Variable_Global = create( "Name.Variable.Global", "vg", Name_Variable );

	public static final TokenType Name_Variable_Instance = create( "Name.Variable.Instance", "vi", Name_Variable );

	// Literals

	public static final TokenType Literal = create( "Literal", "l", Text );

	public static final TokenType Literal_Date = create( "Literal.Date", "ld", Literal );

	// Strings

	public static final TokenType String = create( "String", "s", Text );

	public static final TokenType String_Backtick = create( "String.Backtick", "sb", String );

	public static final TokenType String_Char = create( "String.Char", "sc", String );

	public static final TokenType String_Doc = create( "String.Doc", "sd", String );

	public static final TokenType String_Double = create( "String.Double", "s2", String );

	public static final TokenType String_Escape = create( "String.Escape", "se", String );

	public static final TokenType String_Heredoc = create( "String.Heredoc", "sh", String );

	public static final TokenType String_Interpol = create( "String.Interpol", "si", String );

	public static final TokenType String_Other = create( "String.Other", "sx", String );

	public static final TokenType String_Regex = create( "String.Regex", "sr", String );

	public static final TokenType String_Single = create( "String.Single", "s1", String );

	public static final TokenType String_Symbol = create( "String.Symbol", "ss", String );

	// Numbers

	public static final TokenType Number = create( "Number", "m", Text );

	public static final TokenType Number_Float = create( "Number.Float", "mf", Number );

	public static final TokenType Number_Hex = create( "Number.Hex", "mh", Number );

	public static final TokenType Number_Integer = create( "Number.Integer", "mi", Number );

	public static final TokenType Number_Integer_Long = create( "Number.Integer.Long", "il", Number_Integer );

	public static final TokenType Number_Oct = create( "Number.Oct", "mo", Number );

	// Operators

	public static final TokenType Operator = create( "Operator", "o", Text );

	public static final TokenType Operator_Word = create( "Operator.Word", "ow", Operator );

	// Punctuation

	public static final TokenType Punctuation = create( "Punctuation", "p", Text );

	// Comments

	public static final TokenType Comment = create( "Comment", "c", Text );

	public static final TokenType Comment_Multiline = create( "Comment.Multiline", "cm", Comment );

	public static final TokenType Comment_Preproc = create( "Comment.Preproc", "cp", Comment );

	public static final TokenType Comment_Single = create( "Comment.Single", "c1", Comment );

	public static final TokenType Comment_Special = create( "Comment.Special", "cs", Comment );

	// Generics

	public static final TokenType Generic = create( "Generic", "g", Text );

	public static final TokenType Generic_Deleted = create( "Generic.Deleted", "gd", Generic );

	public static final TokenType Generic_Emph = create( "Generic.Emph", "ge", Generic );

	public static final TokenType Generic_Error = create( "Generic.Error", "gr", Generic );

	public static final TokenType Generic_Heading = create( "Generic.Heading", "gh", Generic );

	public static final TokenType Generic_Inserted = create( "Generic.Inserted", "gi", Generic );

	public static final TokenType Generic_Output = create( "Generic.Output", "go", Generic );

	public static final TokenType Generic_Prompt = create( "Generic.Prompt", "gp", Generic );

	public static final TokenType Generic_Strong = create( "Generic.Strong", "gs", Generic );

	public static final TokenType Generic_Subheading = create( "Generic.Subheading", "gu", Generic );

	public static final TokenType Generic_Traceback = create( "Generic.Traceback", "gt", Generic );

	//
	// Static attributes
	//

	public static TokenType getTokenTypeByName( String name )
	{
		return tokenTypesByName.get( name );
	}

	public static TokenType getTokenTypeByShortName( String shortName )
	{
		return tokenTypesByShortName.get( shortName );
	}

	public static Collection<TokenType> getTokenTypes()
	{
		return tokenTypesByName.values();
	}

	//
	// Attributes
	//

	public String getName()
	{
		return name;
	}

	public String getShortName()
	{
		return shortName;
	}

	public TokenType getParent()
	{
		return parent;
	}

	//
	// Object
	//

	@Override
	public String toString()
	{
		return name;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static Map<String, TokenType> tokenTypesByName;

	private static Map<String, TokenType> tokenTypesByShortName;

	private static final TokenType create( String name, String shortName )
	{
		return create( name, shortName, null );
	}

	private static final TokenType create( String name, String shortName, TokenType parent )
	{
		TokenType tokenType = new TokenType( name, shortName, parent );

		if( tokenTypesByName == null )
			tokenTypesByName = new HashMap<String, TokenType>();
		if( tokenTypesByShortName == null )
			tokenTypesByShortName = new HashMap<String, TokenType>();

		tokenTypesByName.put( name, tokenType );
		tokenTypesByShortName.put( shortName, tokenType );

		return tokenType;
	}

	private final String name;

	private final String shortName;

	private final TokenType parent;

	private TokenType( String name, String shortName, TokenType parent )
	{
		this.name = name;
		this.shortName = shortName;
		this.parent = parent;
	}
}
