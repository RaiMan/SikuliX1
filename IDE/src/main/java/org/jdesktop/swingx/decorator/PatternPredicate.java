/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.decorator;

import java.awt.Component;
import java.util.regex.Pattern;

/**
 * Pattern based HighlightPredicate. <p>
 *
 * Turns on the highlight of a single or all columns of the current row if
 * a match of the String representation of cell content against the given Pattern
 * is found.<p>
 *
 * The match logic can be configured to either test
 * one specific column in the current row or all columns. In the latter case
 * the logic is the same as in RowFilters.GeneralFilter: the row is included
 * if any of the cell contents in the row are matches. <p>
 *
 *
 * @author Jeanette Winzenburg
 */
public class PatternPredicate implements HighlightPredicate {
    public static final int ALL = -1;

    private int highlightColumn;
    private int testColumn;
    private Pattern pattern;

    /**
     * Instantiates a Predicate with the given Pattern and testColumn index
     * (in model coordinates) highlighting all columns.
     *  A column index of -1 is interpreted
     * as "all".
     *
     * @param pattern the Pattern to test the cell value against
     * @param testColumn the column index in model coordinates
     *   of the cell which contains the value to test against the pattern
     */
    public PatternPredicate(Pattern pattern, int testColumn) {
        this(pattern, testColumn, ALL);
    }

    /**
     * Instantiates a Predicate with the given Pattern testing against
     * all columns and highlighting all columns.
     *
     * @param pattern the Pattern to test the cell value against
     */
    public PatternPredicate(Pattern pattern) {
        this(pattern, ALL, ALL);
    }

    /**
     * Instantiates a Predicate with the given Pattern and test-/decorate
     * column index in model coordinates. A column index of -1 is interpreted
     * as "all".
     *
     *
     * @param pattern the Pattern to test the cell value against
     * @param testColumn the column index in model coordinates
     *   of the cell which contains the value
     *   to test against the pattern
     * @param decorateColumn the column index in model coordinates
     *   of the cell which should be
     *   decorated if the test against the value succeeds.
     */
    public PatternPredicate(Pattern pattern, int testColumn, int decorateColumn) {
        this.pattern = pattern;
        this.testColumn = testColumn;
        this.highlightColumn = decorateColumn;
    }

    /**
     * Instantiates a Predicate with the given Pattern testing against
     * all columns and highlighting all columns.
     *
     * @param pattern the Pattern to test the cell value against
     */
    public PatternPredicate(String pattern) {
        this(pattern, ALL, ALL);
    }

    /**
     * Instantiates a Predicate with the given regex and test
     * column index in model coordinates. The pattern string is compiled to a
     * Pattern with flags 0. A column index of -1 is interpreted
     * as "all".
     *
     * @param regex the regex string to test the cell value against
     * @param testColumn the column index in model coordinates
     *   of the cell which contains the value
     *   to test against the pattern
     */
    public PatternPredicate(String regex, int testColumn) {
        this(regex, testColumn, ALL);
    }

    /**
     * Instantiates a Predicate with the given regex and test-/decorate
     * column index in model coordinates. The pattern string is compiled to a
     * Pattern with flags 0. A column index of -1 is interpreted
     * as "all".
     *
     * @param regex the regex string to test the cell value against
     * @param testColumn the column index in model coordinates
     *   of the cell which contains the value
     *   to test against the pattern
     * @param decorateColumn the column index in model coordinates
     *   of the cell which should be
     *   decorated if the test against the value succeeds.
     */
    public PatternPredicate(String regex, int testColumn, int decorateColumn) {
        this(Pattern.compile(regex), testColumn, decorateColumn);
    }

    /**
     *
     * @inherited <p>
     *
     * Implemented to return true if the match of cell content's String representation
     * against the Pattern if found and the adapter's view column maps to the
     * decorateColumn/s. Otherwise returns false.
     *
     */
    @Override
    public boolean isHighlighted(Component renderer, ComponentAdapter adapter) {
        if (isHighlightCandidate(adapter)) {
            return test(adapter);
        }
        return false;
    }

    /**
     * Test the value. This is called only if the
     * pre-check returned true, because accessing the
     * value might be potentially costly
     * @param adapter
     * @return
     */
    private boolean test(ComponentAdapter adapter) {
        // single test column
        if (testColumn >= 0) return testColumn(adapter, testColumn);
        // test all
        for (int column = 0; column < adapter.getColumnCount(); column++) {
            boolean result = testColumn(adapter, column);
            if (result) return true;
        }
        return false;
    }

    /**
     * @param adapter
     * @param testColumn
     * @return
     */
    private boolean testColumn(ComponentAdapter adapter, int testColumn) {
        if (!adapter.isTestable(testColumn))
            return false;
        String value = adapter.getString(testColumn);

        if ((value == null) || (value.length() == 0)) {
            return false;
        }
        return pattern.matcher(value).find();
    }

    /**
     * A quick pre-check.
     * @param adapter
     *
     * @return
     */
    private boolean isHighlightCandidate(ComponentAdapter adapter) {
        return (pattern != null) &&
            ((highlightColumn < 0) ||
               (highlightColumn == adapter.convertColumnIndexToModel(adapter.column)));
    }

    /**
     *
     * @return returns the column index to decorate (in model coordinates)
     */
    public int getHighlightColumn() {
        return highlightColumn;
    }

    /**
     *
     * @return returns the Pattern to test the cell value against
     */
    public Pattern getPattern() {
        return pattern;
    }

    /**
     *
     * @return the column to use for testing (in model coordinates)
     */
    public int getTestColumn() {
        return testColumn;
    }

}
