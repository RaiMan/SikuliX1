package org.sikuli.natives.mac.jna;

import org.rococoa.ObjCClass;
import org.rococoa.ObjCObject;
import org.rococoa.Rococoa;
import org.rococoa.cocoa.foundation.NSArray;

/**
 * JNA binding for NSRunningApplication
 *
 * @author mbalmer
 *
 */
public interface NSRunningApplication extends ObjCObject {

  public static final _Class CLASS = Rococoa.createClass("NSRunningApplication", _Class.class);


  public interface _Class extends ObjCClass {
	 /**
	  * Finds all applications with the given bundle name
	  *
	  * @param bundleIdentifier
	  * @return
	  */
    NSArray runningApplicationsWithBundleIdentifier(String bundleIdentifier);

    /**
     * Finds all applicaitons with the given pid
     *
     * @param pid
     * @return
     */
    NSRunningApplication runningApplicationWithProcessIdentifier(int pid);
  }

  public static interface NSApplicationActivationOptions {
    /** all of the application's windows are brought forward. */
    public static final int NSApplicationActivateAllWindows = 1 << 0;
    /** the application is activated regardless of the currently active app, potentially stealing focus from the user */
    public static final int NSApplicationActivateIgnoringOtherApps = 1 << 1;
  }

  /**
   * @return this running applications bundle identifier
   */
  String bundleIdentifier();

  /**
   * @return this running applications PID
   */
  int processIdentifier();

  /**
   * Activates this running application.
   *
   * @param options
   * @return
   */
  boolean activateWithOptions(int options);

  /**
   * Checks if this running application is currently active.
   *
   * @return
   */
  boolean isActive();
}