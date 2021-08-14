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
			ProcessHandle.Info info = null;
			try {
				info = process.info();
			} catch (Exception e) {
				return "";
			}
			return info.command().orElse("");
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
		Stream<OsProcess> osProcessStream = allProcesses();
		Stream<OsProcess> processStream = osProcessStream.filter((p) -> {
			String procName = "";
			try {
				procName = p.getName().toLowerCase();
			} catch (Exception e) {
				//e.printStackTrace();
				return false;
			}
			return FilenameUtils.getBaseName(procName).equals(FilenameUtils.getBaseName(name.toLowerCase()));
		});
		List<OsProcess> processList = processStream.collect(Collectors.toList());
		return processList;
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
	public OsProcess open(String[] cmd, String workDir, int waitTime) {
		try {

			cmd = openCommand(cmd, workDir);

			ProcessBuilder pb = new ProcessBuilder(cmd);

			if (StringUtils.isNotBlank(workDir)) {
				pb.directory(new File(workDir));
			}

			Process p = pb.start();

			ProcessHandle pHandle = openGetProcess(p, cmd, waitTime);
			if (pHandle == null) {
				return null;
			} else {
				return new GenericOsProcess(pHandle);
			}
		} catch (Exception e) {
			System.out.println("[error] GenericOSUtil.open:\n" + e.getMessage());
			return null;
		}
	}

	protected String[] openCommand(String[] cmd, String workDir) {
		return cmd;
	}

	protected ProcessHandle openGetProcess(Process p, String[] cmd, int waitTime) {
		return p.toHandle();
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
