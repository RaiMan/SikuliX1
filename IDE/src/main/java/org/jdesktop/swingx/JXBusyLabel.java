/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.jdesktop.swingx;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.Timer;
import javax.swing.plaf.LabelUI;

import org.jdesktop.beans.JavaBean;
import org.jdesktop.swingx.icon.PainterIcon;
import org.jdesktop.swingx.painter.BusyPainter;
import org.jdesktop.swingx.plaf.BusyLabelAddon;
import org.jdesktop.swingx.plaf.BusyLabelUI;
import org.jdesktop.swingx.plaf.LookAndFeelAddons;

/**
 * <p>A simple circular animation, useful for denoting an action is taking
 * place that may take an unknown length of time to complete. Similar to an
 * indeterminant JProgressBar, but with a different look.</p>
 *
 * <p>For example:
 * <pre><code>
 *     JXFrame frame = new JXFrame("test", true);
 *     JXBusyLabel label = new JXBusyLabel();
 *     frame.add(label);
 *     //...
 *     label.setBusy(true);
 * </code></pre></p>
 * Another more complicated example:
 * <pre><code>
 * JXBusyLabel label = new JXBusyLabel(new Dimension(100,84));
 * BusyPainter painter = new BusyPainter(
 * new Rectangle2D.Float(0, 0,13.500001f,1),
 * new RoundRectangle2D.Float(12.5f,12.5f,59.0f,59.0f,10,10));
 * painter.setTrailLength(5);
 * painter.setPoints(31);
 * painter.setFrame(1);
 * label.setPreferredSize(new Dimension(100,84));
 * label.setIcon(new EmptyIcon(100,84));
 * label.setBusyPainter(painter);
 *</code></pre>
 *
 * Another example:
 * <pre><code>
 *     JXBusyLabel label = new MyBusyLabel(new Dimension(100, 84));
 * </code></pre>
 *
 * where MyBusyLabel is:<br>
 * <pre><code>
 * public class MyBusyLabel extends JXBusyLabel {
 *     public MyBusyLabel(Dimension prefSize) {
 *         super(prefSize);
 *     }
 *
 *     protected BusyLabel createBusyLabel(Dimension dim) {
 *         BusyPainter painter = new BusyPainter(
 *         new Rectangle2D.Float(0, 0,13.500001f,1),
 *         new RoundRectangle2D.Float(12.5f,12.5f,59.0f,59.0f,10,10));
 *         painter.setTrailLength(5);
 *         painter.setPoints(31);
 *         painter.setFrame(1);
 *
 *         return painter;
 *     }
 * }
 * </code></pre>
 *
 * @author rbair
 * @author joshy
 * @author rah003
 * @author headw01
 */
@JavaBean
public class JXBusyLabel extends JLabel {

    private static final long serialVersionUID = 5979268460848257147L;
    private BusyPainter busyPainter;
    private Timer busy;
    private int delay;
    /** Status flag to save/restore status of timer when moving component between containers. */
    private boolean wasBusyOnNotify = false;

    /**
     * UI Class ID
     */
    public final static String uiClassID = "BusyLabelUI";

    /**
     * Sets direction of rotation. <code>Direction.RIGHT</code> is the default
     * value. Direction is taken from the very top point so <code>Direction.RIGHT</code> enables rotation clockwise.
     * @param dir Direction of rotation.
     */
    public void setDirection(BusyPainter.Direction dir) {
        direction = dir;
        getBusyPainter().setDirection(dir);
    }

    private BusyPainter.Direction direction;

    /**
     * Creates a default JXLoginPane instance
     */
    static {
        LookAndFeelAddons.contribute(new BusyLabelAddon());
    }

    {
        // Initialize the delay from the UI class.
        BusyLabelUI ui = (BusyLabelUI)getUI();
        if (ui != null) {
            delay = ui.getDelay();
        }
    }

    /** Creates a new instance of <code>JXBusyLabel</code> initialized to circular shape in bounds of 26 by 26 points.*/
    public JXBusyLabel() {
        this(null);
    }

    /**
     * Creates a new instance of <code>JXBusyLabel</code> initialized to the arbitrary size and using default circular progress indicator.
     * @param dim Preferred size of the label.
     */
    public JXBusyLabel(Dimension dim) {
        super();
        this.setPreferredSize(dim);

        // Initialize the BusyPainter.
        getBusyPainter();
    }

    /**
     * Initialize the BusyPainter and (this) JXBusyLabel with the given
     * preferred size.  This method is called automatically when the
     * BusyPainter is set/changed.
     *
     * @param dim The new Preferred Size for the BusyLabel.
     *
     * @see #getBusyPainter()
     * @see #setBusyPainter(BusyPainter)
     */
    protected void initPainter(Dimension dim) {
        BusyPainter busyPainter = getBusyPainter();

        // headw01
        // TODO: Should we force the busyPainter to NOT be cached?
        //       I think we probably should, otherwise the UI will never
        //       be updated after the first paint.
        if (null != busyPainter) {
            busyPainter.setCacheable(false);
        }

        PainterIcon icon = new PainterIcon(dim);
        icon.setPainter(busyPainter);
        this.setIcon(icon);
    }
    /**
     * Create and return a BusyPpainter to use for the Label. This may
     * be overridden to return any painter you like.  By default, this
     * method uses the UI (BusyLabelUI)to create a BusyPainter.
     * @param dim Painter size.
     *
     * @see #getUI()
     */
    protected BusyPainter createBusyPainter(Dimension dim) {
        BusyPainter busyPainter = null;

        BusyLabelUI ui = (BusyLabelUI)getUI();
        if (ui != null) {
            busyPainter = ui.getBusyPainter(dim);

        }

        return busyPainter;
    }

    /**
     * <p>Gets whether this <code>JXBusyLabel</code> is busy. If busy, then
     * the <code>JXBusyLabel</code> instance will indicate that it is busy,
     * generally by animating some state.</p>
     *
     * @return true if this instance is busy
     */
    public boolean isBusy() {
        return busy != null;
    }

    /**
     * <p>Sets whether this <code>JXBusyLabel</code> instance should consider
     * itself busy. A busy component may indicate that it is busy via animation,
     * or some other means.</p>
     *
     * @param busy whether this <code>JXBusyLabel</code> instance should
     *        consider itself busy
     */
    public void setBusy(boolean busy) {
        boolean old = isBusy();
        if (!old && busy) {
            startAnimation();
            firePropertyChange("busy", old, isBusy());
        } else if (old && !busy) {
            stopAnimation();
            firePropertyChange("busy", old, isBusy());
        }
    }

    private void startAnimation() {
        if(busy != null) {
            stopAnimation();
        }

        busy = new Timer(delay, new ActionListener() {
            BusyPainter busyPainter = getBusyPainter();
            int frame = busyPainter.getPoints();
            @Override
            public void actionPerformed(ActionEvent e) {
                frame = (frame+1)%busyPainter.getPoints();
                busyPainter.setFrame(direction == BusyPainter.Direction.LEFT ? busyPainter.getPoints() - frame : frame);
                frameChanged();
            }
        });
        busy.start();
    }




    private void stopAnimation() {
        if (busy != null) {
            busy.stop();
            getBusyPainter().setFrame(-1);
            repaint();
            busy = null;
        }
    }

    @Override
    public void removeNotify() {
        // fix for #698
        wasBusyOnNotify = isBusy();
        // fix for #626
        stopAnimation();
        super.removeNotify();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        // fix for #698
        if (wasBusyOnNotify) {
            // fix for #626
            startAnimation();
        }
    }

    protected void frameChanged() {
        repaint();
    }

    /**
     * Returns the current BusyPainter.  If no BusyPainter is currently
     * set on this BusyLabel, the {@link #createBusyPainter(Dimension)}
     * method is called to create one.  Afterwards,
     * {@link #initPainter(Dimension)} is called to update the BusyLabel
     * with the created BusyPainter.
     *
     * @return the busyPainter
     *
     * @see #createBusyPainter(Dimension)
     * @see #initPainter(Dimension)
     */
    public final BusyPainter getBusyPainter() {
        if (null == busyPainter) {
            Dimension prefSize = getPreferredSize();

            busyPainter = createBusyPainter((prefSize.width == 0 && prefSize.height == 0 && !isPreferredSizeSet()) ? null : prefSize);

            if (null != busyPainter) {
                if (!isPreferredSizeSet() && (null == prefSize || prefSize.width == 0 || prefSize.height == 0)) {
                    Rectangle rt = busyPainter.getTrajectory().getBounds();
                    Rectangle rp = busyPainter.getPointShape().getBounds();
                    int max = Math.max(rp.width, rp.height);
                    prefSize = new Dimension(rt.width + max, rt.height + max);
                }

                initPainter(prefSize);
            }
        }
        return busyPainter;
    }

    /**
     * @param busyPainter the busyPainter to set
     */
    public final void setBusyPainter(BusyPainter busyPainter) {
        this.busyPainter = busyPainter;
        initPainter(new Dimension(getIcon().getIconWidth(), getIcon().getIconHeight()));
    }

    /**
     * @return the delay
     */
    public int getDelay() {
        return delay;
    }

    /**
     * @param delay the delay to set
     */
    public void setDelay(int delay) {
        int old = getDelay();
        this.delay = delay;
        if (old != getDelay()) {
            if (busy != null && busy.isRunning()) {
                busy.setDelay(getDelay());
            }
            firePropertyChange("delay", old, getDelay());
        }
    }
    //------------------------------------------------------------- UI Logic

    /**
     * Notification from the <code>UIManager</code> that the L&F has changed.
     * Replaces the current UI object with the latest version from the
     * <code>UIManager</code>.
     *
     * @see javax.swing.JComponent#updateUI
     */
    @Override
    public void updateUI() {
        setUI((LabelUI) LookAndFeelAddons.getUI(this, BusyLabelUI.class));
    }

    /**
     * Returns the name of the L&F class that renders this component.
     *
     * @return the string {@link #uiClassID}
     * @see javax.swing.JComponent#getUIClassID
     * @see javax.swing.UIDefaults#getUI
     */
    @Override
    public String getUIClassID() {
        return uiClassID;
    }

}
