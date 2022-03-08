/*
 * Copyright (c) 2010-2022, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script.runners;

import org.apache.commons.exec.StreamPumper;
import org.apache.commons.lang3.ArrayUtils;
import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;
import org.sikuli.script.runnerSupport.IScriptRunner;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProcessRunner extends AbstractLocalFileScriptRunner {

	public static final String NAME = "Process";
	public static final String TYPE = "text/application";
	public static final String[] EXTENSIONS = new String[0];

	private Process process;
	private PrintStream stdOut;
	private PrintStream stdErr;

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String[] getExtensions() {
		return EXTENSIONS;
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	protected int doEvalScript(String script, IScriptRunner.Options options) {
		String extension = "script";
		String[] extensions = getExtensions();

		if (extensions.length > 0) {
			extension = extensions[0];
		}

		File file = FileManager.createTempFile(extension, null); //TODO
		FileManager.writeStringToFile(script, file);
		return runScript(file.getAbsolutePath(), null, options);
	}

	@Override
	protected int doRunScript(String scriptFile, String[] scriptArgs, IScriptRunner.Options options) {
		String[] cmdArgs = ArrayUtils.addAll(new String[] { scriptFile }, scriptArgs);

		int exitCode = 0;

		try {
			if (options.getOutStream() != null) {
				doRedirect(options.getOutStream());
			} else if (!isStdoutRedirected()) {
				doRedirect(System.out);
			}

			process = Runtime.getRuntime().exec(cmdArgs);

			startStreamPumper(process.getInputStream(), stdOut, options);
			startStreamPumper(process.getErrorStream(), stdErr, options);

			exitCode = process.waitFor();
		} catch (IOException | InterruptedException e) {
			if (!isAborted()) {
				Debug.error("%s failed: %s", getName(), e.getMessage());
				exitCode = 1;
			}
		}

		return exitCode;
	}

	@Override
	public boolean isAbortSupported() {
		return true;
	}

	@Override
	protected void doAbort() {
		if (process != null) {
			process.descendants().forEach((p) -> p.destroyForcibly());
			process.destroyForcibly();
		}
	}

	@Override
	protected boolean doRedirect(PrintStream stdout, PrintStream stderr) {
		this.stdOut = stdout;
		this.stdErr = stderr;
		return true;
	}

	protected boolean doRedirect(PrintStream stdout) {
		this.stdOut = stdout;
		this.stdErr = stdout;
		return true;
	}

	private void startStreamPumper(InputStream in, OutputStream out, IScriptRunner.Options options) {
		if (out != null && !options.isSilent()) {
			new Thread(new StreamPumper(in, out)).start();
		} else {
			// Ensure that we read the input stream buffer.
			// Process might block otherwise as soon as the buffer is full.
			new Thread(new StreamPumper(in, OutputStream.nullOutputStream(), true)).start();
		}
	}

	// TODO Check if rest of the file is really well placed here.
	// Hint: Most probably not :-)

	private static void p(String form, Object... args) {
		System.out.println(String.format(form, args));
	}

	public static String runCommand(String... args) throws IOException, InterruptedException {
		String result = "";
		String work = null;
		if (args.length > 0) {
			ProcessBuilder app = new ProcessBuilder();
			List<String> cmd = new ArrayList<String>();
			Map<String, String> processEnv = app.environment();
			for (String arg : args) {
				if (arg.startsWith("work=")) {
					work = arg.substring(5);
					continue;
				}
				if (arg.startsWith("javahome=")) {
					processEnv.put("JAVA_HOME", arg.substring(9));
					continue;
				}
				if (arg.startsWith("?")) {
					final String fName = arg.substring(1);
					String folder = null;
					if (null == work) {
						folder = System.getProperty("user.dir");
					} else {
						folder = work;
					}
					String[] fList = new File(folder).list(new FilenameFilter() {
						@Override
						public boolean accept(File dir, String name) {
							if (name.startsWith(fName))
								return true;
							return false;
						}
					});
					if (fList.length > 0) {
						arg = fList[0];
					}
				}
				cmd.add(arg);
			}
			app.directory(work == null ? null : new File(work));
			app.redirectErrorStream(true);
			app.command(cmd);
			Process process = app.start();
			InputStreamReader reader = new InputStreamReader(process.getInputStream());
			BufferedReader processOut = new BufferedReader(reader);
			String line = processOut.readLine();
			while (null != line) {
				result += line + "\n";
				line = processOut.readLine();
			}
			process.waitFor();
			int exitValue = process.exitValue();
			if (exitValue > 0) {
				result += "error";
			} else {
				result = "success\n" + result;
			}
		}
		return result;
	}

	public static String run(String... args) {
		List<String> cmd = new ArrayList<String>();
		for (String arg : args) {
			cmd.add(arg);
		}
		return run(cmd);
	}

	public static String run(List<String> cmd) {
		int exitValue = 0;
		String stdout = "";
		String NL = System.lineSeparator();
		if (cmd.size() > 0) {
			ProcessBuilder app = new ProcessBuilder();
			Map<String, String> processEnv = app.environment();
			app.command(cmd);
			app.redirectErrorStream(true);
			Process process = null;
			try {
				process = app.start();
			} catch (Exception e) {
				p("[Error] ProcessRunner: start: %s", e.getMessage());
			}
			try {
				if (process != null) {
					InputStreamReader reader = new InputStreamReader(process.getInputStream());
					BufferedReader processOut = new BufferedReader(reader);
					String line = processOut.readLine();
					while (null != line) {
						line = line.trim();
						if (!line.isEmpty()) {
							stdout += line + NL;
						}
						line = processOut.readLine();
					}
				}
			} catch (IOException e) {
				p("[Error] ProcessRunner: read: %s", e.getMessage());
			}
			try {
				if (process != null) {
					process.waitFor();
					exitValue = process.exitValue();
				}
			} catch (InterruptedException e) {
				p("[Error] ProcessRunner: waitFor: %s", e.getMessage());
			}
		}
		return "" + exitValue + NL + stdout;
	}

	public static void detach(String... args) {
		List<String> cmd = new ArrayList<String>();
		for (String arg : args) {
			cmd.add(arg);
		}
		detach(cmd);
	}

	public static void detach(List<String> cmd) {
		if (cmd.size() > 0) {
			String line = "";
			boolean shouldPrint = false;
			for (String item : cmd) {
				line += " " + item.trim();
				if ("-v".equals(item.trim())) {
					shouldPrint = true;
				}
			}
			if (shouldPrint) {
				System.out.println("[DEBUG] ProcessRunner::detach:");
				System.out.println(line.trim());
			}
			ProcessBuilder app = new ProcessBuilder();
			Map<String, String> processEnv = app.environment();
			app.command(cmd);
			app.redirectErrorStream(true);
			app.redirectInput(ProcessBuilder.Redirect.INHERIT);
			app.redirectOutput(ProcessBuilder.Redirect.INHERIT);
			try {
				app.start();
			} catch (Exception e) {
				p("[Error] ProcessRunner: start: %s", e.getMessage());
			}
		}
	}

	public static int runBlocking(List<String> cmd) {
		int exitValue = 0;
		if (cmd.size() > 0) {
			ProcessBuilder app = new ProcessBuilder();
			app.command(cmd);
			app.redirectInput(ProcessBuilder.Redirect.INHERIT);
			app.redirectOutput(ProcessBuilder.Redirect.INHERIT);
			Process process = null;
			try {
				process = app.start();
			} catch (Exception e) {
				p("[Error] ProcessRunner: start: %s", e.getMessage());
			}

			try {
				if (process != null) {
					process.waitFor();
					exitValue = process.exitValue();
				}
			} catch (InterruptedException e) {
				p("[Error] ProcessRunner: waitFor: %s", e.getMessage());
			}
		}
		return exitValue;
	}
}
