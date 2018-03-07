/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.tree;

import javax.swing.UIManager;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.jdesktop.swingx.SwingXUtilities;

/**
 * Quick fix for #1061-swingx (which actually is a core issue):
 * tree icons lost on toggle laf. Updates colors as well -
 * but beware: this is incomplete as some of super's properties are private!
 *
 * Will not do more because in the longer run (as soon as we've fixed the editor issues)
 * the JXTree's default renderer will be changed to SwingX DefaultTreeRenderer.
 *
 * @author Jeanette Winzenburg
 */
public class DefaultXTreeCellRenderer extends DefaultTreeCellRenderer {

    /**
     * {@inheritDoc} <p>
     *
     * Overridden to update icons and colors.
     */
    @Override
    public void updateUI() {
        super.updateUI();
        updateIcons();
        updateColors();
    }

    /**
     *
     */
    protected void updateColors() {
        if (SwingXUtilities.isUIInstallable(getTextSelectionColor())) {
            setTextSelectionColor(UIManager.getColor("Tree.selectionForeground"));
        }
        if (SwingXUtilities.isUIInstallable(getTextNonSelectionColor())) {
            setTextNonSelectionColor(UIManager.getColor("Tree.textForeground"));
        }
        if (SwingXUtilities.isUIInstallable(getBackgroundSelectionColor())) {
            setBackgroundSelectionColor(UIManager.getColor("Tree.selectionBackground"));
        }
        if (SwingXUtilities.isUIInstallable(getBackgroundNonSelectionColor())) {
            setBackgroundNonSelectionColor(UIManager.getColor("Tree.textBackground"));
        }
        if (SwingXUtilities.isUIInstallable(getBorderSelectionColor())) {
            setBorderSelectionColor(UIManager.getColor("Tree.selectionBorderColor"));
        }
//        Object value = UIManager.get("Tree.drawsFocusBorderAroundIcon");
//        drawsFocusBorderAroundIcon = (value != null && ((Boolean)value).
//                                      booleanValue());
//        value = UIManager.get("Tree.drawDashedFocusIndicator");
//        drawDashedFocusIndicator = (value != null && ((Boolean)value).
//                                    booleanValue());
    }

    /**
     *
     */
    protected void updateIcons() {
        if (SwingXUtilities.isUIInstallable(getLeafIcon())) {
            setLeafIcon(UIManager.getIcon("Tree.leafIcon"));
        }
        if (SwingXUtilities.isUIInstallable(getClosedIcon())) {
            setClosedIcon(UIManager.getIcon("Tree.closedIcon"));
        }
        if (SwingXUtilities.isUIInstallable(getOpenIcon())) {
            setOpenIcon(UIManager.getIcon("Tree.openIcon"));
        }

    }


}
