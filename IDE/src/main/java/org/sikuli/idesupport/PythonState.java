/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.idesupport;

import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sikuli.basics.Debug;

/**
 * This class is used to determine the state at any given position in a python
 * document. Here, state means the following:
 * <ul>
 * <li>the nesting level of parentheses and string literals at the given
 * position,
 * <li>whether the given position is inside a comment,
 * <li>whether the given position terminates a physical line,
 * <li>whether the given position terminates a logical line,
 * <li>the indentation of the last physical line,
 * <li>the indentation of the last logical line.
 * </ul>
 * See <a href=
 * "http://docs.python.org/reference/lexical_analysis.html#line-structure">line
 * structure</a> in the <a href="http://docs.python.org/reference/">python
 * language reference</a> for information about physical and logical lines.
 * <p>
 * To determine the state of a python document at a given position, you feed a
 * {@link PythonState} object with the prefix of the document ending at the
 * given position. The text can be fed in any number of consecutive chunks
 * (input chunks). Typically text is fed to the object in chunks that represent
 * (physical) lines, although this is not a requirement.
 * <p>
 * After each chunk of text, the state is updated and represents the state of
 * (the prefix of) the document given by the concatenation of all the chunks
 * seen so far. For example, if each chunk is one line of the python document,
 * you get information about the state of the document at the end of each line.
 * Possible applications are:
 * <ul>
 * <li>represent a python document as a sequence of logical lines,
 * <li>change the indentation level of the next line.
 * </ul>
 * <p>
 * You can retrieve the last complete physical and logical line seen by the
 * state object.
 * <p>
 * You can retrieve a string that represents the structure of the last logical
 * line seen by the state object (see {@link #getLastLogicalLineStructure()}.
 * The line structure can be used as a hint for automatic code completion, e.g.
 * to automatically add a colon to certain python statements (<tt>if</tt>,
 * <tt>except</tt>).
 * <p>
 * This class does not perform syntactic analysis. You cannot use it to find
 * syntax errors in a python document.
 * <p>
 * This class does not perform full lexical analysis. It does not recognize
 * keywords, identifiers, or numbers.
 */
public class PythonState {

   public static final int DEFAULT_TABSIZE = 4;

   public static enum State{
      DEFAULT, IN_SINGLE_QUOTED_STRING, IN_DOUBLE_QUOTED_STRING, IN_LONG_SINGLE_QUOTED_STRING, IN_LONG_DOUBLE_QUOTED_STRING, IN_PARENTHESIS, IN_COMMENT
   };

   // Matchers to decide what to do next in each state.
   // Each matcher includes escaped EOL and escaped backslash

   // starts a string, parenthesis or comment
   private static final Pattern START_DELIMITER = Pattern.compile(
         "('''|\"\"\"|['\"(\\[{#]|\\\\?(?:\r|\n|\r\n)|\\\\.)",
         Pattern.MULTILINE);

   // starts or ends a string, parenthesis, or starts a comment
   private static final Pattern DELIMITER = Pattern
         .compile("('''|\"\"\"|['\"()\\[\\]{}#]|\\\\?(?:\r|\n|\r\n)|\\\\.)");

   // ends a single quoted string
   private static final Pattern SINGLE_QUOTE_DELIMITER = Pattern
         .compile("('|\\\\?(?:\r|\n|\r\n)|\\\\.)");

   // ends a double quoted string
   private static final Pattern DOUBLE_QUOTE_DELIMITER = Pattern
         .compile("(\"|\\\\?(?:\r|\n|\r\n)|\\\\.)");

   // ends a single quoted long string
   private static final Pattern LONG_SINGLE_QUOTE_DELIMITER = Pattern
         .compile("('''|\\\\?(?:\r|\n|\r\n)|\\\\.)");

   // ends a double quoted long string
   private static final Pattern LONG_DOUBLE_QUOTE_DELIMITER = Pattern
         .compile("(\"\"\"|\\\\?(?:\r|\n|\r\n)|\\\\.)");

   // EOL
   private static final Pattern END_OF_LINE = Pattern.compile("(?:\n|\r\n?)");

   private Matcher startDelimiterMatcher = START_DELIMITER.matcher("")
         .useAnchoringBounds(true);
   private Matcher delimiterMatcher = DELIMITER.matcher("").useAnchoringBounds(
         true);
   private Matcher singleQuoteMatcher = SINGLE_QUOTE_DELIMITER.matcher("")
         .useAnchoringBounds(true);
   private Matcher doubleQuoteMatcher = DOUBLE_QUOTE_DELIMITER.matcher("")
         .useAnchoringBounds(true);
   private Matcher longSingleQuoteMatcher = LONG_SINGLE_QUOTE_DELIMITER
         .matcher("").useAnchoringBounds(true);
   private Matcher longDoubleQuoteMatcher = LONG_DOUBLE_QUOTE_DELIMITER
         .matcher("").useAnchoringBounds(true);
   private Matcher endOfLineMatcher = END_OF_LINE.matcher("")
         .useAnchoringBounds(true);

   private StringBuilder physicalLine;
   private StringBuilder logicalLine;
   private StringBuilder unmatchedChunk;

   private boolean completePhysicalLine;
   private boolean completeLogicalLine;

   /**
    * Set to true to indicate that the next physical line is a continuation of
    * the previous physical line.
    */
   private boolean explicitJoining;

   /**
    * If {@link #explicitJoining} is true, the length of the prefix of
    * {@link #unmatchedChunk} that has already been added to
    * {@link #logicalLine}.
    */
   private int explicitJoinOffset;

   private int physicalLineNumber;
   private int logicalLineNumber;
   private int logicalLinePhysicalStartLineNumber;
   private int physicalLineIndentation;
   private int logicalLineIndentation;
   private int prevPhysicalLineIndentation;
   private int prevLogicalLineIndentation;

   private int tabsize = DEFAULT_TABSIZE;

   private Stack<State> state;

   private StringBuilder logicalLineStructure;

   public PythonState(){
      state = new Stack<State>();
      state.push(State.DEFAULT);
      physicalLine = new StringBuilder();
      logicalLine = new StringBuilder();
      unmatchedChunk = new StringBuilder();
      logicalLineStructure = new StringBuilder();
      reset();
   }

   /**
    * Sets the number of whitespace columns that equals a single tab. This is
    * used to calculate the indentation of lines.
    *
    * @param tabsize
    *           the number of whitespace columns that equals a single tab
    */
   public void setTabSize(int tabsize){
      this.tabsize = tabsize;
   }

   /**
    * Returns the number of whitespace columns equalling a single tab that is
    * used to calculate the indentation of lines.
    *
    * @return the number of whitespace columns that equals a single tab
    */
   public int getTabSize(){
      return tabsize;
   }

   /**
    * Resets the state of this object. The new state is equivalent to an empty
    * document.
    */
   public void reset(){
      state.setSize(1);
      physicalLine.setLength(0);
      logicalLine.setLength(0);
      unmatchedChunk.setLength(0);
      logicalLineStructure.setLength(0);
      completePhysicalLine = false;
      completeLogicalLine = false;
      explicitJoining = false;
      explicitJoinOffset = 0;
      physicalLineNumber = 0;
      logicalLineNumber = 0;
      logicalLinePhysicalStartLineNumber = 0;
      physicalLineIndentation = -1;
      logicalLineIndentation = -1;
      prevPhysicalLineIndentation = -1;
      prevLogicalLineIndentation = -1;
   }

   private boolean isEOL(String s){
      return s.equals("\r") || s.equals("\n") || s.equals("\r\n");
   }

   private boolean isEscapedEOL(String s){
      return s.length() >= 2 && s.charAt(0) == '\\' && isEOL(s.substring(1));
   }

   private boolean isEscapedChar(String s){
      return s.length() == 2 && s.charAt(0) == '\\';
   }

   /**
    * Feeds a chunk of text to this object. The text will be (virtually)
    * appended to any text that was fed to this object earlier since the last
    * reset.
    *
    * @param newChunk
    *           a new chunk of text
    */
   public void update(String newChunk){
      unmatchedChunk.append(newChunk);

      // indexes in unmatchedChunk
      int searchStart;
      int matchEnd;
      int nextSearchStart = 0;
      String match = null;

      SCAN: while( nextSearchStart < unmatchedChunk.length() ){
         searchStart = nextSearchStart;
         Debug.log(9, "%s: [%s]", state.peek().name(),
               unmatchedChunk.substring(searchStart));

         // more input to match
         if( completePhysicalLine ){
            physicalLine.setLength(0);
            completePhysicalLine = false;
            physicalLineNumber++;
            prevPhysicalLineIndentation = physicalLineIndentation;
         }
         if( completeLogicalLine ){
            logicalLine.setLength(0);
            logicalLineStructure.setLength(0);
            completeLogicalLine = false;
            logicalLineNumber++;
            logicalLinePhysicalStartLineNumber = physicalLineNumber;
            prevLogicalLineIndentation = logicalLineIndentation;
            logicalLineIndentation = -1;
         }
         explicitJoining = false;

         // use different matchers, depending on current state
         switch( state.peek() ){
         case DEFAULT:
            // start a string, parenthesis, comment, or EOL
            startDelimiterMatcher.reset(unmatchedChunk);
            startDelimiterMatcher.region(searchStart, unmatchedChunk.length());
            if( startDelimiterMatcher.find() ){
               match = startDelimiterMatcher.group(1);
               matchEnd = startDelimiterMatcher.end(1);
               if( isEscapedEOL(match) ){
                  completePhysicalLine = true;
                  explicitJoining = true;
               }else if( isEOL(match) ){
                  completePhysicalLine = true;
                  // append scanned input except EOL
                  logicalLineStructure.append(unmatchedChunk.substring(
                        searchStart, matchEnd - match.length()));
               }else{
                  if( match.equals("'") ){
                     state.push(State.IN_SINGLE_QUOTED_STRING);
                  }else if( match.equals("\"") ){
                     state.push(State.IN_DOUBLE_QUOTED_STRING);
                  }else if( match.equals("'''") ){
                     state.push(State.IN_LONG_SINGLE_QUOTED_STRING);
                  }else if( match.equals("\"\"\"") ){
                     state.push(State.IN_LONG_DOUBLE_QUOTED_STRING);
                  }else if( match.equals("(") || match.equals("[")
                        || match.equals("{") ){
                     state.push(State.IN_PARENTHESIS);
                  }else if( match.equals("#") ){
                     state.push(State.IN_COMMENT);
                  }else if( isEscapedChar(match) ){
                     // skip
                  }else{
                     throw new Error("unexpected match \"" + match + "\"");
                  }
                  logicalLineStructure.append(unmatchedChunk.substring(
                        searchStart, matchEnd));
               }
            }else{
               break SCAN;
            }
            break;
         case IN_PARENTHESIS:
            // start string, start/end parenthesis, comment, EOL
            delimiterMatcher.reset(unmatchedChunk);
            delimiterMatcher.region(searchStart, unmatchedChunk.length());
            if( delimiterMatcher.find() ){
               match = delimiterMatcher.group(1);
               matchEnd = delimiterMatcher.end(1);
               if( match.equals("'") ){
                  state.push(State.IN_SINGLE_QUOTED_STRING);
               }else if( match.equals("\"") ){
                  state.push(State.IN_DOUBLE_QUOTED_STRING);
               }else if( match.equals("'''") ){
                  state.push(State.IN_LONG_SINGLE_QUOTED_STRING);
               }else if( match.equals("\"\"\"") ){
                  state.push(State.IN_LONG_DOUBLE_QUOTED_STRING);
               }else if( match.equals("(") || match.equals("[") || match.equals("{") ){
                  state.push(State.IN_PARENTHESIS);
               }else if( match.equals(")") || match.equals("]") || match.equals("}") ){
                  state.pop();
                  if( state.peek() == State.DEFAULT ){
                     logicalLineStructure.append(match);
                  }
               }else if( match.equals("#") ){
                  state.push(State.IN_COMMENT);
               }else if( isEOL(match) ){
                  completePhysicalLine = true;
               }else if( isEscapedEOL(match) ){
                  completePhysicalLine = true;
                  explicitJoining = true;
               }else if( isEscapedChar(match) ){
                  // skip
               }else{
                  throw new Error("unexpected match");
               }
            }else{
               break SCAN;
            }
            break;
         case IN_SINGLE_QUOTED_STRING:
            // end single quoted string, or EOL
            singleQuoteMatcher.reset(unmatchedChunk);
            singleQuoteMatcher.region(searchStart, unmatchedChunk.length());
            if( singleQuoteMatcher.find() ){
               match = singleQuoteMatcher.group(1);
               matchEnd = singleQuoteMatcher.end(1);
               if( match.equals("'") ){
                  state.pop();
                  if( state.peek() == State.DEFAULT ){
                     logicalLineStructure.append(match);
                  }
               }else if( isEOL(match) ){
                  completePhysicalLine = true;
               }else if( isEscapedEOL(match) ){
                  completePhysicalLine = true;
                  explicitJoining = true;
               }else if( isEscapedChar(match) ){
                  // skip
               }else{
                  throw new Error("unexpected match");
               }
            }else{
               break SCAN;
            }
            break;
         case IN_DOUBLE_QUOTED_STRING:
            // end double quoted string, or EOL
            doubleQuoteMatcher.reset(unmatchedChunk);
            doubleQuoteMatcher.region(searchStart, unmatchedChunk.length());
            if( doubleQuoteMatcher.find() ){
               match = doubleQuoteMatcher.group(1);
               matchEnd = doubleQuoteMatcher.end(1);
               if( match.equals("\"") ){
                  state.pop();
                  if( state.peek() == State.DEFAULT ){
                     logicalLineStructure.append(match);
                  }
               }else if( isEOL(match) ){
                  completePhysicalLine = true;
               }else if( isEscapedEOL(match) ){
                  completePhysicalLine = true;
                  explicitJoining = true;
               }else if( isEscapedChar(match) ){
                  // skip
               }else{
                  throw new Error("unexpected match");
               }
            }else{
               break SCAN;
            }
            break;
         case IN_LONG_SINGLE_QUOTED_STRING:
            // end single quoted long strong, or EOL
            longSingleQuoteMatcher.reset(unmatchedChunk);
            longSingleQuoteMatcher.region(searchStart, unmatchedChunk.length());
            if( longSingleQuoteMatcher.find() ){
               match = longSingleQuoteMatcher.group(1);
               matchEnd = longSingleQuoteMatcher.end(1);
               if( match.equals("'''") ){
                  state.pop();
                  if( state.peek() == State.DEFAULT ){
                     logicalLineStructure.append(match);
                  }
               }else if( isEOL(match) ){
                  completePhysicalLine = true;
               }else if( isEscapedEOL(match) ){
                  completePhysicalLine = true;
                  explicitJoining = true;
               }else if( isEscapedChar(match) ){
                  // skip
               }else{
                  throw new Error("unexpected match");
               }
            }else{
               break SCAN;
            }
            break;
         case IN_LONG_DOUBLE_QUOTED_STRING:
            // end double quoted long string, or EOL
            longDoubleQuoteMatcher.reset(unmatchedChunk);
            longDoubleQuoteMatcher.region(searchStart, unmatchedChunk.length());
            if( longDoubleQuoteMatcher.find() ){
               match = longDoubleQuoteMatcher.group(1);
               matchEnd = longDoubleQuoteMatcher.end(1);
               if( match.equals("\"\"\"") ){
                  state.pop();
                  if( state.peek() == State.DEFAULT ){
                     logicalLineStructure.append(match);
                  }
               }else if( isEOL(match) ){
                  completePhysicalLine = true;
               }else if( isEscapedEOL(match) ){
                  completePhysicalLine = true;
                  explicitJoining = true;
               }else if( isEscapedChar(match) ){
                  // skip
               }else{
                  throw new Error("unexpected match");
               }
            }else{
               break SCAN;
            }
            break;
         case IN_COMMENT:
            // search EOL
            endOfLineMatcher.reset(unmatchedChunk);
            endOfLineMatcher.region(searchStart, unmatchedChunk.length());
            if( endOfLineMatcher.find() ){
               match = endOfLineMatcher.group();
               matchEnd = endOfLineMatcher.end();
               state.pop();
               completePhysicalLine = true;
            }else{
               break SCAN;
            }
            break;
         default:
            throw new Error("This should never happen (probably a bug)");
         }
         Debug.log(9, "matcher=[%s]", match);

         // add matched input to physical line
         physicalLine.append(unmatchedChunk
               .substring(searchStart + explicitJoinOffset, matchEnd));
         if( completePhysicalLine ){
            physicalLineIndentation = getPhysicalLineIndentation();
            // if this is the first physical line of a logical line, set the
            // logical line indentation
            if( logicalLineIndentation < 0 ){
               logicalLineIndentation = physicalLineIndentation;
            }
         }
         if( explicitJoining ){
            // delete backslash-EOL
            unmatchedChunk.delete(matchEnd - match.length(), matchEnd);
            matchEnd -= match.length();
            // add matched input to logical line (minus input that was already
            // added)
            logicalLine.append(unmatchedChunk.substring(searchStart
                  + explicitJoinOffset, matchEnd));
            explicitJoinOffset = matchEnd - searchStart;
            completeLogicalLine = false;
            // deleting the backslash-EOL effectively merges the current line
            // with the next line, and we attempt to match it again from the
            // start
            nextSearchStart = searchStart;
            if( matchEnd == unmatchedChunk.length() ){
               // no further match is possible until there is new input
               break SCAN;
            }
         }else{
            logicalLine.append(unmatchedChunk.substring(searchStart
                  + explicitJoinOffset, matchEnd));
            completeLogicalLine = completePhysicalLine && inDefaultState();
            explicitJoinOffset = 0;
            nextSearchStart = matchEnd;
         }
      } // end SCAN loop
      unmatchedChunk.delete(0, nextSearchStart);
      Debug.log(9, "%s: unmatched: [%s]", state.peek().name(), unmatchedChunk);
   }

   /**
    * Returns the state of the python document seen so far.
    *
    * @return the current state
    */
   public State getState(){
      return state.peek();
   }

   /**
    * Returns true if the state of the document seen by this object is not
    * inside any parenthesis, string or comment.
    *
    * @return true if the current state is the default state
    */
   public boolean inDefaultState(){
      return state.peek() == State.DEFAULT;
   }

   /**
    * Returns true if the state of the document seen by this object is inside a
    * parenthesis (including square brackets and curly braces).
    *
    * @return true if the current state is inside a parenthesis
    */
   public boolean inParenthesis(){
      return state.peek() == State.IN_PARENTHESIS;
   }

   /**
    * Returns true if the state of the document seen by this object is inside a
    * string (short string or long string).
    *
    * @return true if the current state is inside a string
    */
   public boolean inString(){
      switch( state.peek() ){
      case IN_DOUBLE_QUOTED_STRING:
      case IN_SINGLE_QUOTED_STRING:
      case IN_LONG_SINGLE_QUOTED_STRING:
      case IN_LONG_DOUBLE_QUOTED_STRING:
         return true;
      }
      return false;
   }

   /**
    * Returns true if the state of the document seen by this object is inside a
    * long string.
    *
    * @return true if the current state is inside a long string
    */
   public boolean inLongString(){
      return state.peek() == State.IN_LONG_SINGLE_QUOTED_STRING
            || state.peek() == State.IN_LONG_DOUBLE_QUOTED_STRING;
   }

   /**
    * Returns true if the state of the document seen by this object is inside a
    * comment.
    *
    * @return true if the current state is inside a comment
    */
   public boolean inComment(){
      return state.peek() == State.IN_COMMENT;
   }

   /**
    * Returns the nesting level of parentheses and strings that the state of the
    * document seen by this object is in. The nesting level in the default state
    * is 0.
    * <p>
    * Note that parentheses can be nested at any depth, but only one level of
    * string can be nested inside the innermost parentheses because anything
    * inside a string is not interpreted.
    *
    * @return the nesting level of parentheses and strings of the current state
    */
   public int getDepth(){
      return state.size() - 1;
   }

   /**
    * Returns a string that represents the structure of the last logical line.
    * The returned string is identical to the last logical line except that the
    * contents of any strings or parenthesised expression, and any comment (i.e.
    * any input text with a nesting level greater than 0), and the trailing
    * end-of-line character, are deleted.
    * <table border="1">
    * <caption>Examples:</caption>
    * <tr>
    * <th>Input</th>
    * <th>Structure</th>
    * </tr>
    * <tr>
    * <td>{@code print x}</td>
    * <td>{@code print x}</td>
    * </tr>
    * <tr>
    * <td>{@code print 'x'}</td>
    * <td>{@code print ''}</td>
    * </tr>
    * <tr>
    * <td>{@code print '%s=%d\n' % ('a', f(x[0]))}</td>
    * <td>{@code print '' % ()}</td>
    * </tr>
    * <tr>
    * <td>{@code """a long comment"""}</td>
    * <td>{@code """"""}</td>
    * </tr>
    * <tr>
    * <td>{@code if x: pass # case 1}</td>
    * <td>{@code if x: pass #}</td>
    * </tr>
    * </table>
    *
    */
   public String getLastLogicalLineStructure(){
      return logicalLineStructure.toString();
   }

   /**
    * Returns the last physical line seen by this object, including the
    * terminating end-of-line sequence. If the last line seen by this object is
    * not a complete physical line, the return value is undefined.
    *
    * @return the last complete physical line seen by this object
    */
   public String getLastPhysicalLine(){
      return physicalLine.toString();
   }

   /**
    * Returns the last logical line seen by this object, including the
    * terminating end-of-line sequence. If the input seen by this object does
    * not end with a complete logical line, the return value is guaranteed to
    * include all complete physical lines seen of which the logical line is
    * comprised. If explicit line joining has occurred, any escaped end-of-line
    * sequence is not included in the logical line.
    *
    * @return the last complete logical line seen seen by this instance
    */
   public String getLastLogicalLine(){
      return logicalLine.toString();
   }

   /**
    * Returns the physical line number of the last physical line seen by this
    * object.
    *
    * @return the physical line number of the line returned by
    *         {@link #getLastPhysicalLine()} (0-based)
    */
   public int getPhysicalLineNumber(){
      return physicalLineNumber;
   }

   /**
    * Returns the logical line number of the last logical line seen by this
    * object.
    *
    * @return the logical line number of the line returned by
    *         {@link #getLastLogicalLine()} (0-based)
    */
   public int getLogicalLineNumber(){
      return logicalLineNumber;
   }

   /**
    * Returns the physical line number of the first physical line in the last
    * logical line seen by this object.
    *
    * @return the physical line number of the first physical line in the logical
    *         line returned by {@link #getLastLogicalLine()} (0-based)
    */
   public int getLogicalLinePhysicalStartLineNumber(){
      return logicalLinePhysicalStartLineNumber;
   }

   /**
    * Returns whether the last physical line seen by this object is complete. A
    * physical line is complete if it is terminated by an end-of-line sequence.
    *
    * @return true if the line returned by {@link #getLastPhysicalLine()} is
    *         complete
    */
   public boolean isPhysicalLineComplete(){
      return completePhysicalLine;
   }

   /**
    * Returns whether the last logical line seen by this object is complete. A
    * logical line is complete if all of the following are true:
    * <ul>
    * <li>the physical lines that it is comprised of are complete (i.e. it is
    * terminated by an end-of-line sequence)
    * <li>it does not end with a physical line that is explicitly joined with
    * the following line (i.e. the final end-of-line sequence is not preceded by
    * a backslash, unless the backslash is part of a comment)
    * <li>it does not contain any open parenthesis or string delimiter without
    * the matching closing parenthesis or string delimiter
    * </ul>
    *
    * @return true if the line returned by {@link #getLastLogicalLine()} is
    *         complete
    */
   public boolean isLogicalLineComplete(){
      return completeLogicalLine;
   }

   /**
    * Returns whether the last physical line seen by this object is explicitly
    * joined with the following line, i.e. whether its end-of-line sequence is
    * escaped with a backslash and the backslash is not inside a comment. If the
    * last physical line seen is not complete, the return value is undefined.
    *
    * @return true if the last complete physical line is explicitly joined with
    *         the following line
    */
   public boolean isExplicitLineJoining(){
      return explicitJoining;
   }

   private int getPhysicalLineIndentation(){
      int indentation = 0;
      for( int i = 0; i < physicalLine.length(); i++ ){
         char c = physicalLine.charAt(i);
         if( c == ' ' ){
            indentation++;
         }else if( c == '\t' ){
            indentation += tabsize;
         }else{
            break;
         }
      }
      return indentation;
   }

   /**
    * Returns the indentation (in columns of whitespace) of the last complete
    * physical line seen by this object.
    * <p>
    * Any tab characters in the leading whitespace of the line are counted as
    * the equivalent number of blank characters.
    *
    * @return the indentation of the last complete physical line
    * @throws IllegalStateException
    *            if the last physical line is not complete
    */
   public int getLastPhysicalLineIndentation() throws IllegalStateException{
      if( !completePhysicalLine ){
         throw new IllegalStateException("incomplete physical line");
      }
      return physicalLineIndentation;
   }

   /**
    * Returns the indentation (in columns of whitespace) of the last logical
    * line seen by this object. This is the indentation of the physical line
    * which is the first line in the logical line.
    * <p>
    * Any tab characters in the leading whitespace of the line are counted as
    * the equivalent number of blank characters.
    *
    * @return the indentation of the last logical line
    * @throws IllegalStateException
    *            if the first physical line in the last logical line is not
    *            complete
    */
   public int getLastLogicalLineIndentation() throws IllegalStateException{
      if( logicalLineIndentation < 0 ){
         throw new IllegalStateException("incomplete logical line");
      }
      return logicalLineIndentation;
   }

   /**
    * Returns the indentation of the previous physical line.
    *
    * @return the indentation of the previous physical line
    * @throws IllegalStateException
    *            if no complete physical line or only one complete physical line
    *            has been seen by this object.
    */
   public int getPrevPhysicalLineIndentation() throws IllegalStateException{
      if( prevPhysicalLineIndentation < 0 ){
         throw new IllegalStateException("not enough physical lines");
      }
      return prevPhysicalLineIndentation;
   }

   /**
    * Returns the indentation of the previous logical line.
    *
    * @return the indentation of the previous logical line
    * @throws IllegalStateException
    *            if no logical line or only one logical line has been seen by
    *            this instance
    */
   public int getPrevLogicalLineIndentation() throws IllegalStateException{
      if( prevLogicalLineIndentation < 0 ){
         throw new IllegalStateException("not enough logical lines");
      }
      return prevLogicalLineIndentation;
   }
}
