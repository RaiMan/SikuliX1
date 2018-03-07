/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.scriptrunner;

import java.io.File;

/**
 * Interface for ScriptRunners like Jython.
 */
public interface IScriptRunner {

  /**
   * Can be used to initialize the ScriptRunner. This method is called at the beginning of program
   * execution. The given parameters can be used to parse any ScriptRunner specific custom options.
   *
   * @param args All arguments that were passed to the main-method
   */
  public void init(String[] args);

  /**
   * Executes the Script.
   *
   * @param scriptfile File containing the script
   * @param imagedirectory Directory containing the images (might be null: parent of script)
   * @param scriptArgs Arguments to be passed directly to the script with --args
   * @return exitcode for the script execution
   */
  public int runScript(File scriptfile, File imagedirectory, String[] scriptArgs, String[] forIDE);

  /**
   * Executes the Script as Test.
   *
   * @param scriptfile File containing the script
   * @param imagedirectory Directory containing the images (might be null: parent of script)
   * @param scriptArgs Arguments to be passed directly to the script with --args
   * @param forIDE when called from Sikuli IDE additional info
   * @return exitcode for the script execution
   */
  public int runTest(File scriptfile, File imagedirectory, String[] scriptArgs, String[] forIDE);

  /**
   * Starts an interactive session with the scriptrunner.
   *
   * @param scriptArgs Arguments to be passed directly to the script with --args
   * @return exitcode of the interactive session
   */
  public int runInteractive(String[] scriptArgs);

  /**
   * Gets the scriptrunner specific help text to print on stdout.
   *
   * @return A helping description about how to use the scriptrunner
   */
  public String getCommandLineHelp();

  /**
   * Gets the help text that is shown if the user runs "shelp()" in interactive mode
   *
   * @return The helptext
   */
  public String getInteractiveHelp();

  /**
   * Gets the name of the ScriptRunner. Should be unique. This value is needed to distinguish
   * between different ScriptRunners.
   *
   * @return Name to identify the ScriptRunner or null if not available
   */
  public String getName();

  /**
   * returns the list of possible script file endings, first is the default
   *
   * @return array of strings
   */
  public String[] getFileEndings();

  /**
   * checks wether this ScriptRunner supports the given fileending
   *
   * @return the lowercase fileending
   */
  public String hasFileEnding(String ending);

  /**
   * Is executed before Sikuli closes. Can be used to cleanup the ScriptRunner
   */
  public void close();

  /**
   * generic interface to a special runner action
   * @param action identifies what to do
   * @param args contains the needed parameters
   * @return true if successful, false otherwise
   */
  public boolean doSomethingSpecial(String action, Object[] args);

  /**
   * add statements to be run after SCRIPT_HEADER, but before script is executed
   *
   * @param stmts string array of statements (null resets the statement buffer)
   */
  public void execBefore(String[] stmts);

  /**
   * add statements to be run after script has ended
   *
   * @param stmts string array of statements (null resets the statement buffer)
   */
  public void execAfter(String[] stmts);
}
