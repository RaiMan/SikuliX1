/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.jdesktop.swingx;

import java.applet.Applet;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Window;
import java.awt.event.HierarchyBoundsAdapter;
import java.awt.event.HierarchyEvent;
import java.awt.font.TextAttribute;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Reader;
import java.io.StringReader;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentEvent.ElementChange;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BoxView;
import javax.swing.text.ComponentView;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.IconView;
import javax.swing.text.LabelView;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.ParagraphView;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.WrappedPlainView;

import org.jdesktop.beans.JavaBean;
import org.jdesktop.swingx.painter.AbstractPainter;
import org.jdesktop.swingx.painter.Painter;

/**
 * <p>
 * A {@link javax.swing.JLabel} subclass which supports {@link org.jdesktop.swingx.painter.Painter}s, multi-line text,
 * and text rotation.
 * </p>
 *
 * <p>
 * Painter support consists of the <code>foregroundPainter</code> and <code>backgroundPainter</code> properties. The
 * <code>backgroundPainter</code> refers to a painter responsible for painting <i>beneath</i> the text and icon. This
 * painter, if set, will paint regardless of the <code>opaque</code> property. If the background painter does not
 * fully paint each pixel, then you should make sure the <code>opaque</code> property is set to false.
 * </p>
 *
 * <p>
 * The <code>foregroundPainter</code> is responsible for painting the icon and the text label. If no foregroundPainter
 * is specified, then the look and feel will paint the label. Note that if opaque is set to true and the look and feel
 * is rendering the foreground, then the foreground <i>may</i> paint over the background. Most look and feels will
 * paint a background when <code>opaque</code> is true. To avoid this behavior, set <code>opaque</code> to false.
 * </p>
 *
 * <p>
 * Since JXLabel is not opaque by default (<code>isOpaque()</code> returns false), neither of these problems
 * typically present themselves.
 * </p>
 *
 * <p>
 * Multi-line text is enabled via the <code>lineWrap</code> property. Simply set it to true. By default, line wrapping
 * occurs on word boundaries.
 * </p>
 *
 * <p>
 * The text (actually, the entire foreground and background) of the JXLabel may be rotated. Set the
 * <code>rotation</code> property to specify what the rotation should be. Specify rotation angle in radian units.
 * </p>
 *
 * @author joshua.marinacci@sun.com
 * @author rbair
 * @author rah
 * @author mario_cesar
 */
@JavaBean
public class JXLabel extends JLabel implements BackgroundPaintable {

    /**
     * Text alignment enums. Controls alignment of the text when line wrapping is enabled.
     */
    public enum TextAlignment implements IValue {
        LEFT(StyleConstants.ALIGN_LEFT), CENTER(StyleConstants.ALIGN_CENTER), RIGHT(StyleConstants.ALIGN_RIGHT), JUSTIFY(StyleConstants.ALIGN_JUSTIFIED);

        private int value;
        private TextAlignment(int val) {
            value = val;
        }

        @Override
        public int getValue() {
            return value;
        }

    }

    protected interface IValue {
        int getValue();
    }

    // textOrientation value declarations...
    public static final double NORMAL = 0;

    public static final double INVERTED = Math.PI;

    public static final double VERTICAL_LEFT = 3 * Math.PI / 2;

    public static final double VERTICAL_RIGHT = Math.PI / 2;

    private double textRotation = NORMAL;

    private boolean painting = false;

    private Painter foregroundPainter;

    private Painter backgroundPainter;

    private boolean multiLine;

    private int pWidth;

    private int pHeight;

    // using reverse logic ... some methods causing re-flow of text are called from super constructor, but private variables are initialized only after call to super so have to rely on default for boolean being false
    private boolean dontIgnoreRepaint = false;

    private int occupiedWidth;

    private static final String oldRendererKey = "was" + BasicHTML.propertyKey;

//    private static final Logger log = Logger.getAnonymousLogger();
//    static {
//        log.setLevel(Level.FINEST);
//    }

    /**
     * Create a new JXLabel. This has the same semantics as creating a new JLabel.
     */
    public JXLabel() {
        super();
        initPainterSupport();
        initLineWrapSupport();
    }

    /**
     * Creates new JXLabel with given icon.
     * @param image the icon to set.
     */
    public JXLabel(Icon image) {
        super(image);
        initPainterSupport();
        initLineWrapSupport();
    }

    /**
     * Creates new JXLabel with given icon and alignment.
     * @param image the icon to set.
     * @param horizontalAlignment the text alignment.
     */
    public JXLabel(Icon image, int horizontalAlignment) {
        super(image, horizontalAlignment);
        initPainterSupport();
        initLineWrapSupport();
    }

    /**
     * Create a new JXLabel with the given text as the text for the label. This is shorthand for:
     *
     * <pre><code>
     * JXLabel label = new JXLabel();
     * label.setText(&quot;Some Text&quot;);
     * </code></pre>
     *
     * @param text the text to set.
     */
    public JXLabel(String text) {
        super(text);
        initPainterSupport();
        initLineWrapSupport();
    }

    /**
     * Creates new JXLabel with given text, icon and alignment.
     * @param text the test to set.
     * @param image the icon to set.
     * @param horizontalAlignment the text alignment relative to the icon.
     */
    public JXLabel(String text, Icon image, int horizontalAlignment) {
        super(text, image, horizontalAlignment);
        initPainterSupport();
        initLineWrapSupport();
    }

    /**
     * Creates new JXLabel with given text and alignment.
     * @param text the test to set.
     * @param horizontalAlignment the text alignment.
     */
    public JXLabel(String text, int horizontalAlignment) {
        super(text, horizontalAlignment);
        initPainterSupport();
        initLineWrapSupport();
    }

    private void initPainterSupport() {
        foregroundPainter = new AbstractPainter<JXLabel>() {
            @Override
            protected void doPaint(Graphics2D g, JXLabel label, int width, int height) {
                Insets i = getInsets();
                g = (Graphics2D) g.create(-i.left, -i.top, width, height);

                try {
                    label.paint(g);
                } finally {
                    g.dispose();
                }
            }
            //if any of the state of the JButton that affects the foreground has changed,
            //then I must clear the cache. This is really hard to get right, there are
            //bound to be bugs. An alternative is to NEVER cache.
            @Override
            protected boolean shouldUseCache() {
                return false;
            }

            @Override
            public boolean equals(Object obj) {
                return obj != null && this.getClass().equals(obj.getClass());
            }

        };
        ((AbstractPainter<?>) foregroundPainter).setAntialiasing(false);
    }

    /**
     * Helper method for initializing multi line support.
     */
    private void initLineWrapSupport() {
        addPropertyChangeListener(new MultiLineSupport());
        // FYI: no more listening for componentResized. Those events are delivered out
        // of order and without old values are meaningless and forcing us to react when
        // not necessary. Instead overriding reshape() ensures we have control over old AND new size.
        addHierarchyBoundsListener(new HierarchyBoundsAdapter() {
            @Override
            public void ancestorResized(HierarchyEvent e) {
                // if one of the parents is viewport, resized events will not be propagated down unless viewport is changing visibility of scrollbars.
                // To make sure Label is able to re-wrap text when viewport size changes, initiate re-wrapping here by changing size of view
                if (e.getChanged() instanceof JViewport) {
                    Rectangle viewportBounds = e.getChanged().getBounds();
                    if (viewportBounds.getWidth() < getWidth()) {
                        View view = getWrappingView();
                        if (view != null) {
                            view.setSize(viewportBounds.width, viewportBounds.height);
                        }
                    }
                }
            }});
    }

    /**
     * Returns the current foregroundPainter. This is a bound property. By default the foregroundPainter will be an
     * internal painter which executes the standard painting code (paintComponent()).
     *
     * @return the current foreground painter.
     */
    public final Painter getForegroundPainter() {
        return foregroundPainter;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void reshape(int x, int y, int w, int h) {
        int oldH = getHeight();
        super.reshape(x, y, w, h);
        if (!isLineWrap()) {
            return;
        }
        if (oldH == 0) {
            return;
        }
        if (w > getVisibleRect().width) {
            w = getVisibleRect().width;
        }
        View view = (View) getClientProperty(BasicHTML.propertyKey);
        if (view != null && view instanceof Renderer) {
            view.setSize(w - occupiedWidth, h);
        }
    }

    /**
     * Sets a new foregroundPainter on the label. This will replace the existing foreground painter. Existing painters
     * can be wrapped by using a CompoundPainter.
     *
     * @param painter
     */
    public void setForegroundPainter(Painter painter) {
        Painter old = this.getForegroundPainter();
        if (painter == null) {
            //restore default painter
            initPainterSupport();
        } else {
            this.foregroundPainter = painter;
        }
        firePropertyChange("foregroundPainter", old, getForegroundPainter());
        repaint();
    }

    /**
     * Sets a Painter to use to paint the background of this component By default there is already a single painter
     * installed which draws the normal background for this component according to the current Look and Feel. Calling
     * <CODE>setBackgroundPainter</CODE> will replace that existing painter.
     *
     * @param p the new painter
     * @see #getBackgroundPainter()
     */
    @Override
    public void setBackgroundPainter(Painter p) {
        Painter old = getBackgroundPainter();
        backgroundPainter = p;
        firePropertyChange("backgroundPainter", old, getBackgroundPainter());
        repaint();
    }

    /**
     * Returns the current background painter. The default value of this property is a painter which draws the normal
     * JPanel background according to the current look and feel.
     *
     * @return the current painter
     * @see #setBackgroundPainter(Painter)
     */
    @Override
    public final Painter getBackgroundPainter() {
        return backgroundPainter;
    }

    /**
     * Gets current value of text rotation in rads.
     *
     * @return a double representing the current rotation of the text
     * @see #setTextRotation(double)
     */
    public double getTextRotation() {
        return textRotation;
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension size = super.getPreferredSize();
        //if (true) return size;
        if (isPreferredSizeSet()) {
            //log.fine("ret 0");
            return size;
        } else if (this.textRotation != NORMAL) {
            // #swingx-680 change the preferred size when rotation is set ... ideally this would be solved in the LabelUI rather then here
            double theta = getTextRotation();
            size.setSize(rotateWidth(size, theta), rotateHeight(size,
            theta));
        } else {
            // #swingx-780 preferred size is not set properly when parent container doesn't enforce the width
            View view = getWrappingView();
            if (view == null) {
                if (isLineWrap() && !MultiLineSupport.isHTML(getText())) {
                    getMultiLineSupport();
                    // view might get lost on LAF change ...
                    putClientProperty(BasicHTML.propertyKey,
                            MultiLineSupport.createView(this));
                    view = (View) getClientProperty(BasicHTML.propertyKey);
                } else {
                    return size;
                }
            }
            Insets insets = getInsets();
            int dx = insets.left + insets.right;
            int dy = insets.top + insets.bottom;
            //log.fine("INSETS:" + insets);
            //log.fine("BORDER:" + this.getBorder());
            Rectangle textR = new Rectangle();
            Rectangle viewR = new Rectangle();
            textR.x = textR.y = textR.width = textR.height = 0;
            viewR.x = dx;
            viewR.y = dy;
            viewR.width = viewR.height = Short.MAX_VALUE;
            // layout label
            // 1) icon
            Rectangle iconR = calculateIconRect();
            // 2) init textR
            boolean textIsEmpty = (getText() == null) || getText().equals("");
            int lsb = 0;
            /* Unless both text and icon are non-null, we effectively ignore
             * the value of textIconGap.
             */
            int gap;
            if (textIsEmpty) {
                textR.width = textR.height = 0;
                gap = 0;
            }
            else {
                int availTextWidth;
                gap = (iconR.width == 0) ? 0 : getIconTextGap();

                occupiedWidth = dx + iconR.width + gap;
                Object parent = getParent();
                if (parent != null && (parent instanceof JPanel)) {
                    JPanel panel = ((JPanel) parent);
                    Border b = panel.getBorder();
                    if (b != null) {
                        Insets in = b.getBorderInsets(panel);
                        occupiedWidth += in.left + in.right;
                    }
                }
                if (getHorizontalTextPosition() == CENTER) {
                    availTextWidth = viewR.width;
                }
                else {
                    availTextWidth = viewR.width - (iconR.width + gap);
                }
                float xPrefSpan = view.getPreferredSpan(View.X_AXIS);
                //log.fine("atw:" + availTextWidth + ", vps:" + xPrefSpan);
                textR.width = Math.min(availTextWidth, (int) xPrefSpan);
                if (maxLineSpan > 0) {
                    textR.width = Math.min(textR.width, maxLineSpan);
                    if (xPrefSpan > maxLineSpan) {
                        view.setSize(maxLineSpan, textR.height);
                    }
                }
                textR.height = (int) view.getPreferredSpan(View.Y_AXIS);
                if (textR.height == 0) {
                    textR.height = getFont().getSize();
                }
                //log.fine("atw:" + availTextWidth + ", vps:" + xPrefSpan + ", h:" + textR.height);

            }
            // 3) set text xy based on h/v text pos
            if (getVerticalTextPosition() == TOP) {
                if (getHorizontalTextPosition() != CENTER) {
                    textR.y = 0;
                }
                else {
                    textR.y = -(textR.height + gap);
                }
            }
            else if (getVerticalTextPosition() == CENTER) {
                textR.y = (iconR.height / 2) - (textR.height / 2);
            }
            else { // (verticalTextPosition == BOTTOM)
                if (getVerticalTextPosition() != CENTER) {
                    textR.y = iconR.height - textR.height;
                }
                else {
                    textR.y = (iconR.height + gap);
                }
            }

            if (getHorizontalTextPosition() == LEFT) {
                textR.x = -(textR.width + gap);
            }
            else if (getHorizontalTextPosition() == CENTER) {
                textR.x = (iconR.width / 2) - (textR.width / 2);
            }
            else { // (horizontalTextPosition == RIGHT)
                textR.x = (iconR.width + gap);
            }

            // 4) shift label around based on its alignment
            int labelR_x = Math.min(iconR.x, textR.x);
            int labelR_width = Math.max(iconR.x + iconR.width,
                                        textR.x + textR.width) - labelR_x;
            int labelR_y = Math.min(iconR.y, textR.y);
            int labelR_height = Math.max(iconR.y + iconR.height,
                                         textR.y + textR.height) - labelR_y;

            int dax, day;

            if (getVerticalAlignment() == TOP) {
                day = viewR.y - labelR_y;
            }
            else if (getVerticalAlignment() == CENTER) {
                day = (viewR.y + (viewR.height / 2)) - (labelR_y + (labelR_height / 2));
            }
            else { // (verticalAlignment == BOTTOM)
                day = (viewR.y + viewR.height) - (labelR_y + labelR_height);
            }

            if (getHorizontalAlignment() == LEFT) {
                dax = viewR.x - labelR_x;
            }
            else if (getHorizontalAlignment() == RIGHT) {
                dax = (viewR.x + viewR.width) - (labelR_x + labelR_width);
            }
            else { // (horizontalAlignment == CENTER)
                dax = (viewR.x + (viewR.width / 2)) -
                     (labelR_x + (labelR_width / 2));
            }

            textR.x += dax;
            textR.y += day;

            iconR.x += dax;
            iconR.y += day;

            if (lsb < 0) {
                // lsb is negative. Shift the x location so that the text is
                // visually drawn at the right location.
                textR.x -= lsb;
            }
            // EO layout label

            int x1 = Math.min(iconR.x, textR.x);
            int x2 = Math.max(iconR.x + iconR.width, textR.x + textR.width);
            int y1 = Math.min(iconR.y, textR.y);
            int y2 = Math.max(iconR.y + iconR.height, textR.y + textR.height);
            Dimension rv = new Dimension(x2 - x1, y2 - y1);

            rv.width += dx;
            rv.height += dy;
            //log.fine("returning: " + rv);
            return rv;
        }
        //log.fine("ret 3");
        return size;
    }

    private View getWrappingView() {
        if (super.getTopLevelAncestor() == null) {
            return null;
        }
        View view = (View) getClientProperty(BasicHTML.propertyKey);
        if (!(view instanceof Renderer)) {
            return null;
        }
        return view;
    }

    private Container getViewport() {
        for(Container p = this; p != null; p = p.getParent()) {
            if(p instanceof Window || p instanceof Applet || p instanceof JViewport) {
                return p;
            }
        }
        return null;
    }

    private Rectangle calculateIconRect() {
        Rectangle iconR = new Rectangle();
        Icon icon = isEnabled() ? getIcon() : getDisabledIcon();
        iconR.x = iconR.y = iconR.width = iconR.height = 0;
        if (icon != null) {
            iconR.width = icon.getIconWidth();
            iconR.height = icon.getIconHeight();
        }
        else {
            iconR.width = iconR.height = 0;
        }
        return iconR;
    }

    public int getMaxLineSpan() {
        return maxLineSpan ;
    }

    public void setMaxLineSpan(int maxLineSpan) {
            int old = getMaxLineSpan();
            this.maxLineSpan = maxLineSpan;
            firePropertyChange("maxLineSpan", old, getMaxLineSpan());
    }

    private static int rotateWidth(Dimension size, double theta) {
        return (int)Math.round(size.width*Math.abs(Math.cos(theta)) +
        size.height*Math.abs(Math.sin(theta)));
    }

    private static int rotateHeight(Dimension size, double theta) {
        return (int)Math.round(size.width*Math.abs(Math.sin(theta)) +
        size.height*Math.abs(Math.cos(theta)));
    }

    /**
     * Sets new value for text rotation. The value can be anything in range <0,2PI>. Note that although property name
     * suggests only text rotation, the whole foreground painter is rotated in fact. Due to various reasons it is
     * strongly discouraged to access any size related properties of the label from other threads then EDT when this
     * property is set.
     *
     * @param textOrientation Value for text rotation in range <0,2PI>
     * @see #getTextRotation()
     */
    public void setTextRotation(double textOrientation) {
        double old = getTextRotation();
        this.textRotation = textOrientation;
        if (old != getTextRotation()) {
            firePropertyChange("textRotation", old, getTextRotation());
        }
        repaint();
    }

    /**
     * Enables line wrapping support for plain text. By default this support is disabled to mimic default of the JLabel.
     * Value of this property has no effect on HTML text.
     *
     * @param b the new value
     */
    public void setLineWrap(boolean b) {
        boolean old = isLineWrap();
        this.multiLine = b;
        if (isLineWrap() != old) {
            firePropertyChange("lineWrap", old, isLineWrap());
            if (getForegroundPainter() != null) {
                // XXX There is a bug here. In order to make painter work with this, caching has to be disabled
                ((AbstractPainter) getForegroundPainter()).setCacheable(!b);
            }
            //repaint();
        }
    }

    /**
     * Returns the current status of line wrap support. The default value of this property is false to mimic default
     * JLabel behavior. Value of this property has no effect on HTML text.
     *
     * @return the current multiple line splitting status
     */
    public boolean isLineWrap() {
        return this.multiLine;
    }

    private boolean paintBorderInsets = true;

    private int maxLineSpan = -1;

    public boolean painted;

    private TextAlignment textAlignment = TextAlignment.LEFT;

    /**
     * Gets current text wrapping style.
     *
     * @return the text alignment for this label
     */
    public TextAlignment getTextAlignment() {
        return textAlignment;
    }

    /**
     * Sets style of wrapping the text.
     * @see TextAlignment for accepted values.
     * @param alignment
     */
    public void setTextAlignment(TextAlignment alignment) {
        TextAlignment old = getTextAlignment();
        this.textAlignment = alignment;
        firePropertyChange("textAlignment", old, getTextAlignment());
    }

    /**
     * Returns true if the background painter should paint where the border is
     * or false if it should only paint inside the border. This property is
     * true by default. This property affects the width, height,
     * and initial transform passed to the background painter.
     * @return current value of the paintBorderInsets property
     */
    @Override
    public boolean isPaintBorderInsets() {
        return paintBorderInsets;
    }

    @Override
    public boolean isOpaque() {
        return painting ? false : super.isOpaque();
    }

    /**
     * Sets the paintBorderInsets property.
     * Set to true if the background painter should paint where the border is
     * or false if it should only paint inside the border. This property is true by default.
     * This property affects the width, height,
     * and initial transform passed to the background painter.
     *
     * This is a bound property.
     * @param paintBorderInsets new value of the paintBorderInsets property
     */
    @Override
    public void setPaintBorderInsets(boolean paintBorderInsets) {
        boolean old = this.isPaintBorderInsets();
        this.paintBorderInsets = paintBorderInsets;
        firePropertyChange("paintBorderInsets", old, isPaintBorderInsets());
    }

    /**
     * @param g graphics to paint on
     */
    @Override
    protected void paintComponent(Graphics g) {
        //log.fine("in");
        // resizing the text view causes recursive callback to the paint down the road. In order to prevent such
        // computationally intensive series of repaints every call to paint is skipped while top most call is being
        // executed.
//        if (!dontIgnoreRepaint) {
//            return;
//        }
        painted = true;
        if (painting || backgroundPainter == null && foregroundPainter == null) {
            super.paintComponent(g);
        } else {
            pWidth = getWidth();
            pHeight = getHeight();
            if (backgroundPainter != null) {
                Graphics2D tmp = (Graphics2D) g.create();

                try {
                    SwingXUtilities.paintBackground(this, tmp);
                } finally {
                    tmp.dispose();
                }
            }
            if (foregroundPainter != null) {
                Insets i = getInsets();
                pWidth = getWidth() - i.left - i.right;
                pHeight = getHeight() - i.top - i.bottom;

                Point2D tPoint = calculateT();
                double wx = Math.sin(textRotation) * tPoint.getY() + Math.cos(textRotation) * tPoint.getX();
                double wy = Math.sin(textRotation) * tPoint.getX() + Math.cos(textRotation) * tPoint.getY();
                double x = (getWidth() - wx) / 2 + Math.sin(textRotation) * tPoint.getY();
                double y = (getHeight() - wy) / 2;
                Graphics2D tmp = (Graphics2D) g.create();
                if (i != null) {
                    tmp.translate(i.left + x, i.top + y);
                } else {
                        tmp.translate(x, y);
                }
                tmp.rotate(textRotation);

                painting = true;
                // uncomment to highlight text area
                // Color c = g2.getColor();
                // g2.setColor(Color.RED);
                // g2.fillRect(0, 0, getWidth(), getHeight());
                // g2.setColor(c);
                //log.fine("PW:" + pWidth + ", PH:" + pHeight);
                foregroundPainter.paint(tmp, this, pWidth, pHeight);
                tmp.dispose();
                painting = false;
                pWidth = 0;
                pHeight = 0;
            }
        }
    }

    private Point2D calculateT() {
        double tx = getWidth();
        double ty = getHeight();

        // orthogonal cases are most likely the most often used ones, so give them preferential treatment.
        if ((textRotation > 4.697 && textRotation < 4.727) || (textRotation > 1.555 && textRotation < 1.585)) {
            // vertical
            int tmp = pHeight;
            pHeight = pWidth;
            pWidth = tmp;
            tx = pWidth;
            ty = pHeight;
        } else if ((textRotation > -0.015 && textRotation < 0.015)
                || (textRotation > 3.140 && textRotation < 3.1430)) {
            // normal & inverted
            pHeight = getHeight();
            pWidth = getWidth();
        } else {
            // the rest of it. Calculate best rectangle that fits the bounds. "Best" is considered one that
            // allows whole text to fit in, spanned on preferred axis (X). If that doesn't work, fit the text
            // inside square with diagonal equal min(height, width) (Should be the largest rectangular area that
            // fits in, math proof available upon request)

            dontIgnoreRepaint = false;
            double square = Math.min(getHeight(), getWidth()) * Math.cos(Math.PI / 4d);

            View v = (View) getClientProperty(BasicHTML.propertyKey);
            if (v == null) {
                // no html and no wrapline enabled means no view
                // ... find another way to figure out the heigh
                ty = getFontMetrics(getFont()).getHeight();
                double cw = (getWidth() - Math.abs(ty * Math.sin(textRotation)))
                        / Math.abs(Math.cos(textRotation));
                double ch = (getHeight() - Math.abs(ty * Math.cos(textRotation)))
                        / Math.abs(Math.sin(textRotation));
                // min of whichever is above 0 (!!! no min of abs values)
                tx = cw < 0 ? ch : ch > 0 ? Math.min(cw, ch) : cw;
            } else {
                float w = v.getPreferredSpan(View.X_AXIS);
                float h = v.getPreferredSpan(View.Y_AXIS);
                double c = w;
                double alpha = textRotation;// % (Math.PI/2d);
                boolean ready = false;
                while (!ready) {
                    // shorten the view len until line break is forced
                    while (h == v.getPreferredSpan(View.Y_AXIS)) {
                        w -= 10;
                        v.setSize(w, h);
                    }
                    if (w < square || h > square) {
                        // text is too long to fit no matter what. Revert shape to square since that is the
                        // best option (1st derivation for area size of rotated rect in rect is equal 0 for
                        // rotated rect with equal w and h i.e. for square)
                        w = h = (float) square;
                        // set view height to something big to prevent recursive resize/repaint requests
                        v.setSize(w, 100000);
                        break;
                    }
                    // calc avail width with new view height
                    h = v.getPreferredSpan(View.Y_AXIS);
                    double cw = (getWidth() - Math.abs(h * Math.sin(alpha))) / Math.abs(Math.cos(alpha));
                    double ch = (getHeight() - Math.abs(h * Math.cos(alpha))) / Math.abs(Math.sin(alpha));
                    // min of whichever is above 0 (!!! no min of abs values)
                    c = cw < 0 ? ch : ch > 0 ? Math.min(cw, ch) : cw;
                    // make it one pix smaller to ensure text is not cut on the left
                    c--;
                    if (c > w) {
                        v.setSize((float) c, 10 * h);
                        ready = true;
                    } else {
                        v.setSize((float) c, 10 * h);
                        if (v.getPreferredSpan(View.Y_AXIS) > h) {
                            // set size back to figure out new line break and height after
                            v.setSize(w, 10 * h);
                        } else {
                            w = (float) c;
                            ready = true;
                        }
                    }
                }

                tx = Math.floor(w);// xxx: watch out for first letter on each line missing some pixs!!!
                ty = h;
            }
            pWidth = (int) tx;
            pHeight = (int) ty;
            dontIgnoreRepaint = true;
        }
                return new Point2D.Double(tx,ty);
        }

        @Override
    public void repaint() {
        if (!dontIgnoreRepaint) {
            return;
        }
        super.repaint();
    }

    @Override
    public void repaint(int x, int y, int width, int height) {
        if (!dontIgnoreRepaint) {
            return;
        }
        super.repaint(x, y, width, height);
    }

    @Override
    public void repaint(long tm) {
        if (!dontIgnoreRepaint) {
            return;
        }
        super.repaint(tm);
    }

    @Override
    public void repaint(long tm, int x, int y, int width, int height) {
        if (!dontIgnoreRepaint) {
            return;
        }
        super.repaint(tm, x, y, width, height);
    }

    // ----------------------------------------------------------
    // textOrientation magic
    @Override
    public int getHeight() {
        int retValue = super.getHeight();
        if (painting) {
            retValue = pHeight;
        }
        return retValue;
    }

    @Override
    public int getWidth() {
        int retValue = super.getWidth();
        if (painting) {
            retValue = pWidth;
        }
        return retValue;
    }

    protected MultiLineSupport getMultiLineSupport() {
        return new MultiLineSupport();
    }
    // ----------------------------------------------------------
    // WARNING:
    // Anything below this line is related to lineWrap support and can be safely ignored unless
    // in need to mess around with the implementation details.
    // ----------------------------------------------------------
    // FYI: This class doesn't reinvent line wrapping. Instead it makes use of existing support
    // made for JTextComponent/JEditorPane.
    // All the classes below named Alter* are verbatim copy of swing.text.* classes made to
    // overcome package visibility of some of the code. All other classes here, when their name
    // matches corresponding class from swing.text.* package are copy of the class with removed
    // support for highlighting selection. In case this is ever merged back to JDK all of this
    // can be safely removed as long as corresponding swing.text.* classes make appropriate checks
    // before casting JComponent into JTextComponent to find out selected region since
    // JLabel/JXLabel does not support selection of the text.

    public static class MultiLineSupport implements PropertyChangeListener {

        private static final String HTML = "<html>";

        private static ViewFactory basicViewFactory;

        private static BasicEditorKit basicFactory;

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String name = evt.getPropertyName();
            JXLabel src = (JXLabel) evt.getSource();
            if ("ancestor".equals(name)) {
                src.dontIgnoreRepaint = true;
            }
            if (src.isLineWrap()) {
                if ("font".equals(name) || "foreground".equals(name) || "maxLineSpan".equals(name) || "textAlignment".equals(name) || "icon".equals(name) || "iconTextGap".equals(name)) {
                    if (evt.getOldValue() != null && !isHTML(src.getText())) {
                        updateRenderer(src);
                    }
                } else if ("text".equals(name)) {
                    if (isHTML((String) evt.getOldValue()) && evt.getNewValue() != null
                            && !isHTML((String) evt.getNewValue())) {
                        // was html , but is not
                        if (src.getClientProperty(oldRendererKey) == null
                                && src.getClientProperty(BasicHTML.propertyKey) != null) {
                            src.putClientProperty(oldRendererKey, src.getClientProperty(BasicHTML.propertyKey));
                        }
                        src.putClientProperty(BasicHTML.propertyKey, createView(src));
                    } else if (!isHTML((String) evt.getOldValue()) && evt.getNewValue() != null
                            && !isHTML((String) evt.getNewValue())) {
                        // wasn't html and isn't
                        updateRenderer(src);
                    } else {
                        // either was html and is html or wasn't html, but is html
                        restoreHtmlRenderer(src);
                    }
                } else if ("lineWrap".equals(name) && !isHTML(src.getText())) {
                    src.putClientProperty(BasicHTML.propertyKey, createView(src));
                }
            } else if ("lineWrap".equals(name) && !((Boolean)evt.getNewValue())) {
                restoreHtmlRenderer(src);
            }
        }

        private static void restoreHtmlRenderer(JXLabel src) {
            Object current = src.getClientProperty(BasicHTML.propertyKey);
            if (current == null || current instanceof Renderer) {
                src.putClientProperty(BasicHTML.propertyKey, src.getClientProperty(oldRendererKey));
            }
        }

        private static boolean isHTML(String s) {
            return s != null && s.toLowerCase().startsWith(HTML);
        }

        public static View createView(JXLabel c) {
            BasicEditorKit kit = getFactory();
            float rightIndent = 0;
            if (c.getIcon() != null && c.getHorizontalTextPosition() != SwingConstants.CENTER) {
                rightIndent = c.getIcon().getIconWidth() + c.getIconTextGap();
            }
            Document doc = kit.createDefaultDocument(c.getFont(), c.getForeground(), c.getTextAlignment(), rightIndent);
            Reader r = new StringReader(c.getText() == null ? "" : c.getText());
            try {
                kit.read(r, doc, 0);
            } catch (Throwable e) {
            }
            ViewFactory f = kit.getViewFactory();
            View hview = f.create(doc.getDefaultRootElement());
            View v = new Renderer(c, f, hview, true);
            return v;
        }

        public static void updateRenderer(JXLabel c) {
            View value = null;
            View oldValue = (View) c.getClientProperty(BasicHTML.propertyKey);
            if (oldValue == null || oldValue instanceof Renderer) {
                value = createView(c);
            }
            if (value != oldValue && oldValue != null) {
                for (int i = 0; i < oldValue.getViewCount(); i++) {
                    oldValue.getView(i).setParent(null);
                }
            }
            c.putClientProperty(BasicHTML.propertyKey, value);
        }

        private static BasicEditorKit getFactory() {
            if (basicFactory == null) {
                basicViewFactory = new BasicViewFactory();
                basicFactory = new BasicEditorKit();
            }
            return basicFactory;
        }

        private static class BasicEditorKit extends StyledEditorKit {
            public Document createDefaultDocument(Font defaultFont, Color foreground, TextAlignment textAlignment, float rightIndent) {
                BasicDocument doc = new BasicDocument(defaultFont, foreground, textAlignment, rightIndent);
                doc.setAsynchronousLoadPriority(Integer.MAX_VALUE);
                return doc;
            }

            @Override
            public ViewFactory getViewFactory() {
                return basicViewFactory;
            }
        }
    }

    private static class BasicViewFactory implements ViewFactory {
        @Override
        public View create(Element elem) {

            String kind = elem.getName();
            View view = null;
            if (kind == null) {
                // default to text display
                view = new LabelView(elem);
            } else if (kind.equals(AbstractDocument.ContentElementName)) {
                view = new LabelView(elem);
            } else if (kind.equals(AbstractDocument.ParagraphElementName)) {
                view = new ParagraphView(elem);
            } else if (kind.equals(AbstractDocument.SectionElementName)) {
                view = new BoxView(elem, View.Y_AXIS);
            } else if (kind.equals(StyleConstants.ComponentElementName)) {
                view = new ComponentView(elem);
            } else if (kind.equals(StyleConstants.IconElementName)) {
                view = new IconView(elem);
            }
            return view;
        }
    }

    static class BasicDocument extends DefaultStyledDocument {
        BasicDocument(Font defaultFont, Color foreground, TextAlignment textAlignment, float rightIndent) {
            setFontAndColor(defaultFont, foreground);

            MutableAttributeSet attr = new SimpleAttributeSet();
            StyleConstants.setAlignment(attr, textAlignment.getValue());
            getStyle("default").addAttributes(attr);

            attr = new SimpleAttributeSet();
            StyleConstants.setRightIndent(attr, rightIndent);
            getStyle("default").addAttributes(attr);
        }

        private void setFontAndColor(Font font, Color fg) {
            if (fg != null) {

                MutableAttributeSet attr = new SimpleAttributeSet();
                StyleConstants.setForeground(attr, fg);
                getStyle("default").addAttributes(attr);
            }

            if (font != null) {
                MutableAttributeSet attr = new SimpleAttributeSet();
                StyleConstants.setFontFamily(attr, font.getFamily());
                getStyle("default").addAttributes(attr);

                attr = new SimpleAttributeSet();
                StyleConstants.setFontSize(attr, font.getSize());
                getStyle("default").addAttributes(attr);

                attr = new SimpleAttributeSet();
                StyleConstants.setBold(attr, font.isBold());
                getStyle("default").addAttributes(attr);

                attr = new SimpleAttributeSet();
                StyleConstants.setItalic(attr, font.isItalic());
                getStyle("default").addAttributes(attr);

                attr = new SimpleAttributeSet();
                Object underline = font.getAttributes().get(TextAttribute.UNDERLINE);
                boolean canUnderline = underline instanceof Integer && (Integer) underline != -1;
                StyleConstants.setUnderline(attr,  canUnderline);
                getStyle("default").addAttributes(attr);
            }

            MutableAttributeSet attr = new SimpleAttributeSet();
            StyleConstants.setSpaceAbove(attr, 0f);
            getStyle("default").addAttributes(attr);

        }
    }

    /**
     * Root text view that acts as an renderer.
     */
    static class Renderer extends WrappedPlainView {

        JXLabel host;

        boolean invalidated = false;

        private float width;

        private float height;

        Renderer(JXLabel c, ViewFactory f, View v, boolean wordWrap) {
            super(null, wordWrap);
            factory = f;
            view = v;
            view.setParent(this);
            host = c;
            //log.fine("vir: " +  host.getVisibleRect());
            int w;
            if (host.getVisibleRect().width == 0) {
                invalidated = true;
                return;
            } else {
                w = host.getVisibleRect().width;
            }
            //log.fine("w:" + w);
            // initially layout to the preferred size
            //setSize(c.getMaxLineSpan() > -1 ? c.getMaxLineSpan() : view.getPreferredSpan(X_AXIS), view.getPreferredSpan(Y_AXIS));
            setSize(c.getMaxLineSpan() > -1 ? c.getMaxLineSpan() : w, host.getVisibleRect().height);
        }

        @Override
        protected void updateLayout(ElementChange ec, DocumentEvent e, Shape a) {
            if ( (a != null)) {
                // should damage more intelligently
                preferenceChanged(null, true, true);
                Container host = getContainer();
                if (host != null) {
                    host.repaint();
                }
            }
        }

        @Override
        public void preferenceChanged(View child, boolean width, boolean height) {
            if (host != null && host.painted) {
                host.revalidate();
                host.repaint();
            }
        }

        /**
         * Fetches the attributes to use when rendering. At the root level there are no attributes. If an attribute is
         * resolved up the view hierarchy this is the end of the line.
         */
        @Override
        public AttributeSet getAttributes() {
            return null;
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
            //log.fine("aloc:" + alloc + "::" + host.getVisibleRect() + "::" + host.getBounds());
            //view.setSize(alloc.width, alloc.height);
            //this.width = alloc.width;
            //this.height = alloc.height;
            if (g.getClipBounds() == null) {
                g.setClip(alloc);
                view.paint(g, allocation);
                g.setClip(null);
            } else {
                //g.translate(alloc.x, alloc.y);
                view.paint(g, allocation);
                //g.translate(-alloc.x, -alloc.y);
            }
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
         * Returns the number of views in this view. Since this view simply wraps the root of the view hierarchy it has
         * exactly one child.
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
         * Returns the document model underlying the view.
         *
         * @return the model
         */
        @Override
        public Document getDocument() {
            return view == null ? null : view.getDocument();
        }

        /**
         * Sets the view size.
         *
         * @param width the width
         * @param height the height
         */
        @Override
        public void setSize(float width, float height) {
            if (host.maxLineSpan > 0) {
                width = Math.min(width, host.maxLineSpan);
            }
            if (width == this.width && height == this.height) {
                return;
            }
            this.width = (int) width;
            this.height = (int) height;
            view.setSize(width, height == 0 ? Short.MAX_VALUE : height);
            if (this.height == 0) {
                this.height = view.getPreferredSpan(View.Y_AXIS);
            }
        }

        @Override
        public float getPreferredSpan(int axis) {
            if (axis == X_AXIS) {
                //log.fine("inv: " + invalidated + ", w:" + width + ", vw:" + host.getVisibleRect());
                // width currently laid out to
                if (invalidated) {
                    int w = host.getVisibleRect().width;
                    if (w != 0) {
                        //log.fine("vrh: " + host.getVisibleRect().height);
                        invalidated = false;
                        // JXLabelTest4 works
                        setSize(w - (host.getOccupiedWidth()), host.getVisibleRect().height);
                        // JXLabelTest3 works; 20 == width of the parent border!!! ... why should this screw with us?
                        //setSize(w - (host.getOccupiedWidth()+20), host.getVisibleRect().height);
                    }
                }
                return width > 0 ? width : view.getPreferredSpan(axis);
            } else {
                return  view.getPreferredSpan(axis);
            }
        }

        /**
         * Fetches the container hosting the view. This is useful for things like scheduling a repaint, finding out the
         * host components font, etc. The default implementation of this is to forward the query to the parent view.
         *
         * @return the container
         */
        @Override
        public Container getContainer() {
            return host;
        }

        /**
         * Fetches the factory to be used for building the various view fragments that make up the view that represents
         * the model. This is what determines how the model will be represented. This is implemented to fetch the
         * factory provided by the associated EditorKit.
         *
         * @return the factory
         */
        @Override
        public ViewFactory getViewFactory() {
            return factory;
        }

        private View view;

        private ViewFactory factory;

        @Override
        public int getWidth() {
            return (int) width;
        }

        @Override
        public int getHeight() {
            return (int) height;
        }

    }

   protected int getOccupiedWidth() {
        return occupiedWidth;
    }
}
