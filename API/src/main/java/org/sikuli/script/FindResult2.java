package org.sikuli.script;

import net.sourceforge.tess4j.Word;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Range;
import org.opencv.core.Scalar;
import org.sikuli.basics.Debug;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FindResult2 implements Iterator<Match> {

  private FindInput2 findInput = null;
  private int offX = 0;
  private int offY = 0;
  private Mat result = null;
  private List<Word> words = new ArrayList<>();

  private FindResult2() {
  }

  public FindResult2(List<Word> words, FindInput2 findInput) {
    this.words = words;
    this.findInput = findInput;
  }

  public FindResult2(Mat result, FindInput2 findInput) {
    this.result = result;
    this.findInput = findInput;
  }

  public FindResult2(Mat result, FindInput2 target, int[] off) {
    this(result, target);
    offX = off[0];
    offY = off[1];
  }

  private Core.MinMaxLocResult resultMinMax = null;

  private double currentScore = -1;
  double targetScore = -1;
  double lastScore = -1;
  double scoreMeanDiff = -1;
  double scoreMaxDiff = 0.005;
  double matchCount = 0;

  private int currentX = -1;
  private int currentY = -1;
  private int baseW = -1;
  private int baseH = -1;
  private int targetW = -1;
  private int targetH = -1;
  private int marginX = -1;
  private int marginY = -1;

  public boolean hasNext() {
    if (findInput.isText()) {
      if (words.size() > 0) {
        return true;
      }
      return false;
    }
    resultMinMax = Core.minMaxLoc(result);
    currentScore = resultMinMax.maxVal;
    currentX = (int) resultMinMax.maxLoc.x;
    currentY = (int) resultMinMax.maxLoc.y;
    if (lastScore < 0) {
      lastScore = currentScore;
      targetScore = findInput.getScore();
      baseW = result.width();
      baseH = result.height();
      targetW = findInput.getTarget().width();
      targetH = findInput.getTarget().height();
      marginX = (int) (targetW * 0.8);
      marginY = (int) (targetH * 0.8);
      matchCount = 0;
    }
    boolean isMatch = false;
    if (currentScore > targetScore) {
      if (matchCount == 0) {
        isMatch = true;
      } else if (matchCount == 1) {
        scoreMeanDiff = lastScore - currentScore;
        if (scoreMeanDiff < scoreMaxDiff) {
          isMatch = true;
        }
      } else {
        double scoreDiff = lastScore - currentScore;
        if (scoreDiff <= (scoreMeanDiff + 0.001)) {
          scoreMeanDiff = ((scoreMeanDiff * matchCount) + scoreDiff)/(matchCount + 1);
          isMatch = true;
        }
      }
      if (!isMatch) {
        Debug.log(3, "findAll: stop: %.4f (%.4f) %s", currentScore, scoreMeanDiff, findInput);
      }
    }
    return isMatch;
  }

  public Match next() {
    Match match = null;
    if (hasNext()) {
      if (findInput.isText()) {
        Word nextWord = words.remove(0);
        match = new Match(new Region(nextWord.getBoundingBox()), nextWord.getConfidence()/100);
        match.setText(nextWord.getText().trim());
      } else {
        match = new Match(currentX + offX, currentY + offY, targetW, targetH, currentScore, null);
        matchCount++;
        lastScore = currentScore;
        //int margin = getPurgeMargin();
        Range rangeX = new Range(Math.max(currentX - marginX, 0), Math.min(currentX + marginX, result.width()));
        Range rangeY = new Range(Math.max(currentY - marginY, 0), Math.min(currentY + marginY, result.height()));
        result.colRange(rangeX).rowRange(rangeY).setTo(new Scalar(0f));
      }
    }
    return match;
  }

  private int getPurgeMargin() {
    if (currentScore < 0.95) {
      return 4;
    } else if (currentScore < 0.85) {
      return 8;
    } else if (currentScore < 0.71) {
      return 16;
    }
    return 2;
  }

  double bestScore = 0;
  double meanScore = 0;
  double stdDevScore = 0;

  public List<Match> getMatches() {
    if (hasNext()) {
      List<Match> matches = new ArrayList<Match>();
      List<Double> scores = new ArrayList<>();
      while (true) {
        Match match = next();
        if (Do.SX.isNull(match)) {
          break;
        }
        meanScore = (meanScore * matches.size() + match.getScore()) / (matches.size() + 1);
        bestScore = Math.max(bestScore, match.getScore());
        matches.add(match);
        scores.add(match.getScore());
      }
      stdDevScore = calcStdDev(scores, meanScore);
      return matches;
    }
    return null;
  }

  public double[] getScores() {
    return new double[]{bestScore, meanScore, stdDevScore};
  }

  private double calcStdDev(List<Double> doubles, double mean) {
    double stdDev = 0;
    for (double doubleVal : doubles) {
      stdDev += (doubleVal - mean) * (doubleVal - mean);
    }
    return Math.sqrt(stdDev / doubles.size());
  }

  @Override
  public void remove() {
  }
}

