/*
 * Copyright (c) 2010-2021, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.natives;

import java.awt.Rectangle;
import java.util.List;

public interface OSUtil {

	public interface OsProcess {

		long getPid();

		String getName();

		boolean isRunning();

		boolean close(boolean force);
	}

	public interface OsWindow {
		OsProcess getProcess();

		String getTitle();

		Rectangle getBounds();

		boolean focus();

		boolean minimize();

		boolean maximize();

		boolean restore();
	}

	/**
	 * check if needed command libraries or packages are installed and working<br>
	 * if not ok, respective features will do nothing but issue error messages
	 */
	void init();

	boolean isUserProcess(OsProcess process);

	List<OsProcess> findProcesses(String name);

	List<OsWindow> findWindows(String title);

	List<OsWindow> getWindows(OsProcess process);

	List<OsWindow> getWindows();

	List<OsProcess> getProcesses();

	OsProcess getProcess();

	OsProcess open(String[] cmd, String workDir);

	OsWindow getFocusedWindow();
}
