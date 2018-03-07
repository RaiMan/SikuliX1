/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.ide;

import java.awt.*;
import javax.swing.*;

class PatternSimilaritySlider extends JSlider {

	final JPopupMenu pop = new JPopupMenu();
	JMenuItem item = new JMenuItem();
	private int curVal = -1;
	private JLabel lblVal = null;

	public PatternSimilaritySlider(int min, int max, int val, JLabel lbl) {
		super(min, max, val);
		curVal = val;
		lblVal = lbl;
		init();
	}

	private void init() {
		showValue(lblVal, curVal);
    setPreferredSize(new Dimension(250, 60));
	}

	@Override
	protected void paintComponent(Graphics g) {
		int w = getWidth();
		final int margin = 13;
		final int y1 = 22, y2 = 33;
    int span = w - margin * 2;
		for (int i = 0; i < span; i++) {
			float score = (float) i / span;
			g.setColor(getScoreColor(score));
			g.drawLine(margin + i, y1, margin + i, y2);
		}
		if (getValue() != curVal) {
			curVal = getValue();
			showValue(lblVal, curVal);
		}
		super.paintComponent(g);
	}

	public void showValue(JLabel lbl, int val) {
		float sim = val > 99 ? 0.99f : (float) val / 100;
		String txt = String.format("   ( %.2f )", sim);
		lbl.setText(txt);
		lbl.repaint();
	}

	static Color getScoreColor(double score) {
		// map hue to 0.5~1.0
		Color c = new Color(
						Color.HSBtoRGB(0.5f + (float) score / 2, 1.0f, 1.0f));
		// map alpha to 20~150
		Color cMask = new Color(
						c.getRed(), c.getGreen(), c.getBlue(), 20 + (int) (score * 130));
		return cMask;
	}
}
