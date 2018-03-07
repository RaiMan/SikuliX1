/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Composite;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.Border;

import org.jdesktop.beans.JavaBean;
import org.jdesktop.swingx.util.GraphicsUtilities;

/**
 * <code>JXCollapsiblePane</code> provides a component which can collapse or
 * expand its content area with animation and fade in/fade out effects.
 * It also acts as a standard container for other Swing components.
 * <p>
 * The {@code JXCollapsiblePane} has a "content pane" that actually holds the
 * displayed contents. This means that colors, fonts, and other display
 * configuration items must be set on the content pane.
 *
 * <pre><code>
 * // to set the font
 * collapsiblePane.getContentPane().setFont(font);
 * // to set the background color
 * collapsiblePane.getContentPane().setBackground(Color.RED);
 * </code>
 * </pre>
 *
 * For convenience, the {@code add} and {@code remove} methods forward to the
 * content pane.  The following code shows to ways to add a child to the
 * content pane.
 *
 * <pre><code>
 * // to add a child
 * collapsiblePane.getContentPane().add(component);
 * // to add a child
 * collapsiblePane.add(component);
 * </code>
 * </pre>
 *
 * To set the content pane, do not use {@code add}, use {@link #setContentPane(Container)}.
 *
 * <p>
 * In this example, the <code>JXCollapsiblePane</code> is used to build
 * a Search pane which can be shown and hidden on demand.
 *
 * <pre>
 * <code>
 * JXCollapsiblePane cp = new JXCollapsiblePane();
 *
 * // JXCollapsiblePane can be used like any other container
 * cp.setLayout(new BorderLayout());
 *
 * // the Controls panel with a textfield to filter the tree
 * JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
 * controls.add(new JLabel("Search:"));
 * controls.add(new JTextField(10));
 * controls.add(new JButton("Refresh"));
 * controls.setBorder(new TitledBorder("Filters"));
 * cp.add("Center", controls);
 *
 * JXFrame frame = new JXFrame();
 * frame.setLayout(new BorderLayout());
 *
 * // Put the "Controls" first
 * frame.add("North", cp);
 *
 * // Then the tree - we assume the Controls would somehow filter the tree
 * JScrollPane scroll = new JScrollPane(new JTree());
 * frame.add("Center", scroll);
 *
 * // Show/hide the "Controls"
 * JButton toggle = new JButton(cp.getActionMap().get(JXCollapsiblePane.TOGGLE_ACTION));
 * toggle.setText("Show/Hide Search Panel");
 * frame.add("South", toggle);
 *
 * frame.pack();
 * frame.setVisible(true);
 * </code>
 * </pre>
 *
 * <p>
 * The <code>JXCollapsiblePane</code> has a default toggle action registered
 * under the name {@link #TOGGLE_ACTION}. Bind this action to a button and
 * pressing the button will automatically toggle the pane between expanded
 * and collapsed states. Additionally, you can define the icons to use through
 * the {@link #EXPAND_ICON} and {@link #COLLAPSE_ICON} properties on the action.
 * Example
 * <pre>
 * <code>
 * // get the built-in toggle action
 * Action toggleAction = collapsible.getActionMap().
 *   get(JXCollapsiblePane.TOGGLE_ACTION);
 *
 * // use the collapse/expand icons from the JTree UI
 * toggleAction.putValue(JXCollapsiblePane.COLLAPSE_ICON,
 *                       UIManager.getIcon("Tree.expandedIcon"));
 * toggleAction.putValue(JXCollapsiblePane.EXPAND_ICON,
 *                       UIManager.getIcon("Tree.collapsedIcon"));
 * </code>
 * </pre>
 *
 * <p>
 * Note: <code>JXCollapsiblePane</code> requires its parent container to have a
 * {@link java.awt.LayoutManager} using {@link #getPreferredSize()} when
 * calculating its layout (example {@link org.jdesktop.swingx.VerticalLayout},
 * {@link java.awt.BorderLayout}).
 *
 * @javabean.attribute
 *          name="isContainer"
 *          value="Boolean.TRUE"
 *          rtexpr="true"
 *
 * @javabean.attribute
 *          name="containerDelegate"
 *          value="getContentPane"
 *
 * @javabean.class
 *          name="JXCollapsiblePane"
 *          shortDescription="A pane which hides its content with an animation."
 *          stopClass="java.awt.Component"
 *
 * @author rbair (from the JDNC project)
 * @author <a href="mailto:fred@L2FProd.com">Frederic Lavigne</a>
 * @author Karl George Schaefer
 */
@JavaBean
public class JXCollapsiblePane extends JXPanel {
    /**
     * The direction defines how the collapsible pane will collapse. The
     * constant names were designed by choosing a fixed point and then
     * determining the collapsing direction from that fixed point. This means
     * {@code RIGHT} expands to the right and this is probably the best
     * expansion for a component in {@link BorderLayout#EAST}.
     */
    public enum Direction {
        /**
         * Collapses left. Suitable for {@link BorderLayout#WEST}.
         */
        LEFT(false),

        /**
         * Collapses right. Suitable for {@link BorderLayout#EAST}.
         */
        RIGHT(false),

        /**
         * Collapses up. Suitable for {@link BorderLayout#NORTH}.
         */
        UP(true),

        /**
         * Collapses down. Suitable for {@link BorderLayout#SOUTH}.
         */
        DOWN(true),

        /**
         * Collapses toward the leading edge. Suitable for {@link BorderLayout#LINE_START}.
         */
        LEADING(false) {
            @Override
            Direction getFixedDirection(ComponentOrientation co) {
                return co.isLeftToRight() ? LEFT : RIGHT;
            }
        },

        /**
         * Collapses toward the trailing edge. Suitable for {@link BorderLayout#LINE_END}.
         */
        TRAILING(false) {
            @Override
            Direction getFixedDirection(ComponentOrientation co) {
                return co.isLeftToRight() ? RIGHT : LEFT;
            }
        },

        /**
         * Collapses toward the starting edge. Suitable for {@link BorderLayout#PAGE_START}.
         */
        START(true) {
            @Override
            Direction getFixedDirection(ComponentOrientation co) {
                return UP;
            }
        },

        /**
         * Collapses toward the ending edge. Suitable for {@link BorderLayout#PAGE_END}.
         */
        END(true) {
            @Override
            Direction getFixedDirection(ComponentOrientation co) {
                return DOWN;
            }
        },
        ;

        private final boolean vertical;

        private Direction(boolean vertical) {
            this.vertical = vertical;
        }

        /**
         * Gets the orientation for this direction.
         *
         * @return {@code true} if the direction is vertical, {@code false}
         *         otherwise
         */
        public boolean isVertical() {
            return vertical;
        }

        /**
         * Gets the fixed direction equivalent to this direction for the specified orientation.
         *
         * @param co
         *            the component's orientation
         * @return the fixed direction corresponding to the component's orietnation
         */
        Direction getFixedDirection(ComponentOrientation co) {
            return this;
        }
    }

    /**
     * Toggles the JXCollapsiblePane state and updates its icon based on the
     * JXCollapsiblePane "collapsed" status.
     */
    private class ToggleAction extends AbstractAction implements
                                                      PropertyChangeListener {
        public ToggleAction() {
            super(TOGGLE_ACTION);
            // the action must track the collapsed status of the pane to update its icon
            JXCollapsiblePane.this.addPropertyChangeListener("collapsed", this);
        }

        @Override
        public void putValue(String key, Object newValue) {
            super.putValue(key, newValue);
            if (EXPAND_ICON.equals(key) || COLLAPSE_ICON.equals(key)) {
                updateIcon();
            }
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            setCollapsed(!isCollapsed());
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            updateIcon();
        }

        void updateIcon() {
            if (isCollapsed()) {
                putValue(SMALL_ICON, getValue(EXPAND_ICON));
            } else {
                putValue(SMALL_ICON, getValue(COLLAPSE_ICON));
            }
        }
    }

    /**
     * JXCollapsible has a built-in toggle action which can be bound to buttons.
     * Accesses the action through
     * <code>collapsiblePane.getActionMap().get(JXCollapsiblePane.TOGGLE_ACTION)</code>.
     */
    public final static String TOGGLE_ACTION = "toggle";

    /**
     * The icon used by the "toggle" action when the JXCollapsiblePane is
     * expanded, i.e the icon which indicates the pane can be collapsed.
     */
    public final static String COLLAPSE_ICON = "collapseIcon";

    /**
     * The icon used by the "toggle" action when the JXCollapsiblePane is
     * collapsed, i.e the icon which indicates the pane can be expanded.
     */
    public final static String EXPAND_ICON = "expandIcon";

    /**
     * Indicates whether the component is collapsed or expanded
     */
    private boolean collapsed = false;

    /**
     * Defines the orientation of the component.
     */
    private Direction direction = Direction.UP;

    /**
     * Timer used for doing the transparency animation (fade-in)
     */
    private Timer animateTimer;
    private AnimationListener animator;
    private int currentDimension = -1;
    private WrapperContainer wrapper;
    private boolean useAnimation = true;
    private AnimationParams animationParams;
    private boolean collapseFiringState;

    /**
     * Constructs a new JXCollapsiblePane with a {@link JXPanel} as content pane
     * and a vertical {@link VerticalLayout} with a gap of 2 pixels as layout
     * manager and a vertical orientation.
     */
    public JXCollapsiblePane() {
        this(Direction.UP);
    }

    /**
     * Constructs a new JXCollapsiblePane with a {@link JXPanel} as content pane and the specified
     * direction.
     *
     * @param direction
     *                the direction to collapse the container
     */
    public JXCollapsiblePane(Direction direction) {
        super.setLayout(new BorderLayout());
        this.direction = direction;
        animator = new AnimationListener();
        setAnimationParams(new AnimationParams(30, 8, 0.01f, 1.0f));

        setContentPane(createContentPane());
        setDirection(direction);

        // add an action to automatically toggle the state of the pane
        getActionMap().put(TOGGLE_ACTION, new ToggleAction());
    }

    /**
     * Creates the content pane used by this collapsible pane.
     *
     * @return the content pane
     */
    protected Container createContentPane() {
        return new JXPanel();
    }

    /**
     * @return the content pane
     */
    public Container getContentPane() {
        if (wrapper == null) {
            return null;
        }

        return (Container) wrapper.getView();
    }

    /**
     * Sets the content pane of this JXCollapsiblePane. The {@code contentPanel}
     * <i>should</i> implement {@code Scrollable} and return {@code true} from
     * {@link Scrollable#getScrollableTracksViewportHeight()} and
     * {@link Scrollable#getScrollableTracksViewportWidth()}. If the content
     * pane fails to do so and a {@code JScrollPane} is added as a child, it is
     * likely that the scroll pane will never correctly size. While it is not
     * strictly necessary to implement {@code Scrollable} in this way, the
     * default content pane does so.
     *
     * @param contentPanel
     *                the container delegate used to hold all of the contents
     *                for this collapsible pane
     * @throws IllegalArgumentException
     *                 if contentPanel is null
     */
    public void setContentPane(Container contentPanel) {
        if (contentPanel == null) {
            throw new IllegalArgumentException("Content pane can't be null");
        }

        if (wrapper != null) {
            //these next two lines are as they are because if I try to remove
            //the "wrapper" component directly, then super.remove(comp) ends up
            //calling remove(int), which is overridden in this class, leading to
            //improper behavior.
            assert super.getComponent(0) == wrapper;
            super.remove(0);
        }

        wrapper = new WrapperContainer(contentPanel);
        wrapper.collapsedState = isCollapsed();
        wrapper.getView().setVisible(!wrapper.collapsedState);
        super.addImpl(wrapper, BorderLayout.CENTER, -1);
    }

    /**
     * Overridden to redirect call to the content pane.
     */
    @Override
    public void setLayout(LayoutManager mgr) {
        // wrapper can be null when setLayout is called by "super()" constructor
        if (wrapper != null) {
            getContentPane().setLayout(mgr);
        }
    }

    /**
     * Overridden to redirect call to the content pane.
     */
    @Override
    protected void addImpl(Component comp, Object constraints, int index) {
        getContentPane().add(comp, constraints, index);
    }

    /**
     * Overridden to redirect call to the content pane
     */
    @Override
    public void remove(Component comp) {
        getContentPane().remove(comp);
    }

    /**
     * Overridden to redirect call to the content pane.
     */
    @Override
    public void remove(int index) {
        getContentPane().remove(index);
    }

    /**
     * Overridden to redirect call to the content pane.
     */
    @Override
    public void removeAll() {
        getContentPane().removeAll();
    }

    /**
     * If true, enables the animation when pane is collapsed/expanded. If false,
     * animation is turned off.
     *
     * <p>
     * When animated, the <code>JXCollapsiblePane</code> will progressively
     * reduce (when collapsing) or enlarge (when expanding) the height of its
     * content area until it becomes 0 or until it reaches the preferred height of
     * the components it contains. The transparency of the content area will also
     * change during the animation.
     *
     * <p>
     * If not animated, the <code>JXCollapsiblePane</code> will simply hide
     * (collapsing) or show (expanding) its content area.
     *
     * @param animated
     * @javabean.property bound="true" preferred="true"
     */
    public void setAnimated(boolean animated) {
        if (animated != useAnimation) {
            useAnimation = animated;

            if (!animated) {
            	if (animateTimer.isRunning()) {
            		//TODO should we listen for animation state change?
            		//yes, but we're best off creating a UI delegate for these changes
            		SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							currentDimension = -1;
						}
					});
            	} else {
            		currentDimension = -1;
            	}
            }
            firePropertyChange("animated", !useAnimation, useAnimation);
        }
    }

    /**
     * @return true if the pane is animated, false otherwise
     * @see #setAnimated(boolean)
     */
    public boolean isAnimated() {
        return useAnimation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setComponentOrientation(ComponentOrientation o) {
        if (animateTimer.isRunning()) {
            throw new IllegalStateException("cannot be change component orientation while collapsing.");
        }

        super.setComponentOrientation(o);
    }

    /**
     * Changes the direction of this collapsible pane. Doing so changes the
     * layout of the underlying content pane. If the chosen direction is
     * vertical, a vertical layout with a gap of 2 pixels is chosen. Otherwise,
     * a horizontal layout with a gap of 2 pixels is chosen.
     *
     * @see #getDirection()
     * @param direction the new {@link Direction} for this collapsible pane
     * @throws IllegalStateException when this method is called while a
     *                               collapsing/restore operation is running
     * @javabean.property
     *    bound="true"
     *    preferred="true"
     */
    public void setDirection(Direction direction) {
        if (animateTimer.isRunning()) {
            throw new IllegalStateException("cannot be change direction while collapsing.");
        }

        Direction oldValue = getDirection();
        this.direction = direction;

        if (direction.isVertical()) {
            getContentPane().setLayout(new VerticalLayout(2));
        } else {
            getContentPane().setLayout(new HorizontalLayout(2));
        }

        firePropertyChange("direction", oldValue, getDirection());
    }

    /**
     * @return the current {@link Direction}.
     * @see #setDirection(Direction)
     */
    public Direction getDirection() {
        return direction;
    }

    /**
     * @return true if the pane is collapsed, false if expanded
     */
    public boolean isCollapsed() {
        return collapsed;
    }

    /**
     * Expands or collapses this <code>JXCollapsiblePane</code>.
     *
     * <p>
     * If the component is collapsed and <code>val</code> is false, then this
     * call expands the JXCollapsiblePane, such that the entire JXCollapsiblePane
     * will be visible. If {@link #isAnimated()} returns true, the expansion will
     * be accompanied by an animation.
     *
     * <p>
     * However, if the component is expanded and <code>val</code> is true, then
     * this call collapses the JXCollapsiblePane, such that the entire
     * JXCollapsiblePane will be invisible. If {@link #isAnimated()} returns true,
     * the collapse will be accompanied by an animation.
     *
     * <p>
     * As of SwingX 1.6.3, JXCollapsiblePane only fires property change events when
     * the component's state is accurate.  This means that animated collapsible
     * pane's only fire events once the animation is complete.
     *
     * @see #isAnimated()
     * @see #setAnimated(boolean)
     * @javabean.property
     *    bound="true"
     *    preferred="true"
     */
    public void setCollapsed(boolean val) {
        boolean oldValue = isCollapsed();
        this.collapsed = val;

        if (isAnimated() && isShowing()) {
            if (oldValue == isCollapsed()) {
                return;
            }

            // this ensures that if the user reverses the animation
            // before completion that no property change is fired
            if (!animateTimer.isRunning()) {
                collapseFiringState = oldValue;
            }

            if (oldValue) {
                int dimension = direction.isVertical() ? wrapper.getHeight() : wrapper.getWidth();
                int preferredDimension = direction.isVertical() ? getContentPane()
                        .getPreferredSize().height : getContentPane().getPreferredSize().width;
                int delta = Math.max(8, preferredDimension / 10);

                setAnimationParams(new AnimationParams(30, delta, 0.01f, 1.0f));
                animator.reinit(dimension, preferredDimension);
                wrapper.getView().setVisible(true);
            } else {
                int dimension = direction.isVertical() ? wrapper.getHeight() : wrapper.getWidth();
                setAnimationParams(new AnimationParams(30, Math.max(8, dimension / 10), 1.0f, 0.01f));
                animator.reinit(dimension, 0);
            }

            animateTimer.start();
        } else {
            wrapper.collapsedState = isCollapsed();
            wrapper.getView().setVisible(!isCollapsed());
            revalidate();
            repaint();

            firePropertyChange("collapsed", oldValue, isCollapsed());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Border getBorder() {
        if (getContentPane() instanceof JComponent) {
            return ((JComponent) getContentPane()).getBorder();
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBorder(Border border) {
        if (getContentPane() instanceof JComponent) {
            ((JComponent) getContentPane()).setBorder(border);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Internals of JXCollasiplePane are designed to be opaque because some Look and Feel
     * implementations having painting issues otherwise. JXCollapsiblePane and its internals will
     * respect {@code setOpaque}, calling this method will not only update the collapsible pane, but
     * also all internals. This method does not modify the {@link #getContentPane() content pane},
     * as it is not considered an internal.
     */
    @Override
    public void setOpaque(boolean opaque) {
        super.setOpaque(opaque);

        if (wrapper != null) {
            wrapper.setOpaque(opaque);
        }
    }

    /**
     * A collapsible pane always returns its preferred size for the minimum size
     * to ensure that the collapsing happens correctly.
     * <p>
     * To query the minimum size of the contents user {@code
     * getContentPane().getMinimumSize()}.
     *
     * @return the preferred size of the component
     */
    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    /**
     * Forwards to the content pane.
     *
     * @param minimumSize
     *            the size to set on the content pane
     */
    @Override
    public void setMinimumSize(Dimension minimumSize) {
        getContentPane().setMinimumSize(minimumSize);
    }

    /**
     * The critical part of the animation of this <code>JXCollapsiblePane</code>
     * relies on the calculation of its preferred size. During the animation, its
     * preferred size (specially its height) will change, when expanding, from 0
     * to the preferred size of the content pane, and the reverse when collapsing.
     *
     * @return this component preferred size
     */
    @Override
    public Dimension getPreferredSize() {
        /*
         * The preferred size is calculated based on the current position of the
         * component in its animation sequence. If the Component is expanded, then
         * the preferred size will be the preferred size of the top component plus
         * the preferred size of the embedded content container. <p>However, if the
         * scroll up is in any state of animation, the height component of the
         * preferred size will be the current height of the component (as contained
         * in the currentDimension variable and when orientation is VERTICAL, otherwise
         * the same applies to the width)
         */
        Dimension dim = getContentPane().getPreferredSize();
        if (currentDimension != -1) {
                if (direction.isVertical()) {
                    dim.height = currentDimension;
                } else {
                    dim.width = currentDimension;
                }
        } else if(wrapper.collapsedState) {
            if (direction.isVertical()) {
                dim.height = 0;
            } else {
                dim.width = 0;
            }
        }
        return dim;
    }

    @Override
    public void setPreferredSize(Dimension preferredSize) {
        getContentPane().setPreferredSize(preferredSize);
    }

    /**
     * Sets the parameters controlling the animation
     *
     * @param params
     * @throws IllegalArgumentException
     *           if params is null
     */
    private void setAnimationParams(AnimationParams params) {
        if (params == null) { throw new IllegalArgumentException(
                "params can't be null"); }
        if (animateTimer != null) {
            animateTimer.stop();
        }
        animationParams = params;
        animateTimer = new Timer(animationParams.waitTime, animator);
        animateTimer.setInitialDelay(0);
    }

    /**
     * Tagging interface for containers in a JXCollapsiblePane hierarchy who needs
     * to be revalidated (invalidate/validate/repaint) when the pane is expanding
     * or collapsing. Usually validating only the parent of the JXCollapsiblePane
     * is enough but there might be cases where the parent's parent must be
     * validated.
     */
    public static interface CollapsiblePaneContainer {
        Container getValidatingContainer();
    }

    /**
     * Parameters controlling the animations
     */
    private static class AnimationParams {
        final int waitTime;
        final int delta;
        final float alphaStart;
        final float alphaEnd;

        /**
         * @param waitTime
         *          the amount of time in milliseconds to wait between calls to the
         *          animation thread
         * @param delta
         *          the delta, in the direction as specified by the orientation,
         *          to inc/dec the size of the scroll up by
         * @param alphaStart
         *          the starting alpha transparency level
         * @param alphaEnd
         *          the ending alpha transparency level
         */
        public AnimationParams(int waitTime, int delta, float alphaStart,
                               float alphaEnd) {
            this.waitTime = waitTime;
            this.delta = delta;
            this.alphaStart = alphaStart;
            this.alphaEnd = alphaEnd;
        }
    }

    /**
     * This class actual provides the animation support for scrolling up/down this
     * component. This listener is called whenever the animateTimer fires off. It
     * fires off in response to scroll up/down requests. This listener is
     * responsible for modifying the size of the content container and causing it
     * to be repainted.
     *
     * @author Richard Bair
     */
    private final class AnimationListener implements ActionListener {
        /**
         * Mutex used to ensure that the startDimension/finalDimension are not changed
         * during a repaint operation.
         */
        private final Object ANIMATION_MUTEX = "Animation Synchronization Mutex";
        /**
         * This is the starting dimension when animating. If > finalDimension, then the
         * animation is going to be to scroll up the component. If it is less than
         * finalDimension, then the animation will scroll down the component.
         */
        private int startDimension = 0;
        /**
         * This is the final dimension that the content container is going to be when
         * scrolling is finished.
         */
        private int finalDimension = 0;
        /**
         * The current alpha setting used during "animation" (fade-in/fade-out)
         */
        @SuppressWarnings({"FieldCanBeLocal"})
        private float animateAlpha = 1.0f;

        @Override
        public void actionPerformed(ActionEvent e) {
            /*
            * Pre-1) If startDimension == finalDimension, then we're done so stop the timer
            * 1) Calculate whether we're contracting or expanding. 2) Calculate the
            * delta (which is either positive or negative, depending on the results
            * of (1)) 3) Calculate the alpha value 4) Resize the ContentContainer 5)
            * Revalidate/Repaint the content container
            */
            synchronized (ANIMATION_MUTEX) {
                if (startDimension == finalDimension) {
                    animateTimer.stop();
                    animateAlpha = animationParams.alphaEnd;
                    // keep the content pane hidden when it is collapsed, other it may
                    // still receive focus.
                    if (finalDimension > 0) {
                        currentDimension = -1;
                        wrapper.collapsedState = false;
                        validate();
                        JXCollapsiblePane.this.firePropertyChange("collapsed", collapseFiringState, false);
                        return;
                    } else {
                        wrapper.collapsedState = true;
                        wrapper.getView().setVisible(false);
                        JXCollapsiblePane.this.firePropertyChange("collapsed", collapseFiringState, true);
                    }
                }

                final boolean contracting = startDimension > finalDimension;
                final int delta = contracting?-1 * animationParams.delta
                                  :animationParams.delta;
                int newDimension;
                if (direction.isVertical()) {
                    newDimension = wrapper.getHeight() + delta;
                } else {
                    newDimension = wrapper.getWidth() + delta;
                }
                if (contracting) {
                    if (newDimension < finalDimension) {
                        newDimension = finalDimension;
                    }
                } else {
                    if (newDimension > finalDimension) {
                        newDimension = finalDimension;
                    }
                }
                int dimension;
                if (direction.isVertical()) {
                    dimension = wrapper.getView().getPreferredSize().height;
                } else {
                    dimension = wrapper.getView().getPreferredSize().width;
                }
                animateAlpha = (float)newDimension / (float)dimension;

                Rectangle bounds = wrapper.getBounds();

                if (direction.isVertical()) {
                    int oldHeight = bounds.height;
                    bounds.height = newDimension;
                    wrapper.setBounds(bounds);

                    if (direction.getFixedDirection(getComponentOrientation()) == Direction.DOWN) {
                        wrapper.setViewPosition(new Point(0, wrapper.getView().getPreferredSize().height - newDimension));
                    } else {
                        wrapper.setViewPosition(new Point(0, newDimension));
                    }

                    bounds = getBounds();
                    bounds.height = (bounds.height - oldHeight) + newDimension;
                    currentDimension = bounds.height;
                } else {
                    int oldWidth = bounds.width;
                    bounds.width = newDimension;
                    wrapper.setBounds(bounds);

                    if (direction.getFixedDirection(getComponentOrientation()) == Direction.RIGHT) {
                        wrapper.setViewPosition(new Point(wrapper.getView().getPreferredSize().width - newDimension, 0));
                    } else {
                        wrapper.setViewPosition(new Point(newDimension, 0));
                    }

                    bounds = getBounds();
                    bounds.width = (bounds.width - oldWidth) + newDimension;
                    currentDimension = bounds.width;
                }

                setBounds(bounds);
                startDimension = newDimension;

                // it happens the animateAlpha goes over the alphaStart/alphaEnd range
                // this code ensures it stays in bounds. This behavior is seen when
                // component such as JTextComponents are used in the container.
                if (contracting) {
                    // alphaStart > animateAlpha > alphaEnd
                    if (animateAlpha < animationParams.alphaEnd) {
                        animateAlpha = animationParams.alphaEnd;
                    }
                    if (animateAlpha > animationParams.alphaStart) {
                        animateAlpha = animationParams.alphaStart;
                    }
                } else {
                    // alphaStart < animateAlpha < alphaEnd
                    if (animateAlpha > animationParams.alphaEnd) {
                        animateAlpha = animationParams.alphaEnd;
                    }
                    if (animateAlpha < animationParams.alphaStart) {
                        animateAlpha = animationParams.alphaStart;
                    }
                }

                wrapper.setAlpha(animateAlpha);

                validate();
            }
        }

        void validate() {
            Container parent = SwingUtilities.getAncestorOfClass(
                    CollapsiblePaneContainer.class, JXCollapsiblePane.this);
            if (parent != null) {
                parent = ((CollapsiblePaneContainer)parent).getValidatingContainer();
            } else {
                parent = getParent();
            }

            if (parent != null) {
                if (parent instanceof JComponent) {
                    ((JComponent)parent).revalidate();
                } else {
                    parent.invalidate();
                }
                parent.doLayout();
                parent.repaint();
            }
        }

        /**
         * Reinitializes the timer for scrolling up/down the component. This method
         * is properly synchronized, so you may make this call regardless of whether
         * the timer is currently executing or not.
         *
         * @param startDimension
         * @param stopDimension
         */
        public void reinit(int startDimension, int stopDimension) {
            synchronized (ANIMATION_MUTEX) {
                this.startDimension = startDimension;
                this.finalDimension = stopDimension;
                animateAlpha = animationParams.alphaStart;
                currentDimension = -1;
            }
        }
    }

    private final class WrapperContainer extends JViewport implements AlphaPaintable {
        boolean collapsedState;
        private volatile float alpha;
        private boolean oldOpaque;

        public WrapperContainer(Container c) {
            alpha = 1.0f;
            collapsedState = false;
            setView(c);

            // we must ensure the container is opaque. It is not opaque it introduces
            // painting glitches specially on Linux with JDK 1.5 and GTK look and feel.
            // GTK look and feel calls setOpaque(false)
            if (c instanceof JComponent && !c.isOpaque()) {
                ((JComponent) c).setOpaque(true);
            }
        }

        /**
         * {@inheritDoc} <p>
         *
         * Overridden to not have JViewPort behaviour (that is scroll the view)
         * but delegate to parent scrollRectToVisible just a JComponent does.<p>
         */
        @Override
        public void scrollRectToVisible(Rectangle aRect) {
        	//avoids JViewport's implementation
        	//by using JXCollapsiblePane's it will delegate upward
        	//getting any core fixes, by avoiding c&p
        	JXCollapsiblePane.this.scrollRectToVisible(aRect);
        }

        @Override
        public float getAlpha() {
            return alpha;
        }

        @Override
        public void setAlpha(float alpha) {
            if (alpha < 0f || alpha > 1f) {
                throw new IllegalArgumentException("invalid alpha value " + alpha);
            }

            float oldValue = getAlpha();
            this.alpha = alpha;

            if (getAlpha() < 1f) {
                if (oldValue == 1) {
                    //it used to be 1, but now is not. Save the oldOpaque
                    oldOpaque = isOpaque();
                    setOpaque(false);
                }
            } else {
                //restore the oldOpaque if it was true (since opaque is false now)
                if (oldOpaque) {
                    setOpaque(true);
                }
            }

            firePropertyChange("alpha", oldValue, getAlpha());
            repaint();
        }

        @Override
        public boolean isInheritAlpha() {
            return false;
        }

        @Override
        public void setInheritAlpha(boolean inheritAlpha) {
            //does nothing; always false;
        }

        @Override
        public float getEffectiveAlpha() {
            return getAlpha();
        }

        //support for Java 7 painting improvements
        protected boolean isPaintingOrigin() {
            return getAlpha() < 1f;
        }

        /**
         * Overridden paint method to take into account the alpha setting.
         *
         * @param g
         *            the <code>Graphics</code> context in which to paint
         */
        @Override
        public void paint(Graphics g) {
            //short circuit painting if no transparency
            if (getAlpha() == 1f) {
                super.paint(g);
            } else {
                //the component is translucent, so we need to render to
                //an intermediate image before painting
                // TODO should we cache this image? repaint to same image unless size changes?
                BufferedImage img = GraphicsUtilities.createCompatibleTranslucentImage(getWidth(), getHeight());
                Graphics2D gfx = img.createGraphics();

                try {
                    super.paint(gfx);
                } finally {
                    gfx.dispose();
                }

                Graphics2D g2d = (Graphics2D) g;
                Composite oldComp = g2d.getComposite();

                try {
                    Composite alphaComp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, getEffectiveAlpha());
                    g2d.setComposite(alphaComp);
                    //TODO should we cache the image?
                    g2d.drawImage(img, null, 0, 0);
                } finally {
                    g2d.setComposite(oldComp);
                }
            }
        }
    }

// TEST CASE
//    public static void main(String[] args) {
//        SwingUtilities.invokeLater(new Runnable() {
//            public void run() {
//                JFrame f = new JFrame("Test Oriented Collapsible Pane");
//
//                f.add(new JLabel("Press Ctrl+F or Ctrl+G to collapse panes."),
//                      BorderLayout.NORTH);
//
//                JTree tree1 = new JTree();
//                tree1.setBorder(BorderFactory.createEtchedBorder());
//                f.add(tree1);
//
//                JXCollapsiblePane pane = new JXCollapsiblePane(Orientation.VERTICAL);
//                pane.setCollapsed(true);
//                JTree tree2 = new JTree();
//                tree2.setBorder(BorderFactory.createEtchedBorder());
//                pane.add(tree2);
//                f.add(pane, BorderLayout.SOUTH);
//
//                pane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
//                        KeyStroke.getKeyStroke("ctrl F"),
//                        JXCollapsiblePane.TOGGLE_ACTION);
//
//                pane = new JXCollapsiblePane(Orientation.HORIZONTAL);
//                JTree tree3 = new JTree();
//                pane.add(tree3);
//                tree3.setBorder(BorderFactory.createEtchedBorder());
//                f.add(pane, BorderLayout.WEST);
//
//                pane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
//                        KeyStroke.getKeyStroke("ctrl G"),
//                        JXCollapsiblePane.TOGGLE_ACTION);
//
//                f.setSize(640, 480);
//                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//                f.setVisible(true);
//        }
//        });
//    }
}
