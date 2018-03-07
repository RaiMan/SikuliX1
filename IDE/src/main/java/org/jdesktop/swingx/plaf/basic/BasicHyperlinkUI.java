/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.plaf.basic;

import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Shape;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.logging.Logger;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JToolBar;
import javax.swing.LookAndFeel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicButtonListener;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicGraphicsUtils;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.Position;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.ImageView;
import javax.swing.text.html.StyleSheet;

import org.jdesktop.swingx.JXHyperlink;
import org.jdesktop.swingx.SwingXUtilities;

/**
 * Basic implementation of the <code>JXHyperlink</code> UI. <br>
 * This is copied from org.jdesktop.jdnc.plaf.basic.BasicLinkButtonUI
 */
public class BasicHyperlinkUI extends BasicButtonUI {

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(BasicHyperlinkUI.class
            .getName());

    public static ComponentUI createUI(JComponent c) {
        return new BasicHyperlinkUI();
    }

    private static Rectangle viewRect = new Rectangle();

    private static Rectangle textRect = new Rectangle();

    private static Rectangle iconRect = new Rectangle();

//    private static MouseListener handCursorListener = new HandCursor();

    protected int dashedRectGapX;

    protected int dashedRectGapY;

    protected int dashedRectGapWidth;

    protected int dashedRectGapHeight;

    private Color focusColor;

    private View ulv;

    private PropertyChangeListener pcListener = new PropertyChangeListener() {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            // this method is called from the edt. only other place where ulv is used is in
            // painting which also happens on edt so it should be safe even without synchronization
            // sole purpose of this call is to reinitialize view on every property change
            ulv = null;
        }};

    @Override
    protected void installDefaults(AbstractButton b) {
        super.installDefaults(b);

        JXHyperlink link = (JXHyperlink) b;

        LookAndFeel.installProperty(b, "opaque", false);

        if (SwingXUtilities.isUIInstallable(link.getUnclickedColor())) {
            link.setUnclickedColor(UIManager.getColor("Hyperlink.linkColor"));
        }

        if (SwingXUtilities.isUIInstallable(link.getClickedColor())) {
            link.setClickedColor(UIManager.getColor("Hyperlink.visitedColor"));
        }

        b.setBorderPainted(false);
        b.setRolloverEnabled(true);

        if (SwingXUtilities.isUIInstallable(b.getBorder())) {
            b.setBorder(new BorderUIResource(BorderFactory.createEmptyBorder(0, 1, 0, 0)));
        }

        dashedRectGapX = UIManager.getInt("ButtonUI.dashedRectGapX");
        dashedRectGapY = UIManager.getInt("ButtonUI.dashedRectGapY");
        dashedRectGapWidth = UIManager.getInt("ButtonUI.dashedRectGapWidth");
        dashedRectGapHeight = UIManager.getInt("ButtonUI.dashedRectGapHeight");
        focusColor = UIManager.getColor("ButtonUI.focus");

        b.setHorizontalAlignment(SwingConstants.LEADING);
    }

    @Override
    protected void installListeners(AbstractButton b) {
        super.installListeners(b);
//        b.addMouseListener(handCursorListener);
        b.addPropertyChangeListener(pcListener);
    }

    @Override
    protected void uninstallListeners(AbstractButton b) {
        super.uninstallListeners(b);
//        b.removeMouseListener(handCursorListener);
        b.removePropertyChangeListener(pcListener);
    }

    protected Color getFocusColor() {
        return focusColor;
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        AbstractButton b = (AbstractButton) c;
        ButtonModel model = b.getModel();

        FontMetrics fm = g.getFontMetrics();

        Insets i = c.getInsets();

        viewRect.x = i.left;
        viewRect.y = i.top;
        viewRect.width = b.getWidth() - (i.right + viewRect.x);
        viewRect.height = b.getHeight() - (i.bottom + viewRect.y);

        textRect.x = textRect.y = textRect.width = textRect.height = 0;
        iconRect.x = iconRect.y = iconRect.width = iconRect.height = 0;

        Font f = c.getFont();
        g.setFont(f);

        // layout the text and icon
        String text = SwingUtilities.layoutCompoundLabel(c, fm, b.getText(), b
                .getIcon(), b.getVerticalAlignment(), b
                .getHorizontalAlignment(), b.getVerticalTextPosition(), b
                .getHorizontalTextPosition(), viewRect, iconRect, textRect, b
                .getText() == null ? 0 : b.getIconTextGap());

        clearTextShiftOffset();

        // perform UI specific press action, e.g. Windows L&F shifts text
        if (model.isArmed() && model.isPressed()) {
            paintButtonPressed(g, b);
        }

        // Paint the Icon
        if (b.getIcon() != null) {
            paintIcon(g, c, iconRect);
        }

//        Composite oldComposite = ((Graphics2D) g).getComposite();
//
//        if (model.isRollover()) {
//            ((Graphics2D) g).setComposite(AlphaComposite.getInstance(
//                    AlphaComposite.SRC_OVER, 0.5f));
//        }

        if (text != null && !text.equals("")) {
            View v = (View) c.getClientProperty(BasicHTML.propertyKey);
            if (v != null) {
                paintHTMLText(g, b, textRect, text, v);
            } else {
                paintText(g, b, textRect, text);
            }
        }

        if (b.isFocusPainted() && b.hasFocus()) {
            // paint UI specific focus
            paintFocus(g, b, viewRect, textRect, iconRect);
        }

//        ((Graphics2D) g).setComposite(oldComposite);
    }

    /**
     * Method which renders the text of the current button if html.
     * <p>
     * @param g Graphics context
     * @param b Current button to render
     * @param textRect Bounding rectangle to render the text.
     * @param text String to render
     * @param v the View to use.
     */
    protected void paintHTMLText(Graphics g, AbstractButton b,
            Rectangle textRect, String text, View v) {
        textRect.x += getTextShiftOffset();
        textRect.y += getTextShiftOffset();
        // fix #441-swingx - underline not painted for html
        if (b.getModel().isRollover()) {
            //paintUnderline(g, b, textRect, text);
            if (ulv == null) {
                ulv = ULHtml.createHTMLView(b, text);
            }
            ulv.paint(g, textRect);
        } else {
            v.paint(g, textRect);
        }
        textRect.x -= getTextShiftOffset();
        textRect.y -= getTextShiftOffset();
    }

    /**
     * {@inheritDoc} <p>
     * Overridden to paint the underline on rollover.
     */
    @Override
    protected void paintText(Graphics g, AbstractButton b, Rectangle textRect,
            String text) {
        //kgs -- SwingX #415: pixel-shift when disabled
        //BasicButtonUI shifts disabled text to the left by 1 pixel
        //we compensate for that here, so that all Hyperlinks paint
        //at the same location regardless of state
        if (!b.getModel().isEnabled()) {
            textRect.x += 1;
        }

        super.paintText(g, b, textRect, text);
        if (b.getModel().isRollover()) {
            paintUnderline(g, b, textRect, text);
        }
    }

    private void paintUnderline(Graphics g, AbstractButton b, Rectangle rect,
            String text) {
        // JW: copied from JXTable.LinkRenderer
        FontMetrics fm = g.getFontMetrics();
        int descent = fm.getDescent();

        // REMIND(aim): should we be basing the underline on
        // the font's baseline instead of the text bounds?
        g.drawLine(rect.x + getTextShiftOffset(),
          (rect.y + rect.height) - descent + 1 + getTextShiftOffset(),
          rect.x + rect.width + getTextShiftOffset(),
          (rect.y + rect.height) - descent + 1 + getTextShiftOffset());
    }

    @Override
    protected void paintFocus(Graphics g, AbstractButton b, Rectangle viewRect,
            Rectangle textRect, Rectangle iconRect) {
        if (b.getParent() instanceof JToolBar) {
            // Windows doesn't draw the focus rect for buttons in a toolbar.
            return;
        }

        // focus painted same color as text
        g.setColor(getFocusColor());
        // paint the focus rect around the union of text rect and icon rect
        // PENDING JW: duplicated to handle insets
        Rectangle iconTextRect = getIconTextRect(b);
        // PENDING JW: better factor handling of insets - the bare union doesn't respect insets
//        Rectangle iconTextRect = textRect.union(iconRect);
        BasicGraphicsUtils.drawDashedRect(g, iconTextRect.x, iconTextRect.y,
                iconTextRect.width, iconTextRect.height);
        // pre-#167-swingx: active area too large
//        int width = b.getWidth();
//        int height = b.getHeight();
//        BasicGraphicsUtils.drawDashedRect(g, dashedRectGapX, dashedRectGapY,
//                width - dashedRectGapWidth, height - dashedRectGapHeight);
    }

    @Override
    protected void paintButtonPressed(Graphics g, AbstractButton b) {
         setTextShiftOffset();
    }

    @Override
    protected BasicButtonListener createButtonListener(AbstractButton b) {
        return new BasicHyperlinkListener(b);
    }

    /**
     * {@inheritDoc} <p>
     *
     * Overridden to return true if the position is inside the union of the
     * text and icon rectangle, false otherwise.
     */
    @Override
    public boolean contains(JComponent c, int x, int y) {
        AbstractButton button = (AbstractButton) c;
        return isInside(getIconTextRect(button), x, y);
    }

    /**
     * @param iconTextRect
     * @param point
     * @return
     */
    private boolean isInside(Rectangle iconTextRect, int x, int y) {
        if (iconTextRect == null) return false;
        return iconTextRect.contains(x, y);
    }

    /**
     * C&p'ed from BasicGraphicsUtils (getPreferredButtonSize).
     *
     * @param b the button to analyse.
     * @return the union of the text and icon rectangle of the AbstractButton
     *   or null if the button has children (??)
     */
    protected Rectangle getIconTextRect(AbstractButton b) {
        if (b.getComponentCount() > 0) {
            return null;
        }

        Icon icon = b.getIcon();
        String text = b.getText();

        Font font = b.getFont();
        FontMetrics fm = b.getFontMetrics(font);

        Rectangle iconR = new Rectangle();
        Rectangle textR = new Rectangle();
        Rectangle viewR = new Rectangle(b.getSize());

        SwingUtilities.layoutCompoundLabel(b, fm, text, icon,
                b.getVerticalAlignment(), b.getHorizontalAlignment(), b
                        .getVerticalTextPosition(), b
                        .getHorizontalTextPosition(), viewR, iconR, textR,
                (text == null ? 0 : b.getIconTextGap()));

        /*
         * The preferred size of the button is the size of the text and icon
         * rectangles plus the buttons insets.
         */

        Rectangle r = iconR.union(textR);

        Insets insets = b.getInsets();
        r.width += insets.left + insets.right;
        r.height += insets.top + insets.bottom;
        // PENDING JW: why not?
//        r.x -= insets.left;
        r.y -= insets.top;
        return r;
    }

    /**
     * A BasicButtonListener specialized to the needs of a Hyperlink.
     *
     * @author Jeanette Winzenburg
     */
    public static class BasicHyperlinkListener extends BasicButtonListener {

        /**
         * @param b
         */
        public BasicHyperlinkListener(AbstractButton b) {
            super(b);
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            AbstractButton button = (AbstractButton) e.getSource();
            if (button.isRolloverEnabled()) {
                button.setCursor(button.getModel().isRollover() ?
                        // PENDING JW: support customizable cursor
                        Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : null);
            }
            super.stateChanged(e);
        }
    }

    static class ULHtml extends BasicHTML {
        /**
         * Create an html renderer for the given component and
         * string of html.
         */
        public static View createHTMLView(JComponent c, String html) {
            BasicEditorKit kit = getFactory();
            Document doc = kit.createDefaultDocument(c.getFont(),
                                                     c.getForeground());
            Object base = c.getClientProperty(documentBaseKey);
            if (base instanceof URL) {
                ((HTMLDocument)doc).setBase((URL)base);
            }
            Reader r = new StringReader(html);
            try {
                kit.read(r, doc, 0);
            } catch (Throwable e) {
            }
            ViewFactory f = kit.getViewFactory();
            View hview = f.create(doc.getDefaultRootElement());
            View v = new Renderer(c, f, hview);
            return v;
        }

        static BasicEditorKit getFactory() {
            if (basicHTMLFactory == null) {
                    basicHTMLViewFactory = new BasicHTMLViewFactory();
                basicHTMLFactory = new BasicEditorKit();
            }
            return basicHTMLFactory;
        }

        /**
         * The source of the html renderers
         */
        private static BasicEditorKit basicHTMLFactory;

        /**
         * Creates the Views that visually represent the model.
         */
        private static ViewFactory basicHTMLViewFactory;

        /**
         * Overrides to the default stylesheet.  Should consider
         * just creating a completely fresh stylesheet.
         */
        private static final String styleChanges =
            "p { margin-top: 0; margin-bottom: 0; margin-left: 0; margin-right: 0; text-decoration: underline }" +
            "body { margin-top: 0; margin-bottom: 0; margin-left: 0; margin-right: 0; text-decoration: underline }"+
            "font {text-decoration: underline}";

        static class BasicEditorKit extends HTMLEditorKit {
            /** Shared base style for all documents created by us use. */
            private static StyleSheet defaultStyles;

            /**
             * Overriden to return our own slimmed down style sheet.
             */
            @Override
            public StyleSheet getStyleSheet() {
                if (defaultStyles == null) {
                defaultStyles = new StyleSheet();
                StringReader r = new StringReader(styleChanges);
                try {
                    defaultStyles.loadRules(r, null);
                } catch (Throwable e) {
                    // don't want to die in static initialization...
                    // just display things wrong.
                }
                r.close();
                defaultStyles.addStyleSheet(super.getStyleSheet());
            }
            return defaultStyles;
        }

        /**
         * Sets the async policy to flush everything in one chunk, and
         * to not display unknown tags.
         */
        public Document createDefaultDocument(Font defaultFont,
                                                  Color foreground) {
            StyleSheet styles = getStyleSheet();
            StyleSheet ss = new StyleSheet();
            ss.addStyleSheet(styles);
            BasicDocument doc = new BasicDocument(ss, defaultFont, foreground);
            doc.setAsynchronousLoadPriority(Integer.MAX_VALUE);
            doc.setPreservesUnknownTags(false);
            return doc;
        }

            /**
             * Returns the ViewFactory that is used to make sure the Views don't
             * load in the background.
             */
            @Override
            public ViewFactory getViewFactory() {
                return basicHTMLViewFactory;
            }
        }

        /**
         * BasicHTMLViewFactory extends HTMLFactory to force images to be loaded
         * synchronously.
         */
        static class BasicHTMLViewFactory extends HTMLEditorKit.HTMLFactory {
            @Override
            public View create(Element elem) {
                View view = super.create(elem);

                if (view instanceof ImageView) {
                    ((ImageView)view).setLoadsSynchronously(true);
                }
                return view;
            }
        }

        /**
         * The subclass of HTMLDocument that is used as the model. getForeground
         * is overridden to return the foreground property from the Component this
         * was created for.
         */
        static class BasicDocument extends HTMLDocument {
        private static Class<?> clz;
        private static Method displayPropertiesToCSS;

        /** The host, that is where we are rendering. */
        // private JComponent host;
        // --------- 1.5 x 1.6 incompatibility handling ....
        static {
            String j5 = "com.sun.java.swing.SwingUtilities2";
            String j6 = "sun.swing.SwingUtilities2";
            try {
                // assume 1.6
                clz = Class.forName(j6);
            } catch (ClassNotFoundException e) {
                // or maybe not ..
                try {
                    clz = Class.forName(j5);
                } catch (ClassNotFoundException e1) {
                    throw new RuntimeException("Failed to find SwingUtilities2. Check the classpath.");
                }
            }
            try {
                displayPropertiesToCSS = clz.getMethod("displayPropertiesToCSS", new Class[] { Font.class, Color.class});
            } catch (Exception e) {
                throw new RuntimeException("Failed to use SwingUtilities2. Check the permissions and class version.");
            }
        }

        private static String displayPropertiesToCSS(Font f, Color c) {
            try {
                return (String) displayPropertiesToCSS.invoke(null, new Object[] { f, c });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        // --------- EO 1.5 x 1.6 incompatibility handling ....

        BasicDocument(StyleSheet s, Font defaultFont, Color foreground) {
            super(s);
            setPreservesUnknownTags(false);
                setFontAndColor(defaultFont, foreground);
        }

        /**
         * Sets the default font and default color. These are set by
         * adding a rule for the body that specifies the font and color.
         * This allows the html to override these should it wish to have
         * a custom font or color.
         */
        private void setFontAndColor(Font font, Color fg) {
                getStyleSheet().addRule(displayPropertiesToCSS(font,fg));
        }
    }

    /**
     * Root text view that acts as an HTML renderer.
     */
    static class Renderer extends View {

        Renderer(JComponent c, ViewFactory f, View v) {
                super(null);
            host = c;
            factory = f;
            view = v;
            view.setParent(this);
            // initially layout to the preferred size
            setSize(view.getPreferredSpan(X_AXIS), view.getPreferredSpan(Y_AXIS));
        }

        /**
         * Fetches the attributes to use when rendering.  At the root
         * level there are no attributes.  If an attribute is resolved
         * up the view hierarchy this is the end of the line.
         */
            @Override
            public AttributeSet getAttributes() {
            return null;
        }

        /**
         * Determines the preferred span for this view along an axis.
         *
         * @param axis may be either X_AXIS or Y_AXIS
         * @return the span the view would like to be rendered into.
         *         Typically the view is told to render into the span
         *         that is returned, although there is no guarantee.
         *         The parent may choose to resize or break the view.
         */
        @Override
        public float getPreferredSpan(int axis) {
            if (axis == X_AXIS) {
                // width currently laid out to
                return width;
            }
            return view.getPreferredSpan(axis);
        }

        /**
         * Determines the minimum span for this view along an axis.
         *
         * @param axis may be either X_AXIS or Y_AXIS
         * @return the span the view would like to be rendered into.
         *         Typically the view is told to render into the span
         *         that is returned, although there is no guarantee.
         *         The parent may choose to resize or break the view.
         */
        @Override
        public float getMinimumSpan(int axis) {
            return view.getMinimumSpan(axis);
        }

        /**
         * Determines the maximum span for this view along an axis.
         *
         * @param axis may be either X_AXIS or Y_AXIS
         * @return the span the view would like to be rendered into.
         *         Typically the view is told to render into the span
         *         that is returned, although there is no guarantee.
         *         The parent may choose to resize or break the view.
         */
        @Override
        public float getMaximumSpan(int axis) {
            return Integer.MAX_VALUE;
        }

        /**
         * Specifies that a preference has changed.
         * Child views can call this on the parent to indicate that
         * the preference has changed.  The root view routes this to
         * invalidate on the hosting component.
         * <p>
         * This can be called on a different thread from the
         * event dispatching thread and is basically unsafe to
         * propagate into the component.  To make this safe,
         * the operation is transferred over to the event dispatching
         * thread for completion.  It is a design goal that all view
         * methods be safe to call without concern for concurrency,
         * and this behavior helps make that true.
         *
         * @param child the child view
         * @param width true if the width preference has changed
         * @param height true if the height preference has changed
         */
        @Override
        public void preferenceChanged(View child, boolean width, boolean height) {
            host.revalidate();
            host.repaint();
        }

        /**
         * Determines the desired alignment for this view along an axis.
         *
         * @param axis may be either X_AXIS or Y_AXIS
         * @return the desired alignment, where 0.0 indicates the origin
         *     and 1.0 the full span away from the origin
         */
        @Override
        public float getAlignment(int axis) {
            return view.getAlignment(axis);
        }

        /**
         * Renders the view.
         *
         * @param g the graphics context
         * @param allocation the region to render into
         */
        @Override
        public void paint(Graphics g, Shape allocation) {
            Rectangle alloc = allocation.getBounds();
            view.setSize(alloc.width, alloc.height);
            view.paint(g, allocation);
        }

        /**
         * Sets the view parent.
         *
         * @param parent the parent view
         */
        @Override
        public void setParent(View parent) {
            throw new Error("Can't set parent on root view");
        }

        /**
         * Returns the number of views in this view.  Since
         * this view simply wraps the root of the view hierarchy
         * it has exactly one child.
         *
         * @return the number of views
         * @see #getView
         */
        @Override
        public int getViewCount() {
            return 1;
        }

        /**
         * Gets the n-th view in this container.
         *
         * @param n the number of the view to get
         * @return the view
         */
        @Override
        public View getView(int n) {
            return view;
        }

        /**
         * Provides a mapping from the document model coordinate space
         * to the coordinate space of the view mapped to it.
         *
         * @param pos the position to convert
         * @param a the allocated region to render into
         * @return the bounding box of the given position
         */
        @Override
        public Shape modelToView(int pos, Shape a, Position.Bias b) throws BadLocationException {
            return view.modelToView(pos, a, b);
        }

        /**
         * Provides a mapping from the document model coordinate space
         * to the coordinate space of the view mapped to it.
         *
         * @param p0 the position to convert >= 0
         * @param b0 the bias toward the previous character or the
         *  next character represented by p0, in case the
         *  position is a boundary of two views.
         * @param p1 the position to convert >= 0
         * @param b1 the bias toward the previous character or the
         *  next character represented by p1, in case the
         *  position is a boundary of two views.
         * @param a the allocated region to render into
         * @return the bounding box of the given position is returned
         * @exception BadLocationException  if the given position does
         *   not represent a valid location in the associated document
         * @exception IllegalArgumentException for an invalid bias argument
         * @see View#viewToModel
         */
        @Override
        public Shape modelToView(int p0, Position.Bias b0, int p1,
                     Position.Bias b1, Shape a) throws BadLocationException {
            return view.modelToView(p0, b0, p1, b1, a);
        }

        /**
         * Provides a mapping from the view coordinate space to the logical
         * coordinate space of the model.
         *
         * @param x x coordinate of the view location to convert
         * @param y y coordinate of the view location to convert
         * @param a the allocated region to render into
         * @return the location within the model that best represents the
         *    given point in the view
         */
        @Override
        public int viewToModel(float x, float y, Shape a, Position.Bias[] bias) {
            return view.viewToModel(x, y, a, bias);
        }

        /**
         * Returns the document model underlying the view.
         *
         * @return the model
         */
        @Override
        public Document getDocument() {
            return view.getDocument();
        }

        /**
         * Returns the starting offset into the model for this view.
         *
         * @return the starting offset
         */
        @Override
        public int getStartOffset() {
            return view.getStartOffset();
        }

        /**
         * Returns the ending offset into the model for this view.
         *
         * @return the ending offset
         */
        @Override
        public int getEndOffset() {
            return view.getEndOffset();
        }

        /**
         * Gets the element that this view is mapped to.
         *
         * @return the view
         */
        @Override
        public Element getElement() {
            return view.getElement();
        }

        /**
         * Sets the view size.
         *
         * @param width the width
         * @param height the height
         */
        @Override
        public void setSize(float width, float height) {
            this.width = (int) width;
            view.setSize(width, height);
        }

        /**
         * Fetches the container hosting the view.  This is useful for
         * things like scheduling a repaint, finding out the host
         * components font, etc.  The default implementation
         * of this is to forward the query to the parent view.
         *
         * @return the container
         */
        @Override
        public Container getContainer() {
            return host;
        }

        /**
         * Fetches the factory to be used for building the
         * various view fragments that make up the view that
         * represents the model.  This is what determines
         * how the model will be represented.  This is implemented
         * to fetch the factory provided by the associated
         * EditorKit.
         *
         * @return the factory
         */
        @Override
        public ViewFactory getViewFactory() {
            return factory;
        }

        private int width;
        private View view;
        private ViewFactory factory;
        private JComponent host;

        }
    }
}
