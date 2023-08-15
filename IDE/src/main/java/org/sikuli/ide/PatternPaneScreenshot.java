/*
 * Copyright (c) 2010-2022, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.ide;

import org.sikuli.basics.Debug;
import org.sikuli.script.Finder;
import org.sikuli.script.Match;
import org.sikuli.script.Pattern;
import org.sikuli.script.ScreenImage;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;
import java.util.*;

@SuppressWarnings("serial")
class PatternPaneScreenshot extends JPanel implements ChangeListener, ComponentListener {
  public static final int DEFAULT_NUM_MATCHES = 50;
  public static final int BOTTOM_MARGIN = 200;
  private static final String me = "PatternPaneScreenshot: ";
//  static int MAX_NUM_MATCHING = EditorPatternButton.DEFAULT_NUM_MATCHES;
  int _width, _height;
  double _scale, _ratio;
  boolean _runFind = false;
  double _similarity;
  int _numMatches;
  Set<Match> _fullMatches = Collections.synchronizedSet(new TreeSet<Match>(new Comparator<Match>() {
    @Override
    public int compare(Match o1, Match o2) {
      return -1 * o1.compareTo(o2);
    }
    @Override
    public boolean equals(Object o) {
      return false;
    }
    @Override
    public int hashCode() {
      int hash = 3;
      return hash;
    }
  }));
  ArrayList<Match> _showMatches = null;
  protected ScreenImage _simg;
  protected BufferedImage _screen = null;
  protected Rectangle _uBound;
  private JLabel btnSimilar;
  private JSlider sldSimilar;
  private LoadingSpinner _loading;
  private String patternFileName;

  static String _I(String key, Object... args) {
    return SikuliIDEI18N._I(key, args);
  }
  private JLabel _msgApplied;

  public PatternPaneScreenshot(ScreenImage simg, Dimension pDim, JLabel msgApplied) {
    init(simg, pDim, msgApplied);
  }

  private void init(ScreenImage simg, Dimension pDim, JLabel msgApplied) {
    _msgApplied = msgApplied;
    addComponentListener(this);
    initScreenImage(simg, pDim);
    //TODO Necessary? MAX_NUM_MATCHING = (int) Vision.getParameter("FindAllMaxReturn");
    autoResize();
    _loading = new LoadingSpinner();
  }

  public JComponent createControls() {
    JPanel pane = new JPanel(new GridBagLayout());
    btnSimilar = new JLabel(_I("lblSimilarity"));
    sldSimilar = createSlider();
    sldSimilar.setPreferredSize(new Dimension(250, 35));

    /*
     JLabel lblPreNumMatches = new JLabel(_I("lblNumberOfMatches"));
     _lblMatchCount = new JLabel("0");
     Dimension size = _lblMatchCount.getPreferredSize();
     size.width *= 2;
     _lblMatchCount.setPreferredSize(size);
     SpinnerNumberModel model = new SpinnerNumberModel(10, 0, PatternPaneScreenshot.MAX_NUM_MATCHING, 1);
     txtNumMatches = new JSpinner(model);
     lblPreNumMatches.setLabelFor(txtNumMatches);
     */

    GridBagConstraints c = new GridBagConstraints();

    c.fill = GridBagConstraints.BOTH;
    c.gridy = 0;
    pane.add(sldSimilar, c);
    pane.add(btnSimilar, c);
    pane.add(_msgApplied, c);

//TODO num Matches needed?
/*		c.fill = 0;
     c.gridy = 1;
     c.gridwidth = 1;
     pane.add(lblPreNumMatches, c);
     c.insets = new Insets(0, 10, 0, 0);
     pane.add(_lblMatchCount, c);
     c.insets = new Insets(0, 0, 0, 0);
     pane.add(new JLabel("/"), c);
     c.insets = new Insets(0, 0, 0, 100);
     pane.add(txtNumMatches, c);

     txtNumMatches.addChangeListener(this);
     */

    return pane;
  }

  private JSlider createSlider() {
    sldSimilar = new PatternSimilaritySlider(0, 100, 70, btnSimilar);
    sldSimilar.setMajorTickSpacing(10);
    sldSimilar.setPaintTicks(true);

    Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
    labelTable.put(Integer.valueOf(0), new JLabel("00"));
    labelTable.put(Integer.valueOf(50), new JLabel("50"));
    labelTable.put(Integer.valueOf(100), new JLabel("99"));
    sldSimilar.setLabelTable(labelTable);
    sldSimilar.setPaintLabels(true);

    sldSimilar.addChangeListener(this);
    return sldSimilar;
  }

  public void createMatches(String patFilename, boolean exact, double similarity) {
    if (!_runFind) {
      _runFind = true;
      _showMatches = null;
      _fullMatches.clear();
      repaint();
      patternFileName = patFilename;
      new Thread(() -> {
        try {
          Finder f = new Finder(_simg.getImage()); // screenImage
          f.findAll(new Pattern(patFilename).similar(0.00001));

          int count = 0;
          while (f.hasNext()) {
            if (++count > DEFAULT_NUM_MATCHES) {
              break;
            }
            _fullMatches.add(f.next());
          }

          EventQueue.invokeLater(() -> {
            setParameters(exact, similarity);
          });
        } catch (Exception e) {
          Debug.error(me + "Problems searching image in ScreenUnion\n%s", e.getMessage());
        }
      }).start();
    } else {
      setParameters(exact, similarity);
    }
  }

  public void reloadImage() {
    _runFind = false;
    createMatches(patternFileName, isExact(), getSimilarity());
  }

  public void setParameters(boolean exact, double similarity) {
    if (!exact) {
      _similarity = similarity;
    } else {
      _similarity = 0.99;
    }
    filterMatches(_similarity);
    sldSimilar.setValue((int) Math.round(similarity * 100));
    repaint();
  }

  @Override
  public void componentHidden(ComponentEvent e) {
  }

  @Override
  public void componentMoved(ComponentEvent e) {
  }

  @Override
  public void componentShown(ComponentEvent e) {
  }

  @Override
  public void componentResized(ComponentEvent e) {
    autoResize();
  }

  private void autoResize() {
    _width = getWidth();
    if (_width == 0) {
      _width = (int) getPreferredSize().getWidth();
    }
    _height = (int) ((double) _width / _ratio);
    _scale = (double) _height / _simg.getRect().height;
    setPreferredSize(new Dimension(_width, _height));
    repaint();
    revalidate();
  }

  public boolean isExact() {
    return _similarity >= 0.99;
  }

  public double getSimilarity() {
    return _similarity;
  }

  public int getNumMatches() {
    return _numMatches;
  }

  private void setSimilarity(double similarity) {
    _similarity = similarity > 0.99 ? 0.99 : similarity;
    filterMatches(_similarity);
    repaint();
  }

  void filterMatches(double similarity) {
    int count = 0;
    if (_fullMatches != null) {
      _showMatches = new ArrayList<Match>();

      synchronized(_fullMatches) {
        for (Match m : _fullMatches) {
          if (m.getScore() >= similarity) {
            _showMatches.add(m);
            if (++count >= DEFAULT_NUM_MATCHES) {
              break;
            }
          }
        }
      }

//      _lblMatchCount.setText(Integer.toString(count));
      Debug.log(4, "filterMatches(%.2f,%d): %d", similarity, count);
    }
  }

  @Override
  public void paintComponent(Graphics g) {
    Graphics2D g2d = (Graphics2D) g;
    if (_screen != null) {
      g2d.drawImage(_screen, 0, 0, _width, _height, null);
      if (_showMatches != null) {
        paintMatches(g2d);
      } else {
        paintOverlay(g2d);
      }
    }
  }

  void paintOverlay(Graphics2D g2d) {
    g2d.setColor(new Color(0, 0, 0, 150));
    g2d.fillRect(0, 0, _width, _height);
    BufferedImage spinner = _loading.getFrame();
    g2d.drawImage(spinner, null, _width / 2 - spinner.getWidth() / 2, _height / 2 - spinner.getHeight() / 2);
    repaint();
  }

  void paintMatches(Graphics2D g2d) {
    for (Match m : _showMatches) {
      int x = (int) (m.x * _scale);
      int y = (int) (m.y * _scale);
      int w = (int) (m.w * _scale);
      int h = (int) (m.h * _scale);
      Color c = PatternSimilaritySlider.getScoreColor(m.getScore());
      g2d.setColor(c);
      g2d.fillRect(x, y, w, h);
      g2d.drawRect(x, y, w - 1, h - 1);
    }
  }

  @Override
  public void stateChanged(javax.swing.event.ChangeEvent e) {
    Object src = e.getSource();
    if (src instanceof JSlider) {
      JSlider source = (JSlider) e.getSource();
      setSimilarity((double) source.getValue() / 100);
    }
  }

  public void setScreenImage(ScreenImage simg, Dimension pDim) {
    initScreenImage(simg, pDim);
    reloadImage();
    autoResize();
  }

  private void initScreenImage(ScreenImage simg, Dimension pDim) {
    _simg = simg;
    _screen = simg.getImage();
    _ratio = (double)simg.getRect().width / simg.getRect().height;
    _height = pDim.height - BOTTOM_MARGIN;
    _scale = (double) _height / simg.getRect().height;
    _width = (int) (simg.getRect().width * _scale);
    setPreferredSize(new Dimension(_width, _height));
  }
}
