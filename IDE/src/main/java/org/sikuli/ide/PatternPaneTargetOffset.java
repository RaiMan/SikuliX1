/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.ide;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.event.*;
import org.sikuli.basics.Debug;
import org.sikuli.script.Finder;
import org.sikuli.script.Location;
import org.sikuli.script.Match;
import org.sikuli.script.Region;
import org.sikuli.script.Screen;
import org.sikuli.script.ScreenImage;

class PatternPaneTargetOffset extends JPanel implements
        MouseListener, MouseWheelListener, ChangeListener {

  final static String me = "PatternPaneTargetOffset: ";
	static int DEFAULT_H = 120;
	final static float DEFAULT_PATTERN_RATIO = 0.1f;
	private static final Color COLOR_BG_LINE = new Color(210, 210, 210, 130);
	ScreenImage _simg;
	BufferedImage _img;
	Match _match = null;
	int _viewX, _viewY, _viewW, _viewH;
	float _zoomRatio, _ratio;
	Location _tar = new Location(0, 0);
	Location _offset = new Location(0, 0);
	JSpinner txtX, txtY;
	private LoadingSpinner _loading;
	private boolean _finding = true;
  private JLabel _msgApplied;

	public PatternPaneTargetOffset(
          ScreenImage simg, String patFilename, Location initOffset, Dimension pDim, JLabel msgApplied) {
    _msgApplied = msgApplied;
		_simg = simg;
		_ratio = DEFAULT_PATTERN_RATIO;
		setPreferredSize(new Dimension(pDim.width, pDim.height - DEFAULT_H));

		addMouseListener(this);
		addMouseWheelListener(this);

		_loading = new LoadingSpinner();
		findTarget(patFilename, initOffset);
	}

	void findTarget(final String patFilename, final Location initOffset) {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
        Region screenUnion = Region.create(0, 0, 1, 1);
				Finder f = new Finder(_simg, screenUnion);
				try {
					f.find(patFilename);
					if (f.hasNext()) {
//TODO rewrite completely for ScreenUnion
            Screen s = (Screen) screenUnion.getScreen();
            s.setAsScreenUnion();
						_match = f.next();
            s.setAsScreen();
						if (initOffset != null) {
							setTarget(initOffset.x, initOffset.y);
						} else {
							setTarget(0, 0);
						}
					}
					_img = ImageIO.read(new File(patFilename));
				} catch (IOException e) {
					Debug.error(me + "Can't load " + patFilename);
				}
				synchronized (PatternPaneTargetOffset.this) {
					_finding = false;
				}
				repaint();
			}
		});
		thread.start();
	}

	static String _I(String key, Object... args) {
		return SikuliIDEI18N._I(key, args);
	}

	public void setTarget(int dx, int dy) {
		Debug.log(4, me + "new target: " + dx + "," + dy);
		if (_match != null) {
			Location center = _match.getCenter();
			_tar.x = center.x + dx;
			_tar.y = center.y + dy;
		} else {
			_tar.x = dx;
			_tar.y = dy;
		}

		_offset = new Location(dx, dy);
		if (txtX != null) {
			txtX.setValue(new Integer(dx));
			txtY.setValue(new Integer(dy));
		}
		repaint();
	}

	@Override
	public void mousePressed(MouseEvent me) {
		Location tar = convertViewToScreen(me.getPoint());
		Debug.log(4, "click: " + me.getPoint() + " -> " + tar.toStringShort());
		if (_match != null) {
			Location center = _match.getCenter();
			setTarget(tar.x - center.x, tar.y - center.y);
		} else {
			setTarget(tar.x, tar.y);
		}
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		int rot = e.getWheelRotation();
		int patW = (int) (getWidth() * _ratio);
//		float zoomRatio = patW / (float) _img.getWidth();
		int patH = (int) (_img.getHeight() * _zoomRatio);
		if (rot < 0) {
			if (patW < 2 * getWidth() && patH < 2 * getHeight()) {
				_ratio *= 1.1;
			}
		} else {
			if (patW > 20 && patH > 20) {
				_ratio *= 0.9;
			}
		}
		repaint();
	}

	@Override
	public void mouseClicked(MouseEvent me) {
	}

	@Override
	public void mouseReleased(MouseEvent me) {
	}

	@Override
	public void mouseEntered(MouseEvent me) {
	}

	@Override
	public void mouseExited(MouseEvent me) {
	}

	@Override
	public void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		if (getWidth() > 0 && getHeight() > 0) {
			if (_match != null) {
				zoomToMatch();
				paintSubScreen(g2d);
			} else {
				paintPatternOnly(g2d);
			}
      paintMatch(g2d);
//			paintRulers(g2d);
			paintTarget(g2d);
			synchronized (this) {
				if (_finding) {
					paintLoading(g2d);
				}
			}
		}
	}

	private void zoomToMatch() {
		_viewW = (int) (_match.w / _ratio);
		_zoomRatio = getWidth() / (float) _viewW;
		_viewH = (int) (getHeight() / _zoomRatio);
		_viewX = _match.x + _match.w / 2 - _viewW / 2;
		_viewY = _match.y + _match.h / 2 - _viewH / 2;
	}

	void paintSubScreen(Graphics g2d) {
		if (_viewX < 0 || _viewY < 0) {
			paintBackground(g2d);
		}
		int subX = _viewX < 0 ? 0 : _viewX;
    int subY = _viewY < 0 ? 0 : _viewY;
		int subW = _viewW - (subX - _viewX);
    int subH = _viewH - (subY - _viewY);
		BufferedImage img = _simg.getImage();
		if (subX + subW >= img.getWidth()) {
			subW = img.getWidth() - subX;
		}
		if (subY + subH >= img.getHeight()) {
			subH = img.getHeight() - subY;
		}

		BufferedImage clip = img.getSubimage(subX, subY, subW, subH);
		int destX = (int) ((subX - _viewX) * _zoomRatio),
						destY = (int) ((subY - _viewY) * _zoomRatio);
		int destW = (int) (subW * _zoomRatio),
						destH = (int) (subH * _zoomRatio);
		g2d.drawImage(clip, destX, destY, destW, destH, null);
	}

	void paintMatch(Graphics2D g2d) {
		int w = (int) (getWidth() * _ratio);
		int h = (int) ((float) w / _img.getWidth() * _img.getHeight());
		int x = getWidth() / 2 - w / 2;
    int y = getHeight() / 2 - h / 2;
		Color c = PatternSimilaritySlider.getScoreColor((_match == null ? 1.0 : _match.getScore()));
		g2d.setColor(c);
//		g2d.fillRect(x, y, w, h);
    Stroke savedStroke = g2d.getStroke();
    g2d.setStroke(new BasicStroke(5));
    g2d.drawRect(x, y, w - 1, h - 1);
    g2d.setStroke(savedStroke);
	}

	void paintBackground(Graphics g2d) {
		g2d.setColor(Color.WHITE);
		g2d.fillRect(0, 0, getWidth(), getHeight());
	}

	void paintPatternOnly(Graphics g2d) {
		int patW = (int) (getWidth() * _ratio);
		_zoomRatio = patW / (float) _img.getWidth();
		int patH = (int) (_img.getHeight() * _zoomRatio);
		int patX = getWidth() / 2 - patW / 2, patY = getHeight() / 2 - patH / 2;
		paintBackground(g2d);
		g2d.drawImage(_img, patX, patY, patW, patH, null);
	}

	void paintLoading(Graphics2D g2d) {
		int w = getWidth(), h = getHeight();
		g2d.setColor(new Color(0, 0, 0, 200));
		g2d.fillRect(0, 0, w, h);
		BufferedImage spinner = _loading.getFrame();
		g2d.drawImage(spinner, null, w / 2 - spinner.getWidth() / 2, h / 2 - spinner.getHeight() / 2);
		repaint();
	}

	void paintTarget(Graphics2D g2d) {
		final int CROSS_LEN = 20 / 2;
		Point l = convertScreenToView(_tar);
		g2d.setColor(Color.BLACK);
		g2d.drawLine(l.x - CROSS_LEN, l.y + 1, l.x + CROSS_LEN, l.y + 1);
		g2d.drawLine(l.x + 1, l.y - CROSS_LEN, l.x + 1, l.y + CROSS_LEN);
		g2d.setColor(Color.WHITE);
		g2d.drawLine(l.x - CROSS_LEN, l.y, l.x + CROSS_LEN, l.y);
		g2d.drawLine(l.x, l.y - CROSS_LEN, l.x, l.y + CROSS_LEN);
	}

	void paintRulers(Graphics g2d) {
		int step = (int) (10 * _zoomRatio);
		if (step < 2) {
			step = 2;
		}
		int h = getHeight(), w = getWidth();
		if (h % 2 == 1) {
			h--;
		}
		if (w % 2 == 1) {
			w--;
		}
		g2d.setColor(COLOR_BG_LINE);
		for (int x = w / 2; x >= 0; x -= step) {
			g2d.drawLine(x, 0, x, h);
			g2d.drawLine(w - x, 0, w - x, h);
		}
		for (int y = h / 2; y >= 0; y -= step) {
			g2d.drawLine(0, y, w, y);
			g2d.drawLine(0, h - y, w, h - y);
		}
	}

	Location convertViewToScreen(Point p) {
		Location ret = new Location(0, 0);
		if (_match != null) {
			ret.x = (int) (p.x / _zoomRatio + _viewX);
			ret.y = (int) (p.y / _zoomRatio + _viewY);
		} else {
			ret.x = (int) ((p.x - getWidth() / 2) / _zoomRatio);
			ret.y = (int) ((p.y - getHeight() / 2) / _zoomRatio);
		}
		return ret;
	}

	Point convertScreenToView(Location loc) {
		Point ret = new Point();
		if (_match != null) {
			ret.x = (int) ((loc.x - _viewX) * _zoomRatio);
			ret.y = (int) ((loc.y - _viewY) * _zoomRatio);
		} else {
			ret.x = (int) (getWidth() / 2 + loc.x * _zoomRatio);
			ret.y = (int) (getHeight() / 2 + loc.y * _zoomRatio);
		}
		return ret;
	}

	public JComponent createControls() {
		JPanel pane = new JPanel(new GridBagLayout());
		JLabel lblX = new JLabel(_I("lblTargetOffsetX"));
		JLabel lblY = new JLabel(_I("lblTargetOffsetY"));

		int x = _offset != null ? _offset.x : 0;
		int y = _offset != null ? _offset.y : 0;
		txtX = new JSpinner(new SpinnerNumberModel(x, -999, 999, 1));
		txtY = new JSpinner(new SpinnerNumberModel(y, -999, 999, 1));
		txtX.addChangeListener(this);
		txtY.addChangeListener(this);

		GridBagConstraints c = new GridBagConstraints();

		c.fill = GridBagConstraints.BOTH;
		c.gridy = 0;
		pane.add(lblX, c);
		pane.add(txtX, c);
		pane.add(lblY, c);
		pane.add(txtY, c);
    pane.add(_msgApplied, c);

		return pane;

	}

	@Override
	public void stateChanged(javax.swing.event.ChangeEvent e) {
		int x = (Integer) txtX.getValue();
		int y = (Integer) txtY.getValue();
		setTarget(x, y);
	}

	public Location getTargetOffset() {
		return new Location(_offset);
	}
}
