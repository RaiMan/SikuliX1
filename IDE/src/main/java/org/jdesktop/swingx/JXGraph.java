/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.jdesktop.swingx;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.List;

import org.jdesktop.beans.AbstractBean;
import org.jdesktop.beans.JavaBean;
import org.jdesktop.swingx.painter.Painter;

// TODO: keyboard navigation
// TODO: honor clip rect with text painting
// TODO: let client change zoom multiplier
// TODO: improve text drawing when origin is not on a multiple of majorX/majorY
// TODO: programmatically zoom in and out (or expose ZOOM_MULTIPLIER)

/**
 * <p><code>JXGraph</code> provides a component which can display one or more
 * plots on top of a graduated background (or grid.)</p>
 *
 * <h2>User input</h2>
 *
 * <p>To help analyze the plots, this component allows the user to pan the
 * view by left-clicking and dragging the mouse around. Using the mouse wheel,
 * the user is also able to zoom in and out. Clicking the middle button resets
 * the view to its original position.</p>
 *
 * <p>All user input can be disabled by calling
 * {@link #setInputEnabled(boolean)} and passing false. This does not prevent
 * subclasses from registering their own event listeners, such as mouse or key
 * listeners.</p>
 *
 * <h2>Initializing the component and setting the view</h2>
 *
 * <p>Whenever a new instance of this component is created, the grid boundaries,
 * or view, must be defined. The view is comprised of several elements whose
 * descriptions are the following:</p>
 *
 * <ul>
 *   <li><i>minX</i>: Minimum value initially displayed by the component on the
 *   X axis (horizontally.)</li>
 *   <li><i>minY</i>: Minimum value initially displayed by the component on the
 *   Y axis (vertically.)</li>
 *   <li><i>maxX</i>: Maximum value initially displayed by the component on the
 *   X axis (horizontally.)</li>
 *   <li><i>maxY</i>: Maximum value initially displayed by the component on the
 *   Y axis (vertically.)</li>
 *   <li><i>originX</i>: Origin on the X axis of the vertical axis.</li>
 *   <li><i>originY</i>: Origin on the Y axis of the horizontal axis.</li>
 *   <li><i>majorX</i>: Distance between two major vertical lines of the
 *   grid.</li>
 *   <li><i>majorY</i>: Distance between two major horizontal lines of the
 *   grid.</li>
 *   <li><i>minCountX</i>: Number of minor vertical lines between two major
 *   vertical lines in the grid.</li>
 *   <li><i>minCountY</i>: Number of minor horizontal lines between two major
 *   horizontal lines in the grid.</li>
 * </ul>
 *
 * <h3>View and origin</h3>
 *
 * <p>The default constructor defines a view bounds by <code>-1.0</code> and
 * <code>+1.0</code> on both axis, and centered on an origin at
 * <code>(0, 0)</code>.</p>
 *
 * <p>To simplify the API, the origin can be read and written with a
 * <code>Point2D</code> instance (see {@link #getOrigin()} and
 * {@link #setOrigin(Point2D)}.)</p>
 *
 * <p>Likewise, the view can be read and written with a
 * <code>Rectangle2D</code> instance (see {@link #getView()} and
 * {@link #setView(Rectangle2D)}.) In this case, you need not to define the
 * maximum boundaries of the view. Instead, you need to set the origin of the
 * rectangle as the minimum boundaries. The width and the height of the
 * rectangle define the distance between the minimum and maximum boundaries. For
 * instance, to set the view to minX=-1.0, maxX=1.0, minY=-1.0 and maxY=1.0 you
 * can use the following rectangle:</p>
 *
 * <pre>new Rectangle2D.Double(-1.0d, -1.0d, 2.0d, 2.0d);</pre>
 *
 * <p>You can check the boundaries by calling <code>Rectangle2D.getMaxX()</code>
 * and <code>Rectangle2D.getMaxY()</code> once your rectangle has been
 * created.</p>
 *
 * <p>Alternatively, you can set the view and the origin at the same time by
 * calling the method {@link #setViewAndOrigin(Rectangle2D)}. Calling this
 * method will set the origin so as to center it in the view defined by the
 * rectangle.</p>
 *
 * <h3>Grid lines</h3>
 *
 * <p>By default, the component defines a spacing of 0.2 units between two
 * major grid lines. It also defines 4 minor grid lines between two major
 * grid lines. The spacing between major grid lines and the number of minor
 * grid lines can be accessed through the getters {@link #getMajorX()},
 * {@link #getMajorY()}, {@link #getMinorCountX()} and
 * {@link #getMinorCountY()}.</p>
 *
 * <p>You can change the number of grid lines at runtime by calling the setters
 * {@link #setMajorX(double)}, {@link #setMajorY(double)},
 * {@link #setMinorCountX(int)} and {@link #setMinorCountY(int)}.</p>
 *
 * <h3>Appearance</h3>
 *
 * <p>Although it provides sensible defaults, this component lets you change
 * its appearance in several ways. It is possible to modify the colors of the
 * graph by calling the setters {@link #setAxisColor(Color)},
 * {@link #setMajorGridColor(Color)} and {@link #setMinorGridColor(Color)}.</p>
 *
 * <p>You can also enable or disable given parts of the resulting graph by
 * calling the following setters:</p>
 *
 * <ul>
 *   <li>{@link #setAxisPainted(boolean)}: Defines whether the main axis (see
 *   {@link #getOrigin()}) is painted.</li>
 *   <li>{@link #setBackgroundPainted(boolean)}: Defines whether the background
 *   is painted (see {@link #setBackground(Color)}.)</li>
 *   <li>{@link #setGridPainted(boolean)}: Defines whether the grid is
 *   painted.</li>
 *   <li>{@link #setTextPainted(boolean)}: Defines whether the axis labels are
 *   painted.</li>
 * </ul>
 *
 * <h3>Usage example</h3>
 *
 * <p>The following code snippet creates a new graph centered on
 * <code>(0, 0)</code>, bound to the view <code>[-1.0 1.0 -1.0 1.0]</code>, with
 * a major grid line every 0.5 units and a minor grid line count of 5:</p>
 *
 * <pre>
 * Point2D origin = new Point2D.Double(0.0d, 0.0d);
 * Rectangle2D view = new Rectangle2D.Double(-1.0d, 1.0d, 2.0d, 2.0d);
 * JXGraph graph = new JXGraph(origin, view, 0.5d, 5, 0.5d, 5);
 * </pre>
 *
 * <h2>Plots</h2>
 *
 * <h3>Definition</h3>
 *
 * <p>A plot is defined by a mathematical transformation that, given a value on
 * the graph's X axis, returns a value on the Y axis. The component draws the
 * result by plotting a spot of color at the coordinates defined by
 * <code>(X, f(X))</code> where <code>f()</code> is the aforementionned
 * mathematical transformation. Given the following transformation:</p>
 *
 * <pre>
 * f(X) = X * 2.0
 * </pre>
 *
 * <p>For <code>X=1.0</code>, the component will show a spot of color at the
 * coordinates <code>(1.0, 2.0)</code>.</p>
 *
 * <h3>Creating a new plot</h3>
 *
 * <p>Every plot drawn by the component must be a subclass of
 * {@link JXGraph.Plot}. This abstract public class defines a single method to
 * be implemented by its children:</p>
 *
 * <pre>
 * public double compute(double value)
 * </pre>
 *
 * <p>The previous example can be defined by a concrete
 * <code>JXGraph.Plot</code> as follow:</p>
 *
 * <pre>
 * class TwiceTheValuePlot extends JXGraph.Plot {
 *     public double compute(double value) {
 *         return value * 2.0d;
 *     }
 * }
 * </pre>
 *
 * <p>Most of the time though, a plot requires supplementary parameters. For
 * instance, let's define the X axis of your graph as the mass of an object. To
 * compute the weight of the object given its mass, you need to use the
 * acceleration of gravity (<code>w=m*g</code> where <code>g</code> is the
 * acceleration.) To let the user modify this last parameter, to compute his
 * weight at the surface of the moon for instance, you need to add a parameter
 * to your plot.</p>
 *
 * <p>While <code>JXGraph.Plot</code> does not give you an API for such a
 * purpose, it does define an event dispatching API (see
 * {@link JXGraph#firePropertyChange(String, double, double)}.) Whenever a
 * plot is added to the graph, the component registers itself as a property
 * listener of the plot. If you take care of firing events whenever the user
 * changes a parameter of your plot, the graph will automatically update its
 * display. While not mandatory, it is highly recommended to leverage this
 * API.</p>
 *
 * <h3>Adding and removing plots to and from the graph</h3>
 *
 * <p>To add a plot to the graph, simply call the method
 * {@link #addPlots(Color, JXGraph.Plot...)}. You can use it to add one or more
 * plots at the same time and associate them with a color. This color is used
 * when drawing the plots:</p>
 *
 * <pre>
 * JXGraph.Plot plot = new TwiceTheValuePlot();
 * graph.addPlots(Color.BLUE, plot);
 * </pre>
 *
 * <p>These two lines will display our previously defined plot in blue on
 * screen. Removing one or several plots is as simple as calling the method
 * {@link #removePlots(JXGraph.Plot...)}. You can also remove all plots at once
 * with {@link #removeAllPlots()}.</p>
 *
 * <h2>Painting more information</h2>
 *
 * <h3>How to draw on the graph</h3>
 *
 * <p>If you need to add more information on the graph you need to extend
 * it and override the method {@link #paintExtra(Graphics2D)}. This
 * method has a default empty implementation and is called after everything
 * has been drawn. Its sole parameter is a reference to the component's drawing
 * surface, as configured by {@link #setupGraphics(Graphics2D)}. By default, the
 * setup method activates antialising but it can be overriden to change the
 * drawing surface. (Translation, rotation, new rendering hints, etc.)</p>
 *
 * <h3>Getting the right coordinates</h3>
 *
 * <p>To properly draw on the graph you will need to perform a translation
 * between the graph's coordinates and the screen's coordinates. The component
 * defines 4 methods to assist you in this task:</p>
 *
 * <ul>
 *   <li>{@link #xPixelToPosition(double)}: Converts a pixel coordinate on the
 *   X axis into a world coordinate.</li>
 *   <li>{@link #xPositionToPixel(double)}: Converts a world coordinate on the
 *   X axis into a pixel coordinate.</li>
 *   <li>{@link #yPixelToPosition(double)}: Converts a pixel coordinate on the
 *   Y axis into a world coordinate.</li>
 *   <li>{@link #yPositionToPixel(double)}: Converts a world coordinate on the
 *   Y axis into a pixel coordinate.</li>
 * </ul>
 *
 * <p>If you have defined a graph view centered on the origin
 * <code>(0, 0)</code>, the origin of the graph will be at the exact center of
 * the screen. That means the world coordinates <code>(0, 0)</code> are
 * equivalent to the pixel coordinates <code>(width / 2, height / 2)</code>.
 * Thus, calling <code>xPositionToPixel(0.0d)</code> would give you the same
 * value as the expression <code>getWidth() / 2.0d</code>.</p>
 *
 * <p>Converting from world coordinates to pixel coordinates is mostly used to
 * draw the result of a mathematical transformation. Converting from pixel
 * coordinates to world coordinates is mostly used to get the position in the
 * world of a mouse event.</p>
 *
 * @see JXGraph.Plot
 * @author Romain Guy <romain.guy@mac.com>
 */
@JavaBean
public class JXGraph extends JXPanel {
    // stroke widths used to draw the main axis and the grid
    // the main axis is slightly thicker
    private static final float STROKE_AXIS = 1.2f;
    private static final float STROKE_GRID = 1.0f;

    // defines by how much the view is shrinked or expanded everytime the
    // user zooms in or out
    private static final float ZOOM_MULTIPLIER = 1.1f;

    //listens to changes to plots and repaints the graph
    private PropertyChangeListener plotChangeListener;

    // default color of the graph (does not include plots colors)
    private Color majorGridColor = Color.GRAY.brighter();
    private Color minorGridColor = new Color(220, 220, 220);
    private Color axisColor = Color.BLACK;

    // the list of plots currently known and displayed by the graph
    private List<DrawablePlot> plots;

    // view boundaries as defined by the user
    private double minX;
    private double maxX;
    private double minY;
    private double maxY;

    // the default view is set when the view is manually changed by the client
    // it is used to reset the view in resetView()
    private Rectangle2D defaultView;

    // coordinates of the major axis
    private double originX;
    private double originY;

    // definition of the grid
    // various default values are used when the view is reset
    private double majorX;
    private double defaultMajorX;
    private int minorCountX;
    private double majorY;
    private double defaultMajorY;
    private int minorCountY;

    // enables painting layers
    private boolean textPainted = true;
    private boolean gridPainted = true;
    private boolean axisPainted = true;
    private boolean backPainted = true;

    // used by the PanHandler to move the view
    private Point dragStart;

    // mainFormatter is used for numbers > 0.01 and < 100
    // secondFormatter uses scientific notation
    private NumberFormat mainFormatter;
    private NumberFormat secondFormatter;

    // input handlers
    private boolean inputEnabled = true;
    private ZoomHandler zoomHandler;
    private PanMotionHandler panMotionHandler;
    private PanHandler panHandler;
    private ResetHandler resetHandler;

    /**
     * <p>Creates a new graph display. The following properties are
     * automatically set:</p>
     * <ul>
     *   <li><i>view</i>: -1.0 to +1.0 on both axis</li>
     *   <li><i>origin</i>: At <code>(0, 0)</code></li>
     *   <li><i>grid</i>: Spacing of 0.2 between major lines; minor lines
     *   count is 4</li>
     * </ul>
     */
    public JXGraph() {
        this(0.0, 0.0, -1.0, 1.0, -1.0, 1.0, 0.2, 4, 0.2, 4);
    }

    /**
     * <p>Creates a new graph display with the specified view. The following
     * properties are automatically set:</p>
     * <ul>
     *   <li><i>origin</i>: Center of the specified view</code></li>
     *   <li><i>grid</i>: Spacing of 0.2 between major lines; minor lines
     *   count is 4</li>
     * </ul>
     *
     * @param view the rectangle defining the view boundaries
     */
    public JXGraph(Rectangle2D view) {
        this(new Point2D.Double(view.getCenterX(), view.getCenterY()),
            view, 0.2, 4, 0.2, 4);
    }

    /**
     * <p>Creates a new graph display with the specified view and grid lines.
     * The origin is set at the center of the view.</p>
     *
     * @param view        the rectangle defining the view boundaries
     * @param majorX      the spacing between two major grid lines on the X axis
     * @param minorCountX the number of minor grid lines between two major
     *                    grid lines on the X axis
     * @param majorY      the spacing between two major grid lines on the Y axis
     * @param minorCountY the number of minor grid lines between two major
     *                    grid lines on the Y axis
     * @throws IllegalArgumentException if minX >= maxX or minY >= maxY or
     *                                  minorCountX < 0 or minorCountY < 0 or
     *                                  majorX <= 0.0 or majorY <= 0.0
     */
    public JXGraph(Rectangle2D view,
                   double majorX, int minorCountX,
                   double majorY, int minorCountY) {
        this(new Point2D.Double(view.getCenterX(), view.getCenterY()),
            view, majorX, minorCountX, majorY, minorCountY);
    }

    /**
     * <p>Creates a new graph display with the specified view and origin.
     * The following properties are automatically set:</p>
     * <ul>
     *   <li><i>grid</i>: Spacing of 0.2 between major lines; minor lines
     *   count is 4</li>
     * </ul>
     *
     * @param origin the coordinates of the main axis origin
     * @param view the rectangle defining the view boundaries
     */
    public JXGraph(Point2D origin, Rectangle2D view) {
        this(origin, view, 0.2, 4, 0.2, 4);
    }

    /**
     * <p>Creates a new graph display with the specified view, origin and grid
     * lines.</p>
     *
     * @param origin      the coordinates of the main axis origin
     * @param view        the rectangle defining the view boundaries
     * @param majorX      the spacing between two major grid lines on the X axis
     * @param minorCountX the number of minor grid lines between two major
     *                    grid lines on the X axis
     * @param majorY      the spacing between two major grid lines on the Y axis
     * @param minorCountY the number of minor grid lines between two major
     *                    grid lines on the Y axis
     * @throws IllegalArgumentException if minX >= maxX or minY >= maxY or
     *                                  minorCountX < 0 or minorCountY < 0 or
     *                                  majorX <= 0.0 or majorY <= 0.0
     */
    public JXGraph(Point2D origin, Rectangle2D view,
                   double majorX, int minorCountX,
                   double majorY, int minorCountY) {
        this(origin.getX(), origin.getY(),
            view.getMinX(), view.getMaxX(), view.getMinY(), view.getMaxY(),
            majorX, minorCountX, majorY, minorCountY);
    }

    /**
     * <p>Creates a new graph display with the specified view, origin and grid
     * lines.</p>
     *
     * @param originX     the coordinate of the major X axis
     * @param originY     the coordinate of the major Y axis
     * @param minX        the minimum coordinate on the X axis for the view
     * @param maxX        the maximum coordinate on the X axis for the view
     * @param minY        the minimum coordinate on the Y axis for the view
     * @param maxY        the maximum coordinate on the Y axis for the view
     * @param majorX      the spacing between two major grid lines on the X axis
     * @param minorCountX the number of minor grid lines between two major
     *                    grid lines on the X axis
     * @param majorY      the spacing between two major grid lines on the Y axis
     * @param minorCountY the number of minor grid lines between two major
     *                    grid lines on the Y axis
     * @throws IllegalArgumentException if minX >= maxX or minY >= maxY or
     *                                  minorCountX < 0 or minorCountY < 0 or
     *                                  majorX <= 0.0 or majorY <= 0.0
     */
    public JXGraph(double originX, double originY,
                   double minX,    double maxX,
                   double minY,    double maxY,
                   double majorX,  int minorCountX,
                   double majorY,  int minorCountY) {
        if (minX >= maxX) {
            throw new IllegalArgumentException("minX must be < to maxX");
        }

        if (minY >= maxY) {
            throw new IllegalArgumentException("minY must be < to maxY");
        }

        if (minorCountX < 0) {
            throw new IllegalArgumentException("minorCountX must be >= 0");
        }

        if (minorCountY < 0) {
            throw new IllegalArgumentException("minorCountY must be >= 0");
        }

        if (majorX <= 0.0) {
            throw new IllegalArgumentException("majorX must be > 0.0");
        }

        if (majorY <= 0.0) {
            throw new IllegalArgumentException("majorY must be > 0.0");
        }

        this.originX = originX;
        this.originY = originY;

        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;

        this.defaultView = new Rectangle2D.Double(minX, minY,
            maxX - minX, maxY - minY);

        this.setMajorX(this.defaultMajorX = majorX);
        this.setMinorCountX(minorCountX);
        this.setMajorY(this.defaultMajorY = majorY);
        this.setMinorCountY(minorCountY);

        this.plots = new LinkedList<DrawablePlot>();

        this.mainFormatter = NumberFormat.getInstance();
        this.mainFormatter.setMaximumFractionDigits(2);

        this.secondFormatter = new DecimalFormat("0.##E0");

        resetHandler = new ResetHandler();
        addMouseListener(resetHandler);
        panHandler = new PanHandler();
        addMouseListener(panHandler);
        panMotionHandler = new PanMotionHandler();
        addMouseMotionListener(panMotionHandler);
        zoomHandler = new ZoomHandler();
        addMouseWheelListener(zoomHandler);

        setBackground(Color.WHITE);
        setForeground(Color.BLACK);

        plotChangeListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                repaint();
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
   public boolean isOpaque() {
        if (!isBackgroundPainted()) {
            return false;
        }
        return super.isOpaque();
    }

    /**
     * {@inheritDoc}
     * @see #setInputEnabled(boolean)
     */
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        setInputEnabled(enabled);
    }

    /**
     * <p>Enables or disables user input on the component. When user input is
     * enabled, panning, zooming and view resetting. Disabling input will
     * prevent the user from modifying the currently displayed view.<p>
     * <p>Calling {@link #setEnabled(boolean)} disables the component in the
     * Swing hierarchy and invokes this method.</p>
     *
     * @param enabled true if user input must be enabled, false otherwise
     * @see #setEnabled(boolean)
     * @see #isInputEnabled()
     */
    public void setInputEnabled(boolean enabled) {
        if (inputEnabled != enabled) {
            boolean old = isInputEnabled();
            this.inputEnabled = enabled;

            if (enabled) {
                addMouseListener(resetHandler);
                addMouseListener(panHandler);
                addMouseMotionListener(panMotionHandler);
                addMouseWheelListener(zoomHandler);
            } else {
                removeMouseListener(resetHandler);
                removeMouseListener(panHandler);
                removeMouseMotionListener(panMotionHandler);
                removeMouseWheelListener(zoomHandler);
            }

            firePropertyChange("inputEnabled", old, isInputEnabled());
        }
    }

    /**
     * <p>Defines whether or not user input is accepted and managed by this
     * component. The component is always created with user input enabled.</p>
     *
     * @return true if user input is enabled, false otherwise
     * @see #setInputEnabled(boolean)
     */
    public boolean isInputEnabled() {
        return inputEnabled;
    }

    /**
     * <p>Defines whether or not axis labels are painted by this component.
     * The component is always created with text painting enabled.</p>
     *
     * @return true if axis labels are painted, false otherwise
     * @see #setTextPainted(boolean)
     * @see #getForeground()
     */
    public boolean isTextPainted() {
        return textPainted;
    }

    /**
     * <p>Enables or disables the painting of axis labels depending on the
     * value of the parameter. Text painting is enabled by default.</p>
     *
     * @param textPainted if true, axis labels are painted
     * @see #isTextPainted()
     * @see #setForeground(Color)
     */
    public void setTextPainted(boolean textPainted) {
        boolean old = isTextPainted();
        this.textPainted = textPainted;
        firePropertyChange("textPainted", old, this.textPainted);
    }

    /**
     * <p>Defines whether or not grids lines are painted by this component.
     * The component is always created with grid lines painting enabled.</p>
     *
     * @return true if grid lines are painted, false otherwise
     * @see #setGridPainted(boolean)
     * @see #getMajorGridColor()
     * @see #getMinorGridColor()
     */
    public boolean isGridPainted() {
        return gridPainted;
    }

    /**
     * <p>Enables or disables the painting of grid lines depending on the
     * value of the parameter. Grid painting is enabled by default.</p>
     *
     * @param gridPainted if true, axis labels are painted
     * @see #isGridPainted()
     * @see #setMajorGridColor(Color)
     * @see #setMinorGridColor(Color)
     */
    public void setGridPainted(boolean gridPainted) {
        boolean old = isGridPainted();
        this.gridPainted = gridPainted;
        firePropertyChange("gridPainted", old, isGridPainted());
    }

    /**
     * <p>Defines whether or not the graph main axis is painted by this
     * component. The component is always created with main axis painting
     * enabled.</p>
     *
     * @return true if main axis is painted, false otherwise
     * @see #setTextPainted(boolean)
     * @see #getAxisColor()
     */
    public boolean isAxisPainted() {
        return axisPainted;
    }

    /**
     * <p>Enables or disables the painting of main axis depending on the
     * value of the parameter. Axis painting is enabled by default.</p>
     *
     * @param axisPainted if true, axis labels are painted
     * @see #isAxisPainted()
     * @see #setAxisColor(Color)
     */
    public void setAxisPainted(boolean axisPainted) {
        boolean old = isAxisPainted();
        this.axisPainted = axisPainted;
        firePropertyChange("axisPainted", old, isAxisPainted());
    }

    /**
     * <p>Defines whether or not the background painted by this component.
     * The component is always created with background painting enabled.
     * When background painting is disabled, background painting is deferred
     * to the parent class.</p>
     *
     * @return true if background is painted, false otherwise
     * @see #setBackgroundPainted(boolean)
     * @see #getBackground()
     */
    public boolean isBackgroundPainted() {
        return backPainted;
    }

    /**
     * <p>Enables or disables the painting of background depending on the
     * value of the parameter. Background painting is enabled by default.</p>
     *
     * @param backPainted if true, axis labels are painted
     * @see #isBackgroundPainted()
     * @see #setBackground(Color)
     */
    public void setBackgroundPainted(boolean backPainted) {
        boolean old = isBackgroundPainted();
        this.backPainted = backPainted;
        firePropertyChange("backgroundPainted", old, isBackgroundPainted());
    }

    /**
     * <p>Gets the major grid lines color of this component.</p>
     *
     * @return this component's major grid lines color
     * @see #setMajorGridColor(Color)
     * @see #setGridPainted(boolean)
     */
    public Color getMajorGridColor() {
        return majorGridColor;
    }

    /**
     * <p>Sets the color of major grid lines on this component. The color
     * can be translucent.</p>
     *
     * @param majorGridColor the color to become this component's major grid
     *                       lines color
     * @throws IllegalArgumentException if the specified color is null
     * @see #getMajorGridColor()
     * @see #isGridPainted()
     */
    public void setMajorGridColor(Color majorGridColor) {
        if (majorGridColor == null) {
            throw new IllegalArgumentException("Color cannot be null.");
        }

        Color old = getMajorGridColor();
        this.majorGridColor = majorGridColor;
        firePropertyChange("majorGridColor", old, getMajorGridColor());
    }

    /**
     * <p>Gets the minor grid lines color of this component.</p>
     *
     * @return this component's minor grid lines color
     * @see #setMinorGridColor(Color)
     * @see #setGridPainted(boolean)
     */
    public Color getMinorGridColor() {
        return minorGridColor;
    }

    /**
     * <p>Sets the color of minor grid lines on this component. The color
     * can be translucent.</p>
     *
     * @param minorGridColor the color to become this component's minor grid
     *                       lines color
     * @throws IllegalArgumentException if the specified color is null
     * @see #getMinorGridColor()
     * @see #isGridPainted()
     */
    public void setMinorGridColor(Color minorGridColor) {
        if (minorGridColor == null) {
            throw new IllegalArgumentException("Color cannot be null.");
        }

        Color old = getMinorGridColor();
        this.minorGridColor = minorGridColor;
        firePropertyChange("minorGridColor", old, getMinorGridColor());
    }

    /**
     * <p>Gets the main axis color of this component.</p>
     *
     * @return this component's main axis color
     * @see #setAxisColor(Color)
     * @see #setGridPainted(boolean)
     */
    public Color getAxisColor() {
        return axisColor;
    }

    /**
     * <p>Sets the color of main axis on this component. The color
     * can be translucent.</p>
     *
     * @param axisColor the color to become this component's main axis color
     * @throws IllegalArgumentException if the specified color is null
     * @see #getAxisColor()
     * @see #isAxisPainted()
     */
    public void setAxisColor(Color axisColor) {
        if (axisColor == null) {
            throw new IllegalArgumentException("Color cannot be null.");
        }

        Color old = getAxisColor();
        this.axisColor = axisColor;
        firePropertyChange("axisColor", old, getAxisColor());
    }

    /**
     * <p>Gets the distance, in graph units, between two major grid lines on
     * the X axis.</p>
     *
     * @return the spacing between two major grid lines on the X axis
     * @see #setMajorX(double)
     * @see #getMajorY()
     * @see #setMajorY(double)
     * @see #getMinorCountX()
     * @see #setMinorCountX(int)
     */
    public double getMajorX() {
        return majorX;
    }

    /**
     * <p>Sets the distance, in graph units, between two major grid lines on
     * the X axis.</p>
     *
     * @param majorX the requested spacing between two major grid lines on the
     *               X axis
     * @throws IllegalArgumentException if majorX is <= 0.0d
     * @see #getMajorX()
     * @see #getMajorY()
     * @see #setMajorY(double)
     * @see #getMinorCountX()
     * @see #setMinorCountX(int)
     */
    public void setMajorX(double majorX) {
        if (majorX <= 0.0) {
            throw new IllegalArgumentException("majorX must be > 0.0");
        }

        double old = getMajorX();
        this.majorX = majorX;
        this.defaultMajorX = majorX;
        repaint();
        firePropertyChange("majorX", old, getMajorX());
    }

    /**
     * <p>Gets the number of minor grid lines between two major grid lines
     * on the X axis.</p>
     *
     * @return the number of minor grid lines between two major grid lines
     * @see #setMinorCountX(int)
     * @see #getMinorCountY()
     * @see #setMinorCountY(int)
     * @see #getMajorX()
     * @see #setMajorX(double)
     */
    public int getMinorCountX() {
        return minorCountX;
    }

    /**
     * <p>Sets the number of minor grid lines between two major grid lines on
     * the X axis.</p>
     *
     * @param minorCountX the number of minor grid lines between two major grid
     *                    lines on the X axis
     * @throws IllegalArgumentException if minorCountX is < 0
     * @see #getMinorCountX()
     * @see #getMinorCountY()
     * @see #setMinorCountY(int)
     * @see #getMajorX()
     * @see #setMajorX(double)
     */
    public void setMinorCountX(int minorCountX) {
        if (minorCountX < 0) {
            throw new IllegalArgumentException("minorCountX must be >= 0");
        }

        int old = getMinorCountX();
        this.minorCountX = minorCountX;
        repaint();
        firePropertyChange("minorCountX", old, getMinorCountX());
    }

    /**
     * <p>Gets the distance, in graph units, between two major grid lines on
     * the Y axis.</p>
     *
     * @return the spacing between two major grid lines on the Y axis
     * @see #setMajorY(double)
     * @see #getMajorX()
     * @see #setMajorX(double)
     * @see #getMinorCountY()
     * @see #setMinorCountY(int)
     */
    public double getMajorY() {
        return majorY;
    }

    /**
     * <p>Sets the distance, in graph units, between two major grid lines on
     * the Y axis.</p>
     *
     * @param majorY the requested spacing between two major grid lines on the
     *               Y axis
     * @throws IllegalArgumentException if majorY is <= 0.0d
     * @see #getMajorY()
     * @see #getMajorX()
     * @see #setMajorX(double)
     * @see #getMinorCountY()
     * @see #setMinorCountY(int)
     */
    public void setMajorY(double majorY) {
        if (majorY <= 0.0) {
            throw new IllegalArgumentException("majorY must be > 0.0");
        }

        double old = getMajorY();
        this.majorY = majorY;
        this.defaultMajorY = majorY;
        repaint();
        firePropertyChange("majorY", old, getMajorY());
    }

    /**
     * <p>Gets the number of minor grid lines between two major grid lines
     * on the Y axis.</p>
     *
     * @return the number of minor grid lines between two major grid lines
     * @see #setMinorCountY(int)
     * @see #getMinorCountX()
     * @see #setMinorCountX(int)
     * @see #getMajorY()
     * @see #setMajorY(double)
     */
    public int getMinorCountY() {
        return minorCountY;
    }

    /**
     * <p>Sets the number of minor grid lines between two major grid lines on
     * the Y axis.</p>
     *
     * @param minorCountY the number of minor grid lines between two major grid
     *                    lines on the Y axis
     * @throws IllegalArgumentException if minorCountY is < 0
     * @see #getMinorCountY()
     * @see #getMinorCountX()
     * @see #setMinorCountX(int)
     * @see #getMajorY()
     * @see #setMajorY(double)
     */
    public void setMinorCountY(int minorCountY) {
        if (minorCountY < 0) {
            throw new IllegalArgumentException("minorCountY must be >= 0");
        }

        int old = getMinorCountY();
        this.minorCountY = minorCountY;
        repaint();
        firePropertyChange("minorCountY", old, getMinorCountY());
    }

    /**
     * <p>Sets the view and the origin of the graph at the same time. The view
     * minimum boundaries are defined by the location of the rectangle passed
     * as parameter. The width and height of the rectangle define the distance
     * between the minimum and maximum boundaries:</p>
     *
     * <ul>
     *   <li><i>minX</i>: bounds.getX()</li>
     *   <li><i>minY</i>: bounds.getY()</li>
     *   <li><i>maxY</i>: bounds.getMaxX() (minX + bounds.getWidth())</li>
     *   <li><i>maxX</i>: bounds.getMaxY() (minY + bounds.getHeight())</li>
     * </ul>
     *
     * <p>The origin is located at the center of the view. Its coordinates are
     * defined by calling bounds.getCenterX() and bounds.getCenterY().</p>
     *
     * @param bounds the rectangle defining the graph's view and its origin
     * @see #getView()
     * @see #setView(Rectangle2D)
     * @see #getOrigin()
     * @see #setOrigin(Point2D)
     */
    public void setViewAndOrigin(Rectangle2D bounds) {
        setView(bounds);
        setOrigin(new Point2D.Double(bounds.getCenterX(), bounds.getCenterY()));
    }

    /**
     * <p>Sets the view of the graph. The view minimum boundaries are defined by
     * the location of the rectangle passed as parameter. The width and height
     * of the rectangle define the distance between the minimum and maximum
     * boundaries:</p>
     *
     * <ul>
     *   <li><i>minX</i>: bounds.getX()</li>
     *   <li><i>minY</i>: bounds.getY()</li>
     *   <li><i>maxY</i>: bounds.getMaxX() (minX + bounds.getWidth())</li>
     *   <li><i>maxX</i>: bounds.getMaxY() (minY + bounds.getHeight())</li>
     * </ul>
     *
     * <p>If the specified view is null, nothing happens.</p>
     *
     * <p>Calling this method leaves the origin intact.</p>
     *
     * @param bounds the rectangle defining the graph's view and its origin
     * @see #getView()
     * @see #setViewAndOrigin(Rectangle2D)
     */
    public void setView(Rectangle2D bounds) {
        if (bounds == null) {
            return;
        }
        Rectangle2D old = getView();
        defaultView = new Rectangle2D.Double(bounds.getX(), bounds.getY(),
            bounds.getWidth(), bounds.getHeight());

        minX = defaultView.getMinX();
        maxX = defaultView.getMaxX();
        minY = defaultView.getMinY();
        maxY = defaultView.getMaxY();

        majorX = defaultMajorX;
        majorY = defaultMajorY;
        firePropertyChange("view", old, getView());
        repaint();
    }

    /**
     * <p>Gets the view of the graph. The returned rectangle defines the bounds
     * of the view as follows:</p>
     *
     * <ul>
     *   <li><i>minX</i>: bounds.getX()</li>
     *   <li><i>minY</i>: bounds.getY()</li>
     *   <li><i>maxY</i>: bounds.getMaxX() (minX + bounds.getWidth())</li>
     *   <li><i>maxX</i>: bounds.getMaxY() (minY + bounds.getHeight())</li>
     * </ul>
     *
     * @return the rectangle corresponding to the current view of the graph
     * @see #setView(Rectangle2D)
     * @see #setViewAndOrigin(Rectangle2D)
     */
    public Rectangle2D getView() {
        return new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
    }

    /**
     * <p>Resets the view to the default view if it has been changed by the user
     * by panning and zooming. The default view is defined by the view last
     * specified in a constructor call or a call to the methods
     * {@link #setView(Rectangle2D)} and
     * {@link #setViewAndOrigin(Rectangle2D)}.</p>
     *
     * @see #setView(Rectangle2D)
     * @see #setViewAndOrigin(Rectangle2D)
     */
    public void resetView() {
        setView(defaultView);
    }

    /**
     * <p>Sets the origin of the graph. The coordinates of the origin are
     * defined by the coordinates of the point passed as parameter.</p>
     *
     * <p>If the specified view is null, nothing happens.</p>
     *
     * <p>Calling this method leaves the view intact.</p>
     *
     * @param origin the coordinates of the new origin
     * @see #getOrigin()
     * @see #setViewAndOrigin(Rectangle2D)
     */
    public void setOrigin(Point2D origin) {
        if (origin == null) {
            return;
        }

        Point2D old = getOrigin();
        originX = origin.getX();
        originY = origin.getY();
        firePropertyChange("origin", old, getOrigin());
        repaint();
    }

    /**
     * <p>Gets the origin coordinates of the graph. The coordinates are
     * represented as an instance of <code>Point2D</code> and stored in
     * <code>double</code> format.</p>

     * @return the origin coordinates in double format
     * @see #setOrigin(Point2D)
     * @see #setViewAndOrigin(Rectangle2D)
     */
    public Point2D getOrigin() {
        return new Point2D.Double(originX, originY);
    }

    /**
     * <p>Adds one or more plots to the graph. These plots are associated to
     * a color used to draw them.</p>
     *
     * <p>If plotList is null or empty, nothing happens.</p>
     *
     * <p>This method is not thread safe and should be called only from the
     * EDT.</p>
     *
     * @param color    the color to be usd to draw the plots
     * @param plotList the list of plots to add to the graph
     * @throws IllegalArgumentException if color is null
     * @see #removePlots(JXGraph.Plot...)
     * @see #removeAllPlots()
     */
    public void addPlots(Color color, Plot... plotList) {
        if (color == null) {
            throw new IllegalArgumentException("Plots color cannot be null.");
        }

        if (plotList == null) {
            return;
        }

        for (Plot plot : plotList) {
            DrawablePlot drawablePlot =
                    new DrawablePlot(plot, color);
            if (plot != null && !plots.contains(drawablePlot)) {
                plot.addPropertyChangeListener(plotChangeListener);
                plots.add(drawablePlot);
            }
        }
        repaint();
    }

    /**
     * <p>Removes the specified plots from the graph. Plots to be removed
     * are identified by identity. This means you cannot remove a plot by
     * passing a clone or another instance of the same subclass of
     * {@link JXGraph.Plot}.</p>
     *
     * <p>If plotList is null or empty, nothing happens.</p>
     *
     * <p>This method is not thread safe and should be called only from the
     * EDT.</p>
     *
     * @param plotList the list of plots to be removed from the graph
     * @see #removeAllPlots()
     * @see #addPlots(Color, JXGraph.Plot...)
     */
    public void removePlots(Plot... plotList) {
        if (plotList == null) {
            return;
        }

        for (Plot plot : plotList) {
            if (plot != null) {
                DrawablePlot toRemove = null;
                for (DrawablePlot drawable: plots) {
                    if (drawable.getEquation() == plot) {
                        toRemove = drawable;
                        break;
                    }
                }

                if (toRemove != null) {
                    plot.removePropertyChangeListener(plotChangeListener);
                    plots.remove(toRemove);
                }
            }
        }
        repaint();
    }

    /**
     * <p>Removes all the plots currently associated with this graph.</p>
     *
     * <p>This method is not thread safe and should be called only from the
     * EDT.</p>
     *
     * @see #removePlots(JXGraph.Plot...)
     * @see #addPlots(Color, JXGraph.Plot...)
     */
    public void removeAllPlots() {
        plots.clear();
        repaint();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(400, 400);
    }

    /**
     * <p>Converts a position, in graph units, from the Y axis into a pixel
     * coordinate. For instance, if you defined the origin so it appears at the
     * exact center of the view, calling
     * <code>yPositionToPixel(getOriginY())</code> will return a value
     * approximately equal to <code>getHeight() / 2.0</code>.</p>
     *
     * @param position the Y position to be converted into pixels
     * @return the coordinate in pixels of the specified graph Y position
     * @see #xPositionToPixel(double)
     * @see #yPixelToPosition(double)
     */
    protected double yPositionToPixel(double position) {
        double height = getHeight();
        return height - ((position - minY) * height / (maxY - minY));
    }

    /**
     * <p>Converts a position, in graph units, from the X axis into a pixel
     * coordinate. For instance, if you defined the origin so it appears at the
     * exact center of the view, calling
     * <code>xPositionToPixel(getOriginX())</code> will return a value
     * approximately equal to <code>getWidth() / 2.0</code>.</p>
     *
     * @param position the X position to be converted into pixels
     * @return the coordinate in pixels of the specified graph X position
     * @see #yPositionToPixel(double)
     * @see #xPixelToPosition(double)
     */
    protected double xPositionToPixel(double position) {
        return (position - minX) * getWidth() / (maxX - minX);
    }

    /**
     * <p>Converts a pixel coordinate from the X axis into a graph position, in
     * graph units. For instance, if you defined the origin so it appears at the
     * exact center of the view, calling
     * <code>xPixelToPosition(getWidth() / 2.0)</code> will return a value
     * approximately equal to <code>getOriginX()</code>.</p>
     *
     * @param pixel the X pixel coordinate to be converted into a graph position
     * @return the graph X position of the specified pixel coordinate
     * @see #yPixelToPosition(double)
     * @see #xPositionToPixel(double)
     */
    protected double xPixelToPosition(double pixel) {
//        double axisV = xPositionToPixel(originX);
//        return (pixel - axisV) * (maxX - minX) / (double) getWidth();
        return minX + pixel * (maxX - minX) / getWidth();
    }

    /**
     * <p>Converts a pixel coordinate from the Y axis into a graph position, in
     * graph units. For instance, if you defined the origin so it appears at the
     * exact center of the view, calling
     * <code>yPixelToPosition(getHeight() / 2.0)</code> will return a value
     * approximately equal to <code>getOriginY()</code>.</p>
     *
     * @param pixel the Y pixel coordinate to be converted into a graph position
     * @return the graph Y position of the specified pixel coordinate
     * @see #xPixelToPosition(double)
     * @see #yPositionToPixel(double)
     */
    protected double yPixelToPosition(double pixel) {
//        double axisH = yPositionToPixel(originY);
//        return (getHeight() - pixel - axisH) * (maxY - minY) / (double) getHeight();
        return minY + (getHeight() - pixel) * (maxY - minY) / getHeight();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void paintComponent(Graphics g) {
        if (!isVisible()) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g;
        setupGraphics(g2);

        paintBackground(g2);
        drawGrid(g2);
        drawAxis(g2);
        drawPlots(g2);
        drawLabels(g2);

        paintExtra(g2);
    }

    /**
     * <p>This painting method is meant to be overridden by subclasses of
     * <code>JXGraph</code>. This method is called after all the painting
     * is done. By overriding this method, a subclass can display extra
     * information on top of the graph.</p>
     * <p>The graphics surface passed as parameter is configured by
     * {@link #setupGraphics(Graphics2D)}.</p>
     *
     * @param g2 the graphics surface on which the graph is drawn
     * @see #setupGraphics(Graphics2D)
     * @see #xPixelToPosition(double)
     * @see #yPixelToPosition(double)
     * @see #xPositionToPixel(double)
     * @see #yPositionToPixel(double)
     */
    protected void paintExtra(Graphics2D g2) {
    }

    // Draw all the registered plots with the appropriate color.
    private void drawPlots(Graphics2D g2) {
        for (DrawablePlot drawable: plots) {
            g2.setColor(drawable.getColor());
            drawPlot(g2, drawable.getEquation());
        }
    }

    // Draw a single plot as a GeneralPath made of straight lines.
    private void drawPlot(Graphics2D g2, Plot equation) {
        float x = 0.0f;
        float y = (float) yPositionToPixel(equation.compute(xPixelToPosition(0.0)));

        GeneralPath path = new GeneralPath();
        path.moveTo(x, y);

        float width = getWidth();
        for (x = 0.0f; x < width; x += 1.0f) {
            double position = xPixelToPosition(x);
            y = (float) yPositionToPixel(equation.compute(position));
            path.lineTo(x, y);
        }

        g2.draw(path);
    }

    // Draws the grid. First draw the vertical lines, then the horizontal lines.
    private void drawGrid(Graphics2D g2) {
        Stroke stroke = g2.getStroke();

        if (isGridPainted()) {
            drawVerticalGrid(g2);
            drawHorizontalGrid(g2);
        }

        g2.setStroke(stroke);
    }

    // Draw all labels. First draws labels on the horizontal axis, then labels
    // on the vertical axis. If the axis is set not to be painted, this
    // method draws the origin as a straight cross.
    private void drawLabels(Graphics2D g2) {
        if (isTextPainted()) {
            double axisH = yPositionToPixel(originY);
            double axisV = xPositionToPixel(originX);

            if (isAxisPainted()) {
                Stroke stroke = g2.getStroke();
                g2.setStroke(new BasicStroke(STROKE_AXIS));
                g2.setColor(getAxisColor());
                g2.drawLine((int) axisV - 3, (int) axisH,
                    (int) axisV + 3, (int) axisH);
                g2.drawLine((int) axisV, (int) axisH - 3,
                    (int) axisV, (int) axisH + 3);
                g2.setStroke(stroke);
            }

            g2.setColor(getForeground());
            FontMetrics metrics = g2.getFontMetrics();
            g2.drawString(format(originX) + "; " +
                format(originY), (int) axisV + 5,
                (int) axisH + metrics.getHeight());

            drawHorizontalAxisLabels(g2);
            drawVerticalAxisLabels(g2);
        }
    }

    // Draws labels on the vertical axis. First draws labels below the origin,
    // then draw labels on top of the origin.
    private void drawVerticalAxisLabels(Graphics2D g2) {
        double axisV = xPositionToPixel(originX);

//        double startY = Math.floor((minY - originY) / majorY) * majorY;
        double startY = Math.floor(minY / majorY) * majorY;
        for (double y = startY; y < maxY + majorY; y += majorY) {
            if (((y - majorY / 2.0) < originY) &&
                ((y + majorY / 2.0) > originY)) {
                continue;
            }

            int position = (int) yPositionToPixel(y);
            g2.drawString(format(y), (int) axisV + 5, position);
        }
    }

    // Draws the horizontal lines of the grid. Draws both minor and major
    // grid lines.
    private void drawHorizontalGrid(Graphics2D g2) {
        double minorSpacing = majorY / getMinorCountY();
        double axisV = xPositionToPixel(originX);

        Stroke gridStroke = new BasicStroke(STROKE_GRID);
        Stroke axisStroke = new BasicStroke(STROKE_AXIS);

        Rectangle clip = g2.getClipBounds();

        int position;

        if (!isAxisPainted()) {
            position = (int) xPositionToPixel(originX);
            if (position >= clip.x && position <= clip.x + clip.width) {
                g2.setColor(getMajorGridColor());
                g2.drawLine(position, clip.y, position, clip.y + clip.height);
            }
        }

//        double startY = Math.floor((minY - originY) / majorY) * majorY;
        double startY = Math.floor(minY / majorY) * majorY;
        for (double y = startY; y < maxY + majorY; y += majorY) {
            g2.setStroke(gridStroke);
            g2.setColor(getMinorGridColor());
            for (int i = 0; i < getMinorCountY(); i++) {
                position = (int) yPositionToPixel(y - i * minorSpacing);
                if (position >= clip.y && position <= clip.y + clip.height) {
                    g2.drawLine(clip.x, position, clip.x + clip.width, position);
                }
            }

            position = (int) yPositionToPixel(y);
            if (position >= clip.y && position <= clip.y + clip.height) {
                g2.setColor(getMajorGridColor());
                g2.drawLine(clip.x, position, clip.x + clip.width, position);

                if (isAxisPainted()) {
                    g2.setStroke(axisStroke);
                    g2.setColor(getAxisColor());
                    g2.drawLine((int) axisV - 3, position, (int) axisV + 3, position);
                }
            }
        }
    }

    // Draws labels on the horizontal axis. First draws labels on the right of
    // the origin, then on the left.
    private void drawHorizontalAxisLabels(Graphics2D g2) {
        double axisH = yPositionToPixel(originY);
        FontMetrics metrics = g2.getFontMetrics();

//        double startX = Math.floor((minX - originX) / majorX) * majorX;
        double startX = Math.floor(minX / majorX) * majorX;
        for (double x = startX; x < maxX + majorX; x += majorX) {
            if (((x - majorX / 2.0) < originX) &&
                ((x + majorX / 2.0) > originX)) {
                continue;
            }

            int position = (int) xPositionToPixel(x);
            g2.drawString(format(x), position,
                (int) axisH + metrics.getHeight());
        }
    }

    // Draws the vertical lines of the grid. Draws both minor and major
    // grid lines.
    private void drawVerticalGrid(Graphics2D g2) {
        double minorSpacing = majorX / getMinorCountX();
        double axisH = yPositionToPixel(originY);

        Stroke gridStroke = new BasicStroke(STROKE_GRID);
        Stroke axisStroke = new BasicStroke(STROKE_AXIS);

        Rectangle clip = g2.getClipBounds();

        int position;
        if (!isAxisPainted()) {
            position = (int) yPositionToPixel(originY);
            if (position >= clip.y && position <= clip.y + clip.height) {
                g2.setColor(getMajorGridColor());
                g2.drawLine(clip.x, position, clip.x + clip.width, position);
            }
        }

//        double startX = Math.floor((minX - originX) / majorX) * majorX;
        double startX = Math.floor(minX / majorX) * majorX;
        for (double x = startX; x < maxX + majorX; x += majorX) {
            g2.setStroke(gridStroke);
            g2.setColor(getMinorGridColor());
            for (int i = 0; i < getMinorCountX(); i++) {
                position = (int) xPositionToPixel(x - i * minorSpacing);
                if (position >= clip.x && position <= clip.x + clip.width) {
                    g2.drawLine(position, clip.y, position, clip.y + clip.height);
                }
            }

            position = (int) xPositionToPixel(x);
            if (position >= clip.x && position <= clip.x + clip.width) {
                g2.setColor(getMajorGridColor());
                g2.drawLine(position, clip.y, position, clip.y + clip.height);

                if (isAxisPainted()) {
                    g2.setStroke(axisStroke);
                    g2.setColor(getAxisColor());
                    g2.drawLine(position, (int) axisH - 3, position, (int) axisH + 3);
                }
            }
        }
    }

    // Drase the main axis.
    private void drawAxis(Graphics2D g2) {
        if (!isAxisPainted()) {
            return;
        }

        double axisH = yPositionToPixel(originY);
        double axisV = xPositionToPixel(originX);

        Rectangle clip = g2.getClipBounds();

        g2.setColor(getAxisColor());
        Stroke stroke = g2.getStroke();
        g2.setStroke(new BasicStroke(STROKE_AXIS));

        if (axisH >= clip.y && axisH <= clip.y + clip.height) {
            g2.drawLine(clip.x, (int) axisH, clip.x + clip.width, (int) axisH);
        }
        if (axisV >= clip.x && axisV <= clip.x + clip.width) {
            g2.drawLine((int) axisV, clip.y, (int) axisV, clip.y + clip.height);
        }

        g2.setStroke(stroke);
    }

    /**
     * <p>This method is called by the component prior to any drawing operation
     * to configure the drawing surface. The default implementation enables
     * antialiasing on the graphics.</p>
     * <p>This method can be overriden by subclasses to modify the drawing
     * surface before any painting happens.</p>
     *
     * @param g2 the graphics surface to set up
     * @see #paintExtra(Graphics2D)
     * @see #paintBackground(Graphics2D)
     */
    protected void setupGraphics(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
    }

    /**
     * <p>This method is called by the component whenever it needs to paint
     * its background. The default implementation fills the background with
     * a solid color as defined by {@link #getBackground()}. Background painting
     * does not happen when {@link #isBackgroundPainted()} returns false.</p>
     * <p>It is recommended to subclasses to honor the contract defined by
     * {@link #isBackgroundPainted()} and {@link #setBackgroundPainted(boolean)}.
     *
     * @param g2 the graphics surface on which the background must be drawn
     * @see #setupGraphics(Graphics2D)
     * @see #paintExtra(Graphics2D)
     * @see #isBackgroundPainted()
     * @see #setBackgroundPainted(boolean)
     */
    protected void paintBackground(Graphics2D g2) {
        if (isBackgroundPainted()) {
            Painter p = getBackgroundPainter();
            if (p != null) {
                p.paint(g2, this, getWidth(), getHeight());
            } else {
                g2.setColor(getBackground());
                g2.fill(g2.getClipBounds());
            }
        }
    }

    // Format a number with the appropriate number formatter. Numbers >= 0.01
    // and < 100 are formatted with a regular, 2-digits, numbers formatter.
    // Other numbers use a scientific notation given by a DecimalFormat instance
    private String format(double number) {
        boolean farAway = (number != 0.0d && Math.abs(number) < 0.01d) ||
            Math.abs(number) > 99.0d;
        return (farAway ? secondFormatter : mainFormatter).format(number);
    }

    /**
     * <p>A plot represents a mathematical transformation used by
     * {@link JXGraph}. When a plot belongs to a graph, the graph component
     * asks for the transformation of a value along the X axis. The resulting
     * value defines the Y coordinates at which the graph must draw a spot of
     * color.</p>
     *
     * <p>Here is a sample implementation of this class that draws a straight line
     * once added to a graph (it follows the well-known equation y=a.x+b):</p>
     *
     * <pre>
     * class LinePlot extends JXGraph.Plot {
     *     public double compute(double value) {
     *         return 2.0 * value + 1.0;
     *     }
     * }
     * </pre>
     *
     * <p>When a plot is added to an instance of
     * <code>JXGraph</code>, the <code>JXGraph</code> automatically becomes
     * a new property change listener of the plot. If property change events are
     * fired, the graph will be updated accordingly.</p>
     *
     * <p>More information about plots usage can be found in {@link JXGraph} in
     * the section entitled <i>Plots</i>.</p>
     *
     * @see JXGraph
     * @see JXGraph#addPlots(Color, JXGraph.Plot...)
     */
    public abstract static class Plot extends AbstractBean {
        /**
         * <p>Creates a new, parameter-less plot.</p>
         */
        protected Plot() {
        }

        /**
         * <p>This method must return the result of a mathematical
         * transformation of its sole parameter.</p>
         *
         * @param value a value along the X axis of the graph currently
         *              drawing this plot
         * @return the result of the mathematical transformation of value
         */
        public abstract double compute(double value);
    }

    // Encapsulates a plot and its color. Avoids the use of a full-blown Map.
    private static class DrawablePlot {
        private final Plot equation;
        private final Color color;

        private DrawablePlot(Plot equation, Color color) {
            this.equation = equation;
            this.color = color;
        }

        private Plot getEquation() {
            return equation;
        }

        private Color getColor() {
            return color;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final DrawablePlot that = (DrawablePlot) o;
            if (!color.equals(that.color)) {
                return false;
            }
            return equation.equals(that.equation);
        }

        @Override
        public int hashCode() {
            int result;
            result = equation.hashCode();
            result = 29 * result + color.hashCode();
            return result;
        }
    }

    // Shrinks or expand the view depending on the mouse wheel direction.
    // When the wheel moves down, the view is expanded. Otherwise it is shrunk.
    private class ZoomHandler implements MouseWheelListener {
        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            double distanceX = maxX - minX;
            double distanceY = maxY - minY;

            double cursorX = minX + distanceX / 2.0;
            double cursorY = minY + distanceY / 2.0;

            int rotation = e.getWheelRotation();
            if (rotation < 0) {
                distanceX /= ZOOM_MULTIPLIER;
                distanceY /= ZOOM_MULTIPLIER;

                majorX /= ZOOM_MULTIPLIER;
                majorY /= ZOOM_MULTIPLIER;
            } else {
                distanceX *= ZOOM_MULTIPLIER;
                distanceY *= ZOOM_MULTIPLIER;

                majorX *= ZOOM_MULTIPLIER;
                majorY *= ZOOM_MULTIPLIER;
            }

            minX = cursorX - distanceX / 2.0;
            maxX = cursorX + distanceX / 2.0;
            minY = cursorY - distanceY / 2.0;
            maxY = cursorY + distanceY / 2.0;

            repaint();
        }
    }

    // Listens for a click on the middle button of the mouse and resets the view
    private class ResetHandler extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            if (e.getButton() != MouseEvent.BUTTON2) {
                return;
            }

            resetView();
        }
    }

    // Starts and ends drag gestures with mouse left button.
    private class PanHandler extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            if (e.getButton() != MouseEvent.BUTTON1) {
                return;
            }

            dragStart = e.getPoint();
            setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.getButton() != MouseEvent.BUTTON1) {
                return;
            }

            setCursor(Cursor.getDefaultCursor());
        }
    }

    // Handles drag gesture with the left mouse button and relocates the view
    // accordingly.
    private class PanMotionHandler extends MouseMotionAdapter {
        @Override
        public void mouseDragged(MouseEvent e) {
            Point dragEnd = e.getPoint();

            double distance = xPixelToPosition(dragEnd.getX()) -
                              xPixelToPosition(dragStart.getX());
            minX = minX - distance;
            maxX = maxX - distance;

            distance = yPixelToPosition(dragEnd.getY()) -
                       yPixelToPosition(dragStart.getY());
            minY = minY - distance;
            maxY = maxY - distance;

            repaint();
            dragStart = dragEnd;
        }
    }
}
