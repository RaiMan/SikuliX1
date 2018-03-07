/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.search;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jdesktop.swingx.JXList;
import org.jdesktop.swingx.decorator.AbstractHighlighter;
import org.jdesktop.swingx.decorator.Highlighter;

public class ListSearchable extends AbstractSearchable {

        protected JXList list;

        public ListSearchable(JXList list) {
           this.list = list;
        }

        @Override
        protected void findMatchAndUpdateState(Pattern pattern, int startRow, boolean backwards) {
            SearchResult searchResult = null;
            if (backwards) {
                for (int index = startRow; index >= 0 && searchResult == null; index--) {
                    searchResult = findMatchAt(pattern, index);
                }
            } else {
                for (int index = startRow; index < getSize() && searchResult == null; index++) {
                    searchResult = findMatchAt(pattern, index);
                }
            }
            updateState(searchResult);
        }

        @Override
        protected SearchResult findExtendedMatch(Pattern pattern, int row) {

            return findMatchAt(pattern, row);
        }
        /**
         * Matches the cell content at row/col against the given Pattern.
         * Returns an appropriate SearchResult if matching or null if no
         * matching
         *
         * @param pattern
         * @param row a valid row index in view coordinates
         * @return <code>SearchResult</code> if matched otherwise null
         */
        protected SearchResult findMatchAt(Pattern pattern, int row) {
            String text = list.getStringAt(row);
            if ((text != null) && (text.length() > 0 )) {
                Matcher matcher = pattern.matcher(text);
                if (matcher.find()) {
                    return createSearchResult(matcher, row, 0);
                }
            }
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected int getSize() {
            return list.getElementCount();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public JXList getTarget() {
            return list;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void moveMatchMarker() {
            if (markByHighlighter()) {
                moveMatchByHighlighter();
            } else { // use selection
                moveMatchBySelection();
            }
        }

        protected void moveMatchBySelection() {
          // PENDING JW: #718-swingx - don't move selection on not found
          // complying here is accidental, defaultListSelectionModel doesn't
          // clear on -1 but silently does nothing
          // isn't doc'ed anywhere - so we back out
            if (!hasMatch()) {
                return;
            }
            list.setSelectedIndex(lastSearchResult.foundRow);
            list.ensureIndexIsVisible(lastSearchResult.foundRow);
        }

        /**
         * use and move the match highlighter.
         * PRE: markByHighlighter
         *
         */
        protected void moveMatchByHighlighter() {
            AbstractHighlighter searchHL = getConfiguredMatchHighlighter();
            // no match
            if (!hasMatch()) {
                return;
            } else {
                ensureInsertedSearchHighlighters(searchHL);
                list.ensureIndexIsVisible(lastSearchResult.foundRow);
            }
        }


        /**
         * @param searchHighlighter
         */
        @Override
        protected void removeHighlighter(Highlighter searchHighlighter) {
            list.removeHighlighter(searchHighlighter);
        }

        /**
         * @return all registered highlighters
         */
        @Override
        protected Highlighter[] getHighlighters() {
            return list.getHighlighters();
        }

        /**
         * @param highlighter
         */
        @Override
        protected void addHighlighter(Highlighter highlighter) {
            list.addHighlighter(highlighter);
        }

    }
