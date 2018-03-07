/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.jdesktop.swingx.plaf;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.plaf.TextUI;

/**
 * <p>
 * TODO: queries the text components layout manager for the preferred size.
 * </p>
 *
 * @author Peter Weishapl <petw@gmx.net>
 *
 */
public class BuddyTextFieldUI extends PromptTextFieldUI {
	protected BuddyLayoutAndBorder layoutAndBorder;

	// Bad hacking: FIXME when know how to get the real margin.
	private static final Insets MAC_MARGIN = new Insets(0, 2, 1, 2);

	@Override
	public void paint(Graphics g, JComponent c) {
		// yet another dirty mac hack to prevent painting background outside of
		// border.
		if (hasMacTextFieldBorder(c)) {
			Insets borderInsets = layoutAndBorder.getRealBorderInsets();

			borderInsets.left -= MAC_MARGIN.left;
			int height = c.getHeight() - borderInsets.bottom - borderInsets.top + MAC_MARGIN.bottom + MAC_MARGIN.top;
			int width = c.getWidth() - borderInsets.left - borderInsets.right + MAC_MARGIN.right;
			g.clipRect(borderInsets.left, borderInsets.top, width, height);
		}
		super.paint(g, c);
	}

	private boolean hasMacTextFieldBorder(JComponent c) {
		Border border = c.getBorder();
		if (border == layoutAndBorder) {
			border = layoutAndBorder.getBorderDelegate();
		}
		return border != null && border.getClass().getName().equals("apple.laf.CUIAquaTextFieldBorder");
	}

	/**
	 * Creates a new {@link BuddyTextFieldUI} which delegates most work to
	 * another {@link TextUI}.
	 *
	 * @param delegate
	 */
	public BuddyTextFieldUI(TextUI delegate) {
		super(delegate);
	}

	@Override
	public void installUI(JComponent c) {
		super.installUI(c);
		layoutAndBorder = createBuddyLayoutAndBorder();
		layoutAndBorder.install((JTextField) c);
	}

	protected BuddyLayoutAndBorder createBuddyLayoutAndBorder() {
		return new BuddyLayoutAndBorder();
	}

	@Override
	public void uninstallUI(JComponent c) {
		layoutAndBorder.uninstall();
		super.uninstallUI(c);
	}

	/**
	 * TODO: comment
	 *
	 * @see javax.swing.plaf.ComponentUI#getPreferredSize(javax.swing.JComponent)
	 */
	@Override
    public Dimension getPreferredSize(JComponent c) {
		Dimension d = new Dimension();
		Dimension cd = super.getPreferredSize(c);
		Dimension ld = c.getLayout().preferredLayoutSize(c);

		d.height = Math.max(cd.height, ld.height);
		d.width = Math.max(cd.width, ld.width);

		return d;
	}
}
