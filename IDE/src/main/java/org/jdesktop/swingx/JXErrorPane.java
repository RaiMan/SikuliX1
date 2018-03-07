/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx;

import java.awt.Component;
import java.lang.reflect.InvocationTargetException;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.SwingUtilities;

import org.jdesktop.beans.JavaBean;
import org.jdesktop.swingx.error.ErrorInfo;
import org.jdesktop.swingx.error.ErrorReporter;
import org.jdesktop.swingx.plaf.ErrorPaneAddon;
import org.jdesktop.swingx.plaf.ErrorPaneUI;
import org.jdesktop.swingx.plaf.LookAndFeelAddons;

/**
 * <p>JXErrorPane is a common error component suitable for displaying errors,
 * warnings, and exceptional application behavior to users.</p>
 *
 * <p>User interaction with the <code>JXErrorPane</code> includes the ability to
 * view details associated with the error. This is the primary feature that differentiates
 * <code>JXErrorPane</code> from <code>JOptionPane</code>. In addition,
 * <code>JXErrorPane</code> specializes in handling unrecoverable errors. If you
 * need an error dialog that allows the user to take some action to recover
 * from an error (such as "Repair Disk", "Replace All", etc) then you should
 * use <code>JOptionPane</code>.</p>
 *
 * <p>Data and application state associated with an error are encapsulated
 * in the {@link org.jdesktop.swingx.error.ErrorInfo} class. The
 * {@code JXErrorPane} displays the data contained in the {@code ErrorInfo}.
 * In addition, {@code ErrorInfo} is passed to the
 * {@link org.jdesktop.swingx.error.ErrorReporter} if the user decides to report
 * the incident.</p>
 *
 * <h2>Basic Usage</h2>
 * <p>Typically, the <code>JXErrorPane</code>
 * is not created and displayed directly. Instead, one of the static showXXX methods
 * are called that create and display the <code>JXErrorPane</code> in a
 * <code>JDialog</code>, <code>JFrame</code>, or <code>JInternalFrame</code>.</p>
 *
 * <p>These static showXXX methods all follow the same pattern, namely (
 * where XXX could be one of Dialog, Frame, or InternalFrame):
 * <ul>
 *  <li><b>showXXX(Throwable e)</b>: This usage allows you to show a default error
 *      window, detailing the error</li>
 *  <li><b>showXXX(Component owner, ErrorInfo info)</b>: This usage shows an
 *      error dialog based on the given <code>ErrorInfo</code>. The component
 *      argument is the component over which the dialog should be centered.</li>
 *  <li><b>showXXX(Component owner, JXErrorPane pane)</b>: This usage shows
 *      an error dialog using the given error pane. This allows you to completely
 *      modify the pane (perhaps installing a custom UI delegate, etc) to present
 *      to the user</li>
 *  <li><b>createXXX(Component owner, JXErrorPane pane)</b>: Creates and returns
 *      a dialog for presenting the given <code>JXErrorPane</code>, but does not
 *      show it. This allows the developer to modify properties of the dialog
 *      prior to display</li>
 * </ul></p>
 *
 * <p>Following are some examples and further discussion regarding some of these
 * static methods. Example of the most basic usage:
 * <pre><code>
 *      try {
 *          //do stuff.... something throws an exception in here
 *      } catch (Exception e) {
 *          JXErrorPane.showDialog(e);
 *      }
 * </code></pre>. Alternatively there are <code>showFrame</code> and
 * <code>showInternalFrame</code> variants of each of the <code>showDialog</code>
 * methods described in this API.</p>
 *
 * <p>While this is the simplest usage, it is not the recommended approach for
 * most errors since it yields the most difficult messages for users to understand.
 * Instead it is recommended to provide a more useful message for users. For example:
 * <pre><code>
 *      URL url = null;
 *      try {
 *          url = new URL(userSuppliedUrl);
 *      } catch (MalformedURLException e) {
 *          String msg = "The web resource you entered is not formatted"
 *                      + " correctly.";
 *          String details = "&lt;html&gt;Web resources should begin with \"http://\""
 *                      + " and cannot contain any spaces. Below are a few"
 *                      + " more guidelines.&lt;ul&gt;"
 *                      + getURLGuidelines()
 *                      + "&lt;/ul&gt;&lt;/html&gt;";
 *          JXErrorPane.showDialog(myWindow, "Unknown Resource", msg, details, e);
 *          return false;
 *      }
 * </code></pre></p>
 *
 * <p>Before showing the <code>JXErrorPane</code> in a frame or dialog, you may modify
 * the appearance and behavior of the <code>JXErrorPane</code> by setting one or more of its bean
 * properties. For example, to modify the icon shown with a particular
 * instance of a <code>JXErrorPane</code>, you might do the following:
 * <pre><code>
 *      JXErrorPane pane = new JXErrorPane();
 *      pane.setErrorIcon(myErrorIcon);
 *      pane.setErrorInfo(new ErrorInfo("Fatal Error", exception));
 *      JXErrorPane.showDialog(null, pane);
 * </code></pre></p>
 *
 * <p><code>JXErrorPane</code> may also be configured with a "Report" button which allows
 * the user to send a bug report, typically through email. This is done through
 * the pluggable {@link org.jdesktop.swingx.error.ErrorReporter} class. Simply instantiate
 * some custom subclass of <code>ErrorReporter</code> and pass the instance into the
 * {@link #setErrorReporter} method.</p>
 *
 * <p><code>JXErrorPane</code> can also be used for displaying fatal error messages to
 * users. Fatal messages indicate a serious error in the application that cannot
 * be corrected and that must result in the termination of the application.
 * After the close of a fatal error dialog, the application should
 * automatically exit. Fatal messages are identified by the <code>Level</code>
 * of the <code>ErrorInfo</code> being
 * {@link org.jdesktop.swingx.error.ErrorLevel}<code>.FATAL</code>.</p>
 *
 * <p>By default, when Fatal error dialogs are closed the application exits with
 * a code of "1". In other words, <code>System.exit(1)</code>. If you wish to implement
 * custom handling, you can replace the default fatal action in the <code>ActionMap</code>
 * of the <code>JXErrorPane</code> instance. If you specify a custom fatal
 * action, then the default action of calling
 * System.exit will not occur. You are therefore responsible for shutting down
 * the application.</p>
 *
 * <h2>UI Default Keys</h2>
 * <p>TODO</p>
 * JXErrorPane.errorIcon
 *      or, if not specified, JOptionPane.errorIcon
 * JXErrorPane.warningIcon
 *      or, if not specified, JOptionPane.warningIcon
 * JXErrorPane.details_contract_text (ignored on Mac OS X)
 * JXErrorPane.details_expand_text (ignored on Mac OS X)
 * JXErrorPane.mac.details_contract_text
 * JXErrorPane.mac.details_expand_text
 * Tree.expandedIcon (on Mac OS X)
 * Tree.collapsedIcon (on Mac OS X)
 *
 * <h2>Customizing the Look and Feel</h2>
 * <p>TODO</p>
 *
 *
 * @status REVIEWED
 *
 * @author Richard Bair
 * @author Alexander Zuev
 * @author Shai Almog
 * @author rah003
 */
@JavaBean
public class JXErrorPane extends JComponent {
    //---------------------------------------------------- static properties
    /**
     * Name of the Action used for reporting errors
     */
    public static final String REPORT_ACTION_KEY = "report-action";
    /**
     * Name of the Action used for fatal errors
     */
    public static final String FATAL_ACTION_KEY = "fatal-action";
    /**
     * UI Class ID
     */
    public final static String uiClassID = "ErrorPaneUI";

    /**
     */
    static {
        LookAndFeelAddons.contribute(new ErrorPaneAddon());
    }

    //-------------------------------------------------- instance properties

    /**
     * ErrorInfo that contains all the information prepared for
     * reporting.
     */
    private ErrorInfo errorInfo = new ErrorInfo("Error", "Normally this place contains problem description.\n You see this text because one of the following reasons:\n * Either it is a test\n * Developer have not provided error details\n * This error message was invoked unexpectedly and there are no more details available", null, null, null, null, null);
    /**
     * The Icon to use, regardless of the error message. The UI delegate is
     * responsible for setting this icon, if the developer has not specified
     * the icon.
     */
    private Icon icon;
    /**
     * The delegate to use for reporting errors.
     */
    private ErrorReporter reporter;

    //--------------------------------------------------------- constructors

    /**
     * Create a new <code>JXErrorPane</code>.
     */
    public JXErrorPane() {
        super();
        updateUI();
    }

    //------------------------------------------------------------- UI Logic

    /**
     * Returns the look and feel (L&F) object that renders this component.
     *
     * @return the {@link ErrorPaneUI} object that renders this component
     */
    public ErrorPaneUI getUI() {
        return (ErrorPaneUI)ui;
    }

    /**
     * Sets the look and feel (L&F) object that renders this component.
     *
     * @param ui
     *            the ErrorPaneUI L&F object
     * @see javax.swing.UIDefaults#getUI
     * @beaninfo bound: true hidden: true attribute: visualUpdate true
     *           description: The UI object that implements the Component's
     *           LookAndFeel.
     */
    public void setUI(ErrorPaneUI ui) {
        super.setUI(ui);
    }

    /**
     * Returns the name of the L&F class that renders this component.
     *
     * @return the string {@link #uiClassID}
     * @see javax.swing.JComponent#getUIClassID
     * @see javax.swing.UIDefaults#getUI
     */
    @Override
    public String getUIClassID() {
        return uiClassID;
    }

    /**
     * Notification from the <code>UIManager</code> that the L&F has changed.
     * Replaces the current UI object with the latest version from the
     * <code>UIManager</code>.
     *
     * @see javax.swing.JComponent#updateUI
     */
    @Override
    public void updateUI() {
        setUI((ErrorPaneUI) LookAndFeelAddons
                .getUI(this, ErrorPaneUI.class));
    }

    //-------------------------------------------- public methods/properties

    /**
     * Sets the ErrorInfo for this dialog. ErrorInfo can't be null.
     *
     * @param info ErrorInfo that incorporates all the details about the error. Null value is not supported.
     */
    public void setErrorInfo(ErrorInfo info) {
        if (info == null) {
            throw new NullPointerException("ErrorInfo can't be null. Provide valid ErrorInfo object.");
        }
        ErrorInfo old = this.errorInfo;
        this.errorInfo = info;
        firePropertyChange("errorInfo", old, this.errorInfo);
    }

    /**
     * Gets the <code>JXErrorPane</code>'s <code>ErrorInfo</code>
     *
     * @return <code>ErrorInfo</code> assigned to this dialog
     */
    public ErrorInfo getErrorInfo() {
        return errorInfo;
    }

    /**
     * Specifies the icon to use
     *
     * @param icon the Icon to use. May be null.
     */
    public void setIcon(Icon icon) {
        Icon old = this.icon;
        this.icon = icon;
        firePropertyChange("icon", old, this.icon);
    }

    /**
     * Returns the Icon used
     *
     * @return the Icon
     */
    public Icon getIcon() {
        return icon;
    }

    /**
     * Sets the {@link ErrorReporter} delegate to use. This delegate is called
     * automatically when the report action is fired.
     *
     * @param reporter the ErrorReporter to use. If null, the report button will
     *        not be shown in the error dialog.
     */
    public void setErrorReporter(ErrorReporter reporter) {
        ErrorReporter old = getErrorReporter();
        this.reporter = reporter;
        firePropertyChange("errorReporter", old, getErrorReporter());
    }

    /**
     * Gets the {@link ErrorReporter} delegate in use.
     *
     * @return the ErrorReporter. May be null.
     */
    public ErrorReporter getErrorReporter() {
        return reporter;
    }

    //------------------------------------------------------- static methods

    /**
     * <p>Constructs and shows the error dialog for the given exception.  The
     * exceptions message will be the errorMessage, and the stacktrace will form
     * the details for the error dialog.</p>
     *
     * <p>This method may be called from any thread. It will actually show the error
     * dialog on the AWT event dispatch thread. This method blocks. If called
     * on the EDT, the dialog shown will be modal. Otherwise, this thread will
     * block until the error dialog has been shown and hidden on the EDT.</p>
     *
     * @param e Exception that contains information about the error cause and stack trace
     */
    public static void showDialog(Throwable e) {
        ErrorInfo ii = new ErrorInfo(null, null, null, null, e, null, null);
        showDialog(null, ii);
    }

    /**
     * <p>Constructs and shows the error dialog, using the given
     * <code>ErrorInfo</code> to initialize the view.</p>
     *
     * <p>This method may be called from any thread. It will actually show the error
     * dialog on the AWT event dispatch thread. This method blocks. If called
     * on the EDT, the dialog shown will be modal. Otherwise, this thread will
     * block until the error dialog has been shown and hidden on the EDT.</p>
     *
     * @param owner Owner of this error dialog. Determines the Window in which the dialog
     *        is displayed; if the <code>owner</code> has
     *        no <code>Window</code>, a default <code>Frame</code> is used
     * @param info <code>ErrorInfo</code> that incorporates all the information about the error
     */
    public static void showDialog(Component owner, ErrorInfo info) {
        JXErrorPane pane = new JXErrorPane();
        pane.setErrorInfo(info);
        showDialog(owner, pane);
    }

    /**
     * <p>Constructs and shows the error dialog, using the given
     * <code>JXErrorPane</code> for the view portion of the dialog.</p>
     *
     * <p>This method may be called from any thread. It will actually show the error
     * dialog on the AWT event dispatch thread. This method blocks. If called
     * on the EDT, the dialog shown will be modal. Otherwise, this thread will
     * block until the error dialog has been shown and hidden on the EDT.</p>
     *
     * @param owner Owner of this error dialog. Determines the Window in which the dialog
     *        is displayed; if the <code>owner</code> has
     *        no <code>Window</code>, a default <code>Frame</code> is used
     * @param pane <code>JXErrorPane</code> which will form the content area
     *        of the dialog.
     */
    public static void showDialog(final Component owner, final JXErrorPane pane) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                JDialog dlg = createDialog(owner, pane);
                dlg.setVisible(true);
            }
        };

        if (!SwingUtilities.isEventDispatchThread()) {
            try {
                SwingUtilities.invokeAndWait(r);
            } catch (InvocationTargetException ex) {
                ex.printStackTrace();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        } else {
            r.run();
        }
    }

    /**
     * <p>Constructs and returns an error dialog, using the given
     * <code>JXErrorPane</code> for the view portion of the dialog.</p>
     *
     * <p>This method may be called from any thread. It does not block. The
     * caller is responsible for ensuring that the dialog is shown and manipulated
     * on the AWT event dispatch thread. A common way to do this is to use
     * <code>SwingUtilities.invokeAndWait</code> or
     * <code>SwingUtilities.invokeLater()</code>.</p>
     *
     * @param owner Owner of this error dialog. Determines the Window in which the dialog
     *        is displayed; if the <code>owner</code> has
     *        no <code>Window</code>, a default <code>Frame</code> is used
     * @param pane <code>JXErrorPane</code> which will form the content area
     *        of the dialog.
     * @return a <code>JDialog</code> configured to display the error.
     */
    public static JDialog createDialog(Component owner, JXErrorPane pane) {
        JDialog window = pane.getUI().getErrorDialog(owner);
        // If the owner is null applies orientation of the shared
        // hidden window used as owner.
        if(owner != null) {
            pane.applyComponentOrientation(owner.getComponentOrientation());
        } else {
            pane.applyComponentOrientation(window.getComponentOrientation());
        }
        window.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        window.pack();
        window.setLocationRelativeTo(owner);
        return window;
    }

    /**
     * <p>Constructs and shows the error frame for the given exception.  The
     * exceptions message will be the errorMessage, and the stacktrace will form
     * the details for the error dialog.</p>
     *
     * <p>This method may be called from any thread. It will actually show the error
     * dialog on the AWT event dispatch thread. This method blocks. If called
     * on the EDT, the frame shown will be modal. Otherwise, this thread will
     * block until the error frame has been shown and hidden on the EDT.</p>
     *
     * @param e Exception that contains information about the error cause and stack trace
     */
    public static void showFrame(Throwable e) {
        ErrorInfo ii = new ErrorInfo(null, null, null, null, e, null, null);
        showFrame(null, ii);
    }

    /**
     * <p>Constructs and shows the error frame, using the given
     * <code>ErrorInfo</code> to initialize the view.</p>
     *
     * <p>This method may be called from any thread. It will actually show the error
     * dialog on the AWT event dispatch thread. This method blocks. If called
     * on the EDT, the frame shown will be modal. Otherwise, this thread will
     * block until the error frame has been shown and hidden on the EDT.</p>
     *
     * @param owner Owner of this error frame. Determines the Window in which the frame
     *        is displayed; if the <code>owner</code> has
     *        no <code>Window</code>, a default <code>Frame</code> is used
     * @param info <code>ErrorInfo</code> that incorporates all the information about the error
     */
    public static void showFrame(Component owner, ErrorInfo info) {
        JXErrorPane pane = new JXErrorPane();
        pane.setErrorInfo(info);
        showFrame(owner, pane);
    }

    /**
     * <p>Constructs and shows the error frame, using the given
     * <code>JXErrorPane</code> for the view portion of the frame.</p>
     *
     * <p>This method may be called from any thread. It will actually show the error
     * dialog on the AWT event dispatch thread. This method blocks. If called
     * on the EDT, the frame shown will be modal. Otherwise, this thread will
     * block until the error frame has been shown and hidden on the EDT.</p>
     *
     * @param owner Owner of this error frame. Determines the Window in which the dialog
     *        is displayed; if the <code>owner</code> has
     *        no <code>Window</code>, a default <code>Frame</code> is used
     * @param pane <code>JXErrorPane</code> which will form the content area
     *        of the frame.
     */
    public static void showFrame(final Component owner, final JXErrorPane pane) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                JFrame window = createFrame(owner, pane);
                window.setVisible(true);
            }
        };

        if (!SwingUtilities.isEventDispatchThread()) {
            try {
                SwingUtilities.invokeAndWait(r);
            } catch (InvocationTargetException ex) {
                ex.printStackTrace();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        } else {
            r.run();
        }
    }

    /**
     * <p>Constructs and returns an error frame, using the given
     * <code>JXErrorPane</code> for the view portion of the frame.</p>
     *
     * <p>This method may be called from any thread. It does not block. The
     * caller is responsible for ensuring that the frame is shown and manipulated
     * on the AWT event dispatch thread. A common way to do this is to use
     * <code>SwingUtilities.invokeAndWait</code> or
     * <code>SwingUtilities.invokeLater()</code>.</p>
     *
     * @param owner Owner of this error frame. Determines the Window in which the frame
     *        is displayed; if the <code>owner</code> has
     *        no <code>Window</code>, a default <code>Frame</code> is used
     * @param pane <code>JXErrorPane</code> which will form the content area
     *        of the frame.
     * @return a <code>JFrame</code> configured to display the error.
     */
    public static JFrame createFrame(Component owner, JXErrorPane pane) {
        JFrame window = pane.getUI().getErrorFrame(owner);
        // If the owner is null applies orientation of the shared
        // hidden window used as owner.
        if(owner != null) {
            pane.applyComponentOrientation(owner.getComponentOrientation());
        } else {
            pane.applyComponentOrientation(window.getComponentOrientation());
        }
        window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        window.pack();
//        window.setLocationRelativeTo(owner);
        return window;
    }

    /**
     * <p>Constructs and shows the error frame for the given exception.  The
     * exceptions message will be the errorMessage, and the stacktrace will form
     * the details for the error dialog.</p>
     *
     * <p>This method may be called from any thread. It will actually show the error
     * dialog on the AWT event dispatch thread. This method blocks. If called
     * on the EDT, the frame shown will be modal. Otherwise, this thread will
     * block until the error frame has been shown and hidden on the EDT.</p>
     *
     * @param e Exception that contains information about the error cause and stack trace
     */
    public static void showInternalFrame(Throwable e) {
        ErrorInfo ii = new ErrorInfo(null, null, null, null, e, null, null);
        showInternalFrame(null, ii);
    }

    /**
     * <p>Constructs and shows the error frame, using the given
     * <code>ErrorInfo</code> to initialize the view.</p>
     *
     * <p>This method may be called from any thread. It will actually show the error
     * dialog on the AWT event dispatch thread. This method blocks. If called
     * on the EDT, the frame shown will be modal. Otherwise, this thread will
     * block until the error frame has been shown and hidden on the EDT.</p>
     *
     * @param owner Owner of this error frame. Determines the Window in which the frame
     *        is displayed; if the <code>owner</code> has
     *        no <code>Window</code>, a default <code>Frame</code> is used
     * @param info <code>ErrorInfo</code> that incorporates all the information about the error
     */
    public static void showInternalFrame(Component owner, ErrorInfo info) {
        JXErrorPane pane = new JXErrorPane();
        pane.setErrorInfo(info);
        showInternalFrame(owner, pane);
    }

    /**
     * <p>Constructs and shows the error frame, using the given
     * <code>JXErrorPane</code> for the view portion of the frame.</p>
     *
     * <p>This method may be called from any thread. It will actually show the error
     * dialog on the AWT event dispatch thread. This method blocks. If called
     * on the EDT, the frame shown will be modal. Otherwise, this thread will
     * block until the error frame has been shown and hidden on the EDT.</p>
     *
     * @param owner Owner of this error frame. Determines the Window in which the dialog
     *        is displayed; if the <code>owner</code> has
     *        no <code>Window</code>, a default <code>Frame</code> is used
     * @param pane <code>JXErrorPane</code> which will form the content area
     *        of the frame.
     */
    public static void showInternalFrame(final Component owner, final JXErrorPane pane) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                JInternalFrame window = createInternalFrame(owner, pane);
                window.setVisible(true);
            }
        };

        if (!SwingUtilities.isEventDispatchThread()) {
            try {
                SwingUtilities.invokeAndWait(r);
            } catch (InvocationTargetException ex) {
                ex.printStackTrace();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        } else {
            r.run();
        }
    }

    /**
     * <p>Constructs and returns an error frame, using the given
     * <code>JXErrorPane</code> for the view portion of the frame.</p>
     *
     * <p>This method may be called from any thread. It does not block. The
     * caller is responsible for ensuring that the frame is shown and manipulated
     * on the AWT event dispatch thread. A common way to do this is to use
     * <code>SwingUtilities.invokeAndWait</code> or
     * <code>SwingUtilities.invokeLater()</code>.</p>
     *
     * @param owner Owner of this error frame. Determines the Window in which the frame
     *    is displayed; if the <code>owner</code> has
     *    no <code>Window</code>, a default <code>Frame</code> is used
     * @param pane <code>JXErrorPane</code> which will form the content area
     *    of the frame.
     * @return a <code>JInternalFrame</code> configured to display the error.
     */
    public static JInternalFrame createInternalFrame(Component owner, JXErrorPane pane) {
        JInternalFrame window = pane.getUI().getErrorInternalFrame(owner);
        // If the owner is null applies orientation of the shared
        // hidden window used as owner.
        if(owner != null) {
            pane.applyComponentOrientation(owner.getComponentOrientation());
        } else {
            pane.applyComponentOrientation(window.getComponentOrientation());
        }
        window.setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
        window.pack();
        //TODO!
//                window.setLocationRelativeTo(owner);
        return window;
    }

}
