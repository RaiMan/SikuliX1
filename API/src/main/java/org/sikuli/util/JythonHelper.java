/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.util;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;
import org.sikuli.basics.Settings;
import org.sikuli.script.ImagePath;
import org.sikuli.script.RunTime;
import org.sikuli.script.Sikulix;
import org.sikuli.script.SikulixForJython;

public class JythonHelper implements JLangHelperInterface {

  static RunTime runTime = RunTime.get();

  //<editor-fold defaultstate="collapsed" desc="new logging concept">
  private static final String me = "JythonSupport: ";
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
    runTime.terminate(retVal, me + msg, args);
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
      instance = new JythonHelper();
      instance.log(lvl, "init: starting");
      try {
        cInterpreter = Class.forName("org.python.util.PythonInterpreter");
      } catch (Exception ex) {
        String sJython = new File(runTime.SikuliJython).getName();
        File fJython = new File(runTime.fSikulixDownloadsGeneric, sJython);
        if (fJython.exists()) {
          runTime.addToClasspath(fJython.getAbsolutePath(), "JythonHelper.get");
        } else {
          cInterpreter = null;
        }
      }
      try {
        cInterpreter = Class.forName("org.python.util.PythonInterpreter");
        mGetSystemState = cInterpreter.getMethod("getSystemState", nc);
        mExec = cInterpreter.getMethod("exec", new Class[]{String.class});
        mExecfile = cInterpreter.getMethod("execfile", new Class[]{String.class});
        Constructor PI_new = cInterpreter.getConstructor(nc);
        interpreter = PI_new.newInstance(null);
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
      instance.log(lvl, "init: success");
    }
    if (cInterpreter == null) {
      instance.runTime.terminate(1, "JythonHelper: no Jython available");
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
        log(-1, "prepareRobot: not available: %s", fLibRobot);
        Sikulix.terminate(1);
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
    if(exec("try: reload(" + scriptName + ")\nexcept: import " + scriptName)) {
      return 0;
    } else {
      return -1;
    }
  }

  public String load(String fpJarOrFolder) {
    return load(fpJarOrFolder, false);
  }

  public String load(String fpJarOrFolder, boolean scriptOnly) {
//##
//# loads a Sikuli extension (.jar) from
//#  1. user's sikuli data path
//#  2. bundle path
//#
//def load(jar):
//    def _load(abspath):
//        if os.path.exists(abspath):
//            if not abspath in sys.path:
//                sys.path.append(abspath)
//            return True
//        return False
//
//    if JythonHelper.load(jar):
//        return True
//
//    if _load(jar):
//        return True
//    path = getBundlePath()
//    if path:
//        jarInBundle = os.path.join(path, jar)
//        if _load(jarInBundle):
//            return True
//    path = ExtensionManager.getInstance().getLoadPath(jar)
//    if path and _load(path):
//        return True
//    return False
    log(lvl, "load: to be loaded:\n%s", fpJarOrFolder);
    if (!fpJarOrFolder.endsWith(".jar")) {
      fpJarOrFolder += ".jar";
    }
    File fJar = new File(FileManager.normalizeAbsolute(fpJarOrFolder, false));
    String fpBundle = ImagePath.getBundlePath();
    if (!fJar.exists()) {
      fJar = new File(fpBundle, fpJarOrFolder);
      if (!fJar.exists()) { // in bundle
        fJar = new File(new File(fpBundle).getParentFile(), fpJarOrFolder);
        if (!fJar.exists()) { // in bundles parent
          fJar = new File(runTime.fSikulixExtensions, fpJarOrFolder);
          if (!fJar.exists()) { // in extensions
            fJar = new File(runTime.fSikulixLib, fpJarOrFolder);
            if (!fJar.exists()) { // in Lib folder
              fJar = null;
            }
          }
        }
      }
    }
    if (fJar != null) {
      if (!scriptOnly && !runTime.addToClasspath(fJar.getPath(), "JythonHelper.load")) {
        log(-1, "load: not possible: %s", fJar);
      }
    } else {
      log(-1, "load: not found: %s", fJar);
      return null;
    }
    if (!hasSysPath(fJar.getPath())) {
      insertSysPath(fJar);
    }
    return fJar.getAbsolutePath();
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
        };
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
    String modNameFull =  modName;
    if (nDot > -1) {
      modName = modName.substring(nDot + 1);
    }
    String fpBundle = ImagePath.getBundlePath();
    File fParentBundle = null;
    File fModule = null;
    if (fpBundle != null) {
      fParentBundle = new File(fpBundle).getParentFile();
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

  public void getSysArgv() {
    sysArgv = new ArrayList<String>();
    if (null == cInterpreter) {
      sysArgv = null;
      return;
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
  }

  public void setSysArgv(String[] args) {
    if (null == cInterpreter || null == sysArgv) {
      return;
    }
    try {
      Object aState = mGetSystemState.invoke(interpreter, (Object[]) null);
      Field fArgv = aState.getClass().getField("argv");
      Object pyArgv = fArgv.get(aState);
      mClear.invoke(pyArgv, null);
      for (String arg : args) {
        mAdd.invoke(pyArgv, arg);
      }
    } catch (Exception ex) {
      sysArgv = null;
    }
  }

  public void getSysPath() {
    sysPath = new ArrayList<String>();
    if (null == cInterpreter) {
      sysPath = null;
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
      sysPath = null;
    }
  }

  public void setSysPath() {
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
      sysPath = null;
    }
  }

  public void addSitePackages() {
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
            if (!path.isEmpty()) {
              appendSysPath(path);
              log(lvl, "adding from Lib/site-packages/sites.txt:\n%s", path);
            }
          }
        }
      }
    }
    String fpBundle = ImagePath.getPath(0);
    if (fpBundle != null) {
      addSysPath(fpBundle);
    }
  }

  public void addSysPath(String fpFolder) {
    if (!hasSysPath(fpFolder)) {
      sysPath.add(0, fpFolder);
      setSysPath();
      nPathAdded++;
    }
  }

  public void appendSysPath(String fpFolder) {
    if (!hasSysPath(fpFolder)) {
      sysPath.add(fpFolder);
      setSysPath();
      nPathAdded++;
    }
  }

  public void putSysPath(String fpFolder, int n) {
    if (n < 1 || n > sysPath.size()) {
      addSysPath(fpFolder);
    } else {
      sysPath.add(n, fpFolder);
      setSysPath();
      nPathAdded++;
    }
  }

  public void addSysPath(File fFolder) {
    addSysPath(fFolder.getAbsolutePath());
  }

  public void insertSysPath(File fFolder) {
    getSysPath();
    sysPath.add((nPathSaved > -1 ? nPathSaved : 0), fFolder.getAbsolutePath());
    setSysPath();
    nPathSaved = -1;
  }

  public void removeSysPath(File fFolder) {
    int n;
    if (-1 < (n = getSysPathEntry(fFolder))) {
      sysPath.remove(n);
      nPathSaved = n;
      setSysPath();
      nPathAdded = nPathAdded == 0 ? 0 : nPathAdded--;
    }
  }

  public boolean hasSysPath(String fpFolder) {
    getSysPath();
    for (String fpPath : sysPath) {
      if (FileManager.pathEquals(fpPath, fpFolder)) {
        return true;
      }
    }
    return false;
  }

  public int getSysPathEntry(File fFolder) {
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

  public File existsSysPathModule(String modname) {
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

  public File existsSysPathJar(String fpJar) {
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

  public void showSysPath() {
    if (Debug.is(lvl)) {
      getSysPath();
      log(lvl, "***** Jython sys.path");
      for (int i = 0; i < sysPath.size(); i++) {
        logp(lvl, "%2d: %s", i, sysPath.get(i));
      }
      log(lvl, "***** Jython sys.path end");
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
          if (! line.startsWith("Region (")) {
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

}
