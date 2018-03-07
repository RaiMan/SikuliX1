/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.plaf.basic.core;

import javax.swing.DefaultListSelectionModel;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;

import org.jdesktop.swingx.JXList;
import org.jdesktop.swingx.SwingXUtilities;
import org.jdesktop.swingx.util.Contract;

/**
 * ListSortUI provides support for managing the synchronization between
 * RowSorter, SelectionModel and ListModel if a JXList is sortable.<p>
 *
 * This implementation is an adaption of JTable.SortManager fit to the
 * needs of a ListUI. In contrast to JTable tradition, the ui delegate has
 * full control about listening to model/selection changes and updating
 * the list accordingly. So the role of this class is that of a helper to the ListUI
 * (vs. as a helper of the JTable).
 * <p>
 * It's up to the ListUI to
 * listen to model/selection and propagate the notification to this class if
 * a sorter is installed, but still do the usual updates (layout, repaint) itself.
 * On the other hand, listening to the sorter and updating list state accordingly
 * is completely done by this.
 *
 */
public final class ListSortUI {
    private RowSorter<? extends ListModel> sorter;
    private JXList list;

    // Selection, in terms of the model. This is lazily created
    // as needed.
    private ListSelectionModel modelSelection;
    private int modelLeadIndex;
    // Set to true while in the process of changing the selection.
    // If this is true the selection change is ignored.
    private boolean syncingSelection;
    // Temporary cache of selection, in terms of model. This is only used
    // if we don't need the full weight of modelSelection.
    private int[] lastModelSelection;
    private boolean sorterChanged;
    private boolean ignoreSortChange;
    private RowSorterListener sorterListener;

    /**
     * Intanstiates a SortUI on the list which has the given RowSorter.
     *
     * @param list the list to control, must not be null
     * @param sorter the rowSorter of the list, must not be null
     * @throws NullPointerException if either the list or the sorter is null
     * @throws IllegalStateException if the sorter is not the sorter installed
     *   on the list
     */
    public ListSortUI(JXList list, RowSorter<? extends ListModel> sorter) {
        this.sorter = Contract.asNotNull(sorter, "RowSorter must not be null");
        this.list = Contract.asNotNull(list, "list must not be null");
        if (sorter != list.getRowSorter()) throw
            new IllegalStateException("sorter must be same as the one on list");
        sorterListener = createRowSorterListener();
        sorter.addRowSorterListener(sorterListener);
    }

    /**
     * Disposes any resources used by this SortManager.
     * Note: this instance must not be used after dispose!
     */
    public void dispose() {
        if (sorter != null) {
            sorter.removeRowSorterListener(sorterListener);
        }
        sorter = null;
        list = null;
    }

//----------------------methods called by listeners

    /**
     * Called after notification from ListModel.
     * @param e the change event from the listModel.
     */
    public void modelChanged(ListDataEvent e) {
        ModelChange change = new ModelChange(e);
        prepareForChange(change);
        notifySorter(change);
        if (change.type != ListDataEvent.CONTENTS_CHANGED) {
            // If the Sorter is unsorted we will not have received
            // notification, force treating insert/delete as a change.
            sorterChanged = true;
        }
        processChange(change);
    }

    /**
     * Called after notification from selectionModel.
     *
     * Invoked when the selection, on the view, has changed.
     */
    public void viewSelectionChanged(ListSelectionEvent e) {
        if (!syncingSelection && modelSelection != null) {
            modelSelection = null;
        }
    }

    /**
     * Called after notification from RowSorter.
     *
     * @param e RowSorter event of type SORTED.
     */
    protected void sortedChanged(RowSorterEvent e) {
        sorterChanged = true;
        if (!ignoreSortChange) {
            prepareForChange(e);
            processChange(null);
            // PENDING Jw: this is fix of 1161-swingx - not updated after setting
            // rowFilter
            // potentially costly? but how to distinguish a mere sort from a
            // filterchanged? (only the latter requires a revalidate)
            // first fix had only revalidate/repaint but was not
            // good enough, see #1261-swingx - no items visible
            // after setting rowFilter
            // need to invalidate the cell size cache which might be needed
            // even after plain sorting as the indi-sizes are now at different
            // positions
            list.invalidateCellSizeCache();
        }
    }

//--------------------- prepare change, that is cache selection if needed
    /**
     * Invoked when the RowSorter has changed.
     * Updates the internal cache of the selection based on the change.
     *
     * @param sortEvent the notification
     * @throws NullPointerException if the given event is null.
     */
    private void prepareForChange(RowSorterEvent sortEvent) {
        Contract.asNotNull(sortEvent, "sorter event not null");
        // sort order changed. If modelSelection is null and filtering
        // is enabled we need to cache the selection in terms of the
        // underlying model, this will allow us to correctly restore
        // the selection even if rows are filtered out.
        if (modelSelection == null &&
                sorter.getViewRowCount() != sorter.getModelRowCount()) {
            modelSelection = new DefaultListSelectionModel();
            ListSelectionModel viewSelection = getViewSelectionModel();
            int min = viewSelection.getMinSelectionIndex();
            int max = viewSelection.getMaxSelectionIndex();
            int modelIndex;
            for (int viewIndex = min; viewIndex <= max; viewIndex++) {
                if (viewSelection.isSelectedIndex(viewIndex)) {
                    modelIndex = convertRowIndexToModel(
                            sortEvent, viewIndex);
                    if (modelIndex != -1) {
                        modelSelection.addSelectionInterval(
                            modelIndex, modelIndex);
                    }
                }
            }
            modelIndex = convertRowIndexToModel(sortEvent,
                    viewSelection.getLeadSelectionIndex());
            SwingXUtilities.setLeadAnchorWithoutSelection(
                    modelSelection, modelIndex, modelIndex);
        } else if (modelSelection == null) {
            // Sorting changed, haven't cached selection in terms
            // of model and no filtering. Temporarily cache selection.
            cacheModelSelection(sortEvent);
        }
    }
    /**
     * Invoked when the list model has changed. This is invoked prior to
     * notifying the sorter of the change.
     * Updates the internal cache of the selection based on the change.
     *
     * @param change the notification
     * @throws NullPointerException if the given event is null.
     */
    private void prepareForChange(ModelChange change) {
        Contract.asNotNull(change, "table event not null");
        if (change.allRowsChanged) {
            // All the rows have changed, chuck any cached selection.
            modelSelection = null;
        } else if (modelSelection != null) {
            // Table changed, reflect changes in cached selection model.
            switch (change.type) {
            // JW: core incorrectly uses change.endModelIndex!
            // sneaked into here via c&p
            // reported as #1536-swingx
            case ListDataEvent.INTERVAL_REMOVED:
                modelSelection.removeIndexInterval(change.startModelIndex,
                        // Note: api difference between remove vs. insert
                        // nothing do do here!
                        change.endModelIndex);
                break;
            case ListDataEvent.INTERVAL_ADDED:
                modelSelection.insertIndexInterval(change.startModelIndex,
                        // insert is tested
                        change.length, true);
                break;
            default:
                break;
            }
        } else {
            // list changed, but haven't cached rows, temporarily
            // cache them.
            cacheModelSelection(null);
        }
    }

    private void cacheModelSelection(RowSorterEvent sortEvent) {
        lastModelSelection = convertSelectionToModel(sortEvent);
        modelLeadIndex = convertRowIndexToModel(sortEvent,
                    getViewSelectionModel().getLeadSelectionIndex());
    }

//----------------------- process change, that is restore selection if needed
    /**
     * Inovked when either the table has changed or the sorter has changed
     * and after the sorter has been notified. If necessary this will
     * reapply the selection and variable row heights.
     */
    private void processChange(ModelChange change) {
        if (change != null && change.allRowsChanged) {
            allChanged();
            getViewSelectionModel().clearSelection();
        } else if (sorterChanged) {
            restoreSelection(change);
        }
    }

    /**
     * Restores the selection from that in terms of the model.
     */
    private void restoreSelection(ModelChange change) {
        syncingSelection = true;
        if (lastModelSelection != null) {
            restoreSortingSelection(lastModelSelection,
                                    modelLeadIndex, change);
            lastModelSelection = null;
        } else if (modelSelection != null) {
            ListSelectionModel viewSelection = getViewSelectionModel();
            viewSelection.setValueIsAdjusting(true);
            viewSelection.clearSelection();
            int min = modelSelection.getMinSelectionIndex();
            int max = modelSelection.getMaxSelectionIndex();
            int viewIndex;
            for (int modelIndex = min; modelIndex <= max; modelIndex++) {
                if (modelSelection.isSelectedIndex(modelIndex)) {
                    viewIndex = sorter.convertRowIndexToView(modelIndex);
                    if (viewIndex != -1) {
                        viewSelection.addSelectionInterval(viewIndex,
                                                           viewIndex);
                    }
                }
            }
            // Restore the lead
            int viewLeadIndex = modelSelection.getLeadSelectionIndex();
            if (viewLeadIndex != -1) {
                viewLeadIndex = sorter.convertRowIndexToView(viewLeadIndex);
            }
            SwingXUtilities.setLeadAnchorWithoutSelection(
                    viewSelection, viewLeadIndex, viewLeadIndex);
            viewSelection.setValueIsAdjusting(false);
        }
        syncingSelection = false;
    }

    /**
     * Restores the selection after a model event/sort order changes.
     * All coordinates are in terms of the model.
     */
    private void restoreSortingSelection(int[] selection, int lead,
            ModelChange change) {
        // Convert the selection from model to view
        for (int i = selection.length - 1; i >= 0; i--) {
            selection[i] = convertRowIndexToView(change, selection[i]);
        }
        lead = convertRowIndexToView(change, lead);

        // Check for the common case of no change in selection for 1 row
        if (selection.length == 0 ||
            (selection.length == 1 && selection[0] == list.getSelectedIndex())) {
            return;
        }
        ListSelectionModel selectionModel = getViewSelectionModel();
        // And apply the new selection
        selectionModel.setValueIsAdjusting(true);
        selectionModel.clearSelection();
        for (int i = selection.length - 1; i >= 0; i--) {
            if (selection[i] != -1) {
                selectionModel.addSelectionInterval(selection[i],
                                                    selection[i]);
            }
        }
        SwingXUtilities.setLeadAnchorWithoutSelection(
                selectionModel, lead, lead);
        selectionModel.setValueIsAdjusting(false);
    }

//------------------- row index conversion methods
    /**
     * Converts a model index to view index.  This is called when the
     * sorter or model changes and sorting is enabled.
     *
     * @param change describes the TableModelEvent that initiated the change;
     *        will be null if called as the result of a sort
     */
    private int convertRowIndexToView(ModelChange change, int modelIndex) {
        if (modelIndex < 0) {
            return -1;
        }
//        Contract.asNotNull(change, "change must not be null?");
        if (change != null && modelIndex >= change.startModelIndex) {
            if (change.type == ListDataEvent.INTERVAL_ADDED) {
                if (modelIndex + change.length >= change.modelRowCount) {
                    return -1;
                }
                return sorter.convertRowIndexToView(
                        modelIndex + change.length);
            }
            else if (change.type == ListDataEvent.INTERVAL_REMOVED) {
                if (modelIndex <= change.endModelIndex) {
                    // deleted
                    return -1;
                }
                else {
                    if (modelIndex - change.length >= change.modelRowCount) {
                        return -1;
                    }
                    return sorter.convertRowIndexToView(
                            modelIndex - change.length);
                }
            }
            // else, updated
        }
        if (modelIndex >= sorter.getModelRowCount()) {
            return -1;
        }
        return sorter.convertRowIndexToView(modelIndex);
    }

    private int convertRowIndexToModel(RowSorterEvent e, int viewIndex) {
        // JW: the event is null if the selection is cached in prepareChange
        // after model notification. Then the conversion from the
        // sorter is still valid as the prepare is called before
        // notifying the sorter.
        if (e != null) {
            if (e.getPreviousRowCount() == 0) {
                return viewIndex;
            }
            // range checking handled by RowSorterEvent
            return e.convertPreviousRowIndexToModel(viewIndex);
        }
        // Make sure the viewIndex is valid
        if (viewIndex < 0 || viewIndex >= sorter.getViewRowCount()) {
            return -1;
        }
        return sorter.convertRowIndexToModel(viewIndex);
    }

    /**
     * Converts the selection to model coordinates.  This is used when
     * the model changes or the sorter changes.
     */
    private int[] convertSelectionToModel(RowSorterEvent e) {
        int[] selection = list.getSelectedIndices();
        for (int i = selection.length - 1; i >= 0; i--) {
            selection[i] = convertRowIndexToModel(e, selection[i]);
        }
        return selection;
    }

//------------------
    /**
     * Notifies the sorter of a change in the underlying model.
     */
    private void notifySorter(ModelChange change) {
        try {
            ignoreSortChange = true;
            sorterChanged = false;
            if (change.allRowsChanged) {
                sorter.allRowsChanged();
            } else {
                switch (change.type) {
                case ListDataEvent.CONTENTS_CHANGED:
                    sorter.rowsUpdated(change.startModelIndex,
                            change.endModelIndex);
                    break;
                case ListDataEvent.INTERVAL_ADDED:
                    sorter.rowsInserted(change.startModelIndex,
                            change.endModelIndex);
                    break;
                case ListDataEvent.INTERVAL_REMOVED:
                    sorter.rowsDeleted(change.startModelIndex,
                            change.endModelIndex);
                    break;
                }
            }
        } finally {
            ignoreSortChange = false;
        }
    }

    private ListSelectionModel getViewSelectionModel() {
        return list.getSelectionModel();
    }
    /**
     * Invoked when the underlying model has completely changed.
     */
    private void allChanged() {
        modelLeadIndex = -1;
        modelSelection = null;
    }

//------------------- implementing listeners

    /**
     * Creates and returns a RowSorterListener. This implementation
     * calls sortedChanged if the event is of type SORTED.
     *
     * @return rowSorterListener to install on sorter.
     */
    protected RowSorterListener createRowSorterListener() {
        RowSorterListener l = new RowSorterListener() {

            @Override
            public void sorterChanged(RowSorterEvent e) {
                if (e.getType() == RowSorterEvent.Type.SORTED) {
                    sortedChanged(e);
                }
            }

        };
        return l;
    }
    /**
     * ModelChange is used when sorting to restore state, it corresponds
     * to data from a TableModelEvent.  The values are precalculated as
     * they are used extensively.<p>
     *
     * PENDING JW: this is not yet fully adapted to ListDataEvent.
     */
     final static class ModelChange {
         // JW: if we received a dataChanged, there _is no_ notion
         // of end/start/length of change
        // Starting index of the change, in terms of the model, -1 if dataChanged
        int startModelIndex;

        // Ending index of the change, in terms of the model, -1 if dataChanged
        int endModelIndex;

        // Length of the change (end - start + 1), - 1 if dataChanged
        int length;

        // Type of change
        int type;

        // Number of rows in the model
        int modelRowCount;

        // True if the event indicates all the contents have changed
        boolean allRowsChanged;

        public ModelChange(ListDataEvent e) {
            type = e.getType();
            modelRowCount = ((ListModel) e.getSource()).getSize();
            startModelIndex = e.getIndex0();
            endModelIndex = e.getIndex1();
            allRowsChanged = startModelIndex < 0;
            length = allRowsChanged ? -1 : endModelIndex - startModelIndex + 1;
        }
    }

}
