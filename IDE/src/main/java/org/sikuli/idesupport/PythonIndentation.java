/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.idesupport;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import org.sikuli.basics.PreferencesUser;
import org.sikuli.basics.Debug;

/**
 * Implements the logic for giving hints about the (correct) indentation of new
 * lines of Python code entered by a user. Auto indentation uses these hints to
 * automatically adjust the the indentation of the next line to be entered,
 * and/or the last line entered. The typical usage is:
 * <ol>
 * <li>Call {@link #reset()} to reset the object's state.
 * <li>Feed each line of python text entered by the user (including the
 * terminating end-of-line sequence) to the object by calling
 * {@link #addText(String)}.
 * <li>Call {@link #shouldChangeLastLineIndentation()} and
 * {@link #shouldChangeNextLineIndentation()} to get hints about the indentation
 * of the last line entered, and the next line to be entered.
 * </ol>
 * Note: the proposed indentation change for the next line may depend on the
 * current indentation of the last line entered. If you change the indentation
 * of the last line entered, you may also have to adjust the indentation of the
 * next line by the same amount.
 * <p>
 * This implementation determines the logical line structure of a Python
 * document from the beginning of the document up to the line for which
 * indentation hints are requested. The indentation of a line is based on the
 * current indentation of the line and the indentation of the logical Python
 * line that contains the line. See {@link PythonState} and <a href=
 * "http://docs.python.org/reference/lexical_analysis.html#line-structure">line
 * structure</a> in the <a href="http://docs.python.org/reference/">Python
 * language reference</a> for information about logical lines.
 * <p>
 * This implementation provides indentation hints for the following contexts:
 * <ul>
 * <li>compound statements such as {@code if/elif/else}, {@code for}, {@code
 * while}, {@code try/except/finally}, function and class definitions
 * <li>statements after which indentation is normally decreased: {@code break},
 * {@code continue}, {@code pass}, {@code raise}, {@code return}
 * <li>expressions in parentheses, square brackets and curly braces that extend
 * over multiple lines (implicit line joining)
 * <li>explicit line joining (backslash-escaped by end-of-line)
 * <li>long strings
 * </ul>
 */
public class PythonIndentation implements IIndentationLogic {

   public static final int PARENTHESIS_INDENTATION_TABSTOPS = 2;
   public static final int NESTED_PARENTHESIS_INDENTATION_TABSTOPS = 1;
   public static final int LONG_STRING_INDENTATION_COLUMNS = 3;
   public static final int EXPLICIT_LINE_JOINING_INDENTATION_TABSTOPS = 2;

   private static final Pattern ENDS_WITH_COLON = Pattern.compile(
         "^[^#]*:\\s*(?:#.*)?$", Pattern.DOTALL);

   private static final Pattern UNINDENT_NEXT_LINE_STATEMENT = Pattern
         .compile("^\\s*(?:break|continue|pass|raise|return)\\b");

   private static final Pattern UNINDENT_LAST_LINE_STATEMENT = Pattern
         .compile("^\\s*(?:elif|else|except|finally)\\b");

   // this is matched with a logical line structure
   private static final Pattern COMPOUND_HEADER_STATEMENT_WITH_ARG = Pattern
         .compile("^\\s*(?:if|elif|for|while|with|except|def|class)\\b.*[\\w'\")\\]}](?:\\s*:)?\\s*$");

   private static final Pattern COMPOUND_HEADER_STATEMENT_WITHOUT_ARG = Pattern
         .compile("^\\s*(?:else|try|except|finally)\\b(?:\\s*:)?\\s*$");

   private Matcher endsWithColonMatcher = ENDS_WITH_COLON.matcher("");
   private Matcher unindentNextLineStatementMatcher = UNINDENT_NEXT_LINE_STATEMENT
         .matcher("");
   private Matcher unindentLastLineStatementMatcher = UNINDENT_LAST_LINE_STATEMENT
         .matcher("");
   private Matcher compoundHeaderStatementWithArgMatcher = COMPOUND_HEADER_STATEMENT_WITH_ARG
         .matcher("");
   private Matcher compoundHeaderStatementWithoutArgMatcher = COMPOUND_HEADER_STATEMENT_WITHOUT_ARG
         .matcher("");

   private PythonState pythonState;
   private boolean wasColonAdded;

   public PythonIndentation(){
      pythonState = new PythonState();
      wasColonAdded = false;
   }

	 @Override
   public void checkIndent(String leadingWhitespace, int line) {
     if(leadingWhitespace.contains("\t") && leadingWhitespace.contains(" ")) {

     }
     int lws = leadingWhitespace.length();
     int tws = PreferencesUser.getInstance().getTabWhitespace().length();
//TODo obsolete, when indentation is checked at load time
     if(tws > 1 && leadingWhitespace.contains("\t") && leadingWhitespace.contains(" ")) {
       Debug.error("PythonIndentation: indent has mixed tab and space in line " + line);
     }
     if (tws == 1 || (lws % tws) == 0) {
       return;
     }
     Debug.error("PythonIndentation: indent not consistent with tab settings in line " + line);
   }

	 @Override
   public int checkDedent(String leadingWhitespace, int line) {
     int lws = leadingWhitespace.length();
     int tws = PreferencesUser.getInstance().getTabWhitespace().length();
     if (lws < tws) {
       return lws;
     }
     checkIndent(leadingWhitespace, line);
     return tws;
   }

   /**
    * Checks if the last logical line (logically) ends with a colon. A logical
    * line logically ends with a colon if it contains a colon that is not inside
    * a comment and the colon is only followed by whitespace or by a comment.
    * <p>
    * If {@link #setLastLineEndsWithColon()} was called after the last chunk of
    * text was fed to this object, this method returns true.
    * <p>
    * This method is not thread safe!
    *
    * @return true if the last logical line logically ends with a colon
    */
   public boolean endsLastLogicalLineWithColon(){
      // not thread safe!
      if( wasColonAdded ) {
				return true;
			}
      return endsWithColonMatcher.reset(
            pythonState.getLastLogicalLineStructure()).matches();
   }

   /**
    * Checks if the last logical line (logically) contains a colon. A logical
    * line logically contains a colon if it contains a colon that is not inside
    * a parenthesized expression, a string or a comment.
    *
    * @return true if the last logical line logically contains a colon
    */
   public boolean hasLastLogicalLineColon(){
      if( wasColonAdded ) {
				return true;
			}
      return pythonState.getLastLogicalLineStructure().contains(":");
   }

   /**
    * Checks if the last logical line begins with a python statement that
    * usually terminates an indented block ({@code break}, {@code continue},
    * {@code pass}, {@code raise}, {@code return}).
    * <p>
    * This method returns true only if {@code break}/{@code continue}/ {@code
    * pass}/{@code raise}/{@code return} is the first statement in the logical
    * line. It does not recognize things like {@code print x; return}.
    * <p>
    * This method is not thread safe!
    *
    * @return true if the last logical line begins with a statement that usually
    *         terminates an indented block
    */
   public boolean isLastLogicalLineUnindentNextLineStatement(){
      // not thread safe!
      return unindentNextLineStatementMatcher.reset(
            pythonState.getLastLogicalLine()).find();
   }

   /**
    * Checks if the last physical line begins with a python statement that must
    * have a lower indentation level than the preceding block ({@code else},
    * {@code elif}, {@code except}, {@code finally}).
    * <p>
    * This method is not thread safe!
    *
    * @return true if the last physical line begins with a statement that must
    *         have a lower indentation level than the preceding block
    */
   public boolean isUnindentLastLineStatement(){
      // not thread safe!
      return unindentLastLineStatementMatcher.reset(
            pythonState.getLastPhysicalLine()).find();
   }

   /**
    * Checks if the last logical line begins with a python statement that starts
    * a clause of a compound statement ({@code if}, {@code else}, {@code elif},
    * {@code for}, {@code while}, {@code with}, {@code try}, {@code except},
    * {@code finally}, {@code def}, {@code class}).
    * <p>
    * This method is not thread safe!
    *
    * @return true if the logical line begins with a statement that starts a
    *         clause of a compound statement
    */
   public boolean isLastLogicalLineCompoundHeaderStatement(){
      // not thread safe!
      String structure = pythonState.getLastLogicalLineStructure();
      return compoundHeaderStatementWithArgMatcher.reset(structure).find()
            || compoundHeaderStatementWithoutArgMatcher.reset(structure).find();
   }

   /**
    * Sets the number of whitespace columns that equals a single tab used by
    * this object to calculate the indentation of lines.
    *
    * @param tabwidth
    *           the number of whitespace columns that equals a single tab
    */
	 @Override
   public void setTabWidth(int tabwidth){
      pythonState.setTabSize(tabwidth);
   }

   /**
    * Returns the number of whitespace columns that equals a single tab used by
    * this object to calculate the indentation of lines.
    *
    * @return the number of whitespace columns that equals a single tab
    */
   public int getTabWidth(){
      return pythonState.getTabSize();
   }

   /**
    * Resets the state of this object. The new state will be as if no text had
    * been fed to this object.
    */
	 @Override
   public void reset(){
      pythonState.reset();
   }

   /**
    * Feeds a chunk of text (i.e. code) to this object. The text can be a single
    * line or multiple lines or even incomplete lines. You can feed an entire
    * document at once, or line by line. Any new text will be (virtually)
    * appended to text added earlier.
    *
    * @param text
    *           a chunk of code
    */
	 @Override
   public void addText(String text){
      wasColonAdded = false;
      pythonState.update(text);
   }

   /**
    * Returns the line number of the last line fed to this object.
    *
    * @return the line number of the last line (0-based)
    */
   public int getLastLineNumber(){
      return pythonState.getPhysicalLineNumber();
   }

   /**
    * Tells this object to assume that the last logical line ends with a colon.
    * Auto-completion that adds a colon at the end of a line after the line was
    * fed to this object must call this method to notify this object that a
    * colon was added. This affects the hints for line indentation.
    */
	 @Override
   public void setLastLineEndsWithColon(){
      wasColonAdded = true;
   }

   /**
    * Returns a hint about how the indentation of the last line fed to this
    * object should be changed. A negative value means decrease indentation
    * while a positive value means increase indentation by the returned value.
    *
    * @return the number of columns by which the indentation should be changed
    */
	 @Override
   public int shouldChangeLastLineIndentation(){
      // only change indentation of the first physical line of a logical line
      if( pythonState.getPhysicalLineNumber() > pythonState
            .getLogicalLinePhysicalStartLineNumber() ) {
				return 0;
			}
      // if this is not the first logical line and the indentation level is
      // already less than the previous logical line, do not unindent further
      if( pythonState.getLogicalLineNumber() > 0
            && pythonState.getLastLogicalLineIndentation() < pythonState
                  .getPrevLogicalLineIndentation() ) {
				return 0;
			}
      int change;
      if( isUnindentLastLineStatement() ){
         change = -pythonState.getTabSize();
      }else{
         change = 0;
      }
      // avoid negative indentation
      int physicalIndentation = pythonState.getLastPhysicalLineIndentation();
      if( physicalIndentation + change < 0 ){
         change = -physicalIndentation;
      }
      return change;
   }

   /**
    * Returns a hint about how the indentation of the next line (the line
    * following the last line fed to this object) should be changed. A negative
    * value means decrease indentation while a positive value means increase
    * indentation by the returned value.
    *
    * @return the number of columns by which the indentation should be changed
    */
	 @Override
   public int shouldChangeNextLineIndentation(){
      if( !pythonState.isPhysicalLineComplete() ) {
				return 0;
			}
      int logicalIndentation = pythonState.getLastLogicalLineIndentation();
      int physicalIndentation = pythonState.getLastPhysicalLineIndentation();
      int change = logicalIndentation - physicalIndentation;
      if( pythonState.isLogicalLineComplete() ){
         if( endsLastLogicalLineWithColon() ){
            change += pythonState.getTabSize();
         }else if( isLastLogicalLineUnindentNextLineStatement() ){
            change -= pythonState.getTabSize();
         }
      }else if( pythonState.inLongString() ){
         if( pythonState.getDepth() > 1 ){
            // long string inside parenthesis
            change += (PARENTHESIS_INDENTATION_TABSTOPS + (pythonState
                  .getDepth() - 2) * NESTED_PARENTHESIS_INDENTATION_TABSTOPS)
                  * pythonState.getTabSize();
         }else{
            if( pythonState.getPhysicalLineNumber() == pythonState
                  .getLogicalLinePhysicalStartLineNumber() ){
               change = LONG_STRING_INDENTATION_COLUMNS;
            }else{
               change = 0;
            }
         }
      }else if( pythonState.getDepth() > 0 ){
         // only parenthesis, no string
         change += (PARENTHESIS_INDENTATION_TABSTOPS + (pythonState.getDepth() - 1)
               * NESTED_PARENTHESIS_INDENTATION_TABSTOPS)
               * pythonState.getTabSize();
      }else if( pythonState.isExplicitLineJoining() ){
         if( pythonState.getPhysicalLineNumber() == pythonState
               .getLogicalLinePhysicalStartLineNumber() ){
            change = EXPLICIT_LINE_JOINING_INDENTATION_TABSTOPS
                  * pythonState.getTabSize();
         }else{
            change = 0;
         }
      }else{
         change = 0;
      }

      // avoid negative indentation
      if( physicalIndentation + change < 0 ){
         change = -physicalIndentation;
      }

      return change;
   }

   /**
    * Returns a hint whether a colon should be added at the end of the last
    * logical line. This is the case if the last logical line satisfies all of
    * the following:
    * <ul>
    * <li>it is complete (i.e. no explicit or implicit joining with the next
    * physical line),
    * <li>it begins with a statement that starts a clause in a compound
    * statement,
    * <li>if the statement requires and argument, it is followed by another
    * token,
    * <li>it does not (logically) end with a punctuation symbol,
    * <li>it does not end with a comment.
    * </ul>
    * For example, the following lines should have a colon appended:
    * <pre>
    * if x
    * else
    * elif x
    * while x
    * class C
    * class C(D)
    * def f()
    * try
    * except
    * except E
    * except E, e
    * finally
    * </pre>
    * The following lines should not have a colon appended:
    * <pre>
    * print x
    * 'if x'
    * if
    * if (x
    * if 's
    * if x:
    * if x,
    * if x ?
    * if x # ends with comment
    * elif
    * while
    * class
    * def
    * </pre>
    *
    * @return true if the last logical line should end with a colon but does not
    */
	 @Override
   public boolean shouldAddColon(){
      if( !pythonState.isLogicalLineComplete() ) {
				return false;
			}
      return isLastLogicalLineCompoundHeaderStatement()
            && !hasLastLogicalLineColon();
   }

   //<editor-fold defaultstate="collapsed" desc="support for detecting whitespace">
	 @Override
   public String getLeadingWhitespace(String text) {
     int len = text.length();
     int count = 0;
     while (count < len && isWhitespace(text.charAt(count))) {
       count++;
     }
     return text.substring(0, count);
   }

	 @Override
   public String getLeadingWhitespace(StyledDocument doc, int head, int len) throws BadLocationException {
     String ret = "";
     int pos = head;
     while (pos < head + len) {
       Element e = doc.getCharacterElement(pos);
       if (e.getName().equals(StyleConstants.ComponentElementName)) {
         break;
       }
       int eStart = e.getStartOffset();
       int eEnd = e.getEndOffset();
       String space = getLeadingWhitespace(doc.getText(eStart, eEnd - eStart));
       ret += space;
       if (space.length() < eEnd - eStart) {
         break;
       }
       pos = eEnd;
     }
     return ret;
   }

	 @Override
   public int atEndOfLine(StyledDocument doc, int cpos, int start, String s, int sLen) {
     for (int i = cpos - start; i < sLen; i++) {
       if (doc.getCharacterElement(cpos).getName().equals(StyleConstants.ComponentElementName) || !isWhitespace(s.charAt(i))) {
         return i + start;
       }
       cpos++;
     }
     return -1;
   }

   public static boolean isWhitespace(char ch) {
     return ch == ' ' || ch == '\t';
   }
   //</editor-fold>

}
