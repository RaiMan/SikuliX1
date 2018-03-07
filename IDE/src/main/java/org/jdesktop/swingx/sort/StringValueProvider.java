/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.sort;

import org.jdesktop.swingx.renderer.StringValue;

/**
 * Read-only repository for StringValues. This is meant to be shared by collection views
 * (in rendering a cell) and RowSorters/SortControllers/ComponentAdapters. <p>
 *
 * Note: this is work-in-progress, related to re-enable WYSIWYM in sorting/filtering.
 * It's location and api is expected to change.
 *
 * @author Jeanette Winzenburg
 */
public interface StringValueProvider {

    /**
     * Returns a StringValue to use for conversion of the cell content at row and column.
     * The converter is guaranteed to be not null, so implemenations are responsible for
     * a reasonable fall-back value always, f.i. if they have no converters registered of
     * if any or both of the row/column coordinate is "invalid" (f.i. -1) <p>
     *
     * @param row the row of the cell in model coordinates
     * @param column the column of the cell in model coordinates
     *
     * @return a StringValue to use for conversion, guaranteed to not null.
     */
    StringValue getStringValue(int row, int column);

}
