/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.syntaxhighlight;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import org.sikuli.syntaxhighlight.format.Formatter;
import org.sikuli.syntaxhighlight.grammar.Lexer;
import org.sikuli.syntaxhighlight.grammar.Token;

public class Run {

  private static void p(String text, Object... args) {
    System.out.println(String.format(text, args));
  }

  public static void main(String[] args) throws IOException, ResolutionException {
    String file = System.getProperty("user.dir") + "/src/main/java/org/sikuli/syntaxhighlight/Util.java";
    String aLexer = "python";
    Lexer lexer = Lexer.getByName(aLexer);
    if (lexer != null) {
      Formatter formatter = Formatter.getByName("html");
      String code = Util.streamToString(new FileInputStream(file));
//      code = "      String code = Util.streamToString(new FileInputStream(file));";
      long start = new Date().getTime();
      Iterable<Token> tokens = lexer.getTokens(code);
      long lexing = new Date().getTime() - start;
      formatter.format(tokens, new PrintWriter("/Users/rhocke/Desktop/shtest.html"));
      long formatting = new Date().getTime()- start - lexing;
      p("%s: processed (%d, %d)", aLexer, lexing, formatting);
    } else {
      p("%s: no Lexer found", aLexer);
    }
  }
}
