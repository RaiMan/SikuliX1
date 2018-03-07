/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script;

import org.sikuli.basics.Debug;

/**
 * implements the SikuliX FindFailed exception class
 * and defines constants and settings for the feature FindFailedResponse
 */
public class FindFailed extends SikuliException {

	/**
	 * default FindFailedResponse is ABORT
	 */
	public static FindFailedResponse defaultFindFailedResponse = FindFailedResponse.ABORT;

	/**
	 * FindFailedResponse PROMPT: should display a prompt dialog with the failing image
	 * having the options retry, skip and abort
	 */
	public static final FindFailedResponse PROMPT = FindFailedResponse.PROMPT;

	/**
	 * FindFailedResponse RETRY: should retry the find op on FindFailed
	 */
	public static final FindFailedResponse RETRY = FindFailedResponse.RETRY;

	/**
	 * FindFailedResponse SKIP: should silently continue on FindFailed
	 */
	public static final FindFailedResponse SKIP = FindFailedResponse.SKIP;

	/**
	 * FindFailedResponse ABORT: should abort the SikuliX application
	 */
	public static final FindFailedResponse ABORT = FindFailedResponse.ABORT;

	/**
	 * FindFailedResponse HANDLE: should call a given handler on FindFailed
	 */
	public static final FindFailedResponse HANDLE = FindFailedResponse.HANDLE;

  private static Object ffHandler = null;
  private static Object imHandler = null;
  private static Object defaultHandler = null;

  /**
	 * the exception
	 * @param message to be shown
	 */
	public FindFailed(String message) {
    super(message);
    _name = "FindFailed";
  }

  public static String createdefault(Region reg, Image img) {
    String msg = "";
    if (img.isText()) {
      msg = String.format("%s as text", img.getName());
    } else if (img.getSize().width < 0 && img.getSize().height < 0) {
      msg = String.format("%s not loaded", img.getName());
    } else {
      msg = String.format("%s in %s", img, reg);
    }
    return msg;
  }

  public static FindFailedResponse getResponse() {
    return defaultFindFailedResponse;
  }

  public static FindFailedResponse setResponse(FindFailedResponse response) {
    defaultFindFailedResponse = response;
    return defaultFindFailedResponse;
  }

  public static FindFailedResponse setHandler(Object observer) {
    if (observer != null && (observer.getClass().getName().contains("org.python")
            || observer.getClass().getName().contains("org.jruby"))) {
      observer = new ObserverCallBack(observer, ObserveEvent.Type.FINDFAILED);
    } else {
      ((ObserverCallBack) observer).setType(ObserveEvent.Type.FINDFAILED);
    }
    ffHandler = observer;
    Debug.log(3, "Setting Default FindFailedHandler");
    return defaultFindFailedResponse;
  }

  protected void setFindFailedHandler(Object handler) {
    ffHandler = setHandler(handler, ObserveEvent.Type.FINDFAILED);
  }

  public void setImageMissingHandler(Object handler) {
    imHandler = setHandler(handler, ObserveEvent.Type.MISSING);
  }

  private Object setHandler(Object handler, ObserveEvent.Type type) {
    defaultFindFailedResponse = HANDLE;
    if (handler != null && (handler.getClass().getName().contains("org.python")
            || handler.getClass().getName().contains("org.jruby"))) {
      handler = new ObserverCallBack(handler, type);
    } else {
      ((ObserverCallBack) handler).setType(type);
    }
    return handler;
  }

  public static Object getFindFailedHandler() {
    return ffHandler;
  }

  public static Object getImageMissingHandler() {
    return imHandler;
  }

  public static FindFailedResponse reset() {
    defaultFindFailedResponse = ABORT;
    ffHandler = null;
    imHandler = null;
    return defaultFindFailedResponse;
  }
}
