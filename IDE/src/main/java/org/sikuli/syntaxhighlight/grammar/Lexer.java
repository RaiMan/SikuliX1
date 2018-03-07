/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.syntaxhighlight.grammar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.sikuli.syntaxhighlight.Filter;
import org.sikuli.syntaxhighlight.Jygments;
import org.sikuli.syntaxhighlight.ResolutionException;
import org.sikuli.syntaxhighlight.Util;
import org.sikuli.syntaxhighlight.grammar.def.ChangeStateTokenRuleDef;
import org.sikuli.syntaxhighlight.grammar.def.IncludeDef;
import org.sikuli.syntaxhighlight.grammar.def.TokenRuleDef;

/**
 * @author Tal Liron
 */
public class Lexer extends Grammar
{
	//
	// Static operations
	//
  private static ClassLoader cl = Jygments.class.getClassLoader();

	public static Lexer getByName( String name ) throws ResolutionException
	{
		if( ( name == null ) || ( name.length() == 0 ) )
			name = "Lexer";
		else if( Character.isLowerCase( name.charAt( 0 ) ) )
			name = Character.toUpperCase( name.charAt( 0 ) ) + name.substring( 1 ) + "Lexer";

		Lexer lexer = getByFullName( name );
		if( lexer != null )
			return lexer;
		else
		{
			// Try contrib package
			lexer = getByFullName( "LexerContrib", "", name );
			if( lexer == null )
			{
				// Try this package
				String pack = Lexer.class.getPackage().getName();
				lexer = getByFullName( pack, "", name );
			}
			return lexer;
		}
	}

	public static Lexer getByFullName( String name ) throws ResolutionException {
    return getByFullName("", "", name);
  }

	@SuppressWarnings("unchecked")
	public static Lexer getByFullName( String pack, String sub, String name ) throws ResolutionException
	{
    String fullname = name;
    if (!pack.isEmpty()) {
      if (!sub.isEmpty()) {
        fullname = pack + "." + sub + "." + fullname;
      } else {
        fullname = pack + "." + fullname;
      }
    }
    // Try cache
		Lexer lexer = lexers.get( fullname );
		if( lexer != null )
			return lexer;

		try
		{
      Class<Lexer> cLexer = (Class<Lexer>) cl.loadClass( fullname );
      Lexer iLexer = (Lexer) (cLexer.newInstance());
      return iLexer;
		}
		catch( Exception x )
		{
      //System.out.println("[error] Jygments: Lexer: problem loading class " + fullname);
		}

		InputStream stream = Util.getJsonFile(pack, sub, name, fullname);
		if( stream != null )
		{
			try
			{
				String converted = Util.rejsonToJson( stream );
				ObjectMapper objectMapper = new ObjectMapper();
				objectMapper.getFactory().configure( JsonParser.Feature.ALLOW_COMMENTS, true );
				Map<String, Object> json = objectMapper.readValue( converted, HashMap.class );
				Object className = json.get( "class" );
				if( className == null )
					className = "";

				lexer = getByName( className.toString() );
				lexer.addJson( json );
				lexer.resolve();

				if( lexer != null )
				{
					// Cache it
					Lexer existing = lexers.putIfAbsent( fullname, lexer );
					if( existing != null ) {
						lexer = existing;
					}
				}

				return lexer;
			}
			catch( JsonParseException x )
			{
				throw new ResolutionException( x );
			}
			catch( JsonMappingException x )
			{
				throw new ResolutionException( x );
			}
			catch( IOException x )
			{
				throw new ResolutionException( x );
			}
		}

		return null;
	}

	public static Lexer getForFileName( String fileName ) throws ResolutionException
	{
		if( lexerMap.isEmpty() )
		{
			try
			{
				File jarFile = new File( Jygments.class.getProtectionDomain().getCodeSource().getLocation().toURI() );
				JarInputStream jarInputStream = new JarInputStream( new FileInputStream( jarFile ) );
				try
				{
					for( JarEntry jarEntry = jarInputStream.getNextJarEntry(); jarEntry != null; jarEntry = jarInputStream.getNextJarEntry() )
					{
						if( jarEntry.getName().endsWith( Util.extJSON ) )
						{
							String lexerName = jarEntry.getName();
							// strip off the JSON file ending
							lexerName = lexerName.substring( 0, lexerName.length() - Util.extJSON.length() );
							Lexer lexer = Lexer.getByFullName( lexerName );
							for( String filename : lexer.filenames )
								if( filename.startsWith( "*." ) )
									lexerMap.put( filename.substring( filename.lastIndexOf( '.' ) ), lexer );
						}
					}
				}
				finally
				{
					jarInputStream.close();
				}
			}
			catch( URISyntaxException x )
			{
				throw new ResolutionException( x );
			}
			catch( FileNotFoundException x )
			{
				throw new ResolutionException( x );
			}
			catch( IOException x )
			{
				throw new ResolutionException( x );
			}
		}

		return lexerMap.get( fileName.substring( fileName.lastIndexOf( '.' ) ) );
	}

	//
	// Construction
	//

	public Lexer()
	{
		this( false, false, 4, "utf8" );
	}

	public Lexer( boolean stripNewlines, boolean stripAll, int tabSize, String encoding )
	{
		this.stripNewLines = stripNewlines;
		this.stripAll = stripAll;
		this.tabSize = tabSize;
	}

	//
	// Attributes
	//

	public List<Filter> getFilters()
	{
		return filters;
	}

	public boolean isStripNewLines()
	{
		return stripNewLines;
	}

	public void setStripNewLines( boolean stripNewLines )
	{
		this.stripNewLines = stripNewLines;
	}

	public boolean isStripAll()
	{
		return stripAll;
	}

	public void setStripAll( boolean stripAll )
	{
		this.stripAll = stripAll;
	}

	public int getTabSize()
	{
		return tabSize;
	}

	public void setTabSize( int tabSize )
	{
		this.tabSize = tabSize;
	}

	public void addFilter( Filter filter )
	{
		filters.add( filter );
	}

	public float analyzeText( String text )
	{
		return 0;
	}

	public Iterable<Token> getTokens( String text )
	{
		return getTokens( text, false );
	}

	public Iterable<Token> getTokens( String text, boolean unfiltered )
	{
		// text = text.replace( "\r\n", "\n" ).replace( "\r", "\n" );
		// if( stripAll )
		// text = text.trim();
		// if( stripNewLines )
		// text = text.replace( "\n", "" );
		if( tabSize > 0 )
		{
			// expand tabs
		}
		if( !text.endsWith( "\n" ) )
			text += "\n";
		Iterable<Token> tokens = getTokensUnprocessed( text );
		if( !unfiltered )
		{
			// apply filters
		}
		return tokens;
	}

	public Iterable<Token> getTokensUnprocessed( String text )
	{
		ArrayList<Token> list = new ArrayList<Token>( 1 );
		list.add( new Token( 0, TokenType.Text, text ) );
		return list;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	protected void addAlias( String alias )
	{
		aliases.add( alias );
	}

	protected void addFilename( String filename )
	{
		filenames.add( filename );
	}

	protected void addMimeType( String mimeType )
	{
		mimeTypes.add( mimeType );
	}

	protected void include( String stateName, String includedStateName )
	{
		getState( stateName ).addDef( new IncludeDef( stateName, includedStateName ) );
	}

	protected void rule( String stateName, String pattern, int flags, String tokenTypeName )
	{
		getState( stateName ).addDef( new TokenRuleDef( stateName, pattern, flags, tokenTypeName ) );
	}

	protected void rule( String stateName, String pattern, int flags, String tokenTypeName, String nextStateName )
	{
		getState( stateName ).addDef( new ChangeStateTokenRuleDef( stateName, pattern, flags, new String[]
		{
			tokenTypeName
		}, nextStateName ) );
	}

	protected void rule( String stateName, String pattern, int flags, String[] tokenTypeNames )
	{
		getState( stateName ).addDef( new TokenRuleDef( stateName, pattern, flags, tokenTypeNames ) );
	}

	protected void rule( String stateName, String pattern, int flags, String[] tokenTypeNames, String... nextStateNames )
	{
		getState( stateName ).addDef( new ChangeStateTokenRuleDef( stateName, pattern, flags, tokenTypeNames, nextStateNames ) );
	}

	protected void addJson( Map<String, Object> json ) throws ResolutionException
	{
		@SuppressWarnings("unchecked")
		List<String> filenames = (List<String>) json.get( "filenames" );
		if( filenames == null )
			return;
		for( String filename : filenames )
			addFilename( filename );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final ConcurrentMap<String, Lexer> lexers = new ConcurrentHashMap<String, Lexer>();

	private static final ConcurrentMap<String, Lexer> lexerMap = new ConcurrentHashMap<String, Lexer>();

	private final List<Filter> filters = new ArrayList<Filter>();

	private boolean stripNewLines;

	private boolean stripAll;

	private int tabSize;

	private final List<String> aliases = new ArrayList<String>();

	private final List<String> filenames = new ArrayList<String>();

	private final List<String> mimeTypes = new ArrayList<String>();
}
