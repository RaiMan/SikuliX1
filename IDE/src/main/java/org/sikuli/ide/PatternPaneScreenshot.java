/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.ide;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import org.sikuli.script.Finder;
import org.sikuli.script.Match;
import org.sikuli.script.Pattern;
import org.sikuli.script.Region;
import org.sikuli.script.ScreenImage;
import org.sikuli.script.ScreenUnion;
import org.sikuli.basics.Debug;

class PatternPaneScreenshot extends JPanel implements ChangeListener, ComponentListener {

  private static final String me = "PatternPaneScreenshot: ";
  static int DEFAULT_H;
  static int MAX_NUM_MATCHING = EditorPatternButton.DEFAULT_NUM_MATCHES;
  Region _match_region;
  int _width, _height;
  double _scale, _ratio;
  boolean _runFind = false;
  float _similarity;
  int _numMatches;
  Set<Match> _fullMatches = null;
  final Boolean _fullMatchesSynch = true;
  ArrayList<Match> _showMatches = null;
  final Boolean _showMatchesSynch = true;
  protected ScreenImage _simg;
  protected BufferedImage _screen = null;
  protected Rectangle _uBound;
  private JLabel btnSimilar, _lblMatchCount;
  private JSlider sldSimilar;
  private JSpinner txtNumMatches;
  private LoadingSpinner _loading;

  static String _I(String key, Object... args) {
    return SikuliIDEI18N._I(key, args);
  }
  private JLabel _msgApplied;

  public PatternPaneScreenshot(ScreenImage simg, Dimension pDim, JLabel msgApplied) {
    init(simg, pDim, msgApplied);
  }

  private void init(ScreenImage simg, Dimension pDim, JLabel msgApplied) {
    _msgApplied = msgApplied;
    _match_region = new ScreenUnion();
    _ratio = (double) _match_region.w / _match_region.h;
    _height = pDim.height - 200;
    _scale = (double) _height / _match_region.h;
    _width = (int) (_match_region.w * _scale);
    setPreferredSize(new Dimension(_width, _height));
    addComponentListener(this);
    _simg = simg;
    _screen = simg.getImage();
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
    labelTable.put(new Integer(0), new JLabel("00"));
    labelTable.put(new Integer(50), new JLabel("50"));
    labelTable.put(new Integer(100), new JLabel("99"));
    sldSimilar.setLabelTable(labelTable);
    sldSimilar.setPaintLabels(true);

    sldSimilar.addChangeListener(this);
    return sldSimilar;
  }

  public void setParameters(final String patFilename,
          final boolean exact, final float similarity,
          final int numMatches) {
    if (!_runFind) {
      _runFind = true;
      Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {
          try {
            Finder f = new Finder(_simg, _match_region);
            f.findAll(new Pattern(patFilename).similar(0.00001f));
            _fullMatches = new TreeSet<Match>(new Comparator() {
              @Override
              public int compare(Object o1, Object o2) {
                return -1 * ((Comparable) o1).compareTo(o2);
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
            });
            int count = 0;
            while (f.hasNext()) {
              if (++count > MAX_NUM_MATCHING) {
                break;
              }
              Match m = f.next();
              Debug.log(4, me + "f.next(%d): " + m.toString(), count);
              synchronized (_fullMatchesSynch) {
                _fullMatches.add(m);
              }
              setParameters(exact, similarity, numMatches);
            }
          } catch (Exception e) {
            Debug.error(me + "Problems searching image in ScreenUnion\n%s", e.getMessage());
          }
        }
      });
      thread.start();
    } else {
      setParameters(exact, similarity, numMatches);
    }
  }

  public void setParameters(boolean exact, float similarity, int numMatches) {
    if (numMatches > MAX_NUM_MATCHING) {
      numMatches = MAX_NUM_MATCHING;
    }
    if (!exact) {
      _similarity = similarity;
    } else {
      _similarity = 0.99f;
    }
    _numMatches = numMatches;
    filterMatches(_similarity, _numMatches);
    sldSimilar.setValue((int) (similarity * 100));
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
    _scale = (double) _height / _match_region.h;
    setPreferredSize(new Dimension(_width, _height));
    repaint();
  }

  public boolean isExact() {
    return _similarity >= 0.99f;
  }

  public float getSimilarity() {
    return _similarity;
  }

  public int getNumMatches() {
    return _numMatches;
  }

  public void setSimilarity(float similarity) {
    _similarity = similarity > 0.99f ? 0.99f : similarity;
    filterMatches(_similarity, _numMatches);
    repaint();
  }

  public void setNumMatches(int numMatches) {
    _numMatches = numMatches;
    filterMatches(_similarity, _numMatches);
    repaint();
  }

  void filterMatches(float similarity, int numMatches) {
    int count = 0;
    if (_fullMatches != null && numMatches >= 0) {
      if (_showMatches == null) {
        _showMatches = new ArrayList<Match>();
      }
      synchronized (_showMatchesSynch) {
        _showMatches.clear();
        if (numMatches == 0) {
          return;
        }
        synchronized (_fullMatchesSynch) {
          for (Match m : _fullMatches) {
            if (m.getScore() >= similarity) {
              _showMatches.add(m);
              if (++count >= numMatches) {
                break;
              }
            }
          }
        }
      }
//      _lblMatchCount.setText(Integer.toString(count));
      Debug.log(4, "filterMatches(%.2f,%d): %d", similarity, numMatches, count);
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
    synchronized (_showMatchesSynch) {
      for (Match m : _showMatches) {
        int x = (int) ((m.x - _match_region.x) * _scale);
        int y = (int) ((m.y - _match_region.y) * _scale);
        int w = (int) (m.w * _scale);
        int h = (int) (m.h * _scale);
        Color c = PatternSimilaritySlider.getScoreColor(m.getScore());
        g2d.setColor(c);
        g2d.fillRect(x, y, w, h);
        g2d.drawRect(x, y, w - 1, h - 1);
      }
    }

  }

  @Override
  public void stateChanged(javax.swing.event.ChangeEvent e) {
    Object src = e.getSource();
    if (src instanceof JSlider) {
      JSlider source = (JSlider) e.getSource();
      int val = (int) source.getValue();
      setSimilarity((float) val / 100);
    } else if (src instanceof JSpinner) {
      JSpinner source = (JSpinner) e.getSource();
      int val = (Integer) source.getValue();
      setNumMatches(val);
    }
  }
}
