/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.syntaxhighlight;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.CodeSource;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Tal Liron
 */
public class Util {

  public static String literalRegEx(String expression) {
    return "\\Q" + expression + "\\E";
  }

  public static String replace(String string, String occurence, String replacement) {
    return string.replaceAll(literalRegEx(occurence), replacement);
  }

  public static String streamToString(InputStream stream) throws IOException {
    StringBuilder builder = new StringBuilder();
    String line;

    try {
      BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
      while ((line = reader.readLine()) != null) {
        builder.append(line).append("\n");
      }
    } finally {
      stream.close();
    }

    return builder.toString();
  }

  public static String rejsonToJson(InputStream stream) throws IOException {
    String rejson = streamToString(stream);
    String json = rejsonToJson(rejson, true);
    json = rejsonToJson(json, false);
    return json;
  }

  public static String rejsonToJson(String rejson, boolean doubleQuote) {
    Matcher matcher = doubleQuote ? DOUBLE_QUOTED_STRING.matcher(rejson) : SINGLE_QUOTED_STRING.matcher(rejson);
    StringBuilder json = new StringBuilder();
    int start = 0, end = 0, lastEnd = 0;
    while (matcher.find()) {
      lastEnd = end;
      start = matcher.start();
      end = matcher.end();
      if ((start > 0) && (rejson.charAt(start - 1) == 'r')) {
        // Convert Python-style r"" string to Java-compatible pattern
        String string = rejson.substring(start + 1, end - 1);
        json.append(rejson.substring(lastEnd, start - 1));
        json.append('\"');
        json.append(pythonRegExToJavaPattern(string, doubleQuote));
        json.append('\"');
      } /*
       * else if( !doubleQuote ) { // From single quote to double quote
       * String string = rejson.substring( start + 1, end - 1 );
       * json.append( rejson.substring( lastEnd, start - 1 ) );
       * json.append( '\"' ); json.append( string.replaceAll( "\"",
       * "\\\\\"" ) ); json.append( '\"' ); }
       */ else {
        // As is
        json.append(rejson.substring(lastEnd, end));
      }
    }
    json.append(rejson.substring(end));
    // System.out.println( json );
    return json.toString();
  }

  public static String pythonRegExToJavaPattern(String pattern, boolean doubleQuote) {
    pattern = pattern.replaceAll("\\\\", "\\\\\\\\");
    pattern = pattern.replaceAll("\\{", "\\\\\\\\{");
    pattern = pattern.replaceAll("\\}", "\\\\\\\\}");
    if (!doubleQuote) {
      pattern = pattern.replaceAll("\"", "\\\\\"");
    }
    // System.out.println( pattern );
    return pattern;
  }

  public static String escapeHtml(String text) {
    text = text.replace("&", "&amp;");
    text = text.replace("<", "&lt;");
    text = text.replace(">", "&gt;");
    text = text.replace("\"", "&quot;");
    text = text.replace("'", "&#39;");
    return text;
  }

  public static String asHtml(String text) {
    text = escapeHtml(text);
    text = text.replace(" ", "&nbsp;");
    return text;
  }
  private static final Pattern DOUBLE_QUOTED_STRING = Pattern.compile("\"(?>\\\\.|.)*?\"");
  private static final Pattern SINGLE_QUOTED_STRING = Pattern.compile("'(?>\\\\.|.)*?'");
  public static String extJSON = ".jso";
  private static CodeSource csJygments = Jygments.class.getProtectionDomain().getCodeSource();

  public static InputStream getJsonFile(String pack, String sub, String name, String fullname) {
    URI jarFileURI = null;
    File jarFile = null;
    InputStream stream = null;
    String jsonname = name.replace('.', '/') + extJSON;
    fullname = fullname.replace('.', '/') + extJSON;
    File fileInPack = null;
		File fileInRoot = null;
    try {
      jarFileURI = csJygments.getLocation().toURI();
    } catch (URISyntaxException ex) {
      System.out.println("Util: getJsonFile: URISyntaxException: " + ex.toString());
    }
    if (jarFileURI != null ) {
      String jarFilePath = jarFileURI.getPath();
      if (jarFileURI.getScheme().equals("file") && !jarFilePath.contains(".jar")) {
        if (!pack.isEmpty()) {
          pack = pack.replace(".", "/");
          if (!sub.isEmpty()) {
            sub = sub.replace(".", "/");
            pack = pack + "/" + sub;
            fileInRoot = new File(jarFilePath, sub + "/" + jsonname);
          }
          fileInPack = new File(jarFilePath, pack + "/" + jsonname);
        }
        if (fileInPack != null && fileInPack.exists()) {
					jarFile = fileInPack;
				} else if (fileInRoot != null && fileInRoot.exists()) {
            jarFile = fileInRoot;
        }
        if (jarFile != null) {
          try {
            stream = new FileInputStream(jarFile);
          } catch (FileNotFoundException ex) {
            System.out.println("Jygments: Util: not found:\n" + jarFile) ;
          }
        }
      } else {
        stream = Jygments.class.getClassLoader().getResourceAsStream(fullname);
      }
    }
    return stream;
  }
}
