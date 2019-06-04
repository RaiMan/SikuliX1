/*
 * Copyright (c) 2010-2018, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script.runnerHelpers;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;
import org.sikuli.basics.Settings;
import org.sikuli.script.ImagePath;
import org.sikuli.script.support.RunTime;
import org.sikuli.script.Sikulix;
import org.sikuli.script.SikulixForJython;

public class JythonHelper implements IScriptLanguageHelper {

  private static RunTime runTime;

  //<editor-fold defaultstate="collapsed" desc="new logging concept">
  private static final String me = "Jython: ";
  private static int lvl = 3;

  public void log(int level, String message, Object... args) {
    Debug.logx(level, me + message, args);
  }

  private void logp(String message, Object... args) {
    Debug.logx(-3, message, args);
  }

  private void logp(int level, String message, Object... args) {
    if (level <= Debug.getDebugLevel()) {
      logp(message, args);
    }
  }

  public void terminate(int retVal, String msg, Object... args) {
    Sikulix.terminate(retVal, me + msg, args);
  }
  //</editor-fold>

  static JythonHelper instance = null;
  static Object interpreter = null;
  List<String> sysPath = new ArrayList<String>();
  List<String> sysArgv = new ArrayList<String>();
  int nPathAdded = 0;
  int nPathSaved = -1;
  static Class[] nc = new Class[0];
  static Class[] nc1 = new Class[1];
  static Class cInterpreter = null;
  static Class cPyException = null;
  static Class cList = null;
  static Class cPy = null;
  static Class cPyFunction = null;
  static Class cPyMethod = null;
  static Class cPyInstance = null;
  static Class cPyObject = null;
  static Class cPyString = null;
  static Method mLen, mGet, mSet, mAdd, mRemove, mClear;
  static Method mGetSystemState, mExec, mExecfile;
  static Field PI_path;

  private JythonHelper() {
  }

  public static JythonHelper get() {
    if (instance == null) {
      runTime = RunTime.get();
      runTime.exportLib();
      instance = new JythonHelper();
      //instance.log(lvl, "init: starting");
      try {
        cInterpreter = Class.forName("org.python.util.PythonInterpreter");
        mGetSystemState = cInterpreter.getMethod("getSystemState", nc);
        mExec = cInterpreter.getMethod("exec", new Class[]{String.class});
        mExecfile = cInterpreter.getMethod("execfile", new Class[]{String.class});
        Constructor PI_new = cInterpreter.getConstructor(nc);
        interpreter = PI_new.newInstance();
        cPyException = Class.forName("org.python.core.PyException");
        cList = Class.forName("org.python.core.PyList");
        cPy = Class.forName("org.python.core.Py");
        cPyFunction = Class.forName("org.python.core.PyFunction");
        cPyMethod = Class.forName("org.python.core.PyMethod");
        cPyInstance = Class.forName("org.python.core.PyInstance");
        cPyObject = Class.forName("org.python.core.PyObject");
        cPyString = Class.forName("org.python.core.PyString");
        mLen = cList.getMethod("__len__", nc);
        mClear = cList.getMethod("clear", nc);
        mGet = cList.getMethod("get", new Class[]{int.class});
        mSet = cList.getMethod("set", new Class[]{int.class, Object.class});
        mAdd = cList.getMethod("add", new Class[]{Object.class});
        mRemove = cList.getMethod("remove", new Class[]{int.class});
      } catch (Exception ex) {
        cInterpreter = null;
      }
      //instance.log(lvl, "init: success");
    }
    if (cInterpreter == null) {
      instance.terminate(999, "JythonHelper: no Jython available");
    }
    runTime.isJythonReady = true;
    return instance;
  }

  private void noOp() {
  } // for debugging as breakpoint

  class PyException {

    Object inst = null;
    Field fType = null;
    Field fValue = null;
    Field fTrBack = null;

    public PyException(Object i) {
      inst = i;
      cPyException.cast(inst);
      try {
        fType = cPyException.getField("type");
        fValue = cPyException.getField("value");
        fTrBack = cPyException.getField("traceback");
      } catch (Exception ex) {
        noOp();
      }
    }

    public int isTypeExit() {
      try {
        if (fType.get(inst).toString().contains("SystemExit")) {
          return Integer.parseInt(fValue.get(inst).toString());
        }
      } catch (Exception ex) {
        return -999;
      }
      return -1;
    }
  }

  class PyInstance {

    Object inst = null;
    Method mGetAttr = null;
    Method mInvoke = null;

    public PyInstance(Object i) {
      inst = i;
      cPyInstance.cast(inst);
      try {
        mGetAttr = cPyInstance.getMethod("__getattr__", String.class);
        mInvoke = cPyInstance.getMethod("invoke", String.class, cPyObject);
      } catch (Exception ex) {
        noOp();
      }
    }

    public Object get() {
      return inst;
    }

    Object __getattr__(String mName) {
      if (mGetAttr == null) {
        return null;
      }
      Object method = null;
      try {
        method = mGetAttr.invoke(inst, mName);
      } catch (Exception ex) {
      }
      return method;
    }

    public void invoke(String mName, Object arg) {
      if (mInvoke != null) {
        try {
          mInvoke.invoke(inst, mName, arg);
        } catch (Exception ex) {
          noOp();
        }
      }
    }
  }

  class PyFunction {

    public String __name__;
    Object func = null;
    Method mCall = null;
    Method mCall1 = null;

    public PyFunction(Object f) {
      func = f;
      try {
        cPyFunction.cast(func);
        mCall = cPyFunction.getMethod("__call__");
        mCall1 = cPyFunction.getMethod("__call__", cPyObject);
      } catch (Exception ex) {
        func = null;
      }
      if (func == null) {
        try {
          func = f;
          cPyMethod.cast(func);
          mCall = cPyMethod.getMethod("__call__");
          mCall1 = cPyMethod.getMethod("__call__", cPyObject);
        } catch (Exception ex) {
          func = null;
        }
      }
    }

    void __call__(Object arg) {
      if (mCall1 != null) {
        try {
          mCall1.invoke(func, arg);
        } catch (Exception ex) {
        }
      }
    }

    void __call__() {
      if (mCall != null) {
        try {
          mCall.invoke(func);
        } catch (Exception ex) {
        }
      }
    }
  }

  class Py {

    Method mJava2py = null;

    public Py() {
      try {
        mJava2py = cPy.getMethod("java2py", Object.class);
      } catch (Exception ex) {
        noOp();
      }
    }

    Object java2py(Object arg) {
      if (mJava2py == null) {
        return null;
      }
      Object pyObject = null;
      try {
        pyObject = mJava2py.invoke(null, arg);
      } catch (Exception ex) {
        noOp();
      }
      return pyObject;
    }
  }

  class PyString {

    String aString = "";
    Object pyString = null;

    public PyString(String s) {
      aString = s;
      try {
        pyString = cPyString.getConstructor(String.class).newInstance(aString);
      } catch (Exception ex) {
      }
    }

    public Object get() {
      return pyString;
    }
  }

  public boolean exec(String code) {
    try {
      mExec.invoke(interpreter, code);
    } catch (Exception ex) {
      PyException pex = new PyException(ex.getCause());
      if (pex.isTypeExit() < 0) {
        log(-1, "exec: returns:\n%s", ex.getCause());
      }
      return false;
    }
    return true;
  }

  public int execfile(String fpScript) {
    int retval = -999;
    try {
      mExecfile.invoke(interpreter, fpScript);
    } catch (Exception ex) {
      PyException pex = new PyException(ex.getCause());
      if ((retval = pex.isTypeExit()) < 0) {
        log(-1, "execFile: returns:\n%s", ex.getCause());
      }
    }
    return retval;
  }

  //TODO check signature (instance method)
  public boolean checkCallback(Object[] args) {
    PyInstance inst = new PyInstance(args[0]);
    String mName = (String) args[1];
    Object method = inst.__getattr__(mName);
    if (method == null || !method.getClass().getName().contains("PyMethod")) {
      log(-100, "checkCallback: Object: %s, Method not found: %s", inst, mName);
      return false;
    }
    return true;
  }

  public boolean runLoggerCallback(Object[] args) {
    PyInstance inst = new PyInstance(args[0]);
    String mName = (String) args[1];
    String msg = (String) args[2];
    Object method = inst.__getattr__(mName);
    if (method == null || !method.getClass().getName().contains("PyMethod")) {
      log(-100, "runLoggerCallback: Object: %s, Method not found: %s", inst, mName);
      return false;
    }
    try {
      PyString pmsg = new PyString(msg);
      inst.invoke(mName, pmsg.get());
    } catch (Exception ex) {
      log(-100, "runLoggerCallback: invoke: %s", ex.getMessage());
      return false;
    }
    return true;
  }

  @Override
  public boolean runObserveCallback(Object[] args) {
    PyFunction func = new PyFunction(args[0]);
    boolean success = true;
    try {
      func.__call__(new Py().java2py(args[1]));
    } catch (Exception ex) {
//      if (!"<lambda>".equals(func.__name__)) {
      if (!func.toString().contains("<lambda>")) {
        log(-1, "runObserveCallback: jython invoke: %s", ex.getMessage());
        return false;
      }
      success = false;
    }
    if (success) {
      return true;
    }
    try {
      func.__call__();
    } catch (Exception ex) {
      log(-1, "runObserveCallback: jython invoke <lambda>: %s", ex.getMessage());
      return false;
    }
    return true;
  }

  //TODO implement generalized callback
  public boolean runCallback(Object[] args) {
    PyInstance inst = (PyInstance) args[0];
    String mName = (String) args[1];
    Object method = inst.__getattr__(mName);
    if (method == null || !method.getClass().getName().contains("PyMethod")) {
      log(-1, "runCallback: Object: %s, Method not found: %s", inst, mName);
      return false;
    }
    try {
      PyString pmsg = new PyString("not yet supported");
      inst.invoke(mName, pmsg.get());
    } catch (Exception ex) {
      log(-1, "runCallback: invoke: %s", ex.getMessage());
      return false;
    }
    return true;
  }

  public static JythonHelper set(Object ip) {
    JythonHelper.get();
    interpreter = ip;
    return instance;
  }

  public boolean prepareRobot() {
    if (runTime.isRunningFromJar()) {
      File fLibRobot = new File(runTime.fSikulixLib, "robot");
      if (!fLibRobot.exists()) {
        Sikulix.terminate(999, "prepareRobot: not available: %s", fLibRobot);
      }
      if (!hasSysPath(runTime.fSikulixLib.getAbsolutePath())) {
        insertSysPath(runTime.fSikulixLib);
      }
    }
    if (!hasSysPath(new File(Settings.BundlePath).getParent())) {
      appendSysPath(new File(Settings.BundlePath).getParent());
    }
    exec("import robot");
    return true;
  }

  public int runJar(String fpJarOrFolder) {
    return runJar(fpJarOrFolder, "");
  }

  public int runJar(String fpJarOrFolder, String imagePath) {
    SikulixForJython.get();
    String fpJar = load(fpJarOrFolder, true);
    ImagePath.addJar(fpJar, imagePath);
    String scriptName = new File(fpJar).getName().replace("_sikuli.jar", "");
    if (exec("try: reload(" + scriptName + ")\nexcept: import " + scriptName)) {
      return 0;
    } else {
      return -1;
    }
  }

  public String load(String fpJarOrFolder) {
    return load(fpJarOrFolder, false);
  }

  public String load(String fpJar, boolean scriptOnly) {
    log(lvl, "load: %s", fpJar);
    if (!fpJar.endsWith(".jar")) {
      fpJar += ".jar";
    }
    File fJar = new File(FileManager.normalizeAbsolute(fpJar));
    if (!fJar.exists()) {
      String fpBundle = ImagePath.getBundlePath();
      fJar = null;
      if (null != fpBundle) {
        fJar = new File(fpBundle, fpJar);
        if (!fJar.exists()) { // in bundle
          fJar = new File(new File(fpBundle).getParentFile(), fpJar);
          if (!fJar.exists()) { // in bundle parent
            fJar = null;
          }
        }

      }
      if (fJar == null) {
        fJar = new File(runTime.fSikulixExtensions, fpJar);
        if (!fJar.exists()) { // in extensions
          fJar = new File(runTime.fSikulixLib, fpJar);
          if (!fJar.exists()) { // in Lib folder
            fJar = null;
          }
        }
      }
    }
    if (fJar == null) {
      log(-1, "load: not found: %s", fJar);
      return fpJar;
    } else {
      if (!hasSysPath(fJar.getPath())) {
        insertSysPath(fJar);
      }
      return fJar.getAbsolutePath();
    }
  }

  private long lastRun = 0;

  private List<File> importedScripts = new ArrayList<File>();
  String name = "";

  public void reloadImported() {
    if (lastRun > 0) {
      for (File fMod : importedScripts) {
        name = getPyName(fMod);
        if (new File(fMod, name + ".py").lastModified() > lastRun) {
          log(lvl, "reload: %s", fMod);
          get().exec("reload(" + name + ")");
        }
        ;
      }
    }
    lastRun = new Date().getTime();
  }

  private String getPyName(File fMod) {
    String ending = ".sikuli";
    String name = fMod.getName();
    if (name.endsWith(ending)) {
      name = name.substring(0, name.length() - ending.length());
    }
    return name;
  }

  public String findModule(String modName, Object packPath, Object sysPath) {
    if (modName.endsWith(".*")) {
      log(lvl + 1, "findModule: %s", modName);
      return null;
    }
    if (packPath != null) {
      log(lvl + 1, "findModule: in pack: %s (%s)", modName, packPath);
      return null;
    }
    int nDot = modName.lastIndexOf(".");
    String modNameFull = modName;
    if (nDot > -1) {
      modName = modName.substring(nDot + 1);
    }
    File fModule = null;
    if (ImagePath.getBundlePath() != null) {
      File fParentBundle = new File(ImagePath.getBundlePath()).getParentFile();
      fModule = existsModule(modName, fParentBundle);
    }
    if (fModule == null) {
      fModule = existsSysPathModule(modName);
      if (fModule == null) {
        return null;
      }
    }
    log(lvl + 1, "findModule: final: %s [%s]", fModule.getName(), fModule.getParent());
    if (fModule.getName().endsWith(".sikuli")) {
      importedScripts.add(fModule);
      return fModule.getAbsolutePath();
    }
    return null;
  }

  public String loadModulePrepare(String modName, String modPath) {
    log(lvl + 1, "loadModulePrepare: %s in %s", modName, modPath);
    int nDot = modName.lastIndexOf(".");
    if (nDot > -1) {
      modName = modName.substring(nDot + 1);
    }
    addSysPath(modPath);
    if (modPath.endsWith(".sikuli")) {
      ImagePath.add(modPath);
    }
    return modName;
  }

  private File existsModule(String mName, File fFolder) {
    if (mName.endsWith(".sikuli") || mName.endsWith(".py")) {
      return null;
    }
    File fSikuli = new File(fFolder, mName + ".sikuli");
    if (fSikuli.exists()) {
      return fSikuli;
    }
    File fPython = new File(fFolder, mName + ".py");
    if (fPython.exists()) {
      return fPython;
    }
    return null;
  }

  public List<String> getSysArgv() {
    sysArgv = new ArrayList<String>();
    if (null == cInterpreter) {
      sysArgv = null;
      return null;
    }
    try {
      Object aState = mGetSystemState.invoke(interpreter, (Object[]) null);
      Field fArgv = aState.getClass().getField("argv");
      Object pyArgv = fArgv.get(aState);
      Integer argvLen = (Integer) mLen.invoke(pyArgv, (Object[]) null);
      for (int i = 0; i < argvLen; i++) {
        String entry = (String) mGet.invoke(pyArgv, i);
        log(lvl + 1, "sys.path[%2d] = %s", i, entry);
        sysArgv.add(entry);
      }
    } catch (Exception ex) {
      sysArgv = null;
    }
    return sysArgv;
  }

  public void setSysArgv(String[] args) {
    if (null == cInterpreter || null == sysArgv) {
      return;
    }
    try {
      Object aState = mGetSystemState.invoke(interpreter, (Object[]) null);
      Field fArgv = aState.getClass().getField("argv");
      Object pyArgv = fArgv.get(aState);
      mClear.invoke(pyArgv);
      for (String arg : args) {
        mAdd.invoke(pyArgv, arg);
      }
    } catch (Exception ex) {
      sysArgv = null;
    }
  }

  public void getSysPath() {
    synchronized (sysPath) {
      sysPath.clear();
      if (null == cInterpreter) {
        sysPath.clear();
        return;
      }
      try {
        Object aState = mGetSystemState.invoke(interpreter, (Object[]) null);
        Field fPath = aState.getClass().getField("path");
        Object pyPath = fPath.get(aState);
        Integer pathLen = (Integer) mLen.invoke(pyPath, (Object[]) null);
        for (int i = 0; i < pathLen; i++) {
          String entry = (String) mGet.invoke(pyPath, i);
          log(lvl + 1, "sys.path[%2d] = %s", i, entry);
          sysPath.add(entry);
        }
      } catch (Exception ex) {
        sysPath.clear();
      }
    }
  }

  public void setSysPath() {
    synchronized (sysPath) {
      if (null == cInterpreter || null == sysPath) {
        return;
      }
      try {
        Object aState = mGetSystemState.invoke(interpreter, (Object[]) null);
        Field fPath = aState.getClass().getField("path");
        Object pyPath = fPath.get(aState);
        Integer pathLen = (Integer) mLen.invoke(pyPath, (Object[]) null);
        for (int i = 0; i < pathLen && i < sysPath.size(); i++) {
          String entry = sysPath.get(i);
          log(lvl + 1, "sys.path.set[%2d] = %s", i, entry);
          mSet.invoke(pyPath, i, entry);
        }
        if (pathLen < sysPath.size()) {
          for (int i = pathLen; i < sysPath.size(); i++) {
            String entry = sysPath.get(i);
            log(lvl + 1, "sys.path.add[%2d] = %s", i, entry);
            mAdd.invoke(pyPath, entry);
          }
        }
        if (pathLen > sysPath.size()) {
          for (int i = sysPath.size(); i < pathLen; i++) {
            String entry = (String) mGet.invoke(pyPath, i);
            log(lvl + 1, "sys.path.rem[%2d] = %s", i, entry);
            mRemove.invoke(pyPath, i);
          }
        }
      } catch (Exception ex) {
        sysPath.clear();
      }
    }
  }

  public void addSitePackages() {
    synchronized (sysPath) {
      File fLibFolder = runTime.fSikulixLib;
      File fSitePackages = new File(fLibFolder, "site-packages");
      if (fSitePackages.exists()) {
        addSysPath(fSitePackages);
        if (hasSysPath(fSitePackages.getAbsolutePath())) {
          log(lvl, "added as Jython::sys.path[0]:\n%s", fSitePackages);
        }
        File fSites = new File(fSitePackages, "sites.txt");
        String sSites = "";
        if (fSites.exists()) {
          sSites = FileManager.readFileToString(fSites);
          if (!sSites.isEmpty()) {
            log(lvl, "found Lib/site-packages/sites.txt");
            String[] listSites = sSites.split("\n");
            for (String site : listSites) {
              String path = site.trim();
              if (path.startsWith("#")) {
                continue;
              }
              if (!path.isEmpty()) {
                appendSysPath(path);
                log(lvl, "adding from Lib/site-packages/sites.txt:\n%s", path);
              }
            }
          }
        }
      }
      String fpBundle = ImagePath.getBundlePath();
      if (fpBundle != null) {
        addSysPath(fpBundle);
      }
    }
  }

  public void addSysPath(String fpFolder) {
    synchronized (sysPath) {
      if (!hasSysPath(fpFolder)) {
        sysPath.add(0, fpFolder);
        setSysPath();
        nPathAdded++;
      }
    }
  }

  public void appendSysPath(String fpFolder) {
    synchronized (sysPath) {
      if (!hasSysPath(fpFolder)) {
        sysPath.add(fpFolder);
        setSysPath();
        nPathAdded++;
      }
    }
  }

  public void putSysPath(String fpFolder, int n) {
    synchronized (sysPath) {
      if (n < 1 || n > sysPath.size()) {
        addSysPath(fpFolder);
      } else {
        sysPath.add(n, fpFolder);
        setSysPath();
        nPathAdded++;
      }
    }
  }

  public void addSysPath(File fFolder) {
    addSysPath(fFolder.getAbsolutePath());
  }

  public void insertSysPath(File fFolder) {
    synchronized (sysPath) {
      getSysPath();
      sysPath.add((nPathSaved > -1 ? nPathSaved : 0), fFolder.getAbsolutePath());
      setSysPath();
      nPathSaved = -1;
    }
  }

  public void removeSysPath(File fFolder) {
    synchronized (sysPath) {
      int n;
      if (-1 < (n = getSysPathEntry(fFolder))) {
        sysPath.remove(n);
        nPathSaved = n;
        setSysPath();
        nPathAdded = nPathAdded == 0 ? 0 : nPathAdded--;
      }
    }
  }

  public boolean hasSysPath(String fpFolder) {
    synchronized (sysPath) {
      getSysPath();
      for (String fpPath : sysPath) {
        if (FileManager.pathEquals(fpPath, fpFolder)) {
          return true;
        }
      }
      return false;
    }
  }

  public int getSysPathEntry(File fFolder) {
    synchronized (sysPath) {
      getSysPath();
      int n = 0;
      for (String fpPath : sysPath) {
        if (FileManager.pathEquals(fpPath, fFolder.getAbsolutePath())) {
          return n;
        }
        n++;
      }
      return -1;
    }
  }

  public File existsSysPathModule(String modname) {
    synchronized (sysPath) {
      getSysPath();
      File fModule = null;
      for (String fpPath : sysPath) {
        fModule = existsModule(modname, new File(fpPath));
        if (null != fModule) {
          break;
        }
      }
      return fModule;
    }
  }

  public File existsSysPathJar(String fpJar) {
    synchronized (sysPath) {
      getSysPath();
      File fJar = null;
      for (String fpPath : sysPath) {
        fJar = new File(fpPath, fpJar);
        if (fJar.exists()) {
          break;
        }
        fJar = null;
      }
      return fJar;
    }
  }

  public void showSysPath() {
    synchronized (sysPath) {
      if (Debug.is(lvl)) {
        getSysPath();
        log(lvl, "***** sys.path");
        for (int i = 0; i < sysPath.size(); i++) {
          if (sysPath.get(i).startsWith("__")) {
            continue;
          }
          logp(lvl, "%2d: %s", i, sysPath.get(i));
        }
      }
    }
  }

  public String getCurrentLine() {
    String trace = "";
    Object frame = null;
    Object back = null;
    try {
      Method mGetFrame = cPy.getMethod("getFrame", nc);
      Class cPyFrame = Class.forName("org.python.core.PyFrame");
      Field fLineno = cPyFrame.getField("f_lineno");
      Field fCode = cPyFrame.getField("f_code");
      Field fBack = cPyFrame.getField("f_back");
      Class cPyBaseCode = Class.forName("org.python.core.PyBaseCode");
      Field fFilename = cPyBaseCode.getField("co_filename");
      frame = mGetFrame.invoke(cPy, (Object[]) null);
      back = fBack.get(frame);
      if (null == back) {
        trace = "Jython: at " + getCurrentLineTraceElement(fLineno, fCode, fFilename, frame);
      } else {
        trace = "Jython traceback - current first:\n"
                + getCurrentLineTraceElement(fLineno, fCode, fFilename, frame);
        while (null != back) {
          String line = getCurrentLineTraceElement(fLineno, fCode, fFilename, back);
          if (!line.startsWith("Region (")) {
            trace += "\n" + line;
          }
          back = fBack.get(back);
        }
      }
    } catch (Exception ex) {
      trace = String.format("Jython: getCurrentLine: INSPECT EXCEPTION: %s", ex);
    }
    return trace;
  }

  private String getCurrentLineTraceElement(Field fLineno, Field fCode, Field fFilename, Object frame) {
    String trace = "";
    try {
      int lineno = fLineno.getInt(frame);
      Object code = fCode.get(frame);
      Object filename = fFilename.get(code);
      String fname = FileManager.getName((String) filename);
      fname = fname.replace(".py", "");
      trace = String.format("%s (%d)", fname, lineno);
    } catch (Exception ex) {
    }
    return trace;
  }

  public int findErrorSource(Throwable thr, String filename) {
    errorClass = PY_UNKNOWN;
    String err = "ERROR_UNKNOWN";
    try {
      err = thr.toString();
    } catch (Exception ex) {
      errorClass = PY_JAVA;
      err = thr.getCause().toString();
    }
//    log(-1,"------------- Traceback -------------\n" + err +
//            "------------- Traceback -------------\n");
    errorLine = -1;
    errorColumn = -1;
    errorType = "--UnKnown--";
    errorText = "--UnKnown--";

//  File ".../mainpy.sikuli/mainpy.py", line 25, in <module> NL func() NL
//  File ".../subpy.py", line 4, in func NL 1/0 NL
    Pattern pFile = Pattern.compile("File..(.*?\\.py).*?" + ",.*?line.*?(\\d+),.*?in(.*?)" + NL + "(.*?)" + NL);

    String msg;
    Matcher mFile = null;

    if (PY_JAVA != errorClass) {
      if (err.startsWith("Traceback")) {
        Pattern pError = Pattern.compile(NL + "(.*?):.(.*)$");
        mFile = pFile.matcher(err);
        if (mFile.find()) {
          log(lvl + 2, "Runtime error line: " + mFile.group(2) + "\n in function: " + mFile.group(3) + "\n statement: "
                  + mFile.group(4));
          errorLine = Integer.parseInt(mFile.group(2));
          errorClass = PY_RUNTIME;
          Matcher mError = pError.matcher(err);
          if (mError.find()) {
            log(lvl + 2, "Error:" + mError.group(1));
            log(lvl + 2, "Error:" + mError.group(2));
            errorType = mError.group(1);
            errorText = mError.group(2);
          } else {
//org.sikuli.core.FindFailed: FindFailed: can not find 1352647716171.png on the screen
            Pattern pFF = Pattern.compile(": FindFailed: (.*?)" + NL);
            Matcher mFF = pFF.matcher(err);
            if (mFF.find()) {
              errorType = "FindFailed";
              errorText = mFF.group(1);
            } else {
              errorClass = PY_UNKNOWN;
            }
          }
        }
      } else if (err.startsWith("SyntaxError")) {
        Pattern pLineS = Pattern.compile(", (\\d+), (\\d+),");
        java.util.regex.Matcher mLine = pLineS.matcher(err);
        if (mLine.find()) {
          log(lvl + 2, "SyntaxError error line: " + mLine.group(1));
          Pattern pText = Pattern.compile("\\((.*?)\\(");
          java.util.regex.Matcher mText = pText.matcher(err);
          mText.find();
          errorText = mText.group(1) == null ? errorText : mText.group(1);
          log(lvl + 2, "SyntaxError: " + errorText);
          errorLine = Integer.parseInt(mLine.group(1));
          errorColumn = Integer.parseInt(mLine.group(2));
          errorClass = PY_SYNTAX;
          errorType = "SyntaxError";
        }
      }
    }

    msg = String.format("script [ %s ]", new File(filename).getName().replace(".py", ""));

    if (errorLine != -1) {
      // log(-1,_I("msgErrorLine", srcLine));
      msg += " stopped with error in line " + errorLine;
      if (errorColumn != -1) {
        msg += " at column " + errorColumn;
      }
    } else {
      msg += " stopped with error at line --unknown--";
    }

    if (errorClass == PY_RUNTIME || errorClass == PY_SYNTAX) {
      if (errorText.startsWith(errorType)) {
        if (errorText.startsWith("java.lang.RuntimeException: SikuliX: ")) {
          errorText = errorText.replace("java.lang.RuntimeException: SikuliX: ", "sikulix.RuntimeException: ");
          errorClass = SX_RUNTIME;
        } else if (errorText.startsWith("java.lang.ThreadDeath")) {
          errorClass = SX_ABORT;
        }
      }
      if (errorClass != SX_ABORT) {
        Debug.error(msg);
        Debug.error(errorType + " ( " + errorText + " )");
      } else {
        Debug.error("IDE: terminating script run - abort key was pressed");
      }
      if (errorClass == PY_RUNTIME) {
        errorTrace = findErrorSourceWalkTrace(mFile, filename);
        if (errorTrace.length() > 0) {
          Debug.error("--- Traceback --- error source first\n" + "line: module ( function ) statement \n" + errorTrace
                  + "[error] --- Traceback --- end --------------");
        }
      }
    } else {
      Debug.error(msg);
      Debug.error("Error caused by: %s", err);
    }
    return errorLine;
  }

  private String findErrorSourceWalkTrace(Matcher m, String filename) {
    Pattern pModule;
    if (runTime.runningWindows) {
      pModule = Pattern.compile(".*\\\\(.*?)\\.py");
    } else {
      pModule = Pattern.compile(".*/(.*?)\\.py");
    }
    String mod;
    String modIgnore = "SikuliImporter,Region,";
    StringBuilder trace = new StringBuilder();
    String telem;
    while (m.find()) {
      if (m.group(1).equals(filename)) {
        mod = "main";
      } else {
        Matcher mModule = pModule.matcher(m.group(1));
        mModule.find();
        mod = mModule.group(1);
        if (modIgnore.contains(mod + ",")) {
          continue;
        }
      }
      telem = m.group(2) + ": " + mod + " ( " + m.group(3) + " ) " + m.group(4) + NL;
      trace.insert(0, telem);
    }
    return trace.toString();
  }

  private void findErrorSourceFromJavaStackTrace(Throwable thr, String filename) {
    log(-1, "findErrorSourceFromJavaStackTrace: seems to be an error in the Java API supporting code");
    StackTraceElement[] s;
    Throwable t = thr;
    while (t != null) {
      s = t.getStackTrace();
      log(lvl + 2, "stack trace:");
      for (int i = s.length - 1; i >= 0; i--) {
        StackTraceElement si = s[i];
        log(lvl + 2, si.getLineNumber() + " " + si.getFileName());
        if (si.getLineNumber() >= 0 && filename.equals(si.getFileName())) {
          errorLine = si.getLineNumber();
        }
      }
      t = t.getCause();
      log(lvl + 2, "cause: " + t);
    }
  }

  private int errorLine;
  private int errorColumn;
  private String errorType;
  private String errorText;
  private int errorClass;
  private String errorTrace;

  private static final int PY_SYNTAX = 0;
  private static final int PY_RUNTIME = 1;
  private static final int PY_JAVA = 2;
  private static final int SX_RUNTIME = 3;
  private static final int SX_ABORT = 4;
  private static final int PY_UNKNOWN = -1;

  private static final String NL = String.format("%n");
}
