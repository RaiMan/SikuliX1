/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

/*
 * Created on 11.01.2011
 *
 */
package org.jdesktop.swingx.treetable;

/**
 * Interface which guarantees access to a TreeTableModel. It is implemented by
 * the internal TreeTableModelAdapter of JXTreeTable to allow direct access to
 * the underlying TreeTableModel from the adapter.
 * <p>
 *
 * That's useful f.i. when trying to configure TableColumnExt in a
 * ColumnFactory, like in
 *
 * <pre>
 * <code>
 * JXTreeTable table = new JXTreeTable();
 * ColumnFactory factory = new ColumnFactory() {
 *
 *     @Override
 *     public void configureTableColumn(TableModel model,
 *             TableColumnExt columnExt) {
 *         super.configureTableColumn(model, columnExt);
 *         if (model instanceof TreeTableModelProvider) {
 *             TreeTableModel treeTableModel = ((TreeTableModelProvider) model).getTreeTableModel();
 *             if (treeTableModel.getHierarchicalColumn() == columnExt.getModelIndex()) {
 *                 columnExt.setTitle("Hierarchical: " + columnExt.getTitle());
 *             }
 *         }
 *     }
 * };
 * table.setColumnFactory(factory);
 * table.setTreeTableModel(new FileSystemModel());
 *
 * </code>
 * </pre>
 *
 * @author Jeanette Winzenburg, Berlin
 */
public interface TreeTableModelProvider {

    /**
     * Returns a TreeTableModel, guaranteed to be not null.
     *
     * @return a TreeTableModel, guaranteed to be not null.
     */
    TreeTableModel getTreeTableModel();

}
