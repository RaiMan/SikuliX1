/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.decorator;

import java.awt.Component;
import java.util.regex.Pattern;

/**
 * Pattern based HighlightPredicate for searching. Highlights
 * the current adapter cell if the value matches the pattern.
 * The highlight scope can be limited to a certain column and
 * row. <p>
 *
 * Note: this differs from PatternPredicate in that it is focused
 * on the current cell (highlight coordinates == test coordinates)
 * while the PatternPredicate can have separate test and highlight
 * coordinates. <p>
 *
 *
 * @author Jeanette Winzenburg
 */
public class SearchPredicate implements HighlightPredicate {
    public static final int ALL = -1;
    public static final String MATCH_ALL = ".*";
    private int highlightColumn;
    private int highlightRow; // in view coordinates?
    private Pattern pattern;

    /**
     * Instantiates a Predicate with the given Pattern.
     * All matching cells are highlighted.
     *
     *
     * @param pattern the Pattern to test the cell value against
     */
    public SearchPredicate(Pattern pattern) {
        this(pattern, ALL, ALL);
    }

    /**
     * Instantiates a Predicate with the given Pattern. Highlighting
     * is limited to matching cells in the given column.
     *
     * @param pattern the Pattern to test the cell value against
     * @param column the column to limit the highlight to
     */
    public SearchPredicate(Pattern pattern, int column) {
        this(pattern, ALL, column);
    }

    /**
     * Instantiates a Predicate with the given Pattern. Highlighting
     * is limited to matching cells in the given column and row. A
     * value of -1 indicates all rows/columns. <p>
     *
     * Note: the coordinates are asymmetric - rows are in view- and
     * column in model-coordinates - due to corresponding methods in
     * ComponentAdapter. Hmm... no need to? This happens on the
     * current adapter state which is view always, so could use view
     * only?
     *
     * @param pattern the Pattern to test the cell value against
     * @param row the row index in view coordinates to limit the
     *    highlight.
     * @param column the column in model coordinates
     *    to limit the highlight to
     */
    public SearchPredicate(Pattern pattern, int row, int column) {
        this.pattern = pattern;
        this.highlightColumn = column;
        this.highlightRow = row;
    }

    /**
     * Instantiates a Predicate with a Pattern compiled from the given
     * regular expression.
     * All matching cells are highlighted.
     *
     * @param regex the regular expression to test the cell value against
     */
    public SearchPredicate(String regex) {
        this(regex, ALL, ALL);

    }

    /**
     * Instantiates a Predicate with a Pattern compiled from the given
     * regular expression. Highlighting
     * is applied to matching cells in all rows, but only in the given column. A
     * value of ALL indicates all columns. <p>
     *
     * @param regex the regular expression to test the cell value against
     * @param column the column index in model coordinates to limit the highlight to
     */
    public SearchPredicate(String regex, int column) {
        this(regex, ALL, column);
    }

    /**
     * Instantiates a Predicate with a Pattern compiled from the given
     * regular expression. Highlighting
     * is limited to matching cells in the given column and row. A
     * value of ALL indicates all rows/columns. <p>
     *
     * Note: the coordinates are asymmetric - rows are in view- and
     * column in model-coordinates - due to corresponding methods in
     * ComponentAdapter. Hmm... no need to? This happens on the
     * current adapter state which is view always, so could use view
     * only?
     *
     * @param regex the Pattern to test the cell value against
     * @param row the row index in view coordinates to limit the
     *    highlight.
     * @param column the column in model coordinates
     *    to limit the highlight to
     */
    public SearchPredicate(String regex, int row, int column) {
        // test against empty string
        this((regex != null) && (regex.length() > 0) ?
                Pattern.compile(regex) : null, row, column);
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
     * @return returns the column index to decorate (in model coordinates)
     */
    public int getHighlightRow() {
        return highlightRow;
    }

    /**
     *
     * @return returns the Pattern to test the cell value against
     */
    public Pattern getPattern() {
        return pattern;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isHighlighted(Component renderer, ComponentAdapter adapter) {
        if (isHighlightCandidate(renderer, adapter)) {
            return test(renderer, adapter);
        }
        return false;
    }

    /**
     * Test the value. This is called only if the
     * pre-check returned true, because accessing the
     * value might be potentially costly
     * @param renderer
     * @param adapter
     * @return
     */
    private boolean test(Component renderer, ComponentAdapter adapter) {
        // PENDING JW: why convert here? we are focused on the adapter's cell
        // looks like an oversight as of ol' days ;-)
         int  columnToTest = adapter.convertColumnIndexToModel(adapter.column);
         String value = adapter.getString(columnToTest);

         if ((value == null) || (value.length() == 0)) {
             return false;
         }
         return pattern.matcher(value).find();
     }

    /**
     * A quick pre-check.
     *
     * @param renderer
     * @param adapter
     * @return
     */
    private boolean isHighlightCandidate(Component renderer, ComponentAdapter adapter) {
        if (!isEnabled()) return false;
        if (highlightRow >= 0 && (adapter.row != highlightRow)) {
            return false;
        }
        return
            ((highlightColumn < 0) ||
               (highlightColumn == adapter.convertColumnIndexToModel(adapter.column)));
    }

    private boolean isEnabled() {
        Pattern pattern = getPattern();
        if (pattern == null || MATCH_ALL.equals(pattern.pattern())) {
            return false;
        }
        return true;
    }

}
