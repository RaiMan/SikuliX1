/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.util;

/**
 * Enum that stores the info about the commandline args
 */
public enum CommandArgsEnum {

	/**
	 * Shows the help
	 */
	HELP("help", "h", null, "print this help message"),
	/**
	 * set debug level
	 */
	DEBUG("debug", "d", "debug level", "positive integer (1)"),
	/**
	 * outputfile for Sikuli logging messages
	 */
	LOGFILE("logfile", "f", "Sikuli logfile", "a valid filename (WorkingDir/SikuliLog.txt)"),
	/**
	 * outputfile for user logging messages
	 */
	USERLOGFILE("userlog", "u", "User logfile", "a valid filename (WorkingDir/UserLog.txt)"),
	/**
	 * Starts an interactive session
	 */
	INTERACTIVE("interactive", "i", "[runner (jython)]", "start interactive session and/or select ScriptRunner"),
	/**
	 * Runs the script
	 */
	RUN("run", "r", "foobar.sikuli", "run script"),
	/**
	 * Runs the script as testcase
	 */
	TEST("test", "t", "foobar.sikuli", "runs script as unittest"),
	/**
	 * Prints all errormessages to stdout
	 */
	CONSOLE("console", "c", null, "print all output to commandline (IDE message area)"),
	/**
	 * Prints all errormessages to stdout
	 */
	SPLASH("splash", "x", null, "show a splash screen to enter options"),
	/**
	 * Preloads script in IDE
	 */
	LOAD("load", "l", "one or more foobar.sikuli", "preload scripts in IDE"),
	/**
	 * run as server
	 */
	SERVER("server", "s", "[port (50001)]", "run as server on optional port");

	/**
	 * Longname of the parameter
	 */
	private String longname;

	/**
	 * Shortname of the parameter
	 */
	private String shortname;

	/**
	 * The param name
	 */
	private String argname;

	/**
	 * The description
	 */
	private String description;

	/**
	 * Returns the long name
	 *
	 * @return Longname of the parameter
	 */
	public String longname() {
		return longname;
	}

	/**
	 * Returns the short name
	 *
	 * @return Shortname of the parameter
	 */
	public String shortname() {
		return shortname;
	}

	/**
	 * Returns the argname
	 *
	 * @return The argname
	 */
	public String argname() {
		return argname;
	}

	/**
	 * Description for the param
	 *
	 * @return the description
	 */
	public String description() {
		return description;
	}

	/**
	 * Private constructor for class CommandArgsEnum.
	 *
	 * @param longname The long name for the param
	 * @param shortname The short name for the param
	 * @param argname The argname
	 * @param description The description for the Command Args
	 */
	private CommandArgsEnum(String longname, String shortname, String argname, String description) {
		this.longname = longname;
		this.shortname = shortname;
		this.argname = argname;
		this.description = description;
	}
}
