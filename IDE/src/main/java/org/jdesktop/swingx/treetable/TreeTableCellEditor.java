/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.jdesktop.swingx.treetable;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import java.util.logging.Logger;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.tree.TreeCellRenderer;

import org.jdesktop.swingx.JXTable.GenericEditor;

/**
 * An editor that can be used to edit the tree column. This extends
 * DefaultCellEditor and uses a JTextField (actually, TreeTableTextField)
 * to perform the actual editing.
 * <p>To support editing of the tree column we can not make the tree
 * editable. The reason this doesn't work is that you can not use
 * the same component for editing and rendering. The table may have
 * the need to paint cells, while a cell is being edited. If the same
 * component were used for the rendering and editing the component would
 * be moved around, and the contents would change. When editing, this
 * is undesirable, the contents of the text field must stay the same,
 * including the caret blinking, and selections persisting. For this
 * reason the editing is done via a TableCellEditor.
 * <p>Another interesting thing to be aware of is how tree positions
 * its render and editor. The render/editor is responsible for drawing the
 * icon indicating the type of node (leaf, branch...). The tree is
 * responsible for drawing any other indicators, perhaps an additional
 * +/- sign, or lines connecting the various nodes. So, the renderer
 * is positioned based on depth. On the other hand, table always makes
 * its editor fill the contents of the cell. To get the allusion
 * that the table cell editor is part of the tree, we don't want the
 * table cell editor to fill the cell bounds. We want it to be placed
 * in the same manner as tree places it editor, and have table message
 * the tree to paint any decorations the tree wants. Then, we would
 * only have to worry about the editing part. The approach taken
 * here is to determine where tree would place the editor, and to override
 * the <code>reshape</code> method in the JTextField component to
 * nudge the textfield to the location tree would place it. Since
 * JXTreeTable will paint the tree behind the editor everything should
 * just work. So, that is what we are doing here. Determining of
 * the icon position will only work if the TreeCellRenderer is
 * an instance of DefaultTreeCellRenderer. If you need custom
 * TreeCellRenderers, that don't descend from DefaultTreeCellRenderer,
 * and you want to support editing in JXTreeTable, you will have
 * to do something similar.
 *
 * @author Scott Violet
 * @author Ramesh Gupta
 */
public class TreeTableCellEditor extends GenericEditor {
    //DefaultCellEditor {
// JW: changed to extends GenericEditor to fix #1365-swingx -
//    borders different in hierarchical column vs. table column
//
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TreeTableCellEditor.class.getName());

    public TreeTableCellEditor(JTree tree) {
        super(new TreeTableTextField());
        if (tree == null) {
            throw new IllegalArgumentException("null tree");
        }
        // JW: no need to...
        this.tree = tree; // immutable
    }

    /**
     * Overriden to determine an offset that tree would place the editor at. The
     * offset is determined from the <code>getRowBounds</code> JTree method,
     * and additionaly from the icon DefaultTreeCellRenderer will use.
     * <p>
     * The offset is then set on the TreeTableTextField component created in the
     * constructor, and returned.
     */
    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
            boolean isSelected, int row, int column) {
        Component component = super.getTableCellEditorComponent(table, value,
                isSelected, row, column);
        // JW: this implementation is not bidi-compliant, need to do better
        initEditorOffset(table, row, column, isSelected);
        return component;
    }

    /**
     * @param row
     * @param isSelected
     */
    protected void initEditorOffset(JTable table, int row, int column,
            boolean isSelected) {
        if (tree == null)
            return;
//        Rectangle bounds = tree.getRowBounds(row);
//        int offset = bounds.x;
        Object node = tree.getPathForRow(row).getLastPathComponent();
        boolean leaf = tree.getModel().isLeaf(node);
        boolean expanded = tree.isExpanded(row);
        TreeCellRenderer tcr = tree.getCellRenderer();
        Component editorComponent = tcr.getTreeCellRendererComponent(tree, node,
                isSelected, expanded, leaf, row, false);

        ((TreeTableTextField) getComponent()).init(row,
                column, table, tree, editorComponent);
    }

    /**
     * This is overriden to forward the event to the tree. This will
     * return true if the click count >= clickCountToStart, or the event is null.
     */
    @Override
    public boolean isCellEditable(EventObject e) {
        // JW: quick fix for #592-swingx -
        // editing not started on keyEvent in hierarchical column (1.6)
        if (e instanceof MouseEvent) {
          return (((MouseEvent) e).getClickCount() >= clickCountToStart);
        }
        return true;
    }

    /**
     * Component used by TreeTableCellEditor. The only thing this does
     * is to override the <code>reshape</code> method, and to ALWAYS
     * make the x location be <code>offset</code>.
     */
    static class TreeTableTextField extends JTextField {
        private int iconWidth;

        void init(int row, int column, JTable table, JTree tree, Component editorComponent) {
            this.column = column;
            this.row = row;
            this.table = table;
            this.tree = tree;
            updateIconWidth(editorComponent);
            setComponentOrientation(table.getComponentOrientation());
        }

        /**
         * @param treeComponent
         */
        private void updateIconWidth(Component treeComponent) {
            iconWidth = 0;
            if (!(treeComponent instanceof JLabel)) return;
            Icon icon = ((JLabel) treeComponent).getIcon();
            if (icon != null) {
                iconWidth = icon.getIconWidth() + ((JLabel) treeComponent).getIconTextGap();
            }

        }

        private int column;
        private int row;
        private JTable table;
        private JTree tree;

        /**
         * {@inheritDoc} <p>
         *
         * Overridden to place the textfield in the node content boundaries,
         * leaving the icon to the renderer. <p>
         *
         * PENDING JW: insets?
         *
         */
        @SuppressWarnings("deprecation")
        @Override
        public void reshape(int x, int y, int width, int height) {
            // Allows precise positioning of text field in the tree cell.
            // following three lines didn't work out
            //Border border = this.getBorder(); // get this text field's border
            //Insets insets = border == null ? null : border.getBorderInsets(this);
            //int newOffset = offset - (insets == null ? 0 : insets.left);

            Rectangle cellRect = table.getCellRect(0, column, false);
            Rectangle nodeRect = tree.getRowBounds(row);
            nodeRect.width -= iconWidth;
            if(table.getComponentOrientation().isLeftToRight()) {
                int nodeStart = cellRect.x + nodeRect.x + iconWidth;
                int nodeEnd = cellRect.x + cellRect.width;
                super.reshape(nodeStart, y, nodeEnd - nodeStart, height);
//                int newOffset = nodeLeftX - getInsets().left;
//                super.reshape(x + newOffset, y, width - newOffset, height);
            } else {
                int nodeRightX = nodeRect.x + nodeRect.width;
                nodeRect.x = 0; //Math.max(0, nodeRect.x);
                // ignore the parameter
                width = nodeRightX - nodeRect.x;
                super.reshape(cellRect.x + nodeRect.x, y, width, height);
            }
        }

    }

    private final JTree tree; // immutable
}
