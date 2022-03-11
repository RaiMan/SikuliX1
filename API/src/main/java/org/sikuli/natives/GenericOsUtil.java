/*
 * Copyright (c) 2010-2022, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.natives;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.sikuli.basics.Debug;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class GenericOsUtil implements OSUtil {

  protected static class GenericOsProcess extends OsProcess {

    private ProcessHandle process;

    public GenericOsProcess(ProcessHandle process) {
      this.process = process;
    }

    @Override
    public long getPid() {
      return process.pid();
    }

    @Override
    public String getExecutable() {
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
  public List<OsProcess> findProcesses(String name) {
    final String usedName = name.toLowerCase();
    Stream<OsProcess> osProcessStream = allProcesses();
    Stream<OsProcess> processStream = osProcessStream.filter((p) -> {
      String procName;
      try {
        procName = p.getExecutable().toLowerCase();
      } catch (Exception e) {
        //e.printStackTrace();
        return false;
      }
      return FilenameUtils.getBaseName(procName).equals(FilenameUtils.getBaseName(usedName));
    });
    List<OsProcess> processList = processStream.collect(Collectors.toList());
    return processList;
  }

  @Override
  public List<OsProcess> findProcesses(long pidGiven) {
    Stream<OsProcess> osProcessStream = allProcesses();
    Stream<OsProcess> processStream = osProcessStream.filter((p) -> {
      long pid = p.getPid();
      return pid == pidGiven;
    });
    List<OsProcess> processList = processStream.collect(Collectors.toList());
    return processList;
  }

  @Override
  public List<OsProcess> getProcesses() {
    return allProcesses().collect(Collectors.toList());
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
      Debug.error("GenericOSUtil.open:\n" + e.getMessage());
      return null;
    }
  }

  protected String[] openCommand(String[] cmd, String workDir) {
    return cmd;
  }

  protected ProcessHandle openGetProcess(Process p, String[] cmd, int waitTime) {
    return p.toHandle();
  }

  protected static Stream<OsProcess> allProcesses() {
    return ProcessHandle.allProcesses().map((h) -> new GenericOsProcess(h));
  }
}
