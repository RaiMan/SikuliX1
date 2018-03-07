/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractListModel;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.View;

import org.jdesktop.beans.JavaBean;
import org.jdesktop.swingx.action.AbstractActionExt;
import org.jdesktop.swingx.auth.DefaultUserNameStore;
import org.jdesktop.swingx.auth.LoginAdapter;
import org.jdesktop.swingx.auth.LoginEvent;
import org.jdesktop.swingx.auth.LoginListener;
import org.jdesktop.swingx.auth.LoginService;
import org.jdesktop.swingx.auth.PasswordStore;
import org.jdesktop.swingx.auth.UserNameStore;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import org.jdesktop.swingx.painter.MattePainter;
import org.jdesktop.swingx.plaf.LoginPaneAddon;
import org.jdesktop.swingx.plaf.LoginPaneUI;
import org.jdesktop.swingx.plaf.LookAndFeelAddons;
import org.jdesktop.swingx.plaf.UIManagerExt;
import org.jdesktop.swingx.plaf.basic.CapsLockSupport;
import org.jdesktop.swingx.util.WindowUtils;

/**
 *  <p>JXLoginPane is a specialized JPanel that implements a Login dialog with
 *  support for saving passwords supplied for future use in a secure
 *  manner. <strong>LoginService</strong> is invoked to perform authentication
 *  and optional <strong>PasswordStore</strong> can be provided to store the user
 *  login information.</p>
 *
 *  <p> In order to perform the authentication, <strong>JXLoginPane</strong>
 *  calls the <code>authenticate</code> method of the <strong>LoginService
 *  </strong>. In order to perform the persistence of the password,
 *  <strong>JXLoginPane</strong> calls the put method of the
 *  <strong>PasswordStore</strong> object that is supplied. If
 *  the <strong>PasswordStore</strong> is <code>null</code>, then the password
 *  is not saved. Similarly, if a <strong>PasswordStore</strong> is
 *  supplied and the password is null, then the <strong>PasswordStore</strong>
 *  will be queried for the password using the <code>get</code> method.
 *
 *  Example:
 *  <code><pre>
 *         final JXLoginPane panel = new JXLoginPane(new LoginService() {
 *                      public boolean authenticate(String name, char[] password,
 *                                      String server) throws Exception {
 *                              // perform authentication and return true on success.
 *                              return false;
 *                      }});
 *      final JFrame frame = JXLoginPane.showLoginFrame(panel);
 * </pre></code>
 *
 * @author Bino George
 * @author Shai Almog
 * @author rbair
 * @author Karl Schaefer
 * @author rah003
 * @author Jonathan Giles
 */
@JavaBean
public class JXLoginPane extends JXPanel {

    /**
     * The Logger
     */
    private static final Logger LOG = Logger.getLogger(JXLoginPane.class.getName());
    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 3544949969896288564L;
    /**
     * UI Class ID
     */
    public final static String uiClassID = "LoginPaneUI";
    /**
     * Action key for an Action in the ActionMap that initiates the Login
     * procedure
     */
    public static final String LOGIN_ACTION_COMMAND = "login";
    /**
     * Action key for an Action in the ActionMap that cancels the Login
     * procedure
     */
    public static final String CANCEL_LOGIN_ACTION_COMMAND = "cancel-login";
    /**
     * The JXLoginPane can attempt to save certain user information such as
     * the username, password, or both to their respective stores.
     * This type specifies what type of save should be performed.
     */
    public static enum SaveMode {NONE, USER_NAME, PASSWORD, BOTH}
    /**
     * Returns the status of the login process
     */
    public enum Status {NOT_STARTED, IN_PROGRESS, FAILED, CANCELLED, SUCCEEDED}
    /**
     * Used as a prefix when pulling data out of UIManager for i18n
     */
    private static String CLASS_NAME = JXLoginPane.class.getSimpleName();

    /**
     * The current login status for this panel
     */
    private Status status = Status.NOT_STARTED;
    /**
     * An optional banner at the top of the panel
     */
    private JXImagePanel banner;
    /**
     * Text that should appear on the banner
     */
    private String bannerText;
    /**
     * Custom label allowing the developer to display some message to the user
     */
    private JLabel messageLabel;
    /**
     * Shows an error message such as "user name or password incorrect" or
     * "could not contact server" or something like that if something
     * goes wrong
     */
    private JXLabel errorMessageLabel;
    /**
     * A Panel containing all of the input fields, check boxes, etc necessary
     * for the user to do their job. The items on this panel change whenever
     * the SaveMode changes, so this panel must be recreated at runtime if the
     * SaveMode changes. Thus, I must maintain this reference so I can remove
     * this panel from the content panel at runtime.
     */
    private JXPanel loginPanel;
    /**
     * The panel on which the input fields, messageLabel, and errorMessageLabel
     * are placed. While the login thread is running, this panel is removed
     * from the dialog and replaced by the progressPanel
     */
    private JXPanel contentPanel;
    /**
     * This is the area in which the name field is placed. That way it can toggle on the fly
     * between text field and a combo box depending on the situation, and have a simple
     * way to get the user name
     */
    private NameComponent namePanel;
    /**
     * The password field presented allowing the user to enter their password
     */
    private JPasswordField passwordField;
    /**
     * A combo box presenting the user with a list of servers to which they
     * may log in. This is an optional feature, which is only enabled if
     * the List of servers supplied to the JXLoginPane has a length greater
     * than 1.
     */
    private JComboBox serverCombo;
    /**
     * Check box presented if a PasswordStore is used, allowing the user to decide whether to
     * save their password
     */
    private JCheckBox saveCB;

    /**
     * Label displayed whenever caps lock is on.
     */
    private JLabel capsOn;
    /**
     * A special panel that displays a progress bar and cancel button, and
     * which notify the user of the login process, and allow them to cancel
     * that process.
     */
    private JXPanel progressPanel;
    /**
     * A JLabel on the progressPanel that is used for informing the user
     * of the status of the login procedure (logging in..., canceling login...)
     */
    private JLabel progressMessageLabel;
    /**
     * The LoginService to use. This must be specified for the login dialog to operate.
     * If no LoginService is defined, a default login service is used that simply
     * allows all users access. This is useful for demos or prototypes where a proper login
     * server is not available.
     */
    private LoginService loginService;
    /**
     * Optional: a PasswordStore to use for storing and retrieving passwords for a specific
     * user.
     */
    private PasswordStore passwordStore;
    /**
     * Optional: a UserNameStore to use for storing user names and retrieving them
     */
    private UserNameStore userNameStore;
    /**
     * A list of servers where each server is represented by a String. If the
     * list of Servers is greater than 1, then a combo box will be presented to
     * the user to choose from. If any servers are specified, the selected one
     * (or the only one if servers.size() == 1) will be passed to the LoginService
     */
    private List<String> servers;
    /**
     *  Whether to save password or username or both.
     */
    private SaveMode saveMode;
    /**
     * Tracks the cursor at the time that authentication was started, and restores to that
     * cursor after authentication ends, or is canceled;
     */
    private Cursor oldCursor;

    private boolean namePanelEnabled = true;

    /**
     * The default login listener used by this panel.
     */
    private LoginListener defaultLoginListener;

    /**
     * Login/cancel control pane;
     */
    private JXBtnPanel buttonPanel;

    /**
     * Card pane holding user/pwd fields view and the progress view.
     */
    private JPanel contentCardPane;
    private boolean isErrorMessageSet;

    /**
     * Creates a default JXLoginPane instance
     */
    static {
        LookAndFeelAddons.contribute(new LoginPaneAddon());
    }

    /**
     * Populates UIDefaults with the localizable Strings we will use
     * in the Login panel.
     */
    private void reinitLocales(Locale l) {
        // PENDING: JW - use the locale given as parameter
        // as this probably (?) should be called before super.setLocale
        setBannerText(UIManagerExt.getString(CLASS_NAME + ".bannerString", getLocale()));
        banner.setImage(createLoginBanner());
        if (!isErrorMessageSet) {
            errorMessageLabel.setText(UIManager.getString(CLASS_NAME + ".errorMessage", getLocale()));
        }
        progressMessageLabel.setText(UIManagerExt.getString(CLASS_NAME + ".pleaseWait", getLocale()));
        recreateLoginPanel();
        Window w = SwingUtilities.getWindowAncestor(this);
        if (w instanceof JXLoginFrame) {
            JXLoginFrame f = (JXLoginFrame) w;
            f.setTitle(UIManagerExt.getString(CLASS_NAME + ".titleString", getLocale()));
            if (buttonPanel != null) {
                buttonPanel.getOk().setText(UIManagerExt.getString(CLASS_NAME + ".loginString", getLocale()));
                buttonPanel.getCancel().setText(UIManagerExt.getString(CLASS_NAME + ".cancelString", getLocale()));
            }
        }
        JLabel lbl = (JLabel) passwordField.getClientProperty("labeledBy");
        if (lbl != null) {
            lbl.setText(UIManagerExt.getString(CLASS_NAME + ".passwordString", getLocale()));
        }
        lbl = (JLabel) namePanel.getComponent().getClientProperty("labeledBy");
        if (lbl != null) {
            lbl.setText(UIManagerExt.getString(CLASS_NAME + ".nameString", getLocale()));
        }
        if (serverCombo != null) {
            lbl = (JLabel) serverCombo.getClientProperty("labeledBy");
            if (lbl != null) {
                lbl.setText(UIManagerExt.getString(CLASS_NAME + ".serverString", getLocale()));
            }
        }
        saveCB.setText(UIManagerExt.getString(CLASS_NAME + ".rememberPasswordString", getLocale()));
        // by default, caps is initialized in off state - i.e. without warning. Setting to
        // whitespace preserves formatting of the panel.
        capsOn.setText(isCapsLockOn() ? UIManagerExt.getString(CLASS_NAME + ".capsOnWarning", getLocale()) : " ");

        getActionMap().get(LOGIN_ACTION_COMMAND).putValue(Action.NAME, UIManagerExt.getString(CLASS_NAME + ".loginString", getLocale()));
        getActionMap().get(CANCEL_LOGIN_ACTION_COMMAND).putValue(Action.NAME, UIManagerExt.getString(CLASS_NAME + ".cancelString", getLocale()));

    }

    //--------------------------------------------------------- Constructors
    /**
     * Create a {@code JXLoginPane} that always accepts the user, never stores
     * passwords or user ids, and has no target servers.
     * <p>
     * This constructor should <i>NOT</i> be used in a real application. It is
     * provided for compliance to the bean specification and for use with visual
     * editors.
     */
    public JXLoginPane() {
        this(null);
    }

    /**
     * Create a {@code JXLoginPane} with the specified {@code LoginService}
     * that does not store user ids or passwords and has no target servers.
     *
     * @param service
     *            the {@code LoginService} to use for logging in
     */
    public JXLoginPane(LoginService service) {
        this(service, null, null);
    }

    /**
     * Create a {@code JXLoginPane} with the specified {@code LoginService},
     * {@code PasswordStore}, and {@code UserNameStore}, but without a server
     * list.
     * <p>
     * If you do not want to store passwords or user ids, those parameters can
     * be {@code null}. {@code SaveMode} is autoconfigured from passed in store
     * parameters.
     *
     * @param service
     *            the {@code LoginService} to use for logging in
     * @param passwordStore
     *            the {@code PasswordStore} to use for storing password
     *            information
     * @param userStore
     *            the {@code UserNameStore} to use for storing user information
     */
    public JXLoginPane(LoginService service, PasswordStore passwordStore, UserNameStore userStore) {
        this(service, passwordStore, userStore, null);
    }

    /**
     * Create a {@code JXLoginPane} with the specified {@code LoginService},
     * {@code PasswordStore}, {@code UserNameStore}, and server list.
     * <p>
     * If you do not want to store passwords or user ids, those parameters can
     * be {@code null}. {@code SaveMode} is autoconfigured from passed in store
     * parameters.
     * <p>
     * Setting the server list to {@code null} will unset all of the servers.
     * The server list is guaranteed to be non-{@code null}.
     *
     * @param service
     *            the {@code LoginService} to use for logging in
     * @param passwordStore
     *            the {@code PasswordStore} to use for storing password
     *            information
     * @param userStore
     *            the {@code UserNameStore} to use for storing user information
     * @param servers
     *            a list of servers to authenticate against
     */
    public JXLoginPane(LoginService service, PasswordStore passwordStore, UserNameStore userStore, List<String> servers) {
        setLoginService(service);
        setPasswordStore(passwordStore);
        setUserNameStore(userStore);
        setServers(servers);

        //create the login and cancel actions, and add them to the action map
        getActionMap().put(LOGIN_ACTION_COMMAND, createLoginAction());
        getActionMap().put(CANCEL_LOGIN_ACTION_COMMAND, createCancelAction());

        //initialize the save mode
        if (passwordStore != null && userStore != null) {
            saveMode = SaveMode.BOTH;
        } else if (passwordStore != null) {
            saveMode = SaveMode.PASSWORD;
        } else if (userStore != null) {
            saveMode = SaveMode.USER_NAME;
        } else {
            saveMode = SaveMode.NONE;
        }

        // #732 set all internal components opacity to false in order to allow top level (frame's content pane) background painter to have any effect.
        setOpaque(false);
        CapsLockSupport.getInstance().addPropertyChangeListener("capsLockEnabled", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (capsOn != null) {
                    if (Boolean.TRUE.equals(evt.getNewValue())) {
                        capsOn.setText(UIManagerExt.getString(CLASS_NAME + ".capsOnWarning", getLocale()));
                    } else {
                        capsOn.setText(" ");
                    }
                }
            }
        });
        initComponents();
    }

    /**
     * Gets current state of the caps lock as seen by the login panel. The state seen by the login
     * panel and therefore returned by this method can be delayed in comparison to the real caps
     * lock state and displayed by the keyboard light. This is usually the case when component or
     * its text fields are not focused.
     *
     * @return True when caps lock is on, false otherwise. Returns always false when
     * <code>isCapsLockDetectionSupported()</code> returns false.
     */
    public boolean isCapsLockOn() {
        return CapsLockSupport.getInstance().isCapsLockEnabled();
    }

    //------------------------------------------------------------- UI Logic

    /**
     * {@inheritDoc}
     */
    @Override
    public LoginPaneUI getUI() {
        return (LoginPaneUI) super.getUI();
    }

    /**
     * Sets the look and feel (L&F) object that renders this component.
     *
     * @param ui the LoginPaneUI L&F object
     * @see javax.swing.UIDefaults#getUI
     */
    public void setUI(LoginPaneUI ui) {
        // initialized here due to implicit updateUI call from JPanel
        if (banner == null) {
            banner = new JXImagePanel();
        }
        if (errorMessageLabel == null) {
            errorMessageLabel = new JXLabel(UIManagerExt.getString(CLASS_NAME + ".errorMessage", getLocale()));
        }
        super.setUI(ui);
        banner.setImage(createLoginBanner());
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
        setUI((LoginPaneUI) LookAndFeelAddons.getUI(this, LoginPaneUI.class));
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
     * Recreates the login panel, and replaces the current one with the new one
     */
    protected void recreateLoginPanel() {
        JXPanel old = loginPanel;
        loginPanel = createLoginPanel();
        loginPanel.setBorder(BorderFactory.createEmptyBorder(0, 36, 7, 11));
        contentPanel.remove(old);
        contentPanel.add(loginPanel, 1);
    }

    /**
     * Creates and returns a new LoginPanel, based on the SaveMode state of
     * the login panel. Whenever the SaveMode changes, the panel is recreated.
     * I do this rather than hiding/showing components, due to a cleaner
     * implementation (no invisible components, components are not sharing
     * locations in the LayoutManager, etc).
     */
    private JXPanel createLoginPanel() {
        JXPanel loginPanel = new JXPanel();

        JPasswordField oldPwd = passwordField;
        //create the password component
        passwordField = new JPasswordField("", 15);
        JLabel passwordLabel = new JLabel(UIManagerExt.getString(CLASS_NAME + ".passwordString", getLocale()));
        passwordLabel.setLabelFor(passwordField);
        if (oldPwd != null) {
            passwordField.setText(new String(oldPwd.getPassword()));
        }

        NameComponent oldPanel = namePanel;
        //create the NameComponent
        if (saveMode == SaveMode.NONE) {
            namePanel = new SimpleNamePanel();
        } else {
            namePanel = new ComboNamePanel();
        }
        if (oldPanel != null) {
            // need to reset here otherwise value will get lost during LAF change as panel gets recreated.
            namePanel.setUserName(oldPanel.getUserName());
            namePanel.setEnabled(oldPanel.isEnabled());
            namePanel.setEditable(oldPanel.isEditable());
        } else {
            namePanel.setEnabled(namePanelEnabled);
            namePanel.setEditable(namePanelEnabled);
        }
        JLabel nameLabel = new JLabel(UIManagerExt.getString(CLASS_NAME + ".nameString", getLocale()));
        nameLabel.setLabelFor(namePanel.getComponent());

        //create the server combo box if necessary
        JLabel serverLabel = new JLabel(UIManagerExt.getString(CLASS_NAME + ".serverString", getLocale()));
        if (servers.size() > 1) {
            serverCombo = new JComboBox(servers.toArray());
            serverLabel.setLabelFor(serverCombo);
        } else {
            serverCombo = null;
        }

        //create the save check box. By default, it is not selected
        saveCB = new JCheckBox(UIManagerExt.getString(CLASS_NAME + ".rememberPasswordString", getLocale()));
        saveCB.setIconTextGap(10);
        //TODO should get this from preferences!!! And, it should be based on the user
        saveCB.setSelected(false);
        //determine whether to show/hide the save check box based on the SaveMode
        saveCB.setVisible(saveMode == SaveMode.PASSWORD || saveMode == SaveMode.BOTH);
        saveCB.setOpaque(false);

        capsOn = new JLabel();
        capsOn.setText(isCapsLockOn() ? UIManagerExt.getString(CLASS_NAME + ".capsOnWarning", getLocale()) : " ");

        int lShift = 3;// lShift is used to align all other components with the checkbox
        GridLayout grid = new GridLayout(2,1);
        grid.setVgap(5);
        JPanel fields = new JPanel(grid);
        fields.setOpaque(false);
        fields.add(namePanel.getComponent());
        fields.add(passwordField);

        loginPanel.setLayout(new GridBagLayout());
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new Insets(4, lShift, 5, 11);
        loginPanel.add(nameLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(0, 0, 5, 0);
        loginPanel.add(fields, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new Insets(5, lShift, 5, 11);
        loginPanel.add(passwordLabel, gridBagConstraints);

        if (serverCombo != null) {
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 2;
            gridBagConstraints.anchor = GridBagConstraints.LINE_START;
            gridBagConstraints.insets = new Insets(0, lShift, 5, 11);
            loginPanel.add(serverLabel, gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = 2;
            gridBagConstraints.gridwidth = 1;
            gridBagConstraints.anchor = GridBagConstraints.LINE_START;
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.insets = new Insets(0, 0, 5, 0);
            loginPanel.add(serverCombo, gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 3;
            gridBagConstraints.gridwidth = 2;
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.anchor = GridBagConstraints.LINE_START;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.insets = new Insets(0, 0, 4, 0);
            loginPanel.add(saveCB, gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 4;
            gridBagConstraints.gridwidth = 2;
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.anchor = GridBagConstraints.LINE_START;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.insets = new Insets(0, lShift, 0, 11);
            loginPanel.add(capsOn, gridBagConstraints);
        } else {
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 2;
            gridBagConstraints.gridwidth = 2;
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.anchor = GridBagConstraints.LINE_START;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.insets = new Insets(0, 0, 4, 0);
            loginPanel.add(saveCB, gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 3;
            gridBagConstraints.gridwidth = 2;
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.anchor = GridBagConstraints.LINE_START;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.insets = new Insets(0, lShift, 0, 11);
            loginPanel.add(capsOn, gridBagConstraints);
        }
        loginPanel.setOpaque(false);
        return loginPanel;
    }

    /**
     * This method adds functionality to support bidi languages within this
     * component
     */
    @Override
    public void setComponentOrientation(ComponentOrientation orient) {
        // this if is used to avoid needless creations of the image
        if(orient != super.getComponentOrientation()) {
            super.setComponentOrientation(orient);
            banner.setImage(createLoginBanner());
            progressPanel.applyComponentOrientation(orient);
        }
    }

    /**
     * Create all of the UI components for the login panel
     */
    private void initComponents() {
        //create the default banner
        banner.setImage(createLoginBanner());

        //create the default label
        messageLabel = new JLabel(" ");
        messageLabel.setOpaque(false);
        messageLabel.setFont(messageLabel.getFont().deriveFont(Font.BOLD));

        //create the main components
        loginPanel = createLoginPanel();

        //create the message and hyperlink and hide them
        errorMessageLabel.setIcon(UIManager.getIcon(CLASS_NAME + ".errorIcon", getLocale()));
        errorMessageLabel.setVerticalTextPosition(SwingConstants.TOP);
        errorMessageLabel.setLineWrap(true);
        errorMessageLabel.setPaintBorderInsets(false);
        errorMessageLabel.setBackgroundPainter(new MattePainter(UIManager.getColor(CLASS_NAME + ".errorBackground", getLocale()), true));
        errorMessageLabel.setMaxLineSpan(320);
        errorMessageLabel.setVisible(false);

        //aggregate the optional message label, content, and error label into
        //the contentPanel
        contentPanel = new JXPanel(new LoginPaneLayout());
        contentPanel.setOpaque(false);
        messageLabel.setBorder(BorderFactory.createEmptyBorder(12, 12, 7, 11));
        contentPanel.add(messageLabel);
        loginPanel.setBorder(BorderFactory.createEmptyBorder(0, 36, 7, 11));
        contentPanel.add(loginPanel);
        errorMessageLabel.setBorder(UIManager.getBorder(CLASS_NAME + ".errorBorder", getLocale()));
        contentPanel.add(errorMessageLabel);

        //create the progress panel
        progressPanel = new JXPanel(new GridBagLayout());
        progressPanel.setOpaque(false);
        progressMessageLabel = new JLabel(UIManagerExt.getString(CLASS_NAME + ".pleaseWait", getLocale()));
        progressMessageLabel.setFont(UIManager.getFont(CLASS_NAME +".pleaseWaitFont", getLocale()));
        JProgressBar pb = new JProgressBar();
        pb.setIndeterminate(true);
        JButton cancelButton = new JButton(getActionMap().get(CANCEL_LOGIN_ACTION_COMMAND));
        progressPanel.add(progressMessageLabel, new GridBagConstraints(0, 0, 2, 1, 1.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(12, 12, 11, 11), 0, 0));
        progressPanel.add(pb, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 24, 11, 7), 0, 0));
        progressPanel.add(cancelButton, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 11, 11), 0, 0));

        //layout the panel
        setLayout(new BorderLayout());
        add(banner, BorderLayout.NORTH);
        contentCardPane = new JPanel(new CardLayout());
        contentCardPane.setOpaque(false);
        contentCardPane.add(contentPanel, "0");
        contentCardPane.add(progressPanel, "1");
        add(contentCardPane, BorderLayout.CENTER);

    }

    private final class LoginPaneLayout extends VerticalLayout implements LayoutManager {
        @Override
	public Dimension preferredLayoutSize(Container parent) {
            Insets insets = parent.getInsets();
            Dimension pref = new Dimension(0, 0);
            int gap = getGap();
            for (int i = 0, c = parent.getComponentCount(); i < c; i++) {
              Component m = parent.getComponent(i);
              if (m.isVisible()) {
                Dimension componentPreferredSize = m.getPreferredSize();
                // swingx-917 - don't let jlabel to force width due to long text
                if (m instanceof JLabel) {
                    View view = (View) ((JLabel)m).getClientProperty(BasicHTML.propertyKey);
                    if (view != null) {
                        view.setSize(pref.width, m.getHeight());
                        // get fresh preferred size since we have forced new size on label
                        componentPreferredSize = m.getPreferredSize();
                    }
                } else {
                    pref.width = Math.max(pref.width, componentPreferredSize.width);
                }
                pref.height += componentPreferredSize.height + gap;
              }
            }

            pref.width += insets.left + insets.right;
            pref.height += insets.top + insets.bottom;

            return pref;
          }
    }

    /**
     * Create and return an image to use for the Banner. This may be overridden
     * to return any image you like
     */
    protected Image createLoginBanner() {
        return getUI() == null ? null : getUI().getBanner();
    }

    /**
     * Create and return an Action for logging in
     */
    protected Action createLoginAction() {
        return new LoginAction(this);
    }

    /**
     * Create and return an Action for canceling login
     */
    protected Action createCancelAction() {
        return new CancelAction(this);
    }

    //------------------------------------------------------ Bean Properties
    //REMEMBER: when adding new methods, they need to fire property change events!!!
    /**
     * @return Returns the saveMode.
     */
    public SaveMode getSaveMode() {
        return saveMode;
    }

    /**
     * The save mode indicates whether the "save" password is checked by default. This method
     * makes no difference if the passwordStore is null.
     *
     * @param saveMode The saveMode to set either SAVE_NONE, SAVE_PASSWORD or SAVE_USERNAME
     */
    public void setSaveMode(SaveMode saveMode) {
        if (this.saveMode != saveMode) {
            SaveMode oldMode = getSaveMode();
            this.saveMode = saveMode;
            recreateLoginPanel();
            firePropertyChange("saveMode", oldMode, getSaveMode());
        }
    }

    public boolean isRememberPassword() {
	return saveCB.isVisible() && saveCB.isSelected();
    }

    /**
     * @return the List of servers
     */
    public List<String> getServers() {
        return Collections.unmodifiableList(servers);
    }

    /**
     * Sets the list of servers. See the servers field javadoc for more info.
     */
    public void setServers(List<String> servers) {
        //only at startup
        if (this.servers == null) {
            this.servers = servers == null ? new ArrayList<String>() : servers;
        } else if (this.servers != servers) {
            List<String> old = getServers();
            this.servers = servers == null ? new ArrayList<String>() : servers;
            recreateLoginPanel();
            firePropertyChange("servers", old, getServers());
        }
    }

    private LoginListener getDefaultLoginListener() {
        if (defaultLoginListener == null) {
            defaultLoginListener = new LoginListenerImpl();
        }

        return defaultLoginListener;
    }

    /**
     * Sets the {@code LoginService} for this panel. Setting the login service
     * to {@code null} will actually set the service to use
     * {@code NullLoginService}.
     *
     * @param service
     *            the service to set. If {@code service == null}, then a
     *            {@code NullLoginService} is used.
     */
    public void setLoginService(LoginService service) {
        LoginService oldService = getLoginService();
        LoginService newService = service == null ? new NullLoginService() : service;

        //newService is guaranteed to be nonnull
        if (!newService.equals(oldService)) {
            if (oldService != null) {
                oldService.removeLoginListener(getDefaultLoginListener());
            }

            loginService = newService;
            this.loginService.addLoginListener(getDefaultLoginListener());

            firePropertyChange("loginService", oldService, getLoginService());
        }
    }

    /**
     * Gets the <strong>LoginService</strong> for this panel.
     *
     * @return service service
     */
    public LoginService getLoginService() {
        return loginService;
    }

    /**
     * Sets the <strong>PasswordStore</strong> for this panel.
     *
     * @param store PasswordStore
     */
    public void setPasswordStore(PasswordStore store) {
        PasswordStore oldStore = getPasswordStore();
        PasswordStore newStore = store == null ? new NullPasswordStore() : store;

        //newStore is guaranteed to be nonnull
        if (!newStore.equals(oldStore)) {
            passwordStore = newStore;

            firePropertyChange("passwordStore", oldStore, getPasswordStore());
        }
    }

    /**
     * Gets the {@code UserNameStore} for this panel.
     *
     * @return the {@code UserNameStore}
     */
    public UserNameStore getUserNameStore() {
        return userNameStore;
    }

    /**
     * Sets the user name store for this panel.
     * @param store
     */
    public void setUserNameStore(UserNameStore store) {
        UserNameStore oldStore = getUserNameStore();
        UserNameStore newStore = store == null ? new DefaultUserNameStore() : store;

        //newStore is guaranteed to be nonnull
        if (!newStore.equals(oldStore)) {
            userNameStore = newStore;

            firePropertyChange("userNameStore", oldStore, getUserNameStore());
        }
    }

    /**
     * Gets the <strong>PasswordStore</strong> for this panel.
     *
     * @return store PasswordStore
     */
    public PasswordStore getPasswordStore() {
        return passwordStore;
    }

    /**
     * Sets the <strong>User name</strong> for this panel.
     *
     * @param username User name
     */
    public void setUserName(String username) {
        if (namePanel != null) {
            String old = getUserName();
            namePanel.setUserName(username);
            firePropertyChange("userName", old, getUserName());
        }
    }

    /**
     * Enables or disables <strong>User name</strong> for this panel.
     *
     * @param enabled
     */
    public void setUserNameEnabled(boolean enabled) {
        boolean old = isUserNameEnabled();
        this.namePanelEnabled = enabled;
        if (namePanel != null) {
            namePanel.setEnabled(enabled);
            namePanel.setEditable(enabled);
        }
        firePropertyChange("userNameEnabled", old, isUserNameEnabled());
    }

    /**
     * Gets current state of the user name field. Field can be either disabled (false) for editing or enabled (true).
     * @return True when user name field is enabled and editable, false otherwise.
     */
    public boolean isUserNameEnabled() {
        return this.namePanelEnabled;
    }

    /**
     * Gets the <strong>User name</strong> for this panel.
     * @return the user name
     */
    public String getUserName() {
        return namePanel == null ? null : namePanel.getUserName();
    }

    /**
     * Sets the <strong>Password</strong> for this panel.
     *
     * @param password Password
     */
    public void setPassword(char[] password) {
        passwordField.setText(new String(password));
    }

    /**
     * Gets the <strong>Password</strong> for this panel.
     *
     * @return password Password
     */
    public char[] getPassword() {
        return passwordField.getPassword();
    }

    /**
     * Return the image used as the banner
     */
    public Image getBanner() {
        return banner.getImage();
    }

    /**
     * Set the image to use for the banner. If the {@code img} is {@code null},
     * then no image will be displayed.
     *
     * @param img
     *            the image to display
     */
    public void setBanner(Image img) {
        // we do not expose the ImagePanel, so we will produce property change
        // events here
        Image oldImage = getBanner();

        if (oldImage != img) {
            banner.setImage(img);
            firePropertyChange("banner", oldImage, getBanner());
        }
    }

    /**
     * Set the text to use when creating the banner. If a custom banner image is
     * specified, then this is ignored. If {@code text} is {@code null}, then
     * no text is displayed.
     *
     * @param text
     *            the text to display
     */
    public void setBannerText(String text) {
        if (text == null) {
            text = "";
        }

        if (!text.equals(this.bannerText)) {
            String oldText = this.bannerText;
            this.bannerText = text;
            //fix the login banner
            this.banner.setImage(createLoginBanner());
            firePropertyChange("bannerText", oldText, text);
        }
    }

    /**
     * Returns text used when creating the banner
     */
    public String getBannerText() {
        return bannerText;
    }

    /**
     * Returns the custom message for this login panel
     */
    public String getMessage() {
        return messageLabel.getText();
    }

    /**
     * Sets a custom message for this login panel
     */
    public void setMessage(String message) {
        String old = messageLabel.getText();
        messageLabel.setText(message);
        firePropertyChange("message", old, messageLabel.getText());
    }

    /**
     * Returns the error message for this login panel
     */
    public String getErrorMessage() {
        return errorMessageLabel.getText();
    }

    /**
     * Sets the error message for this login panel
     */
    public void setErrorMessage(String errorMessage) {
        isErrorMessageSet = true;
        String old = errorMessageLabel.getText();
        errorMessageLabel.setText(errorMessage);
        firePropertyChange("errorMessage", old, errorMessageLabel.getText());
    }

    /**
     * Returns the panel's status
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Change the status
     */
    protected void setStatus(Status newStatus) {
        if (status != newStatus) {
            Status oldStatus = status;
            status = newStatus;
            firePropertyChange("status", oldStatus, newStatus);
        }
    }

    @Override
    public void setLocale(Locale l) {
        super.setLocale(l);
        reinitLocales(l);
    }
    //-------------------------------------------------------------- Methods

    /**
     * Initiates the login procedure. This method is called internally by
     * the LoginAction. This method handles cursor management, and actually
     * calling the LoginService's startAuthentication method. Method will return
     * immediately if asynchronous login is enabled or will block until
     * authentication finishes if <code>getSynchronous()</code> returns true.
     */
    protected void startLogin() {
        oldCursor = getCursor();
        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            progressMessageLabel.setText(UIManagerExt.getString(CLASS_NAME + ".pleaseWait", getLocale()));
            String name = getUserName();
            char[] password = getPassword();
            String server = servers.size() == 1 ? servers.get(0) : serverCombo == null ? null : (String)serverCombo.getSelectedItem();

            loginService.startAuthentication(name, password, server);
        } catch(Exception ex) {
        //The status is set via the loginService listener, so no need to set
        //the status here. Just log the error.
        LOG.log(Level.WARNING, "Authentication exception while logging in", ex);
        } finally {
            setCursor(oldCursor);
        }
    }

    /**
     * Cancels the login procedure. Handles cursor management and interfacing
     * with the LoginService's cancelAuthentication method. Calling this method
     * has an effect only when authentication is still in progress (i.e. after
     * previous call to <code>startAuthentications()</code> and only when
     * authentication is performed asynchronously (<code>getSynchronous()</code>
     * returns false).
     */
    protected void cancelLogin() {
        progressMessageLabel.setText(UIManagerExt.getString(CLASS_NAME + ".cancelWait", getLocale()));
        getActionMap().get(CANCEL_LOGIN_ACTION_COMMAND).setEnabled(false);
        loginService.cancelAuthentication();
        setCursor(oldCursor);
    }

    /**
     * Puts the password into the password store. If password store is not set, method will do
     * nothing.
     */
    protected void savePassword() {
        if (saveCB.isSelected()
            && (saveMode == SaveMode.BOTH || saveMode == SaveMode.PASSWORD)
            && passwordStore != null) {
            passwordStore.set(getUserName(),getLoginService().getServer(),getPassword());
        }
    }

    //--------------------------------------------- Listener Implementations
    /*

     For Login (initiated in LoginAction):
        0) set the status
        1) Immediately disable the login action
        2) Immediately disable the close action (part of enclosing window)
        3) initialize the progress pane
          a) enable the cancel login action
          b) set the message text
        4) hide the content pane, show the progress pane

     When cancelling (initiated in CancelAction):
         0) set the status
         1) Disable the cancel login action
         2) Change the message text on the progress pane

     When cancel finishes (handled in LoginListener):
         0) set the status
         1) hide the progress pane, show the content pane
         2) enable the close action (part of enclosing window)
         3) enable the login action

     When login fails (handled in LoginListener):
         0) set the status
         1) hide the progress pane, show the content pane
         2) enable the close action (part of enclosing window)
         3) enable the login action
         4) Show the error message
         5) resize the window (part of enclosing window)

     When login succeeds (handled in LoginListener):
         0) set the status
         1) close the dialog/frame (part of enclosing window)
     */
    /**
     * Listener class to track state in the LoginService
     */
    protected class LoginListenerImpl extends LoginAdapter {
        @Override
	public void loginSucceeded(LoginEvent source) {
            //save the user names and passwords
            String userName = namePanel.getUserName();
            if ((getSaveMode() == SaveMode.USER_NAME || getSaveMode() == SaveMode.BOTH)
                    && userName != null && !userName.trim().equals("")) {
                userNameStore.addUserName(userName);
                userNameStore.saveUserNames();
            }

            // if the user and/or password store knows of this user,
            // and the checkbox is unchecked, we remove them, otherwise
            // we save the password
            if (saveCB.isSelected()) {
        	savePassword();
            } else {
        	// remove the password from the password store
        	if (passwordStore != null) {
        	    passwordStore.removeUserPassword(userName);
        	}
            }

            setStatus(Status.SUCCEEDED);
        }

        @Override
	public void loginStarted(LoginEvent source) {
            assert EventQueue.isDispatchThread();
            getActionMap().get(LOGIN_ACTION_COMMAND).setEnabled(false);
            getActionMap().get(CANCEL_LOGIN_ACTION_COMMAND).setEnabled(true);
//            remove(contentPanel);
//            add(progressPanel, BorderLayout.CENTER);
            ((CardLayout) contentCardPane.getLayout()).last(contentCardPane);
            revalidate();
            repaint();
            setStatus(Status.IN_PROGRESS);
        }

        @Override
	public void loginFailed(LoginEvent source) {
            assert EventQueue.isDispatchThread();
//            remove(progressPanel);
//            add(contentPanel, BorderLayout.CENTER);
            ((CardLayout) contentCardPane.getLayout()).first(contentCardPane);
            getActionMap().get(LOGIN_ACTION_COMMAND).setEnabled(true);
            errorMessageLabel.setVisible(true);
            revalidate();
            repaint();
            setStatus(Status.FAILED);
        }

        @Override
	public void loginCanceled(LoginEvent source) {
            assert EventQueue.isDispatchThread();
//            remove(progressPanel);
//            add(contentPanel, BorderLayout.CENTER);
            ((CardLayout) contentCardPane.getLayout()).first(contentCardPane);
            getActionMap().get(LOGIN_ACTION_COMMAND).setEnabled(true);
            errorMessageLabel.setVisible(false);
            revalidate();
            repaint();
            setStatus(Status.CANCELLED);
        }
    }

    //---------------------------------------------- Default Implementations
    /**
     * Action that initiates a login procedure. Delegates to JXLoginPane.startLogin
     */
    private static final class LoginAction extends AbstractActionExt {
        private static final long serialVersionUID = 7256761187925982485L;
        private JXLoginPane panel;
        public LoginAction(JXLoginPane p) {
            super(UIManagerExt.getString(CLASS_NAME + ".loginString", p.getLocale()), LOGIN_ACTION_COMMAND);
            this.panel = p;
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            panel.startLogin();
        }
        @Override
	public void itemStateChanged(ItemEvent e) {}
    }

    /**
     * Action that cancels the login procedure.
     */
    private static final class CancelAction extends AbstractActionExt {
        private static final long serialVersionUID = 4040029973355439229L;
        private JXLoginPane panel;
        public CancelAction(JXLoginPane p) {
            super(UIManagerExt.getString(CLASS_NAME + ".cancelLogin", p.getLocale()), CANCEL_LOGIN_ACTION_COMMAND);
            this.panel = p;
            this.setEnabled(false);
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            panel.cancelLogin();
        }
        @Override
        public void itemStateChanged(ItemEvent e) {}
    }

    /**
     * Simple login service that allows everybody to login. This is useful in demos and allows
     * us to avoid having to check for LoginService being null
     */
    private static final class NullLoginService extends LoginService {
        @Override
        public boolean authenticate(String name, char[] password, String server) throws Exception {
            return true;
        }

	@Override
        public boolean equals(Object obj) {
            return obj instanceof NullLoginService;
        }

	@Override
        public int hashCode() {
            return 7;
        }
    }

    /**
     * Simple PasswordStore that does not remember passwords
     */
    private static final class NullPasswordStore extends PasswordStore {
	@Override
        public boolean set(String username, String server, char[] password) {
            //null op
            return false;
        }

	@Override
        public char[] get(String username, String server) {
            return new char[0];
        }

	@Override
	public void removeUserPassword(String username) {
	    return;
	}

	@Override
        public boolean equals(Object obj) {
            return obj instanceof NullPasswordStore;
        }

	@Override
        public int hashCode() {
            return 7;
        }
    }

    //--------------------------------- Default NamePanel Implementations
    private static interface NameComponent {
        public String getUserName();
        public boolean isEnabled();
        public boolean isEditable();
        public void setEditable(boolean enabled);
        public void setEnabled(boolean enabled);
        public void setUserName(String userName);
        public JComponent getComponent();
    }

    private void updatePassword(final String username) {
        String password = "";
        if (username != null) {
    		char[] pw = passwordStore.get(username, null);
    		password = pw == null ? "" : new String(pw);

    		// if the userstore has this username, we should change the
    		// 'remember me' checkbox to be selected. Unselecting this will
    		// result in the user being 'forgotten'.
    		saveCB.setSelected(userNameStore.containsUserName(username));
        }

        passwordField.setText(password);
    }

    /**
     * If a UserNameStore is not used, then this text field is presented allowing the user
     * to simply enter their user name
     */
    private final class SimpleNamePanel extends JTextField implements NameComponent {
        private static final long serialVersionUID = 6513437813612641002L;

	public SimpleNamePanel() {
	    super("", 15);

	    // auto-complete based on the users input
	    // AutoCompleteDecorator.decorate(this, Arrays.asList(userNameStore.getUserNames()), false);

	    // listen to text input, and offer password suggestion based on current
	    // text
	    if (passwordStore != null && passwordField!=null) {
		addKeyListener(new KeyAdapter() {
		    @Override
		    public void keyReleased(KeyEvent e) {
			updatePassword(getText());
		    }
		});
	    }
	}

        @Override
        public String getUserName() {
            return getText();
        }
        @Override
        public void setUserName(String userName) {
            setText(userName);
        }
        @Override
        public JComponent getComponent() {
            return this;
        }
    }

    /**
     * If a UserNameStore is used, then this combo box is presented allowing the user
     * to select a previous login name, or type in a new login name
     */
    private final class ComboNamePanel extends JComboBox implements NameComponent {
        private static final long serialVersionUID = 2511649075486103959L;

        public ComboNamePanel() {
            super();
            setModel(new NameComboBoxModel());
            setEditable(true);

            // auto-complete based on the users input
            AutoCompleteDecorator.decorate(this);

            // listen to selection or text input, and offer password suggestion based on current
            // text
            if (passwordStore != null && passwordField!=null) {
        	final JTextField textfield = (JTextField) getEditor().getEditorComponent();
        	textfield.addKeyListener(new KeyAdapter() {
        	    @Override
        	    public void keyReleased(KeyEvent e) {
        		updatePassword(textfield.getText());
        	    }
        	});

        	super.addItemListener(new ItemListener() {
        	    @Override
                public void itemStateChanged(ItemEvent e) {
        		updatePassword((String)getSelectedItem());
        	    }
        	});
            }
        }

        @Override
        public String getUserName() {
            Object item = getModel().getSelectedItem();
            return item == null ? null : item.toString();
        }
        @Override
        public void setUserName(String userName) {
            getModel().setSelectedItem(userName);
        }
        public void setUserNames(String[] names) {
            setModel(new DefaultComboBoxModel(names));
        }
        @Override
        public JComponent getComponent() {
            return this;
        }

        private final class NameComboBoxModel extends AbstractListModel implements ComboBoxModel {
            private static final long serialVersionUID = 7097674687536018633L;
            private Object selectedItem;
            @Override
            public void setSelectedItem(Object anItem) {
                selectedItem = anItem;
                fireContentsChanged(this, -1, -1);
            }
            @Override
            public Object getSelectedItem() {
                return selectedItem;
            }
            @Override
            public Object getElementAt(int index) {
                if (index == -1) {
                    return null;
                }

                return userNameStore.getUserNames()[index];
            }
            @Override
            public int getSize() {
                return userNameStore.getUserNames().length;
            }
        }
    }

    //------------------------------------------ Static Construction Methods
    /**
     * Shows a login dialog. This method blocks.
     * @return The status of the login operation
     */
    public static Status showLoginDialog(Component parent, LoginService svc) {
        return showLoginDialog(parent, svc, null, null);
    }

    /**
     * Shows a login dialog. This method blocks.
     * @return The status of the login operation
     */
    public static Status showLoginDialog(Component parent, LoginService svc, PasswordStore ps, UserNameStore us) {
        return showLoginDialog(parent, svc, ps, us, null);
    }

    /**
     * Shows a login dialog. This method blocks.
     * @return The status of the login operation
     */
    public static Status showLoginDialog(Component parent, LoginService svc, PasswordStore ps, UserNameStore us, List<String> servers) {
        JXLoginPane panel = new JXLoginPane(svc, ps, us, servers);
        return showLoginDialog(parent, panel);
    }

    /**
     * Shows a login dialog. This method blocks.
     * @return The status of the login operation
     */
    public static Status showLoginDialog(Component parent, JXLoginPane panel) {
        Window w = WindowUtils.findWindow(parent);
        JXLoginDialog dlg =  null;
        if (w == null) {
            dlg = new JXLoginDialog((Frame)null, panel);
        } else if (w instanceof Dialog) {
            dlg = new JXLoginDialog((Dialog)w, panel);
        } else if (w instanceof Frame) {
            dlg = new JXLoginDialog((Frame)w, panel);
        } else {
            throw new AssertionError("Shouldn't be able to happen");
        }
        dlg.setVisible(true);
        return dlg.getStatus();
    }

    /**
     * Shows a login frame. A JFrame is not modal, and thus does not block
     */
    public static JXLoginFrame showLoginFrame(LoginService svc) {
        return showLoginFrame(svc, null, null);
    }

    /**
     */
    public static JXLoginFrame showLoginFrame(LoginService svc, PasswordStore ps, UserNameStore us) {
        return showLoginFrame(svc, ps, us, null);
    }

    /**
     */
    public static JXLoginFrame showLoginFrame(LoginService svc, PasswordStore ps, UserNameStore us, List<String> servers) {
        JXLoginPane panel = new JXLoginPane(svc, ps, us, servers);
        return showLoginFrame(panel);
    }

    /**
     */
    public static JXLoginFrame showLoginFrame(JXLoginPane panel) {
        return new JXLoginFrame(panel);
    }

    public static final class JXLoginDialog extends JDialog {
        private static final long serialVersionUID = -3185639594267828103L;
        private JXLoginPane panel;

        public JXLoginDialog(Frame parent, JXLoginPane p) {
            super(parent, true);
            init(p);
        }

        public JXLoginDialog(Dialog parent, JXLoginPane p) {
            super(parent, true);
            init(p);
        }

        protected void init(JXLoginPane p) {
            setTitle(UIManagerExt.getString(CLASS_NAME + ".titleString", getLocale()));
            this.panel = p;
            initWindow(this, panel);
        }

        public JXLoginPane.Status getStatus() {
            return panel.getStatus();
        }
    }

    public static final class JXLoginFrame extends JXFrame {
        private static final long serialVersionUID = -9016407314342050807L;
        private JXLoginPane panel;

        public JXLoginFrame(JXLoginPane p) {
            super(UIManagerExt.getString(CLASS_NAME + ".titleString", p.getLocale()));
            JXPanel cp = new JXPanel();
            cp.setOpaque(true);
            setContentPane(cp);
            this.panel = p;
            initWindow(this, panel);
        }

        @Override
	public JXPanel getContentPane() {
            return (JXPanel) super.getContentPane();
        }

        public JXLoginPane.Status getStatus() {
            return panel.getStatus();
        }

        public JXLoginPane getPanel() {
            return panel;
        }
    }

    /**
     * Utility method for initializing a Window for displaying a LoginDialog.
     * This is particularly useful because the differences between JFrame and
     * JDialog are so minor.
     *
     * Note: This method is package private for use by JXLoginDialog (proper,
     * not JXLoginPane.JXLoginDialog). Change to private if JXLoginDialog is
     * removed.
     */
    static void initWindow(final Window w, final JXLoginPane panel) {
        w.setLayout(new BorderLayout());
        w.add(panel, BorderLayout.CENTER);
        JButton okButton = new JButton(panel.getActionMap().get(LOGIN_ACTION_COMMAND));
        final JButton cancelButton = new JButton(
                UIManagerExt.getString(CLASS_NAME + ".cancelString", panel.getLocale()));
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //change panel status to canceled!
                panel.status = JXLoginPane.Status.CANCELLED;
                w.setVisible(false);
                w.dispose();
            }
        });
        panel.addPropertyChangeListener("status", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                JXLoginPane.Status status = (JXLoginPane.Status)evt.getNewValue();
                switch (status) {
                    case NOT_STARTED:
                        break;
                    case IN_PROGRESS:
                        cancelButton.setEnabled(false);
                        break;
                    case CANCELLED:
                        cancelButton.setEnabled(true);
                        w.pack();
                        break;
                    case FAILED:
                        cancelButton.setEnabled(true);
                        panel.passwordField.requestFocusInWindow();
                        w.pack();
                        break;
                    case SUCCEEDED:
                        w.setVisible(false);
                        w.dispose();
                }
                for (PropertyChangeListener l : w.getPropertyChangeListeners("status")) {
                    PropertyChangeEvent pce = new PropertyChangeEvent(w, "status", evt.getOldValue(), evt.getNewValue());
                    l.propertyChange(pce);
                }
            }
        });
        // FIX for #663 - commented out two lines below. Not sure why they were here in a first place.
        // cancelButton.setText(UIManager.getString(CLASS_NAME + ".cancelString"));
        // okButton.setText(UIManager.getString(CLASS_NAME + ".loginString"));
        JXBtnPanel buttonPanel = new JXBtnPanel(okButton, cancelButton);
        buttonPanel.setOpaque(false);
        panel.setButtonPanel(buttonPanel);
        JXPanel controls = new JXPanel(new FlowLayout(FlowLayout.RIGHT));
        controls.setOpaque(false);
        new BoxLayout(controls, BoxLayout.X_AXIS);
        controls.add(Box.createHorizontalGlue());
        controls.add(buttonPanel);
        w.add(controls, BorderLayout.SOUTH);
        w.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                panel.cancelLogin();
            }
        });

        if (w instanceof JFrame) {
            final JFrame f = (JFrame)w;
            f.getRootPane().setDefaultButton(okButton);
            f.setResizable(false);
            f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
            ActionListener closeAction = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    f.setVisible(false);
                    f.dispose();
                }
            };
            f.getRootPane().registerKeyboardAction(closeAction, ks, JComponent.WHEN_IN_FOCUSED_WINDOW);
        } else if (w instanceof JDialog) {
            final JDialog d = (JDialog)w;
            d.getRootPane().setDefaultButton(okButton);
            d.setResizable(false);
            KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
            ActionListener closeAction = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    d.setVisible(false);
                }
            };
            d.getRootPane().registerKeyboardAction(closeAction, ks, JComponent.WHEN_IN_FOCUSED_WINDOW);
        }
        w.pack();
        w.setLocation(WindowUtils.getPointForCentering(w));
    }

    private void setButtonPanel(JXBtnPanel buttonPanel) {
        this.buttonPanel = buttonPanel;
    }

    private static class JXBtnPanel extends JXPanel {
        private static final long serialVersionUID = 4136611099721189372L;
        private JButton cancel;
        private JButton ok;

        public JXBtnPanel(JButton okButton, JButton cancelButton) {
            GridLayout layout = new GridLayout(1,2);
            layout.setHgap(5);
            setLayout(layout);
            this.ok = okButton;
            this.cancel = cancelButton;
            add(okButton);
            add(cancelButton);
            setBorder(new EmptyBorder(0,0,7,11));
        }

        /**
         * @return the cancel button.
         */
        public JButton getCancel() {
            return cancel;
        }

        /**
         * @return the ok button.
         */
        public JButton getOk() {
            return ok;
        }

    }
}
