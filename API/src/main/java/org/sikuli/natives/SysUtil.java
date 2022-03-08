/*
 * Copyright (c) 2010-2022, sikuli.org, sikulix.com - MIT license
 */

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sikuli.natives;

import java.lang.reflect.Constructor;

/**
 *
 * @author rhocke
 */
public class SysUtil {

  static OSUtil osUtil = null;

  static String getOSUtilClass() {
    String pkg = "org.sikuli.natives.";
    String os = System.getProperty("os.name").toLowerCase();
    if (os.startsWith("mac")) {
      return pkg + "MacUtil";
    } else if (os.startsWith("windows")) {
      return pkg + "WinUtil";
    } else {
      return pkg + "LinuxUtil";
    }
  }

  public static OSUtil getOSUtil() {
    if (osUtil == null) {
      try {
        Class<?> c = Class.forName(SysUtil.getOSUtilClass());
        Constructor<?> constr = c.getConstructor();
        osUtil = (OSUtil) constr.newInstance();
        osUtil.init();
      } catch (Exception e) {
        throw new RuntimeException(String.format("SikuliX: fatal: getOSUtil:" + e.getMessage()));
      }
    }
    return osUtil;
  }
}
