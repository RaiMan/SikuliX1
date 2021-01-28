/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.natives;

import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.sikuli.basics.Debug;

public class LinuxUtil extends GenericOsUtil {

	private static final class LinuxWindow implements OsWindow {
		private long id;

		public LinuxWindow(long id) {
			this.id = id;
		}

		@Override
		public OsProcess getProcess() {
			List<String> lines = xdotool(new String[] { "getwindowpid", Long.toString(id) });

			if (!lines.isEmpty()) {
				Optional<ProcessHandle> handle = ProcessHandle.of(Long.parseLong(lines.get(0)));
				if (handle.isPresent()) {
					return new GenericOsProcess(handle.get());
				}
			}
			return null;
		}

		@Override
		public String getTitle() {
			List<String> lines = xdotool(new String[] { "getwindowname", Long.toString(id) });
			return lines.stream().findFirst().orElse("");
		}

		@Override
		public Rectangle getBounds() {
			List<String> lines = xdotool(new String[] { "getwindowgeometry", "--shell", Long.toString(id) });

			Properties props = new Properties();
			try {
				props.load(new StringReader(String.join("\n", lines)));
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}

			return new Rectangle(Integer.parseInt(props.getProperty("X")), Integer.parseInt(props.getProperty("Y")),
					Integer.parseInt(props.getProperty("WIDTH")), Integer.parseInt(props.getProperty("HEIGHT")));
		}

		@Override
		public boolean focus() {
			xdotool(new String[] { "windowactivate", "--sync", Long.toString(id) });
			return true;
		}

		@Override
		public boolean equals(Object other) {
			return other != null && other instanceof LinuxWindow && this.id == (((LinuxWindow) other).id);
		}
	}

	private static boolean xdotoolAvailable = false;

	static {
		try {
			Process p = Runtime.getRuntime().exec("xdotool -v");
			int exitValue = p.waitFor();
			if (exitValue != 0) {
				throw new RuntimeException("Bad exit value: " + exitValue);
			}
			xdotoolAvailable = true;
		} catch (Exception e) {
			Debug.error("xdotool not available.\n"
					+ "While you can use purely process based functionallity of the App class (e.g. open(), close(), isRunning()), you need to install xdotool to use window based stuff.\n"
					+ "Error message: %s", e.getMessage());
		}
	}

	@Override
	public List<OsWindow> findWindows(String title) {
		List<String> lines = xdotool(new String[] { "search", "--onlyvisible", "--name", title });
		return lines.stream().map((l) -> new LinuxWindow(Long.parseLong(l))).collect(Collectors.toList());
	}

	@Override
	public List<OsWindow> getWindows(OsProcess process) {
		List<String> lines = xdotool(
				new String[] { "search", "--onlyvisible", "--pid", Long.toString(process.getPid()) });
		return lines.stream().map((l) -> new LinuxWindow(Long.parseLong(l))).collect(Collectors.toList());
	}

	@Override
	public OsWindow getFocusedWindow() {
		List<String> lines = xdotool(new String[] { "getactivewindow" });

		if (!lines.isEmpty()) {
			return new LinuxWindow(Long.parseLong(lines.get(0)));
		}
		return null;
	}

	private static List<String> xdotool(String[] args) {	
		List<String> lines = new ArrayList<>();

		if (xdotoolAvailable) {
			String[] cmd = ArrayUtils.insert(0, args, "xdotool");

			try {
				Process p = Runtime.getRuntime().exec(cmd);

				try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
					String line;

					while ((line = reader.readLine()) != null) {
						lines.add(line);
					}

					return lines;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return lines;
	}
}
