/*
 * Copyright (c) 2017 - sikulix.com - MIT license
 */

package org.sikuli.script;

import net.sourceforge.tess4j.Tesseract1;
import net.sourceforge.tess4j.Word;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.sikuli.basics.Debug;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Finder2 {

  static {
    RunTime.loadLibrary(RunTime.libOpenCV);
  }

  protected static void init() {
  }

  //<editor-fold desc="housekeeping">
  static class Log {
    private static String prefix = "UnKnown";

    public Log(String prefix) {
      this.prefix = prefix + ": ";
    }

    public static void error(String msg, Object... args) {
      Debug.error(prefix + msg, args);
    }

    public static void trace(String msg, Object... args) {
      Debug.log(3, prefix + msg, args);
    }
  }

  private static Log log = new Log("Finder2");

  private Mat mBase = getNewMat();
  private Mat mResult = getNewMat();

  private enum FindType {
    ONE, ALL
  }

  private Finder2() {
  }

  public boolean isValid() {
    return !mBase.empty();
  }
  //</editor-fold>

  private FindInput2 fInput = null;

  protected static FindResult2 find(FindInput2 findInput) {
    findInput.setAttributes();
    Finder2 finder2 = new Finder2();
    finder2.fInput = findInput;
    FindResult2 results = finder2.doFind();
    return results;
  }

  private final float resizeMinFactor = 1.5f;
  private final float[] resizeLevels = new float[]{1f, 0.4f};
  private boolean isCheckLastSeen = false;
  private static final double downSimDiff = 0.15;

  private int levelWord = 3;
  private int levelLine = 2;

  private boolean isWord() {
    return fInput.getTextLevel() == levelWord;
  }

  private boolean isLine() {
    return fInput.getTextLevel() == levelLine;
  }

  private boolean isTextMatching(String base, String probe, Pattern pattern) {
    if (Do.SX.isNull(pattern)) {
      return base.equals(probe);
    }
    Matcher matcher = pattern.matcher(base.trim());
    return matcher.find();
  }

  private boolean isTextContained(String base, String probe, Pattern pattern) {
    if (Do.SX.isNull(pattern)) {
      return base.contains(probe);
    }
    Matcher matcher = pattern.matcher(base);
    return matcher.find();
  }

  private FindResult2 doFind() {
    if (!fInput.isValid()) {
      return null;
    }
    FindResult2 findResult = null;
    if (fInput.isText()) {
      boolean globalSearch = false;
      Region where = fInput.getWhere();
      BufferedImage bimg = where.getScreen().capture(where).getImage();
      BufferedImage bimgWork = null;
      String text = fInput.getTargetText();
      TextRecognizer tr = TextRecognizer.start();
      if (tr.isValid()) {
        Tesseract1 tapi = tr.getAPI();
        long timer = new Date().getTime();
        int textLevel = fInput.getTextLevel();
        List<Word> wordsFound = null;
        bimgWork = tr.resize(bimg);
        boolean singleWord = true;
        String[] textSplit = new String[0];
        Pattern pattern = null;
        if (Finder.isRegEx(text)) {
          if (textLevel < 0) {
            text = text.substring(1);
            log.error("RegEx not supported: %s", text);
          } else {
            pattern = Finder.getRegEx(text);
          }
        } else {
          text = text.trim();
        }
        if (textLevel > -1) {
          wordsFound = tapi.getWords(bimgWork, textLevel);
        } else {
          globalSearch = true;
          textSplit = text.split("\\s");
          if (textSplit.length > 1) {
            singleWord = false;
            if (textSplit.length == 3 && textSplit[1].contains("+")) {
              pattern = Pattern.compile(textSplit[0] + ".*?" + textSplit[2]);
            }
          }
          wordsFound = tapi.getWords(bimgWork, levelLine);
        }
        timer = new Date().getTime() - timer;
        List<Word> wordsMatch = new ArrayList<>();
        if (!text.isEmpty()) {
          for (Word word : wordsFound) {
            if (isWord()) {
              if (!isTextMatching(word.getText(), text, pattern)) {
                continue;
              }
            } else if (isLine()) {
              if (!isTextContained(word.getText(), text, pattern)) {
                continue;
              }
            } else if (globalSearch) {
              if (!isTextContained(word.getText().toLowerCase(), text.toLowerCase(), pattern)) {
                continue;
              }
            } else {
              continue;
            }
            Rectangle wordOrLine = word.getBoundingBox();
            Word found = null;
            List<Word> wordsInLine = null;
            List<String> wordsInText = new ArrayList<>();
            if (globalSearch) {
              BufferedImage bLine = Image.getSubimage(bimgWork, wordOrLine);
              wordsInLine = tapi.getWords(bLine, levelWord);
              if (singleWord) {
                for (Word wordInLine : wordsInLine) {
                  if (!isTextContained(wordInLine.getText().toLowerCase(), text.toLowerCase(), null)) {
                    continue;
                  }
                  Rectangle rword = new Rectangle(wordInLine.getBoundingBox());
                  rword.x += wordOrLine.x;
                  rword.y += wordOrLine.y;
                  Rectangle trueRectangel = tr.relocateAsRectangle(rword, where);
                  wordsMatch.add(new Word(wordInLine.getText(), wordInLine.getConfidence(), trueRectangel));
                }
              } else {
                int startText = -1;
                int endText = -1;
                int ix = 0;
                String firstWord = textSplit[0].toLowerCase();
                String lastWord = textSplit[textSplit.length - 1].toLowerCase();
                for (Word wordInLine : wordsInLine) {
                  if (startText < 0) {
                    if (isTextContained(wordInLine.getText().toLowerCase(), firstWord, null)) {
                      startText = ix;
                    }
                  } else if (endText < 0) {
                    if (isTextContained(wordInLine.getText().toLowerCase(), lastWord, null)) {
                      endText = ix;
                    }
                  } else {
                    break;
                  }
                  ix++;
                }
                if (startText > -1 && endText > -1) {
                  Rectangle rword = (new Rectangle(wordsInLine.get(startText).getBoundingBox())).
                          union(new Rectangle(wordsInLine.get(endText).getBoundingBox()));
                  rword.x += wordOrLine.x;
                  rword.y += wordOrLine.y;
                  Rectangle trueRectangel = tr.relocateAsRectangle(rword, where);
                  wordsMatch.add(new Word(text, wordsInLine.get(startText).getConfidence(), trueRectangel));
                }
              }
            } else {
              Rectangle trueRectangel = tr.relocateAsRectangle(wordOrLine, where);
              wordsMatch.add(new Word(word.getText(), word.getConfidence(), trueRectangel));
            }
          }
          if (wordsMatch.size() > 0) {
            log.trace("doFindText: %s found: %d times (%d msec) ", text, wordsMatch.size(), timer);
            findResult = new FindResult2(wordsMatch, fInput);
          } else {
            log.trace("doFindText: %s (%d msec): not found", text, timer);
          }
        } else {
          if (isWord()) {
            log.trace("doFindText: listWords: %d words (%d msec) ", wordsFound.size(), timer);
          } else {
            log.trace("doFindText: listLines: %d lines (%d msec) ", wordsFound.size(), timer);
          }
          for (Word word : wordsFound) {
            Rectangle wordOrLine = word.getBoundingBox();
            Rectangle trueRectangel = tr.relocateAsRectangle(wordOrLine, where);
            wordsMatch.add(new Word(word.getText(), word.getConfidence(), trueRectangel));
          }
          findResult = new FindResult2(wordsMatch, fInput);
        }
      }
    } else {
      FindInput2 findInput = fInput;
      log.trace("doFind: start %s", findInput);
      mBase = findInput.getBase();
      boolean success = false;
      long begin_lap = 0;
      long begin_find = new Date().getTime();
      Core.MinMaxLocResult mMinMax = null;

      double rfactor = 0;
      boolean downSizeFound = false;
      double downSizeScore = -1;
      double downSizeWantedScore = 0;
      Mat findWhere = getNewMat();
      Mat findWhat = getNewMat();

      boolean trueOrFalse = findInput.shouldSearchDownsized(resizeMinFactor);
      //TODO search downsized?
      trueOrFalse = false;
      if (trueOrFalse) {
        // ************************************************* search in downsized
        begin_lap = new Date().getTime();
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
        log.trace("doFind: down: %%%.2f %d msec", 100 * mMinMax.maxVal, new Date().getTime() - begin_lap);
      }
      findWhere = this.mBase;
      trueOrFalse = !findInput.isFindAll() && downSizeFound;
      if (trueOrFalse) {
        // ************************************* check after downsized success
        if (findWhere.size().equals(findInput.getTarget().size())) {
          // trust downsized mResult, if images have same size
          return new FindResult2(mResult, findInput);
        } else {
          int maxLocX = (int) (mMinMax.maxLoc.x * rfactor);
          int maxLocY = (int) (mMinMax.maxLoc.y * rfactor);
          begin_lap = new Date().getTime();
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
                    maxVal * 100, wantedScore * 100, new Date().getTime() - begin_lap);
          }
        }
      }
      // ************************************** search in original
      if (downSizeScore < 0) {
        begin_lap = new Date().getTime();
        mResult = doFindMatch(findInput.getTarget(), findWhere, findInput);
        mMinMax = Core.minMaxLoc(mResult);
        if (!isCheckLastSeen) {
          log.trace("doFind: in original: %%%.4f (?%.0f) %d msec %s",
                  mMinMax.maxVal * 100, findInput.getScore() * 100, new Date().getTime() - begin_lap,
                  findInput.hasMask() ? " **withMask" : "");
        }
        if (mMinMax.maxVal > findInput.getScore()) {
          findResult = new FindResult2(mResult, findInput);
        }
      }
      log.trace("doFind: end %d msec", new Date().getTime() - begin_find);
    }
    return findResult;
  }

  private Mat doFindMatch(Mat what, Mat where, FindInput2 findInput) {
    Mat mResult = getNewMat();
    if (what.empty()) {
      log.error("doFindMatch: image conversion to cvMat did not work");
    } else {
      Mat mWhere = where;
      if (findInput.isGray()) {
        Imgproc.cvtColor(where, mWhere, Imgproc.COLOR_BGR2GRAY);
      }
      if (!findInput.isPlainColor()) {
        if (findInput.hasMask()) {
          Mat mask = findInput.getMask();
          Imgproc.matchTemplate(mWhere, what, mResult, Imgproc.TM_CCORR_NORMED, mask);
        } else {
          Imgproc.matchTemplate(mWhere, what, mResult, Imgproc.TM_CCOEFF_NORMED);
        }
      } else {
        Mat wherePlain = mWhere;
        Mat whatPlain = what;
        if (findInput.isBlack()) {
          Core.bitwise_not(mWhere, wherePlain);
          Core.bitwise_not(what, whatPlain);
        }
        if (findInput.hasMask()) {
          Imgproc.matchTemplate(wherePlain, what, mResult, Imgproc.TM_SQDIFF_NORMED, findInput.getMask());
        } else {
          Imgproc.matchTemplate(wherePlain, whatPlain, mResult, Imgproc.TM_SQDIFF_NORMED);
        }
        Core.subtract(Mat.ones(mResult.size(), CvType.CV_32F), mResult, mResult);
      }
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
    Mat previousGray = getNewMat();
    Mat nextGray = getNewMat();
    Mat mDiffAbs = getNewMat();
    Mat mDiffTresh = getNewMat();

    Imgproc.cvtColor(findInput.getBase(), previousGray, toGray);
    Imgproc.cvtColor(findInput.getTarget(), nextGray, toGray);
    Core.absdiff(previousGray, nextGray, mDiffAbs);
    Imgproc.threshold(mDiffAbs, mDiffTresh, PIXEL_DIFF_THRESHOLD, 0.0, Imgproc.THRESH_TOZERO);

    List<Region> rectangles = new ArrayList<>();
    if (Core.countNonZero(mDiffTresh) > IMAGE_DIFF_THRESHOLD) {
      Imgproc.threshold(mDiffAbs, mDiffAbs, PIXEL_DIFF_THRESHOLD, 255, Imgproc.THRESH_BINARY);
      Imgproc.dilate(mDiffAbs, mDiffAbs, getNewMat());
      Mat se = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5, 5));
      Imgproc.morphologyEx(mDiffAbs, mDiffAbs, Imgproc.MORPH_CLOSE, se);

      List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
      Mat mHierarchy = getNewMat();
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

  //<editor-fold desc="OpenCV Mat">
  public static boolean isOpaque(BufferedImage bImg) {
    if (bImg.getType() == BufferedImage.TYPE_4BYTE_ABGR) {
      List<Mat> mats = getMatList(bImg);
      Mat transMat = mats.get(0);
      int allPixel = (int) transMat.size().area();
      int nonZeroPixel = Core.countNonZero(transMat);
      if (nonZeroPixel != allPixel) {
        return false;
      }
    }
    return true;
  }

  private static List<Mat> getMatList(BufferedImage bImg) {
    byte[] data = ((DataBufferByte) bImg.getRaster().getDataBuffer()).getData();
    Mat aMat = new Mat(bImg.getHeight(), bImg.getWidth(), CvType.CV_8UC4);
    aMat.put(0, 0, data);
    List<Mat> mats = new ArrayList<Mat>();
    Core.split(aMat, mats);
    return mats;
  }

  public static Mat makeMat(BufferedImage bImg) {
    return makeMat(bImg, true);
  }

  public static Mat makeMat(BufferedImage bImg, boolean asBGR) {
    if (bImg.getType() == BufferedImage.TYPE_INT_RGB) {
      log.trace("makeMat: INT_RGB (%dx%d)", bImg.getWidth(), bImg.getHeight());
      int[] data = ((DataBufferInt) bImg.getRaster().getDataBuffer()).getData();
      ByteBuffer byteBuffer = ByteBuffer.allocate(data.length * 4);
      IntBuffer intBuffer = byteBuffer.asIntBuffer();
      intBuffer.put(data);
      Mat aMat = new Mat(bImg.getHeight(), bImg.getWidth(), CvType.CV_8UC4);
      aMat.put(0, 0, byteBuffer.array());
      Mat oMatBGR = new Mat(bImg.getHeight(), bImg.getWidth(), CvType.CV_8UC3);
      Mat oMatA = new Mat(bImg.getHeight(), bImg.getWidth(), CvType.CV_8UC1);
      List<Mat> mixIn = new ArrayList<Mat>(Arrays.asList(new Mat[]{aMat}));
      List<Mat> mixOut = new ArrayList<Mat>(Arrays.asList(new Mat[]{oMatA, oMatBGR}));
      //A 0 - R 1 - G 2 - B 3 -> A 0 - B 1 - G 2 - R 3
      Core.mixChannels(mixIn, mixOut, new MatOfInt(0, 0, 1, 3, 2, 2, 3, 1));
      return oMatBGR;
    } else if (bImg.getType() == BufferedImage.TYPE_3BYTE_BGR) {
      log.trace("makeMat: 3BYTE_BGR (%dx%d)", bImg.getWidth(), bImg.getHeight());
      byte[] data = ((DataBufferByte) bImg.getRaster().getDataBuffer()).getData();
      Mat aMatBGR = new Mat(bImg.getHeight(), bImg.getWidth(), CvType.CV_8UC3);
      aMatBGR.put(0, 0, data);
      return aMatBGR;
    } else if (bImg.getType() == BufferedImage.TYPE_BYTE_INDEXED
                || bImg.getType() == BufferedImage.TYPE_BYTE_BINARY) {
      String bImgType = "BYTE_BINARY";
      if (bImg.getType() == BufferedImage.TYPE_BYTE_INDEXED) {
        bImgType = "BYTE_INDEXED";
      }
      log.trace("makeMat: %s (%dx%d)", bImgType, bImg.getWidth(), bImg.getHeight());
      BufferedImage bimg3b = new BufferedImage(bImg.getWidth(), bImg.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
      Graphics graphics = bimg3b.getGraphics();
      graphics.drawImage(bImg,0, 0, null);
      byte[] data = ((DataBufferByte) bimg3b.getRaster().getDataBuffer()).getData();
      Mat aMatBGR = new Mat(bImg.getHeight(), bImg.getWidth(), CvType.CV_8UC3);
      aMatBGR.put(0, 0, data);
      return aMatBGR;
    } else if (bImg.getType() == BufferedImage.TYPE_4BYTE_ABGR) {
      log.trace("makeMat: TYPE_4BYTE_ABGR (%dx%d)", bImg.getWidth(), bImg.getHeight());
      List<Mat> mats = getMatList(bImg);
      Size size = mats.get(0).size();
      if (asBGR) {
        Mat mBGR = getNewMat(size, 3, -1);
        mats.remove(0);
        Core.merge(mats, mBGR);
        return mBGR;
      } else {
        Mat mBGRA = getNewMat(size, 4, -1);
        mats.add(mats.remove(0));
        Core.merge(mats, mBGRA);
        return mBGRA;
      }
    } else if (bImg.getType() == BufferedImage.TYPE_BYTE_GRAY) {
      log.trace("makeMat: BYTE_GRAY (%dx%d)", bImg.getWidth(), bImg.getHeight());
      byte[] data = ((DataBufferByte) bImg.getRaster().getDataBuffer()).getData();
      Mat aMat = new Mat(bImg.getHeight(), bImg.getWidth(), CvType.CV_8UC1);
      aMat.put(0, 0, data);
      return aMat;
    } else if (bImg.getType() == BufferedImage.TYPE_BYTE_BINARY) {
      log.trace("makeMat: BYTE_BINARY (%dx%d)", bImg.getWidth(), bImg.getHeight());
      BufferedImage bimg3b = new BufferedImage(bImg.getWidth(), bImg.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
      Graphics graphics = bimg3b.getGraphics();
      graphics.drawImage(bImg,0, 0, null);
      byte[] data = ((DataBufferByte) bimg3b.getRaster().getDataBuffer()).getData();
      Mat aMatBGR = new Mat(bImg.getHeight(), bImg.getWidth(), CvType.CV_8UC3);
      aMatBGR.put(0, 0, data);
      return aMatBGR;
    } else {
      log.error("makeMat: BufferedImage: type not supported: %d --- please report this problem", bImg.getType());
    }
    return getNewMat();
  }

  public static Mat makeMat() {
    return getNewMat();
  }

  public static Mat makeMat(Size size, int type, int fill) {
    return getNewMat(size, type, fill);
  }

  protected static Mat getNewMat() {
    return new Mat();
  }

  protected static Mat getNewMat(Size size, int type, int fill) {
    switch (type) {
      case 1:
        type = CvType.CV_8UC1;
        break;
      case 3:
        type = CvType.CV_8UC3;
        break;
      case 4:
        type = CvType.CV_8UC4;
        break;
      default:
        type = -1;
    }
    if (type < 0) {
      return new Mat();
    }
    Mat result;
    if (fill < 0) {
      result = new Mat(size, type);
    } else {
      result = new Mat(size, type, new Scalar(fill));
    }
    return result;
  }

  protected static Mat matMulti(Mat mat, int channels) {
    if (mat.type() != CvType.CV_8UC1 || mat.channels() == channels) {
      return mat;
    }
    List<Mat> listMat = new ArrayList<>();
    for (int n = 0; n < channels; n++) {
      listMat.add(mat);
    }
    Mat mResult = getNewMat();
    Core.merge(listMat, mResult);
    return mResult;
  }

  protected static List<Mat> extractMask(Mat target, boolean onlyChannel4) {
    List<Mat> extracted = new ArrayList<>();
    Mat mask = new Mat();
    Mat targetBGR = new Mat();
    int nChannels = target.channels();
    if (nChannels == 4) {
      List<Mat> mats = new ArrayList<Mat>();
      Core.split(target, mats);
      mask = mats.remove(3);
      Core.merge(mats, targetBGR);
      int allPixel = (int) mask.size().area();
      int nonZeroPixel = Core.countNonZero(mask);
      if (nonZeroPixel != allPixel) {
        Mat maskMask = new Mat();
        Imgproc.threshold(mask, maskMask, 0.0, 1.0, Imgproc.THRESH_BINARY);
        mask = Finder2.matMulti(maskMask, 3);
      } else {
        mask = new Mat();
      }
    } else {
      targetBGR = target;
    }
    if (!onlyChannel4 && (mask.empty() || nChannels == 3)) {
      Mat mGray = new Mat();
      Imgproc.cvtColor(targetBGR, mGray, toGray);
      int allPixel = (int) mGray.size().area();
      int nonZeroPixel = Core.countNonZero(mGray);
      if (nonZeroPixel != allPixel) {
        Mat maskMask = new Mat();
        Imgproc.threshold(mGray, maskMask, 0.0, 1.0, Imgproc.THRESH_BINARY);
        mask = Finder2.matMulti(maskMask, 3);
      }
    }
    extracted.add(targetBGR);
    extracted.add(mask);
    return extracted;
  }

  protected final static String PNG = "png";
  protected final static String dotPNG = "." + PNG;

  public static BufferedImage getBufferedImage(Mat mat) {
    return getBufferedImage(mat, dotPNG);
  }

  public static BufferedImage getBufferedImage(Mat mat, String type) {
    BufferedImage bImg = null;
    MatOfByte bytemat = new MatOfByte();
    if (Do.SX.isNull(mat)) {
      mat = getNewMat();
    }
    Imgcodecs.imencode(type, mat, bytemat);
    byte[] bytes = bytemat.toArray();
    InputStream in = new ByteArrayInputStream(bytes);
    try {
      bImg = ImageIO.read(in);
    } catch (IOException ex) {
      log.error("getBufferedImage: %s error(%s)", mat, ex.getMessage());
    }
    return bImg;
  }

  //</editor-fold>
}
