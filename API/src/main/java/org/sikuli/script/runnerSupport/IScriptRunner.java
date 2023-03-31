/*
 * Copyright (c) 2010-2022, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script.runnerSupport;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.sikuli.script.SikuliXception;

/**
 * Interface for ScriptRunners like Jython.
 */
public interface IScriptRunner {

  /**
   * Special options to pass to the runner.
   * <p>
   * Options are more to be interpreted as hints instead of strict directives.
   * A specific runner implementation can decide to ignore an option entirely.
   *
   * @author mbalmer
   */
  public class Options {

    public boolean isSilent() {
      return silent;
    }

    /**
     * Indicates that the runner should behave silent. E.g. not making
     * output to SDTIO.
     *
     * @param silent
     * @return this to allow chaining
     */
    public Options setSilent(boolean silent) {
      this.silent = silent;
      return this;
    }

    private boolean silent = false;

    /**
     * Get the error source line set by the runner.
     *
     * @return source line where the error happened
     */
    public int getErrorLine() {
      return errorLine;
    }

    /**
     * If a script run fails, the runner can set the source
     * line where the error happened.
     * <p>
     * There is no effect setting this from the outside.
     *
     * @param errorLine
     */
    public Options setErrorLine(int errorLine) {
      this.errorLine = errorLine;
      return this;
    }

    private int errorLine = -1;

    public String getOutput() {
      output = bout.toString();
      return output;
    }

    public PrintStream getOutStream() {
      if (output != null) {
        bout = new ByteArrayOutputStream();
        return new PrintStream(bout);
      } else {
        return null;
      }
    }

    public Options setOutput(String output) {
      this.output = output;
      return this;
    }

    public Options setOutput() {
      this.output = "";
      return this;
    }

    private String output = null;
    private ByteArrayOutputStream bout = null;

    public boolean isRunningInIDE() {
      return runningInIDE;
    }

    public Options setRunningInIDE() {
      this.runningInIDE = true;
      return this;
    }

    private boolean runningInIDE = false;

    public boolean isRunningFromScript() {
      return runningFromScript;
    }

    public Options setRunningFromScript() {
      this.runningFromScript = true;
      return this;
    }

    private boolean runningFromScript = false;

    public void setRunningInIDE(boolean runningInIDE) {
      this.runningInIDE = runningInIDE;
    }

    private long timeout = 0;

    public long getTimeout() {
      return timeout;
    }

    public Options setTimeout(long timeout) {
      this.timeout = timeout;
      return this;
    }

    public File getBaseFolder() {
      return baseFolder;
    }

    public void setBaseFolder(File baseFolder) {
      this.baseFolder = baseFolder;
    }

    private File baseFolder = null;
  }

  class EffectiveRunner {
    private IScriptRunner runner = null;
    String script = null;
    private Boolean bundle = false;

    public EffectiveRunner() {
    }

    public EffectiveRunner(IScriptRunner runner, String script, Boolean bundle) {
      this.runner = runner;
      this.script = script;
      this.bundle = bundle;
    }

    public IScriptRunner getRunner() {
      return runner;
    }

    public String getScript() {
      return script;
    }

    public boolean isBundle() {
      return bundle == null || bundle;
    }

    public boolean isTempBundle() {
      return bundle == null;
    }
  }

  /**
   * Can be used to initialize the ScriptRunner. This method is called at the beginning of program
   * execution. The given parameters can be used to parse any ScriptRunner specific custom options.
   *
   * @param args All arguments that were passed to the main-method
   */
  public void init(String[] args) throws SikuliXception;

  /**
   * Executes the Script.
   *
   * @param scriptFile Identifier pointing to the script. This can either by a file path
   *                   or an URI, depending on the runner implementation
   * @param scriptArgs Arguments to be passed directly to the script with --args
   * @param options    Implementation specific options.
   * @return exitcode for the script execution
   */
  public int runScript(String scriptFile, String[] scriptArgs, Options options);

  /**
   * Evaluates the Script.
   *
   * @param script  Script content
   * @param options Implementation specific options.
   * @return exitcode for the script execution
   */
  public int evalScript(String script, Options options);

  /**
   * Run the given script lines.
   * The implementation might perform some optimizations on
   * the code (e.g. fix indentation) before executing it.
   *
   * @param lines   Code do execute
   * @param options Implementation specific options.
   */
  public void runLines(String lines, Options options);

  /**
   * Checks if the current platform supports this runner.
   *
   * @return true if platform supports this runner, false otherwise
   */
  public boolean isSupported();

  /**
   * Gets the name of the ScriptRunner. Should be unique. This value is needed to distinguish
   * between different ScriptRunners.
   *
   * @return Name to identify the ScriptRunner or null if not available
   */
  public String getName();

  /**
   * returns the list of possible script file extensions, first is the default
   *
   * @return array of strings
   */
  public String[] getExtensions();

  /**
   * convenience: return the first defined extension (default)
   *
   * @return default extension
   */
  public String getDefaultExtension();

  /**
   * return the type of script this handler can execute.
   *
   * @return
   */
  public String getType();

  /**
   * checks whether this ScriptRunner supports the given file extension
   *
   * @return true if the runner has the given extension, false otherwise
   */
  public boolean hasExtension(String ending);

  /**
   * Is executed before Sikuli closes. Can be used to cleanup the ScriptRunner
   */
  public void close();

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

  /**
   * Checks if this runner can handle the given identifier.
   *
   * @param identifier Can be Runner name, type or one of its supported extensions
   *                   Can also be script code prefixed with the runnerName
   *                   (e.g. JavaScript*console.log("hello"))
   * @return true if the runner can handle the identifier, false otherwise
   */
  public boolean canHandle(String identifier);

  /**
   * Needed in the IDE to detect the runner when file or folder is opened
   *
   * @param script
   * @return array: EffectiveRunner object
   */
  public EffectiveRunner getEffectiveRunner(String script);

  /**
   * Redirects the runner's STDIO to the given PrintStream.
   * <p>
   * Subsequent calls to this function override the previously set streams.
   * <p>
   * If one of the parameters is set to null, STDIO redirection is reset to
   * System.out and System.err.
   *
   * @param stdout PrintStream for STDOUT
   * @param stderr PrintStream for STDERR
   */
  public void redirect(PrintStream stdout, PrintStream stderr);

  /**
   * Resets this runner.
   * <p>
   * The runner gets closed and initialized again using init.
   */
  public void reset();

  /**
   * @return true if the runner is currently executing a script, false otherwise
   */
  public boolean isRunning();

  /**
   * Aborts the current running script.
   * <p>
   * Not all runners can be aborted, please check abort support using isAbortSupported().
   */
  public void abort();

  /**
   * Checks if the currently running script has been aborted.
   *
   * @return true if the currently running script has been aborted, false otherwise.
   */
  public boolean isAborted();

  /**
   * Checks if abort is supported by this script runner implementation.
   *
   * @return true is abort is supported, false otherwise
   */
  public boolean isAbortSupported();

  /**
   * Usually the same as getExtensions() but with the leading dot.
   * <p>
   * Some files (e.g. $py.class) might have a somewhat unusual but
   * very specific file ending.
   *
   * @return An Array containing the supported line endings
   */
  public String[] getFileEndings();

  public List<String> getImportPlaces();

  public void adjustImportPath(Map<String, String> script, IScriptRunner.Options options);

  public void beforeScriptRun(String script, Options options);
  public void afterScriptRun(String script, Options options);
}
