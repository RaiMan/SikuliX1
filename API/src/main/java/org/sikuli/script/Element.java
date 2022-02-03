/*
 * Copyright (c) 2010-2021, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script;

import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;
import org.sikuli.basics.Settings;
import org.sikuli.script.support.Commons;
import org.sikuli.script.support.FindFailedDialog;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * INTERNAL: An abstract super-class for {@link Region} and {@link Image}.
 * <br>
 * <p>BE AWARE: This class cannot be used as such (cannot be instantiated)
 * <br>... instead use the classes Region or Image as needed</p>
 * NOTES:
 * <br>- the classname might change in the future without notice
 * <br>- the intention is, to have only one implementation for features, that are the same for Region and Image
 * <br>- the implementation here is ongoing beginning with version 2.0.2 and hence not complete yet
 * <br>- you might get <b>not-implemented exceptions</b> until complete
 */
public abstract class Element {

  protected static final int logLevel = 3;
  protected static boolean silent = false;

  protected static void log(int level, String message, Object... args) {
    if (!Debug.is(level) || silent) {
      return;
    }
    String className = Thread.currentThread().getStackTrace()[2].getClassName();
    String caller = className.substring(className.lastIndexOf(".") + 1);
    Debug.logx(level, caller + ": " + message, args);
  }

  public Region asRegion() {
    return (Region) this;
  }

  public Image asImage() {
    return (Image) this;
  }

  //<editor-fold desc="01 Fields x, y, w, h">

  /**
   * @return x of top left corner
   */
  public int getX() {
    return x;
  }

  /**
   * X-coordinate of the Region (ignored for Image)
   */
  public int x = 0;

  /**
   * @return y of top left corner
   */
  public int getY() {
    return y;
  }

  /**
   * Y-coordinate of the Region (ignored for Image)
   */
  public int y = 0;

  /**
   * @return width of region/image
   */
  public int getW() {
    return w;
  }

  /**
   * Width of the Region/Image
   */
  public int w = 0;

  /**
   * @return height of region/image
   */
  public int getH() {
    return h;
  }

  /**
   * Height of the Region/Image
   */
  public int h = 0;

  public boolean isEmpty() {
    return w <= 1 && h <= 1;
  }

  /**
   * INTERNAL: to identify a Region or Image
   *
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Convenience: App: the window title
   *
   * @return the name
   */
  public String getTitle() {
    return name;
  }

  /**
   * INTERNAL: to identify a Region or Image
   *
   * @param name to be used
   */
  public void setName(String name) {
    this.name = name;
  }

  private String name = "";
  //</editor-fold>

  //<editor-fold desc="06 Fields wait observe timing">

  /**
   * the time in seconds a find operation should wait
   * <p>
   * for the appearence of the target in this region<br>
   * initial value is the global AutoWaitTimeout setting at time of Region creation<br>
   *
   * @param sec seconds
   */
  public void setAutoWaitTimeout(double sec) {
    autoWaitTimeout = sec;
  }

  /**
   * current setting for this region (see setAutoWaitTimeout)
   *
   * @return value of seconds
   */
  public double getAutoWaitTimeout() {
    return autoWaitTimeout;
  }

  /**
   * Default time to wait for an image {@link Settings}
   */
  double autoWaitTimeoutDefault = Settings.AutoWaitTimeout;
  double autoWaitTimeout = autoWaitTimeoutDefault;

  /**
   * @return the regions current WaitScanRate
   */
  public float getWaitScanRate() {
    return waitScanRate;
  }

  /**
   * set the regions individual WaitScanRate
   *
   * @param waitScanRate decimal number
   */
  public void setWaitScanRate(float waitScanRate) {
    this.waitScanRate = waitScanRate;
  }

  float waitScanRateDefault = Settings.WaitScanRate;
  float waitScanRate = waitScanRateDefault;


  /**
   * @return the regions current ObserveScanRate
   */
  public float getObserveScanRate() {
    return observeScanRate;
  }

  /**
   * set the regions individual ObserveScanRate
   *
   * @param observeScanRate decimal number
   */
  public void setObserveScanRate(float observeScanRate) {
    this.observeScanRate = observeScanRate;
  }

  float observeScanRateDefault = Settings.ObserveScanRate;
  float observeScanRate = observeScanRateDefault;

  /**
   * INTERNAL USE: Observe
   *
   * @return the regions current RepeatWaitTime time in seconds
   */
  public int getRepeatWaitTime() {
    return repeatWaitTime;
  }

  /**
   * INTERNAL USE: Observe set the regions individual WaitForVanish
   *
   * @param time in seconds
   */
  public void setRepeatWaitTime(int time) {
    repeatWaitTime = time;
  }

  int repeatWaitTimeDefault = Settings.RepeatWaitTime;
  int repeatWaitTime = repeatWaitTimeDefault;
  //</editor-fold>

  //<editor-fold desc="07 Fields throwException, findFailed/imageMissing">
  //<editor-fold desc="1 throwexception">

  /**
   * true - should throw {@link FindFailed} if not found in this region<br>
   * false - do not abort script on FindFailed (might lead to NPE's later)<br>
   * default: {@link Settings#ThrowException}<br>
   * sideEffects: {@link #setFindFailedResponse(FindFailedResponse)} true:ABORT, false:SKIP<br>
   * see also: {@link #setFindFailedResponse(FindFailedResponse)}<br>
   * and: {@link #setFindFailedHandler(Object)}
   *
   * @param flag true/false
   */
  public void setThrowException(boolean flag) {
    throwException = flag;
    if (throwException) {
      findFailedResponse = FindFailedResponse.ABORT;
    } else {
      findFailedResponse = FindFailedResponse.SKIP;
    }
  }

  /**
   * reset to default {@link #setThrowException(boolean)}
   */
  public void resetThrowException() {
    setThrowException(throwExceptionDefault);
  }

  /**
   * current setting {@link #setThrowException(boolean)}
   *
   * @return true/false
   */
  public boolean getThrowException() {
    return throwException;
  }

  boolean throwExceptionDefault = Settings.ThrowException;
  boolean throwException = throwExceptionDefault;
  //</editor-fold>

  //<editor-fold desc="2 findFailedResponse">

  /**
   * FindFailedResponse.<br>
   * ABORT - abort script on FindFailed <br>
   * SKIP - ignore FindFailed<br>
   * PROMPT - display prompt on FindFailed to let user decide how to proceed<br>
   * RETRY - continue to wait for appearence after FindFailed<br>
   * HANDLE - call a handler on exception {@link #setFindFailedHandler(Object)}<br>
   * default: ABORT<br>
   * see also: {@link #setThrowException(boolean)}
   *
   * @param response {@link FindFailed}
   */
  public void setFindFailedResponse(FindFailedResponse response) {
    if (!FindFailedResponse.HANDLE.equals(response) && !FindFailedResponse.RETRY.equals(response)) {
      findFailedResponse = response;
    } else {
      findFailedResponse = FindFailedResponse.PROMPT;
    }
  }

  /**
   * reset to default {@link #setFindFailedResponse(FindFailedResponse)}
   */
  public void resetFindFailedResponse() {
    setFindFailedResponse(findFailedResponseDefault);
  }

  /**
   * @return the current setting {@link #setFindFailedResponse(FindFailedResponse)}
   */
  public FindFailedResponse getFindFailedResponse() {
    return findFailedResponse;
  }

  FindFailedResponse findFailedResponseDefault = FindFailed.getResponse();
  FindFailedResponse findFailedResponse = findFailedResponseDefault;

  void setImageMissingHandler(Object handler) {
    imageMissingHandler = FindFailed.setHandler(handler, ObserveEvent.Type.MISSING);
  }

  Object imageMissingHandler = FindFailed.getImageMissingHandler();
  //</editor-fold>

  //<editor-fold desc="3 findFailedHandler">
  public void setFindFailedHandler(Object handler) {
    findFailedResponse = FindFailedResponse.HANDLE;
    findFailedHandler = FindFailed.setHandler(handler, ObserveEvent.Type.FINDFAILED);
    log(logLevel, "Setting FindFailedHandler");
  }

  Object findFailedHandler = FindFailed.getFindFailedHandler();
  //</editor-fold>

  //</editor-fold>

  //<editor-fold desc="08 Handler imageMissing findFailed">
  Boolean handleImageMissing(Image img, boolean recap) {
    Commons.trace("");
    log(logLevel, "handleImageMissing: %s", img.getName());
    ObserveEvent evt = null;
    FindFailedResponse response = findFailedResponse;
    if (!recap && imageMissingHandler != null) {
      log(logLevel, "handleImageMissing: calling handler");
      evt = new ObserveEvent("", ObserveEvent.Type.MISSING, null, img, this, 0);
      ((ObserverCallBack) imageMissingHandler).missing(evt);
      response = evt.getResponse();
    }
    if (recap || FindFailedResponse.PROMPT.equals(response)) {
      if (!recap) {
        log(logLevel, "handleImageMissing: Response.PROMPT");
      }
      response = handleFindFailedShowDialog(img, true);
    }
    if (FindFailedResponse.RETRY.equals(response)) {
      log(logLevel, "handleImageMissing: Response.RETRY: %s", (recap ? "recapture " : "capture missing "));
      Commons.pause(0.500);
      //TODO ((Region) this).getScreen().userCapture(
      ScreenImage simg = ((Region) this).getScreen().userCapture(
          (recap ? "recapture " : "capture missing ") + img.getName());
      if (simg != null) {
        String path = ImagePath.getBundlePath();
        if (path == null) {
          log(-1, "handleImageMissing: no bundle path - aborting");
          return null;
        }
        String saveImage = FileManager.saveImage(simg.getImage(), img.getName(), path);
        if (saveImage == null) {
          return null;
        }
        Image.reinit(img);
        if (img.isValid()) {
          log(logLevel, "handleImageMissing: %scaptured: %s", (recap ? "re" : ""), img);
          Image.setIDEshouldReload(img);
          return true;
        }
      }
      return null;
    } else if (findFailedResponse.ABORT.equals(response)) {
      log(-1, "handleImageMissing: Response.ABORT: aborting");
      return null;
    }
    log(logLevel, "handleImageMissing: skip requested on %s", (recap ? "recapture " : "capture missing "));
    return false;
  }

  <PSI> Boolean handleFindFailed(PSI target, Image img) {
    log(logLevel, "handleFindFailed: %s", target);
    Boolean state = null;
    ObserveEvent evt = null;
    FindFailedResponse response = findFailedResponse;
    if (FindFailedResponse.HANDLE.equals(response)) {
      ObserveEvent.Type type = ObserveEvent.Type.FINDFAILED;
      if (findFailedHandler != null && ((ObserverCallBack) findFailedHandler).getType().equals(type)) {
        log(logLevel, "handleFindFailed: Response.HANDLE: calling handler");
        evt = new ObserveEvent("", type, target, img, this, 0);
        ((ObserverCallBack) findFailedHandler).findfailed(evt);
        response = evt.getResponse();
      }
    }
    if (FindFailedResponse.ABORT.equals(response)) {
      state = null;
    } else if (FindFailedResponse.SKIP.equals(response)) {
      state = false;
    } else if (FindFailedResponse.RETRY.equals(response)) {
      state = true;
    }
    if (FindFailedResponse.PROMPT.equals(response)) {
      response = handleFindFailedShowDialog(img, false);
    } else {
      return state;
    }
    if (FindFailedResponse.ABORT.equals(response)) {
      state = null;
    } else if (FindFailedResponse.SKIP.equals(response)) {
      // TODO HACK to allow recapture on FindFailed PROMPT
      state = handleImageMissing(img, true); //hack: FindFailed-ReCapture
    } else if (FindFailedResponse.RETRY.equals(response)) {
      state = true;
    }
    return state;
  }

  FindFailedResponse handleFindFailedShowDialog(Image img, boolean shouldCapture) {
    log(logLevel, "handleFindFailedShowDialog: requested %s", (shouldCapture ? "(with capture)" : ""));
    FindFailedResponse response;
    FindFailedDialog fd = new FindFailedDialog(img, shouldCapture);
    fd.setVisible(true);
    response = fd.getResponse();
    fd.dispose();
    Commons.pause(0.5);
    log(logLevel, "handleFindFailedShowDialog: answer is %s", response);
    return response;
  }
  //</editor-fold>

  //<editor-fold desc="100 find image">

  /**
   * finds the given Pattern, String or Image in the Region/Image
   * and returns the best match.
   *
   * @param <PSI>  Pattern, String or Image
   * @param target what (PSI) to find
   * @return If found, the element. null otherwise
   * @throws FindFailed if the Find operation failed
   */
  public <PSI> Match find(PSI target) throws FindFailed {
    Commons.trace("");
    return executeFind(target, 0, 0, null, FINDTYPE.SINGLE).getMatch(); // find
  }
  //</editor-fold>

  //<editor-fold desc="101 wait for image">

  /**
   * WARNING: wait(long timeout) is taken by Java Object as final. This method catches any interruptedExceptions
   *
   * @param timeout The time to wait
   */
  public void wait(double timeout) {
    try {
      Thread.sleep((long) (timeout * 1000L));
    } catch (InterruptedException e) {
    }
  }

  /**
   * Waits for the Pattern, String or Image to appear or timeout (in second) is passed
   *
   * @param <PSI>   Pattern, String or Image
   * @param target  The target to search for
   * @param timeout Timeout in seconds (will be 0 when Image)
   * @return The found Match
   * @throws FindFailed if the Find operation finally failed
   */
  public <PSI> Match wait(PSI target, double timeout) throws FindFailed {
    Commons.trace("");
    return executeFind(target, timeout, 0, null, FINDTYPE.SINGLE).getMatch(); // wait
  }

  /**
   * Waits for the Pattern, String or Image to appear until the AutoWaitTimeout value is exceeded.
   *
   * @param <PSI>  Pattern, String or Image
   * @param target The target to search for
   * @return The found Match
   * @throws FindFailed if the Find operation finally failed
   */
  public <PSI> Match wait(PSI target) throws FindFailed {
    if (target instanceof Float || target instanceof Double) {
      wait(0.0 + ((Double) target));
      return null;
    }
    return wait(target, autoWaitTimeout);
  }
  //</editor-fold>

  //<editor-fold desc="102 exists/has image">

  /**
   * Check if target exists with a specified timeout<br>
   * timout = 0: returns immediately after first search,
   * does not raise FindFailed
   *
   * @param <PSI>   Pattern, String or Image
   * @param target  The target to search for
   * @param timeout Timeout in seconds (will be 0 when Image)
   * @return the match (null if not found or image file missing)
   */
  public <PSI> Match exists(PSI target, double timeout) {
    try {
      return wait(target, timeout);
    } catch (FindFailed e) {
      return null;
    }
  }

  /**
   * Check if target exists (with the default autoWaitTimeout (0 when Image)),
   * does not raise FindFailed
   *
   * @param <PSI>  Pattern, String or Image
   * @param target Pattern, String or Image
   * @return the match (null if not found or image file missing)
   */
  public <PSI> Match exists(PSI target) {
    return exists(target, autoWaitTimeout);
  }

  /**
   * Check if target exists<br>
   * - does not raise FindFailed
   * - like exists(target, 0) but returns true/false<br>
   * - which means only one search <br>
   * - no wait for target to appear<br>
   * - intended to be used in logical expressions<br>
   * - use getLastMatch() to get the match if found
   *
   * @param <PSI>  Pattern, String or Image
   * @param target Pattern, String or Image
   * @return true if found, false otherwise
   */
  public <PSI> boolean has(PSI target) {
    return null != exists(target, 0);
  }

  /**
   * Check if target appears within the specified time<br>
   * - does not raise FindFailed
   * - like exists(target, timeout) but returns true/false<br>
   * - intended to be used in logical expressions<br>
   * - use getLastMatch() to get the match if found
   *
   * @param <PSI>   Pattern, String or Image
   * @param target  The target to search for
   * @param timeout Timeout in seconds  (will be 0 when Image)
   * @return true if found, false otherwise
   */
  public <PSI> boolean has(PSI target, double timeout) {
    return null != exists(target, timeout);
  }
  //</editor-fold>

  //<editor-fold desc="103 findAll images">

  /**
   * Finds all occurences of the given Pattern, String or Image in the Region/Image and returns an Iterator of Matches.
   *
   * @param <PSI>  Pattern, String or Image
   * @param target A search criteria
   * @return the elements matching
   * @throws FindFailed if the Find operation failed
   */
  public <PSI> Iterator<Match> findAll(PSI target) throws FindFailed {
    Commons.trace("");
    return executeFind(target, 0, 0, null, FINDTYPE.ALL); // findAll
  }

  /**
   * Finds all occurences of the given Pattern, String or Image in the Region/Image and returns a List of Matches.
   *
   * @param <PSI>  Pattern, String or Image
   * @param target A search criteria
   * @return All elements matching (empty list when FindFailed)
   */
  public <PSI> List<Match> getAll(PSI target) {
    Commons.trace("");
    try {
      return executeFind(target, 0, 0, null, FINDTYPE.ALL).getMatches(); // getAll
    } catch (FindFailed e) {
      return new ArrayList<>();
    }
  }

  /**
   * Waits for Pattern, String or Image in the Region/Image and returns a List of Matches.
   *
   * @param <PSI>   Pattern, String or Image
   * @param target  A search criteria
   * @param timeout Timeout in seconds (will be 0 when Image)
   * @return All elements matching (empty list when FindFailed)
   */
  public <PSI> List<Match> getAll(double timeout, PSI target) {
    Commons.trace("");
    try {
      return executeFind(target, timeout, 0, null, FINDTYPE.ALL).getMatches(); // getAll wait
    } catch (FindFailed e) {
      return new ArrayList<>();
    }
  }

  /**
   * @deprecated use {@link #getAll(PSI)}
   */
  @Deprecated
  public <PSI> List<Match> findAllList(PSI target) {
    return getAll(target);
  }

  public <PSI> List<Match> findAllByRow(PSI target) {
    List<Match> mList = getAll(target);
    if (mList.isEmpty()) {
      return null;
    }
    Collections.sort(mList, (m1, m2) -> {
      int xMid1 = m1.getCenter().x;
      int yMid1 = m1.getCenter().y;
      int yTop = yMid1 - m1.h / 2;
      int yBottom = yMid1 + m1.h / 2;
      int xMid2 = m2.getCenter().x;
      int yMid2 = m2.getCenter().y;
      if (yMid2 > yTop && yMid2 < yBottom) {
        if (xMid1 > xMid2) {
          return 1;
        }
      } else if (yMid2 < yTop) {
        return 1;
      }
      return -1;
    });
    return mList;
  }

  public <PSI> List<Match> findAllByColumn(PSI target) {
    List<Match> mList = getAll(target);
    if (mList.isEmpty()) {
      return null;
    }
    Collections.sort(mList, (m1, m2) -> {
      int xMid1 = m1.getCenter().x;
      int yMid1 = m1.getCenter().y;
      int xLeft = xMid1 - m1.w / 2;
      int xRight = xMid1 + m1.w / 2;
      int xMid2 = m2.getCenter().x;
      int yMid2 = m2.getCenter().y;
      if (xMid2 > xLeft && xMid2 < xRight) {
        if (yMid1 > yMid2) {
          return 1;
        }
      } else if (xMid2 < xLeft) {
        return 1;
      }
      return -1;
    });
    return mList;
  }

  //</editor-fold>

  //<editor-fold desc="104 findBest">
  //TODO ***************** findBest
  public Match findBest(Object... args) {
    if (args.length == 0) {
      return null;
    }
    List<Object> pList = new ArrayList<>();
    if (args[0] instanceof  ArrayList) {
      pList = (ArrayList) args[0];
    } else {
      pList.addAll(Arrays.asList(args));
    }
    return getBest(0, pList);
  }

  public Match waitBest(double time, Object... args) {
    if (args.length == 0) {
      return null;
    }
    List<Object> pList = new ArrayList<>();
    if (args[0] instanceof  ArrayList) {
      pList = (ArrayList) args[0];
    } else {
      pList.addAll(Arrays.asList(args));
    }
    return getBest(time, pList);
  }

  public Match getBest(List<Object> pList) {
    return getBest(0, pList);
  }

  public Match getBest(double time, List<Object> pList) {
    List<Match> mList = getAny(time, pList);
    if (mList.size() > 1) {
      Collections.sort(mList, (m1, m2) -> {
        double ms = m2.getScore() - m1.getScore();
        if (ms < 0) {
          return -1;
        } else if (ms > 0) {
          return 1;
        }
        return 0;
      });
    }
    if (mList.size() > 0) {
      return mList.get(0);
    } else {
      return null;
    }
  }
  //</editor-fold>

  //<editor-fold desc="105 findAny">
  //TODO ***************** findAny
  public List<Match> findAny(Object... args) {
    if (args.length == 0) {
      return new ArrayList<>();
    }
    List<Object> pList = new ArrayList<>();
    if (args[0] instanceof  ArrayList) {
      pList = (ArrayList) args[0];
    } else {
      pList.addAll(Arrays.asList(args));
    }
    return getAny(0, pList); // findAny
  }

  public List<Match> waitAny(double time, Object... args) {
    if (args.length == 0) {
      return new ArrayList<>();
    }
    List<Object> pList = new ArrayList<>();
    if (args[0] instanceof  ArrayList) {
      pList = (ArrayList) args[0];
    } else {
      pList.addAll(Arrays.asList(args));
    }
    return getAny(time, pList); // waitAny
  }

  public List<Match> getAny(List<Object> pList) {
    return getAny(0, pList); // getAny
  }

  public List<Match> getAny(double time, List<Object> pList) {
    if (pList == null || pList.isEmpty()) {
      return new ArrayList<>();
    }
    Image img;
    int nTarget = -1;
    for (Object target : pList) {
      nTarget++;
      img = getImageFromTarget(target);
      if (!img.isValid()) {
        Boolean response = handleImageMissing(img, false);
        if (null == response) {
            throw new RuntimeException(String.format("SikuliX: ImageMissing: %s", target)); // abort
        } else if (!response) { // skip
          pList.set(nTarget, null);
          continue;
        }
        if (target instanceof Pattern) {
          ((Pattern) target).setImage(img);
        } else {
          target = img;
        }
        pList.set(nTarget, target);
        // image was recaptured
      }
      Commons.info("");
    }

    Match[] mArray = new Match[pList.size()];
    RepeatableFinder[] rfArray = new RepeatableFinder[pList.size()];
    SubFindRun[] theSubs = new SubFindRun[pList.size()];
    int nobj = 0;
    ScreenImage base = ((Region) this).getScreen().capture((Region) this); //TODO
    for (Object obj : pList) {
      mArray[nobj] = null;
      if (obj instanceof Pattern || obj instanceof String || obj instanceof Image) {
        theSubs[nobj] = new SubFindRun(time, mArray, rfArray, nobj, base, obj, (Region) this);
        new Thread(theSubs[nobj]).start();
      }
      nobj++;
    }
    Debug.log(logLevel, "findAnyCollect: waiting for SubFindRuns");
    if (time > 0) {
      boolean any = false;
      while (!any) {
        for (SubFindRun sub : theSubs) {
          if (sub.hasFinished()) {
            any = true;
            break;
          }
        }
      }
      for (SubFindRun sub : theSubs) {
        sub.shouldStop();
      }
    } else {
      boolean all = false;
      while (!all) {
        all = true;
        for (SubFindRun sub : theSubs) {
          all &= sub.hasFinished();
        }
      }
    }
    if (time > 0)
      Debug.log(logLevel, "waitAnyCollect: first SubFindRun finished");
    else {
      Debug.log(logLevel, "findAnyCollect: all SubFindRuns finished");
    }
    nobj = 0;
    List<Match> mList = new ArrayList<>();
    for (Match match : mArray) {
      if (match != null) {
        match.setIndex(nobj);
        mList.add(match);
      }
      nobj++;
    }
    return mList;
  }
  //</editor-fold>

  //<editor-fold desc="106 findAnyAll">
  //TODO ***************** findAnyAll
  public List<List<Match>> getAnyAll(List<Object> pList) {
    return getAnyAll(0, pList);
  }

  public List<List<Match>> getAnyAll(double time, List<Object> pList) {
    if (pList == null || pList.isEmpty()) {
      return new ArrayList<>();
    }
    Finder[] finders = new Finder[pList.size()];
    //TODO SubFindRun[] theSubs = new SubFindAll[pList.size()];
    Region.SubFindRun[] theSubs = new Region.SubFindRun[pList.size()];
    int nTarget = 0;
    ScreenImage base = ((Region) this).getScreen().capture((Region) this); //TODO
    for (Object target : pList) {
      finders[nTarget] = null;
      if (target instanceof Pattern || target instanceof String || target instanceof Image) {
        //TODO theSubs[nTarget] = new SubFindAll(finders, nTarget, target, this, base, time);
        theSubs[nTarget] = new Region.SubFindRun();
        new Thread(theSubs[nTarget]).start();
      }
      nTarget++;
    }
    Debug.log(logLevel, "findAnyCollect: waiting for SubFindRuns");
    if (time > 0) {
      boolean any = false;
      while (!any) {
        any = false;
        for (Region.SubFindRun sub : theSubs) {
          if (sub.hasFinished()) {
            any = true;
            break;
          }
        }
      }
      for (Region.SubFindRun sub : theSubs) {
        sub.shouldStop();
      }
    } else {
      boolean all = false;
      while (!all) {
        all = true;
        for (Region.SubFindRun sub : theSubs) {
          all &= sub.hasFinished();
        }
      }
    }
    if (time > 0)
      Debug.log(logLevel, "waitAnyCollect: first SubFindRun finished");
    else {
      Debug.log(logLevel, "findAnyCollect: all SubFindRuns finished");
    }
    nTarget = 0;
    List<List<Match>> mList = new ArrayList<>();
    for (Finder finder : finders) {
      if (finder != null) {
        List<Match> matches = new ArrayList<>();
        while (finder.hasNext()) {
          Match next = finder.next();
          next.setIndex(nTarget);
          matches.add(next);
        }
        if (matches.size() > 0) {
          mList.add(matches);
        }
      }
      nTarget++;
    }
    return mList;
  }
  //</editor-fold>

  //<editor-fold desc="109 find image internals">
  enum FINDTYPE {
    SINGLE, ALL, ANY, ANYALL, VANISH
  }

  <PSI> Finder executeFind(PSI target, double findTimeout, int index, Finder finder, FINDTYPE findtype) throws FindFailed {
    Commons.trace("");
    findTimeout = (this instanceof Image) ? 0 : findTimeout;
    long findTime = -1;
    long searchTime = -1;
    Image img;
    while (true) {
      img = getImageFromTarget(target);
      if (!img.isValid()) {
        Boolean response = handleImageMissing(img, false);
        if (null == response) {
          if (Settings.SwitchToText) {
            log(logLevel, "wait: image missing: switching to text search (deprecated - use text methods)");
            img.setIsText(true);
          } else {
            throw new RuntimeException(String.format("SikuliX: ImageMissing: %s", target)); // abort
          }
        } else if (!response) { // skip
          return new Finder(this);
        }
        // image was recaptured
      }
      boolean findingText = false;
      String someText = "";
      findTime = Commons.timeNow();
      searchTime = findTime;
      if (target instanceof String) {
        if (((String) target).startsWith("\t") && ((String) target).endsWith("\t")) {
          findingText = true;
          someText = ((String) target).replaceAll("\\t", "");
        } else {
          if (img.isValid()) {
            finder = runFirstFinder(finder, new Pattern().setImage(img), findtype); // String
          } else if (img.isText()) {
            findingText = true;
            someText = img.getNameGiven();
          }
        }
        if (findingText) {
          log(logLevel, "doFind: Switching to TextSearch");
          finder = new Finder(this);
          finder.findText(someText);
        }
      } else if (target instanceof Pattern) {
        if (img.isValid()) {
          finder = runFirstFinder(finder, ((Pattern) target).setImage(img), findtype); // Pattern
        }
      } else if (target instanceof Image || target instanceof ScreenImage) {
        if (img.isValid()) {
          finder = runFirstFinder(finder, new Pattern().setImage(img), findtype); // image
        }
      } else {
        throw new RuntimeException(String.format("SikuliX: find, wait, exists: invalid parameter: %s", target));
      }
      searchTime = Commons.timeSince(searchTime);
      boolean shouldRepeat = false;
      if (findtype.equals(FINDTYPE.VANISH)) {
        shouldRepeat = finder.hasNext();
        if (!shouldRepeat) {
          throw new FindFailed(String.format("in %s with %s", this, getImageFromTarget(target)));
        }
      } else {
        shouldRepeat = !finder.hasNext();
      }
      if (!(this instanceof Image) && !findingText && shouldRepeat && findTimeout > 0) {
        RepeatableFinder rf = new RepeatableFinder(finder);
        rf.setFindType(findtype);
        rf.repeat(findTimeout);
        searchTime = rf.getSearchTime();
      }
      findTime = Commons.timeSince(findTime);
      if (findtype.equals(FINDTYPE.VANISH)) {
        break;
      }
      boolean findFailed;
      Boolean handleFindFailedState;
      if (!finder.hasNext()) {
        findFailed = true;
        handleFindFailedState = handleFindFailed(target, img); //TODO for Image
        if (FindFailed.isAbort(handleFindFailedState)) {
          throw new FindFailed(String.format("in %s with %s", this, getImageFromTarget(target)));
        }
      } else {
        break;
      }
      if (findFailed && FindFailed.isSkip(handleFindFailedState)) {
        break;
      }
      finder = null;
    }
    finder.setTimes(findTime, searchTime);
    finder.setIndex(index);
    if (this instanceof Image) {
      finder.setWhereImage((Image) this);
    }
    finder.setFindType(findtype);
    return finder;
  }

  private Finder runFirstFinder(Finder finder, Pattern target, FINDTYPE type) {
    Commons.trace("");
    if (Debug.shouldHighlight()) { //TODO
//      if (getScreen().getW() > w + 20 && getScreen().getH() > h + 20) {
//        highlight(2, "#000255000");
//      }
    }
    if (null != finder) {
      return finder;
    }
    if (type.equals(FINDTYPE.SINGLE) || type.equals(FINDTYPE.VANISH)) {
      finder = checkLastSeenAndCreateFinder(target);
    } else {
      finder = new Finder(this);
    }
    if (!finder.hasNext()) {
      finder.findAll(target);
    }
    return finder;
  }

  private Finder checkLastSeenAndCreateFinder(Pattern ptn) {
    Commons.trace("");
    if (this instanceof Image) {
      return new Finder(this);
    }
    boolean shouldCheckLastSeen = false;
    double score = 0;
    Image img = ptn.getImage();
    if (Settings.CheckLastSeen) {
      if (null != img.getLastSeen() && ptn.getResize() == 0) {
        score = img.getLastSeenScore() - 0.01;
        if (!(ptn.getSimilar() > score)) {
          shouldCheckLastSeen = true;
        }
      }
    }
    if (shouldCheckLastSeen) {
      Region r = Region.create(img.getLastSeen());
      if (((Region) this).contains(r)) {
        Finder lastSeenFinder = new Finder(r);
        if (Debug.shouldHighlight()) { //TODO
//          if (getScreen().getW() > w + 10 && getScreen().getH() > h + 10) {
//            highlight(2, "#000255000");
//          }
        }
        lastSeenFinder.find(ptn.similar(score));
        if (lastSeenFinder.hasNext()) {
          log(logLevel, "checkLastSeen: still there");
          return lastSeenFinder;
        }
        log(logLevel, "checkLastSeen: not there");
      }
    }
    return new Finder(this);
  }

  abstract class Repeatable {
    volatile AtomicBoolean shouldStop = new AtomicBoolean();

    public void setShouldStop() {
      shouldStop.set(true);
    }

    public void resetShouldStop() {
      shouldStop.set(false);
    }

    private double findTimeout;

    abstract void run();

    abstract boolean isSuccessful();

    double getFindTimeOut() {
      return findTimeout;
    }

    Element.FINDTYPE _findType = FINDTYPE.SINGLE;

    void setFindType(Element.FINDTYPE findType) {
      _findType = findType;
    }

    long searchTime = -1;
    long findTime = -1;

    public long getSearchTime() {
      return searchTime;
    }

    public long getFindTime() {
      return findTime;
    }

    boolean repeat(double timeout) {
      findTimeout = timeout;
      int MaxTimePerScan = (int) (1000.0 / waitScanRate);
      int timeoutMilli = (int) (timeout * 1000);
      long begin_t = Commons.timeNow();
      boolean success = false;
      do {
        long before_find = Commons.timeNow();
        run();
        findTime = Commons.timeSince(before_find);
        if (isSuccessful()) {
          success = true;
          break;
        } else if (timeoutMilli < MaxTimePerScan) {
          break;
        }
        if (null != shouldStop && shouldStop.get()) {
          break;
        }
        long after_find = Commons.timeNow();
        if (after_find - before_find < MaxTimePerScan) {
          try {
            Thread.sleep(MaxTimePerScan - (after_find - before_find));
          } catch (InterruptedException e) {
            break;
          }
        } else {
          try {
            Thread.sleep(10);
          } catch (InterruptedException e) {
            break;
          }
        }
      } while (begin_t + timeout * 1000 > Commons.timeNow());
      return success;
    }
  }

  class RepeatableFinder extends Repeatable {
    Finder _finder;
    List<Match> _matches = new ArrayList<>();

    public <PSI> RepeatableFinder(Finder finder) {
      _finder = finder;
      _finder.setRepeating();
    }

    @Override
    public void run() {
      _finder.newShot();
      long start = Commons.timeNow();
      _finder.findAllRepeat();
      searchTime = Commons.timeSince(start);
    }

    @Override
    boolean isSuccessful() {
      if (_findType.equals(FINDTYPE.VANISH)) {
        return !_finder.hasNext();
      }
      return _finder.hasNext();
    }
  }

  class SubFindRun implements Runnable {

    Match[] matches;
    Repeatable[] repeatables;
    ScreenImage base;
    Object target;
    Region reg;
    double waitTime;
    boolean finished = false;
    int subN;

    SubFindRun() {
    }

    ;

    public SubFindRun(double pTime, Match[] pMArray, RepeatableFinder[] pRepeatables, int pSubN,
                      ScreenImage pBase, Object pTarget, Region pReg) {
      subN = pSubN;
      base = pBase;
      target = pTarget;
      reg = pReg;
      matches = pMArray;
      repeatables = pRepeatables;
      waitTime = pTime;
    }

    @Override
    public void run() {
      if (waitTime > 0) {
        //TODO SubFindRun(double pTime, ...
//        repeatables[subN] = reg.existsStoppableInit(target);
//        matches[subN] = reg.doExists((RepeatableFind) repeatables[subN], waitTime);
      } else {
        try {
          matches[subN] = null;  //TODO reg.findInImage(base, target);
        } catch (Exception ex) {
          log(-1, "findAnyCollect: image file not found:\n", target);
        }
      }
      hasFinished(true);
    }

    public boolean hasFinished() {
      return hasFinished(false);
    }

    public synchronized boolean hasFinished(boolean state) {
      if (state) {
        finished = true;
      }
      return finished;
    }

    public void shouldStop() {
      if (!hasFinished()) {
        repeatables[subN].setShouldStop();
      }
    }
  }

  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="12 OCR - read text, line, word, char">

  /**
   * tries to read the text in this region/image<br>
   * might contain misread characters, NL characters and
   * other stuff, when interpreting contained grafics as text<br>
   * Best results: one or more lines of text with no contained grafics
   *
   * @return the text read (utf8 encoded)
   */
  public String text() {
    return OCR.readText(this);
  }

  /**
   * get text from this region/image
   * supposing it is one line of text
   *
   * @return the text or empty string
   */
  public String textLine() {
    return OCR.readLine(this);
  }

  /**
   * get text from this region/image
   * supposing it is one word
   *
   * @return the text or empty string
   */
  public String textWord() {
    return OCR.readWord(this);
  }

  /**
   * get text from this region/image
   * supposing it is one character
   *
   * @return the text or empty string
   */
  public String textChar() {
    return OCR.readChar(this);
  }

  /**
   * find text lines in this region/image
   *
   * @return list of strings each representing one line of text
   */
  public List<String> textLines() {
    List<String> lines = new ArrayList<>();
    List<Match> matches = findLines();
    for (Match match : matches) {
      lines.add(match.getText());
    }
    return lines;
  }

  /**
   * find the words as text in this region/image (top left to bottom right)<br>
   * a word is a sequence of detected utf8-characters surrounded by significant background space
   * might contain characters misinterpreted from contained grafics
   *
   * @return list of strings each representing one word
   */
  public List<String> textWords() {
    List<String> words = new ArrayList<>();
    List<Match> matches = findWords();
    for (Match match : matches) {
      words.add(match.getText());
    }
    return words;
  }

  /**
   * Find all lines as text (top left to bottom right) in this {@link Region} or {@link Image}
   *
   * @return a list of text {@link Match}es or empty list if not found
   */
  public List<Match> findLines() {
    return relocate(OCR.readLines(this));
  }

  /**
   * Find all words as text (top left to bottom right)
   *
   * @return a list of text matches
   */
  public List<Match> findWords() {
    return relocate(OCR.readWords(this));
  }
  //</editor-fold>

  //<editor-fold desc="15 find text (word, line)">

  /**
   * Find the first word as text (top left to bottom right) containing the given text
   *
   * @param word to be searched
   * @return a text match or null if not found
   */
  public Match findWord(String word) {
    Match match = null;
    if (!word.isEmpty()) {
      Object result = doFindText(word, levelWord, false);
      if (result != null) {
        match = relocate((Match) result);
      }
    }
    return match;
  }

  /**
   * Find all words as text (top left to bottom right) containing the given text
   *
   * @param word to be searched
   * @return a list of text matches
   */
  public List<Match> findWords(String word) {
    Finder finder = ((Finder) doFindText(word, levelWord, true));
    if (null != finder) {
      return finder.getListFor(this);
    }
    return new ArrayList<>();
  }

  /**
   * Find the first line as text (top left to bottom right) containing the given text
   *
   * @param text the line should contain
   * @return a text match or null if not found
   */
  public Match findLine(String text) {
    Match match = null;
    if (!text.isEmpty()) {
      Object result = doFindText(text, levelLine, false);
      if (result != null) {
        match = relocate((Match) result);
      }
    }
    return match;
  }

  /**
   * Find all lines as text (top left to bottom right) containing the given text
   *
   * @param text the lines should contain
   * @return a list of text matches or empty list if not found
   */
  public List<Match> findLines(String text) {
    Finder finder = (Finder) doFindText(text, levelLine, true);
    if (null != finder) {
      return finder.getListFor(this);
    }
    return new ArrayList<>();
  }

  private int levelWord = 3;
  private int levelLine = 2;

  private Object doFindText(String text, int level, boolean multi) {
    Object returnValue = null;
    Finder finder = new Finder(this);
    lastSearchTime = (new Date()).getTime();
    if (level == levelWord) {
      if (multi) {
        if (finder.findWords(text)) {
          returnValue = finder;
        }
      } else {
        if (finder.findWord(text)) {
          returnValue = finder.next();
        }
      }
    } else if (level == levelLine) {
      if (multi) {
        if (finder.findLines(text)) {
          returnValue = finder;
        }
      } else {
        if (finder.findLine(text)) {
          returnValue = finder.next();
        }
      }
    }
    return returnValue;
  }
  //</editor-fold>

  //<editor-fold desc="17 find text like find image">
  public Match findText(String text) throws FindFailed {
    //TODO implement findText
    throw new SikuliXception(String.format("Pixels: findText: not implemented for", this.getClass().getCanonicalName()));
    //return match;
  }

  public Match findT(String text) throws FindFailed {
    return findText(text);
  }

  public Match existsText(String text) {
    //TODO existsText: try: findText:true catch: false
    throw new SikuliXception(String.format("Pixels: existsText: not implemented for", this.getClass().getCanonicalName()));
    //return match;
  }

  public Match existsT(String text) {
    return existsText(text);
  }

  public boolean hasText(String text) {
    return null != existsText(text);
  }

  public boolean hasT(String text) {
    return hasText(text);
  }

  public List<Match> findAllText(String text) {
    List<Match> matches = new ArrayList<>();
    throw new SikuliXception(String.format("Pixels: findAllText: not implemented for", this.getClass().getCanonicalName()));
    //return matches;
  }

  public List<Match> findAllT(String text) {
    return findAllText(text);
  }
  //</editor-fold>

  //<editor-fold desc="20 helper">

  /**
   * INTERNAL: get Image from target
   *
   * @param <PSI>  Pattern, Filename, Image, ScreenImage
   * @param target what(PSI) to search
   * @return Image object
   */
  public static <PSI> Image getImageFromTarget(PSI target) {
    Commons.trace("");
    if (target instanceof Pattern) {
      return ((Pattern) target).getImage();
    } else if (target instanceof String) {
      Image img = Image.create((String) target);
      return img;
    } else if (target instanceof Image) {
      return (Image) target;
    } else if (target instanceof ScreenImage) {
      return new Image(((ScreenImage) target).getImage());
    } else {
      throw new IllegalArgumentException(String.format("SikuliX: find, wait, exists: invalid parameter: %s", target));
    }
  }

  protected static <SFIRBS> BufferedImage getBufferedImage(SFIRBS whatEver) {
    if (whatEver instanceof String) {
      return Image.create((String) whatEver).get();
    } else if (whatEver instanceof File) {
      return Image.create((File) whatEver).get();
    } else if (whatEver instanceof Match) {
      Region theRegion = new Region((Match) whatEver);
      return theRegion.getImage().get();
    } else if (whatEver instanceof Region) {
      return ((Region) whatEver).getImage().get();
    } else if (whatEver instanceof Image) {
      return ((Image) whatEver).get();
    } else if (whatEver instanceof ScreenImage) {
      return ((ScreenImage) whatEver).getImage();
    } else if (whatEver instanceof BufferedImage) {
      return (BufferedImage) whatEver;
    }
    throw new IllegalArgumentException(String.format("Illegal OCR source: %s", whatEver != null ? whatEver.getClass() : "null"));
  }

  protected Image getImage() {
    throw new SikuliXception(String.format("Pixels: getImage: not implemented for", this.getClass().getCanonicalName()));
  }

  protected List<Match> relocate(List<Match> matches) {
    return matches;
  }

  protected Match relocate(Match match) {
    return match;
  }
  //</editor-fold>

  //<editor-fold desc="25 lastMatch">
  /**
   * The last found {@link Match} in the Region
   */
  protected Match lastMatch = null;

  /**
   * The last found {@link Match}es in the Region
   */
  protected Iterator<Match> lastMatches = null;
  protected long lastSearchTime = -1;
  protected long lastFindTime = -1;
  protected long lastSearchTimeRepeat = -1;

  /**
   * a find operation saves its match on success in this region/image.
   * <br>... unchanged if not successful
   *
   * @return the Match object from last successful find
   */
  public Match getLastMatch() {
    return lastMatch;
  }

  // ************************************************

  /**
   * a searchAll operation saves its matches on success in this region/image
   * <br>... unchanged if not successful
   *
   * @return a Match-Iterator of matches from last successful searchAll
   */
  public Iterator<Match> getLastMatches() {
    return lastMatches;
  }
  //</editor-fold>

  //<editor-fold desc="99 obsolete">

  /**
   * @return a list of matches
   * @see #findLines()
   * @deprecated use findLines() instead
   */
  public List<Match> collectLines() {
    return findLines();
  }

  /**
   * @return a list of lines as strings
   * @see #textLines()
   * @deprecated use textLines() instead
   */
  @Deprecated
  public List<String> collectLinesText() {
    return textLines();
  }

  /**
   * @return a list of matches
   * @see #findWords()
   * @deprecated use findWords() instead
   */
  @Deprecated
  public List<Match> collectWords() {
    return findWords();
  }

  /**
   * @return a list of words sa strings
   * @see #textWords()
   * @deprecated use textWords() instead
   */
  @Deprecated
  public List<String> collectWordsText() {
    return textWords();
  }
  //</editor-fold>


}
