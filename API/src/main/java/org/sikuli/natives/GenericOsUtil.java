/*
 * Copyright (c) 2010-2021, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.natives;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class GenericOsUtil implements OSUtil {

	protected static class GenericOsProcess implements OsProcess {

		private ProcessHandle process;

		public GenericOsProcess(ProcessHandle process) {
			this.process = process;
		}

		@Override
		public long getPid() {
			return process.pid();
		}

		@Override
		public String getName() {
			return process.info().command().orElse("");
		}

		@Override
		public boolean isRunning() {
			return process.isAlive();
		}

		@Override
		public boolean close(boolean force) {
			if (force) {
				process.descendants().forEach((h) -> h.destroyForcibly());
				return process.destroyForcibly();
			} else {
				process.descendants().forEach((h) -> h.destroy());
				return process.destroy();
			}
		}

		@Override
		public boolean equals(Object other) {
			return other != null && other instanceof OsProcess && this.getPid() == ((OsProcess) other).getPid();
		}
	}

	@Override
	public void init() {
		// nothing to do
	}

	@Override
	public boolean isUserProcess(OsProcess process) {
		return true;
	}

	@Override
	public List<OsProcess> findProcesses(String name) {
		return allProcesses().filter((p) -> FilenameUtils.getBaseName(p.getName().toLowerCase()).equals(FilenameUtils.getBaseName(name.toLowerCase())))
				.collect(Collectors.toList());
	}

	@Override
	public List<OsWindow> findWindows(String title) {
		throw new UnsupportedOperationException("findWindows not implemented");
	}

	@Override
	public List<OsWindow> getWindows(OsProcess process) {
		throw new UnsupportedOperationException("getWindows not implemented");
	}

	@Override
	public List<OsWindow> getWindows() {
		throw new UnsupportedOperationException("getWindows not implemented");
	}

	@Override
	public List<OsProcess> getProcesses() {
		return allProcesses().collect(Collectors.toList());
	}

	public OsProcess getProcess() {
		return thisProcess();
	}

	@Override
	public OsProcess open(String[] cmd, String workDir) {
		try {

			ProcessBuilder pb = new ProcessBuilder(cmd);

			if (StringUtils.isNotBlank(workDir)) {
				pb.directory(new File(workDir));
			}

			Process p = pb.start();

			return new GenericOsProcess(p.toHandle());
		} catch (Exception e) {
			System.out.println("[error] WinUtil.open:\n" + e.getMessage());
			return null;
		}
	}

	@Override
	public OsWindow getFocusedWindow() {
		throw new UnsupportedOperationException("getFocusedWindow not implemented");
	}

	protected static Stream<OsProcess> allProcesses() {
		return ProcessHandle.allProcesses().map((h) -> new GenericOsProcess(h));
	}

	protected static OsProcess thisProcess() {
		return new GenericOsProcess(ProcessHandle.current());
	}
}
