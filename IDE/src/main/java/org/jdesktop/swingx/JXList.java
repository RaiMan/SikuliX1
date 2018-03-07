/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.jdesktop.swingx;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Vector;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ListUI;
import javax.swing.text.Position.Bias;

import org.jdesktop.beans.JavaBean;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.CompoundHighlighter;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.plaf.LookAndFeelAddons;
import org.jdesktop.swingx.plaf.UIAction;
import org.jdesktop.swingx.plaf.XListAddon;
import org.jdesktop.swingx.plaf.basic.core.BasicXListUI;
import org.jdesktop.swingx.renderer.AbstractRenderer;
import org.jdesktop.swingx.renderer.DefaultListRenderer;
import org.jdesktop.swingx.renderer.StringValue;
import org.jdesktop.swingx.rollover.ListRolloverController;
import org.jdesktop.swingx.rollover.ListRolloverProducer;
import org.jdesktop.swingx.rollover.RolloverProducer;
import org.jdesktop.swingx.rollover.RolloverRenderer;
import org.jdesktop.swingx.search.ListSearchable;
import org.jdesktop.swingx.search.SearchFactory;
import org.jdesktop.swingx.search.Searchable;
import org.jdesktop.swingx.sort.DefaultSortController;
import org.jdesktop.swingx.sort.ListSortController;
import org.jdesktop.swingx.sort.SortController;
import org.jdesktop.swingx.sort.StringValueRegistry;
import org.jdesktop.swingx.table.TableColumnExt;

/**
 * Enhanced List component with support for general SwingX sorting/filtering,
 * rendering, highlighting, rollover and search functionality. List specific
 * enhancements include ?? PENDING JW ...
 *
 * <h2>Sorting and Filtering</h2>
 * JXList supports sorting and filtering.
 *
 * Changed to use core support. Usage is very similar to J/X/Table.
 * It provides api to apply a specific sort order, to toggle the sort order and to reset a sort.
 * Sort sequence can be configured by setting a custom comparator.
 *
 * <pre><code>
 * list.setAutoCreateRowSorter(true);
 * list.setComparator(myComparator);
 * list.setSortOrder(SortOrder.DESCENDING);
 * list.toggleSortOder();
 * list.resetSortOrder();
 * </code></pre>
 *
 * <p>
 * JXList provides api to access items of the underlying model in view coordinates
 * and to convert from/to model coordinates.
 *
 * <b>Note</b>: JXList needs a specific ui-delegate - BasicXListUI and subclasses - which
 * is aware of model vs. view coordiate systems and which controls the synchronization of
 * selection/dataModel and sorter state. SwingX comes with a subclass for Synth.
 *
 * <h2>Rendering and Highlighting</h2>
 *
 * As all SwingX collection views, a JXList is a HighlighterClient (PENDING JW:
 * formally define and implement, like in AbstractTestHighlighter), that is it
 * provides consistent api to add and remove Highlighters which can visually
 * decorate the rendering component.
 * <p>
 *
 * <pre><code>
 *
 * JXList list = new JXList(new Contributors());
 * // implement a custom string representation, concated from first-, lastName
 * StringValue sv = new StringValue() {
 *     public String getString(Object value) {
 *        if (value instanceof Contributor) {
 *           Contributor contributor = (Contributor) value;
 *           return contributor.lastName() + ", " + contributor.firstName();
 *        }
 *        return StringValues.TO_STRING(value);
 *     }
 * };
 * list.setCellRenderer(new DefaultListRenderer(sv);
 * // highlight condition: gold merits
 * HighlightPredicate predicate = new HighlightPredicate() {
 *    public boolean isHighlighted(Component renderer,
 *                     ComponentAdapter adapter) {
 *       if (!(value instanceof Contributor)) return false;
 *       return ((Contributor) value).hasGold();
 *    }
 * };
 * // highlight with foreground color
 * list.addHighlighter(new PainterHighlighter(predicate, goldStarPainter);
 *
 * </code></pre>
 *
 * <i>Note:</i> to support the highlighting this implementation wraps the
 * ListCellRenderer set by client code with a DelegatingRenderer which applies
 * the Highlighter after delegating the default configuration to the wrappee. As
 * a side-effect, getCellRenderer does return the wrapper instead of the custom
 * renderer. To access the latter, client code must call getWrappedCellRenderer.
 * <p>
 *
 * <h2>Rollover</h2>
 *
 * As all SwingX collection views, a JXList supports per-cell rollover. If
 * enabled, the component fires rollover events on enter/exit of a cell which by
 * default is promoted to the renderer if it implements RolloverRenderer, that
 * is simulates live behaviour. The rollover events can be used by client code
 * as well, f.i. to decorate the rollover row using a Highlighter.
 *
 * <pre><code>
 *
 * JXList list = new JXList();
 * list.setRolloverEnabled(true);
 * list.setCellRenderer(new DefaultListRenderer());
 * list.addHighlighter(new ColorHighlighter(HighlightPredicate.ROLLOVER_ROW,
 *      null, Color.RED);
 *
 * </code></pre>
 *
 *
 * <h2>Search</h2>
 *
 * As all SwingX collection views, a JXList is searchable. A search action is
 * registered in its ActionMap under the key "find". The default behaviour is to
 * ask the SearchFactory to open a search component on this component. The
 * default keybinding is retrieved from the SearchFactory, typically ctrl-f (or
 * cmd-f for Mac). Client code can register custom actions and/or bindings as
 * appropriate.
 * <p>
 *
 * JXList provides api to vend a renderer-controlled String representation of
 * cell content. This allows the Searchable and Highlighters to use WYSIWYM
 * (What-You-See-Is-What-You-Match), that is pattern matching against the actual
 * string as seen by the user.
 *
 *
 * @author Ramesh Gupta
 * @author Jeanette Winzenburg
 */
@JavaBean
public class JXList extends JList {
    @SuppressWarnings("all")
    private static final Logger LOG = Logger.getLogger(JXList.class.getName());

    /**
     * UI Class ID
     */
    public final static String uiClassID = "XListUI";

    /**
     * Registers a Addon for JXList.
     */
    static {
        LookAndFeelAddons.contribute(new XListAddon());
    }


    public static final String EXECUTE_BUTTON_ACTIONCOMMAND = "executeButtonAction";

    /**
     * The pipeline holding the highlighters.
     */
    protected CompoundHighlighter compoundHighlighter;

    /** listening to changeEvents from compoundHighlighter. */
    private ChangeListener highlighterChangeListener;

    /** The ComponentAdapter for model data access. */
    protected ComponentAdapter dataAdapter;

    /**
     * Mouse/Motion/Listener keeping track of mouse moved in cell coordinates.
     */
    private RolloverProducer rolloverProducer;

    /**
     * RolloverController: listens to cell over events and repaints
     * entered/exited rows.
     */
    private ListRolloverController<JXList> linkController;

    /** A wrapper around the default renderer enabling decoration. */
    private transient DelegatingRenderer delegatingRenderer;

    private Searchable searchable;

    private Comparator<?> comparator;

    private boolean autoCreateRowSorter;

    private RowSorter<? extends ListModel> rowSorter;

    private boolean sortable;

    private boolean sortsOnUpdates;

    private StringValueRegistry stringValueRegistry;

    private SortOrder[] sortOrderCycle;

    /**
    * Constructs a <code>JXList</code> with an empty model and filters disabled.
    *
    */
    public JXList() {
        this(false);
    }

    /**
     * Constructs a <code>JXList</code> that displays the elements in the
     * specified, non-<code>null</code> model and automatic creation of a RowSorter disabled.
     *
     * @param dataModel   the data model for this list
     * @exception IllegalArgumentException   if <code>dataModel</code>
     *                                           is <code>null</code>
     */
    public JXList(ListModel dataModel) {
        this(dataModel, false);
    }

    /**
     * Constructs a <code>JXList</code> that displays the elements in
     * the specified array and automatic creation of a RowSorter disabled.
     *
     * @param  listData  the array of Objects to be loaded into the data model
     * @throws IllegalArgumentException   if <code>listData</code>
     *                                          is <code>null</code>
     */
    public JXList(Object[] listData) {
        this(listData, false);
    }

    /**
     * Constructs a <code>JXList</code> that displays the elements in
     * the specified <code>Vector</code> and automatic creation of a RowSorter disabled.
     *
     * @param  listData  the <code>Vector</code> to be loaded into the
     *          data model
     * @throws IllegalArgumentException   if <code>listData</code>
     *                                          is <code>null</code>
     */
    public JXList(Vector<?> listData) {
        this(listData, false);
    }

    /**
     * Constructs a <code>JXList</code> with an empty model and
     * automatic creation of a RowSorter as given.
     *
     * @param autoCreateRowSorter <code>boolean</code> to determine if
     *  a RowSorter should be created automatically.
     */
    public JXList(boolean autoCreateRowSorter) {
        init(autoCreateRowSorter);
    }

    /**
     * Constructs a <code>JXList</code> with the specified model and
     * automatic creation of a RowSorter as given.
     *
     * @param dataModel   the data model for this list
     * @param autoCreateRowSorter <code>boolean</code> to determine if
     *  a RowSorter should be created automatically.
     * @throws IllegalArgumentException   if <code>dataModel</code>
     *                                          is <code>null</code>
     */
    public JXList(ListModel dataModel, boolean autoCreateRowSorter) {
        super(dataModel);
        init(autoCreateRowSorter);
    }

    /**
     * Constructs a <code>JXList</code> that displays the elements in
     * the specified array and automatic creation of a RowSorter as given.
     *
     * @param  listData  the array of Objects to be loaded into the data model
     * @param autoCreateRowSorter <code>boolean</code> to determine if
     *  a RowSorter should be created automatically.
     * @throws IllegalArgumentException   if <code>listData</code>
     *                                          is <code>null</code>
     */
    public JXList(Object[] listData, boolean autoCreateRowSorter) {
        super(listData);
        if (listData == null)
           throw new IllegalArgumentException("listData must not be null");
        init(autoCreateRowSorter);
    }

    /**
     * Constructs a <code>JXList</code> that displays the elements in
     * the specified <code>Vector</code> and filtersEnabled property.
     *
     * @param  listData  the <code>Vector</code> to be loaded into the
     *          data model
     * @param autoCreateRowSorter <code>boolean</code> to determine if
     *  a RowSorter should be created automatically.
     * @throws IllegalArgumentException if <code>listData</code> is <code>null</code>
     */
    public JXList(Vector<?> listData, boolean autoCreateRowSorter) {
        super(listData);
        if (listData == null)
           throw new IllegalArgumentException("listData must not be null");
        init(autoCreateRowSorter);
    }

    private void init(boolean autoCreateRowSorter) {
        sortOrderCycle = DefaultSortController.getDefaultSortOrderCycle();
        setSortable(true);
        setSortsOnUpdates(true);
        setAutoCreateRowSorter(autoCreateRowSorter);
        Action findAction = createFindAction();
        getActionMap().put("find", findAction);

        KeyStroke findStroke = SearchFactory.getInstance().getSearchAccelerator();
        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(findStroke, "find");
    }

    private Action createFindAction() {
        return new UIAction("find") {
            @Override
            public void actionPerformed(ActionEvent e) {
                doFind();
            }
        };
    }

    /**
     * Starts a search on this List's visible items. This implementation asks the
     * SearchFactory to open a find widget on itself.
     */
    protected void doFind() {
        SearchFactory.getInstance().showFindInput(this, getSearchable());
    }

    /**
     * Returns a Searchable for this component, guaranteed to be not null. This
     * implementation lazily creates a ListSearchable if necessary.
     *
     * @return a not-null Searchable for this list.
     *
     * @see #setSearchable(Searchable)
     * @see org.jdesktop.swingx.search.ListSearchable
     */
    public Searchable getSearchable() {
        if (searchable == null) {
            searchable = new ListSearchable(this);
        }
        return searchable;
    }

    /**
     * Sets the Searchable for this component. If null, a default
     * Searchable will be created and used.
     *
     * @param searchable the Searchable to use for this component, may be null to indicate
     *   using the list's default searchable.
     * @see #getSearchable()
     */
    public void setSearchable(Searchable searchable) {
        this.searchable = searchable;
    }

    /**
     * {@inheritDoc} <p>
     *
     * Overridden to cope with sorting/filtering, taking over completely.
     */
    @Override
    public int getNextMatch(String prefix, int startIndex, Bias bias) {
        Pattern pattern = Pattern.compile("^" + prefix, Pattern.CASE_INSENSITIVE);
        return getSearchable().search(pattern, startIndex, bias ==Bias.Backward);
    }
//--------------------- Rollover support

    /**
     * Sets the property to enable/disable rollover support. If enabled, the list
     * fires property changes on per-cell mouse rollover state, i.e.
     * when the mouse enters/leaves a list cell. <p>
     *
     * This can be enabled to show "live" rollover behaviour, f.i. the cursor over a cell
     * rendered by a JXHyperlink.<p>
     *
     * Default value is disabled.
     *
     * @param rolloverEnabled a boolean indicating whether or not the rollover
     *   functionality should be enabled.
     *
     * @see #isRolloverEnabled()
     * @see #getLinkController()
     * @see #createRolloverProducer()
     * @see org.jdesktop.swingx.rollover.RolloverRenderer
     *
     */
    public void setRolloverEnabled(boolean rolloverEnabled) {
        boolean old = isRolloverEnabled();
        if (rolloverEnabled == old)
            return;
        if (rolloverEnabled) {
            rolloverProducer = createRolloverProducer();
            rolloverProducer.install(this);
            getLinkController().install(this);
        } else {
            rolloverProducer.release(this);
            rolloverProducer = null;
            getLinkController().release();
        }
        firePropertyChange("rolloverEnabled", old, isRolloverEnabled());
    }

    /**
     * Returns a boolean indicating whether or not rollover support is enabled.
     *
     * @return a boolean indicating whether or not rollover support is enabled.
     *
     * @see #setRolloverEnabled(boolean)
     */
    public boolean isRolloverEnabled() {
        return rolloverProducer != null;
    }

    /**
     * Returns the RolloverController for this component. Lazyly creates the
     * controller if necessary, that is the return value is guaranteed to be
     * not null. <p>
     *
     * PENDING JW: rename to getRolloverController
     *
     * @return the RolloverController for this tree, guaranteed to be not null.
     *
     * @see #setRolloverEnabled(boolean)
     * @see #createLinkController()
     * @see org.jdesktop.swingx.rollover.RolloverController
     */
    protected ListRolloverController<JXList> getLinkController() {
        if (linkController == null) {
            linkController = createLinkController();
        }
        return linkController;
    }

    /**
     * Creates and returns a RolloverController appropriate for this component.
     *
     * @return a RolloverController appropriate for this component.
     *
     * @see #getLinkController()
     * @see org.jdesktop.swingx.rollover.RolloverController
     */
    protected ListRolloverController<JXList> createLinkController() {
        return new ListRolloverController<JXList>();
    }

    /**
     * Creates and returns the RolloverProducer to use with this tree.
     * <p>
     *
     * @return <code>RolloverProducer</code> to use with this tree
     *
     * @see #setRolloverEnabled(boolean)
     */
    protected RolloverProducer createRolloverProducer() {
        return new ListRolloverProducer();
    }

    //--------------------- public sort api

    /**
     * Returns {@code true} if whenever the model changes, a new
     * {@code RowSorter} should be created and installed
     * as the table's sorter; otherwise, returns {@code false}.
     *
     * @return true if a {@code RowSorter} should be created when
     *         the model changes
     * @since 1.6
     */
    public boolean getAutoCreateRowSorter() {
        return autoCreateRowSorter;
    }

    /**
     * Specifies whether a {@code RowSorter} should be created for the
     * list whenever its model changes.
     * <p>
     * When {@code setAutoCreateRowSorter(true)} is invoked, a {@code
     * RowSorter} is immediately created and installed on the
     * list.  While the {@code autoCreateRowSorter} property remains
     * {@code true}, every time the model is changed, a new {@code
     * RowSorter} is created and set as the list's row sorter.<p>
     *
     * The default value is false.
     *
     * @param autoCreateRowSorter whether or not a {@code RowSorter}
     *        should be automatically created
     * @beaninfo
     *        bound: true
     *    preferred: true
     *  description: Whether or not to turn on sorting by default.
     */
    public void setAutoCreateRowSorter(boolean autoCreateRowSorter) {
        if (getAutoCreateRowSorter() == autoCreateRowSorter) return;
        boolean oldValue = getAutoCreateRowSorter();
        this.autoCreateRowSorter = autoCreateRowSorter;
        if (autoCreateRowSorter) {
            setRowSorter(createDefaultRowSorter());
        }
        firePropertyChange("autoCreateRowSorter", oldValue,
                           getAutoCreateRowSorter());
    }

    /**
     * Creates and returns the default RowSorter. Note that this is already
     * configured to the current ListModel.
     *
     * PENDING JW: review method signature - better expose the need for the
     * model by adding a parameter?
     *
     * @return the default RowSorter.
     */
    protected RowSorter<? extends ListModel> createDefaultRowSorter() {
        return new ListSortController<ListModel>(getModel());
    }
    /**
     * Returns the object responsible for sorting.
     *
     * @return the object responsible for sorting
     * @since 1.6
     */
    public RowSorter<? extends ListModel> getRowSorter() {
        return rowSorter;
    }

    /**
     * Sets the <code>RowSorter</code>.  <code>RowSorter</code> is used
     * to provide sorting and filtering to a <code>JXList</code>.
     * <p>
     * This method clears the selection and resets any variable row heights.
     * <p>
     * If the underlying model of the <code>RowSorter</code> differs from
     * that of this <code>JXList</code> undefined behavior will result.
     *
     * @param sorter the <code>RowSorter</code>; <code>null</code> turns
     *        sorting off
     */
    public void setRowSorter(RowSorter<? extends ListModel> sorter) {
        RowSorter<? extends ListModel> oldRowSorter = getRowSorter();
        this.rowSorter = sorter;
        configureSorterProperties();
        firePropertyChange("rowSorter", oldRowSorter, sorter);
    }

    /**
     * Propagates sort-related properties from table/columns to the sorter if it
     * is of type SortController, does nothing otherwise.
     *
     */
    protected void configureSorterProperties() {
        if (!getControlsSorterProperties()) return;
        // configure from table properties
        getSortController().setSortable(sortable);
        getSortController().setSortsOnUpdates(sortsOnUpdates);
        getSortController().setComparator(0, comparator);
        getSortController().setSortOrderCycle(getSortOrderCycle());
        getSortController().setStringValueProvider(getStringValueRegistry());
    }

    /**
     * Sets &quot;sortable&quot; property indicating whether or not this list
     * isSortable.
     *
     * <b>Note</b>: as of post-1.0 this property is propagated to the SortController.
     * Whether or not a change triggers a re-sort is up to either the concrete controller
     * implementation (the default doesn't) or client code. This behaviour is
     * different from old SwingX style sorting.
     *
     * @see TableColumnExt#isSortable()
     * @param sortable boolean indicating whether or not this table supports
     *        sortable columns
     */
    public void setSortable(boolean sortable) {
        boolean old = isSortable();
        this.sortable = sortable;
        if (getControlsSorterProperties()) {
            getSortController().setSortable(sortable);
        }
        firePropertyChange("sortable", old, isSortable());
    }

    /**
     * Returns the table's sortable property.<p>
     *
     * @return true if the table is sortable.
     */
    public boolean isSortable() {
        return sortable;
    }

    /**
     * If true, specifies that a sort should happen when the underlying
     * model is updated (<code>rowsUpdated</code> is invoked).  For
     * example, if this is true and the user edits an entry the
     * location of that item in the view may change.  The default is
     * true.
     *
     * @param sortsOnUpdates whether or not to sort on update events
     */
    public void setSortsOnUpdates(boolean sortsOnUpdates) {
        boolean old = getSortsOnUpdates();
        this.sortsOnUpdates = sortsOnUpdates;
        if (getControlsSorterProperties()) {
            getSortController().setSortsOnUpdates(sortsOnUpdates);
        }
        firePropertyChange("sortsOnUpdates", old, getSortsOnUpdates());
    }

    /**
     * Returns true if  a sort should happen when the underlying
     * model is updated; otherwise, returns false.
     *
     * @return whether or not to sort when the model is updated
     */
    public boolean getSortsOnUpdates() {
        return sortsOnUpdates;
    }

    /**
     * Sets the sortorder cycle used when toggle sorting this table's columns.
     * This property is propagated to the SortController
     * if controlsSorterProperties is true.
     *
     * @param cycle the sequence of zero or more not-null SortOrders to cycle through.
     * @throws NullPointerException if the array or any of its elements are null
     *
     */
    public void setSortOrderCycle(SortOrder... cycle) {
        SortOrder[] old = getSortOrderCycle();
        if (getControlsSorterProperties()) {
            getSortController().setSortOrderCycle(cycle);
        }
        this.sortOrderCycle = Arrays.copyOf(cycle, cycle.length);
        firePropertyChange("sortOrderCycle", old, getSortOrderCycle());
    }

    /**
     * Returns the sortOrder cycle used when toggle sorting this table's columns, guaranteed
     * to be not null.
     *
     * @return the sort order cycle used in toggle sort, not null
     */
    public SortOrder[] getSortOrderCycle() {
        return Arrays.copyOf(sortOrderCycle, sortOrderCycle.length);
    }

    /**
     *
     * @return the comparator used.
     * @see #setComparator(Comparator)
     */
    public Comparator<?> getComparator() {
        return comparator;
    }

    /**
     * Sets the comparator to use for sorting.<p>
     *
     * <b>Note</b>: as of post-1.0 the property is propagated to the SortController,
     * if available.
     * Whether or not a change triggers a re-sort is up to either the concrete controller
     * implementation (the default doesn't) or client code. This behaviour is
     * different from old SwingX style sorting.
     *
     * @param comparator the comparator to use.
     */
    public void setComparator(Comparator<?> comparator) {
        Comparator<?> old = getComparator();
        this.comparator = comparator;
        updateSortAfterComparatorChange();
        firePropertyChange("comparator", old, getComparator());
    }

    /**
     * Updates the SortController's comparator, if available. Does nothing otherwise.
     *
     */
    protected void updateSortAfterComparatorChange() {
        if (getControlsSorterProperties()) {
            getSortController().setComparator(0, getComparator());
        }
    }

//------------------------- sort: do sort/filter

    /**
     * Sets the filter to the sorter, if available and of type SortController.
     * Does nothing otherwise.
     * <p>
     *
     * @param filter the filter used to determine what entries should be
     *        included
     */
    @SuppressWarnings("unchecked")
    public <R extends ListModel> void setRowFilter(RowFilter<? super R, ? super Integer> filter) {
        if (hasSortController()) {
            // all fine, because R is a ListModel (R extends ListModel)
            SortController<R> controller = (SortController<R>) getSortController();
            controller.setRowFilter(filter);
        }
    }

    /**
     * Returns the filter of the sorter, if available and of type SortController.
     * Returns null otherwise.<p>
     *
     * PENDING JW: generics? had to remove return type from getSortController to
     * make this compilable, so probably wrong.
     *
     * @return the filter used in the sorter.
     */
    @SuppressWarnings("unchecked")
    public RowFilter<?, ?> getRowFilter() {
        return hasSortController() ? getSortController().getRowFilter() : null;
    }

    /**
     * Resets sorting of all columns.
     * Delegates to the SortController if available, or does nothing if not.<p>
     *
     * PENDING JW: method name - consistent in SortController and here.
     *
     */
    public void resetSortOrder() {
        if (hasSortController())
            getSortController().resetSortOrders();
    }

    /**
     *
     * Toggles the sort order of the list.
     * Delegates to the SortController if available, or does nothing if not.<p>
     *
     * <p>
     * The exact behaviour is defined by the SortController's toggleSortOrder
     * implementation. Typically a unsorted list is sorted in ascending order,
     * a sorted list's order is reversed.
     * <p>
     *
     *
     */
    public void toggleSortOrder() {
        if (hasSortController())
            getSortController().toggleSortOrder(0);
    }

    /**
     * Sorts the list using SortOrder.
     * Delegates to the SortController if available, or does nothing if not.<p>
     *
     * @param sortOrder the sort order to use.
     *
     */
    public void setSortOrder(SortOrder sortOrder) {
        if (hasSortController())
            getSortController().setSortOrder(0, sortOrder);
    }

    /**
     * Returns the SortOrder.
     * Delegates to the SortController if available, or returns SortOrder.UNSORTED if not.<p>
     *
     * @return the current SortOrder
     */
    public SortOrder getSortOrder() {
        if (hasSortController())
            return getSortController().getSortOrder(0);
        return SortOrder.UNSORTED;
    }

    /**
     * Returns the currently active SortController. May be null if RowSorter
     * is null or not of type SortController.<p>
     *
     * PENDING JW: swaying about hiding or not - currently the only way to
     * make the view not configure a RowSorter of type SortController is to
     * let this return null.
     *
     * @return the currently active <code>SortController</code> may be null
     */
    @SuppressWarnings("unchecked")
    protected SortController<? extends ListModel> getSortController() {
        if (hasSortController()) {
            // JW: the RowSorter is always of type <? extends ListModel>
            // so the unchecked cast is safe
            return (SortController<? extends ListModel>) getRowSorter();
        }
        return null;
    }

    /**
     * Returns a boolean indicating whether the table has a SortController.
     * If true, the call to getSortController is guaranteed to return a not-null
     * value.
     *
     * @return a boolean indicating whether the table has a SortController.
     *
     * @see #getSortController()
     */
    protected boolean hasSortController() {
        return getRowSorter() instanceof SortController<?>;
    }

    /**
     * Returns a boolean indicating whether the table configures the sorter's
     * properties. If true, guaranteed that table's and the columns' sort related
     * properties are propagated to the sorter. If false, guaranteed to not
     * touch the sorter's configuration.<p>
     *
     * This implementation returns true if the sorter is of type SortController.
     *
     * Note: the synchronization is unidirection from the table to the sorter.
     * Changing the sorter under the table's feet might lead to undefined
     * behaviour.
     *
     * @return a boolean indicating whether the table configurers the sorter's
     *  properties.
     */
    protected boolean getControlsSorterProperties() {
        return hasSortController() && getAutoCreateRowSorter();
    }

    // ---------------------------- filters

    /**
     * Returns the element at the given index. The index is in view coordinates
     * which might differ from model coordinates if filtering is enabled and
     * filters/sorters are active.
     *
     * @param viewIndex the index in view coordinates
     * @return the element at the index
     * @throws IndexOutOfBoundsException if viewIndex < 0 or viewIndex >=
     *         getElementCount()
     */
    public Object getElementAt(int viewIndex) {
        return getModel().getElementAt(convertIndexToModel(viewIndex));
    }

    /**
     * Returns the value for the smallest selected cell index;
     * <i>the selected value</i> when only a single item is selected in the
     * list. When multiple items are selected, it is simply the value for the
     * smallest selected index. Returns {@code null} if there is no selection.
     * <p>
     * This is a convenience method that simply returns the model value for
     * {@code getMinSelectionIndex}, taking into account sorting and filtering.
     *
     * @return the first selected value
     * @see #getMinSelectionIndex
     * @see #getModel
     * @see #addListSelectionListener
     */
    @Override
    public Object getSelectedValue() {
        int i = getSelectedIndex();
        return (i == -1) ? null : getElementAt(i);
    }

    /**
     * Selects the specified object from the list, taking into account
     * sorting and filtering.
     *
     * @param anObject      the object to select
     * @param shouldScroll  {@code true} if the list should scroll to display
     *                      the selected object, if one exists; otherwise {@code false}
     */
    @Override
    public void setSelectedValue(Object anObject,boolean shouldScroll) {
        // Note: this method is a copy of JList.setSelectedValue,
        // including comments. It simply usues getElementCount() and getElementAt()
        // instead of the model.
        if(anObject == null)
            setSelectedIndex(-1);
        else if(!anObject.equals(getSelectedValue())) {
            int i,c;
            for(i=0,c=getElementCount();i<c;i++)
                if(anObject.equals(getElementAt(i))){
                    setSelectedIndex(i);
                    if(shouldScroll)
                        ensureIndexIsVisible(i);
                    repaint();  /** FIX-ME setSelectedIndex does not redraw all the time with the basic l&f**/
                    return;
                }
            setSelectedIndex(-1);
        }
        repaint(); /** FIX-ME setSelectedIndex does not redraw all the time with the basic l&f**/
    }

    /**
     * Returns an array of all the selected values, in increasing order based
     * on their indices in the list and taking into account sourting and filtering.
     *
     * @return the selected values, or an empty array if nothing is selected
     * @see #isSelectedIndex
     * @see #getModel
     * @see #addListSelectionListener
     */
    @Override
    public Object[] getSelectedValues() {
        int[] selectedIndexes = getSelectedIndices();
        Object[] selectedValues = new Object[selectedIndexes.length];
        for (int i = 0; i < selectedIndexes.length; i++) {
            selectedValues[i] = getElementAt(selectedIndexes[i]);
        }
        return selectedValues;
    }

    /**     * Returns the number of elements in this list in view
     * coordinates. If filters are active this number might be
     * less than the number of elements in the underlying model.
     *
     * @return number of elements in this list in view coordinates
     */
    public int getElementCount() {
        return getRowSorter() != null ?
                getRowSorter().getViewRowCount(): getModel().getSize();
    }

    /**
     * Convert row index from view coordinates to model coordinates accounting
     * for the presence of sorters and filters.
     *
     * @param viewIndex index in view coordinates
     * @return index in model coordinates
     * @throws IndexOutOfBoundsException if viewIndex < 0 or viewIndex >= getElementCount()
     */
    public int convertIndexToModel(int viewIndex) {
        return getRowSorter() != null ?
                getRowSorter().convertRowIndexToModel(viewIndex):viewIndex;
    }

    /**
     * Convert index from model coordinates to view coordinates accounting
     * for the presence of sorters and filters.
     *
     * @param modelIndex index in model coordinates
     * @return index in view coordinates if the model index maps to a view coordinate
     *          or -1 if not contained in the view.
     *
     */
    public int convertIndexToView(int modelIndex) {
        return getRowSorter() != null
            ? getRowSorter().convertRowIndexToView(modelIndex) : modelIndex;
    }

    /**
     * {@inheritDoc} <p>
     *
     * Sets the underlying data model. Note that if isFilterEnabled you must
     * call getWrappedModel to access the model given here. In this case
     * getModel returns a wrapper around the data!
     *
     * @param model the data model for this list.
     *
     */
    @Override
    public void setModel(ListModel model) {
        super.setModel(model);
        if (getAutoCreateRowSorter()) {
            setRowSorter(createDefaultRowSorter());
        }
    }

    // ---------------------------- uniform data model

    /**
     * @return the unconfigured ComponentAdapter.
     */
    protected ComponentAdapter getComponentAdapter() {
        if (dataAdapter == null) {
            dataAdapter = new ListAdapter(this);
        }
        return dataAdapter;
    }

    /**
     * Convenience to access a configured ComponentAdapter.
     * Note: the column index of the configured adapter is always 0.
     *
     * @param index the row index in view coordinates, must be valid.
     * @return the configured ComponentAdapter.
     */
    protected ComponentAdapter getComponentAdapter(int index) {
        ComponentAdapter adapter = getComponentAdapter();
        adapter.column = 0;
        adapter.row = index;
        return adapter;
    }

    /**
     * A component adapter targeted at a JXList.
     */
    protected static class ListAdapter extends ComponentAdapter {
        private final JXList list;

        /**
         * Constructs a <code>ListAdapter</code> for the specified target
         * JXList.
         *
         * @param component  the target list.
         */
        public ListAdapter(JXList component) {
            super(component);
            list = component;
        }

        /**
         * Typesafe accessor for the target component.
         *
         * @return the target component as a {@link org.jdesktop.swingx.JXList}
         */
        public JXList getList() {
            return list;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean hasFocus() {
            /** TODO: Think through printing implications */
            return list.isFocusOwner() && (row == list.getLeadSelectionIndex());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getRowCount() {
            return list.getModel().getSize();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Object getValueAt(int row, int column) {
            return list.getModel().getElementAt(row);
        }

        /**
         * {@inheritDoc}
         * This is implemented to query the table's StringValueRegistry for an appropriate
         * StringValue and use that for getting the string representation.
         */
        @Override
        public String getStringAt(int row, int column) {
            StringValue sv = list.getStringValueRegistry().getStringValue(row, column);
            return sv.getString(getValueAt(row, column));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Rectangle getCellBounds() {
            return list.getCellBounds(row, row);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isEditable() {
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isSelected() {
            /** TODO: Think through printing implications */
            return list.isSelectedIndex(row);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int convertRowIndexToView(int rowModelIndex) {
            return list.convertIndexToView(rowModelIndex);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int convertRowIndexToModel(int rowViewIndex) {
            return list.convertIndexToModel(rowViewIndex);
        }
    }

    // ------------------------------ renderers


    /**
     * Sets the <code>Highlighter</code>s to the table, replacing any old settings.
     * None of the given Highlighters must be null.<p>
     *
     * This is a bound property. <p>
     *
     * Note: as of version #1.257 the null constraint is enforced strictly. To remove
     * all highlighters use this method without param.
     *
     * @param highlighters zero or more not null highlighters to use for renderer decoration.
     * @throws NullPointerException if array is null or array contains null values.
     *
     * @see #getHighlighters()
     * @see #addHighlighter(Highlighter)
     * @see #removeHighlighter(Highlighter)
     *
     */
    public void setHighlighters(Highlighter... highlighters) {
        Highlighter[] old = getHighlighters();
        getCompoundHighlighter().setHighlighters(highlighters);
        firePropertyChange("highlighters", old, getHighlighters());
    }

    /**
     * Returns the <code>Highlighter</code>s used by this table.
     * Maybe empty, but guarantees to be never null.
     *
     * @return the Highlighters used by this table, guaranteed to never null.
     * @see #setHighlighters(Highlighter[])
     */
    public Highlighter[] getHighlighters() {
        return getCompoundHighlighter().getHighlighters();
    }
    /**
     * Appends a <code>Highlighter</code> to the end of the list of used
     * <code>Highlighter</code>s. The argument must not be null.
     * <p>
     *
     * @param highlighter the <code>Highlighter</code> to add, must not be null.
     * @throws NullPointerException if <code>Highlighter</code> is null.
     *
     * @see #removeHighlighter(Highlighter)
     * @see #setHighlighters(Highlighter[])
     */
    public void addHighlighter(Highlighter highlighter) {
        Highlighter[] old = getHighlighters();
        getCompoundHighlighter().addHighlighter(highlighter);
        firePropertyChange("highlighters", old, getHighlighters());
    }

    /**
     * Removes the given Highlighter. <p>
     *
     * Does nothing if the Highlighter is not contained.
     *
     * @param highlighter the Highlighter to remove.
     * @see #addHighlighter(Highlighter)
     * @see #setHighlighters(Highlighter...)
     */
    public void removeHighlighter(Highlighter highlighter) {
        Highlighter[] old = getHighlighters();
        getCompoundHighlighter().removeHighlighter(highlighter);
        firePropertyChange("highlighters", old, getHighlighters());
    }

    /**
     * Returns the CompoundHighlighter assigned to the table, null if none.
     * PENDING: open up for subclasses again?.
     *
     * @return the CompoundHighlighter assigned to the table.
     */
    protected CompoundHighlighter getCompoundHighlighter() {
        if (compoundHighlighter == null) {
            compoundHighlighter = new CompoundHighlighter();
            compoundHighlighter.addChangeListener(getHighlighterChangeListener());
        }
        return compoundHighlighter;
    }

    /**
     * Returns the <code>ChangeListener</code> to use with highlighters. Lazily
     * creates the listener.
     *
     * @return the ChangeListener for observing changes of highlighters,
     *   guaranteed to be <code>not-null</code>
     */
    protected ChangeListener getHighlighterChangeListener() {
        if (highlighterChangeListener == null) {
            highlighterChangeListener = createHighlighterChangeListener();
        }
        return highlighterChangeListener;
    }

    /**
     * Creates and returns the ChangeListener observing Highlighters.
     * <p>
     * Here: repaints the table on receiving a stateChanged.
     *
     * @return the ChangeListener defining the reaction to changes of
     *         highlighters.
     */
    protected ChangeListener createHighlighterChangeListener() {
        return new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                repaint();
            }
        };
    }

    /**
     * Returns the StringValueRegistry which defines the string representation for
     * each cells. This is strictly for internal use by the table, which has the
     * responsibility to keep in synch with registered renderers.<p>
     *
     * Currently exposed for testing reasons, client code is recommended to not use nor override.
     *
     * @return the current string value registry
     */
    protected StringValueRegistry getStringValueRegistry() {
        if (stringValueRegistry == null) {
            stringValueRegistry = createDefaultStringValueRegistry();
        }
        return stringValueRegistry;
    }

    /**
     * Creates and returns the default registry for StringValues.<p>
     *
     * @return the default registry for StringValues.
     */
    protected StringValueRegistry createDefaultStringValueRegistry() {
        return new StringValueRegistry();
    }


    /**
     * Returns the string representation of the cell value at the given position.
     *
     * @param row the row index of the cell in view coordinates
     * @return the string representation of the cell value as it will appear in the
     *   table.
     */
    public String getStringAt(int row) {
        // changed implementation to use StringValueRegistry
        StringValue stringValue = getStringValueRegistry().getStringValue(
                convertIndexToModel(row), 0);
        return stringValue.getString(getElementAt(row));
    }

    private DelegatingRenderer getDelegatingRenderer() {
        if (delegatingRenderer == null) {
            // only called once... to get hold of the default?
            delegatingRenderer = new DelegatingRenderer();
        }
        return delegatingRenderer;
    }

    /**
     * Creates and returns the default cell renderer to use. Subclasses
     * may override to use a different type. Here: returns a <code>DefaultListRenderer</code>.
     *
     * @return the default cell renderer to use with this list.
     */
    protected ListCellRenderer createDefaultCellRenderer() {
        return new DefaultListRenderer();
    }

    /**
     * {@inheritDoc} <p>
     *
     * Overridden to return the delegating renderer which is wrapped around the
     * original to support highlighting. The returned renderer is of type
     * DelegatingRenderer and guaranteed to not-null<p>
     *
     * @see #setCellRenderer(ListCellRenderer)
     * @see DelegatingRenderer
     */
    @Override
    public ListCellRenderer getCellRenderer() {
        return getDelegatingRenderer();
    }

    /**
     * Returns the renderer installed by client code or the default if none has
     * been set.
     *
     * @return the wrapped renderer.
     * @see #setCellRenderer(ListCellRenderer)
     */
    public ListCellRenderer getWrappedCellRenderer() {
        return getDelegatingRenderer().getDelegateRenderer();
    }

    /**
     * {@inheritDoc} <p>
     *
     * Overridden to wrap the given renderer in a DelegatingRenderer to support
     * highlighting. <p>
     *
     * Note: the wrapping implies that the renderer returned from the getCellRenderer
     * is <b>not</b> the renderer as given here, but the wrapper. To access the original,
     * use <code>getWrappedCellRenderer</code>.
     *
     * @see #getWrappedCellRenderer()
     * @see #getCellRenderer()
     *
     */
    @Override
    public void setCellRenderer(ListCellRenderer renderer) {
        // PENDING JW: super fires for very first setting
        // as defaults are automagically set (by delegatingRenderer
        // using this list's factory method) there is no
        // easy way to _not_ force, this isn't working
        // but then ... it's only the very first time around.
        // Safe enough to wait for complaints ;-)
        boolean forceFire = (delegatingRenderer != null) ;
        // JW: Pending - probably fires propertyChangeEvent with wrong newValue?
        // how about fixedCellWidths?
        // need to test!!
        getDelegatingRenderer().setDelegateRenderer(renderer);
        getStringValueRegistry().setStringValue(
                renderer instanceof StringValue ? (StringValue) renderer: null,
                        0);
        super.setCellRenderer(delegatingRenderer);
        if (forceFire)
           firePropertyChange("cellRenderer", null, delegatingRenderer);
    }

    /**
     * A decorator for the original ListCellRenderer. Needed to hook highlighters
     * after messaging the delegate.<p>
     *
     * PENDING JW: formally implement UIDependent?
     */
    public class DelegatingRenderer implements ListCellRenderer, RolloverRenderer {
        /** the delegate. */
        private ListCellRenderer delegateRenderer;

        /**
         * Instantiates a DelegatingRenderer with list's default renderer as delegate.
         */
        public DelegatingRenderer() {
            this(null);
        }

        /**
         * Instantiates a DelegatingRenderer with the given delegate. If the
         * delegate is null, the default is created via the list's factory method.
         *
         * @param delegate the delegate to use, if null the list's default is
         *   created and used.
         */
        public DelegatingRenderer(ListCellRenderer delegate) {
            setDelegateRenderer(delegate);
        }

        /**
         * Sets the delegate. If the
         * delegate is null, the default is created via the list's factory method.
         *
         * @param delegate the delegate to use, if null the list's default is
         *   created and used.
         */
        public void setDelegateRenderer(ListCellRenderer delegate) {
            if (delegate == null) {
                delegate = createDefaultCellRenderer();
            }
            delegateRenderer = delegate;
        }

        /**
         * Returns the delegate.
         *
         * @return the delegate renderer used by this renderer, guaranteed to
         *   not-null.
         */
        public ListCellRenderer getDelegateRenderer() {
            return delegateRenderer;
        }

        /**
         * Updates the ui of the delegate.
         */
         public void updateUI() {
             updateRendererUI(delegateRenderer);
         }

         /**
          *
          * @param renderer the renderer to update the ui of.
          */
         private void updateRendererUI(ListCellRenderer renderer) {
             if (renderer == null) return;
             Component comp = null;
             if (renderer instanceof AbstractRenderer) {
                 comp = ((AbstractRenderer) renderer).getComponentProvider().getRendererComponent(null);
             } else if (renderer instanceof Component) {
                 comp = (Component) renderer;
             } else {
                 try {
                     comp = renderer.getListCellRendererComponent(
                             JXList.this, null, -1, false, false);
                } catch (Exception e) {
                    // nothing to do - renderer barked on off-range row
                }
             }
             if (comp != null) {
                 SwingUtilities.updateComponentTreeUI(comp);
             }

         }

         // --------- implement ListCellRenderer
        /**
         * {@inheritDoc} <p>
         *
         * Overridden to apply the highlighters, if any, after calling the delegate.
         * The decorators are not applied if the row is invalid.
         */
       @Override
    public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            Component comp = delegateRenderer.getListCellRendererComponent(list, value, index,
                    isSelected, cellHasFocus);
            if ((compoundHighlighter != null) && (index >= 0) && (index < getElementCount())) {
                comp = compoundHighlighter.highlight(comp, getComponentAdapter(index));
            }
            return comp;
        }

        // implement RolloverRenderer

        /**
         * {@inheritDoc}
         *
         */
        @Override
        public boolean isEnabled() {
            return (delegateRenderer instanceof RolloverRenderer) &&
               ((RolloverRenderer) delegateRenderer).isEnabled();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void doClick() {
            if (isEnabled()) {
                ((RolloverRenderer) delegateRenderer).doClick();
            }
        }

    }

    /**
     * Invalidates cell size caching in the ui delegate. May do nothing if there's no
     * safe (i.e. without reflection) way to message the delegate. <p>
     *
     * This implementation calls the corresponding method on BasicXListUI if available,
     * does nothing otherwise.
     *
     */
    public void invalidateCellSizeCache() {
        if (getUI() instanceof BasicXListUI) {
            ((BasicXListUI) getUI()).invalidateCellSizeCache();
        }
    }

    // --------------------------- updateUI

    /**
     * {@inheritDoc} <p>
     *
     * Overridden to update renderer and Highlighters.
     */
    @Override
    public void updateUI() {
        // PENDING JW: temporary during dev to quickly switch between default and custom ui
        if (getUIClassID() == super.getUIClassID()) {
            super.updateUI();
        } else {
            setUI((ListUI) LookAndFeelAddons.getUI(this, ListUI.class));
        }
        updateRendererUI();
        updateHighlighterUI();
    }

    @Override
    public String getUIClassID() {
        // PENDING JW: temporary during dev to quickly switch between default and custom ui
//        return super.getUIClassID();
        return uiClassID;
    }

    private void updateRendererUI() {
        if (delegatingRenderer != null) {
            delegatingRenderer.updateUI();
        } else {
            ListCellRenderer renderer = getCellRenderer();
            if (renderer instanceof Component) {
                SwingUtilities.updateComponentTreeUI((Component) renderer);
            }
        }
    }

    /**
     * Updates highlighter after <code>updateUI</code> changes.
     *
     * @see org.jdesktop.swingx.plaf.UIDependent
     */
    protected void updateHighlighterUI() {
        if (compoundHighlighter == null) return;
        compoundHighlighter.updateUI();
    }

}
