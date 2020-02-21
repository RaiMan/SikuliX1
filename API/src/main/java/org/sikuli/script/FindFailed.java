/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script;

/**
 * SikuliX FindFailed exception<br>
 * constants and settings for the features<br>
 * FindFailedResponse<br>
 * FindFailedHandler<br>
 * ImageMissingHandler
 */
public class FindFailed extends SikuliException {

  //<editor-fold desc="00 instance">
  /**
   * the exception
   * @param message to be shown
   */
  public FindFailed(String message) {
    super(message);
    _name = "FindFailed";
  }

  /**
   * reset all: response ABORT, findFailedHandler null, imageMissingHandler null
   */
  public static void reset() {
    response = ABORT;
    ffHandler = null;
    imHandler = null;
  }
  //</editor-fold>

  //<editor-fold desc="05 FindFailedResponse">
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
	 * FindFailedResponse HANDLE: should call a handler {@link #setFindFailedHandler(Object)} on FindFailed
	 */
	public static final FindFailedResponse HANDLE = FindFailedResponse.HANDLE;

  /**
   * Global FindFailedResponse for new {@link Region}s<br>
   * ABORT - abort script on FindFailed <br>
   * SKIP - ignore FindFailed<br>
   * PROMPT - display prompt on FindFailed to let user decide how to proceed<br>
   * RETRY - continue to wait for appearence after FindFailed<br>
   * HANDLE - (set implicit) call a handler on exception {@link #setFindFailedHandler(Object)}<br>
   * default: ABORT
   *
   * @param response {@link FindFailed}
   */
  public static void setResponse(FindFailedResponse response) {
    if (!HANDLE.equals(response)) {
      FindFailed.response = response;
    }
  }

  /**
   * reset to default {@link #setResponse(FindFailedResponse)}
   */
  public static void resetResponse() {
    FindFailed.response = ABORT;
  }

  /**
   * @return the current setting {@link #setResponse(FindFailedResponse)}
   */
  public static FindFailedResponse getResponse() {
    return response;
  }

  private static FindFailedResponse response = ABORT;
  //</editor-fold>

  //<editor-fold desc="010 FindFailed handler">
  /**
   * Global FindFailedHandler for new {@link Region}s<br>
   * default: none
   *
   * @param handler {@link ObserverCallBack}
   */
  public static void setFindFailedHandler(Object handler) {
    response = HANDLE;
    ffHandler = setHandler(handler, ObserveEvent.Type.FINDFAILED);
  }

  /**
   * reset to default: no handler, response ABORT
   */
  public static void resetFindFailedHandler() {
    response = ABORT;
    ffHandler = null;
  }

  /**
   * @return the current handler
   */
  public static Object getFindFailedHandler() {
    return ffHandler;
  }

  private static Object ffHandler = null;
  //</editor-fold>

  //<editor-fold desc="015 ImageMissing handler">
  /**
   * Global ImageMissingHandler for new {@link Region}s<br>
   * default: none
   *
   * @param handler {@link ObserverCallBack}
   */
  public static void setImageMissingHandler(Object handler) {
    imHandler = setHandler(handler, ObserveEvent.Type.MISSING);
  }

  /**
   * reset to default: no handler
   */
  public static void resetImageMissingHandler() {
    imHandler = null;
  }

  /**
   * @return the current handler
   */
  public static Object getImageMissingHandler() {
    return imHandler;
  }

  private static Object imHandler = null;
  //</editor-fold>

  protected static Object setHandler(Object handler, ObserveEvent.Type type) {
    if (handler != null && (handler.getClass().getName().contains("org.python")
            || handler.getClass().getName().contains("org.jruby"))) {
      handler = new ObserverCallBack(handler, type);
    } else {
      ((ObserverCallBack) handler).setType(type);
    }
    return handler;
  }

  protected static String createErrorMessage(Element reg, Image img) {
    String msg = "";
    if (img.isText()) {
      msg = String.format("%s as text", img.getName());
    } else if (img.getSize().width < 0 && img.getSize().height < 0) {
      msg = String.format("%s not loaded", img.url());
    } else {
      msg = String.format("%s in %s", img, reg);
    }
    return msg;
  }
}
