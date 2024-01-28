/*
 * Copyright (c) 2010-2022, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.util;

import org.apache.commons.cli.*;
import org.sikuli.script.support.Commons;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandArgs {

  private Options cmdArgs;
  ArrayList<String> userArgs = new ArrayList<>();
  ArrayList<String> sikuliArgs = new ArrayList<>();
  List<String> extendedArgs = new ArrayList<>();

  private boolean isIDE = false;

  public CommandArgs(boolean isIDE) {
    this.isIDE = isIDE;
    init();
  }

  public CommandLine getCommandLine(String[] args) {
    CommandLineParser parser = new DefaultParser();
    CommandLine cmd = null;

    boolean isUserArg = false;
    for (int i = 0; i < args.length; i++) {
      if (!isUserArg && args[i].startsWith("--")) {
        if (args[i].equalsIgnoreCase("--reset")) {
          extendedArgs.add("reset");
          continue;
        }
        if (args[i].equalsIgnoreCase("--jruby")) {
          extendedArgs.add("jruby");
          continue;
        }
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
      cmd = parser.parse(cmdArgs, sikuliArgs.toArray(new String[]{}), true);
    } catch (ParseException exp) {
      Commons.terminate(isIDE ? 998 : 999, "%s", exp.getMessage());
    }
    return cmd;
  }

  public String[] getUserArgs() {
    return userArgs.toArray(new String[0]);
  }

  public List<String> getExtendedArgs() {
    return extendedArgs;
  }
  /**
   * Adds all options to the Options object
   */
  @SuppressWarnings("static-access")
  private void init() {
    cmdArgs = new Options();
    cmdArgs.addOption(makeOption(CommandArgsEnum.QUIET));
    cmdArgs.addOption(makeOption(CommandArgsEnum.DEBUG));
    cmdArgs.addOption(makeOption(CommandArgsEnum.LOGFILE));
    cmdArgs.addOption(makeOption(CommandArgsEnum.USERLOGFILE));
    cmdArgs.addOption(makeOption(CommandArgsEnum.RUNPYSERVER));
    cmdArgs.addOption(makeOption(CommandArgsEnum.APPDATA));
    if (isIDE) {
      cmdArgs.addOption(makeOption(CommandArgsEnum.HELP));
      cmdArgs.addOption(makeOption(CommandArgsEnum.VERBOSE));
      cmdArgs.addOption(makeOption(CommandArgsEnum.CONSOLE));
      cmdArgs.addOption(makeOption(CommandArgsEnum.LOAD));
      cmdArgs.addOption(makeOption(CommandArgsEnum.RUN));
      cmdArgs.addOption(makeOption(CommandArgsEnum.RUNSERVER));
      cmdArgs.addOption(makeOption(CommandArgsEnum.RECORD));
    }
  }

  private Option makeOption(CommandArgsEnum anOption) {
    Option.Builder builder = Option.builder(anOption.shortname())
        .longOpt(anOption.longname())
        .desc(anOption.description());
    if (anOption.hasArgs() != null) {
      if (anOption.hasArgs()) {
        builder.hasArgs();
      } else {
        builder.hasArg(true).optionalArg(true);
      }
    }
    return builder.build();
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
            + "------\n-reset use this as the only option if the IDE does not start up\n"
            + "and start the IDE normally afterwards\n"
            + "----------------------------------------------------------------",
        true);
  }

  static String argsOrg = "";

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
