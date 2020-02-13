/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script.support;

import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.sikuli.basics.Debug;
import org.sikuli.script.Element;
import org.sikuli.script.Image;

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

  public static BufferedImage makeBufferedImage(Mat content, String type) {
    BufferedImage bImg = null;
    MatOfByte bytemat = new MatOfByte();
    Imgcodecs.imencode(type, content, bytemat);
    byte[] bytes = bytemat.toArray();
    InputStream in = new ByteArrayInputStream(bytes);
    try {
      bImg = ImageIO.read(in);
    } catch (IOException ex) {
      Debug.log(-1,"SXOpenCV: makeBufferedImage: %s error(%s)", content, ex.getMessage());
    }
    return bImg;
  }

  public static BufferedImage makeBufferedImage(Mat content) {
    if (content.empty()) {
      return null;
    }
    return (BufferedImage) HighGui.toBufferedImage(content);
  }

  public static Mat makeMat(BufferedImage bImg) {
    return makeMat(bImg, true);
  }

  public static Mat makeMat(BufferedImage bImg, boolean asBGR) {
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
      Debug.error("makeMat: BufferedImage: type not supported: %d --- please report this problem", bImg.getType());
    }
    return new Mat();
  }

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

  private static Mat matMulti(Mat mat, int channels) {
    if (mat.type() != CvType.CV_8UC1 || mat.channels() == channels) {
      return mat;
    }
    List<Mat> listMat = new ArrayList<>();
    for (int n = 0; n < channels; n++) {
      listMat.add(mat);
    }
    Mat mResult = new Mat();
    Core.merge(listMat, mResult);
    return mResult;
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
    if (!onlyChannel4 && (mask.empty() || nChannels == 3)) {
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
    public static void resize(Mat mat, float factor) {
      resize(mat, factor, Image.Interpolation.CUBIC);
    }

  public static void resize(Mat mat, float factor, Image.Interpolation interpolation) {
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
    Mat mimg = makeMat(bimg);

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

    return Element.getBufferedImage(mimg);
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
}
