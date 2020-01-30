/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
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
	 * Runs the script
	 */
	RUN("run", "r", "foobar.sikuli", "run script"),
	/**
	 * Prints all errormessages to stdout
	 */
	CONSOLE("console", "c", null, "print all output to commandline (IDE message area)"),
	/**
	 * special debugging during startup
	 */
	VERBOSE("verbose", "v", null, "Debug level 3 and elapsed time during startup"),
	/**
	 * special debugging during startup
	 */
	QUIET("quiet", "q", null, "show nothing"),
	/**
	 * Preloads script in IDE
	 */
	LOAD("load", "l", "one or more foobar.sikuli", "preload scripts in IDE"),
	/**
	 * run as server
	 */
	SERVER("server", "s", "ip:port | ip port", "run as server, listen on ip, port"),
	/**
	 * define group shortcuts
	 */
	GROUPS("groups", "g", "groups", "define group -> folder"),
	/**
	 * run as server
	 */
	XTRA("xtra", "x", "extra", "some extra options"),
	/**
	 * run the server for Python
	 */
	PYTHONSERVER("pythonserver", "p", "Python support", "use SikuliX features from Python"),
	/**
	 * allow multiple IDE
	 */
	MULTI("multi", "m", "more than one IDE", "two or more IDE instances are allowed");

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
