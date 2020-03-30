/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script.support;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.sikuli.basics.Debug;
import org.sikuli.script.*;
import org.sikuli.script.Image;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * INTERNAL: OpenCV related support for other features
 */
public class SXOpenCV {

  static {
    RunTime.loadLibrary(RunTime.libOpenCV);
  }

  public static Mat newMat() {
    return new Mat();
  }

  private static BufferedImage makeBufferedImage(Mat content, String type) {
    if (content.empty()) {
      return null;
    }
    BufferedImage bImg = null;
    MatOfByte bytemat = new MatOfByte();
    Imgcodecs.imencode(type, content, bytemat);
    byte[] bytes = bytemat.toArray();
    InputStream in = new ByteArrayInputStream(bytes);
    try {
      bImg = ImageIO.read(in);
    } catch (IOException ex) {
      Debug.log(-1, "SXOpenCV: makeBufferedImage: %s error(%s)", content, ex.getMessage());
    }
    return bImg;
  }

  public static BufferedImage makeBufferedImage(Mat content) {
    return makeBufferedImage(content, ".png");
  }

  public static Mat makeMat(BufferedImage bImg) {

    return makeMat(bImg, true);
  }

  public static Mat makeMat(BufferedImage bImg, boolean asBGR) {
    if (null == bImg) {
      return new Mat();
    }
    if (bImg.getType() == BufferedImage.TYPE_INT_RGB) {
      Debug.trace("makeMat: INT_RGB (%dx%d)", bImg.getWidth(), bImg.getHeight());
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
      Debug.trace("makeMat: 3BYTE_BGR (%dx%d)", bImg.getWidth(), bImg.getHeight());
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
      Debug.trace("makeMat: %s (%dx%d)", bImgType, bImg.getWidth(), bImg.getHeight());
      BufferedImage bimg3b = new BufferedImage(bImg.getWidth(), bImg.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
      Graphics graphics = bimg3b.getGraphics();
      graphics.drawImage(bImg, 0, 0, null);
      byte[] data = ((DataBufferByte) bimg3b.getRaster().getDataBuffer()).getData();
      Mat aMatBGR = new Mat(bImg.getHeight(), bImg.getWidth(), CvType.CV_8UC3);
      aMatBGR.put(0, 0, data);
      return aMatBGR;
    } else if (bImg.getType() == BufferedImage.TYPE_4BYTE_ABGR) {
      Debug.trace("makeMat: TYPE_4BYTE_ABGR (%dx%d)", bImg.getWidth(), bImg.getHeight());
      List<Mat> mats = getMatList(bImg);
      Size size = mats.get(0).size();
      if (asBGR) {
        Mat mBGR = makeMat(size, 3, -1);
        mats.remove(0);
        Core.merge(mats, mBGR);
        return mBGR;
      } else {
        Mat mBGRA = makeMat(size, 4, -1);
        mats.add(mats.remove(0));
        Core.merge(mats, mBGRA);
        return mBGRA;
      }
    } else if (bImg.getType() == BufferedImage.TYPE_BYTE_GRAY) {
      Debug.trace("makeMat: BYTE_GRAY (%dx%d)", bImg.getWidth(), bImg.getHeight());
      byte[] data = ((DataBufferByte) bImg.getRaster().getDataBuffer()).getData();
      Mat aMat = new Mat(bImg.getHeight(), bImg.getWidth(), CvType.CV_8UC1);
      aMat.put(0, 0, data);
      return aMat;
    } else if (bImg.getType() == BufferedImage.TYPE_BYTE_BINARY) {
      Debug.trace("makeMat: BYTE_BINARY (%dx%d)", bImg.getWidth(), bImg.getHeight());
      BufferedImage bimg3b = new BufferedImage(bImg.getWidth(), bImg.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
      Graphics graphics = bimg3b.getGraphics();
      graphics.drawImage(bImg, 0, 0, null);
      byte[] data = ((DataBufferByte) bimg3b.getRaster().getDataBuffer()).getData();
      Mat aMatBGR = new Mat(bImg.getHeight(), bImg.getWidth(), CvType.CV_8UC3);
      aMatBGR.put(0, 0, data);
      return aMatBGR;
    } else {
      //TYPE_CUSTOM 0
      //*OK* TYPE_INT_RGB 1
      //TYPE_INT_ARGB 2
      //TYPE_INT_ARGB_PRE 3
      //TYPE_INT_BGR 4
      //*OK* TYPE_3BYTE_BGR 5
      //*OK* TYPE_4BYTE_ABGR 6
      //TYPE_4BYTE_ABGR_PRE 7
      //TYPE_USHORT_565_RGB 8
      //TYPE_USHORT_555_RGB 9
      //*OK* TYPE_BYTE_GRAY 10
      //TYPE_USHORT_GRAY 11
      //*OK* TYPE_BYTE_BINARY 12
      //TYPE_BYTE_INDEXED 13
      throw new SikuliXception("SXOpenCV::makeMat: BufferedImage: type not supported: " + bImg.getType());
    }
  }

  private static List<Mat> getMatList(BufferedImage bImg) {
    byte[] data = ((DataBufferByte) bImg.getRaster().getDataBuffer()).getData();
    Mat aMat = new Mat(bImg.getHeight(), bImg.getWidth(), CvType.CV_8UC4);
    aMat.put(0, 0, data);
    List<Mat> mats = new ArrayList<Mat>();
    Core.split(aMat, mats);
    return mats;
  }

  private static Mat makeMat(Size size, int type, int fill) {
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

  public static Mat getSubMat(Element element, Mat where, Element subElement) {
    int x = subElement.x - element.x;
    int y = subElement.y - element.y;
    int w = subElement.w;
    int h = subElement.h;
    Rect rect = new Rect(x, y, w, h);
    Mat subMat = where.submat(rect);
    return subMat;
  }

  private static Mat matMulti(Mat mat, int channels) {
    if (mat.type() != CvType.CV_8UC1 || mat.channels() == channels) {
      return mat;
    }
    List<Mat> listMat = new ArrayList<>();
    for (int n = 0; n < channels; n++) {
      listMat.add(mat);
    }
    Mat result = new Mat();
    Core.merge(listMat, result);
    return result;
  }

  public static List<Mat> extractMask(Mat target, boolean onlyChannel4) {
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
        mask = matMulti(maskMask, 3);
      } else {
        mask = new Mat();
      }
    } else {
      targetBGR = target;
    }
    if (!onlyChannel4 && mask.empty()) {
      Mat mGray = new Mat();
      Imgproc.cvtColor(targetBGR, mGray, Imgproc.COLOR_BGR2GRAY);
      int allPixel = (int) mGray.size().area();
      int nonZeroPixel = Core.countNonZero(mGray);
      if (nonZeroPixel != allPixel) {
        Mat maskMask = new Mat();
        Imgproc.threshold(mGray, maskMask, 0.0, 1.0, Imgproc.THRESH_BINARY);
        mask = matMulti(maskMask, 3);
      }
    }
    extracted.add(targetBGR);
    extracted.add(mask);
    return extracted;
  }

  // resize the given image (as cvMat in place) with factor using OpenCV ImgProc.resize()
  public static void resize(Mat mat, double factor) {
    resize(mat, factor, Image.Interpolation.CUBIC);
  }

  public static void resize(Mat mat, double factor, Image.Interpolation interpolation) {
    cvResize(mat, factor, interpolation);
  }

  public static Mat cvResize(BufferedImage bimg, double rFactor, Image.Interpolation interpolation) {
    Mat mat = makeMat(bimg);
    return cvResize(mat, rFactor, interpolation);
  }

  public static Mat cvResize(Mat mat, double rFactor, Image.Interpolation interpolation) {
    int newW = (int) (rFactor * mat.width());
    int newH = (int) (rFactor * mat.height());
    Imgproc.resize(mat, mat, new Size(newW, newH), 0, 0, interpolation.value);
    return mat;
  }

  public static BufferedImage optimize(BufferedImage bimg, float rFactor, Image.Interpolation interpolation) {
    return Element.getBufferedImage(optimize(makeMat(bimg), rFactor, interpolation));
  }

  public static Mat optimize(Mat mimg, float rFactor, Image.Interpolation interpolation) {
    Imgproc.cvtColor(mimg, mimg, Imgproc.COLOR_BGR2GRAY);

    // sharpen original image to primarily get rid of sub pixel rendering artifacts
    mimg = unsharpMask(mimg, 3);

    if (rFactor > 0 && rFactor != 1) {
      resize(mimg, rFactor, interpolation);
    }

    // sharpen the enlarged image again
    mimg = unsharpMask(mimg, 5);

    // invert in case of mainly dark background
    if (Core.mean(mimg).val[0] < 127) {
      Core.bitwise_not(mimg, mimg);
    }
    return mimg;
  }

  /*
   * sharpens the image using an unsharp mask
   */
  private static Mat unsharpMask(Mat img, double sigma) {
    Mat blurred = new Mat();
    Imgproc.GaussianBlur(img, blurred, new Size(), sigma, sigma);
    Core.addWeighted(img, 1.5, blurred, -0.5, 0, img);
    return img;
  }

  public static double diffPercentage(Mat mat1, Mat mat2) {
    if (mat1.type() != mat2.type()) {
      return 1.0;
    }
    if (mat1.cols() != mat2.cols() || mat1.rows() != mat2.rows()) {
      return 1.0;
    }
    Mat thisGray = SXOpenCV.newMat();
    Mat otherGray = SXOpenCV.newMat();
    Mat mDiffAbs = SXOpenCV.newMat();
    Imgproc.cvtColor(mat1, thisGray, Imgproc.COLOR_BGR2GRAY);
    Imgproc.cvtColor(mat2, otherGray, Imgproc.COLOR_BGR2GRAY);
    Core.absdiff(thisGray, otherGray, mDiffAbs);
    return (double) Core.countNonZero(mDiffAbs) / (thisGray.cols() * thisGray.rows());
  }

  private static boolean downSize = false;
  public static final double MIN_THRESHHOLD = 1.0E-5;

  public static Match findMatch(Mat where, FindAttributes findAttributes, boolean findAll) {
    if (downSize) {
      //TODO downsizing
      double downSizeFactor;
      Mat what = findAttributes.what();
      final int downSizeMinSample = 12;
      double downW = ((double) what.width()) / downSizeMinSample;
      double downH = ((double) what.height()) / downSizeMinSample;
      downSizeFactor = Math.max(1.0, Math.min(downW, downH));
    }
    Match matchResult = doFindMatch(where, findAttributes, findAll);
    return matchResult;
  }

  public static Match checkLastSeen(Mat where, FindAttributes findAttributes) {
    Match matchResult = doFindMatch(where, findAttributes, false);
    return matchResult;
  }

  private static Match doFindMatch(Mat where, FindAttributes findAttributes, boolean findAll) {
    Mat result = new Mat();
    Mat finalWhere = where;
    if (findAttributes.gray()) {
      Imgproc.cvtColor(where, finalWhere, Imgproc.COLOR_BGR2GRAY);
    }
    Mat what = findAttributes.what();
    Mat mask = findAttributes.mask();
    if (findAttributes.target().plain()) {
      Mat finalWherePlain = finalWhere;
      Mat finalWhatPlain = what;
      if (findAttributes.target().black()) {
        Core.bitwise_not(finalWhere, finalWherePlain);
        Core.bitwise_not(what, finalWhatPlain);
      }
      if (mask.empty()) {
        Imgproc.matchTemplate(finalWherePlain, finalWhatPlain, result, Imgproc.TM_SQDIFF_NORMED);
      } else {
        Imgproc.matchTemplate(finalWherePlain, what, result, Imgproc.TM_SQDIFF_NORMED, mask);
      }
      Core.subtract(Mat.ones(result.size(), CvType.CV_32F), result, result);
    } else if (mask.empty()) {
      Imgproc.matchTemplate(where, what, result, Imgproc.TM_CCOEFF_NORMED);
    } else {
      Imgproc.matchTemplate(where, what, result, Imgproc.TM_CCORR_NORMED, mask);
    }
    Core.MinMaxLocResult minMax = Core.minMaxLoc(result);
    double maxVal = minMax.maxVal;
    if (maxVal > findAttributes.target().similarity()) {
      Point point = new Point((int) minMax.maxLoc.x, (int) minMax.maxLoc.y);
      if (!findAll) {
        result = null;
      }
      return new Match(point, maxVal, result);
    }
    return null;
  }

  public static boolean isColorEqual(int[] cvColor, Color otherColor) {
    Color col = new Color(cvColor[2], cvColor[1], cvColor[0]);
    int r = (col.getRed() - otherColor.getRed()) * (col.getRed() - otherColor.getRed());
    int g = (col.getGreen() - otherColor.getGreen()) * (col.getGreen() - otherColor.getGreen());
    int b = (col.getBlue() - otherColor.getBlue()) * (col.getBlue() - otherColor.getBlue());
    return Math.sqrt(r + g + b) < MIN_THRESHHOLD;
  }

  private static int toGray = Imgproc.COLOR_BGR2GRAY;
  private static int toColor = Imgproc.COLOR_GRAY2BGR;
  private static int gray = CvType.CV_8UC1;
  private static int colored = CvType.CV_8UC3;
  private static int transparent = CvType.CV_8UC4;

  public static List<Match> doFindChanges(Image original, Image changed) {
    List<Match> changes = new ArrayList<>();
    if (changed.isValid()) {
      int PIXEL_DIFF_THRESHOLD = 3;
      int IMAGE_DIFF_THRESHOLD = 5;
      Mat previousGray = SXOpenCV.newMat();
      Mat nextGray = SXOpenCV.newMat();
      Mat mDiffAbs = SXOpenCV.newMat();
      Mat mDiffTresh = SXOpenCV.newMat();

      Imgproc.cvtColor(original.getContent(), previousGray, toGray);
      Imgproc.cvtColor(changed.getContent(), nextGray, toGray);
      Core.absdiff(previousGray, nextGray, mDiffAbs);
      Imgproc.threshold(mDiffAbs, mDiffTresh, PIXEL_DIFF_THRESHOLD, 0.0, Imgproc.THRESH_TOZERO);

      if (Core.countNonZero(mDiffTresh) > IMAGE_DIFF_THRESHOLD) {
        Imgproc.threshold(mDiffAbs, mDiffAbs, PIXEL_DIFF_THRESHOLD, 255, Imgproc.THRESH_BINARY);
        Imgproc.dilate(mDiffAbs, mDiffAbs, SXOpenCV.newMat());
        Mat se = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5, 5));
        Imgproc.morphologyEx(mDiffAbs, mDiffAbs, Imgproc.MORPH_CLOSE, se);

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat mHierarchy = SXOpenCV.newMat();
        Imgproc.findContours(mDiffAbs, contours, mHierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        changes = contoursToRectangle(contours);
      }
    }
    return changes;
  }

  private static List<Match> contoursToRectangle(List<MatOfPoint> contours) {
    List<Match> rectangles = new ArrayList<>();
    for (MatOfPoint contour : contours) {
      int x1 = 99999;
      int y1 = 99999;
      int x2 = 0;
      int y2 = 0;
      List<org.opencv.core.Point> points = contour.toList();
      for (org.opencv.core.Point point : points) {
        int x = (int) point.x;
        int y = (int) point.y;
        if (x < x1) x1 = x;
        if (x > x2) x2 = x;
        if (y < y1) y1 = y;
        if (y > y2) y2 = y;
      }
      rectangles.add(new Match(x1, y1, x2 - x1, y2 - y1));
    }
    return rectangles;
  }
}
