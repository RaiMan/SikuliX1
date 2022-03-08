/*
 * Copyright (c) 2010-2022, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.natives.mac.jna;

import java.util.Arrays;
import java.util.List;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.mac.CoreFoundation.CFArrayRef;
import com.sun.jna.platform.mac.CoreFoundation.CFStringRef;

/**
 * JNA bindings to CoreGraphics *
 */

public interface CoreGraphics extends Library {

	/**
	 * Window number key
	 */
	public static final CFStringRef kCGWindowNumber = CFStringRef.createCFString("kCGWindowNumber");

	/**
	 * Window owner pid key
	 */
	public static final CFStringRef kCGWindowOwnerPID = CFStringRef.createCFString("kCGWindowOwnerPID");

	/**
	 * Window name key
	 */
	public static final CFStringRef kCGWindowName = CFStringRef.createCFString("kCGWindowName");

	/**
	 * Window owner name key
	 */
	public static final CFStringRef kCGWindowOwnerName = CFStringRef.createCFString("kCGWindowOwnerName");

	/**
	 * Window bounds key
	 */
	public static final CFStringRef kCGWindowBounds = CFStringRef.createCFString("kCGWindowBounds");

	/**
	 * List all windows in this user session, including both on- and off-screen
	 * windows. The parameter `relativeToWindow' should be `kCGNullWindowID'.
	 */
	public static final int kCGWindowListOptionAll = 0;

	/*
	 * List all on-screen windows in this user session, ordered from front to back.
	 * The parameter `relativeToWindow' should be `kCGNullWindowID'.
	 */
	public static final int kCGWindowListOptionOnScreenOnly = (1 << 0);

	/*
	 * List all on-screen windows above the window specified by `relativeToWindow',
	 * ordered from front to back.
	 */
	public static final int kCGWindowListOptionOnScreenAboveWindow = (1 << 1);

	/*
	 * List all on-screen windows below the window specified by `relativeToWindow',
	 * ordered from front to back.
	 */
	public static final int kCGWindowListOptionOnScreenBelowWindow = (1 << 2);

	/*
	 * Include the window specified by `relativeToWindow' in any list, effectively
	 * creating `at-or-above' or `at-or-below' lists.
	 */
	public static final int kCGWindowListOptionIncludingWindow = (1 << 3);

	/* Exclude any windows from the list that are elements of the desktop. */
	public static final int kCGWindowListExcludeDesktopElements = (1 << 4);

	CoreGraphics INSTANCE = Native.load("CoreGraphics", CoreGraphics.class);

	CFArrayRef CGWindowListCopyWindowInfo(int option, int relativeToWindow);
	boolean CGRectMakeWithDictionaryRepresentation(Pointer dict, CGRectRef rect);

	class CGPoint extends Structure {
	    public double x;
	    public double y;

	    @Override
	    protected List<String> getFieldOrder() {
	      return Arrays.asList("x", "y");
	    }
	  }


	  class CGSize extends Structure {
	    public double width;
	    public double height;

	    @Override
	    protected List<String> getFieldOrder() {
	      return Arrays.asList("width", "height");
	    }
	  }


	  class CGRect extends Structure implements Structure.ByValue {
	    public static class CGRectByValue extends CGRect {
	    }

	    public CGPoint origin;
	    public CGSize size;

	    @Override
	    protected List<String> getFieldOrder() {
	      return Arrays.asList("origin", "size");
	    }
	  }

	  class CGRectRef extends Structure implements Structure.ByReference {
	    public static class CGRectByReference extends CGRect {
	    }

	    public CGPoint origin;
	    public CGSize size;

	    @Override
	    protected List<String> getFieldOrder() {
	      return Arrays.asList("origin", "size");
	    }
	  }
}