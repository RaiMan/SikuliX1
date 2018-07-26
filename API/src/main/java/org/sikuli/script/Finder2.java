/*
 * Copyright (c) 2017 - sikulix.com - MIT license
 */

package org.sikuli.script;

import net.sourceforge.tess4j.Tesseract1;
import net.sourceforge.tess4j.Word;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;
import org.sikuli.basics.Debug;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

public class Finder2 {

  //<editor-fold desc="housekeeping">
  static class Log {
    private static String prefix = "UnKnown";

    public Log(String prefix) {
      this.prefix = prefix + ": ";
    }

    public static void error(String msg, Object... args) {
      Debug.log(-1, prefix + msg, args);
    }

    public static void trace(String msg, Object... args) {
      Debug.log(3, prefix + msg, args);
    }
  }

  private static Log log = new Log("Finder2");

  private Mat mBase = Finder.getNewMat();
  private Mat mResult = Finder.getNewMat();

  private enum FindType {
    ONE, ALL
  }

  private Finder2() {
  }

  public boolean isValid() {
    return !mBase.empty();
  }
  //</editor-fold>

  public static FindResult2 find(FindInput2 findInput) {
    findInput.setAttributes();
    Finder2 finder2 = new Finder2();
    FindResult2 results = finder2.doFind(findInput);
    return results;
  }

  //<editor-fold desc="find basic">
  private final double resizeMinFactor = 1.5;
  private final double[] resizeLevels = new double[]{1f, 0.4f};
  private int resizeMaxLevel = resizeLevels.length - 1;
  private double resizeMinSim = 0.8;
  private boolean isCheckLastSeen = false;
  private static final double downSimDiff = 0.15;

  private FindResult2 doFind(FindInput2 findInput) {
    if (!findInput.isValid()) {
      return null;
    }
    FindResult2 findResult = null;
    if (findInput.isText()) {
      Region where = findInput.getWhere();
      String text = findInput.getTargetText().trim();
      if (!text.isEmpty()) {
        String[] strings = text.split("\\s");
        if (strings.length > 0) {
          text = strings[0];
        }
        if (strings.length > 1) {
          log.error("doFindText: phrase not implemented, taking first word: %s", text);
        }
      }
      BufferedImage bimg = where.getScreen().capture(where).getImage();
      TextRecognizer tr = TextRecognizer.start();
      if (tr.isValid()) {
        Tesseract1 tapi = tr.getAPI();
        long timer = new Date().getTime();
        List<Word> wordsFound = tapi.getWords(tr.resize(bimg), 3);
        timer = new Date().getTime() - timer;
        List<Word> wordsMatch = new ArrayList<>();
        for (Word word : wordsFound) {
          String text1 = word.getText();
          if (!word.getText().equals(text)) {
            continue;
          }
          Rectangle trueRectangel = tr.relocateAsRectangle(word.getBoundingBox(), where);
          Word found = new Word(word.getText(), word.getConfidence(), trueRectangel);
          wordsMatch.add(found);
        }
        if (wordsMatch.size() > 0) {
          log.trace("doFindText: %s found: %d times (%d msec) ", text, wordsMatch.size(), timer);
          findResult = new FindResult2(wordsMatch, findInput);
        } else {
          log.trace("doFindText: %s (%d msec): not found", text, timer);
        }
      }
    } else {
      log.trace("doFind: start");
      mBase = findInput.getBase();
      boolean success = false;
      long begin_t = 0;
      Core.MinMaxLocResult mMinMax = null;

      double rfactor = 0;
      boolean downSizeFound = false;
      double downSizeScore = 0;
      double downSizeWantedScore = 0;
      Mat findWhere = Finder.getNewMat();
      Mat findWhat = Finder.getNewMat();

      if (!findInput.isFindAll() && findInput.getResizeFactor() > resizeMinFactor) {
        // ************************************************* search in downsized
        begin_t = new Date().getTime();
        double imgFactor = findInput.getResizeFactor();
        Size sizeBase, sizePattern;
        mResult = null;
        for (double factor : resizeLevels) {
          rfactor = factor * imgFactor;
          sizeBase = new Size(this.mBase.cols() / rfactor, this.mBase.rows() / rfactor);
          sizePattern = new Size(findInput.getTarget().cols() / rfactor, findInput.getTarget().rows() / rfactor);
          Imgproc.resize(this.mBase, findWhere, sizeBase, 0, 0, Imgproc.INTER_AREA);
          Imgproc.resize(findInput.getTarget(), findWhat, sizePattern, 0, 0, Imgproc.INTER_AREA);
          mResult = doFindMatch(findWhat, findWhere, findInput);
          mMinMax = Core.minMaxLoc(mResult);
          downSizeWantedScore = ((int) ((findInput.getScore() - downSimDiff) * 100)) / 100.0;
          downSizeScore = mMinMax.maxVal;
          if (downSizeScore > downSizeWantedScore) {
            downSizeFound = true;
            break;
          }
        }
        log.trace("downSizeFound: %s", downSizeFound);
        log.trace("doFind: down: %%%.2f %d msec", 100 * mMinMax.maxVal, new Date().getTime() - begin_t);
      }
      if (!findInput.isFindAll() && downSizeFound) {
        findWhere = this.mBase;
        // ************************************* check after downsized success
        if (findWhere.size().equals(findInput.getTarget().size())) {
          // trust downsized mResult, if images have same size
          return new FindResult2(mResult, findInput);
        } else {
          int maxLocX = (int) (mMinMax.maxLoc.x * rfactor);
          int maxLocY = (int) (mMinMax.maxLoc.y * rfactor);
          begin_t = new Date().getTime();
          int margin = ((int) findInput.getResizeFactor()) + 1;
          Rectangle rSub = new Rectangle(Math.max(0, maxLocX - margin), Math.max(0, maxLocY - margin),
                  Math.min(findInput.getTarget().width() + 2 * margin, findWhere.width()),
                  Math.min(findInput.getTarget().height() + 2 * margin, findWhere.height()));
          Rectangle rWhere = new Rectangle(0, 0, findWhere.cols(), findWhere.rows());
          Rectangle rSubNew = rWhere.intersection(rSub);
          Rect rectSub = new Rect(rSubNew.x, rSubNew.y, rSubNew.width, rSubNew.height);
          mResult = doFindMatch(findInput.getTarget(), findWhere.submat(rectSub), findInput);
          mMinMax = Core.minMaxLoc(mResult);
          double maxVal = mMinMax.maxVal;
          double wantedScore = findInput.getScore();
          if (maxVal > wantedScore) {
            findResult = new FindResult2(mResult, findInput, new int[]{rectSub.x, rectSub.y});
          }
          if (Do.SX.isNotNull(findResult)) {
            log.trace("doFind: after down: %%%.2f(?%%%.2f) %d msec",
                    maxVal * 100, wantedScore * 100, new Date().getTime() - begin_t);
          }
        }
      }
      // ************************************** search in original
      if (((int) (100 * downSizeScore)) == 0) {
        begin_t = new Date().getTime();
        mResult = doFindMatch(findInput.getTarget(), findWhere, findInput);
        mMinMax = Core.minMaxLoc(mResult);
        if (!isCheckLastSeen) {
          log.trace("doFind: search in original: %%%.2f(?%%%.2f) %d msec",
                  mMinMax.maxVal * 100, findInput.getScore() * 100, new Date().getTime() - begin_t);
        }
        if (mMinMax.maxVal > findInput.getScore()) {
          findResult = new FindResult2(mResult, findInput);
        }
      }
      log.trace("doFind: end");
    }
    return findResult;
  }

  private Mat doFindMatch(Mat what, Mat where, FindInput2 findInput) {
    Mat mResult = Finder.getNewMat();
    if (!findInput.isPlainColor()) {
      Imgproc.matchTemplate(where, what, mResult, Imgproc.TM_CCOEFF_NORMED);
    } else {
      Mat wherePlain = where;
      Mat whatPlain = what;
      if (findInput.isBlack()) {
        Core.bitwise_not(where, wherePlain);
        Core.bitwise_not(what, whatPlain);
      }
      Imgproc.matchTemplate(wherePlain, whatPlain, mResult, Imgproc.TM_SQDIFF_NORMED);
      Core.subtract(Mat.ones(mResult.size(), CvType.CV_32F), mResult, mResult);
    }
    return mResult;
  }
  //</editor-fold>

  //<editor-fold desc="detect changes">
  private static int toGray = Imgproc.COLOR_BGR2GRAY;
  private static int toColor = Imgproc.COLOR_GRAY2BGR;
  private static int gray = CvType.CV_8UC1;
  private static int colored = CvType.CV_8UC3;
  private static int transparent = CvType.CV_8UC4;

  private static boolean isGray(Mat mat) {
    return mat.type() == gray;
  }

  private static boolean isColored(Mat mat) {
    return mat.type() == colored || mat.type() == transparent;
  }

  public static List<Region> findChanges(FindInput2 findInput) {
    findInput.setAttributes();
    int PIXEL_DIFF_THRESHOLD = 3;
    int IMAGE_DIFF_THRESHOLD = 5;
    Mat previousGray = Finder.getNewMat();
    Mat nextGray = Finder.getNewMat();
    Mat mDiffAbs = Finder.getNewMat();
    Mat mDiffTresh = Finder.getNewMat();

    Imgproc.cvtColor(findInput.getBase(), previousGray, toGray);
    Imgproc.cvtColor(findInput.getTarget(), nextGray, toGray);
    Core.absdiff(previousGray, nextGray, mDiffAbs);
    Imgproc.threshold(mDiffAbs, mDiffTresh, PIXEL_DIFF_THRESHOLD, 0.0, Imgproc.THRESH_TOZERO);

    List<Region> rectangles = new ArrayList<>();
    if (Core.countNonZero(mDiffTresh) > IMAGE_DIFF_THRESHOLD) {
      Imgproc.threshold(mDiffAbs, mDiffAbs, PIXEL_DIFF_THRESHOLD, 255, Imgproc.THRESH_BINARY);
      Imgproc.dilate(mDiffAbs, mDiffAbs, Finder.getNewMat());
      Mat se = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5, 5));
      Imgproc.morphologyEx(mDiffAbs, mDiffAbs, Imgproc.MORPH_CLOSE, se);

      List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
      Mat mHierarchy = Finder.getNewMat();
      Imgproc.findContours(mDiffAbs, contours, mHierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
      rectangles = contoursToRectangle(contours);

      //Core.subtract(mDiffAbs, mDiffAbs, mChanges);
      //Imgproc.drawContours(mChanges, contours, -1, new Scalar(255));
      //logShow(mDiffAbs);
    }
    return rectangles;
  }

  public static List<Region> contoursToRectangle(List<MatOfPoint> contours) {
    List<Region> rects = new ArrayList<>();
    for (MatOfPoint contour : contours) {
      //log.trace("*** new contour");
      int x1 = 99999;
      int y1 = 99999;
      int x2 = 0;
      int y2 = 0;
      List<Point> points = contour.toList();
      for (Point point : points) {
        int x = (int) point.x;
        int y = (int) point.y;
        //log.trace("x: %d y: %d", x, y);
        if (x < x1) x1 = x;
        if (x > x2) x2 = x;
        if (y < y1) y1 = y;
        if (y > y2) y2 = y;
      }
      Region rect = new Region(x1, y1, x2 - x1, y2 - y1);
      rects.add(rect);
    }
    return rects;
  }
  //</editor-fold>
}
