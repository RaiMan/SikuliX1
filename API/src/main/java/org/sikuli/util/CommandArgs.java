/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.sikuli.basics.Debug;

public class CommandArgs {

  private static String _callerType = "";
  Options _options;
  ArrayList<String> userArgs = new ArrayList<String>();
  ArrayList<String> sikuliArgs = new ArrayList<String>();
  static String argsOrg = "";

  private static boolean isIDE(String callerType) {
    return ("IDE".equals(callerType));
  }

  public static boolean isIDE() {
    return ("IDE".equals(_callerType));
  }

  private static boolean isScript(String callerType) {
    return ("SCRIPT".equals(callerType));
  }

  public static boolean isScript() {
    return ("SCRIPT".equals(_callerType));
  }

  private static boolean isOther(String callerType) {
    return (!isIDE(callerType) && !isScript(callerType));
  }

  public CommandArgs(String type) {
    if (!isIDE(type) && !isScript(type)) {
      Debug.error("Commandline Parser not configured for " + type);
      _callerType = "OTHER";
    } else {
      _callerType = type;
    }
    init();
  }

  public CommandLine getCommandLine(String[] args) {
    CommandLineParser parser = new PosixParser();
    CommandLine cmd = null;

    boolean isUserArg = false;
    for (int i=0; i < args.length; i++) {
      if (!isUserArg && args[i].startsWith("--")) {
        isUserArg = true;
        continue;
      }
      if (isUserArg) {
        userArgs.add(args[i]);
      } else {
        sikuliArgs.add(args[i]);
      }
    }
    try {
      cmd = parser.parse(_options, sikuliArgs.toArray(new String[]{}), true);
    } catch (ParseException exp) {
      Debug.error(exp.getMessage());
    }
    return cmd;
  }

  public String[] getUserArgs() {
    return userArgs.toArray(new String[0]);
  }

  public String[] getSikuliArgs() {
    return sikuliArgs.toArray(new String[0]);
  }

  /**
   * Adds all options to the Options object
   */
  @SuppressWarnings("static-access")
  private void init() {
    _options = new Options();
    _options.addOption(CommandArgsEnum.HELP.shortname(),
            CommandArgsEnum.HELP.longname(), false, CommandArgsEnum.HELP.description());

    _options.addOption(
            OptionBuilder.withLongOpt(CommandArgsEnum.DEBUG.longname())
            .hasOptionalArg()
            .withArgName(CommandArgsEnum.DEBUG.argname())
            .withDescription(CommandArgsEnum.DEBUG.description())
            .create(CommandArgsEnum.DEBUG.shortname().charAt(0)));

    _options.addOption(
            OptionBuilder.withLongOpt(CommandArgsEnum.LOGFILE.longname())
            .hasOptionalArg()
            .withArgName(CommandArgsEnum.LOGFILE.argname())
            .withDescription(CommandArgsEnum.LOGFILE.description())
            .create(CommandArgsEnum.LOGFILE.shortname().charAt(0)));

    _options.addOption(
            OptionBuilder.withLongOpt(CommandArgsEnum.USERLOGFILE.longname())
            .hasOptionalArg()
            .withArgName(CommandArgsEnum.USERLOGFILE.argname())
            .withDescription(CommandArgsEnum.USERLOGFILE.description())
            .create(CommandArgsEnum.USERLOGFILE.shortname().charAt(0)));

    _options.addOption(CommandArgsEnum.CONSOLE.shortname(),
            CommandArgsEnum.CONSOLE.longname(), false, CommandArgsEnum.CONSOLE.description());

    _options.addOption(CommandArgsEnum.SPLASH.shortname(),
            CommandArgsEnum.SPLASH.longname(), false, CommandArgsEnum.SPLASH.description());

    _options.addOption(
            OptionBuilder.withLongOpt(CommandArgsEnum.INTERACTIVE.longname())
            .hasOptionalArg()
            .withArgName(CommandArgsEnum.INTERACTIVE.argname())
            .withDescription(CommandArgsEnum.INTERACTIVE.description())
            .create(CommandArgsEnum.INTERACTIVE.shortname().charAt(0)));

    _options.addOption(
            OptionBuilder.withLongOpt(CommandArgsEnum.SERVER.longname())
            .hasOptionalArg()
            .withArgName(CommandArgsEnum.SERVER.argname())
            .withDescription(CommandArgsEnum.SERVER.description())
            .create(CommandArgsEnum.SERVER.shortname().charAt(0)));

     _options.addOption(
            OptionBuilder.withLongOpt(CommandArgsEnum.LOAD.longname())
            .withDescription(CommandArgsEnum.LOAD.description())
            .hasOptionalArgs()
            .withArgName(CommandArgsEnum.LOAD.argname())
            .create(CommandArgsEnum.LOAD.shortname().charAt(0)));

    _options.addOption(
            OptionBuilder.withLongOpt(CommandArgsEnum.TEST.longname())
            .withDescription(CommandArgsEnum.TEST.description())
            .hasOptionalArgs()
            .withArgName(CommandArgsEnum.TEST.argname())
            .create(CommandArgsEnum.TEST.shortname().charAt(0)));

    _options.addOption(
            OptionBuilder.withLongOpt(CommandArgsEnum.RUN.longname())
            .withDescription(CommandArgsEnum.RUN.description())
            .hasOptionalArgs()
            .withArgName(CommandArgsEnum.RUN.argname())
            .create(CommandArgsEnum.RUN.shortname().charAt(0)));
  }

  /**
   * Prints the help
   */
  public void printHelp() {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp(80, "\n",
        "----- Running SikuliX " + "-------------",
      _options,
        "-----\n<foobar.sikuli> (.sikuli might be omitted, is assumed)\n"
      + "path relative to current working directory or absolute path\n"
      + "though deprecated: so called executables .skl can be used too\n"
      + "------\nanything after --\nor after something beginning with --\n"
      + "go to script as user parameters (respecting enclosing \")\n"
      + "------\n-d use this option if you encounter any weird problems\n"
      + "DebugLevel=3 and all output goes to <workingFolder>/SikuliLog.text\n"
      + "----------------------------------------------------------------",
      true);
  }

  public static String[] scanArgs(String[] args) {
//TODO detect leading and/or trailing blanks
    argsOrg = System.getenv("SIKULI_COMMAND");
    if (argsOrg == null) {
      argsOrg = System.getProperty("sikuli.SIKULI_COMMAND");
    }
    if (argsOrg == null) {
      argsOrg = "";
    }
    String sep = "\"";
    String temp = null;
    Pattern pat;
    Matcher m;
    List<String> nargs = new ArrayList<String>();
    for (String arg : args) {
      if (arg.startsWith("asApp")) {
        continue;
      }
      if (arg.startsWith(sep)) {
        if (!arg.endsWith(sep)) {
          temp = arg.substring(1);
          continue;
        }
      } else if (arg.endsWith(sep)) {
        if (temp != null) {
          arg = temp + " " + arg.substring(0, arg.length() - 1);
          if (argsOrg != null && !argsOrg.contains(arg)) {
            arg = arg.replace(" ", " *?");
            pat = Pattern.compile("(" + arg + ")");
            m = pat.matcher(argsOrg);
            if (m.find()) {
              arg = m.group();
            } else {
              arg = "?" + arg + "?";
            }
          }
          temp = null;
        }
      } else if (temp != null) {
        temp += " " + arg;
        continue;
      }
      nargs.add(arg);
    }
    return nargs.toArray(new String[0]);
  }

  public String getArgsOrg() {
    return argsOrg;
  }
}
