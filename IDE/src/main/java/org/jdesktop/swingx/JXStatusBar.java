/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.jdesktop.swingx;

import java.awt.Insets;

import javax.swing.JComponent;

import org.jdesktop.beans.JavaBean;
import org.jdesktop.swingx.plaf.LookAndFeelAddons;
import org.jdesktop.swingx.plaf.StatusBarAddon;
import org.jdesktop.swingx.plaf.StatusBarUI;

/**
 * <p>A container for <code>JComponents</code> that is typically placed at
 * the bottom of a form and runs the entire width of the form. There are 3
 * important functions that <code>JXStatusBar</code> provides.
 * First, <code>JXStatusBar</code> provides a hook for a pluggable look.
 * There is a definite look associated with status bars on windows, for instance.
 * By implementing a subclass of {@link JComponent}, we provide a way for the
 * pluggable look and feel system to modify the look of the status bar.</p>
 *
 * <p>Second, <code>JXStatusBar</code> comes with its own layout manager. Each item is added to
 * the <code>JXStatusBar</code> with a <code>JXStatusBar.Constraint</code>
 * as the constraint argument. The <code>JXStatusBar.Constraint</code> contains
 * an <code>Insets</code> object, as well as a <code>ResizeBehavior</code>,
 * which can be FIXED or FILL. The resize behaviour applies to the width of
 * components. All components added will maintain there preferred height, and the
 * height of the <code>JXStatusBar</code> will be the height of the highest
 * component plus insets.</p>
 *
 * <p>A constraint with <code>JXStatusBar.Constraint.ResizeBehavior.FIXED</code>
 * will cause the component to occupy a fixed area on the <code>JXStatusBar</code>.
 * The size of the area remains constant when the <code>JXStatusBar</code> is resized.
 * A constraint with this behavior may also take a width value, see
 * {@link JXStatusBar.Constraint#setFixedWidth(int)}. The width is a preferred
 * minimum width. If the component preferred width is greater than the constraint
 * width, the component width will apply.</p>
 *
 * <p>All components with constraint <code>JXStatusBar.Constraint.ResizeBehavior.FILL</code>
 * will share equally any spare space in the <code>JXStatusBar</code>. Spare space
 * is that left over after allowing for all FIXED component and the preferred
 * width of FILL components, plus insets
 *
 * <p>Constructing a <code>JXStatusBar</code> is very straightforward:
 * <pre><code>
 *      JXStatusBar bar = new JXStatusBar();
 *      JLabel statusLabel = new JLabel("Ready");
 *      JXStatusBar.Constraint c1 = new JXStatusBar.Constraint()
 *      c1.setFixedWidth(100);
 *      bar.add(statusLabel, c1);     // Fixed width of 100 with no inserts
 *      JXStatusBar.Constraint c2 = new JXStatusBarConstraint(
 *              JXStatusBar.Constraint.ResizeBehavior.FILL) // Fill with no inserts
 *      JProgressBar pbar = new JProgressBar();
 *      bar.add(pbar, c2);            // Fill with no inserts - will use remaining space
 * </code></pre></p>
 *
 * <p>Two common use cases for status bars include tracking application status and
 * progress. <code>JXStatusBar</code> does not manage these tasks, but instead special components
 * exist or can be created that do manage these tasks. For example, if your application
 * has a TaskManager or some other repository of currently running jobs, you could
 * easily create a TaskManagerProgressBar that tracks those jobs. This component
 * could then be added to the <code>JXStatusBar</code> like any other component.</p>
 *
 * <h2>Client Properties</h2>
 * <p>The BasicStatusBarUI.AUTO_ADD_SEPARATOR client property can be specified, which
 *    will disable the auto-adding of separators. In this case, you must add your own
 *    JSeparator components. To use:
 * <pre><code>
 *      JXStatusBar sbar = new JXStatusBar();
 *      sbar.putClientProperty(BasicStatusBarUI.AUTO_ADD_SEPARATOR, false);
 *      sbar.add(comp1);
 *      sbar.add(new JSeparator(JSeparator.VERTICAL));
 *      sbar.add(comp2);
 *      sbar.add(comp3);
 *  </code></pre></p>
 *
 * @status REVIEWED
 *
 * @author pdoubleya
 * @author rbair
 * @author Karl George Schaefer
 */
@JavaBean
public class JXStatusBar extends JComponent {
    /**
     * @see #getUIClassID
     * @see #readObject
     */
    public static final String uiClassID = "StatusBarUI";

    //TODO how to handle UI delegate setting of primitive?
    private boolean resizeHandleEnabled;

    /**
     * Initialization that would ideally be moved into various look and feel
     * classes.
     */
    static {
        LookAndFeelAddons.contribute(new StatusBarAddon());
    }

    /**
     * Creates a new JXStatusBar
     */
    public JXStatusBar() {
        super();
        updateUI();
    }

    /**
     * @param resizeHandleEnabled the resizeHandleEnabled to set
     */
    public void setResizeHandleEnabled(boolean resizeHandleEnabled) {
        boolean oldValue = isResizeHandleEnabled();
        this.resizeHandleEnabled = resizeHandleEnabled;
        firePropertyChange("resizeHandleEnabled", oldValue, isResizeHandleEnabled());
    }

    /**
     * @return the resizeHandleEnabled
     */
    public boolean isResizeHandleEnabled() {
        return resizeHandleEnabled;
    }

    /**
     * Returns the look and feel (L&F) object that renders this component.
     *
     * @return the StatusBarUI object that renders this component
     */
    public StatusBarUI getUI() {
        return (StatusBarUI) ui;
    }

    /**
     * Sets the look and feel (L&F) object that renders this component.
     *
     * @param ui
     *            the StatusBarUI L&F object
     * @see javax.swing.UIDefaults#getUI
     * @beaninfo
     *        bound: true
     *       hidden: true
     *    attribute: visualUpdate true
     *  description: The component's look and feel delegate.
     */
    public void setUI(StatusBarUI ui) {
        super.setUI(ui);
    }

    /**
     * Returns a string that specifies the name of the L&F class that renders
     * this component.
     *
     * @return "StatusBarUI"
     * @see javax.swing.JComponent#getUIClassID
     * @see javax.swing.UIDefaults#getUI
     * @beaninfo expert: true description: A string that specifies the name of
     *           the L&F class.
     */
    @Override
    public String getUIClassID() {
        return uiClassID;
    }

    /**
     * Notification from the <code>UIManager</code> that the L&F has changed.
     * Replaces the current UI object with the latest version from the
     * <code>UIManager</code>.
     *
     * @see javax.swing.JComponent#updateUI
     */
    @Override
    public void updateUI() {
        setUI((StatusBarUI) LookAndFeelAddons
                .getUI(this, StatusBarUI.class));
    }

    /**
     * The constraint object to be used with the <code>JXStatusBar</code>. It takes
     * a ResizeBehaviour, Insets and a Width. Width is only applicable for
     * ResizeBehavior.FIXED. @see JXStatusBar class documentation.
     */
    public static class Constraint {
        public static enum ResizeBehavior {FILL, FIXED}

        private Insets insets;
        private ResizeBehavior resizeBehavior;
        private int fixedWidth = 0;

        /**
         * Creates a new Constraint with default FIXED behaviour and no insets.
         */
        public Constraint() {
            this(ResizeBehavior.FIXED, null);
        }

        /**
         * Creates a new Constraint with default FIXED behaviour and the given insets
         *
         * @param insets may be null. If null, an Insets with 0 values will be used.
         */
        public Constraint(Insets insets) {
            this(ResizeBehavior.FIXED, insets);
        }

        /**
         * Creates a new Constraint with default FIXED behaviour and the given fixed
         * width.
         *
         * @param fixedWidth must be >= 0
         */
        public Constraint(int fixedWidth) {
            this(fixedWidth, null);
        }

        /**
         * Creates a new Constraint with default FIXED behaviour and the given fixed
         * width, and using the given Insets.
         *
         * @param fixedWidth must be >= 0
         * @param insets may be null. If null, an Insets with 0 values will be used.
         */
        public Constraint(int fixedWidth, Insets insets) {
            if (fixedWidth < 0) {
                throw new IllegalArgumentException("fixedWidth must be >= 0");
            }
            this.fixedWidth = fixedWidth;
            this.insets = insets == null ? new Insets(0, 0, 0, 0) : (Insets)insets.clone();
            this.resizeBehavior = ResizeBehavior.FIXED;
        }

        /**
         * Creates a new Constraint with the specified resize behaviour and no insets
         *
         * @param resizeBehavior - either JXStatusBar.Constraint.ResizeBehavior.FIXED
         * or JXStatusBar.Constraint.ResizeBehavior.FILL.
         */
        public Constraint(ResizeBehavior resizeBehavior) {
            this(resizeBehavior, null);
        }

        /**
         * Creates a new Constraint with the specified resize behavior and insets.
         *
         * @param resizeBehavior - either JXStatusBar.Constraint.ResizeBehavior.FIXED
         * or JXStatusBar.Constraints.ResizeBehavior.FILL.
         * @param insets may be null. If null, an Insets with 0 values will be used.
         */
        public Constraint(ResizeBehavior resizeBehavior, Insets insets) {
            this.resizeBehavior = resizeBehavior;
            this.insets = insets == null ? new Insets(0, 0, 0, 0) : (Insets)insets.clone();
        }

        /**
         * Set the fixed width the component added with this
         * constraint will occupy on the <code>JXStatusBar</code>. Only applies
         * to ResizeBehavior.FIXED. Will be ignored for ResizeBehavior.FILL.
         *
         * @param width - minimum width component will occupy. If 0, the preferred
         * width of the component will be used.
         * The width specified must be >= 0
         */
        public void setFixedWidth(int width) {
            if (width < 0) {
                throw new IllegalArgumentException("width must be >= 0");
            }
            fixedWidth = resizeBehavior == ResizeBehavior.FIXED ? width : 0;
        }

        /**
         * Returns the ResizeBehavior.
         *
         * @return ResizeBehavior
         */
        public ResizeBehavior getResizeBehavior() {
            return resizeBehavior;
        }

        /**
         * Returns the insets.
         *
         * @return insets
         */
        public Insets getInsets() {
            return (Insets)insets.clone();
        }

        /**
         * Get fixed width. Width is zero for resize behavior FILLED
         * @return the width of this constraint
         */
        public int getFixedWidth() {
            return fixedWidth;
        }
    }

}
