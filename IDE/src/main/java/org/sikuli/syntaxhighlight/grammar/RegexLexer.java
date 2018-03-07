/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.syntaxhighlight.grammar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sikuli.syntaxhighlight.ResolutionException;
import org.sikuli.syntaxhighlight.grammar.def.ChangeStateTokenRuleDef;
import org.sikuli.syntaxhighlight.grammar.def.SaveDef;
import org.sikuli.syntaxhighlight.grammar.def.TokenRuleDef;
import org.sikuli.syntaxhighlight.grammar.def.UsingRuleDef;

/**
 * @author Tal Liron
 */
public class RegexLexer extends Lexer
{
	// //////////////////////////////////////////////////////////////////////////
	// Protected

	@Override
	public List<Token> getTokensUnprocessed( String text )
	{
		List<Token> tokens = new ArrayList<Token>();

		// Start at root state
		LinkedList<State> stateStack = new LinkedList<State>();
		State state = getState( "root" );
		stateStack.add( state );

		int pos = 0;
		int length = text.length();
		while( pos < length )
		{
			int eol = text.indexOf( '\n', pos );
			// int endRegion = eol >= 0 ? eol + 2 : length;
			// if( endRegion > length )
			// endRegion = length;
			int endRegion = length;
			boolean matches = false;

			// Does any rule in the current state match at the current position?
			// System.out.println("Text: " + text.substring( pos ));
			for( Rule rule : new ArrayList<Rule>( state.getRules() ) )
			{
				if( rule instanceof PatternRule )
				{
					PatternRule patternRule = (PatternRule) rule;
					// System.out.println( "Trying pattern: " +
					// rule.getPattern().pattern() );
					Matcher matcher = patternRule.getPattern().matcher( text );
					// From current position to end of line
					// matcher.useTransparentBounds( true );
					matcher.region( pos, endRegion );
					if( matcher.lookingAt() )
					{
						// System.out.println( "Match! " + matcher.group() + " "
						// +
						// rule );

						// Yes, so apply it!
						if( rule instanceof TokenRule )
						{
							TokenRule tokenRule = (TokenRule) rule;
							List<TokenType> tokenTypes = tokenRule.getTokenTypes();
							if( tokenTypes.size() == 1 )
								// Single token
								tokens.add( new Token( pos, tokenTypes.get( 0 ), matcher.group() ) );
							else
							{
								if( tokenTypes.size() != matcher.groupCount() )
									throw new RuntimeException( "The number of token types in the rule does not match the number of groups in the regular expression" );

								// Multiple tokens by group
								int group = 1;
								for( TokenType tokenType : tokenTypes )
								{
									String value = matcher.group( group );
									// System.out.println( matcher.pattern() +
									// " " +
									// value + " " + tokenType );
									// pos = matcher.start( group );
									tokens.add( new Token( pos, tokenType, value ) );
									// pos = matcher.end( group );
									group++;
								}
							}

							// Change state
							List<State> nextStates = tokenRule.getNextStates();
							if( nextStates != null )
							{
								for( State nextState : nextStates )
								{
									if( nextState instanceof RelativeState )
									{
										RelativeState relativeState = (RelativeState) nextState;
										if( relativeState.isPush() )
											// Push
											stateStack.addLast( state );
										else
											// Pop
											for( int depth = relativeState.getDepth(); ( depth > 0 ) && !stateStack.isEmpty(); depth-- )
												state = stateStack.removeLast();
									}
									else
									{
										// Push and switch
										stateStack.addLast( state );
										state = nextState;
									}
								}
							}
							/*
							 * else { // Pop if( stateStack.size() > 1 ) state =
							 * stateStack.removeLast(); }
							 */
						}
						else if( rule instanceof UsingRule )
						{
							UsingRule usingRule = (UsingRule) rule;
							// System.err.println( "!!!!!!!" +
							// rule.getPattern().pattern() );
							// System.err.println( "!!!!!!!!!!!!!!" +
							// matcher.group().length() );
							Iterable<Token> usingTokens = usingRule.getLexer().getTokensUnprocessed( matcher.group() );
							for( Token usingToken : usingTokens )
								tokens.add( usingToken );
						}

						pos = matcher.end();
						// System.out.println( pos );
						matches = true;

						// Don't process other rules here
						break;
					}
				}
				else if( rule instanceof SaveRule )
				{
					SaveRule saveRule = (SaveRule) rule;
					State saveState = saveRule.getState();
					if( saveState != state )
					{
						saveState.getRules().clear();
						saveState.include( state );
					}
				}
			}

			if( !matches )
			{
				// tokens.add( new Token( pos, TokenType.Error, state.getName()
				// ) );
				if( pos != eol )
				{
					// Unmatched character
					tokens.add( new Token( pos, TokenType.Error, text.substring( pos, pos + 1 ) ) );
				}
				else
				{
					// Fallback for states that don't explicitly match new
					// lines.

					tokens.add( new Token( pos, TokenType.Text, "\n" ) );

					// Reset state stack
					/*
					 * state = getState( "root" ); stateStack.clear();
					 * stateStack.addLast( state );
					 */
				}

				pos += 1;
			}
		}

		return tokens;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void addJson( Map<String, Object> json ) throws ResolutionException
	{
		super.addJson( json );

		// Initialize constants
		Object constantsObject = json.get( "constants" );
		Map<String, List<String>> constants = new HashMap<String, List<String>>();
		if( constantsObject != null )
		{
			if( !( constantsObject instanceof Map<?, ?> ) )
				throw new ResolutionException( "\"constants\" must be a map" );

			for( Map.Entry<String, Object> entry : ( (Map<String, Object>) constantsObject ).entrySet() )
			{
				String constantName = entry.getKey();
				Object constantObject = entry.getValue();
				ArrayList<String> strings = new ArrayList<String>();
				constants.put( constantName, strings );
				if( constantObject instanceof List<?> )
				{
					StringBuilder pattern = new StringBuilder();
					for( String patternElement : (List<String>) constantObject )
						pattern.append( patternElement );
					strings.add( pattern.toString() );
				}
				else if( constantObject instanceof String )
					strings.add( (String) constantObject );
				else
					throw new ResolutionException( "Unexpected value in \"constants\" map: " + constantObject );
			}
		}

		// Flags
		int defaultFlags = Pattern.MULTILINE;
		Object flagsObject = json.get( "flags" );
		if( flagsObject != null )
		{
			if( !( flagsObject instanceof List<?> ) )
				throw new ResolutionException( "\"flags\" must be an array of strings" );

			for( Object flagObject : (List<?>) flagsObject )
			{
				if( !( flagObject instanceof String ) )
					throw new ResolutionException( "\"flags\" must be an array of strings" );

				String flag = (String) flagObject;
				if( flag.equalsIgnoreCase( "CANON_EQ" ) )
					defaultFlags |= Pattern.CANON_EQ;
				else if( flag.equalsIgnoreCase( "CASE_INSENSITIVE" ) || flag.equalsIgnoreCase( "IGNORECASE" ) )
					defaultFlags |= Pattern.CASE_INSENSITIVE;
				else if( flag.equalsIgnoreCase( "COMMENTS" ) )
					defaultFlags |= Pattern.COMMENTS;
				else if( flag.equalsIgnoreCase( "DOTALL" ) )
					defaultFlags |= Pattern.DOTALL;
				else if( flag.equalsIgnoreCase( "LITERAL" ) )
					defaultFlags |= Pattern.LITERAL;
				else if( flag.equalsIgnoreCase( "MULTILINE" ) )
					defaultFlags |= Pattern.MULTILINE;
				else if( flag.equalsIgnoreCase( "UNICODE_CASE" ) )
					defaultFlags |= Pattern.UNICODE_CASE;
				else if( flag.equalsIgnoreCase( "UNIX_LINES" ) )
					defaultFlags |= Pattern.UNIX_LINES;
				else
					throw new ResolutionException( "\"flags\" contains an unrecognized flag: " + flag );
			}
		}

		Object statesObject = json.get( "states" );
		if( statesObject == null )
			throw new ResolutionException( "Grammar does not contain \"states\" map" );

		if( !( statesObject instanceof Map<?, ?> ) )
			throw new ResolutionException( "\"states\" must be a map" );

		for( Map.Entry<String, Object> entry : ( (Map<String, Object>) statesObject ).entrySet() )
		{
			String stateName = entry.getKey();
			Object stateObject = entry.getValue();
			if( !( stateObject instanceof Iterable<?> ) )
				throw new ResolutionException( "State \"" + stateName + "\" must be an array" );

			for( Iterable<Object> arguments : (Iterable<Iterable<Object>>) stateObject )
			{
				List<Object> argumentsList = new ArrayList<Object>();
				for( Object argument : (List<Object>) arguments )
					argumentsList.add( argument );

				if( argumentsList.isEmpty() )
					throw new ResolutionException( "Entry in state \"" + stateName + "\" must have at least one argument" );

				Object command = argumentsList.get( 0 );
				if( !( command instanceof String ) )
					throw new ResolutionException( "Entry in state \"" + stateName + "\" must have a string as the first argument" );

				if( command.equals( "#include" ) )
				{
					if( argumentsList.size() != 2 )
						throw new ResolutionException( "\"#include\" command in state \"" + stateName + "\" must have a string as an argument" );

					Object includedState = argumentsList.get( 1 );
					if( !( includedState instanceof String ) )
						throw new ResolutionException( "\"#include\" command in state \"" + stateName + "\" must have a string as an argument" );

					include( stateName, (String) includedState );
				}
				else if( command.equals( "#using" ) )
				{
					if( argumentsList.size() != 3 )
						throw new ResolutionException( "\"#using\" command in state \"" + stateName + "\" must have two strings as arguments" );

					Object pattern = argumentsList.get( 1 );
					if( !( pattern instanceof String ) )
						throw new ResolutionException( "\"#using\" command in state \"" + stateName + "\" must have two strings as arguments" );

					Object usingLexerName = argumentsList.get( 2 );
					if( !( usingLexerName instanceof String ) )
						throw new ResolutionException( "\"#using\" command in state \"" + stateName + "\" must have two strings as arguments" );

					getState( stateName ).addDef( new UsingRuleDef( stateName, (String) pattern, (String) usingLexerName ) );
				}
				else if( command.equals( "#save" ) )
				{
					if( argumentsList.size() != 2 )
						throw new ResolutionException( "\"#save\" command in state \"" + stateName + "\" must have one string as an argument" );

					Object savedStateName = argumentsList.get( 1 );
					if( !( savedStateName instanceof String ) )
						throw new ResolutionException( "\"#save\" command in state \"" + stateName + "\" must have one string as an argument" );

					getState( stateName ).addDef( new SaveDef( stateName, (String) savedStateName ) );
				}
				else
				{
					// Command is a pattern
					String pattern = (String) command;

					if( pattern.startsWith( "#constant:" ) )
					{
						// Concatenate
						StringBuilder builder = new StringBuilder();
						String[] concatArguments = pattern.substring( 10 ).split( "," );
						for( String concatArgument : concatArguments )
						{
							List<String> strings = constants.get( concatArgument );
							if( strings == null )
								throw new ResolutionException( "Unknown constant \"" + concatArgument + "\" for #pattern in state \"" + stateName + "\" must have at least a token type as an argument" );
							for( String string : strings )
								builder.append( string );
						}
						pattern = builder.toString();
					}

					if( argumentsList.size() < 2 )
						throw new ResolutionException( "Rule in state \"" + stateName + "\" must have at least a token type as an argument" );

					Object tokenTypeNames = argumentsList.get( 1 );
					if( tokenTypeNames instanceof String )
					{
						ArrayList<String> list = new ArrayList<String>( 1 );
						list.add( (String) tokenTypeNames );
						tokenTypeNames = list;
					}

					if( !( tokenTypeNames instanceof List<?> ) )
						throw new ResolutionException( "Expected token type name or array of token type names in rule in state \"" + stateName + "\"" );

					if( argumentsList.size() == 2 )
					{
						// Token rule
						getState( stateName ).addDef( new TokenRuleDef( stateName, pattern, defaultFlags, (List<String>) tokenTypeNames ) );
					}
					else if( argumentsList.size() == 3 )
					{
						// Change state token rule
						Object nextStateNames = argumentsList.get( 2 );
						if( nextStateNames instanceof String )
						{
							ArrayList<String> list = new ArrayList<String>( 1 );
							list.add( (String) nextStateNames );
							nextStateNames = list;
						}

						if( !( nextStateNames instanceof List<?> ) )
							throw new ResolutionException( "Expected state name or array of state names in rule in state \"" + stateName + "\"" );

						getState( stateName ).addDef( new ChangeStateTokenRuleDef( stateName, pattern, defaultFlags, (List<String>) tokenTypeNames, (List<String>) nextStateNames ) );
					}
					else
						throw new ResolutionException( "Too many arguments for rule in state \"" + stateName + "\"" );
				}
			}
		}
	}
}
