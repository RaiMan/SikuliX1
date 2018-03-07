/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.search;

import java.util.regex.Pattern;

/**
 * Implemented by classes that work with {@link java.util.regex.Pattern} objects.

 * @author Ramesh Gupta
 */
public interface PatternMatcher {
    public Pattern getPattern();
    public void setPattern(Pattern pattern);
}
