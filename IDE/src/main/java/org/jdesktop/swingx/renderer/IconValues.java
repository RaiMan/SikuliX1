/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.renderer;

import java.io.File;

import javax.swing.Icon;
import javax.swing.filechooser.FileSystemView;

/**
 * A collection of common {@code IconValue} implementations.
 *
 * @author Karl George Schaefer
 * @author Jeanette Winzenburg
 */
public final class IconValues {
    /**
     * Always NULL_ICON. This is useful to indicate that we really want
     * no icon instead of f.i. a default provided by the CellContext.
     */
    @SuppressWarnings("serial")
    public static final IconValue NONE = new IconValue() {

        @Override
        public Icon getIcon(Object value) {
            return IconValue.NULL_ICON;
        }

    };

    /**
     * Returns the value as Icon if possible or null.
     */
    @SuppressWarnings("serial")
    public static final IconValue ICON = new IconValue() {

        @Override
        public Icon getIcon(Object value) {
            if (value instanceof Icon) {
                return (Icon) value;
            }
            return null;
        }
    };

    /**
     * An {@code IconValue} that presents the current L&F icon for a given file.
     * If the value passed to {@code FILE_ICON} is not a {@link File}, this has
     * the same effect as {@link IconValues#NONE}.
     */
    @SuppressWarnings("serial")
    public static final IconValue FILE_ICON = new IconValue() {
        @Override
        public Icon getIcon(Object value) {
            if (value instanceof File) {
                FileSystemView fsv = FileSystemView.getFileSystemView();

                return fsv.getSystemIcon((File) value);
            }

            return IconValues.NONE.getIcon(value);
        }
    };

    private IconValues() {
        // does nothing
    }
}
