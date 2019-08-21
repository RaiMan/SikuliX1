/*
 * Copyright (c) 2010-2019, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.util;

import org.apache.commons.cli.*;
import org.sikuli.basics.Debug;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandArgs {

  private Options cmdArgs;
  ArrayList<String> userArgs = new ArrayList<String>();
  ArrayList<String> sikuliArgs = new ArrayList<String>();
  static String argsOrg = "";

  public CommandArgs() {
    init();
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
    StringBuilder temp = null;
    Pattern pat;
    Matcher m;
    List<String> nargs = new ArrayList<String>();
    for (String arg : args) {
      if (arg.startsWith("asApp")) {
        continue;
      }
      if (arg.startsWith(sep)) {
        if (!arg.endsWith(sep)) {
          temp = new StringBuilder(arg.substring(1));
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
        temp.append(" ").append(arg);
        continue;
      }
      nargs.add(arg);
    }
    return nargs.toArray(new String[0]);
  }

  public String[] getUserArgs() {
    return userArgs.toArray(new String[0]);
  }

  public String[] getSXArgs() {
    return sikuliArgs.toArray(new String[0]);
  }

  /**
   * Adds all options to the Options object
   */
  @SuppressWarnings("static-access")
  private void init() {
    cmdArgs = new Options();
    cmdArgs.addOption(CommandArgsEnum.HELP.shortname(),
        CommandArgsEnum.HELP.longname(), false, CommandArgsEnum.HELP.description());

    cmdArgs.addOption(
        OptionBuilder.withLongOpt(CommandArgsEnum.DEBUG.longname())
            .hasOptionalArg()
            .withArgName(CommandArgsEnum.DEBUG.argname())
            .withDescription(CommandArgsEnum.DEBUG.description())
            .create(CommandArgsEnum.DEBUG.shortname().charAt(0)));

    cmdArgs.addOption(
        OptionBuilder.withLongOpt(CommandArgsEnum.LOGFILE.longname())
            .hasOptionalArg()
            .withArgName(CommandArgsEnum.LOGFILE.argname())
            .withDescription(CommandArgsEnum.LOGFILE.description())
            .create(CommandArgsEnum.LOGFILE.shortname().charAt(0)));

    cmdArgs.addOption(
        OptionBuilder.withLongOpt(CommandArgsEnum.USERLOGFILE.longname())
            .hasOptionalArg()
            .withArgName(CommandArgsEnum.USERLOGFILE.argname())
            .withDescription(CommandArgsEnum.USERLOGFILE.description())
            .create(CommandArgsEnum.USERLOGFILE.shortname().charAt(0)));

    cmdArgs.addOption(CommandArgsEnum.CONSOLE.shortname(),
        CommandArgsEnum.CONSOLE.longname(), false, CommandArgsEnum.CONSOLE.description());

    cmdArgs.addOption(CommandArgsEnum.VERBOSE.shortname(),
        CommandArgsEnum.VERBOSE.longname(), false, CommandArgsEnum.VERBOSE.description());

    cmdArgs.addOption(CommandArgsEnum.QUIET.shortname(),
        CommandArgsEnum.QUIET.longname(), false, CommandArgsEnum.QUIET.description());

    cmdArgs.addOption(CommandArgsEnum.MULTI.shortname(),
        CommandArgsEnum.MULTI.longname(), false, CommandArgsEnum.MULTI.description());

    cmdArgs.addOption(
        OptionBuilder.withLongOpt(CommandArgsEnum.SERVER.longname())
            .hasOptionalArg()
            .withArgName(CommandArgsEnum.SERVER.argname())
            .withDescription(CommandArgsEnum.SERVER.description())
            .create(CommandArgsEnum.SERVER.shortname().charAt(0)));

    cmdArgs.addOption(
        OptionBuilder.withLongOpt(CommandArgsEnum.PYTHONSERVER.longname())
            .hasOptionalArg()
            .withArgName(CommandArgsEnum.PYTHONSERVER.argname())
            .withDescription(CommandArgsEnum.PYTHONSERVER.description())
            .create(CommandArgsEnum.PYTHONSERVER.shortname().charAt(0)));

    cmdArgs.addOption(
        OptionBuilder.withLongOpt(CommandArgsEnum.LOAD.longname())
            .withDescription(CommandArgsEnum.LOAD.description())
            .hasOptionalArgs()
            .withArgName(CommandArgsEnum.LOAD.argname())
            .create(CommandArgsEnum.LOAD.shortname().charAt(0)));

    cmdArgs.addOption(
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
        cmdArgs,
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

  public CommandLine getCommandLine(String[] args) {
    CommandLineParser parser = new PosixParser();
    CommandLine cmd = null;

    boolean isUserArg = false;
    for (String arg : args) {
      if (!isUserArg && arg.startsWith("--")) {
        isUserArg = true;
        continue;
      }
      if (isUserArg) {
        userArgs.add(arg);
      } else {
        sikuliArgs.add(arg);
      }
    }
    try {
      cmd = parser.parse(cmdArgs, sikuliArgs.toArray(new String[]{}), true);
    } catch (ParseException exp) {
      Debug.error(exp.getMessage());
    }
    return cmd;
  }

  public String getArgsOrg() {
    return argsOrg;
  }
}
