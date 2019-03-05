/*
 * Copyright (c) 2010-2018, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.natives;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Psapi;
import com.sun.jna.platform.win32.Tlhelp32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.RECT;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.ptr.IntByReference;

import org.apache.commons.io.FilenameUtils;
import org.sikuli.basics.Debug;
import org.sikuli.script.App;
import org.sikuli.script.Region;
import org.sikuli.script.RunTime;
import org.sikuli.util.ProcessRunner;

import java.awt.Rectangle;
import java.awt.Window;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.sun.jna.platform.win32.Kernel32;

public class WinUtil implements OSUtil {

  static final int BUFFERSIZE = 32 * 1024 - 1;
  static final Kernel32 kernel32 = Kernel32.INSTANCE;
  static final SXUser32 sxuser32 = SXUser32.INSTANCE;
  static final User32 user32 = User32.INSTANCE;  
  static final Psapi psapi = Psapi.INSTANCE;
  
  public static final class ProcessInfo {    
    private int pid;  
    private String imageName;

    public ProcessInfo(
            final int pid,            
            final String imageName) {
        this.pid = pid;        
        this.imageName = imageName;
    }

    public int getPid() {
      return pid;
    }

    public String getImageName() {
      return imageName;
    }       
  }  
  
  public static final class WindowInfo{
    public HWND hwnd;
    public int pid;
    public String title;
    
    public WindowInfo(HWND hwnd, int pid, String title) {
      super();
      this.hwnd = hwnd;
      this.pid = pid;
      this.title = title;
    }

    public HWND getHwnd() {
      return hwnd;
    }

    public int getPid() {
      return pid;
    }

    public String getTitle() {
      return title;
    }        
  }

  public static List<WindowInfo> allWindows() {
    /* Initialize the empty window list. */
    final List<WindowInfo> windows = new ArrayList<>();

    /* Enumerate all of the windows and add all of the one for the
     * given process id to our list. */
    boolean result = user32.EnumWindows(
        new WinUser.WNDENUMPROC() {
            public boolean callback(
                    final HWND hwnd, final Pointer data) {                 
              
              if (user32.IsWindowVisible(hwnd)){
                IntByReference windowPid = new IntByReference();
                user32.GetWindowThreadProcessId(hwnd, windowPid);
                
                String windowTitle = getWindowTitle(hwnd); 
                
                windows.add(new WindowInfo(hwnd, windowPid.getValue(), windowTitle));               
              }

              return true;
            }
        },
        null);

    /* Handle errors. */
    if (!result && Kernel32.INSTANCE.GetLastError() != 0) {
        throw new RuntimeException("Couldn't enumerate windows.");
    }

    /* Return the window list. */
    return windows;
  }
  
  public static String getWindowTitle(HWND hWnd){
    char[] text = new char[1024];
    int length = user32.GetWindowText(hWnd, text, 1024);
    return length > 0 ? new String(text,0,length) : null;
  }
  
  public static String getTopWindowTitle(int pid){
    List<WindowInfo> windows = getWindowsForPid(pid);
    if (!windows.isEmpty()){    
      return getWindowsForPid(pid).get(0).getTitle();     
    }
    return null;
  }

  public static List<ProcessInfo> allProcesses() {
    List<ProcessInfo> processList = new ArrayList<ProcessInfo>();

    HANDLE snapshot = Kernel32.INSTANCE.CreateToolhelp32Snapshot(
            Tlhelp32.TH32CS_SNAPPROCESS, new DWORD(0));

    try {    
      Tlhelp32.PROCESSENTRY32.ByReference pe
          = new Tlhelp32.PROCESSENTRY32.ByReference();
      for (boolean more = Kernel32.INSTANCE.Process32First(snapshot, pe);
              more;
              more = Kernel32.INSTANCE.Process32Next(snapshot, pe)) {        
          processList.add(new ProcessInfo(
              pe.th32ProcessID.intValue(),
              getProcessImageName(pe.th32ProcessID.intValue())));
      }
    
      return processList;
    } finally {
      Kernel32.INSTANCE.CloseHandle(snapshot);
    }
  }
  
  private static String getProcessImageName(int pid){
    HANDLE hProcess = Kernel32.INSTANCE.OpenProcess(
            0x1000,
            false,
            pid);
    
    if (hProcess == null) {
        return null;
    }
    
    try{    
      char[] imageNameChars = new char[1024];
      IntByReference imageNameLen
          = new IntByReference(imageNameChars.length);
      boolean success = Kernel32.INSTANCE.QueryFullProcessImageName(
              hProcess, 0, imageNameChars, imageNameLen);    
      
      return success ? FilenameUtils.getName(new String(imageNameChars, 0, imageNameLen.getValue())) : null;
    
    } finally {
      Kernel32.INSTANCE.CloseHandle(hProcess);
    }
  }
  
  private static List<WindowInfo> getWindowsForPid(int pid){
    return allWindows().stream().filter((w) -> w.getPid() == pid).collect(Collectors.toList());
  }
  
  private static List<WindowInfo> getWindowsForName(String name){            
    return allWindows().stream().filter((w) -> {        
        String imageName = getProcessImageName(w.getPid());
        
        if (imageName != null && imageName.equals(name + ".exe")){
          return true;
        }
        
        String windowTitle = w.getTitle();
        
        if(windowTitle != null && windowTitle.contains(name)){
          return true;
        }
        
        return false;        
    }).collect(Collectors.toList());
  }

  public static String getEnv(String envKey) {
    char[] retChar = new char[BUFFERSIZE];
    String envVal = null;
    int retInt = kernel32.GetEnvironmentVariable(envKey, retChar, BUFFERSIZE);
    if (retInt > 0) {
      envVal = new String(Arrays.copyOfRange(retChar, 0, retInt));
    }
    sxuser32.EnumWindows(new WinUser.WNDENUMPROC() {
      @Override
      public boolean callback(WinDef.HWND hwnd, Pointer pointer) {
        return false;
      }
    }, null);
    return envVal;
  }

  public static String setEnv(String envKey, String envVal) {
    boolean retOK = kernel32.SetEnvironmentVariable(envKey, envVal);
    if (retOK) {
      return getEnv(envKey);
    }
    return null;
  }

  /*
  https://msdn.microsoft.com/pt-br/library/windows/desktop/dd375731
  VK_NUM_LOCK 0x90
  VK_SCROLL 0x91
  VK_CAPITAL 0x14
  */
  private static int WinNumLock = 0x90;
  private static int WinScrollLock = 0x91;
  private static int WinCapsLock = 0x14;

  public static int isNumLockOn() {
    int state = sxuser32.GetKeyState(WinNumLock);
    return state;
  }

  public static int isScrollLockOn() {
    int state = sxuser32.GetKeyState(WinScrollLock);
    return state;
  }

  public static int isCapsLockOn() {
    int state = sxuser32.GetKeyState(WinCapsLock);
    return state;
  }

  @Override
  public void checkFeatureAvailability() {
    RunTime.loadLibrary("WinUtil");
  }

  @Override
  public App get(App app) {
    if (app.getPID() > 0) {
      app = getTaskByPID(app);
    } else {
      app = getTaskByName(app);
    }
    return app;
  }

  //<editor-fold desc="old getApp">
/*
  @Override
  public App getApp(App app) {
    if (app.getPID() == 0) {
      return app;
    }
    Object filter;
    if (appPID < 0) {
      filter = appName;
    } else {
      filter = appPID;
    }
    String name = "";
    String execName = "";
    String options = "";
    Integer pid = -1;
    String[] parts;
    if (filter instanceof String) {
      name = (String) filter;
      if (name.startsWith("!")) {
        name = name.substring(1);
        execName = name;
      } else {
        if (name.startsWith("\"")) {
          parts = name.substring(1).split("\"");
          if (parts.length > 1) {
            options = name.substring(parts[0].length() + 3);
            name = "\"" + parts[0] + "\"";
          }
        } else {
          parts = name.split(" -- ");
          if (parts.length > 1) {
            options = parts[1];
            name = parts[0];
          }
        }
        if (name.startsWith("\"")) {
          execName = new File(name.substring(1, name.length() - 1)).getName().toUpperCase();
        } else {
          execName = new File(name).getName().toUpperCase();
        }
      }
    } else if (filter instanceof Integer) {
      pid = (Integer) filter;
    } else {
      return app;
    }
    Debug.log(3, "WinUtil.getApp: %s", filter);
    String cmd;
    if (pid < 0) {
      cmd = cmd = "!tasklist /V /FO CSV /NH /FI \"SESSIONNAME eq Console\"";
    } else {
      cmd = cmd = "!tasklist /V /FO CSV /NH /FI \"PID eq " + pid.toString() + "\"";
    }
    String result = RunTime.get().runcmd(cmd);
    String[] lines = result.split("\r\n");
    if ("0".equals(lines[0].trim())) {
      for (int nl = 1; nl < lines.length; nl++) {
        parts = lines[nl].split("\"");
        if (parts.length < 2) {
          continue;
        }
        String theWindow = parts[parts.length - 1];
        String theName = parts[1];
        String thePID = parts[3];
        //Debug.log(3, "WinUtil.getApp: %s:%s(%s)", thePID, theName, theWindow);
        if (!name.isEmpty()) {
          if ((theName.toUpperCase().contains(execName) && !theWindow.contains("N/A"))
                  || theWindow.contains(name)) {
            return new App.AppEntry(theName, thePID, theWindow, "", "");
          }
        } else {
          try {
            if (Integer.parseInt(thePID) == pid) {
              return new App.AppEntry(theName, thePID, theWindow, "", "");
            }
          } catch (Exception ex) {
          }
        }
      }
    } else {
      Debug.logp(result);
    }
    if (!options.isEmpty()) {
      return new App.AppEntry(name, "", "", "", options);
    }
    if (app == null) {
      List<String> theApp = getTaskByName(execName);
      if (theApp.size() > 0) {
        app = new App.AppEntry(theApp.get(0), theApp.get(1), theApp.get(2), "", "");
      }
    }
    return app;
  }
*/
  //</editor-fold>

  private static App getTaskByName(App app) { 
    
    String appName = app.getToken().isEmpty() ? app.getName() + ".exe" : app.getToken();
   
    List<ProcessInfo> processes = allProcesses();    
  
    for(ProcessInfo p : processes){
      if (p.getImageName() != null && p.getImageName().equals(appName)){
        app.setPID(p.getPid());
        app.setWindow(getTopWindowTitle(p.getPid()));
        return app;
      }         
    } 
                
    return getTaskByWindow(app);
  }

  private static App getTaskByPID(App app) {
    if (!app.isValid()) {
      return app;
    }    
   
    List<ProcessInfo> processes = allProcesses();    
  
    for(ProcessInfo p : processes){
      if (p.getPid() == app.getPID()){         
        app.setWindow(getTopWindowTitle(p.getPid()));
        return app;
      }         
    }    
   
    app.reset();
    return app;   
  }

  private static App getTaskByWindow(App app) {
    String title = app.getName();
   
    List<WindowInfo> windows = allWindows();
    
    for (WindowInfo window : windows){
      String windowTitle = window.getTitle();
          
      if (windowTitle != null && windowTitle.contains(title)){
        app.setPID(window.getPid());
        app.setWindow(windowTitle);
        return app;
      }
    }         
     
    return app;
  }

  @Override
  public List<App> getApps(String name) {            
    List<App> apps = new ArrayList<>();

    List<ProcessInfo> processes = allProcesses();  
  
    for(ProcessInfo p : processes){
      if(p.getImageName().equals(name)){        
        App theApp = new App();
        theApp.setName(p.getImageName());
        theApp.setWindow(getTopWindowTitle(p.getPid()));
        theApp.setPID(p.getPid());
        apps.add(theApp); 
      }
    }     
    
    return apps;
  }

  @Override
  public boolean open(App app) {
    if (app.isValid()) {
      return 0 == switchApp(app.getPID(), 0);
    } else {
      String cmd = app.getExec();
      String workDir = app.getWorkDir();
      if (!app.getOptions().isEmpty()) {
        start(cmd, workDir, app.getOptions());
      } else {
        start(cmd, workDir);
      }
    }
    return true;
  }

  private int start(String... cmd) {
    return ProcessRunner.startApp(cmd);
  }

  @Override
  public boolean switchto(App app) {
    if (!app.isValid()) {
      return false;
    }
    int loopCount = 0;
    while (loopCount < 100) {
      int pid = switchApp(app.getPID(), 0);
      if (pid > 0) {
        if (pid == app.getPID()) {          
          app.setFocused(true);
          getTaskByPID(app);
          return true;
        }
      } else {
        break;
      }
      loopCount++;
    }
    return false;
  }

  @Override
  public App switchto(String title, int index) {
    App app = new App();
    int pid = switchApp(title, index);
    if (pid > 0) {
      app.setPID(pid);
      return getTaskByPID(app);
    }
    return app;  
  }

  @Override
  public boolean close(App app) {
    if (closeApp(app.getPID()) == 0) {
      app.reset();
      return true;
    }
    return false;
  }

  @Override
  public Rectangle getWindow(App app) {
    return getWindow(app, 0);
  }

  @Override
  public Rectangle getWindow(App app, int winNum) {
    get(app);
    if (!app.isValid()) {
      return new Rectangle();
    }
    return getWindow(app.getPID(), winNum);
  }

  @Override
  public Rectangle getWindow(String title) {
    return getWindow(title, 0);
  }

  private Rectangle getWindow(String title, int winNum) {
    HWND hwnd = getHwnd(title, winNum);
    return hwnd != null ? _getWindow(hwnd, winNum) : null;
  }

  private Rectangle getWindow(int pid, int winNum) {
    HWND hwnd = getHwnd(pid, winNum);
    return hwnd != null ? _getWindow(hwnd, winNum) : null;
  }

  private Rectangle _getWindow(HWND hwnd, int winNum) {    
    Rectangle rect = getRegion(hwnd, winNum);
    return rect;
  }

  @Override
  public Rectangle getFocusedWindow() {
    return getFocusedRegion();    
  }

  @Override
  public List<Region> getWindows(App app) {
    app = get(app);        
    List<WindowInfo> windows = getWindowsForPid(app.getPID());
    
    List<Region> regions = new ArrayList<>();
    
    for(WindowInfo w : windows){
      regions.add(Region.create(_getWindow(w.getHwnd(),0)));
    }    
    
    return regions;    
  }

  public int switchApp(String appName, int num){        
    List<WindowInfo> windows = getWindowsForName(appName);
    
    if (windows.size() > num){
       WindowInfo window = windows.get(num);
       
       HWND hwnd = window.getHwnd();
              
       boolean success = user32.SetForegroundWindow(hwnd);
         
       if(success){
         user32.SetFocus(hwnd);
         IntByReference windowPid = new IntByReference();
         user32.GetWindowThreadProcessId(hwnd, windowPid);
         
         return windowPid.getValue();
       }else{
         return 0;
       }
    }    
    
    return 0;
  };

  public int switchApp(int pid, int num){        
    List<WindowInfo> windows = getWindowsForPid(pid);
    
    if(windows.size() > num){ 
      WindowInfo window = windows.get(num); 
      
      HWND hwnd = window.getHwnd();
      
      boolean success = user32.SetForegroundWindow(hwnd);
      
      if (success){
        user32.SetFocus(hwnd);
        return pid;
      }
    }      
    
    return 0;
  };

  public native int openApp(String appName);

  public native int closeApp(String appName);

  public native int closeApp(int pid);

  @Override
  public native void bringWindowToFront(Window win, boolean ignoreMouse);

  private static HWND getHwnd(String appName, int winNum){
    List<WindowInfo> windows = getWindowsForName(appName);
    
    if(windows.size() > winNum){
      return windows.get(winNum).getHwnd();     
    }
    return null;
  }

  private static HWND getHwnd(int pid, int winNum){
    List<WindowInfo> windows = getWindowsForPid(pid);
    
    if(windows.size() > winNum){
      return windows.get(winNum).getHwnd();     
    }
    return null;
  }

  private static Rectangle getRegion(HWND hwnd, int winNum){
    RECT rect = new User32.RECT();    
    boolean success = user32.GetWindowRect(hwnd, rect);    
    return success ? rect.toRectangle() : null;
  };

  private static Rectangle getFocusedRegion(){
    HWND hwnd = user32.GetForegroundWindow();
    RECT rect = new User32.RECT();
    boolean success = user32.GetWindowRect(hwnd, rect);
    return success ? rect.toRectangle() : null;
  }
}