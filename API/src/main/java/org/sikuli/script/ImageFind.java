/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.sikuli.basics.Debug;
import org.sikuli.basics.Settings;

/**
 * EXPERIMENTAL --- INTERNAL USE ONLY<br>
 *   is not official API --- will not be in version 2
 */
public class ImageFind implements Iterator<Match>{

  private static String me = "ImageFind: ";
  private static int lvl = 3;

  private static void log(int level, String message, Object... args) {
    Debug.logx(level, me + message, args);
  }

  private ImageFinder owner = null;

  private boolean isValid = false;
  private boolean isInnerFind = false;

  private Image pImage = null;
  private Mat probe = new Mat();
  private boolean isPlainColor = false;
  private boolean isBlack = false;
  private double similarity = Settings.MinSimilarity;
  private double waitingTime = Settings.AutoWaitTimeout;
  private boolean shouldCheckLastSeen = Settings.CheckLastSeen;
  private Object[] findArgs = null;

  private int resizeMinDownSample = 12;
  private double resizeFactor;
  private float[] resizeLevels = new float[] {1f, 0.4f};
  private int resizeMaxLevel = resizeLevels.length - 1;
  private double resizeMinSim = 0.9;
  private double resizeMinFactor = 1.5;
  private Core.MinMaxLocResult findDownRes = null;

  private int sorted;
  public static final int AS_ROWS = 0;
  public static final int AS_COLUMNS = 1;
  public static final int BEST_FIRST = 2;

  private int finding = -1;
  public static final int FINDING_ANY = 0;
  public static final int FINDING_SOME = 1;
  public static final int FINDING_ALL = 2;

  private int count = 0;
  public static int SOME_COUNT = 5;

  public static int ALL_MAX = 100;
  private int allMax = 0;

  private List<Match> matches = Collections.synchronizedList(new ArrayList<Match>());

  private boolean repeating;
  private long lastFindTime = 0;
  private long lastSearchTime = 0;

  public ImageFind() {
    matches.add(null);
  }

  public boolean isValid() {
    return true;
  }

  public void setIsInnerFind() {
    isInnerFind = true;
  }

  void setSimilarity(double sim) {
    similarity = sim;
  }

  public void setFindTimeout(double t) {
    waitingTime = t;
  }

  public void setFinding(int ftyp) {
    finding = ftyp;
  }

  public void setSorted(int styp) {
    sorted = styp;
  }

  public void setCount(int c) {
    count = c;
  }

  public List<Match> getMatches() {
    return matches;
  }

  protected boolean checkFind(ImageFinder owner, Object pprobe, Object... args) {
    if (owner.isValid()) {
      this.owner = owner;
    } else {
      return false;
    }
    isValid = false;
    shouldCheckLastSeen = Settings.CheckLastSeen;
    if (pprobe instanceof String) {
      pImage = Image.create((String) pprobe);
      if (pImage.isValid()) {
        isValid = true;
      }
    } else if (pprobe instanceof Image) {
      if (((Image) pprobe).isValid()) {
        isValid = true;
        pImage = (Image) pprobe;
      }
    } else if (pprobe instanceof Pattern) {
      if (((Pattern) pprobe).getImage().isValid()) {
        isValid = true;
        pImage = ((Pattern) pprobe).getImage();
        similarity = ((Pattern) pprobe).getSimilar();
      }
    } else if (pprobe instanceof Mat) {
      isValid = true;
      probe = (Mat) pprobe;
      waitingTime = 0.0;
      shouldCheckLastSeen = false;
    } else {
      log(-1, "find(... some, any, all): probe invalid (not Pattern, String nor valid Image)");
      return false;
    }
    if (probe.empty()) {
      probe = Image.createMat(pImage.get());
    }
    checkProbe();
    if (!owner.isImage()) {
      if (args.length > 0) {
        if (args[0] instanceof Integer) {
          waitingTime = 0.0 + (Integer) args[0];
        } else if (args[0] instanceof Double) {
          waitingTime = (Double) args[0];
        }
      }
      if (args.length > 1) {
        findArgs = Arrays.copyOfRange(args, 1, args.length);
      } else {
        findArgs = null;
      }
    }
    return isValid;
  }

  private void checkProbe() {
    MatOfDouble pMean = new MatOfDouble();
    MatOfDouble pStdDev = new MatOfDouble();
    Core.meanStdDev(probe, pMean, pStdDev);
    double min = 0.00001;
    isPlainColor = false;
    double sum = 0.0;
    double arr[] = pStdDev.toArray();
    for (int i = 0; i < arr.length; i++) {
      sum += arr[i];
    }
    if (sum < min) {
      isPlainColor = true;
    }
    sum = 0.0;
    arr = pMean.toArray();
    for (int i = 0; i < arr.length; i++) {
      sum += arr[i];
    }
    if (sum < min && isPlainColor) {
      isBlack = true;
    }
    resizeFactor = Math.min(((double) probe.width())/resizeMinDownSample, ((double) probe.height())/resizeMinDownSample);
    resizeFactor = Math.max(1.0, resizeFactor);
  }

  protected ImageFind doFind() {
    Debug.enter(me + ": doFind");
    Core.MinMaxLocResult fres = null;
    repeating = false;
    long begin = (new Date()).getTime();
    long lap;
    while (true) {
      lastFindTime = (new Date()).getTime();
      if (shouldCheckLastSeen && !repeating && !owner.isImage && pImage.getLastSeen() != null) {
        log(3, "checkLastSeen: trying ...");
        ImageFinder f = new ImageFinder(new Region(pImage.getLastSeen()));
        if (null != f.findInner(probe, pImage.getLastSeenScore() - 0.01)) {
          log(lvl, "checkLastSeen: success");
          set(f.next());
          if (pImage != null) {
            pImage.setLastSeen(get().getRect(), get().getScore());
          }
          break;
        }
        log(lvl, "checkLastSeen: not found");
      }
      if (!owner.isMultiFinder || owner.base.empty()) {
        if (owner.isRegion) {
          owner.setBase(owner.region.getScreen().capture(owner.region).getImage());
        } else if (owner.isScreen) {
          owner.setBase(owner.screen.capture().getImage());
        }
      }
      if (!isInnerFind && resizeFactor > resizeMinFactor) {
        log(3, "downsampling: trying ...");
        doFindDown(0, resizeFactor);
        fres = findDownRes;
      }
      if (fres == null) {
        if (!isInnerFind) {
          log(3, "downsampling: not found with (%f) - trying original size", resizeFactor);
        }
        fres = doFindDown(0, 0.0);
        if(fres != null && fres.maxVal > similarity - 0.01) {
          set(new Match((int) fres.maxLoc.x + owner.offX, (int) fres.maxLoc.y + owner.offY,
                  probe.width(), probe.height(), fres.maxVal, null, null));
        }
      } else {
        log(lvl, "downsampling: success: adjusting match");
        set(checkFound(fres));
      }
      lastFindTime = (new Date()).getTime() - lastFindTime;
      if (hasNext()) {
        get().setTimes(lastFindTime, lastSearchTime);
        if (pImage != null) {
          pImage.setLastSeen(get().getRect(), get().getScore());
        }
        break;
      } else {
        if (isInnerFind || owner.isImage()) {
          break;
        }
        else {
          if (waitingTime < 0.001 || (lap = (new Date()).getTime() - begin) > waitingTime * 1000) {
            break;
          }
          if (owner.MaxTimePerScan > lap) {
            try {
              Thread.sleep(owner.MaxTimePerScan - lap);
            } catch (Exception ex) {
            }
          }
          repeating = true;
        }
      }
    }
    return this;
  }

  private Match checkFound(Core.MinMaxLocResult res) {
    Match match = null;
    ImageFinder f;
    Rect r = null;
    if (owner.isImage()) {
      int off = ((int) resizeFactor) + 1;
      r = getSubMatRect(owner.base, (int) res.maxLoc.x, (int) res.maxLoc.y,
                            probe.width(), probe.height(), off);
      f = new ImageFinder(owner.base.submat(r));
    } else {
      f = new ImageFinder((new Region((int) res.maxLoc.x + owner.offX, (int) res.maxLoc.y + owner.offY,
                            probe.width(), probe.height())).grow(((int) resizeFactor) + 1));
    }
    if (null != f.findInner(probe, similarity)) {
      log(lvl, "check after downsampling: success");
      match = f.next();
      if (owner.isImage()) {
        match.x += r.x;
        match.y += r.y;
      }
    }
    return match;
  }

  private static Rect getSubMatRect(Mat mat, int x, int y, int w, int h, int margin) {
    x = Math.max(0, x - margin);
    y = Math.max(0, y - margin);
    w = Math.min(w + 2 * margin, mat.width() - x);
    h = Math.min(h + 2 * margin, mat.height()- y);
    return new Rect(x, y, w, h);
  }

  private Core.MinMaxLocResult doFindDown(int level, double factor) {
    Debug.enter(me + ": doFindDown (%d - 1/%.2f)", level, factor * resizeLevels[level]);
    Debug timer = Debug.startTimer("doFindDown");
    Mat b = new Mat();
    Mat p = new Mat();
    Core.MinMaxLocResult dres = null;
    double rfactor;
    if (factor > 0.0) {
      rfactor = factor * resizeLevels[level];
      if (rfactor < resizeMinFactor) return null;
      Size sb = new Size(owner.base.cols()/rfactor, owner.base.rows()/factor);
      Size sp = new Size(probe.cols()/rfactor, probe.rows()/factor);
      Imgproc.resize(owner.base, b, sb, 0, 0, Imgproc.INTER_AREA);
      Imgproc.resize(probe, p, sp, 0, 0, Imgproc.INTER_AREA);
      dres = doFindMatch(b, p);
      log(lvl, "doFindDown: score: %.2f at (%d, %d)", dres.maxVal,
              (int) (dres.maxLoc.x * rfactor), (int) (dres.maxLoc.y * rfactor));
    } else {
      dres = doFindMatch(owner.base, probe);
      timer.end();
      return dres;
    }
    if (dres.maxVal < resizeMinSim) {
      if (level == resizeMaxLevel) {
        timer.end();
        return null;
      }
      if (level == 0) {
        findDownRes = null;
      }
      level++;
      doFindDown(level, factor);
    } else {
        dres.maxLoc.x *= rfactor;
        dres.maxLoc.y *= rfactor;
        findDownRes = dres;
    }
    timer.end();
    return null;
  }

  private Core.MinMaxLocResult doFindMatch(Mat base, Mat probe) {
    Mat res = new Mat();
    Mat bi = new Mat();
    Mat pi = new Mat();
    if (!isPlainColor) {
      Imgproc.matchTemplate(base, probe, res, Imgproc.TM_CCOEFF_NORMED);
    } else {
      if (isBlack) {
        Core.bitwise_not(base, bi);
        Core.bitwise_not(probe, pi);
      } else {
        bi = base;
        pi = probe;
      }
      Imgproc.matchTemplate(bi, pi, res, Imgproc.TM_SQDIFF_NORMED);
      Core.subtract(Mat.ones(res.size(), CvType.CV_32F), res, res);
    }
    return Core.minMaxLoc(res);
  }

  @Override
  public boolean hasNext() {
    if (matches.size() > 0) {
      return matches.get(0) != null;
    }
    return false;
  }

  @Override
  public Match next() {
    Match m = null;
    if (matches.size() > 0) {
      m = matches.get(0);
      remove();
    }
    return m;
  }

  @Override
  public void remove() {
    if (matches.size() > 0) {
      matches.remove(0);
    }
  }

  public Match get() {
    return get(0);
  }

  public Match get(int n) {
    if (n < matches.size()) {
      return matches.get(n);
    }
    return null;
  }

  private Match add(Match m) {
    if (matches.add(m)) {
      return m;
    }
    return null;
  }

  private Match set(Match m) {
    if (matches.size() > 0) {
      matches.set(0, m);
    } else {
      matches.add(m);
    }
    return m;
  }

  public int getSize() {
    return matches.size();
  }
}
