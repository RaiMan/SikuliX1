/*
 * Copyright (c) 2010-2022, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.natives;

import java.awt.Rectangle;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import org.sikuli.basics.Debug;
import org.sikuli.script.runners.ProcessRunner;
import org.sikuli.script.runnerSupport.IScriptRunner;

public class LinuxUtil extends GenericOsUtil {

	private static final class LinuxWindow extends OsWindow {
		private long id;

		public LinuxWindow(long id) {
			this.id = id;
		}

		@Override
		public OsProcess getProcess() {
			try {
				List<String> lines = xdotool(new String[] { "getwindowpid", Long.toString(id) });

				if (!lines.isEmpty()) {
					Optional<ProcessHandle> handle = ProcessHandle.of(Long.parseLong(lines.get(0)));
					if (handle.isPresent()) {
						return new GenericOsProcess(handle.get());
					}
				}

				return null;
			} catch (XdotoolException e) {
				return null;
			}
		}

		@Override
		public String getTitle() {
			try {
				List<String> lines = xdotool(new String[] { "getwindowname", Long.toString(id) });
				return lines.stream().findFirst().orElse("");
			} catch (XdotoolException e) {
				return "";
			}
		}

		@Override
		public Rectangle getBounds() {
			try {
				List<String> lines = xdotool(new String[] { "getwindowgeometry", "--shell", Long.toString(id) });

				if (!lines.isEmpty()) {
					Properties props = new Properties();
					try {
						props.load(new StringReader(String.join("\n", lines)));
					} catch (IOException e) {
						e.printStackTrace();
						return null;
					}

					return new Rectangle(Integer.parseInt(props.getProperty("X")),
							Integer.parseInt(props.getProperty("Y")), Integer.parseInt(props.getProperty("WIDTH")),
							Integer.parseInt(props.getProperty("HEIGHT")));
				}

				return null;
			} catch (XdotoolException e) {
				return null;
			}
		}

		@Override
		public boolean focus() {
			try {
				xdotool(new String[] { "windowactivate", "--sync", Long.toString(id) });
				return true;
			} catch (XdotoolException e) {
				return false;
			}
		}

		@Override
		public boolean focus(int winNum) {
			return  focus();
		}

		@Override
		public boolean minimize() {
			throw new UnsupportedOperationException("minimize not implemented");
		}

		@Override
		public boolean maximize() {
			throw new UnsupportedOperationException("maximize not implemented");
		}

		@Override
		public boolean restore() {
			throw new UnsupportedOperationException("restore not implemented");
		}

		@Override
		public boolean equals(Object other) {
			return other != null && other instanceof LinuxWindow && this.id == (((LinuxWindow) other).id);
		}
	}

	private static boolean xdotoolAvailable = true;
	private static ProcessRunner xdotoolRunner = new ProcessRunner();

	static {
		try {
			xdotool(new String[] { "-v" });
		} catch (Exception e) {
			xdotoolAvailable = false;
			Debug.error("xdotool not available.\n"
					+ "While you can use purely process based functionallity of the App class (e.g. open(), close(), isRunning()), you need to install xdotool to use window based stuff.\n"
					+ "Error message: %s", e.getMessage());
		}
	}

	@SuppressWarnings("serial")
	private static class XdotoolException extends RuntimeException {
		public XdotoolException(String message) {
			super(message);
		}
	};

	private static synchronized List<String> xdotool(String[] args) {
		if (xdotoolAvailable) {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ByteArrayOutputStream err = new ByteArrayOutputStream();

			xdotoolRunner.redirect(new PrintStream(out), new PrintStream(err));

			int exitCode = xdotoolRunner.runScript("xdotool", args, new IScriptRunner.Options());

			if (exitCode != 0) {
				String message = String.format("xdotool failed with code %s: %s", exitCode, err);
				throw new XdotoolException(message);
			}

			return Arrays.asList(out.toString().trim().split("\n"));
		}
		return new ArrayList<>(0);
	}

	@Override
	public List<OsWindow> getWindows() {
		throw new UnsupportedOperationException("getWindows not implemented");
	}

	@Override
	public List<OsWindow> getAppWindows() { //TODO
		throw new UnsupportedOperationException("getAppWindows not implemented");
	}

	@Override
	public List<OsWindow> findWindows(String title) {
		try {
			List<String> lines = xdotool(new String[] { "search", "--onlyvisible", "--name", title });
			return lines.stream().map((l) -> new LinuxWindow(Long.parseLong(l))).collect(Collectors.toList());
		} catch (XdotoolException e) {
			return new ArrayList<>(0);
		}
	}

	@Override
	public List<OsWindow> getWindows(OsProcess process) {
		try {
			List<String> lines = xdotool(
					new String[] { "search", "--onlyvisible", "--pid", Long.toString(process.getPid()) });
			return lines.stream().map((l) -> new LinuxWindow(Long.parseLong(l))).collect(Collectors.toList());
		} catch (XdotoolException e) {
			return new ArrayList<>(0);
		}
	}

	@Override
	public OsWindow getFocusedWindow() {
		try {
			List<String> lines = xdotool(new String[] { "getactivewindow" });

			if (!lines.isEmpty()) {
				return new LinuxWindow(Long.parseLong(lines.get(0)));
			}
		} catch (XdotoolException e) {
			return null;
		}

		return null;
	}

	@Override
	public OsProcess getFocusedProcess() {
		final OsWindow focusedWindow = getFocusedWindow();
		return focusedWindow.getProcess();
	}

	@Override
	public boolean isUserApp(OsProcess process) { //TODO
		return true;
	}
}
