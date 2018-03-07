/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.event;

import java.beans.PropertyChangeEvent;

import javax.swing.event.TableColumnModelListener;

/**
 * Extended <code>TableColumnModelListener</code> which is interested
 * in property changes of contained <code>TableColumn</code>s. <p>
 *
 * Enhanced <code>TableColumnModelExt</code> guarantees to notify
 * these extended column listeners. An example of a client which
 * adjusts itself based on <code>headerValue</code> property of visible columns:
 * <pre><code>
 * TableColumnModelExtListener l = new TableColumnModelExtListener() {
 *
 *     public void columnPropertyChange(PropertyChangeEvent event) {
 *         if (&quot;headerValue&quot;.equals(event.getPropertyName())) {
 *             TableColumn column = (TableColumn) event.getSource();
 *             if ((column instanceof TableColumnExt)
 *                     &amp;&amp; !((TableColumnExt) column).isVisible()) {
 *                 return;
 *             }
 *             resizeAndRepaint();
 *         }
 *     }
 *
 *     public void columnAdded(TableColumnModelEvent e) {
 *     }
 *
 *     public void columnMarginChanged(ChangeEvent e) {
 *     }
 *
 *     public void columnMoved(TableColumnModelEvent e) {
 *     }
 *
 *     public void columnRemoved(TableColumnModelEvent e) {
 *     }
 *
 *     public void columnSelectionChanged(ListSelectionEvent e) {
 *     }
 *
 * };
 * columnModel.addColumnModelListener(l);
 * </code></pre>
 *
 * @author Jeanette Winzenburg
 * @see org.jdesktop.swingx.table.TableColumnModelExt
 */
public interface TableColumnModelExtListener extends TableColumnModelListener {

    /**
     * Notifies listeners about property changes of contained columns.
     * The event is the original as fired from the <code>TableColumn</code>.
     * @param event a <code>PropertyChangeEvent</code> fired by a <code>TableColumn</code>
     *   contained in a <code>TableColumnModel</code>
     */
    void columnPropertyChange(PropertyChangeEvent event);
}
