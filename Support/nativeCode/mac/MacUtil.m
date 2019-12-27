/*
 * Copyright 2010-2016, Sikuli.org, sikulix.com
 * Released under the MIT License.
 *
 * modified RaiMan 2013
 */

#pragma GCC diagnostic ignored "-Wdeprecated-declarations"

#import "org_sikuli_natives_MacUtil.h"
#import <Cocoa/Cocoa.h>
//#import <jawt.h>
#import <JavaVM/jawt_md.h>
#import <AppKit/NSAccessibility.h>
#include <stdio.h>

NSWindow * GetWindowFromComponent(jobject parent, JNIEnv *env) {
   JAWT awt;
   JAWT_DrawingSurface* ds;
   JAWT_DrawingSurfaceInfo* dsi;
   JAWT_MacOSXDrawingSurfaceInfo* dsi_mac;
   jboolean result;
   jint lock;

	// printf("%s\n", "***** Entering GetWindowFromComponent");
   // Get the AWT
   awt.version = JAWT_VERSION_1_4;
   if( (result = JAWT_GetAWT(env, &awt)) == JNI_FALSE){
      NSLog(@"AWT not found");
   }
	// printf("%s\n", "***** GetWindowFromComponent: AWT found");

   // Get the drawing surface
   ds = awt.GetDrawingSurface(env, parent);
   if(ds == NULL)
      NSLog(@"no drawing surface");
	// printf("%s\n", "***** GetWindowFromComponent: DS found");

   // Lock the drawing surface
   lock = ds->Lock(ds);
   if((lock & JAWT_LOCK_ERROR) != 0) {
      NSLog(@"error locking surface");
      awt.FreeDrawingSurface(ds);
      return NULL;
   }
	// printf("%s\n", "***** GetWindowFromComponent: DS locked");

   // Get the drawing surface info
   dsi = ds->GetDrawingSurfaceInfo(ds);
   //NSLog(@"drawing info %x", dsi);
	// printf("%s\n", "***** GetWindowFromComponent: DSI");

   // Get the platform-specific drawing info
   dsi_mac = (JAWT_MacOSXDrawingSurfaceInfo*)dsi->platformInfo;
   //NSLog(@"mac drawing info %x", dsi_mac);
	// printf("%s\n", "***** GetWindowFromComponent: DSI MAC");

   // Get the NSView corresponding to the component that was passed
   NSView *view = (NSView *) dsi_mac->cocoaViewRef;
	// printf("%s\n", "***** GetWindowFromComponent: got view");

   // Free the drawing surface info
   ds->FreeDrawingSurfaceInfo(dsi);
   // Unlock the drawing surface
   ds->Unlock(ds);

   // Free the drawing surface
   awt.FreeDrawingSurface(ds);

   // Get the view's parent window; this is what we need to show a sheet
	return [view window];
}

JNIEXPORT void JNICALL Java_org_sikuli_natives_MacUtil_bringWindowToFront
  (JNIEnv *env, jclass jobj, jobject jwin, jboolean ignoreMouse){

   NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init];

   NSWindow *win = GetWindowFromComponent(jwin, env);
   //NSLog(@"bringWindowToFront: %@", win);
	 [win setIgnoresMouseEvents:ignoreMouse];
   /*
   [win setBackgroundColor:[NSColor blackColor]];
   [win setOpaque:NO];
   [win setAlphaValue:0.60];
   */
   [win setLevel:NSScreenSaverWindowLevel];
   [win orderFront:nil];

   [pool release];
}

JNIEXPORT jint JNICALL Java_org_sikuli_natives_MacUtil_getPID
   (JNIEnv *env, jclass jobj, jstring jAppName){
   int pid = 0;
   NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init];
   const jchar *chars = (*env)->GetStringChars(env, jAppName, NULL);
   NSString *appName = [NSString stringWithCharacters:(UniChar *)chars
               length:(*env)->GetStringLength(env, jAppName)];
   (*env)->ReleaseStringChars(env, jAppName, chars);
   appName = [@"/" stringByAppendingPathComponent:appName];
   NSArray* apps = [[NSWorkspace sharedWorkspace] runningApplications];
   for(NSRunningApplication* app in apps){
      NSURL *url = [app executableURL];
      NSString *path = [url path];
      //NSLog(@"app: %@", path);
      NSRange range = [path rangeOfString:appName];
      if( range.location != NSNotFound ){
         pid = [app processIdentifier];
         //NSLog(@"%@ pid: %d", path, pid);
         break;
      }
   }
   [pool release];
   return pid;
}

NSRect getBoundOfUIElement(AXUIElementRef elem){
   NSRect rect;
   AXValueRef value;

   AXUIElementCopyAttributeValue(elem, kAXPositionAttribute,(CFTypeRef*)&value);
   AXValueGetValue(value, kAXValueCGPointType, (void *) &rect.origin);
   AXUIElementCopyAttributeValue(elem, kAXSizeAttribute, (CFTypeRef *)&value);
   AXValueGetValue(value, kAXValueCGSizeType, (void *) &rect.size);
   return rect;
}

#define CLASS_RECTANGLE "java/awt/Rectangle"

jobject convertNSRectToJRectangle(JNIEnv *env, NSRect r){
   jclass jClassRect = (*env)->FindClass(env, CLASS_RECTANGLE);
   jmethodID initMethod = (*env)->GetMethodID(env, jClassRect, "setRect", "(DDDD)V");
   jobject ret = NULL;
   if(initMethod!=NULL){
      ret = (*env)->AllocObject(env, jClassRect);
      (*env)->CallVoidMethod(env, ret, initMethod,
                                      r.origin.x, r.origin.y,
                                      r.size.width, r.size.height);
   }
   (*env)->DeleteLocalRef(env, jClassRect);
   return ret;
}

JNIEXPORT jobject JNICALL Java_org_sikuli_natives_MacUtil_getFocusedRegion
  (JNIEnv *env, jclass jobj){
   AXUIElementRef sysElement = AXUIElementCreateSystemWide();
   AXUIElementRef focusedApp;
   AXUIElementRef focusedWindow;
   AXUIElementCopyAttributeValue(sysElement,
      (CFStringRef)kAXFocusedApplicationAttribute,
      (CFTypeRef*)&focusedApp);
   if(AXUIElementCopyAttributeValue((AXUIElementRef)focusedApp,
      (CFStringRef)NSAccessibilityFocusedWindowAttribute,
      (CFTypeRef*)&focusedWindow) == kAXErrorSuccess){
      NSRect r = getBoundOfUIElement(focusedWindow);
      return convertNSRectToJRectangle(env, r);
   }
   return NULL;
}

JNIEXPORT jobject JNICALL Java_org_sikuli_natives_MacUtil_getRegion
  (JNIEnv *env, jclass jobj, jint pid, jint winNum){
   AXUIElementRef ui = AXUIElementCreateApplication(pid);

   NSArray       *proc_attrs;
   CFStringRef    attribute  = CFSTR("AXWindows");
   id             windows    = nil;

   if (AXUIElementCopyAttributeNames(ui,(CFArrayRef *)&proc_attrs) == kAXErrorSuccess) {
      if (AXUIElementCopyAttributeValue(ui,attribute,(CFTypeRef *)&windows) ==kAXErrorSuccess)
      {
         if (CFGetTypeID(windows) == CFArrayGetTypeID())
         {
            NSArray       *window_attrs = (NSArray *)windows;
            if( winNum < [window_attrs count] ){
               AXUIElementRef aref = (AXUIElementRef) [window_attrs objectAtIndex:winNum];
               NSRect r = getBoundOfUIElement(aref);
               //NSLog(@"%f %f %f %f", r.origin.x, r.origin.y, r.size.width, r.size.height);
               return convertNSRectToJRectangle(env, r);
            }
         }
       }

   }
   return NULL;
}

JNIEXPORT jboolean JNICALL Java_org_sikuli_natives_MacUtil__1openApp
  (JNIEnv *env, jclass jobj, jstring jAppName){
   NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init];
   const jchar *chars = (*env)->GetStringChars(env, jAppName, NULL);
   NSString *appName = [NSString stringWithCharacters:(UniChar *)chars
               length:(*env)->GetStringLength(env, jAppName)];
   (*env)->ReleaseStringChars(env, jAppName, chars);
   BOOL ret = [[NSWorkspace sharedWorkspace] launchApplication:appName];
   [pool release];
   return ret;
}

JNIEXPORT jboolean JNICALL Java_org_sikuli_natives_MacUtil_isAxEnabled
  (JNIEnv *env, jclass jobj){
     return AXAPIEnabled();
}

JNIEXPORT void JNICALL Java_org_sikuli_natives_MacUtil_openAxSetting
  (JNIEnv *env, jclass jobj){
     NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init];
     NSAppleScript *a = [[NSAppleScript alloc] initWithSource:@"tell application \"System Preferences\"\nactivate\nset current pane to pane \"com.apple.preference.universalaccess\"\nend tell"];
     [a executeAndReturnError:nil];
     [a release];
     [pool release];
}
