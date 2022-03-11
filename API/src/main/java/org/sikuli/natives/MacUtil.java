/*
 * Copyright (c) 2010-2022, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.natives;

import com.sun.jna.Pointer;
import com.sun.jna.platform.mac.CoreFoundation;
import com.sun.jna.platform.mac.CoreFoundation.CFArrayRef;
import com.sun.jna.platform.mac.CoreFoundation.CFDictionaryRef;
import com.sun.jna.platform.mac.CoreFoundation.CFNumberRef;
import com.sun.jna.platform.mac.CoreFoundation.CFStringRef;
import org.apache.commons.lang3.RegExUtils;
import org.sikuli.natives.mac.jna.CoreGraphics;
import org.sikuli.script.App;
import org.sikuli.script.runnerSupport.IScriptRunner;
import org.sikuli.script.runners.AppleScriptRunner;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MacUtil extends GenericOsUtil {

  private static final class MacWindow extends OsWindow {
    private long number;
    private String title;
    private long pid;
    private Rectangle bounds;

    public MacWindow(long number, String title, long pid, Rectangle bounds) {
      this.number = number;
      this.title = title;
      this.pid = pid;
      this.bounds = bounds;
    }

    @Override
    public OsProcess getProcess() {
      Optional<ProcessHandle> handle = ProcessHandle.of(pid);
      if (handle.isPresent()) {
        return new GenericOsProcess(handle.get());
      }
      return null;
    }

    @Override
    public String getTitle() {
      return title;
    }

    @Override
    public Rectangle getBounds() {
      return bounds;
    }

    @Override
    public boolean focus() {
      if (pid > 0) {
        String script = "tell application \"System Events\" to set frontmost of first process in (processes where unix id is %d) to true";
        script = String.format(script, pid);
        new AppleScriptRunner().evalScript(script, new IScriptRunner.Options().setSilent(true));
        return true;
      }
      return false;
    }

    @Override
    public boolean focus(int winNum) {
      if (focus()) {
        String script = "tell application \"System Events\" to perform action \"AXRaise\" " +
                "of item %d of (windows of item 1 of (processes whose unix id is %d))";
        script = String.format(script, winNum + 1, pid);
        new AppleScriptRunner().evalScript(script, new IScriptRunner.Options().setSilent(true));
        return true;
      }
      return false;
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
      return other != null && other instanceof MacWindow && this.getProcess() == (((MacWindow) other).getProcess());
    }
  }

  @Override
  public List<OsWindow> findWindows(String title) {
    return allWindows().stream().filter((w) -> w.getTitle().contains(title)).collect(Collectors.toList());
  }

  @Override
  public OsProcess getFocusedProcess() {
    String script = "tell application \"System Events\" to " +
        "unix id of first process in (processes where frontmost is true)";
    final IScriptRunner.Options options = new IScriptRunner.Options().setOutput();
    new AppleScriptRunner().evalScript(script, options);
    final String spid = options.getOutput().strip();
    int pid = -1;
    try {
      pid = Integer.parseInt(spid);
    } catch (NumberFormatException e) {
    }
    if (pid > 0) {
      return getProcess(pid);
    }
    return null;
  }

  private OsProcess getProcess(int pid) {
    Optional<ProcessHandle> handle = ProcessHandle.of(pid);
    if (handle.isPresent()) {
      return new GenericOsProcess(handle.get());
    }
    return null;
  }

  static Pattern isApp = Pattern.compile(".*?/(Applications)/.*?/Contents/(.*)");

  @Override
  public boolean isUserApp(OsProcess process) {
    String exec = process.getExecutable();
    if (exec.isEmpty()) {
      return false;
    }
    if (getWindows(process).size() > 0) {
      if (getWindows(process).get(0).getTitle().toLowerCase().contains("item-0") || getWindows(process).get(0).getBounds().y == 0) {
        return false;
      }
    }
    Matcher matcher = isApp.matcher(exec);
    if (matcher.matches()) {
      if (matcher.group(2).startsWith("MacOS/")) {
        return true;
      }
    }
    return false;
  }

  @Override
  public OsWindow getFocusedWindow() {
    List<OsWindow> windows = getWindows(getFocusedProcess());
    return windows.size() > 0 ? windows.get(0) : null;
  }


  @Override
  public List<OsWindow> getWindows(OsProcess process) {
    if (process != null) {
      List<OsWindow> windows = allWindows().stream().filter((w) -> process.equals(w.getProcess())).collect(Collectors.toList());
      return windows;
    }
    return new ArrayList<>(0);
  }

  @Override
  public List<OsWindow> getWindows() {
    return allWindows();
  }

  @Override
  public List<OsWindow> getAppWindows() {
    return allWindows().stream().filter((w) -> w.getBounds().y > 0).collect(Collectors.toList());
  }

  private static List<OsWindow> allWindows() {
    ArrayList<OsWindow> windows = new ArrayList<>();

    CFArrayRef windowInfo = CoreGraphics.INSTANCE.CGWindowListCopyWindowInfo(
        CoreGraphics.kCGWindowListExcludeDesktopElements | CoreGraphics.kCGWindowListOptionOnScreenOnly, 0);

    try {
      int numWindows = windowInfo.getCount();
      for (int i = 0; i < numWindows; i++) {
        Pointer pointer = windowInfo.getValueAtIndex(i);
        CFDictionaryRef windowRef = new CFDictionaryRef(pointer);

        Pointer numberPointer = windowRef.getValue(CoreGraphics.kCGWindowNumber);
        long windowNumber = new CFNumberRef(numberPointer).longValue();

        Pointer pidPointer = windowRef.getValue(CoreGraphics.kCGWindowOwnerPID);
        long windowPid = new CFNumberRef(pidPointer).longValue();

        String windowName = "";
        Pointer namePointer = windowRef.getValue(CoreGraphics.kCGWindowName);

        if (namePointer != null) {
          CFStringRef nameRef = new CFStringRef(namePointer);

          if (CoreFoundation.INSTANCE.CFStringGetLength(nameRef).intValue() > 0) {
            windowName = new CFStringRef(namePointer).stringValue();
          }
        }

        Pointer boundsPointer = windowRef.getValue(CoreGraphics.kCGWindowBounds);
        CoreGraphics.CGRectRef rect = new CoreGraphics.CGRectRef();

        boolean result = CoreGraphics.INSTANCE.CGRectMakeWithDictionaryRepresentation(boundsPointer, rect);

        Rectangle javaRect = null;

        if (result) {
          int x = (int) rect.origin.x;
          int y = (int) rect.origin.y;
          int width = (int) rect.size.width;
          int height = (int) rect.size.height;

          javaRect = new Rectangle(x, y, width, height);
        }
        if (!windowName.isEmpty() && !javaRect.isEmpty()) {
          windows.add(new MacWindow(windowNumber, windowName, windowPid, javaRect));
        }
      }
    } finally {
      windowInfo.release();
    }

    return windows;
  }

  protected String[] openCommand(String[] cmd, String workDir) {
    //open -a cmd[0] --args cmd[1:]
    String[] newCmd = new String[4 + cmd.length - 1];
    newCmd[0] = "open";
    newCmd[1] = "-a";
    newCmd[2] = cmd[0];
    newCmd[3] = "--args";
    System.arraycopy(cmd, 1, newCmd, 4, cmd.length - 1);
    return newCmd;
  }

  protected ProcessHandle openGetProcess(Process p, String[] cmd, int waitTime) {
    //return p.toHandle();
    List<OsProcess> processes;
    do {
      App.pause(1);
      processes = findProcesses(cmd[2]);
    } while (processes.size() == 0 && --waitTime > 0);
    if (processes.size() == 0) {
      return null;
    } else {
      final long pid = processes.get(0).getPid();
      final Optional<ProcessHandle> handle = ProcessHandle.of(pid);
      return handle.get();
    }
  }
}

