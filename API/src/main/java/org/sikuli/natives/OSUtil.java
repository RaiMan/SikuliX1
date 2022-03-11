/*
 * Copyright (c) 2010-2022, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.natives;

import java.awt.Rectangle;
import java.util.List;

public interface OSUtil {

	public abstract static class OsProcess {

		public abstract long getPid();

		public abstract String getExecutable();

		public abstract boolean isRunning();

		public abstract boolean close(boolean force);
	}

	public abstract static class OsWindow {
		public abstract OsProcess getProcess();

		public abstract String getTitle();

		public abstract Rectangle getBounds();

		public abstract boolean focus();

		public abstract boolean focus(int winNum); //TODO macOS trick: window not directly focusable

		public abstract boolean minimize();

		public abstract boolean maximize();

		public abstract boolean restore();

		public String toString() {
			Rectangle r = getBounds();
			return String.format("%s (%d,%d %dx%d)", getTitle(), r.x, r.y, r.width, r.height);
		}
	}

	/**
	 * check if needed command libraries or packages are installed and working<br>
	 * if not ok, respective features will do nothing but issue error messages
	 */
	void init();

	List<OsProcess> findProcesses(String name);

	List<OsProcess> findProcesses(long pid);

	List<OsWindow> findWindows(String title);

	List<OsWindow> getWindows(OsProcess process);

	List<OsWindow> getWindows();

	List<OsWindow> getAppWindows();

	List<OsProcess> getProcesses();

	boolean isUserApp(OsProcess process);

	OsProcess open(String[] cmd, String workDir, int waitTime);

	OsWindow getFocusedWindow();

	OsProcess getFocusedProcess();
}
