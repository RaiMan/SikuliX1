/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.sort;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.RowFilter;

import org.jdesktop.swingx.util.Contract;

/**
 * Factory of additional <code>RowFilter</code>s. <p>
 *
 * Trigger is the missing of Pattern/Regex+matchflags factory method in core.
 * Can't do much other than c&p core as both abstract base class GeneralFilter and
 * concrete RowFilter are private. Expose the base as public for custom subclasses
 *
 * @author Jeanette Winzenburg
 */
@SuppressWarnings("unchecked")
public class RowFilters {

    /**
     * Returns a <code>RowFilter</code> that uses a regular
     * expression to determine which entries to include.  Only entries
     * with at least one matching value are included.  For
     * example, the following creates a <code>RowFilter</code> that
     * includes entries with at least one value starting with
     * "a":
     * <pre>
     *   RowFilter.regexFilter("^a");
     * </pre>
     * <p>
     * The returned filter uses {@link java.util.regex.Matcher#find}
     * to test for inclusion.  To test for exact matches use the
     * characters '^' and '$' to match the beginning and end of the
     * string respectively.  For example, "^foo$" includes only rows whose
     * string is exactly "foo" and not, for example, "food".  See
     * {@link java.util.regex.Pattern} for a complete description of
     * the supported regular-expression constructs.
     *
     * @param regex the regular expression to filter on
     * @param indices the indices of the values to check.  If not supplied all
     *               values are evaluated
     * @return a <code>RowFilter</code> implementing the specified criteria
     * @throws NullPointerException if <code>regex</code> is
     *         <code>null</code>
     * @throws IllegalArgumentException if any of the <code>indices</code>
     *         are &lt; 0
     * @throws PatternSyntaxException if <code>regex</code> is
     *         not a valid regular expression.
     * @see java.util.regex.Pattern
     */
    public static <M,I> RowFilter<M,I> regexFilter(String regex,
            int... indices) {
        return regexFilter(0, regex, indices);
    }

    /**
     * Returns a <code>RowFilter</code> that uses a regular
     * expression to determine which entries to include.  Only entries
     * with at least one matching value are included.  For
     * example, the following creates a <code>RowFilter</code> that
     * includes entries with at least one value starting with
     * "a" ignoring case:
     * <pre>
     *   RowFilter.regexFilter(Pattern.CASE_INSENSITIVE, "^a");
     * </pre>
     * <p>
     * The returned filter uses {@link java.util.regex.Matcher#find}
     * to test for inclusion.  To test for exact matches use the
     * characters '^' and '$' to match the beginning and end of the
     * string respectively.  For example, "^foo$" includes only rows whose
     * string is exactly "foo" and not, for example, "food".  See
     * {@link java.util.regex.Pattern} for a complete description of
     * the supported regular-expression constructs.
     *
     * @param matchFlags
     *         Match flags, a bit mask that may include
     *         {@link Pattern#CASE_INSENSITIVE}, {@link Pattern#MULTILINE}, {@link Pattern#DOTALL},
     *         {@link Pattern#UNICODE_CASE}, {@link Pattern#CANON_EQ}, {@link Pattern#UNIX_LINES},
     *         {@link Pattern#LITERAL} and {@link Pattern#COMMENTS}
     *
     * @param regex the regular expression to filter on
     * @param indices the indices of the values to check.  If not supplied all
     *               values are evaluated
     * @return a <code>RowFilter</code> implementing the specified criteria
     * @throws NullPointerException if <code>regex</code> is
     *         <code>null</code>
     * @throws IllegalArgumentException if any of the <code>indices</code>
     *         are &lt; 0
     * @throws  IllegalArgumentException
     *          If bit values other than those corresponding to the defined
     *          match flags are set in <tt>flags</tt>
     * @throws PatternSyntaxException if <code>regex</code> is
     *         not a valid regular expression.
     * @see java.util.regex.Pattern
     */
    public static <M,I> RowFilter<M,I> regexFilter(int matchFlags, String regex,
            int... indices) {
        return regexFilter(Pattern.compile(regex, matchFlags), indices);
    }

    /**
     * Returns a <code>RowFilter</code> that uses a regular
     * expression to determine which entries to include.
     *
     * @param pattern the Pattern to use for matching
     * @param indices the indices of the values to check.  If not supplied all
     *               values are evaluated
     * @return a <code>RowFilter</code> implementing the specified criteria
     * @throws NullPointerException if <code>pattern</code> is
     *         <code>null</code>
     * @see java.util.regex.Pattern
     */
    public static <M,I> RowFilter<M,I> regexFilter(Pattern pattern,
                                                       int... indices) {
        return (RowFilter<M,I>)new RegexFilter(pattern, indices);
    }

    /**
     * C&P from core Swing to allow subclassing.
     */
    public static abstract class GeneralFilter extends RowFilter<Object,Object> {
        private int[] columns;

        protected GeneralFilter(int... columns) {
            checkIndices(columns);
            this.columns = columns;
        }

        @Override
        public boolean include(Entry<? extends Object,? extends Object> value){
            int count = value.getValueCount();
            if (columns.length > 0) {
                for (int i = columns.length - 1; i >= 0; i--) {
                    int index = columns[i];
                    if (index < count) {
                        if (include(value, index)) {
                            return true;
                        }
                    }
                }
            }
            else {
                while (--count >= 0) {
                    if (include(value, count)) {
                        return true;
                    }
                }
            }
            return false;
        }

        protected abstract boolean include(
              Entry<? extends Object,? extends Object> value, int index);
        /**
         * Throws an IllegalArgumentException if any of the values in
         * columns are < 0.
         */
        protected void checkIndices(int[] columns) {
            for (int i = columns.length - 1; i >= 0; i--) {
                if (columns[i] < 0) {
                    throw new IllegalArgumentException("Index must be >= 0");
                }
            }
        }
    }

    /**
     * C&P from core to allow richer factory methods.
     */
    private static class RegexFilter extends GeneralFilter {
        private Matcher matcher;

        RegexFilter(Pattern regex, int[] columns) {
            super(columns);
            if (regex == null) {
                // JW: Exception type changed to comply with swingx convention
                Contract.asNotNull(regex, "Pattern must be non-null");
//                throw new IllegalArgumentException("Pattern must be non-null");
            }
            matcher = regex.matcher("");
        }

        @Override
        protected boolean include(
                Entry<? extends Object,? extends Object> value, int index) {
            matcher.reset(value.getStringValue(index));
            return matcher.find();
        }
    }

    private RowFilters() {};

}
