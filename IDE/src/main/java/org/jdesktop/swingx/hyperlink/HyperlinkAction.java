/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.hyperlink;

import java.awt.Desktop;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Desktop.Action;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;

/**
 * A implementation wrapping <code>Desktop</code> actions BROWSE and MAIL, that is
 * URI-related.
 *
 * @author Jeanette Winzenburg
 */
public class HyperlinkAction extends AbstractHyperlinkAction<URI> {
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(HyperlinkAction.class
            .getName());

    private Action desktopAction;
    private URIVisitor visitor;

    /**
     * Factory method to create and return a HyperlinkAction for the given uri. Tries
     * to guess the appropriate type from the uri. If uri is not null and has a
     * scheme of mailto, create one of type Mail. In all other cases, creates one
     * for BROWSE.
     *
     * @param uri to uri to create a HyperlinkAction for, maybe null.
     * @return a HyperlinkAction for the given URI.
     * @throws HeadlessException if {@link
     * GraphicsEnvironment#isHeadless()} returns {@code true}
     * @throws UnsupportedOperationException if the current platform doesn't support
     *   Desktop
     */
    public static HyperlinkAction createHyperlinkAction(URI uri) {
        Action type = isMailURI(uri) ? Action.MAIL : Action.BROWSE;
        return createHyperlinkAction(uri, type);
    }

    /**
     * Creates and returns a HyperlinkAction with the given target and action type.
     * @param uri the target uri, maybe null.
     * @param desktopAction the type of desktop action this class should perform, must be
     *    BROWSE or MAIL
     * @return a HyperlinkAction
     * @throws HeadlessException if {@link
     * GraphicsEnvironment#isHeadless()} returns {@code true}
     * @throws UnsupportedOperationException if the current platform doesn't support
     *   Desktop
     * @throws IllegalArgumentException if unsupported action type
     */
    public static HyperlinkAction createHyperlinkAction(URI uri, Action type) {
        return new HyperlinkAction(uri, type);
    }

    /**
     * @param uri
     * @return
     */
    private static boolean isMailURI(URI uri) {
        return uri != null && "mailto".equalsIgnoreCase(uri.getScheme());
    }

    /**
     * Instantiates a HyperlinkAction with action type BROWSE.
     *
     * @throws HeadlessException if {@link
     * GraphicsEnvironment#isHeadless()} returns {@code true}
     * @throws UnsupportedOperationException if the current platform doesn't support
     *   Desktop
     * @throws IllegalArgumentException if unsupported action type
     */
    public HyperlinkAction() {
        this(Action.BROWSE);
    }

    /**
     * Instantiates a HyperlinkAction with the given action type.
     *
     * @param desktopAction the type of desktop action this class should perform, must be
     *    BROWSE or MAIL
     * @throws HeadlessException if {@link
     * GraphicsEnvironment#isHeadless()} returns {@code true}
     * @throws UnsupportedOperationException if the current platform doesn't support
     *   Desktop
     * @throws IllegalArgumentException if unsupported action type
     */
    public HyperlinkAction(Action desktopAction) {
        this(null, desktopAction);
    }

    /**
     *
     * @param uri the target uri, maybe null.
     * @param desktopAction the type of desktop action this class should perform, must be
     *    BROWSE or MAIL
     * @throws HeadlessException if {@link
     * GraphicsEnvironment#isHeadless()} returns {@code true}
     * @throws UnsupportedOperationException if the current platform doesn't support
     *   Desktop
     * @throws IllegalArgumentException if unsupported action type
     */
    public HyperlinkAction(URI uri, Action desktopAction) {
        super();
        if (!Desktop.isDesktopSupported()) {
            throw new UnsupportedOperationException("Desktop API is not " +
                                                    "supported on the current platform");
        }
        if (desktopAction != Desktop.Action.BROWSE && desktopAction != Desktop.Action.MAIL) {
           throw new IllegalArgumentException("Illegal action type: " + desktopAction +
                   ". Must be BROWSE or MAIL");
        }
        this.desktopAction = desktopAction;
        getURIVisitor();
        setTarget(uri);
    }

    /**
     * {@inheritDoc} <p>
     *
     * Implemented to perform the appropriate Desktop action if supported on the current
     * target. Sets the visited property to true if the desktop action doesn't throw
     * an exception or to false if it did.
     *
     * Does nothing if the action isn't supported.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (!getURIVisitor().isEnabled(getTarget())) return;
        try {
            getURIVisitor().visit(getTarget());
            setVisited(true);
        } catch (IOException e1) {
            setVisited(false);
            LOG.fine("cant visit Desktop " + e);
        }
    }

    /**
     * @return
     */
    public Action getDesktopAction() {
        return desktopAction;
    }

    @Override
    protected void installTarget() {
        // doohh ... this is called from super's constructor before we are
        // fully initialized
        if (visitor == null) return;
        super.installTarget();
        updateEnabled();
    }

    /**
     *
     */
    private void updateEnabled() {
        setEnabled(getURIVisitor().isEnabled(getTarget()));
    }

    /**
     * @return
     */
    private URIVisitor getURIVisitor() {
        if (visitor == null) {
            visitor = createURIVisitor();
        }
        return visitor;
    }

    /**
     * @return
     */
    private URIVisitor createURIVisitor() {
        return getDesktopAction() == Action.BROWSE ?
                new BrowseVisitor() : new MailVisitor();
    }

    /**
     * Thin wrapper around Desktop functionality to allow uniform handling of
     * different actions in HyperlinkAction.
     *
     */
    private abstract class URIVisitor {
        protected boolean desktopSupported = Desktop.isDesktopSupported();

        /**
         * Returns a boolean indicating whether the action is supported on the
         * given URI. This implementation returns true if both the Desktop is
         * generally supported and <code>isActionSupported()</code>.
         *
         * PENDING JW: hmm ... which class exactly has to check for valid combination
         * of Action and URI?
         *
         * @param uri
         * @return
         *
         * @see #isActionSupported()
         */
        public boolean isEnabled(URI uri) {
            return desktopSupported && isActionSupported();
        }

        /**
         * Visits the given URI via Desktop functionality. Must not be called if not
         * enabled.
         *
         * @param uri the URI to visit
         * @throws IOException if the Desktop method throws IOException.
         *
         */
        public abstract void visit(URI uri) throws IOException;

        /**
         * Returns a boolean indicating if the action is supported by the current
         * Desktop.
         *
         * @return true if the Action is supported by the current desktop, false
         * otherwise.
         */
        protected abstract boolean isActionSupported();
    }

    private class BrowseVisitor extends URIVisitor {

        /**
         * {@inheritDoc} <p>
         *
         * Implemented to message the browse method of Desktop.
         */
        @Override
        public void visit(URI uri) throws IOException {
            Desktop.getDesktop().browse(uri);
        }

        /**
         * {@inheritDoc} <p>
         *
         * Implemented to query the Desktop for support of BROWSE action.
         */
        @Override
        protected boolean isActionSupported() {
            return Desktop.getDesktop().isSupported(Desktop.Action.BROWSE);
        }

        /**
         * {@inheritDoc} <p>
         *
         * Implemented to guard against null URI in addition to super.
         */
        @Override
        public boolean isEnabled(URI uri) {
            return uri != null && super.isEnabled(uri);
        }

    }

    private class MailVisitor extends URIVisitor {

        /**
         * {@inheritDoc} <p>
         *
         * Implemented to message the mail function of Desktop.
         */
        @Override
        public void visit(URI uri) throws IOException {
            if (uri == null) {
                Desktop.getDesktop().mail();
            } else {
                Desktop.getDesktop().mail(uri);
            }
        }
        /**
         * {@inheritDoc} <p>
         *
         * Implemented to query the Desktop for support of MAIL action.
         */
        @Override
        protected boolean isActionSupported() {
            return Desktop.getDesktop().isSupported(Desktop.Action.MAIL);
        }

    }
}
