/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.syntaxhighlight.style;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.sikuli.syntaxhighlight.Jygments;
import org.sikuli.syntaxhighlight.NestedDef;
import org.sikuli.syntaxhighlight.ResolutionException;
import org.sikuli.syntaxhighlight.Util;
import org.sikuli.syntaxhighlight.grammar.TokenType;
import org.sikuli.syntaxhighlight.style.def.StyleElementDef;

/**
 * @author Tal Liron
 */
public class Style extends NestedDef<Style>
{
	static String extJSON = ".jso";
	//
	// Static operations
	//

	public static Style getByName( String name ) throws ResolutionException
	{
		if( Character.isLowerCase( name.charAt( 0 ) ) )
			name = Character.toUpperCase( name.charAt( 0 ) ) + name.substring( 1 ) + "Style";

		Style style = getByFullName( name );
		if( style != null )
			return style;
		else
		{
			// Try contrib package
			String pack = Jygments.class.getPackage().getName();
			return getByFullName( pack, "contrib", name );
		}
	}

	public static Style getByFullName( String name ) throws ResolutionException {
    return getByFullName("", "", name);
  }

	@SuppressWarnings("unchecked")
	public static Style getByFullName( String pack, String sub, String name ) throws ResolutionException
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
		Style style = styles.get( fullname );
		if( style != null )
			return style;

		try
		{
			return (Style) Jygments.class.getClassLoader().loadClass( fullname ).newInstance();
		}
		catch( InstantiationException x )
		{
		}
		catch( IllegalAccessException x )
		{
		}
		catch( ClassNotFoundException x )
		{
		}

		InputStream stream = Util.getJsonFile(pack, sub, name, fullname);
		if( stream != null )
		{
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.getFactory().configure( JsonParser.Feature.ALLOW_COMMENTS, true );
			try
			{
				Map<String, Object> json = objectMapper.readValue( stream, HashMap.class );
				style = new Style();
				style.addJson( json );
				style.resolve();

				// Cache it
				Style existing = styles.putIfAbsent( fullname, style );
				if( existing != null )
					style = existing;

				return style;
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

	//
	// Attributes
	//

	public Map<TokenType, List<StyleElement>> getStyleElements()
	{
		return styleElements;
	}

	//
	// Operations
	//

	public void addStyleElement( TokenType tokenType, StyleElement styleElement )
	{
		List<StyleElement> styleElementsForTokenType = styleElements.get( tokenType );
		if( styleElementsForTokenType == null )
		{
			styleElementsForTokenType = new ArrayList<StyleElement>();
			styleElements.put( tokenType, styleElementsForTokenType );
		}
		styleElementsForTokenType.add( styleElement );
	}

	public void resolve() throws ResolutionException
	{
		resolve( this );
	}

	//
	// Def
	//

	@Override
	public boolean resolve( Style style ) throws ResolutionException
	{
		if( super.resolve( style ) )
		{
			boolean done = false;
			while( !done )
			{
				done = true;
				for( TokenType tokenType : TokenType.getTokenTypes() )
				{
					if( tokenType != TokenType.Token )
					{
						if( !styleElements.containsKey( tokenType ) )
						{
							boolean doneOne = false;
							TokenType parent = tokenType.getParent();
							while( parent != null )
							{
								if( parent == TokenType.Token )
								{
									doneOne = true;
									break;
								}

								List<StyleElement> parentElements = styleElements.get( parent );
								if( parentElements != null )
								{
									styleElements.put( tokenType, parentElements );
									doneOne = true;
									break;
								}

								parent = parent.getParent();
							}

							if( !doneOne )
								done = false;
						}
					}
				}
			}

			return true;
		}
		else
			return false;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	protected void add( String tokenTypeName, String... styleElementNames )
	{
		ArrayList<String> list = new ArrayList<String>( styleElementNames.length );
		for( String styleElementName : styleElementNames )
			list.add( styleElementName );
		addDef( new StyleElementDef( tokenTypeName, list ) );
	}

	@SuppressWarnings("unchecked")
	protected void addJson( Map<String, Object> json ) throws ResolutionException
	{
		for( Map.Entry<String, Object> entry : json.entrySet() )
		{
			String tokenTypeName = entry.getKey();
			if( entry.getValue() instanceof Iterable<?> )
			{
				for( String styleElementName : (Iterable<String>) entry.getValue() )
					add( tokenTypeName, styleElementName );
			}
			else if( entry.getValue() instanceof String )
				add( tokenTypeName, (String) entry.getValue() );
			else
				throw new ResolutionException( "Unexpected value in style definition: " + entry.getValue() );
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final ConcurrentMap<String, Style> styles = new ConcurrentHashMap<String, Style>();

	private final Map<TokenType, List<StyleElement>> styleElements = new HashMap<TokenType, List<StyleElement>>();
}
