/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.ide;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

public class CloseableModernTabbedPaneUI extends BasicTabbedPaneUI {

	private static final String TABBED_PANE_UI_LOGGER = "TabbedPaneUI";
	private static final Logger LOGGER = Logger.getLogger(TABBED_PANE_UI_LOGGER);
	private int TAB_WIDTH = 0;
	private static int TAB_HEIGHT = 24;
	private static BufferedImage tabSelectedPressedEnd;
	private static BufferedImage tabSelectedPressed;
	private static BufferedImage tabSelectedEnd;
	private static BufferedImage tabSelected;
	@SuppressWarnings({"unused", "UnusedDeclaration", "FieldCanBeLocal"})
	private static BufferedImage tabClosePressed;
	@SuppressWarnings({"unused", "UnusedDeclaration", "FieldCanBeLocal"})
	private static BufferedImage tabCloseRollover;
	@SuppressWarnings({"unused", "UnusedDeclaration", "FieldCanBeLocal"})
	private static BufferedImage tabClose;
	private static BufferedImage tabRolloverEnd;
	private static BufferedImage tabRollover;
	private static BufferedImage tabEnd;
	private static BufferedImage tab;
	private int tabPressed = -1;
	private int width;

	static {
		try {
			tabSelectedPressedEnd = ImageIO.read(
							CloseableModernTabbedPaneUI.class.getResource(
							"/icons/tab/tab-aqua-highlight-sep.png"));
			tabSelectedPressed = ImageIO.read(
							CloseableModernTabbedPaneUI.class.getResource(
							"/icons/tab/tab-aqua-highlight.png"));
			tabSelectedEnd = ImageIO.read(CloseableModernTabbedPaneUI.class.getResource(
							"/icons/tab/tab-aqua-sep.png"));
			tabSelected = ImageIO.read(CloseableModernTabbedPaneUI.class.getResource(
							"/icons/tab/tab-aqua.png"));
			/*
			 tabClosePressed = ImageIO.read(ModernTabbedPaneUI.class.getResource(
			 "/icons/tab/tab-close-pressed.png"));
			 tabCloseRollover = ImageIO.read(
			 ModernTabbedPaneUI.class.getResource(
			 "/icons/tab/tab-close-rollover.png"));
			 tabClose = ImageIO.read(ModernTabbedPaneUI.class.getResource(
			 "/icons/tab/tab-close.png"));
			 */
			tabRolloverEnd = ImageIO.read(CloseableModernTabbedPaneUI.class.getResource(
							"/icons/tab/tab-normal-highlight-sep.png"));
			tabRollover = ImageIO.read(CloseableModernTabbedPaneUI.class.getResource(
							"/icons/tab/tab-normal-highlight.png"));
			tabEnd = ImageIO.read(CloseableModernTabbedPaneUI.class.getResource(
							"/icons/tab/tab-normal-sep.png"));
			tab = ImageIO.read(CloseableModernTabbedPaneUI.class.getResource(
							"/icons/tab/tab-normal.png"));
		} catch (IOException e) {
			LOGGER.warning("Could not load SliderUI images");
		}
	}

	// TODO Paint & handle close buttons but on first tab
	public CloseableModernTabbedPaneUI(int width) {
		TAB_WIDTH = width;
	}

	@Override
	public void installUI(JComponent c) {
		JTabbedPane tabPane = (JTabbedPane) c;
		tabPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

		// TODO Test on Windows, this is a Mac OS X workaround
		Constructor<?> constructor = null;
		try {
			Class<?> aClass = Class.forName(
							"javax.swing.plaf.basic.BasicTabbedPaneUI$Actions");
			constructor = aClass.getDeclaredConstructor(String.class);
			constructor.setAccessible(true);
		} catch (ClassNotFoundException e) {
			getLogger().warning("Cannot access tabbed pane UI actions");
		} catch (NoSuchMethodException e) {
			getLogger().warning("Constructor does not exist");
		}

		if (constructor != null) {
			ActionMap map = tabPane.getActionMap();
			try {
				map.put("scrollTabsBackwardAction",
								(Action) constructor.newInstance("scrollTabsBackwardAction"));
				map.put("scrollTabsForwardAction",
								(Action) constructor.newInstance("scrollTabsForwardAction"));
			} catch (InstantiationException e) {
				getLogger().warning("Cannot instantiate action");
			} catch (IllegalAccessException e) {
				getLogger().warning("Action cannot be accessed");
			} catch (InvocationTargetException e) {
				getLogger().warning("Cannot instantiate action");
			}
		}

		super.installUI(c);
	}

	@Override
	protected void installDefaults() {
		UIManager.put("TabbedPane.tabAreaInsets", new Insets(0, 0, 0, 0));
		UIManager.put("TabbedPane.font",
						((Font) UIManager.get("TabbedPane.font")).deriveFont(Font.BOLD));

		/* UIManager.put("TabbedPane.font",
		 new Font("Thoma",Font.BOLD,12));
		 */
		UIManager.put("TabbedPane.contentBorderInsets", new Insets(0, 0, 0, 0));
		UIManager.put("TabbedPane.selectedTabPadInsets", new Insets(0, 0, 0, 0));

		super.installDefaults();
	}

	@Override
	protected void installListeners() {
		super.installListeners();

		tabPane.addMouseListener(new TabPressedTracker());
	}

	@Override
	protected int calculateTabHeight(int tabPlacement, int tabIndex,
					int fontHeight) {
		return TAB_HEIGHT;
	}

	@Override
	protected int calculateMaxTabHeight(int tabPlacement) {
		return TAB_HEIGHT;
	}

	/*
	 @Override
	 protected int calculateTabWidth(int tabPlacement, int tabIndex,
	 FontMetrics metrics) {
	 return TAB_WIDTH;
	 }

	 @Override
	 protected int calculateMaxTabWidth(int tabPlacement) {
	 return TAB_WIDTH;
	 }
	 */
	@Override
	protected int getTabRunIndent(int tabPlacement, int run) {
		return 0;
	}

	@Override
	protected void setRolloverTab(int index) {
		int oldIndex = getRolloverTab();
		super.setRolloverTab(index);

		if (oldIndex != index) {
			if (oldIndex != -1 && oldIndex < tabPane.getTabCount()) {
				tabPane.repaint(getTabBounds(tabPane, oldIndex));
			}

			if (index != -1 && index < tabPane.getTabCount()) {
				tabPane.repaint(getTabBounds(tabPane, index));
			}
		}
	}

	@Override
	protected int getTabLabelShiftX(int tabPlacement, int tabIndex,
					boolean isSelected) {
		return rects[tabIndex].width % 2;
	}

	@Override
	protected int getTabLabelShiftY(int tabPlacement, int tabIndex,
					boolean isSelected) {
		return 0;
	}

	@Override
	public void paint(Graphics g, JComponent c) {
		int tabPlacement = tabPane.getTabPlacement();
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		Insets insets = c.getInsets();
		Dimension size = c.getSize();

		if (tabPane.getTabPlacement() == TOP) {
			g2d.drawImage(tab, insets.left, insets.top,
							size.width - insets.right - insets.left - 1,
							calculateTabAreaHeight(tabPlacement, runCount,
							maxTabHeight),
							null);
			g2d.setColor(Color.gray);
			g2d.drawLine(size.width - 1, insets.top + 2, size.width - 1, insets.top
							+ calculateTabAreaHeight(tabPlacement, runCount,
							maxTabHeight));
		}
		/*System.out.println("Tab Height"+calculateTabAreaHeight(tabPlacement, runCount,
		 maxTabHeight));*/
		super.paint(g2d, c);
	}

	@Override
	protected void paintTabBackground(Graphics g, int tabPlacement,
					int tabIndex, int x, int y, int w, int h,
					boolean isSelected) {
		BufferedImage background;
		BufferedImage end;
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		if (isSelected) {
			if (tabPressed == tabIndex) {
				background = tabSelectedPressed;
				end = tabSelectedPressedEnd;
			} else {
				background = tabSelected;
				end = tabSelectedEnd;
			}
		} else {
			if (getRolloverTab() == tabIndex) {
				background = tabRollover;
				end = tabRolloverEnd;
			} else {
				background = tab;
				end = tabEnd;
			}
		}

		if (x < 0) {
			x = 0;
		}

		if (y < 0) {
			y = 0;
		}
		g2d.drawImage(background, x + 1, y + 1, w - 1, TAB_HEIGHT, null);
		//g2d.drawLine(end.getWidth(), x + w- end.getWidth(),end.getWidth(),TAB_HEIGHT);
		//g2d.drawImage(end, x + w - end.getWidth()+1, TAB_HEIGHT, null);
	}

	private class TabPressedTracker extends MouseAdapter {

		@Override
		public void mousePressed(MouseEvent e) {
			if (tabPane == null || !tabPane.isEnabled()) {
				return;
			}
			tabPressed = tabForCoordinate(tabPane, e.getX(), e.getY());
			if (tabPressed != -1) {
				tabPane.repaint(getTabBounds(tabPane, tabPressed));
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			int oldTabPressed = tabPressed;
			tabPressed = -1;
			if (oldTabPressed != -1) {
				tabPane.repaint(getTabBounds(tabPane, oldTabPressed));
			}
		}
	}

	// Methods below are overriden to get rid of the painting
	@Override
	protected void paintFocusIndicator(Graphics g, int tabPlacement,
					Rectangle[] rects, int tabIndex,
					Rectangle iconRect, Rectangle textRect,
					boolean isSelected) {
	}

	@Override
	protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex,
					int x, int y, int w, int h,
					boolean isSelected) {
	}

	@Override
	protected void paintContentBorder(Graphics g, int tabPlacement,
					int selectedIndex) {
	}

	private static Logger getLogger() {
		return LOGGER;
	}
}
class AquaCloseableTabbedPaneUI extends CloseableModernTabbedPaneUI {

	/**
	 * the horizontal position of the text
	 */
	private int horizontalTextPosition = SwingUtilities.LEFT;

	/**
	 * Creates a new instance of
	 * <code>CloseableTabbedPaneUI</code>
	 */
	public AquaCloseableTabbedPaneUI() {
		super(100);
	}

	/**
	 * Creates a new instance of
	 * <code>CloseableTabbedPaneUI</code>
	 *
	 * @param horizontalTextPosition the horizontal position of the text (e.g.
	 * SwingUtilities.TRAILING or SwingUtilities.LEFT)
	 */
	public AquaCloseableTabbedPaneUI(int horizontalTextPosition) {
		this();
		this.horizontalTextPosition = horizontalTextPosition;
	}
	Color darkTabColor = new Color(200, 200, 200, 100);
	Color borderColor = new Color(150, 150, 150);

	@Override
	protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {

		super.paintTabBackground(g, tabPlacement, tabIndex, x, y, w, h, isSelected);
		if (!isSelected) {
			g.setColor(darkTabColor);
			g.fillRect(x + 1, y + 2, w - 1, h - 1);
		}
		g.setColor(borderColor);
		g.drawLine(x + w, y + 2, x + w, y + h);
		g.drawLine(x, y + 2, x, y + h);
		if (isSelected) {
			g.setColor(Color.white);
			g.drawLine(x, y + h, x + w, y + h);
		}
	}

	/**
	 * Layouts the label
	 *
	 * @param tabPlacement the placement of the tabs
	 * @param metrics the font metrics
	 * @param tabIndex the index of the tab
	 * @param title the title of the tab
	 * @param icon the icon of the tab
	 * @param tabRect the tab boundaries
	 * @param iconRect the icon boundaries
	 * @param textRect the text boundaries
	 * @param isSelected true whether the tab is selected, false otherwise
	 */
	@Override
	protected void layoutLabel(int tabPlacement, FontMetrics metrics,
					int tabIndex, String title, Icon icon,
					Rectangle tabRect, Rectangle iconRect,
					Rectangle textRect, boolean isSelected) {

		textRect.x = textRect.y = iconRect.x = iconRect.y = 0;

		javax.swing.text.View v = getTextViewForTab(tabIndex);
		if (v != null) {
			tabPane.putClientProperty("html", v);
		}

		SwingUtilities.layoutCompoundLabel((JComponent) tabPane,
						metrics, title, icon,
						SwingUtilities.CENTER,
						SwingUtilities.CENTER,
						SwingUtilities.CENTER,
						//SwingUtilities.TRAILING,
						horizontalTextPosition,
						tabRect,
						iconRect,
						textRect,
						textIconGap + 2);

		tabPane.putClientProperty("html", null);

		int xNudge = getTabLabelShiftX(tabPlacement, tabIndex, isSelected);
		int yNudge = getTabLabelShiftY(tabPlacement, tabIndex, isSelected);
		iconRect.x += xNudge;
		iconRect.y += yNudge;
		textRect.x += xNudge;
		textRect.y += yNudge;
	}
}
