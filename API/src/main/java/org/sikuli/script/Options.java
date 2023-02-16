/*
 * Copyright (c) 2010-2022, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script;

import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;
import org.sikuli.script.support.PreferencesUser;
import org.sikuli.basics.Settings;
import org.sikuli.script.support.Commons;

import java.io.*;
import java.util.*;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

public class Options {

  public interface Filter {
    boolean accept(String key);
  }

  int lvl = 3;

  private void log(int level, String message, Object... args) {
    Debug.logx(level, "Options: " + message, args);
  }

  private void logp(String message, Object... args) {
    log(-3, message, args);
  }

  private void p(String message, Object... args) {
    System.out.println(String.format(message, args));
  }

  private Properties options;

  public Options() {
    this.options = new Properties();
  }

  public Options(File optFile) {
    this();
    load(optFile);
  }

  public Options(String optFilePath) {
    this();
    load(optFilePath);
  }

  private String propOptionsFile = "SX_OPTION_FILE";

  public void load(File optFile) {
    add(optFile);
    set(propOptionsFile, optFile.getAbsolutePath());
  }

  public void load(File optFile, Filter filter) {
    add(optFile, filter);
    set(propOptionsFile, optFile.getAbsolutePath());
  }

  public void add(File optFile) {
    add(optFile, null);
  }

  public void add(File optFile, Filter filter) {
    String optContent = FileManager.readFileToString(optFile);
    String[] lines = optContent.split(System.lineSeparator());
    Map<String, String> optlines = new HashMap<>();
    if (lines.length > 0) {
      for (String line : lines) {
        line = line.strip();
        if (!line.isEmpty()) {
          if (line.startsWith("/") || line.startsWith("#") || line.startsWith("=")) {
            continue;
          }
          if (line.contains("=")) {
            String[] parts = line.split("=");
            if (parts.length > 1) {
              optlines.put(parts[0].strip(), parts[1].strip());
              continue;
            }
          }
          optlines.put(line, "");
        }
      }
      for (String key : optlines.keySet()) {
        if (filter == null || filter.accept(key)) {
          set(key, optlines.get(key));
        }
      }
    }
  }

  public void load(String optFilePath) {
    File optFile = add(optFilePath);
    if (optFile != null) {
      set(propOptionsFile, optFile.getAbsolutePath());
    }
  }

  public void load(String optFilePath, Filter filter) {
    File optFile = add(optFilePath, filter);
    if (optFile != null) {
      set(propOptionsFile, optFile.getAbsolutePath());
    }
  }

  public File add(String optFilePath) {
    return add(optFilePath, null);
  }

  public File add(String optFilePath, Filter filter) {
    File optFile = new File(optFilePath);
    if (optFile.isAbsolute()) {
      if (!optFile.exists()) {
        optFile = null;
        log(-1, "loadOptions: not exists: %s", optFile);
      }
    } else {
      for (File aFile : new File[]{Commons.getAppDataStore(), Commons.getWorkDir(), Commons.getUserHome()}) {
        optFile = new File(aFile, optFilePath);
        if (optFile.exists()) {
          break;
        } else {
          optFile = null;
        }
      }
    }
    if (optFile != null) {
      add(optFile, filter);
    }
    return optFile;
  }

  /**
   * save a properties store to a file (prop: this.comingfrom = abs. filepath)
   *
   * @return success
   */
  public boolean save() {
    String fpOptions = get(propOptionsFile, null);
    if (null == fpOptions) {
      log(-1, "saveOptions: no prop %s", propOptionsFile);
      return false;
    }
    return save(fpOptions);
  }

  /**
   * save a properties store to the given file
   *
   * @param optFilePath path to a file
   * @return success
   */
  public boolean save(String optFilePath) {
    File fOptions = new File(optFilePath);
    if (!fOptions.isAbsolute()) {
      fOptions = new File(Commons.getWorkDir(), optFilePath);
    }
    try {
      set(propOptionsFile, fOptions.getAbsolutePath());
      OutputStream os;
      os = new FileOutputStream(fOptions);
      options.store(os, "");
      os.close();
    } catch (Exception ex) {
      log(-1, "saveOptions: %s (error %s)", fOptions, ex.getMessage());
      return false;
    }
    log(lvl, "saveOptions: saved: %s", optFilePath);
    return true;
  }

  public boolean has(String pName) {
    return null == options.getProperty(pName) ? false : true;
  }

  /**
   * if no option file is found, the option is taken as not existing<br>
   * side-effect: if no options file is there, an options store will be created in memory<br>
   * in this case and when the option is absent or empty, the given default will be stored<br>
   * you might later save the options store to a file with saveOptions()<br>
   * the default value is either the empty string, number 0 or false
   *
   * @param pName    the option key (case-sensitive)
   * @param sDefault the default to be returned if option absent or empty
   * @return the associated value, the default value if absent or empty
   */
  public String get(String pName, String sDefault) {
    String pVal = options.getProperty(pName, sDefault);
    if (pVal.isEmpty()) {
      options.setProperty(pName, sDefault);
      return sDefault;
    }
    return pVal;
  }

  /**
   * {link getOption}
   *
   * @param pName the option key (case-sensitive)
   * @return the associated value, empty string if absent
   */
  public String get(String pName) {
    return get(pName, "");
  }

  /**
   * {link getOption}
   *
   * @param pName  the option key (case-sensitive)
   * @param sValue the value to be set
   */
  public void set(String pName, Object sValue) {
    if (pName.startsWith(Commons.SETTINGS_OPT)) {
      if (!Settings.set(pName.substring(Commons.SETTINGS_OPT.length()), sValue)) {
        return;
      }
    } else if (pName.startsWith(Commons.SXPREFS_OPT) && !Commons.isSandBox()) {
      PreferencesUser.get().getStore().put(pName.substring(Commons.SXPREFS_OPT.length()), sValue.toString());
    }
    options.setProperty(pName, sValue.toString());
  }

  public void add(String pName, Object sValue) {
    options.setProperty(pName, sValue.toString());
  }

  /**
   * {link getOption}
   *
   * @param pName    the option key (case-sensitive)
   * @param nDefault the default to be returned if option absent, empty or not convertible
   * @return the converted integer number, default if absent, empty or not possible
   */
  public int getInteger(String pName, Integer nDefault) {
    String pVal = options.getProperty(pName, nDefault.toString());
    int nVal = nDefault;
    try {
      nVal = Integer.decode(pVal);
    } catch (Exception ex) {
    }
    return nVal;
  }

  /**
   * {link getOption}
   *
   * @param pName the option key (case-sensitive)
   * @return the converted integer number, 0 if absent or not possible
   */
  public int getInteger(String pName) {
    return getInteger(pName, 0);
  }

  /**
   * {link getOption}
   *
   * @param pName  the option key (case-sensitive)
   * @param nValue the value to be set
   */
  public void setInteger(String pName, int nValue) {
    options.setProperty(pName, "" + nValue);
  }

  /**
   * {link getOption}
   *
   * @param pName    the option key (case-sensitive)
   * @param nDefault the default to be returned if option absent, empty or not convertible
   * @return the converted integer number, default if absent, empty or not possible
   */
  public long getLong(String pName, Long nDefault) {
    String pVal = options.getProperty(pName, nDefault.toString());
    long nVal = nDefault;
    try {
      nVal = Long.decode(pVal);
    } catch (Exception ex) {
    }
    return nVal;
  }

  /**
   * {link getOption}
   *
   * @param pName the option key (case-sensitive)
   * @return the converted long number, 0 if absent or not possible
   */
  public long getLong(String pName) {
    return getInteger(pName, 0);
  }

  /**
   * {link getOption}
   *
   * @param pName  the option key (case-sensitive)
   * @param nValue the value to be set
   */
  public void setLong(String pName, long nValue) {
    options.setProperty(pName, "" + nValue);
  }

  /**
   * {link getOption}
   *
   * @param pName the option key (case-sensitive)
   * @return the converted float number, default if absent or not possible
   */
  public float getFloat(String pName, float nDefault) {
    String pVal = options.getProperty(pName, "0");
    float nVal = nDefault;
    try {
      nVal = Float.parseFloat(pVal);
    } catch (Exception ex) {
    }
    return nVal;
  }

  /**
   * {link getOption}
   *
   * @param pName the option key (case-sensitive)
   * @return the converted float number, 0 if absent or not possible
   */
  public float getFloat(String pName) {
    return getFloat(pName, 0);
  }

  /**
   * {link getOption}
   *
   * @param pName  the option key (case-sensitive)
   * @param nValue the value to be set
   */
  public void setFloat(String pName, float nValue) {
    options.setProperty(pName, "" + nValue);
  }

  /**
   * {link getOption}
   *
   * @param pName the option key (case-sensitive)
   * @return the converted float number, default if absent or not possible
   */
  public double getDouble(String pName, double nDefault) {
    String pVal = options.getProperty(pName, "0");
    double nVal = nDefault;
    try {
      nVal = Double.parseDouble(pVal);
    } catch (Exception ex) {
    }
    return nVal;
  }

  /**
   * {link getOption}
   *
   * @param pName the option key (case-sensitive)
   * @return the converted double number, 0 if absent or not possible
   */
  public double getDouble(String pName) {
    return getDouble(pName, 0);
  }

  /**
   * {link getOption}
   *
   * @param pName  the option key (case-sensitive)
   * @param nValue the value to be set
   */
  public void setDouble(String pName, double nValue) {
    options.setProperty(pName, "" + nValue);
  }

  /**
   * {link getOption}
   *
   * @param pName    the option key (case-sensitive)
   * @param bDefault the default to be returned if option absent or empty
   * @return true if option has yes or no, false for no or false (not case-sensitive)
   */
  public boolean is(String pName, boolean bDefault) {
    String pVal = options.getProperty(pName, bDefault ? "true" : "false").toLowerCase();
    if (pVal.isEmpty()) {
      return bDefault;
    } else if (pVal.contains("yes") || pVal.contains("true") || pVal.contains("on")) {
      return true;
    }
    return false;
  }

  /**
   * {link getOption}
   *
   * @param pName the option key (case-sensitive)
   * @return true only if option exists and has yes or true (not case-sensitive), in all other cases false
   */
  public boolean is(String pName) {
    return is(pName, false);
  }

  /**
   * {link getOption}
   *
   * @param pName  the option key (case-sensitive)
   * @param bValue the value to be set
   */
  public void setBool(String pName, boolean bValue) {
    options.setProperty(pName, bValue ? "true" : "false");
  }

  /**
   * check whether options are defined
   *
   * @return true if at lest one option defined else false
   */
  public boolean hasOptions() {
    return options != null && options.size() > 0;
  }

  /**
   * all options and their values
   *
   * @return a map of key-value pairs containing the found options, empty if no options file found
   */
  public Map<String, String> getAll() {
    return getAll("");
  }

  public Map<String, String> getAll(String prefix) {
    Map<String, String> mapOptions = new HashMap<String, String>();
    Enumeration<?> optionNames = options.propertyNames();
    String optionName;
    while (optionNames.hasMoreElements()) {
      optionName = (String) optionNames.nextElement();
      if (prefix.isEmpty() || optionName.startsWith(prefix)) {
        mapOptions.put(optionName, get(optionName));
      }
    }
    return mapOptions;
  }

  /**
   * all options and their values written to sysout as key = value
   */
  public void dump(String key) {
    if (hasOptions()) {
      Map<String, String> mapOptions = getAll();
      logp("*** options dump" + (key.isEmpty() ? "" : " for: " + key));
      for (String sOpt : mapOptions.keySet()) {
        if (!options.isEmpty() && !sOpt.startsWith(key)) continue;
        logp("%s = %s", sOpt, mapOptions.get(sOpt));
      }
      logp("*** options dump end");
    }
  }

  //region static for Jython
  //  def makeOpts():
  //      return SXOpts.makeOpts()
  //
  public static Options makeOpts() {
    return new Options();
  }

  //  def loadOpts(filePath):
  //      return SXOpts.loadOpts(filePath)
  public static Options loadOpts(String file) {
    Options options = makeOpts();
    options.load(file);
    return options;
  }

  //  def saveOpts(props, filePath = None):
  //      if not filePath:
  //      return SXOpts.saveOpts(props)
  //      else:
  //      return SXOpts.saveOpts(props, filePath)
  public static String saveOpts(Options opts) {
    return saveOpts(opts, opts.get("OptionsFile", "optionsfile"));
  }

  public static String saveOpts(Options opts, String file) {
    opts.save(file);
    return opts.options.getProperty("OptionsFile", "no filename");
  }

  //  def getOpts(props):
  //      return SXOpts.getOpts(props)
  public static Map<String, String> getOpts(Options opts) {
    return opts.getAll();
  }

  //  def hasOpts(props):
  //      return SXOpts.hasOpts(props)
  public static boolean hasOpts(Options opts) {
    return opts.hasOptions();
  }

  //  def setOpts(props, adict):
  //      return SXOpts.setOpts(props, adict)
  public static void setOpts(Options opts, Map<String, String> entries) {
    for (String key : entries.keySet()) {
      opts.options.setProperty(key, entries.get(key));
    }
  }

  //  def delOpts(props):
  //      return SXOpts.delOpts(props)
  public static void delOpts(Options opts) {
    String optsFile = opts.get("OptionsFile", "optionsfile");
    opts.options.clear();
    opts.options.setProperty("OptionsFile", optsFile);
  }

  //  def hasOpt(props, key):
  //      return SXOpts.hasOpts(props, key)
  public static boolean hasOpt(Options opts, String key) {
    return opts.options.containsKey(key);
  }

  //  def getOpt(props, key, deflt = ""):
  //      return SXOpts.getOpt(props, key, deflt)
  public static String getOpt(Options opts, String key, String theDefault) {
    return opts.options.getProperty(key, theDefault);
  }

  //  def getOptNum(props, key, deflt = 0):
  //      return SXOpts.getOptNum(props, key, deflt)
  public static Double getOptNum(Options opts, String key, Object theDefault) {
    try {
      return Double.parseDouble(opts.options.getProperty(key, "" + theDefault));
    } catch (NumberFormatException e) {
      return 0.0;
    }
  }

  public static Integer getOptInt(Options opts, String key, Integer theDefault) {
    Double value = 0.0;
    try {
      value = Double.parseDouble(opts.options.getProperty(key, "" + theDefault));
    } catch (NumberFormatException e) {
    }
    return value.intValue();
  }

  //  def setOpt(props, key, value):
  //      return SXOpts.setOpt(props, key, value)
  public static void setOpt(Options opts, String key, String value) {
    opts.options.setProperty(key, value);
  }

  //  def setOptNum(props, key, value):
  //      return SXOpts.setOptNum(props, key, value)
  public static void setOptNum(Options opts, String key, Object value) {
    opts.options.setProperty(key, "" + value);
  }

  //  def delOpt(props, key):
  //      return SXOpts.delOpt(props, key)
  public static void delOpt(Options opts, String key) {
    opts.options.remove(key);
  }

  //endregion

  //region user preferences store
  public static void prefStore(String key, Object value) {
    PreferencesUser prefs = PreferencesUser.get();
    prefs.put(userPrefix + key, "" + value);
  }

  public static String prefLoad(String key) {
    return prefLoad(userPrefix + key, "");
  }

  private static final String userPrefix = "USER::";

  public static Map<String, String> prefAll() {
    return prefComplete()
        .entrySet()
        .stream()
        .filter(e -> e.getKey().startsWith(userPrefix))
        .sorted(Map.Entry.comparingByKey())
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            Map.Entry::getValue,
            (oldValue, newValue) -> oldValue, LinkedHashMap::new));
  }

  private static Map<String, String> prefComplete() {
    PreferencesUser prefs = PreferencesUser.get();
    Map<String, String> allPrefs = new HashMap<>();
    try {
      final Preferences store = prefs.getStore();
      String[] keys = store.keys();
      for (String key : keys) {
        allPrefs.put(key, store.get(key, ""));
      }
    } catch (BackingStoreException e) {
    }
    return allPrefs;
  }

  public static void prefDump() {
    final Map<String, String> prefsSX = Options.prefComplete();
    System.out.println("***** Preferences SikuliX *****");
    prefsSX.entrySet()
        .stream()
        .filter(e -> !e.getKey().startsWith(userPrefix))
        .sorted(Map.Entry.comparingByKey())
        .forEach(System.out::println);
    System.out.println("***** Preferences User:: *****");
    prefsSX.entrySet()
        .stream()
        .filter(e -> e.getKey().startsWith(userPrefix))
        .sorted(Map.Entry.comparingByKey())
        .forEach(System.out::println);
    System.out.println("***** Preferences Dump End *****");
  }

  public static String prefLoad(String key, Object deflt) {
    PreferencesUser prefs = PreferencesUser.get();
    return prefs.get(userPrefix + key, "" + deflt);
  }

  public static String prefRemove(String key) {
    PreferencesUser prefs = PreferencesUser.get();
    String value = prefs.get(userPrefix + key, "");
    prefs.remove(userPrefix + key);
    return value;
  }

  public static Map<String, String> prefRemoveAll() {
    PreferencesUser prefs = PreferencesUser.get();
    Map<String, String> allPrefs = prefAll();
    prefs.removeAll(userPrefix);
    return allPrefs;
  }

  public static boolean prefExport(String path) {
    PreferencesUser prefs = PreferencesUser.get();
    return prefs.save(path);
  }

  public static boolean prefImport(String path) {
    PreferencesUser prefs = PreferencesUser.get();
    return prefs.load(path);
  }
  //endregion
}
