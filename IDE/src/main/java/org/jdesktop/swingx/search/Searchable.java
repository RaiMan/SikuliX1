/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.jdesktop.swingx.search;

import java.util.regex.Pattern;

/**
 * Interface that used to implement search logic in all the search capable
 * components.
 *
 * @author Ramesh Gupta
 */
public interface Searchable {

    /**
     * Search <code>searchString</code> from the beginning of a document.
     *
     * @param searchString <code>String</code> we should find in a document.
     *
     * @return index of matched <code>String</code> or -1 if a match cannot be found.
     */
    public int search(String searchString);

    /**
     * Search <code>searchString</code> from the given position in a document.
     *
     * @param searchString <code>String</code> we should find in a document.
     * @param startIndex Start position in a document or -1 if we want to search from the beginning.
     *
     * @return index of matched <code>String</code> or -1 if a match cannot be found.
     */
    public int search(String searchString, int startIndex);

    /**
     * Search <code>searchString</code> in the given direction from the some position in a document.
     *
     * @param searchString <code>String</code> we should find in a document.
     * @param startIndex Start position in a document or -1 if we want to search from the beginning.
     * @param backward Indicates search direction, will search from the given position towards the
     *                 beginning of a document if this parameter is <code>true</code>.
     *
     * @return index of matched <code>String</code> or -1 if a match cannot be found.
     */
    public int search(String searchString, int startIndex, boolean backward);

    /**
     * Search for the pattern from the beginning of the document.
     *
     * @param pattern Pattern for search
     *
     * @return  index of matched <code>Pattern</code> or -1 if a match cannot be found.
     */
    public int search(Pattern pattern);

    /**
     * Search for the pattern from the start index.
     * @param pattern Pattern for search
     * @param startIndex starting index of search. If -1 then start from the beginning
     * @return index of matched pattern or -1 if a match cannot be found.
     */
    public int search(Pattern pattern, int startIndex);

    /**
     * Search for the pattern from the start index.
     * @param pattern Pattern for search
     * @param startIndex starting index of search. If -1 then start from the beginning
     * @param backward indicates the direction if true then search is backwards
     * @return index of matched pattern or -1 if a match cannot be found.
     */
    public int search(Pattern pattern, int startIndex, boolean backward);
}
