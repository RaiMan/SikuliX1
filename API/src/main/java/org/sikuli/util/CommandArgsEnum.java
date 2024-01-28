/*
 * Copyright (c) 2010-2022, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.util;

/**
 * Enum that stores the info about the commandline args
 */
public enum CommandArgsEnum {

	/**
	 * Shows the help
	 */
	HELP("help", "h", null, "print this help message and actual SikuliX preferences"),
	/**
	 * set debug level
	 */
	DEBUG("debug", "d", "debug level", "positive integer ", false),
	/**
	 * outputfile for Sikuli logging messages
	 */
	LOGFILE("logfile", "f", "Sikuli logfile", "a valid filename (WorkingDir/SikuliLog.txt)", false),
	/**
	 * outputfile for user logging messages
	 */
	USERLOGFILE("userlog", "u", "User logfile", "a valid filename (WorkingDir/UserLog.txt)", false),
	/**
	 * Runs the script
	 */
	RUN("run", "r", "foobar.sikuli", "run script", true),
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
	LOAD("load", "l", "one or more foobar.sikuli", "preload scripts in IDE", true),
	/**
	 * run as server
	 */
	RUNSERVER("server", "s", "[port (50001)]", "run as server on optional port", false),
	/**
	 * run the server for Python
	 */
	RUNPYSERVER("pythonserver", "p", "python support", "use SikuliX features from Python"),
	/**
	 * run in sandbox, multiple IDE instances allowed
	 */
	APPDATA("appdata", "a", "appdata path", "Path for SikuliX AppData (Sandbox)", false),
	/**
	 *  record a workflow (scrennshots, mouse and keyboard actions)
	 */
	RECORD("record", "o", "record", "record a workflow", false);
	/**
	 * Longname of the parameter
	 */
	private String longname;
	public String longname() {
		return longname;
	}

	/**
	 * Shortname of the parameter
	 */
	private String shortname;
	public String shortname() {
		return shortname;
	}

	/**
	 * The param name
	 */
	private String argname;
	public String argname() {
		return argname;
	}

	/**
	 * The description
	 */
	private String description;
	public String description() {
		return description;
	}

	/**
	 * has args
	 */
	private Boolean hasArgs;
	public Boolean hasArgs() {
		return hasArgs;
	}

	/**
	 * Private constructor for class CommandArgsEnum.
	 *
	 * @param longname The long name for the param
	 * @param shortname The short name for the param
	 * @param argname The argname
	 * @param description The description for the Command Args
	 */
	private CommandArgsEnum(String longname, String shortname, String argname, String description, boolean hasArgs) {
		this.longname = longname;
		this.shortname = shortname;
		this.argname = argname;
		this.description = description;
		this.hasArgs = hasArgs;
	}

	// variant having no args
	private CommandArgsEnum(String longname, String shortname, String argname, String description) {
		this.longname = longname;
		this.shortname = shortname;
		this.argname = argname;
		this.description = description;
		this.hasArgs = null;
	}
}
