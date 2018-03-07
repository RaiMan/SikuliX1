/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.search;

import java.awt.Color;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JComponent;

import org.jdesktop.swingx.decorator.AbstractHighlighter;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.SearchPredicate;

/**
 * An abstract implementation of Searchable supporting
 * incremental search.
 *
 * Keeps internal state to represent the previous search result.
 * For all methods taking a String as parameter: compiles the String
 * to a Pattern as-is and routes to the central method taking a Pattern.
 *
 *
 * @author Jeanette Winzenburg
 */
public abstract class AbstractSearchable implements Searchable {

    /**
     * stores the result of the previous search.
     */
    protected final SearchResult lastSearchResult = new SearchResult();

    private AbstractHighlighter matchHighlighter;

    /** key for client property to use SearchHighlighter as match marker. */
    public static final String MATCH_HIGHLIGHTER = "match.highlighter";

    /**
     * Performs a forward search starting at the beginning
     * across the Searchable using String that represents a
     * regex pattern; {@link java.util.regex.Pattern}.
     *
     * @param searchString <code>String</code> that we will try to locate
     * @return the position of the match in appropriate coordinates or -1 if
     *   no match found.
     */
    @Override
    public int search(String searchString) {
        return search(searchString, -1);
    }

    /**
     * Performs a forward search starting at the given startIndex
     * using String that represents a regex
     * pattern; {@link java.util.regex.Pattern}.
     *
     * @param searchString <code>String</code> that we will try to locate
     * @param startIndex position in the document in the appropriate coordinates
     * from which we will start search or -1 to start from the beginning
     * @return the position of the match in appropriate coordinates or -1 if
     *   no match found.
     */
    @Override
    public int search(String searchString, int startIndex) {
        return search(searchString, startIndex, false);
    }

    /**
     * Performs a  search starting at the given startIndex
     * using String that represents a regex
     * pattern; {@link java.util.regex.Pattern}. The search direction
     * depends on the boolean parameter: forward/backward if false/true, respectively.
     *
     * @param searchString <code>String</code> that we will try to locate
     * @param startIndex position in the document in the appropriate coordinates
     * from which we will start search or -1 to start from the beginning
     * @param backward <code>true</code> if we should perform search towards the beginning
     * @return the position of the match in appropriate coordinates or -1 if
     *   no match found.
     */
    @Override
    public int search(String searchString, int startIndex, boolean backward) {
        Pattern pattern = null;
        if (!isEmpty(searchString)) {
            pattern = Pattern.compile(searchString, 0);
        }
        return search(pattern, startIndex, backward);
    }

    /**
     * Performs a forward search starting at the beginning
     * across the Searchable using the pattern; {@link java.util.regex.Pattern}.
     *
     * @param pattern <code>Pattern</code> that we will try to locate
     * @return the position of the match in appropriate coordinates or -1 if
     *   no match found.
     */
    @Override
    public int search(Pattern pattern) {
        return search(pattern, -1);
    }

    /**
     * Performs a forward search starting at the given startIndex
     * using the Pattern; {@link java.util.regex.Pattern}.
     *
     * @param pattern <code>Pattern</code> that we will try to locate
     * @param startIndex position in the document in the appropriate coordinates
     * from which we will start search or -1 to start from the beginning
     * @return the position of the match in appropriate coordinates or -1 if
     *   no match found.
     */
    @Override
    public int search(Pattern pattern, int startIndex) {
        return search(pattern, startIndex, false);
    }

    /**
     * Performs a  search starting at the given startIndex
     * using the pattern; {@link java.util.regex.Pattern}.
     * The search direction depends on the boolean parameter:
     * forward/backward if false/true, respectively.<p>
     *
     * Updates visible and internal search state.
     *
     * @param pattern <code>Pattern</code> that we will try to locate
     * @param startIndex position in the document in the appropriate coordinates
     * from which we will start search or -1 to start from the beginning
     * @param backwards <code>true</code> if we should perform search towards the beginning
     * @return the position of the match in appropriate coordinates or -1 if
     *   no match found.
     */
    @Override
    public int search(Pattern pattern, int startIndex, boolean backwards) {
        int matchingRow = doSearch(pattern, startIndex, backwards);
        moveMatchMarker();
        return matchingRow;
    }

    /**
     * Performs a  search starting at the given startIndex
     * using the pattern; {@link java.util.regex.Pattern}.
     * The search direction depends on the boolean parameter:
     * forward/backward if false/true, respectively.<p>
     *
     * Updates internal search state.
     *
     * @param pattern <code>Pattern</code> that we will try to locate
     * @param startIndex position in the document in the appropriate coordinates
     * from which we will start search or -1 to start from the beginning
     * @param backwards <code>true</code> if we should perform search towards the beginning
     * @return the position of the match in appropriate coordinates or -1 if
     *   no match found.
     */
    protected int doSearch(Pattern pattern, final int startIndex, boolean backwards) {
        if (isTrivialNoMatch(pattern, startIndex)) {
            updateState(null);
            return lastSearchResult.foundRow;
        }

        int startRow;
        if (isEqualStartIndex(startIndex)) { // implies: the last found coordinates are valid
            if (!isEqualPattern(pattern)) {
               SearchResult searchResult = findExtendedMatch(pattern, startIndex);
               if (searchResult != null) {
                   updateState(searchResult);
                   return lastSearchResult.foundRow;
               }

            }
            // didn't find a match, make sure to move the startPosition
            // for looking for the next/previous match
            startRow = moveStartPosition(startIndex, backwards);

        } else {
            // startIndex is different from last search, reset the column to -1
            // and make sure a -1 startIndex is mapped to first/last row, respectively.
            startRow = adjustStartPosition(startIndex, backwards);
        }
        findMatchAndUpdateState(pattern, startRow, backwards);
        return lastSearchResult.foundRow;
    }

    /**
     * Loops through the searchable until a match is found or the
     * end is reached. Updates internal search state.
     *
     * @param pattern <code>Pattern</code> that we will try to locate
     * @param startRow position in the document in the appropriate coordinates
     * from which we will start search or -1 to start from the beginning
     * @param backwards <code>true</code> if we should perform search towards the beginning
     */
    protected abstract void findMatchAndUpdateState(Pattern pattern, int startRow, boolean backwards);

    /**
     * Returns a boolean indicating if it can be trivially decided to not match.
     * <p>
     *
     * This implementation returns true if pattern is null or startIndex
     * exceeds the upper size limit.<p>
     *
     * @param pattern <code>Pattern</code> that we will try to locate
     * @param startIndex position in the document in the appropriate coordinates
     * from which we will start search or -1 to start from the beginning
     * @return true if we can say ahead that no match will be found with given search criteria
     */
    protected boolean isTrivialNoMatch(Pattern pattern, final int startIndex) {
        return (pattern == null) || (startIndex >= getSize());
    }

    /**
     * Called if <code>startIndex</code> is different from last search
     * and make sure a backwards/forwards search starts at last/first row,
     * respectively.<p>
     *
     * @param startIndex position in the document in the appropriate coordinates
     * from which we will start search or -1 to start from the beginning
     * @param backwards <code>true</code> if we should perform search from towards the beginning
     * @return adjusted <code>startIndex</code>
     */
    protected int adjustStartPosition(int startIndex, boolean backwards) {
        if (startIndex < 0) {
            if (backwards) {
                return getSize() - 1;
            } else {
                return 0;
            }
        }
        return startIndex;
    }

    /**
     * Moves the internal start position for matching as appropriate and returns
     * the new startIndex to use. Called if search was messaged with the same
     * startIndex as previously.
     * <p>
     *
     * This implementation returns a by 1 decremented/incremented startIndex
     * depending on backwards true/false, respectively.
     *
     * @param startIndex position in the document in the appropriate coordinates
     * from which we will start search or -1 to start from the beginning
     * @param backwards <code>true</code> if we should perform search towards the beginning
     * @return adjusted <code>startIndex</code>
     */
    protected int moveStartPosition(int startIndex, boolean backwards) {
        if (backwards) {
                   startIndex--;
           } else {
                   startIndex++;
           }
        return startIndex;
    }

    /**
     * Checks if the given Pattern should be considered as the same as
     * in a previous search.
     * <p>
     * This implementation compares the patterns' regex.
     *
     * @param pattern <code>Pattern</code> that we will compare with last request
     * @return if provided <code>Pattern</code> is the same as the stored from
     * the previous search attempt
     */
    protected boolean isEqualPattern(Pattern pattern) {
        return pattern.pattern().equals(lastSearchResult.getRegEx());
    }

    /**
     * Checks if the startIndex should be considered as the same as in
     * the previous search.
     *
     * @param startIndex <code>startIndex</code> that we will compare with the index
     * stored by the previous search request
     * @return true if the startIndex should be re-matched, false if not.
     */
    protected boolean isEqualStartIndex(final int startIndex) {
        return isValidIndex(startIndex) && (startIndex == lastSearchResult.foundRow);
    }

    /**
     * Checks if the searchString should be interpreted as empty.
     * <p>
     * This implementation returns true if string is null or has zero length.
     *
     * @param searchString <code>String</code> that we should evaluate
     * @return true if the provided <code>String</code> should be interpreted as empty
     */
    protected boolean isEmpty(String searchString) {
        return (searchString == null) || searchString.length() == 0;
    }

    /**
     * Matches the cell at row/lastFoundColumn against the pattern.
     * Called if sameRowIndex && !hasEqualRegEx.
     * PRE: lastFoundColumn valid.
     *
     * @param pattern <code>Pattern</code> that we will try to match
     * @param row position at which we will get the value to match with the provided <code>Pattern</code>
     * @return result of the match; {@link SearchResult}
     */
    protected abstract SearchResult findExtendedMatch(Pattern pattern, int row);

    /**
     * Factory method to create a SearchResult from the given parameters.
     *
     * @param matcher the matcher after a successful find. Must not be null.
     * @param row the found index
     * @param column the found column
     * @return newly created <code>SearchResult</code>
     */
    protected SearchResult createSearchResult(Matcher matcher, int row, int column) {
        return new SearchResult(matcher.pattern(),
                matcher.toMatchResult(), row, column);
    }

   /**
    * Checks if index is in range: 0 <= index < getSize().
    *
    * @param index possible start position that we will check for validity
    * @return <code>true</code> if given parameter is valid index
    */
   protected boolean isValidIndex(int index) {
        return index >= 0 && index < getSize();
    }

   /**
    * Returns the size of this searchable.
    *
    * @return size of this searchable
    */
   protected abstract int getSize();

    /**
     * Updates inner searchable state based on provided search result
     *
     * @param searchResult <code>SearchResult</code> that represents the new state
     *  of this <code>AbstractSearchable</code>
     */
    protected void updateState(SearchResult searchResult) {
        lastSearchResult.updateFrom(searchResult);
    }

    /**
     * Moves the match marker according to current found state.
     */
    protected abstract void moveMatchMarker();

    /**
     * It's the responsibility of subclasses to covariant override.
     *
     * @return the target component
     */
    public abstract JComponent getTarget();

    /**
     * Removes the highlighter.
     *
     * @param searchHighlighter the Highlighter to remove.
     */
    protected abstract void removeHighlighter(Highlighter searchHighlighter);

    /**
     * Returns the highlighters registered on the search target.
     *
     * @return all registered highlighters
     */
    protected abstract Highlighter[] getHighlighters();

    /**
     * Adds the highlighter to the target.
     *
     * @param highlighter the Highlighter to add.
     */
    protected abstract void addHighlighter(Highlighter highlighter);

    /**
     * Ensure that the given Highlighter is the last in the list of
     * the highlighters registered on the target.
     *
     * @param highlighter the Highlighter to be inserted as last.
     */
    protected void ensureInsertedSearchHighlighters(Highlighter highlighter) {
        if (!isInPipeline(highlighter)) {
            addHighlighter(highlighter);
        }
    }

    /**
     * Returns a flag indicating if the given highlighter is last in the
     * list of highlighters registered on the target. If so returns true.
     * If not, it has the side-effect of removing the highlighter and returns false.
     *
     * @param searchHighlighter the highlighter to check for being last
     * @return a boolean indicating whether the highlighter is last.
     */
    private boolean isInPipeline(Highlighter searchHighlighter) {
        Highlighter[] inPipeline = getHighlighters();
        if ((inPipeline.length > 0) &&
           (searchHighlighter.equals(inPipeline[inPipeline.length -1]))) {
            return true;
        }
        removeHighlighter(searchHighlighter);
        return false;
    }

    /**
     * Converts and returns the given column index from view coordinates to model
     * coordinates.
     * <p>
     * This implementation returns the view coordinate, that is assumes
     * that both coordinate systems are the same.
     *
     * @param viewColumn the column index in view coordinates, must be a valid index
     *   in that system.
     * @return the column index in model coordinates.
     */
    protected int convertColumnIndexToModel(int viewColumn) {
        return viewColumn;
    }

    /**
     *
     * @param result
     * @return {@code true} if the {@code result} contains a match;
     *         {@code false} otherwise
     */
    private boolean hasMatch(SearchResult result) {
        boolean noMatch =  (result.getFoundRow() < 0) || (result.getFoundColumn() < 0);
        return !noMatch;
    }

    /**
     * Returns a boolean indicating whether the current search result is a match.
     * <p>
     * PENDING JW: move to SearchResult?
     * @return a boolean indicating whether the current search result is a match.
     */
    protected boolean hasMatch() {
        return hasMatch(lastSearchResult);
    }

    /**
     * Returns a boolean indicating whether a match should be marked with a
     * Highlighter. Typically, if true, the match highlighter is used, otherwise
     * a match is indicated by selection.
     * <p>
     *
     * This implementation returns true if the target component has a client
     * property for key MATCH_HIGHLIGHTER with value Boolean.TRUE, false
     * otherwise. The SearchFactory sets that client property in incremental
     * search mode, that is when triggering a search via the JXFindBar as
     * installed by the factory.
     *
     * @return a boolean indicating whether a match should be marked by a using
     *         a Highlighter.
     *
     * @see SearchFactory
     */
    protected boolean markByHighlighter() {
        return Boolean.TRUE.equals(getTarget().getClientProperty(
                MATCH_HIGHLIGHTER));
    }

    /**
     * Sets the AbstractHighlighter to use as match marker, if enabled. A null value
     * will re-install the default.
     *
     * @param hl the Highlighter to use as match marker.
     */
    public void setMatchHighlighter(AbstractHighlighter hl) {
        removeHighlighter(matchHighlighter);
        matchHighlighter = hl;
        if (markByHighlighter()) {
            moveMatchMarker();
        }
    }

    /**
     * Returns the Hihglighter to use as match marker, lazyly created if null.
     *
     * @return a highlighter used for matching, guaranteed to be not null.
     */
    protected AbstractHighlighter getMatchHighlighter() {
        if (matchHighlighter == null) {
            matchHighlighter = createMatchHighlighter();
        }
        return matchHighlighter;
    }

    /**
     * Creates and returns the Highlighter used as match marker.
     *
     * @return a highlighter used for matching
     */
    protected AbstractHighlighter createMatchHighlighter() {
        return new ColorHighlighter(HighlightPredicate.NEVER, Color.YELLOW.brighter(),
                null, Color.YELLOW.brighter(),
                null);
    }

    /**
     * Configures and returns the match highlighter for the current match.
     *
     * @return a highlighter configured for matching
     */
    protected AbstractHighlighter getConfiguredMatchHighlighter() {
        AbstractHighlighter searchHL = getMatchHighlighter();
        searchHL.setHighlightPredicate(createMatchPredicate());
        return searchHL;
    }

    /**
     * Creates and returns a HighlightPredicate appropriate for the current
     * search result.
     *
     * @return a HighlightPredicate appropriate for the current search result.
     */
    protected HighlightPredicate createMatchPredicate() {
        return hasMatch() ?
                new SearchPredicate(lastSearchResult.pattern, lastSearchResult.foundRow,
                        convertColumnIndexToModel(lastSearchResult.foundColumn))
                : HighlightPredicate.NEVER;
    }

    /**
     * A convenience class to hold search state.<p>
     *
     * NOTE: this is still in-flow, probably will take more responsibility/
     * or even change altogether on further factoring
     */
    public static class SearchResult {
        int foundRow;
        int foundColumn;
        MatchResult matchResult;
        Pattern pattern;

        /**
         * Instantiates an empty SearchResult.
         */
        public SearchResult() {
            reset();
        }

        /**
         * Instantiates a SearchResult with the given state.
         *
         * @param ex the Pattern used for matching
         * @param result the current MatchResult
         * @param row the row index of the current match
         * @param column  the column index of the current match
         */
        public SearchResult(Pattern ex, MatchResult result, int row, int column) {
            pattern = ex;
            matchResult = result;
            foundRow = row;
            foundColumn = column;
        }

        /**
         * Sets internal state to the same as the given SearchResult. Resets internals
         * if the param is null.
         *
         * @param searchResult the SearchResult to copy internal state from.
         */
        public void updateFrom(SearchResult searchResult) {
            if (searchResult == null) {
                reset();
                return;
            }
            foundRow = searchResult.foundRow;
            foundColumn = searchResult.foundColumn;
            matchResult = searchResult.matchResult;
            pattern = searchResult.pattern;
        }

        /**
         * Returns the regex of the Pattern used for matching.
         *
         * @return the regex of the Pattern used for matching.
         */
        public String getRegEx() {
            return pattern != null ? pattern.pattern() : null;
        }

        /**
         * Resets all internal state to no-match.
         */
        public void reset() {
            foundRow= -1;
            foundColumn = -1;
            matchResult = null;
            pattern = null;
        }

        /**
         * Resets the column to OFF.
         */
        public void resetFoundColumn() {
            foundColumn = -1;
        }

        /**
         * Returns the column index of the match position.
         *
         * @return the column index of the match position.
         */
        public int getFoundColumn() {
            return foundColumn;
        }

        /**
         * Returns the row index of the match position.
         *
         * @return the row index of the match position.
         */
        public int getFoundRow() {
            return foundRow;
        }

        /**
         * Returns the MatchResult representing the current match.
         *
         * @return the MatchResult representing the current match.
         */
        public MatchResult getMatchResult() {
            return matchResult;
        }

        /**
         * Returns the Pattern used for matching.
         *
         * @return the Pattern used for the matching.
         */
        public Pattern getPattern() {
            return pattern;
        }

    }

}
