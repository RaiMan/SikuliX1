/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.jdesktop.swingx;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>An annotation that can be applied to a {@link javax.swing.RepaintManager} to suggest that
 * the <code>RepaintManager</code> supports translucency. If a <code>JXPanel</code>
 * is made translucent by setting it's alpha property to a value between 0 and 1,
 * then the <code>JXPanel</code> must ensure that a <code>RepaintManager</code>
 * capable of handling transparency is installed. This annotation tells the
 * <code>JXPanel</code> that the installed <code>RepaintManager</code> does not
 * need to be replaced. This is critical for custom <code>RepaintManager</code>s
 * which are used in applications along with transparent <code>JXPanel</code>s.</p>
 *
 * <p>A <code>RepaintManager</code> supports translucency if, when a repaint on a
 * child component occurs, it begins painting <em>not</em> on the child component,
 * but on the child component's <code>JXPanel</code> ancestor if: a) there is such
 * an ancestor and b) the ancestor returns an effective alpha of &lt; 1.</p>
 *
 * @see RepaintManagerX
 * @see JXPanel
 * @author rbair
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TranslucentRepaintManager {
}
