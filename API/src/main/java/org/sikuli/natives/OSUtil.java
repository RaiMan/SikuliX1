/*
 * Copyright (c) 2010-2021, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.natives;

import java.awt.Rectangle;
import java.util.List;

public interface OSUtil {

	public interface OsProcess {

		long getPid();

		String getExecutable();

		boolean isRunning();

		boolean close(boolean force);
	}

	public interface OsWindow {
		OsProcess getProcess();

		String getTitle();

		Rectangle getBounds();

		boolean focus();

		boolean focus(int winNum);

		boolean minimize();

		boolean maximize();

		boolean restore();
	}

	/**
	 * check if needed command libraries or packages are installed and working<br>
	 * if not ok, respective features will do nothing but issue error messages
	 */
	void init();

	List<OsProcess> findProcesses(String name);

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
