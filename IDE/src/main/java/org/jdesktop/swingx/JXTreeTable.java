/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.jdesktop.swingx;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.EventObject;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.ActionMap;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.jdesktop.beans.JavaBean;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.event.TreeExpansionBroadcaster;
import org.jdesktop.swingx.plaf.UIAction;
import org.jdesktop.swingx.renderer.StringValue;
import org.jdesktop.swingx.renderer.StringValues;
import org.jdesktop.swingx.rollover.RolloverProducer;
import org.jdesktop.swingx.rollover.RolloverRenderer;
import org.jdesktop.swingx.tree.DefaultXTreeCellRenderer;
import org.jdesktop.swingx.treetable.DefaultTreeTableModel;
import org.jdesktop.swingx.treetable.TreeTableCellEditor;
import org.jdesktop.swingx.treetable.TreeTableModel;
import org.jdesktop.swingx.treetable.TreeTableModelProvider;
import org.jdesktop.swingx.util.Contract;

/**
 * <p><code>JXTreeTable</code> is a specialized {@link javax.swing.JTable table}
 * consisting of a single column in which to display hierarchical data, and any
 * number of other columns in which to display regular data. The interface for
 * the data model used by a <code>JXTreeTable</code> is
 * {@link org.jdesktop.swingx.treetable.TreeTableModel}. It extends the
 * {@link javax.swing.tree.TreeModel} interface to allow access to cell data by
 * column indices within each node of the tree hierarchy.</p>
 *
 * <p>The most straightforward way create and use a <code>JXTreeTable</code>, is to
 * first create a suitable data model for it, and pass that to a
 * <code>JXTreeTable</code> constructor, as shown below:
 * <pre>
 *  TreeTableModel  treeTableModel = new FileSystemModel(); // any TreeTableModel
 *  JXTreeTable     treeTable = new JXTreeTable(treeTableModel);
 *  JScrollPane     scrollpane = new JScrollPane(treeTable);
 * </pre>
 * See {@link javax.swing.JTable} for an explanation of why putting the treetable
 * inside a scroll pane is necessary.</p>
 *
 * <p>A single treetable model instance may be shared among more than one
 * <code>JXTreeTable</code> instances. To access the treetable model, always call
 * {@link #getTreeTableModel() getTreeTableModel} and
 * {@link #setTreeTableModel(org.jdesktop.swingx.treetable.TreeTableModel) setTreeTableModel}.
 * <code>JXTreeTable</code> wraps the supplied treetable model inside a private
 * adapter class to adapt it to a {@link javax.swing.table.TableModel}. Although
 * the model adapter is accessible through the {@link #getModel() getModel} method, you
 * should avoid accessing and manipulating it in any way. In particular, each
 * model adapter instance is tightly bound to a single table instance, and any
 * attempt to share it with another table (for example, by calling
 * {@link #setModel(javax.swing.table.TableModel) setModel})
 * will throw an <code>IllegalArgumentException</code>!
 *
 * <b>Note</b>: <p>
 * This implementation is basically as hacky as the very first version
 * more than a decaded ago: the renderer of the hierarchical column is a
 * JXTree which is trickst into painting a single row at the position of
 * the table cell. TreeModel changes must be adapted to TableModel changes
 * <i>after</i> the tree received them, that is the TableModel events are asynchronous
 * as compared to their base trigger. As a consequence, the adapted TableModel
 * doesn't play nicely when shared in other J/X/Tables (f.i. used as rowHeader -
 * see http://java.net/jira/browse/SWINGX-1529)
 *
 * @author Philip Milne
 * @author Scott Violet
 * @author Ramesh Gupta
 *
 */
@JavaBean
public class JXTreeTable extends JXTable {
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(JXTreeTable.class
            .getName());
    /**
     * Key for clientProperty to decide whether to apply hack around #168-jdnc.
     */
    public static final String DRAG_HACK_FLAG_KEY = "treeTable.dragHackFlag";
    /**
     * Key for clientProperty to decide whether to apply hack around #766-swingx.
     */
    public static final String DROP_HACK_FLAG_KEY = "treeTable.dropHackFlag";
    /**
     * Renderer used to render cells within the
     *  {@link #isHierarchical(int) hierarchical} column.
     *  renderer extends JXTree and implements TableCellRenderer
     */
    private TreeTableCellRenderer renderer;

    /**
     * Editor used to edit cells within the
     *  {@link #isHierarchical(int) hierarchical} column.
     */
    private TreeTableCellEditor hierarchicalEditor;

    private TreeTableHacker treeTableHacker;
    private boolean consumedOnPress;
    private TreeExpansionBroadcaster treeExpansionBroadcaster;

    /**
     * Constructs a JXTreeTable using a
     * {@link org.jdesktop.swingx.treetable.DefaultTreeTableModel}.
     */
    public JXTreeTable() {
        this(new DefaultTreeTableModel());
    }

    /**
     * Constructs a JXTreeTable using the specified
     * {@link org.jdesktop.swingx.treetable.TreeTableModel}.
     *
     * @param treeModel model for the JXTreeTable
     */
    public JXTreeTable(TreeTableModel treeModel) {
        this(new JXTreeTable.TreeTableCellRenderer(treeModel));
    }

    /**
     * Constructs a <code>JXTreeTable</code> using the specified
     * {@link org.jdesktop.swingx.JXTreeTable.TreeTableCellRenderer}.
     *
     * @param renderer
     *                cell renderer for the tree portion of this JXTreeTable
     *                instance.
     */
    private JXTreeTable(TreeTableCellRenderer renderer) {
        // To avoid unnecessary object creation, such as the construction of a
        // DefaultTableModel, it is better to invoke
        // super(TreeTableModelAdapter) directly, instead of first invoking
        // super() followed by a call to setTreeTableModel(TreeTableModel).

        // Adapt tree model to table model before invoking super()
        super(new TreeTableModelAdapter(renderer));

        // renderer-related initialization
        init(renderer); // private method
        initActions();
        // disable sorting
        super.setSortable(false);
        super.setAutoCreateRowSorter(false);
        super.setRowSorter(null);
        // no grid
        setShowGrid(false, false);

        hierarchicalEditor = new TreeTableCellEditor(renderer);

//        // No grid.
//        setShowGrid(false); // superclass default is "true"
//
//        // Default intercell spacing
//        setIntercellSpacing(spacing); // for both row margin and column margin

    }

    /**
     * Initializes this JXTreeTable and permanently binds the specified renderer
     * to it.
     *
     * @param renderer private tree/renderer permanently and exclusively bound
     * to this JXTreeTable.
     */
    private void init(TreeTableCellRenderer renderer) {
        this.renderer = renderer;
        assert ((TreeTableModelAdapter) getModel()).tree == this.renderer;

        // Force the JTable and JTree to share their row selection models.
        ListToTreeSelectionModelWrapper selectionWrapper =
            new ListToTreeSelectionModelWrapper();

        // JW: when would that happen?
        if (renderer != null) {
            renderer.bind(this); // IMPORTANT: link back!
            renderer.setSelectionModel(selectionWrapper);
        }
        // adjust the tree's rowHeight to this.rowHeight
        adjustTreeRowHeight(getRowHeight());
        adjustTreeBounds();
        setSelectionModel(selectionWrapper.getListSelectionModel());

        // propagate the lineStyle property to the renderer
        PropertyChangeListener l = new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                JXTreeTable.this.renderer.putClientProperty(evt.getPropertyName(), evt.getNewValue());

            }

        };
        addPropertyChangeListener("JTree.lineStyle", l);

    }

    private void initActions() {
        // Register the actions that this class can handle.
        ActionMap map = getActionMap();
        map.put("expand-all", new Actions("expand-all"));
        map.put("collapse-all", new Actions("collapse-all"));
    }

    /**
     * A small class which dispatches actions.
     * TODO: Is there a way that we can make this static?
     */
    private class Actions extends UIAction {
        Actions(String name) {
            super(name);
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            if ("expand-all".equals(getName())) {
        expandAll();
            }
            else if ("collapse-all".equals(getName())) {
                collapseAll();
            }
        }
    }

    /**
     * {@inheritDoc} <p>
     * Overridden to do nothing.
     *
     * TreeTable is not sortable because there is no equivalent to
     * RowSorter (which is targeted to linear structures) for
     * hierarchical data.
     *
     */
    @Override
    public void setSortable(boolean sortable) {
        // no-op
    }

    /**
     * {@inheritDoc} <p>
     * Overridden to do nothing.
     *
     * TreeTable is not sortable because there is no equivalent to
     * RowSorter (which is targeted to linear structures) for
     * hierarchical data.
     *
     */
    @Override
    public void setAutoCreateRowSorter(boolean autoCreateRowSorter) {
    }

    /**
     * {@inheritDoc} <p>
     * Overridden to do nothing.
     *
     * TreeTable is not sortable because there is no equivalent to
     * RowSorter (which is targeted to linear structures) for
     * hierarchical data.
     *
     */
    @Override
    public void setRowSorter(RowSorter<? extends TableModel> sorter) {
    }

    /**
     * Hook into super's setAutoCreateRowSorter for use in sub-classes which want to experiment
     * with tree table sorting/filtering.<p>
     *
     * <strong> NOTE: While subclasses may use this method to allow access to
     * super that usage alone will not magically turn sorting/filtering on! They have
     * to implement an appropriate RowSorter/SortController
     * as well. This is merely a hook to hang themselves, as requested in Issue #479-swingx
     * </strong>
     *
     * @param autoCreateRowSorter
     */
    protected void superSetAutoCreateRowSorter(boolean autoCreateRowSorter) {
        super.setAutoCreateRowSorter(autoCreateRowSorter);
    }

    /**
     * Hook into super's setSortable for use in sub-classes which want to experiment
     * with tree table sorting/filtering.<p>
     *
     * <strong> NOTE: While subclasses may use this method to allow access to
     * super that usage alone will not magically turn sorting/filtering on! They have
     * to implement an appropriate RowSorter/SortController
     * as well. This is merely a hook to hang themselves, as requested in Issue #479-swingx
     * </strong>
     *
     * @param sortable
     */
    protected void superSetSortable(boolean sortable) {
        super.setSortable(sortable);
    }

    /**
     * Hook into super's setRowSorter for use in sub-classes which want to experiment
     * with tree table sorting/filtering.<p>
     *
     * <strong> NOTE: While subclasses may use this method to allow access to
     * super that usage alone will not magically turn sorting/filtering on! They have
     * to implement an appropriate RowSorter/SortController
     * as well. This is merely a hook to hang themselves, as requested in Issue #479-swingx
     * </strong>
     *
     * @param sorter
     */
    protected void superSetRowSorter(RowSorter <? extends TableModel> sorter) {
        super.setRowSorter(sorter);
    }

    /**
     * {@inheritDoc} <p>
     *
     * Overridden to keep the tree's enabled in synch.
     */
    @Override
    public void setEnabled(boolean enabled) {
        renderer.setEnabled(enabled);
        super.setEnabled(enabled);
    }

    /**
     * {@inheritDoc} <p>
     *
     * Overridden to keep the tree's selectionBackground in synch.
     */
    @Override
    public void setSelectionBackground(Color selectionBackground) {
        // happens on instantiation, updateUI is called before the renderer is installed
        if (renderer != null)
            renderer.setSelectionBackground(selectionBackground);
        super.setSelectionBackground(selectionBackground);
    }

    /**
     * {@inheritDoc} <p>
     *
     * Overridden to keep the tree's selectionForeground in synch.
     */
    @Override
    public void setSelectionForeground(Color selectionForeground) {
        // happens on instantiation, updateUI is called before the renderer is installed
        if (renderer != null)
            renderer.setSelectionForeground(selectionForeground);
        super.setSelectionForeground(selectionForeground);
    }

    /**
     * Overriden to invoke repaint for the particular location if
     * the column contains the tree. This is done as the tree editor does
     * not fill the bounds of the cell, we need the renderer to paint
     * the tree in the background, and then draw the editor over it.
     * You should not need to call this method directly. <p>
     *
     * Additionally, there is tricksery involved to expand/collapse
     * the nodes.
     *
     * {@inheritDoc}
     */
    @Override
    public boolean editCellAt(int row, int column, EventObject e) {
        getTreeTableHacker().hitHandleDetectionFromEditCell(column, e);    // RG: Fix Issue 49!
        boolean canEdit = super.editCellAt(row, column, e);
        if (canEdit && isHierarchical(column)) {
            repaint(getCellRect(row, column, false));
        }
        return canEdit;
    }

    /**
     * Overridden to enable hit handle detection a mouseEvent which triggered
     * a expand/collapse.
     */
    @Override
    protected void processMouseEvent(MouseEvent e) {
        // BasicTableUI selects on released if the pressed had been
        // consumed. So we try to fish for the accompanying released
        // here and consume it as wll.
        if ((e.getID() == MouseEvent.MOUSE_RELEASED) && consumedOnPress) {
            consumedOnPress = false;
            e.consume();
            return;
        }
        if (getTreeTableHacker().hitHandleDetectionFromProcessMouse(e)) {
            // Issue #332-swing: hacking around selection loss.
            // prevent the
            // _table_ selection by consuming the mouseEvent
            // if it resulted in a expand/collapse
            consumedOnPress = true;
            e.consume();
            return;
        }
        consumedOnPress = false;
        super.processMouseEvent(e);
    }

    protected TreeTableHacker getTreeTableHacker() {
        if (treeTableHacker == null) {
            treeTableHacker = createTreeTableHacker();
        }
        return treeTableHacker;
    }

    /**
     * Hacking around various issues. Subclass and let it return
     * your favourite. The current default is TreeTableHackerExt5 (latest
     * evolution to work around #1230), the old long-standing default was
     * TreeTableHackerExt3. If you experience problems with the latest, please
     * let us know.
     *
     * @return
     */
    protected TreeTableHacker createTreeTableHacker() {
//        return new TreeTableHacker();
//        return new TreeTableHackerExt();
//        return new TreeTableHackerExt2();
//        return new TreeTableHackerExt3();
//        return new TreeTableHackerExt4();
        return new TreeTableHackerExt5();
    }

    private boolean processMouseMotion = true;

    @Override
    protected void processMouseMotionEvent(MouseEvent e) {
        if (processMouseMotion)
                super.processMouseMotionEvent(e);
    }

    /**
     * This class extends TreeTableHackerExt instead of TreeTableHackerExt3 so
     * as to serve as a clue that it is a complete overhaul and looking in
     * TreeTableHackerExt2 and TreeTableHackerExt3 for methods to change the
     * behavior will do you no good.
     * <p>
     * The methods previously used are abandoned as they would be misnomers to
     * the behavior as implemented in this class.
     * <p>
     * Changes:
     * <ol>
     * <li>
     * According to TreeTableHackerExt3, clickCounts > 1 are not sent to the
     * JTree so that double clicks will start edits (Issue #474). Well, mouse
     * events are only sent to the JTree if they occur within the tree handle
     * space - so that is not the behavior desired. Double clicks on the
     * text/margin opposite the tree handle already started edits without that
     * modification (I checked). The only thing that modification does is
     * introduce bugs when one actually double clicks on a tree handle... so
     * that idea was abandoned.</li>
     * <li>
     * There is no longer any discrimination between events that cause an
     * expansion/collapse. Since the event location is check to see if it is in
     * the tree handle margin area, this doesn't seem necessary. Plus it is more
     * user friendly: if someone missed the tree handle by 1 pixel, then it
     * caused a selection change instead of a node expansion/ collapse.</li>
     * <li>
     * The consumption of events are handled within this class itself because
     * the behavior associated with the way that <code>processMouseEvent(MouseEvent)</code>
     * consumed events was incompatible with the way this
     * class does things. As a consequence,
     * <code>hitHandleDetectionFromProcessMouse(MouseEvent)</code>
     * always returns false so that <code>processMoueEvent(MouseEvent)</code> will not
     * doing anything other than call its super
     * method.</li>
     * <li>
     * All events of type MOUSE_PRESSED, MOUSE_RELEASED, and MOUSE_CLICKED, but
     * excluding when <code>isPopupTrigger()</code> returns true, are sent to
     * the JTree. This has the added benefit of not having to piggy back a mouse
     * released event as we can just use the real mouse released event. This
     * keeps the look and feel consistent for the user of UI's that
     * expand/collapse nodes on the release of the mouse.</li>
     * <li>
     * The previous implementations have a spiel about avoiding events with
     * modifiers because the UI might try to change the selection. Well that
     * didn't occur in any of the look and feels I tested. Perhaps that was the
     * case if events that landed within the content area of a node were sent to
     * the JTree. If that behavior is actually necessary, then it can be added
     * to the <code>isTreeHandleEventType(MouseEvent)</code> method. This
     * implementation sends all events regardless of the modifiers.</li>
     * <li>
     * This implementation toggles the processing of mouse motion events. When
     * events are sent to the tree, it is turned off and turned back on when an
     * event is not sent to the tree. This fixes selection changes that occur
     * when one drags the mouse after pressing on a tree handle.</li>
     * </ol>
     *
     * contributed by member aephyr@dev.java.net
     */
    public class TreeTableHackerExt4 extends TreeTableHackerExt {

        /**
         * Filter to find mouse events that are candidates for node expansion/
         * collapse. MOUSE_PRESSED and MOUSE_RELEASED are used by default UIs.
         * MOUSE_CLICKED is included as it may be used by a custom UI.
         *
         * @param e the currently dispatching mouse event
         * @return true if the event is a candidate for sending to the JTree
         */
        protected boolean isTreeHandleEventType(MouseEvent e) {
            switch (e.getID()) {
            case MouseEvent.MOUSE_CLICKED:
            case MouseEvent.MOUSE_PRESSED:
            case MouseEvent.MOUSE_RELEASED:
                return !e.isPopupTrigger();
            }
            return false;
        }

        /**
         * This method checks if the location of the event is in the tree handle
         * margin and translates the coordinates for the JTree.
         *
         * @param e the currently dispatching mouse event
         * @return the mouse event to dispatch to the JTree or null if nothing
         *         should be dispatched
         */
        protected MouseEvent getEventForTreeRenderer(MouseEvent e) {
            Point pt = e.getPoint();
            int col = columnAtPoint(pt);
            if (col >= 0 && isHierarchical(col)) {
                int row = rowAtPoint(pt);
                if (row >= 0) {
                    // There will not be a check to see if the y coordinate is in range
                    // because the use of row = rowAtPoint(pt) will only return
                    // a row that has the y coordinates in the range of our point.
                    Rectangle cellBounds = getCellRect(row, col, false);
                    int x = e.getX() - cellBounds.x;
                    Rectangle nodeBounds = renderer.getRowBounds(row);
                    // The renderer's component orientation is checked because that
                    // is the one that really matters. Though it seems to always be
                    // in sync with the JXTreeTable's component orientation, maybe
                    // someone wants them to be different for some reason.
                    if (renderer.getComponentOrientation().isLeftToRight() ? x < nodeBounds.x
                            : x > nodeBounds.x + nodeBounds.width) {
                        return new MouseEvent(renderer, e.getID(), e.getWhen(),
                                e.getModifiers(), x, e.getY(),
                                e.getXOnScreen(), e.getYOnScreen(), e
                                        .getClickCount(), false, e.getButton());
                    }
                }
            }
            return null;
        }

        /**
         *
         * @return this method always returns false, so that processMouseEvent
         *         always just simply calls its super method
         */
        @Override
        public boolean hitHandleDetectionFromProcessMouse(MouseEvent e) {
            if (!isHitDetectionFromProcessMouse())
                return false;
            if (isTreeHandleEventType(e)) {
                MouseEvent newE = getEventForTreeRenderer(e);
                if (newE != null) {
                    renderer.dispatchEvent(newE);
                    if (shouldDisableMouseMotionOnTable(e)) {
                        // This fixes the issue of drags on tree handles
                        // (often unintentional) from selecting all nodes from the
                        // anchor to the node of said tree handle.
                        processMouseMotion = false;
                        // part of 561-swingx: if focus elsewhere and dispatching the
                        // mouseEvent the focus doesn't move from elsewhere
                        // still doesn't help in very first click after startup
                        // probably lead of row selection event not correctly
                        // updated on synch from treeSelectionModel
                        requestFocusInWindow();
                    } else {
                        processMouseMotion = true;
                    }
                    e.consume();
                    // Return false to prevent JXTreeTable.processMouseEvent(MouseEvent)
                    // from stopping the processing of the event. This allows the
                    // listeners to see the event even though it is consumed
                    // (perhaps useful for a user supplied listener). A proper UI
                    // listener will ignore consumed events.
                    return false;
                    // alternatively, you would have to use: return e.getID() == MouseEvent.MOUSE_PRESSED;
                    // because JXTreeTable.processMouseEvent(MouseEvent) assumes true
                    // will only be returned for MOUSE_PRESSED events. Also, if true
                    // were to be returned, then you'd have to piggy back a released
                    // event as the previous implementation does, because the actual
                    // released event would never reach this method.
                }
            }
            processMouseMotion = true;
            return false;
        }

        /**
         * Returns a boolean indicating whether mouseMotionEvents to the
         * table should be disabled. This is called from hitHandleDetectionFromMouseEvent
         * if the event was passed to the rendering tree and consumed. Returning
         * true has the side-effect of requesting focus to the table.<p>
         *
         * NOTE JW: this was extracted to from the calling method to fix
         * Issue #1527-swingx (no tooltips on JXTreeTable after expand/collapse)
         * and at the same time allow subclasses to further hack around ... <p>
         *
         * @param e the mouseEvent that was routed to the renderer.
         * @return true if disabling mouseMotionEvents to table, false if enabling them
         */
        protected boolean shouldDisableMouseMotionOnTable(MouseEvent e) {
            return processMouseMotion && e.getID() == MouseEvent.MOUSE_PRESSED;
        }
    }

    /*
     * Changed to calculate the area of the tree handle and only forward mouse
     * events to the tree if the event lands within that area. This keeps the
     * selection behavior consistent with TreeTableHackerExt3.
     *
     * contributed by member aephyr@dev.java.net
     */
    public class TreeTableHackerExt5 extends TreeTableHackerExt4 {

        /**
         * If a negative number is returned, then all events that occur in the
         * leading margin will be forwarded to the tree and consumed.
         *
         * @return the width of the tree handle if it can be determined, else -1
         */
        protected int getTreeHandleWidth() {
            if (renderer.getUI() instanceof BasicTreeUI) {
                BasicTreeUI ui = (BasicTreeUI) renderer.getUI();
                return ui.getLeftChildIndent() + ui.getRightChildIndent();
            } else {
                return -1;
            }
        }

        @Override
        protected MouseEvent getEventForTreeRenderer(MouseEvent e) {
            Point pt = e.getPoint();
            int col = columnAtPoint(pt);
            if (col >= 0 && isHierarchical(col)) {
                int row = rowAtPoint(pt);
                // There will not be a check to see if the y coordinate is in
                // range
                // because the use of row = rowAtPoint(pt) will only return a
                // row
                // that has the y coordinates in the range of our point.
                if (row >= 0) {
                    TreePath path = getPathForRow(row);
                    Object node = path.getLastPathComponent();
                    // Check if the node has a tree handle and if so, check
                    // if the event location falls over the tree handle.
                    if (!getTreeTableModel().isLeaf(node)
                            && (getTreeTableModel().getChildCount(node) > 0 || !renderer
                                    .hasBeenExpanded(path))) {
                        Rectangle cellBounds = getCellRect(row, col, false);
                        int x = e.getX() - cellBounds.x;
                        Rectangle nb = renderer.getRowBounds(row);
                        int thw = getTreeHandleWidth();
                        // The renderer's component orientation is checked
                        // because that
                        // is the one that really matters. Though it seems to
                        // always be
                        // in sync with the JXTreeTable's component orientation,
                        // maybe
                        // someone wants them to be different for some reason.
                        if (renderer.getComponentOrientation().isLeftToRight() ? x < nb.x
                                && (thw < 0 || x > nb.x - thw)
                                : x > nb.x + nb.width
                                        && (thw < 0 || x < nb.x + nb.width
                                                + thw)) {
                            return new MouseEvent(renderer, e.getID(), e
                                    .getWhen(), e.getModifiers(), x, e.getY(),
                                    e.getXOnScreen(), e.getYOnScreen(), e
                                            .getClickCount(), false, e
                                            .getButton());
                        }
                    }
                }
            }
            return null;
        }

    }


    /**
     * Temporary class to have all the hacking at one place. Naturally, it will
     * change a lot. The base class has the "stable" behaviour as of around
     * jun2006 (before starting the fix for 332-swingx). <p>
     *
     * specifically:
     *
     * <ol>
     * <li> hitHandleDetection triggeredn in editCellAt
     * </ol>
     *
     */
    public class TreeTableHacker {

        protected boolean expansionChangedFlag;

        /**
         * Decision whether the handle hit detection
         *   should be done in processMouseEvent or editCellAt.
         * Here: returns false.
         *
         * @return true for handle hit detection in processMouse, false
         *   for editCellAt.
         */
        protected boolean isHitDetectionFromProcessMouse() {
            return false;
        }

        /**
        * Entry point for hit handle detection called from editCellAt,
        * does nothing if isHitDetectionFromProcessMouse is true;
        *
        * @see #isHitDetectionFromProcessMouse()
        */
        public void hitHandleDetectionFromEditCell(int column, EventObject e) {
            if (!isHitDetectionFromProcessMouse()) {
                expandOrCollapseNode(column, e);
            }
        }

        /**
         * Entry point for hit handle detection called from processMouse.
         * Does nothing if isHitDetectionFromProcessMouse is false.
         *
         * @return true if the mouseEvent triggered an expand/collapse in
         *   the renderer, false otherwise.
         *
         * @see #isHitDetectionFromProcessMouse()
         */
        public boolean hitHandleDetectionFromProcessMouse(MouseEvent e) {
            if (!isHitDetectionFromProcessMouse())
                return false;
            int col = columnAtPoint(e.getPoint());
            return ((col >= 0) && expandOrCollapseNode(columnAtPoint(e
                    .getPoint()), e));
        }

        /**
         * Complete editing if collapsed/expanded.
         * <p>
         *
         * Is: first try to stop editing before falling back to cancel.
         * <p>
         * This is part of fix for #730-swingx - editingStopped not always
         * called. The other part is to call this from the renderer before
         * expansion related state has changed.
         * <p>
         *
         * Was: any editing is always cancelled.
         * <p>
         * This is a rude fix to #120-jdnc: data corruption on collapse/expand
         * if editing. This is called from the renderer after expansion related
         * state has changed.
         *
         */
        protected void completeEditing() {
            // JW: fix for 1126 - ignore complete if not editing hierarchical
            // reverted - introduced regression .... for details please see the bug report
            if (isEditing()) { // && isHierarchical(getEditingColumn())) {
                boolean success = getCellEditor().stopCellEditing();
                if (!success) {
                    getCellEditor().cancelCellEditing();
                }
            }
        }

        /**
         * Tricksery to make the tree expand/collapse.
         * <p>
         *
         * This might be - indirectly - called from one of two places:
         * <ol>
         * <li> editCellAt: original, stable but buggy (#332, #222) the table's
         * own selection had been changed due to the click before even entering
         * into editCellAt so all tree selection state is lost.
         *
         * <li> processMouseEvent: the idea is to catch the mouseEvent, check
         * if it triggered an expanded/collapsed, consume and return if so or
         * pass to super if not.
         * </ol>
         *
         * <p>
         * widened access for testing ...
         *
         *
         * @param column the column index under the event, if any.
         * @param e the event which might trigger a expand/collapse.
         *
         * @return this methods evaluation as to whether the event triggered a
         *         expand/collaps
         */
        protected boolean expandOrCollapseNode(int column, EventObject e) {
            if (!isHierarchical(column))
                return false;
            if (!mightBeExpansionTrigger(e))
                return false;
            boolean changedExpansion = false;
            MouseEvent me = (MouseEvent) e;
            if (hackAroundDragEnabled(me)) {
                /*
                 * Hack around #168-jdnc: dirty little hack mentioned in the
                 * forum discussion about the issue: fake a mousePressed if drag
                 * enabled. The usability is slightly impaired because the
                 * expand/collapse is effectively triggered on released only
                 * (drag system intercepts and consumes all other).
                 */
                me = new MouseEvent((Component) me.getSource(),
                        MouseEvent.MOUSE_PRESSED, me.getWhen(), me
                                .getModifiers(), me.getX(), me.getY(), me
                                .getClickCount(), me.isPopupTrigger());

            }
            // If the modifiers are not 0 (or the left mouse button),
            // tree may try and toggle the selection, and table
            // will then try and toggle, resulting in the
            // selection remaining the same. To avoid this, we
            // only dispatch when the modifiers are 0 (or the left mouse
            // button).
            if (me.getModifiers() == 0
                    || me.getModifiers() == InputEvent.BUTTON1_MASK) {
                MouseEvent pressed = new MouseEvent(renderer, me.getID(), me
                        .getWhen(), me.getModifiers(), me.getX()
                        - getCellRect(0, column, false).x, me.getY(), me
                        .getClickCount(), me.isPopupTrigger());
                renderer.dispatchEvent(pressed);
                // For Mac OS X, we need to dispatch a MOUSE_RELEASED as well
                MouseEvent released = new MouseEvent(renderer,
                        java.awt.event.MouseEvent.MOUSE_RELEASED, pressed
                                .getWhen(), pressed.getModifiers(), pressed
                                .getX(), pressed.getY(), pressed
                                .getClickCount(), pressed.isPopupTrigger());
                renderer.dispatchEvent(released);
                if (expansionChangedFlag) {
                    changedExpansion = true;
                }
            }
            expansionChangedFlag = false;
            return changedExpansion;
        }

        protected boolean mightBeExpansionTrigger(EventObject e) {
            if (!(e instanceof MouseEvent)) return false;
            MouseEvent me = (MouseEvent) e;
            if (!SwingUtilities.isLeftMouseButton(me)) return false;
            return me.getID() == MouseEvent.MOUSE_PRESSED;
        }

        /**
         * called from the renderer's setExpandedPath after
         * all expansion-related updates happend.
         *
         */
        protected void expansionChanged() {
            expansionChangedFlag = true;
        }

    }

    /**
     *
     * Note: currently this class looks a bit funny (only overriding
     * the hit decision method). That's because the "experimental" code
     * as of the last round moved to stable. But I expect that there's more
     * to come, so I leave it here.
     *
     * <ol>
     * <li> hit handle detection in processMouse
     * </ol>
     */
    public class TreeTableHackerExt extends TreeTableHacker {

        /**
         * Here: returns true.
         * @inheritDoc
         */
        @Override
        protected boolean isHitDetectionFromProcessMouse() {
            return true;
        }

    }

    /**
     * Patch for #471-swingx: no selection on click in hierarchical column
     * if outside of node-text. Mar 2007.
     * <p>
     *
     * Note: with 1.6 the expansion control was broken even with the "normal extended"
     * TreeTableHackerExt. When fixing that (renderer must have correct width for
     * BasicTreeUI since 1.6) took a look into why this didn't work and made it work.
     * So, now this is bidi-compliant.
     *
     * @author tiberiu@dev.java.net
     */
    public class TreeTableHackerExt2 extends TreeTableHackerExt {
        @Override
        protected boolean expandOrCollapseNode(int column, EventObject e) {
            if (!isHierarchical(column))
                return false;
            if (!mightBeExpansionTrigger(e))
                return false;
            boolean changedExpansion = false;
            MouseEvent me = (MouseEvent) e;
            if (hackAroundDragEnabled(me)) {
                /*
                 * Hack around #168-jdnc: dirty little hack mentioned in the
                 * forum discussion about the issue: fake a mousePressed if drag
                 * enabled. The usability is slightly impaired because the
                 * expand/collapse is effectively triggered on released only
                 * (drag system intercepts and consumes all other).
                 */
                me = new MouseEvent((Component) me.getSource(),
                        MouseEvent.MOUSE_PRESSED, me.getWhen(), me
                        .getModifiers(), me.getX(), me.getY(), me
                        .getClickCount(), me.isPopupTrigger());
            }
            // If the modifiers are not 0 (or the left mouse button),
            // tree may try and toggle the selection, and table
            // will then try and toggle, resulting in the
            // selection remaining the same. To avoid this, we
            // only dispatch when the modifiers are 0 (or the left mouse
            // button).
            if (me.getModifiers() == 0
                    || me.getModifiers() == InputEvent.BUTTON1_MASK) {
                // compute where the mouse point is relative to the tree
                // as renderer, that the x coordinate translated to be relative
                // to the column x-position
                Point treeMousePoint = getTreeMousePoint(column, me);
                int treeRow = renderer.getRowForLocation(treeMousePoint.x,
                        treeMousePoint.y);
                int row = 0;
                // mouse location not inside the node content
                if (treeRow < 0) {
                    // get the row for mouse location
                    row = renderer.getClosestRowForLocation(treeMousePoint.x,
                            treeMousePoint.y);
                    // check against actual bounds of the row
                    Rectangle bounds = renderer.getRowBounds(row);
                    if (bounds == null) {
                        row = -1;
                    } else {
                        // check if the mouse location is "leading"
                        // relative to the content box
                        // JW: fix issue 1168-swingx: expansion control broken in
                        if (getComponentOrientation().isLeftToRight()) {
                            // this is LToR only
                            if ((bounds.y + bounds.height < treeMousePoint.y)
                                    || bounds.x > treeMousePoint.x) {
                                row = -1;
                            }
                        } else {
                            if ((bounds.y + bounds.height < treeMousePoint.y)
                                    || bounds.x + bounds.width < treeMousePoint.x) {
                                row = -1;
                            }

                        }
                    }
                    // make sure the expansionChangedFlag is set to false for
                    // the case that up in the tree nothing happens
                    expansionChangedFlag = false;
                }

                if ((treeRow >= 0) // if in content box
                        || ((treeRow < 0) && (row < 0))) {// or outside but leading
                    if (treeRow >= 0)  { //Issue 561-swingx: in content box, update column lead to focus
                        getColumnModel().getSelectionModel().setLeadSelectionIndex(column);
                    }
                    // dispatch the translated event to the tree
                    // which either triggers a tree selection
                    // or expands/collapses a node
                    MouseEvent pressed = new MouseEvent(renderer, me.getID(),
                            me.getWhen(), me.getModifiers(), treeMousePoint.x,
                            treeMousePoint.y, me.getClickCount(), me
                            .isPopupTrigger());
                    renderer.dispatchEvent(pressed);
                    // For Mac OS X, we need to dispatch a MOUSE_RELEASED as
                    // well
                    MouseEvent released = new MouseEvent(renderer,
                            java.awt.event.MouseEvent.MOUSE_RELEASED, pressed
                            .getWhen(), pressed.getModifiers(), pressed
                            .getX(), pressed.getY(), pressed
                            .getClickCount(), pressed.isPopupTrigger());
                    renderer.dispatchEvent(released);
                    // part of 561-swingx: if focus elsewhere and dispatching the
                    // mouseEvent the focus doesn't move from elsewhere
                    // still doesn't help in very first click after startup
                    // probably lead of row selection event not correctly updated
                    // on synch from treeSelectionModel
                    requestFocusInWindow();
                }
                if (expansionChangedFlag) {
                    changedExpansion = true;
                } else {
                }
            }
            expansionChangedFlag = false;
            return changedExpansion;
        }

        /**
         * This is a patch provided for Issue #980-swingx which should
         * improve the bidi-compliance. Still doesn't work in our
         * visual tests...<p>
         *
         * Problem was not in the translation to renderer coordinate system,
         * it was in the method itself: the check whether we are "beyond" the
         * cell content box is bidi-dependent. Plus (since 1.6), width of
         * renderer must be > 0.
         *
         *
         * @param column the column index under the event, if any.
         * @param e the event which might trigger a expand/collapse.
         * @return the Point adjusted for bidi
         */
        protected Point getTreeMousePoint(int column, MouseEvent me) {
            // could inline as it wasn't the place to fix for broken RToL
            return new Point(me.getX()
                    - getCellRect(0, column, false).x, me.getY());
        }
    }
    /**
     * A more (or less, depending in pov :-) aggressiv hacker. Compared
     * to super, it dispatches less events to address open issues.<p>
     *
     * Issue #474-swingx: double click should start edit (not expand/collapse)
     *    changed mightBeExpansionTrigger to filter out clickCounts > 1
     * <p>
     * Issue #875-swingx: cell selection mode
     *    changed the dispatch to do so only if mouse event outside content
     *    box and leading
     * <p>
     * Issue #1169-swingx: remove 1.5 dnd hack
     *    removed the additional dispatch here and
     *    changed in the implementation of hackAroundDragEnabled
     *    to no longer look for the system property (it's useless even if set)
     *
     * @author tiberiu@dev.java.net
     */
    public class TreeTableHackerExt3 extends TreeTableHackerExt2 {
        @Override
        protected boolean expandOrCollapseNode(int column, EventObject e) {
            if (!isHierarchical(column))
                return false;
            if (!mightBeExpansionTrigger(e))
                return false;
            boolean changedExpansion = false;
            MouseEvent me = (MouseEvent) e;
            // If the modifiers are not 0 (or the left mouse button),
            // tree may try and toggle the selection, and table
            // will then try and toggle, resulting in the
            // selection remaining the same. To avoid this, we
            // only dispatch when the modifiers are 0 (or the left mouse
            // button).
            if (me.getModifiers() == 0
                    || me.getModifiers() == InputEvent.BUTTON1_MASK) {
                // compute where the mouse point is relative to the tree
                // as renderer, that the x coordinate translated to be relative
                // to the column x-position
                Point treeMousePoint = getTreeMousePoint(column, me);
                int treeRow = renderer.getRowForLocation(treeMousePoint.x,
                        treeMousePoint.y);
                int row = 0;
                // mouse location not inside the node content
                if (treeRow < 0) {
                    // get the row for mouse location
                    row = renderer.getClosestRowForLocation(treeMousePoint.x,
                            treeMousePoint.y);
                    // check against actual bounds of the row
                    Rectangle bounds = renderer.getRowBounds(row);
                    if (bounds == null) {
                        row = -1;
                    } else {
                        // check if the mouse location is "leading"
                        // relative to the content box
                        // JW: fix issue 1168-swingx: expansion control broken in
                        if (getComponentOrientation().isLeftToRight()) {
                            // this is LToR only
                            if ((bounds.y + bounds.height < treeMousePoint.y)
                                    || bounds.x > treeMousePoint.x) {
                                row = -1;
                            }
                        } else {
                            if ((bounds.y + bounds.height < treeMousePoint.y)
                                    || bounds.x + bounds.width < treeMousePoint.x) {
                                row = -1;
                            }

                        }
                    }
                }
                // make sure the expansionChangedFlag is set to false for
                // the case that up in the tree nothing happens
                expansionChangedFlag = false;

                if  ((treeRow < 0) && (row < 0)) {// outside and leading
                    // dispatch the translated event to the tree
                    // which either triggers a tree selection
                    // or expands/collapses a node
                    MouseEvent pressed = new MouseEvent(renderer, me.getID(),
                            me.getWhen(), me.getModifiers(), treeMousePoint.x,
                            treeMousePoint.y, me.getClickCount(), me
                                    .isPopupTrigger());
                    renderer.dispatchEvent(pressed);
                    // For Mac OS X, we need to dispatch a MOUSE_RELEASED as
                    // well
                    MouseEvent released = new MouseEvent(renderer,
                            java.awt.event.MouseEvent.MOUSE_RELEASED, pressed
                                    .getWhen(), pressed.getModifiers(), pressed
                                    .getX(), pressed.getY(), pressed
                                    .getClickCount(), pressed.isPopupTrigger());
                    renderer.dispatchEvent(released);
                    // part of 561-swingx: if focus elsewhere and dispatching the
                    // mouseEvent the focus doesn't move from elsewhere
                    // still doesn't help in very first click after startup
                    // probably lead of row selection event not correctly updated
                    // on synch from treeSelectionModel
                    requestFocusInWindow();
                }
                if (expansionChangedFlag) {
                    changedExpansion = true;
                } else {
                }
            }
            expansionChangedFlag = false;
            return changedExpansion;
        }
        /**
         * Overridden to exclude clickcounts > 1.
         */
        @Override
        protected boolean mightBeExpansionTrigger(EventObject e) {
            if (!(e instanceof MouseEvent)) return false;
            MouseEvent me = (MouseEvent) e;
            if (!SwingUtilities.isLeftMouseButton(me)) return false;
            if (me.getClickCount() > 1) return false;
            return me.getID() == MouseEvent.MOUSE_PRESSED;
        }

    }

    /**
     * Decides whether we want to apply the hack for #168-jdnc. here: returns
     * true if dragEnabled() and a client property with key DRAG_HACK_FLAG_KEY
     * has a value of boolean true.<p>
     *
     * Note: this is updated for 1.6, as the intermediate system property
     * for enabled drag support is useless now (it's the default)
     *
     * @param me the mouseEvent that triggered a editCellAt
     * @return true if the hack should be applied.
     */
    protected boolean hackAroundDragEnabled(MouseEvent me) {
        Boolean dragHackFlag = (Boolean) getClientProperty(DRAG_HACK_FLAG_KEY);
        return getDragEnabled() && Boolean.TRUE.equals(dragHackFlag);
    }

    /**
     * Overridden to provide a workaround for BasicTableUI anomaly. Make sure
     * the UI never tries to resize the editor. The UI currently uses different
     * techniques to paint the renderers and editors. So, overriding setBounds()
     * is not the right thing to do for an editor. Returning -1 for the
     * editing row in this case, ensures the editor is never painted.
     *
     * {@inheritDoc}
     */
    @Override
    public int getEditingRow() {
        if (editingRow == -1) return -1;
        return isHierarchical(editingColumn) ? -1 : editingRow;
    }

    /**
     * Returns the actual row that is editing as <code>getEditingRow</code>
     * will always return -1.
     */
    private int realEditingRow() {
        return editingRow;
    }

    /**
     * Sets the data model for this JXTreeTable to the specified
     * {@link org.jdesktop.swingx.treetable.TreeTableModel}. The same data model
     * may be shared by any number of JXTreeTable instances.
     *
     * @param treeModel data model for this JXTreeTable
     */
    public void setTreeTableModel(TreeTableModel treeModel) {
        TreeTableModel old = getTreeTableModel();
//        boolean rootVisible = isRootVisible();
//        setRootVisible(false);
        renderer.setModel(treeModel);
//        setRootVisible(rootVisible);

        firePropertyChange("treeTableModel", old, getTreeTableModel());
    }

    /**
     * Returns the underlying TreeTableModel for this JXTreeTable.
     *
     * @return the underlying TreeTableModel for this JXTreeTable
     */
    public TreeTableModel getTreeTableModel() {
        return (TreeTableModel) renderer.getModel();
    }

    /**
     * <p>Overrides superclass version to make sure that the specified
     * {@link javax.swing.table.TableModel} is compatible with JXTreeTable before
     * invoking the inherited version.</p>
     *
     * <p>Because JXTreeTable internally adapts an
     * {@link org.jdesktop.swingx.treetable.TreeTableModel} to make it a compatible
     * TableModel, <b>this method should never be called directly</b>. Use
     * {@link #setTreeTableModel(org.jdesktop.swingx.treetable.TreeTableModel) setTreeTableModel} instead.</p>
     *
     * <p>While it is possible to obtain a reference to this adapted
     * version of the TableModel by calling {@link javax.swing.JTable#getModel()},
     * any attempt to call setModel() with that adapter will fail because
     * the adapter might have been bound to a different JXTreeTable instance. If
     * you want to extract the underlying TreeTableModel, which, by the way,
     * <em>can</em> be shared, use {@link #getTreeTableModel() getTreeTableModel}
     * instead</p>.
     *
     * @param tableModel must be a TreeTableModelAdapter
     * @throws IllegalArgumentException if the specified tableModel is not an
     * instance of TreeTableModelAdapter
     */
    @Override
    public final void setModel(TableModel tableModel) { // note final keyword
        if (tableModel instanceof TreeTableModelAdapter) {
            if (((TreeTableModelAdapter) tableModel).getTreeTable() == null) {
                // Passing the above test ensures that this method is being
                // invoked either from JXTreeTable/JTable constructor or from
                // setTreeTableModel(TreeTableModel)
                super.setModel(tableModel); // invoke superclass version

                ((TreeTableModelAdapter) tableModel).bind(this); // permanently bound
                // Once a TreeTableModelAdapter is bound to any JXTreeTable instance,
                // invoking JXTreeTable.setModel() with that adapter will throw an
                // IllegalArgumentException, because we really want to make sure
                // that a TreeTableModelAdapter is NOT shared by another JXTreeTable.
            }
            else {
                throw new IllegalArgumentException("model already bound");
            }
        }
        else {
            throw new IllegalArgumentException("unsupported model type");
        }
    }


    @Override
    public void tableChanged(TableModelEvent e) {
        if (isStructureChanged(e) || isUpdate(e)) {
            super.tableChanged(e);
        } else {
            resizeAndRepaint();
        }
    }

    /**
     * Throws UnsupportedOperationException because variable height rows are
     * not supported.
     *
     * @param row ignored
     * @param rowHeight ignored
     * @throws UnsupportedOperationException because variable height rows are
     * not supported
     */
    @Override
    public final void setRowHeight(int row, int rowHeight) {
        throw new UnsupportedOperationException("variable height rows not supported");
    }

    /**
     * Sets the row height for this JXTreeTable and forwards the
     * row height to the renderering tree.
     *
     * @param rowHeight height of a row.
     */
    @Override
    public void setRowHeight(int rowHeight) {
        super.setRowHeight(rowHeight);
        adjustTreeRowHeight(getRowHeight());
    }

    /**
     * Forwards tableRowHeight to tree.
     *
     * @param tableRowHeight height of a row.
     */
    protected void adjustTreeRowHeight(int tableRowHeight) {
        if (renderer != null && renderer.getRowHeight() != tableRowHeight) {
            renderer.setRowHeight(tableRowHeight);
        }
    }

    /**
     * Forwards treeRowHeight to table. This is for completeness only: the
     * rendering tree is under our total control, so we don't expect
     * any external call to tree.setRowHeight.
     *
     * @param treeRowHeight height of a row.
     */
    protected void adjustTableRowHeight(int treeRowHeight) {
        if (getRowHeight() != treeRowHeight) {
            adminSetRowHeight(treeRowHeight);
        }
    }

    /**
     * {@inheritDoc} <p>
     *
     * Overridden to adjust the renderer's size.
     */
    @Override
    public void columnMarginChanged(ChangeEvent e) {
        super.columnMarginChanged(e);
        adjustTreeBounds();
    }

    /**
     * Forces the renderer to resize for fitting into hierarchical column.
     */
    private void adjustTreeBounds() {
        if (renderer != null) {
            renderer.setBounds(0, 0, 0, 0);
        }
    }

    /**
     * <p>Overridden to ensure that private renderer state is kept in sync with the
     * state of the component. Calls the inherited version after performing the
     * necessary synchronization. If you override this method, make sure you call
     * this version from your version of this method.</p>
     *
     * <p>This version maps the selection mode used by the renderer to match the
     * selection mode specified for the table. Specifically, the modes are mapped
     * as follows:
     * <pre>
     *  ListSelectionModel.SINGLE_INTERVAL_SELECTION: TreeSelectionModel.CONTIGUOUS_TREE_SELECTION;
     *  ListSelectionModel.MULTIPLE_INTERVAL_SELECTION: TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION;
     *  any other (default): TreeSelectionModel.SINGLE_TREE_SELECTION;
     * </pre>
     *
     * {@inheritDoc}
     *
     * @param mode any of the table selection modes
     */
    @Override
    public void setSelectionMode(int mode) {
        if (renderer != null) {
            switch (mode) {
                case ListSelectionModel.SINGLE_INTERVAL_SELECTION: {
                    renderer.getSelectionModel().setSelectionMode(
                        TreeSelectionModel.CONTIGUOUS_TREE_SELECTION);
                    break;
                }
                case ListSelectionModel.MULTIPLE_INTERVAL_SELECTION: {
                    renderer.getSelectionModel().setSelectionMode(
                        TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
                    break;
                }
                default: {
                    renderer.getSelectionModel().setSelectionMode(
                        TreeSelectionModel.SINGLE_TREE_SELECTION);
                    break;
                }
            }
        }
        super.setSelectionMode(mode);
    }

    /**
     * {@inheritDoc} <p>
     *
     * Overridden to decorate the tree's renderer after calling super.
     * At that point, it is only the tree itself that has been decorated.
     *
     * @param renderer the <code>TableCellRenderer</code> to prepare
     * @param row the row of the cell to render, where 0 is the first row
     * @param column the column of the cell to render, where 0 is the first column
     * @return the <code>Component</code> used as a stamp to render the specified cell
     *
     * @see #applyRenderer(Component, ComponentAdapter)
     */
    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int row,
        int column) {
        Component component = super.prepareRenderer(renderer, row, column);
        return applyRenderer(component, getComponentAdapter(row, column));
    }

    /**
     * Performs configuration of the tree's renderer if the adapter's column is
     * the hierarchical column, does nothing otherwise.
     * <p>
     *
     * Note: this is legacy glue if the treeCellRenderer is of type
     * DefaultTreeCellRenderer. In that case the renderer's
     * background/foreground/Non/Selection colors are set to the tree's
     * background/foreground depending on the adapter's selection state. Does
     * nothing if the treeCellRenderer is backed by a ComponentProvider.
     *
     * @param component the rendering component
     * @param adapter component data adapter
     * @throws NullPointerException if the specified component or adapter is
     *         null
     */
    protected Component applyRenderer(Component component,
            ComponentAdapter adapter) {
        if (component == null) {
            throw new IllegalArgumentException("null component");
        }
        if (adapter == null) {
            throw new IllegalArgumentException("null component data adapter");
        }

        if (isHierarchical(adapter.column)) {
            // After all decorators have been applied, make sure that relevant
            // attributes of the table cell renderer are applied to the
            // tree cell renderer before the hierarchical column is rendered!
            TreeCellRenderer tcr = renderer.getCellRenderer();
            if (tcr instanceof JXTree.DelegatingRenderer) {
                tcr = ((JXTree.DelegatingRenderer) tcr).getDelegateRenderer();

            }
            if (tcr instanceof DefaultTreeCellRenderer) {

                DefaultTreeCellRenderer dtcr = ((DefaultTreeCellRenderer) tcr);
                // this effectively overwrites the dtcr settings
                if (adapter.isSelected()) {
                    dtcr.setTextSelectionColor(component.getForeground());
                    dtcr.setBackgroundSelectionColor(component.getBackground());
                } else {
                    dtcr.setTextNonSelectionColor(component.getForeground());
                    dtcr.setBackgroundNonSelectionColor(component
                            .getBackground());
                }
            }
        }
        return component;
    }

    /**
     * Sets the specified TreeCellRenderer as the Tree cell renderer.
     *
     * @param cellRenderer to use for rendering tree cells.
     */
    public void setTreeCellRenderer(TreeCellRenderer cellRenderer) {
        if (renderer != null) {
            renderer.setCellRenderer(cellRenderer);
        }
    }

    public TreeCellRenderer getTreeCellRenderer() {
        return renderer.getCellRenderer();
    }

    /**
     * {@inheritDoc} <p>
     *
     * Overridden to special-case the hierarchical column.
     */
    @Override
    public String getToolTipText(MouseEvent event) {
        int column = columnAtPoint(event.getPoint());
        if (column >= 0 && isHierarchical(column)) {
            int row = rowAtPoint(event.getPoint());
            return renderer.getToolTipText(event, row, column);
        }
        return super.getToolTipText(event);
    }

    /**
     * {@inheritDoc} <p>
     *
     * Overridden to set the fixed tooltip text to the tree that is rendering the
     * hierarchical column.
     */
    @Override
    public void setToolTipText(String text) {
        super.setToolTipText(text);
        renderer.setToolTipText(text);
    }

    /**
     * Sets the specified icon as the icon to use for rendering collapsed nodes.
     *
     * @param icon to use for rendering collapsed nodes
     *
     * @see JXTree#setCollapsedIcon(Icon)
     */
    public void setCollapsedIcon(Icon icon) {
        renderer.setCollapsedIcon(icon);
    }

    /**
     * Sets the specified icon as the icon to use for rendering expanded nodes.
     *
     * @param icon to use for rendering expanded nodes
     *
     * @see JXTree#setExpandedIcon(Icon)
     */
    public void setExpandedIcon(Icon icon) {
        renderer.setExpandedIcon(icon);
    }

    /**
     * Sets the specified icon as the icon to use for rendering open container nodes.
     *
     * @param icon to use for rendering open nodes
     *
     * @see JXTree#setOpenIcon(Icon)
     */
    public void setOpenIcon(Icon icon) {
        renderer.setOpenIcon(icon);
    }

    /**
     * Sets the specified icon as the icon to use for rendering closed container nodes.
     *
     * @param icon to use for rendering closed nodes
     *
     * @see JXTree#setClosedIcon(Icon)
     */
    public void setClosedIcon(Icon icon) {
        renderer.setClosedIcon(icon);
    }

    /**
     * Sets the specified icon as the icon to use for rendering leaf nodes.
     *
     * @param icon to use for rendering leaf nodes
     *
     * @see JXTree#setLeafIcon(Icon)
     */
    public void setLeafIcon(Icon icon) {
        renderer.setLeafIcon(icon);
    }

    /**
     * Property to control whether per-tree icons should be
     * copied to the renderer on setTreeCellRenderer. <p>
     *
     * The default value is false.
     *
     * @param overwrite a boolean to indicate if the per-tree Icons should
     *   be copied to the new renderer on setTreeCellRenderer.
     *
     * @see #isOverwriteRendererIcons()
     * @see #setLeafIcon(Icon)
     * @see #setOpenIcon(Icon)
     * @see #setClosedIcon(Icon)
     * @see JXTree#setOverwriteRendererIcons(boolean)
     */
    public void setOverwriteRendererIcons(boolean overwrite) {
        renderer.setOverwriteRendererIcons(overwrite);
    }

    /**
     * Returns a boolean indicating whether the per-tree icons should be
     * copied to the renderer on setTreeCellRenderer.
     *
     * @return true if a TreeCellRenderer's icons will be overwritten with the
     *   tree's Icons, false if the renderer's icons will be unchanged.
     *
     * @see #setOverwriteRendererIcons(boolean)
     * @see #setLeafIcon(Icon)
     * @see #setOpenIcon(Icon)
     * @see #setClosedIcon(Icon)
     * @see JXTree#isOverwriteRendererIcons()
     *
     */
    public boolean isOverwriteRendererIcons() {
        return renderer.isOverwriteRendererIcons();
    }

    /**
     * Overridden to ensure that private renderer state is kept in sync with the
     * state of the component. Calls the inherited version after performing the
     * necessary synchronization. If you override this method, make sure you call
     * this version from your version of this method.
     */
    @Override
    public void clearSelection() {
        if (renderer != null) {
            renderer.clearSelection();
        }
        super.clearSelection();
    }

    /**
     * Collapses all nodes in the treetable.
     */
    public void collapseAll() {
        renderer.collapseAll();
    }

    /**
     * Expands all nodes in the treetable.
     */
    public void expandAll() {
        renderer.expandAll();
    }

    /**
     * Collapses the node at the specified path in the treetable.
     *
     * @param path path of the node to collapse
     */
    public void collapsePath(TreePath path) {
        renderer.collapsePath(path);
    }

    /**
     * Expands the the node at the specified path in the treetable.
     *
     * @param path path of the node to expand
     */
    public void expandPath(TreePath path) {
        renderer.expandPath(path);
    }

    /**
     * Makes sure all the path components in path are expanded (except
     * for the last path component) and scrolls so that the
     * node identified by the path is displayed. Only works when this
     * <code>JTree</code> is contained in a <code>JScrollPane</code>.
     *
     * (doc copied from JTree)
     *
     * PENDING: JW - where exactly do we want to scroll? Here: the scroll
     * is in vertical direction only. Might need to show the tree column?
     *
     * @param path  the <code>TreePath</code> identifying the node to
     *          bring into view
     */
    public void scrollPathToVisible(TreePath path) {
        renderer.scrollPathToVisible(path);
//        if (path == null) return;
//        renderer.makeVisible(path);
//        int row = getRowForPath(path);
//        scrollRowToVisible(row);
    }

    /**
     * Collapses the row in the treetable. If the specified row index is
     * not valid, this method will have no effect.
     */
    public void collapseRow(int row) {
        renderer.collapseRow(row);
    }

    /**
     * Expands the specified row in the treetable. If the specified row index is
     * not valid, this method will have no effect.
     */
    public void expandRow(int row) {
        renderer.expandRow(row);
    }

    /**
     * Returns true if the value identified by path is currently viewable, which
     * means it is either the root or all of its parents are expanded. Otherwise,
     * this method returns false.
     *
     * @return true, if the value identified by path is currently viewable;
     * false, otherwise
     */
    public boolean isVisible(TreePath path) {
        return renderer.isVisible(path);
    }

    /**
     * Returns true if the node identified by path is currently expanded.
     * Otherwise, this method returns false.
     *
     * @param path path
     * @return true, if the value identified by path is currently expanded;
     * false, otherwise
     */
    public boolean isExpanded(TreePath path) {
        return renderer.isExpanded(path);
    }

    /**
     * Returns true if the node at the specified display row is currently expanded.
     * Otherwise, this method returns false.
     *
     * @param row row
     * @return true, if the node at the specified display row is currently expanded.
     * false, otherwise
     */
    public boolean isExpanded(int row) {
        return renderer.isExpanded(row);
    }

    /**
     * Returns true if the node identified by path is currently collapsed,
     * this will return false if any of the values in path are currently not
     * being displayed.
     *
     * @param path path
     * @return true, if the value identified by path is currently collapsed;
     * false, otherwise
     */
    public boolean isCollapsed(TreePath path) {
        return renderer.isCollapsed(path);
    }

    /**
     * Returns true if the node at the specified display row is collapsed.
     *
     * @param row row
     * @return true, if the node at the specified display row is currently collapsed.
     * false, otherwise
     */
    public boolean isCollapsed(int row) {
        return renderer.isCollapsed(row);
    }

    /**
     * Returns an <code>Enumeration</code> of the descendants of the
     * path <code>parent</code> that
     * are currently expanded. If <code>parent</code> is not currently
     * expanded, this will return <code>null</code>.
     * If you expand/collapse nodes while
     * iterating over the returned <code>Enumeration</code>
     * this may not return all
     * the expanded paths, or may return paths that are no longer expanded.
     *
     * @param parent  the path which is to be examined
     * @return an <code>Enumeration</code> of the descendents of
     *        <code>parent</code>, or <code>null</code> if
     *        <code>parent</code> is not currently expanded
     */

    public Enumeration<?> getExpandedDescendants(TreePath parent) {
        return renderer.getExpandedDescendants(parent);
    }

    /**
     * Returns the TreePath for a given x,y location.
     *
     * @param x x value
     * @param y y value
     *
     * @return the <code>TreePath</code> for the givern location.
     */
     public TreePath getPathForLocation(int x, int y) {
        int row = rowAtPoint(new Point(x,y));
        if (row == -1) {
          return null;
        }
        return renderer.getPathForRow(row);
     }

    /**
     * Returns the TreePath for a given row.
     *
     * @param row
     *
     * @return the <code>TreePath</code> for the given row.
     */
     public TreePath getPathForRow(int row) {
        return renderer.getPathForRow(row);
     }

     /**
      * Returns the row for a given TreePath.
      *
      * @param path
      * @return the row for the given <code>TreePath</code>.
      */
     public int getRowForPath(TreePath path) {
       return renderer.getRowForPath(path);
     }

//------------------------------ exposed Tree properties

     /**
      * Determines whether or not the root node from the TreeModel is visible.
      *
      * @param visible true, if the root node is visible; false, otherwise
      */
     public void setRootVisible(boolean visible) {
         renderer.setRootVisible(visible);
         // JW: the revalidate forces the root to appear after a
         // toggling a visible from an initially invisible root.
         // JTree fires a propertyChange on the ROOT_VISIBLE_PROPERTY
         // BasicTreeUI reacts by (ultimately) calling JTree.treeDidChange
         // which revalidate the tree part.
         // Might consider to listen for the propertyChange (fired only if there
         // actually was a change) instead of revalidating unconditionally.
         revalidate();
         repaint();
     }

     /**
      * Returns true if the root node of the tree is displayed.
      *
      * @return true if the root node of the tree is displayed
      */
     public boolean isRootVisible() {
         return renderer.isRootVisible();
     }

    /**
     * Sets the value of the <code>scrollsOnExpand</code> property for the tree
     * part. This property specifies whether the expanded paths should be scrolled
     * into view. In a look and feel in which a tree might not need to scroll
     * when expanded, this property may be ignored.
     *
     * @param scroll true, if expanded paths should be scrolled into view;
     * false, otherwise
     */
    public void setScrollsOnExpand(boolean scroll) {
        renderer.setScrollsOnExpand(scroll);
    }

    /**
     * Returns the value of the <code>scrollsOnExpand</code> property.
     *
     * @return the value of the <code>scrollsOnExpand</code> property
     */
    public boolean getScrollsOnExpand() {
        return renderer.getScrollsOnExpand();
    }

    /**
     * Sets the value of the <code>showsRootHandles</code> property for the tree
     * part. This property specifies whether the node handles should be displayed.
     * If handles are not supported by a particular look and feel, this property
     * may be ignored.
     *
     * @param visible true, if root handles should be shown; false, otherwise
     */
    public void setShowsRootHandles(boolean visible) {
        renderer.setShowsRootHandles(visible);
        repaint();
    }

    /**
     * Returns the value of the <code>showsRootHandles</code> property.
     *
     * @return the value of the <code>showsRootHandles</code> property
     */
    public boolean getShowsRootHandles() {
        return renderer.getShowsRootHandles();
    }

    /**
     * Sets the value of the <code>expandsSelectedPaths</code> property for the tree
     * part. This property specifies whether the selected paths should be expanded.
     *
     * @param expand true, if selected paths should be expanded; false, otherwise
     */
    public void setExpandsSelectedPaths(boolean expand) {
        renderer.setExpandsSelectedPaths(expand);
    }

    /**
     * Returns the value of the <code>expandsSelectedPaths</code> property.
     *
     * @return the value of the <code>expandsSelectedPaths</code> property
     */
    public boolean getExpandsSelectedPaths() {
        return renderer.getExpandsSelectedPaths();
    }

    /**
     * Returns the number of mouse clicks needed to expand or close a node.
     *
     * @return number of mouse clicks before node is expanded
     */
    public int getToggleClickCount() {
        return renderer.getToggleClickCount();
    }

    /**
     * Sets the number of mouse clicks before a node will expand or close.
     * The default is two.
     *
     * @param clickCount the number of clicks required to expand/collapse a node.
     */
    public void setToggleClickCount(int clickCount) {
        renderer.setToggleClickCount(clickCount);
    }

    /**
     * Returns true if the tree is configured for a large model.
     * The default value is false.
     *
     * @return true if a large model is suggested
     * @see #setLargeModel
     */
    public boolean isLargeModel() {
        return renderer.isLargeModel();
    }

    /**
     * Specifies whether the UI should use a large model.
     * (Not all UIs will implement this.) <p>
     *
     * <strong>NOTE</strong>: this method is exposed for completeness -
     * currently it's not recommended
     * to use a large model because there are some issues
     * (not yet fully understood), namely
     * issue #25-swingx, and probably #270-swingx.
     *
     * @param newValue true to suggest a large model to the UI
     */
    public void setLargeModel(boolean newValue) {
        renderer.setLargeModel(newValue);
        // JW: random method calling ... doesn't help
//        renderer.treeDidChange();
//        revalidate();
//        repaint();
    }

//------------------------------ exposed tree listeners

    /**
     * Adds a listener for <code>TreeExpansion</code> events.
     *
     * @param tel a TreeExpansionListener that will be notified
     * when a tree node is expanded or collapsed
     */
    public void addTreeExpansionListener(TreeExpansionListener tel) {
        getTreeExpansionBroadcaster().addTreeExpansionListener(tel);
    }

    /**
     * @return
     */
    private TreeExpansionBroadcaster getTreeExpansionBroadcaster() {
        if (treeExpansionBroadcaster == null) {
            treeExpansionBroadcaster = new TreeExpansionBroadcaster(this);
            renderer.addTreeExpansionListener(treeExpansionBroadcaster);
        }
        return treeExpansionBroadcaster;
    }

    /**
     * Removes a listener for <code>TreeExpansion</code> events.
     * @param tel the <code>TreeExpansionListener</code> to remove
     */
    public void removeTreeExpansionListener(TreeExpansionListener tel) {
        if (treeExpansionBroadcaster == null) return;
        treeExpansionBroadcaster.removeTreeExpansionListener(tel);
    }

    /**
     * Adds a listener for <code>TreeSelection</code> events.
     * TODO (JW): redirect event source to this.
     *
     * @param tsl a TreeSelectionListener that will be notified
     * when a tree node is selected or deselected
     */
    public void addTreeSelectionListener(TreeSelectionListener tsl) {
        renderer.addTreeSelectionListener(tsl);
    }

    /**
     * Removes a listener for <code>TreeSelection</code> events.
     * @param tsl the <code>TreeSelectionListener</code> to remove
     */
    public void removeTreeSelectionListener(TreeSelectionListener tsl) {
        renderer.removeTreeSelectionListener(tsl);
    }

    /**
     * Adds a listener for <code>TreeWillExpand</code> events.
     * TODO (JW): redirect event source to this.
     *
     * @param tel a TreeWillExpandListener that will be notified
     * when a tree node will be expanded or collapsed
     */
    public void addTreeWillExpandListener(TreeWillExpandListener tel) {
        renderer.addTreeWillExpandListener(tel);
    }

    /**
     * Removes a listener for <code>TreeWillExpand</code> events.
     * @param tel the <code>TreeWillExpandListener</code> to remove
     */
    public void removeTreeWillExpandListener(TreeWillExpandListener tel) {
        renderer.removeTreeWillExpandListener(tel);
     }

    /**
     * Returns the selection model for the tree portion of the this treetable.
     *
     * @return selection model for the tree portion of the this treetable
     */
    public TreeSelectionModel getTreeSelectionModel() {
        return renderer.getSelectionModel();    // RG: Fix JDNC issue 41
    }

    /**
     * Overriden to invoke supers implementation, and then,
     * if the receiver is editing a Tree column, the editors bounds is
     * reset. The reason we have to do this is because JTable doesn't
     * think the table is being edited, as <code>getEditingRow</code> returns
     * -1, and therefore doesn't automaticly resize the editor for us.
     */
    @Override
    public void sizeColumnsToFit(int resizingColumn) {
        /** TODO: Review wrt doLayout() */
        super.sizeColumnsToFit(resizingColumn);
        // rg:changed
        if (getEditingColumn() != -1 && isHierarchical(editingColumn)) {
            Rectangle cellRect = getCellRect(realEditingRow(),
                getEditingColumn(), false);
            Component component = getEditorComponent();
            component.setBounds(cellRect);
            component.validate();
        }
    }

    /**
     * Determines if the specified column is defined as the hierarchical column.
     *
     * @param column
     *            zero-based index of the column in view coordinates
     * @return true if the column is the hierarchical column; false otherwise.
     * @throws IllegalArgumentException
     *             if the column is less than 0 or greater than or equal to the
     *             column count
     */
    public boolean isHierarchical(int column) {
        if (column < 0 || column >= getColumnCount()) {
            throw new IllegalArgumentException("column must be valid, was" + column);
        }

        return (getHierarchicalColumn() == column);
    }

    /**
     * Returns the index of the hierarchical column. This is the column that is
     * displayed as the tree.
     *
     * @return the index of the hierarchical column, -1 if there is
     *   no hierarchical column
     *
     */
    public int getHierarchicalColumn() {
        return convertColumnIndexToView(((TreeTableModel) renderer.getModel()).getHierarchicalColumn());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
        if (isHierarchical(column)) {
            return renderer;
        }

        return super.getCellRenderer(row, column);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableCellEditor getCellEditor(int row, int column) {
        if (isHierarchical(column)) {
            return hierarchicalEditor;
        }

        return super.getCellEditor(row, column);
    }

    @Override
    public void updateUI() {
        super.updateUI();
        updateHierarchicalRendererEditor();
    }

    /**
     * Updates Ui of renderer/editor for the hierarchical column. Need to do so
     * manually, as not accessible by the default lookup.
     */
    protected void updateHierarchicalRendererEditor() {
        if (renderer != null) {
           SwingUtilities.updateComponentTreeUI(renderer);
        }
    }

    /**
     * {@inheritDoc} <p>
     *
     * Overridden to message the tree directly if the column is the view index of
     * the hierarchical column. <p>
     *
     * PENDING JW: revisit once we switch to really using a table renderer. As is, it's
     * a quick fix for #821-swingx: string rep for hierarchical column incorrect.
     */
    @Override
    public String getStringAt(int row, int column) {
        if (isHierarchical(column)) {
            return getHierarchicalStringAt(row);
        }
        return super.getStringAt(row, column);
    }

    /**
     * Returns the String representation of the hierarchical column at the given
     * row. <p>
     *
     * @param row the row index in view coordinates
     * @return the string representation of the hierarchical column at the given row.
     *
     * @see #getStringAt(int, int)
     */
    private String getHierarchicalStringAt(int row) {
        return renderer.getStringAt(row);
    }

    /**
     * ListToTreeSelectionModelWrapper extends DefaultTreeSelectionModel
     * to listen for changes in the ListSelectionModel it maintains. Once
     * a change in the ListSelectionModel happens, the paths are updated
     * in the DefaultTreeSelectionModel.
     */
    class ListToTreeSelectionModelWrapper extends DefaultTreeSelectionModel {
        /** Set to true when we are updating the ListSelectionModel. */
        protected boolean updatingListSelectionModel;

        public ListToTreeSelectionModelWrapper() {
            super();
            getListSelectionModel().addListSelectionListener
                (createListSelectionListener());
        }

        /**
         * Returns the list selection model. ListToTreeSelectionModelWrapper
         * listens for changes to this model and updates the selected paths
         * accordingly.
         */
        ListSelectionModel getListSelectionModel() {
            return listSelectionModel;
        }

        /**
         * This is overridden to set <code>updatingListSelectionModel</code>
         * and message super. This is the only place DefaultTreeSelectionModel
         * alters the ListSelectionModel.
         */
        @Override
        public void resetRowSelection() {
            if (!updatingListSelectionModel) {
                updatingListSelectionModel = true;
                try {
                    super.resetRowSelection();
                }
                finally {
                    updatingListSelectionModel = false;
                }
            }
            // Notice how we don't message super if
            // updatingListSelectionModel is true. If
            // updatingListSelectionModel is true, it implies the
            // ListSelectionModel has already been updated and the
            // paths are the only thing that needs to be updated.
        }

        /**
         * Creates and returns an instance of ListSelectionHandler.
         */
        protected ListSelectionListener createListSelectionListener() {
            return new ListSelectionHandler();
        }

        /**
         * If <code>updatingListSelectionModel</code> is false, this will
         * reset the selected paths from the selected rows in the list
         * selection model.
         */
        protected void updateSelectedPathsFromSelectedRows() {
            if (!updatingListSelectionModel) {
                updatingListSelectionModel = true;
                try {
                    if (listSelectionModel.isSelectionEmpty()) {
                        clearSelection();
                    } else {
                        // This is way expensive, ListSelectionModel needs an
                        // enumerator for iterating.
                        int min = listSelectionModel.getMinSelectionIndex();
                        int max = listSelectionModel.getMaxSelectionIndex();

                        List<TreePath> paths = new ArrayList<TreePath>();
                        for (int counter = min; counter <= max; counter++) {
                            if (listSelectionModel.isSelectedIndex(counter)) {
                                TreePath selPath = renderer.getPathForRow(
                                    counter);

                                if (selPath != null) {
                                    paths.add(selPath);
                                }
                            }
                        }
                        setSelectionPaths(paths.toArray(new TreePath[paths.size()]));
                        // need to force here: usually the leadRow is adjusted
                        // in resetRowSelection which is disabled during this method
                        leadRow = leadIndex;
                    }
                }
                finally {
                    updatingListSelectionModel = false;
                }
            }
        }

        /**
         * Class responsible for calling updateSelectedPathsFromSelectedRows
         * when the selection of the list changse.
         */
        class ListSelectionHandler implements ListSelectionListener {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    updateSelectedPathsFromSelectedRows();
                }
            }
        }
    }

    /**
     *
     */
    protected static class TreeTableModelAdapter extends AbstractTableModel
        implements TreeTableModelProvider {
        private TreeModelListener treeModelListener;
        private final JTree tree; // immutable
        private JXTreeTable treeTable; // logically immutable

        /**
         * Maintains a TreeTableModel and a JTree as purely implementation details.
         * Developers can plug in any type of custom TreeTableModel through a
         * JXTreeTable constructor or through setTreeTableModel().
         *
         * @param tree TreeTableCellRenderer instantiated with the same model as
         * the driving JXTreeTable's TreeTableModel.
         * @throws IllegalArgumentException if a null tree argument is passed
         */
        TreeTableModelAdapter(JTree tree) {
            Contract.asNotNull(tree, "tree must not be null");

            this.tree = tree; // need tree to implement getRowCount()
            tree.getModel().addTreeModelListener(getTreeModelListener());
            tree.addTreeExpansionListener(new TreeExpansionListener() {
                // Don't use fireTableRowsInserted() here; the selection model
                // would get updated twice.
                @Override
                public void treeExpanded(TreeExpansionEvent event) {
                    updateAfterExpansionEvent(event);
                }

                @Override
                public void treeCollapsed(TreeExpansionEvent event) {
                    updateAfterExpansionEvent(event);
                }
            });
            tree.addPropertyChangeListener("model", new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    TreeTableModel model = (TreeTableModel) evt.getOldValue();
                    model.removeTreeModelListener(getTreeModelListener());

                    model = (TreeTableModel) evt.getNewValue();
                    model.addTreeModelListener(getTreeModelListener());

                    fireTableStructureChanged();
                }
            });
        }

        /**
         * updates the table after having received an TreeExpansionEvent.<p>
         *
         * @param event the TreeExpansionEvent which triggered the method call.
         */
        protected void updateAfterExpansionEvent(TreeExpansionEvent event) {
            // moved to let the renderer handle directly
//            treeTable.getTreeTableHacker().setExpansionChangedFlag();
            // JW: delayed fire leads to a certain sluggishness occasionally?
            fireTableDataChanged();
        }

        /**
         * Returns the JXTreeTable instance to which this TreeTableModelAdapter is
         * permanently and exclusively bound. For use by
         * {@link org.jdesktop.swingx.JXTreeTable#setModel(javax.swing.table.TableModel)}.
         *
         * @return JXTreeTable to which this TreeTableModelAdapter is permanently bound
         */
        protected JXTreeTable getTreeTable() {
            return treeTable;
        }

        /**
         * Immutably binds this TreeTableModelAdapter to the specified JXTreeTable.
         *
         * @param treeTable the JXTreeTable instance that this adapter is bound to.
         */
        protected final void bind(JXTreeTable treeTable) {
            // Suppress potentially subversive invocation!
            // Prevent clearing out the deck for possible hijack attempt later!
            if (treeTable == null) {
                throw new IllegalArgumentException("null treeTable");
            }

            if (this.treeTable == null) {
                this.treeTable = treeTable;
            }
            else {
                throw new IllegalArgumentException("adapter already bound");
            }
        }

        /**
         *
         * @inherited <p>
         *
         * Implemented to return the the underlying TreeTableModel.
         */
        @Override
        public TreeTableModel getTreeTableModel() {
            return (TreeTableModel) tree.getModel();
        }

        // Wrappers, implementing TableModel interface.
        // TableModelListener management provided by AbstractTableModel superclass.

        @Override
        public Class<?> getColumnClass(int column) {
            return getTreeTableModel().getColumnClass(column);
        }

        @Override
        public int getColumnCount() {
            return getTreeTableModel().getColumnCount();
        }

        @Override
        public String getColumnName(int column) {
            return getTreeTableModel().getColumnName(column);
        }

        @Override
        public int getRowCount() {
            return tree.getRowCount();
        }

        @Override
        public Object getValueAt(int row, int column) {
            // Issue #270-swingx: guard against invisible row
            Object node = nodeForRow(row);
            return node != null ? getTreeTableModel().getValueAt(node, column) : null;
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            // Issue #270-swingx: guard against invisible row
            Object node = nodeForRow(row);
            return node != null ? getTreeTableModel().isCellEditable(node, column) : false;
        }

        @Override
        public void setValueAt(Object value, int row, int column) {
            // Issue #270-swingx: guard against invisible row
            Object node = nodeForRow(row);
            if (node != null) {
                getTreeTableModel().setValueAt(value, node, column);
            }
        }

        protected Object nodeForRow(int row) {
            // Issue #270-swingx: guard against invisible row
            TreePath path = tree.getPathForRow(row);
            return path != null ? path.getLastPathComponent() : null;
        }

        /**
         * @return <code>TreeModelListener</code>
         */
        private TreeModelListener getTreeModelListener() {
            if (treeModelListener == null) {
                treeModelListener = new TreeModelListener() {

                    @Override
                    public void treeNodesChanged(TreeModelEvent e) {
//                        LOG.info("got tree event: changed " + e);
                        delayedFireTableDataUpdated(e);
                    }

                    // We use delayedFireTableDataChanged as we can
                    // not be guaranteed the tree will have finished processing
                    // the event before us.
                    @Override
                    public void treeNodesInserted(TreeModelEvent e) {
                        delayedFireTableDataChanged(e, 1);
                    }

                    @Override
                    public void treeNodesRemoved(TreeModelEvent e) {
//                        LOG.info("got tree event: removed " + e);
                       delayedFireTableDataChanged(e, 2);
                    }

                    @Override
                    public void treeStructureChanged(TreeModelEvent e) {
                        // ?? should be mapped to structureChanged -- JW
                        if (isTableStructureChanged(e)) {
                            delayedFireTableStructureChanged();
                        } else {
                            delayedFireTableDataChanged();
                        }
                    }
                };
            }

            return treeModelListener;
        }

        /**
         * Decides if the given treeModel structureChanged should
         * trigger a table structureChanged. Returns true if the
         * source path is the root or null, false otherwise.<p>
         *
         * PENDING: need to refine? "Marker" in Event-Object?
         *
         * @param e the TreeModelEvent received in the treeModelListener's
         *   treeStructureChanged
         * @return a boolean indicating whether the given TreeModelEvent
         *   should trigger a structureChanged.
         */
        private boolean isTableStructureChanged(TreeModelEvent e) {
            if ((e.getTreePath() == null) ||
                    (e.getTreePath().getParentPath() == null)) return true;
            return false;
        }

        /**
         * Invokes fireTableDataChanged after all the pending events have been
         * processed. SwingUtilities.invokeLater is used to handle this.
         */
        private void delayedFireTableStructureChanged() {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    fireTableStructureChanged();
                }
            });
        }

        /**
         * Invokes fireTableDataChanged after all the pending events have been
         * processed. SwingUtilities.invokeLater is used to handle this.
         */
        private void delayedFireTableDataChanged() {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    fireTableDataChanged();
                }
            });
        }

        /**
         * Invokes fireTableDataChanged after all the pending events have been
         * processed. SwingUtilities.invokeLater is used to handle this.
         * Allowed event types: 1 for insert, 2 for delete
         */
        private void delayedFireTableDataChanged(final TreeModelEvent tme, final int typeChange) {
            if ((typeChange < 1 ) || (typeChange > 2))
                throw new IllegalArgumentException("Event type must be 1 or 2, was " + typeChange);
            // expansion state before invoke may be different
            // from expansion state in invoke
            final boolean expanded = tree.isExpanded(tme.getTreePath());
            // quick test if tree throws for unrelated path. Seems like not.
//            tree.getRowForPath(new TreePath("dummy"));
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    int indices[] = tme.getChildIndices();
                    TreePath path = tme.getTreePath();
                    // quick test to see if bailing out is an option
//                    if (false) {
                    if (indices != null) {
                        if (expanded) { // Dont bother to update if the parent
                            // node is collapsed
                            // indices must in ascending order, as per TreeEvent/Listener doc
                            int min = indices[0];
                            int max = indices[indices.length - 1];
                            int startingRow = tree.getRowForPath(path) + 1;
                            min = startingRow + min;
                            max = startingRow + max;
                            switch (typeChange) {
                            case 1:
//                                LOG.info("rows inserted: path " + path + "/" + min + "/"
//                                        + max);
                                fireTableRowsInserted(min, max);
                                break;
                            case 2:
//                                LOG.info("rows deleted path " + path + "/" + min + "/"
//                                                + max);
                                fireTableRowsDeleted(min, max);
                                break;
                            }
                        } else {
                            // not expanded - but change might effect appearance
                            // of parent
                            // Issue #82-swingx
                            int row = tree.getRowForPath(path);
                            // fix Issue #247-swingx: prevent accidental
                            // structureChanged
                            // for collapsed path
                            // in this case row == -1, which ==
                            // TableEvent.HEADER_ROW
                            if (row >= 0)
                                fireTableRowsUpdated(row, row);
                        }
                    } else { // case where the event is fired to identify
                                // root.
                        fireTableDataChanged();
                    }
                }
            });
        }

        /**
         * This is used for updated only. PENDING: not necessary to delay?
         * Updates are never structural changes which are the critical.
         *
         * @param tme
         */
        protected void delayedFireTableDataUpdated(final TreeModelEvent tme) {
            final boolean expanded = tree.isExpanded(tme.getTreePath());
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    int indices[] = tme.getChildIndices();
                    TreePath path = tme.getTreePath();
                    if (indices != null) {
                        if (expanded) { // Dont bother to update if the parent
                            // node is collapsed
                            Object children[] = tme.getChildren();
                            // can we be sure that children.length > 0?
                            // int min = tree.getRowForPath(path.pathByAddingChild(children[0]));
                            // int max = tree.getRowForPath(path.pathByAddingChild(children[children.length -1]));
                            int min = Integer.MAX_VALUE;
                            int max = Integer.MIN_VALUE;
                            for (int i = 0; i < indices.length; i++) {
                                Object child = children[i];
                                TreePath childPath = path
                                        .pathByAddingChild(child);
                                int index = tree.getRowForPath(childPath);
                                if (index < min) {
                                    min = index;
                                }
                                if (index > max) {
                                    max = index;
                                }
                            }
//                            LOG.info("Updated: parentPath/min/max" + path + "/" + min + "/" + max);
                            // JW: the index is occasionally - 1 - need further digging
                            fireTableRowsUpdated(Math.max(0, min), Math.max(0, max));
                        } else {
                            // not expanded - but change might effect appearance
                            // of parent Issue #82-swingx
                            int row = tree.getRowForPath(path);
                            // fix Issue #247-swingx: prevent accidental structureChanged
                            // for collapsed path in this case row == -1,
                            // which == TableEvent.HEADER_ROW
                            if (row >= 0)
                                fireTableRowsUpdated(row, row);
                        }
                    } else { // case where the event is fired to identify
                                // root.
                        fireTableDataChanged();
                    }
                }
            });

        }

    }

    static class TreeTableCellRenderer extends JXTree implements
        TableCellRenderer
        // need to implement RolloverRenderer
        // PENDING JW: method name clash rolloverRenderer.isEnabled and
        // component.isEnabled .. don't extend, use? And change
        // the method name in rolloverRenderer?
        // commented - so doesn't show the rollover cursor.
        //
//      ,  RolloverRenderer
        {
        private PropertyChangeListener rolloverListener;
        private Border cellBorder;

        // Force user to specify TreeTableModel instead of more general
        // TreeModel
        public TreeTableCellRenderer(TreeTableModel model) {
            super(model);
            putClientProperty("JTree.lineStyle", "None");
            setRootVisible(false); // superclass default is "true"
            setShowsRootHandles(true); // superclass default is "false"
                /**
                 * TODO: Support truncated text directly in
                 * DefaultTreeCellRenderer.
                 */
            // removed as fix for #769-swingx: defaults for treetable should be same as tree
//            setOverwriteRendererIcons(true);
// setCellRenderer(new DefaultTreeRenderer());
            setCellRenderer(new ClippedTreeCellRenderer());
        }

        /**
         * {@inheritDoc} <p>
         *
         * Overridden to hack around #766-swingx: cursor flickering in DnD
         * when dragging over tree column. This is a core bug (#6700748) related
         * to painting the rendering component on a CellRendererPane. A trick
         * around is to let this return false. <p>
         *
         * This implementation applies the trick, that is returns false always.
         * The hack can be disabled by setting the treeTable's client property
         * DROP_HACK_FLAG_KEY to Boolean.FALSE.
         *
         */
        @Override
        public boolean isVisible() {
            return shouldApplyDropHack() ? false : super.isVisible();
        }

        /**
         * Returns a boolean indicating whether the drop hack should be applied.
         *
         * @return a boolean indicating whether the drop hack should be applied.
         */
        protected boolean shouldApplyDropHack() {
            return !Boolean.FALSE.equals(treeTable.getClientProperty(DROP_HACK_FLAG_KEY));
        }

        /**
         * Hack around #297-swingx: tooltips shown at wrong row.
         *
         * The problem is that - due to much tricksery when rendering the tree -
         * the given coordinates are rather useless. As a consequence, super
         * maps to wrong coordinates. This takes over completely.
         *
         * PENDING: bidi?
         *
         * @param event the mouseEvent in treetable coordinates
         * @param row the view row index
         * @param column the view column index
         * @return the tooltip as appropriate for the given row
         */
        private String getToolTipText(MouseEvent event, int row, int column) {
            if (row < 0) return null;
            String toolTip = null;
            TreeCellRenderer renderer = getCellRenderer();
            TreePath     path = getPathForRow(row);
            Object       lastPath = path.getLastPathComponent();
            Component    rComponent = renderer.getTreeCellRendererComponent
                (this, lastPath, isRowSelected(row),
                 isExpanded(row), getModel().isLeaf(lastPath), row,
                 true);

            if(rComponent instanceof JComponent) {
                Rectangle       pathBounds = getPathBounds(path);
                Rectangle cellRect = treeTable.getCellRect(row, column, false);
                // JW: what we are after
                // is the offset into the hierarchical column
                // then intersect this with the pathbounds
                Point mousePoint = event.getPoint();
                // translate to coordinates relative to cell
                mousePoint.translate(-cellRect.x, -cellRect.y);
                // translate horizontally to
                mousePoint.translate(-pathBounds.x, 0);
                // show tooltip only if over renderer?
//                if (mousePoint.x < 0) return null;
//                p.translate(-pathBounds.x, -pathBounds.y);
                MouseEvent newEvent = new MouseEvent(rComponent, event.getID(),
                      event.getWhen(),
                      event.getModifiers(),
                      mousePoint.x,
                      mousePoint.y,
//                    p.x, p.y,
                      event.getClickCount(),
                      event.isPopupTrigger());

                toolTip = ((JComponent)rComponent).getToolTipText(newEvent);
            }
            if (toolTip != null) {
                return toolTip;
            }
            return getToolTipText();
        }

        /**
         * {@inheritDoc} <p>
         *
         * Overridden to not automatically de/register itself from/to the ToolTipManager.
         * As rendering component it is not considered to be active in any way, so the
         * manager must not listen.
         */
        @Override
        public void setToolTipText(String text) {
            putClientProperty(TOOL_TIP_TEXT_KEY, text);
        }

        /**
         * Immutably binds this TreeTableModelAdapter to the specified JXTreeTable.
         * For internal use by JXTreeTable only.
         *
         * @param treeTable the JXTreeTable instance that this renderer is bound to
         */
        public final void bind(JXTreeTable treeTable) {
            // Suppress potentially subversive invocation!
            // Prevent clearing out the deck for possible hijack attempt later!
            if (treeTable == null) {
                throw new IllegalArgumentException("null treeTable");
            }

            if (this.treeTable == null) {
                this.treeTable = treeTable;
                // commented because still has issus
//                bindRollover();
            }
            else {
                throw new IllegalArgumentException("renderer already bound");
            }
        }

        /**
         * Install rollover support.
         * Not used - still has issues.
         * - not bidi-compliant
         * - no coordinate transformation for hierarchical column != 0
         * - method name clash enabled
         * - keyboard triggered click unreliable (triggers the treetable)
         * ...
         */
        @SuppressWarnings("unused")
        private void bindRollover() {
            setRolloverEnabled(treeTable.isRolloverEnabled());
            treeTable.addPropertyChangeListener(getRolloverListener());
        }

        /**
         * @return
         */
        private PropertyChangeListener getRolloverListener() {
            if (rolloverListener == null) {
                rolloverListener = createRolloverListener();
            }
            return rolloverListener;
        }

        /**
         * Creates and returns a property change listener for
         * table's rollover related properties.
         *
         * This implementation
         * - Synchs the tree's rolloverEnabled
         * - maps rollover cell from the table to the cell
         *   (still incomplete: first column only)
         *
         * @return
         */
        protected PropertyChangeListener createRolloverListener() {
            PropertyChangeListener l = new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if ((treeTable == null) || (treeTable != evt.getSource()))
                        return;
                    if ("rolloverEnabled".equals(evt.getPropertyName())) {
                        setRolloverEnabled(((Boolean) evt.getNewValue()).booleanValue());
                    }
                    if (RolloverProducer.ROLLOVER_KEY.equals(evt.getPropertyName())){
                        rollover(evt);
                    }
                }

                private void rollover(PropertyChangeEvent evt) {
                    boolean isHierarchical = isHierarchical((Point)evt.getNewValue());
                    putClientProperty(evt.getPropertyName(), isHierarchical ?
                           new Point((Point) evt.getNewValue()) : null);
                }

                private boolean isHierarchical(Point point) {
                    if (point != null) {
                        int column = point.x;
                        if (column >= 0) {
                            return treeTable.isHierarchical(column);
                        }
                    }
                   return false;
                }
                @SuppressWarnings("unused")
                Point rollover = new Point(-1, -1);
            };
            return l;
        }

        /**
         * {@inheritDoc} <p>
         *
         * Overridden to produce clicked client props only. The
         * rollover are produced by a propertyChangeListener to
         * the table's corresponding prop.
         *
         */
        @Override
        protected RolloverProducer createRolloverProducer() {
            return new RolloverProducer() {

                /**
                 * Overridden to do nothing.
                 *
                 * @param e
                 * @param property
                 */
                @Override
                protected void updateRollover(MouseEvent e, String property, boolean fireAlways) {
                    if (CLICKED_KEY.equals(property)) {
                        super.updateRollover(e, property, fireAlways);
                    }
                }
                @Override
                protected void updateRolloverPoint(JComponent component,
                        Point mousePoint) {
                    JXTree tree = (JXTree) component;
                    int row = tree.getClosestRowForLocation(mousePoint.x, mousePoint.y);
                    Rectangle bounds = tree.getRowBounds(row);
                    if (bounds == null) {
                        row = -1;
                    } else {
                        if ((bounds.y + bounds.height < mousePoint.y) ||
                                bounds.x > mousePoint.x)   {
                               row = -1;
                           }
                    }
                    int col = row < 0 ? -1 : 0;
                    rollover.x = col;
                    rollover.y = row;
                }

            };
        }

        @Override
        public void scrollRectToVisible(Rectangle aRect) {
            treeTable.scrollRectToVisible(aRect);
        }

        @Override
        protected void setExpandedState(TreePath path, boolean state) {
            // JW: fix for #1126 - CellEditors are removed immediately after starting an
            // edit if they involve a change of selection and the
            // expandsOnSelection property is true
            // back out if the selection change does not cause a change in
            // expansion state
            if (isExpanded(path) == state) return;
            // on change of expansion state, the editor's row might be changed
            // for simplicity, it's stopped always (even if the row is not changed)
            treeTable.getTreeTableHacker().completeEditing();
            super.setExpandedState(path, state);
            treeTable.getTreeTableHacker().expansionChanged();

        }

        /**
         * updateUI is overridden to set the colors of the Tree's renderer
         * to match that of the table.
         */
        @Override
        public void updateUI() {
            super.updateUI();
            // Make the tree's cell renderer use the table's cell selection
            // colors.
            // TODO JW: need to revisit...
            // a) the "real" of a JXTree is always wrapped into a DelegatingRenderer
            //  consequently the if-block never executes
            // b) even if it does it probably (?) should not
            // unconditionally overwrite custom selection colors.
            // Check for UIResources instead.
            TreeCellRenderer tcr = getCellRenderer();
            if (tcr instanceof DefaultTreeCellRenderer) {
                DefaultTreeCellRenderer dtcr = ((DefaultTreeCellRenderer) tcr);
                // For 1.1 uncomment this, 1.2 has a bug that will cause an
                // exception to be thrown if the border selection color is null.
                dtcr.setBorderSelectionColor(null);
                dtcr.setTextSelectionColor(
                    UIManager.getColor("Table.selectionForeground"));
                dtcr.setBackgroundSelectionColor(
                    UIManager.getColor("Table.selectionBackground"));
            }
        }

        /**
         * Sets the row height of the tree, and forwards the row height to
         * the table.
         *
         *
         */
        @Override
        public void setRowHeight(int rowHeight) {
            // JW: can't ... updateUI invoked with rowHeight = 0
            // hmmm... looks fishy ...
//            if (rowHeight <= 0) throw
//               new IllegalArgumentException("the rendering tree must have a fixed rowHeight > 0");
            super.setRowHeight(rowHeight);
            if (rowHeight > 0) {
                if (treeTable != null) {
                    treeTable.adjustTableRowHeight(rowHeight);
                }
            }
        }

        /**
         * This is overridden to set the location to (0, 0) and set
         * the dimension to exactly fill the bounds of the hierarchical
         * column.<p>
         */
        @Override
        public void setBounds(int x, int y, int w, int h) {
            // location is relative to the hierarchical column
            y = 0;
            x = 0;
            if (treeTable != null) {
                // adjust height to table height
                // It is not enough to set the height to treeTable.getHeight()
                // JW: why not?
                h = treeTable.getRowCount() * this.getRowHeight();
                int hierarchicalC = treeTable.getHierarchicalColumn();
                // JW: re-introduced to fix Issue 1168-swingx
                if (hierarchicalC >= 0) {
                    TableColumn column = treeTable.getColumn(hierarchicalC);
                    // adjust width to width of hierarchical column
                    w = column.getWidth();
                }
            }
            super.setBounds(x, y, w, h);
        }

        /**
         * Sublcassed to translate the graphics such that the last visible row
         * will be drawn at 0,0.
         */
        @Override
        public void paint(Graphics g) {
            Rectangle cellRect = treeTable.getCellRect(visibleRow, 0, false);
            g.translate(0, -cellRect.y);

            hierarchicalColumnWidth = getWidth();
            super.paint(g);

            Border border = cellBorder;
            if (highlightBorder != null) {
                border = highlightBorder;
            }
            // Draw the Table border if we have focus.
            if (border != null) {
                // #170: border not drawn correctly
                // JW: position the border to be drawn in translated area
                // still not satifying in all cases...
                // RG: Now it satisfies (at least for the row margins)
                // Still need to make similar adjustments for column margins...
                border.paintBorder(this, g, 0, cellRect.y,
                        getWidth(), cellRect.height);
            }
        }

        /**
         * {@inheritDoc} <p>
         *
         * Overridden to fix #swingx-1525: BorderHighlighter fills tree column.<p>
         *
         * Basically, the reason was that the border is set on the tree as a whole
         * instead of on the cell level. The fix is to bypass super completely, keep
         * a reference to the cell border and manually paint it around the cell
         * in the overridden paint. <p>
         *
         * Note: in the paint we need to paint either the focus border or the
         * cellBorder, the former taking precedence.
         *
         */
        @Override
        public void setBorder(Border border) {
            cellBorder = border;
        }

        public void doClick() {
            if ((getCellRenderer() instanceof RolloverRenderer)
                    && ((RolloverRenderer) getCellRenderer()).isEnabled()) {
                ((RolloverRenderer) getCellRenderer()).doClick();
            }

        }

        @Override
        public boolean isRowSelected(int row) {
            if ((treeTable == null) || (treeTable.getHierarchicalColumn() <0)) return false;
            return treeTable.isCellSelected(row, treeTable.getHierarchicalColumn());
        }

        @Override
        public Component getTableCellRendererComponent(JTable table,
            Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
            assert table == treeTable;
            // JW: quick fix for the tooltip part of #794-swingx:
            // visual properties must be reset in each cycle.
            // reverted - otherwise tooltip per Highlighter doesn't work
            //
//            setToolTipText(null);

            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            }
            else {
                setBackground(table.getBackground());
               setForeground(table.getForeground());
            }

            highlightBorder = null;
            if (treeTable != null) {
                if (treeTable.realEditingRow() == row &&
                    treeTable.getEditingColumn() == column) {
                }
                else if (hasFocus) {
                    highlightBorder = UIManager.getBorder(
                        "Table.focusCellHighlightBorder");
                }
            }

            visibleRow = row;

            return this;
        }

        private class ClippedTreeCellRenderer extends DefaultXTreeCellRenderer
            implements StringValue
            {
            @SuppressWarnings("unused")
            private boolean inpainting;
            private String shortText;
            @Override
            public void paint(Graphics g) {
                String fullText = super.getText();

                 shortText = SwingUtilities.layoutCompoundLabel(
                    this, g.getFontMetrics(), fullText, getIcon(),
                    getVerticalAlignment(), getHorizontalAlignment(),
                    getVerticalTextPosition(), getHorizontalTextPosition(),
                    getItemRect(itemRect), iconRect, textRect,
                    getIconTextGap());

                /** TODO: setText is more heavyweight than we want in this
                 * situation. Make JLabel.text protected instead of private.
         */

                try {
                    inpainting = true;
                    // TODO JW: don't - override getText to return the short version
                    // during painting
                    setText(shortText); // temporarily truncate text
                    super.paint(g);
                } finally {
                    inpainting = false;
                    setText(fullText); // restore full text
                }
            }

            private Rectangle getItemRect(Rectangle itemRect) {
                getBounds(itemRect);
//                LOG.info("rect" + itemRect);
                itemRect.width = hierarchicalColumnWidth - itemRect.x;
                return itemRect;
            }

            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                return super.getTreeCellRendererComponent(tree, getHierarchicalTableValue(value), sel, expanded, leaf,
                        row, hasFocus);
            }

            /**
             *
             * @param node the node in the treeModel as passed into the TreeCellRenderer
             * @return the corresponding value of the hierarchical cell in the TreeTableModel
             */
            private Object getHierarchicalTableValue(Object node) {
                Object val = node;

                if (treeTable != null) {
                    int treeColumn = treeTable.getTreeTableModel().getHierarchicalColumn();
                    Object o = null;
                    if (treeColumn >= 0) {
                        // following is unreliable during a paint cycle
                        // somehow interferes with BasicTreeUIs painting cache
//                        o = treeTable.getValueAt(row, treeColumn);
                        // ask the model - that's always okay
                        // might blow if the TreeTableModel is strict in
                        // checking the containment of the value and
                        // this renderer is called for sizing with a prototype
                        o = treeTable.getTreeTableModel().getValueAt(node, treeColumn);
                    }
                    val = o;
                }
                return val;
            }

            /**
             * {@inheritDoc} <p>
             */
            @Override
            public String getString(Object node) {
//                int treeColumn = treeTable.getTreeTableModel().getHierarchicalColumn();
//                if (treeColumn >= 0) {
//                    return StringValues.TO_STRING.getString(treeTable.getTreeTableModel().getValueAt(value, treeColumn));
//                }
                return StringValues.TO_STRING.getString(getHierarchicalTableValue(node));
            }

            // Rectangles filled in by SwingUtilities.layoutCompoundLabel();
            private final Rectangle iconRect = new Rectangle();
            private final Rectangle textRect = new Rectangle();
            // Rectangle filled in by this.getItemRect();
            private final Rectangle itemRect = new Rectangle();
        }

        /** Border to draw around the tree, if this is non-null, it will
         * be painted. */
        protected Border highlightBorder = null;
        protected JXTreeTable treeTable = null;
        protected int visibleRow = 0;

        // A JXTreeTable may not have more than one hierarchical column
        private int hierarchicalColumnWidth = 0;

    }

    /**
     * Returns the adapter that knows how to access the component data model.
     * The component data adapter is used by filters, sorters, and highlighters.
     *
     * @return the adapter that knows how to access the component data model
     */
    @Override
    protected ComponentAdapter getComponentAdapter() {
        if (dataAdapter == null) {
            dataAdapter = new TreeTableDataAdapter(this);
        }
        return dataAdapter;
    }

    protected static class TreeTableDataAdapter extends JXTable.TableAdapter {
        private final JXTreeTable table;

        /**
         * Constructs a <code>TreeTableDataAdapter</code> for the specified
         * target component.
         *
         * @param component the target component
         */
        public TreeTableDataAdapter(JXTreeTable component) {
            super(component);
            table = component;
        }

        public JXTreeTable getTreeTable() {
            return table;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isExpanded() {
            return table.isExpanded(row);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getDepth() {
            return table.getPathForRow(row).getPathCount() - 1;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isLeaf() {
            // Issue #270-swingx: guard against invisible row
            TreePath path = table.getPathForRow(row);
            if (path != null) {
                return table.getTreeTableModel().isLeaf(path.getLastPathComponent());
            }
            // JW: this is the same as BasicTreeUI.isLeaf.
            // Shouldn't happen anyway because must be called for visible rows only.
            return true;
        }
        /**
         *
         * @return true if the cell identified by this adapter displays hierarchical
         *      nodes; false otherwise
         */
        @Override
        public boolean isHierarchical() {
            return table.isHierarchical(column);
        }

        /**
         * {@inheritDoc} <p>
         *
         * Overridden to fix #821-swingx: string rep of hierarchical column incorrect.
         * In this case we must delegate to the tree directly (via treetable.getHierarchicalString).
         *
         * PENDING JW: revisit once we switch to really using a table renderer.
         */
        @Override
        public String getFilteredStringAt(int row, int column) {
            if (table.getTreeTableModel().getHierarchicalColumn() == column) {
                if (convertColumnIndexToView(column) < 0) {
                    // hidden hierarchical column, access directly
                    // PENDING JW: after introducing and wiring StringValueRegistry,
                    // had to change to query the hierarchicalString always
                    // could probably be done more elegantly, but ...
                }
                return table.getHierarchicalStringAt(row);
            }
            return super.getFilteredStringAt(row, column);
        }

        /**
         * {@inheritDoc} <p>
         *
         * Overridden to fix #821-swingx: string rep of hierarchical column incorrect.
         * In this case we must delegate to the tree directly (via treetable.getHierarchicalString).
         *
         * PENDING JW: revisit once we switch to really using a table renderer.
         */
        @Override
        public String getStringAt(int row, int column) {
            if (table.getTreeTableModel().getHierarchicalColumn() == column) {
                if (convertColumnIndexToView(column) < 0) {
                    // hidden hierarchical column, access directly
                    // PENDING JW: after introducing and wiring StringValueRegistry,
                    // had to change to query the hierarchicalString always
                    // could probably be done more elegantly, but ...
                }
                return table.getHierarchicalStringAt(row);
            }
            return super.getStringAt(row, column);
        }

    }

}
