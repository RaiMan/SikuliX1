/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx;

import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Locale;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.jdesktop.swingx.action.AbstractActionExt;
import org.jdesktop.swingx.action.ActionContainerFactory;
import org.jdesktop.swingx.action.BoundAction;
import org.jdesktop.swingx.plaf.LookAndFeelAddons;
import org.jdesktop.swingx.plaf.UIManagerExt;
import org.jdesktop.swingx.search.PatternModel;

/**
 * Common base class of ui clients.
 *
 * Implements basic synchronization between PatternModel state and
 * actions bound to it.
 *
 *
 *
 * PENDING: extending JXPanel is a convenience measure, should be extracted
 *   into a dedicated controller.
 * PENDING: should be re-visited when swingx goes binding-aware
 *
 * @author Jeanette Winzenburg
 */
public abstract class AbstractPatternPanel extends JXPanel {

    public static final String SEARCH_FIELD_LABEL = "searchFieldLabel";
    public static final String SEARCH_FIELD_MNEMONIC = SEARCH_FIELD_LABEL + ".mnemonic";
    public static final String SEARCH_TITLE = "searchTitle";
    public static final String MATCH_ACTION_COMMAND = "match";

    static {
        // Hack to enforce loading of SwingX framework ResourceBundle
        LookAndFeelAddons.getAddon();
    }

    protected JLabel searchLabel;
    protected JTextField searchField;
    protected JCheckBox matchCheck;

    protected PatternModel patternModel;
    private ActionContainerFactory actionFactory;

//------------------------ actions

    /**
     * Callback action bound to MATCH_ACTION_COMMAND.
     */
    public abstract void match();

    /**
     * convenience method for type-cast to AbstractActionExt.
     *
     * @param key Key to retrieve action
     * @return Action bound to this key
     * @see AbstractActionExt
     */
    protected AbstractActionExt getAction(String key) {
        // PENDING: outside clients might add different types?
        return (AbstractActionExt) getActionMap().get(key);
    }

    /**
     * creates and registers all actions for the default the actionMap.
     */
    protected void initActions() {
        initPatternActions();
        initExecutables();
    }

    /**
     * creates and registers all "executable" actions.
     * Meaning: the actions bound to a callback method on this.
     *
     * PENDING: not quite correctly factored? Name?
     *
     */
    protected void initExecutables() {
        Action execute = createBoundAction(MATCH_ACTION_COMMAND, "match");
        getActionMap().put(JXDialog.EXECUTE_ACTION_COMMAND,
                execute);
        getActionMap().put(MATCH_ACTION_COMMAND, execute);
        refreshEmptyFromModel();
    }

    /**
     * creates actions bound to PatternModel's state.
     */
    protected void initPatternActions() {
        ActionMap map = getActionMap();
        map.put(PatternModel.MATCH_CASE_ACTION_COMMAND,
                createModelStateAction(PatternModel.MATCH_CASE_ACTION_COMMAND,
                        "setCaseSensitive", getPatternModel().isCaseSensitive()));
        map.put(PatternModel.MATCH_WRAP_ACTION_COMMAND,
                createModelStateAction(PatternModel.MATCH_WRAP_ACTION_COMMAND,
                        "setWrapping", getPatternModel().isWrapping()));
        map.put(PatternModel.MATCH_BACKWARDS_ACTION_COMMAND,
                createModelStateAction(PatternModel.MATCH_BACKWARDS_ACTION_COMMAND,
                        "setBackwards", getPatternModel().isBackwards()));
        map.put(PatternModel.MATCH_INCREMENTAL_ACTION_COMMAND,
                createModelStateAction(PatternModel.MATCH_INCREMENTAL_ACTION_COMMAND,
                        "setIncremental", getPatternModel().isIncremental()));
    }

    /**
     * Returns a potentially localized value from the UIManager. The given key
     * is prefixed by this component|s <code>UIPREFIX</code> before doing the
     * lookup. The lookup respects this table's current <code>locale</code>
     * property. Returns the key, if no value is found.
     *
     * @param key the bare key to look up in the UIManager.
     * @return the value mapped to UIPREFIX + key or key if no value is found.
     */
    protected String getUIString(String key) {
        return getUIString(key, getLocale());
    }

    /**
     * Returns a potentially localized value from the UIManager for the
     * given locale. The given key
     * is prefixed by this component's <code>UIPREFIX</code> before doing the
     * lookup. Returns the key, if no value is found.
     *
     * @param key the bare key to look up in the UIManager.
     * @param locale the locale use for lookup
     * @return the value mapped to UIPREFIX + key in the given locale,
     *    or key if no value is found.
     */
    protected String getUIString(String key, Locale locale) {
        String text = UIManagerExt.getString(PatternModel.SEARCH_PREFIX + key, locale);
        return text != null ? text : key;
    }

    /**
     * creates, configures and returns a bound state action on a boolean property
     * of the PatternModel.
     *
     * @param command the actionCommand - same as key to find localizable resources
     * @param methodName the method on the PatternModel to call on item state changed
     * @param initial the initial value of the property
     * @return newly created action
     */
    protected AbstractActionExt createModelStateAction(String command, String methodName, boolean initial) {
        String actionName = getUIString(command);
        BoundAction action = new BoundAction(actionName,
                command);
        action.setStateAction();
        action.registerCallback(getPatternModel(), methodName);
        action.setSelected(initial);
        return action;
    }

    /**
     * creates, configures and returns a bound action to the given method of
     * this.
     *
     * @param actionCommand the actionCommand, same as key to find localizable resources
     * @param methodName the method to call an actionPerformed.
     * @return newly created action
     */
    protected AbstractActionExt createBoundAction(String actionCommand, String methodName) {
        String actionName = getUIString(actionCommand);
        BoundAction action = new BoundAction(actionName,
                actionCommand);
        action.registerCallback(this, methodName);
        return action;
    }

//------------------------ dynamic locale support

    /**
     * {@inheritDoc} <p>
     * Overridden to update locale-dependent properties.
     *
     * @see #updateLocaleState(Locale)
     */
    @Override
    public void setLocale(Locale l) {
        updateLocaleState(l);
        super.setLocale(l);
    }

    /**
     * Updates locale-dependent state.
     *
     * Here: updates registered column actions' locale-dependent state.
     * <p>
     *
     * PENDING: Try better to find all column actions including custom
     * additions? Or move to columnControl?
     *
     * @see #setLocale(Locale)
     */
    protected void updateLocaleState(Locale locale) {
        for (Object key : getActionMap().allKeys()) {
            if (key instanceof String) {
                String keyString = getUIString((String) key, locale);
                if (!key.equals(keyString)) {
                    getActionMap().get(key).putValue(Action.NAME, keyString);

                }
            }
        }
        bindSearchLabel(locale);
    }

    //---------------------- synch patternModel <--> components

    /**
     * called from listening to pattern property of PatternModel.
     *
     * This implementation calls match() if the model is in
     * incremental state.
     *
     */
    protected void refreshPatternFromModel() {
        if (getPatternModel().isIncremental()) {
            match();
        }
    }

    /**
     * returns the patternModel. Lazyly creates and registers a
     * propertyChangeListener if null.
     *
     * @return current <code>PatternModel</code> if it exists or newly created
     * one if it was not initialized before this call
     */
    protected PatternModel getPatternModel() {
        if (patternModel == null) {
            patternModel = createPatternModel();
            patternModel.addPropertyChangeListener(getPatternModelListener());
        }
        return patternModel;
    }

    /**
     * factory method to create the PatternModel.
     * Hook for subclasses to install custom models.
     *
     * @return newly created <code>PatternModel</code>
     */
    protected PatternModel createPatternModel() {
        return new PatternModel();
    }

    /**
     * creates and returns a PropertyChangeListener to the PatternModel.
     *
     * NOTE: the patternModel is totally under control of this class - currently
     * there's no need to keep a reference to the listener.
     *
     * @return created and bound to appropriate callback methods
     *  <code>PropertyChangeListener</code>
     */
    protected PropertyChangeListener getPatternModelListener() {
        return new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                String property = evt.getPropertyName();
                if ("pattern".equals(property)) {
                    refreshPatternFromModel();
                } else if ("rawText".equals(property)) {
                    refreshDocumentFromModel();
                } else if ("caseSensitive".equals(property)){
                    getAction(PatternModel.MATCH_CASE_ACTION_COMMAND).
                        setSelected(((Boolean) evt.getNewValue()).booleanValue());
                } else if ("wrapping".equals(property)) {
                    getAction(PatternModel.MATCH_WRAP_ACTION_COMMAND).
                    setSelected(((Boolean) evt.getNewValue()).booleanValue());
                } else if ("backwards".equals(property)) {
                    getAction(PatternModel.MATCH_BACKWARDS_ACTION_COMMAND).
                    setSelected(((Boolean) evt.getNewValue()).booleanValue());
                } else if ("incremental".equals(property)) {
                    getAction(PatternModel.MATCH_INCREMENTAL_ACTION_COMMAND).
                    setSelected(((Boolean) evt.getNewValue()).booleanValue());

                } else if ("empty".equals(property)) {
                    refreshEmptyFromModel();
                }

            }

        };
    }

    /**
     * called from listening to empty property of PatternModel.
     *
     * this implementation synch's the enabled state of the action with
     * MATCH_ACTION_COMMAND to !empty.
     *
     */
    protected void refreshEmptyFromModel() {
        boolean enabled = !getPatternModel().isEmpty();
        getAction(MATCH_ACTION_COMMAND).setEnabled(enabled);

    }

    /**
     * callback method from listening to searchField.
     *
     */
    protected void refreshModelFromDocument() {
        getPatternModel().setRawText(searchField.getText());
    }

    /**
     * callback method that updates document from the search field
     *
     */
    protected void refreshDocumentFromModel() {
        if (searchField.getText().equals(getPatternModel().getRawText())) return;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                searchField.setText(getPatternModel().getRawText());
            }
        });
    }

    /**
     * Create <code>DocumentListener</code> for the search field that calls
     * corresponding callback method whenever the search field contents is being changed
     *
     * @return newly created <code>DocumentListener</code>
     */
    protected DocumentListener getSearchFieldListener() {
        return new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent ev) {
                // JW - really?? we've a PlainDoc without Attributes
                refreshModelFromDocument();
            }

            @Override
            public void insertUpdate(DocumentEvent ev) {
                refreshModelFromDocument();
            }

            @Override
            public void removeUpdate(DocumentEvent ev) {
                refreshModelFromDocument();
            }

        };
    }

//-------------------------- config helpers

    /**
     * configure and bind components to/from PatternModel
     */
    protected void bind() {
       bindSearchLabel(getLocale());
        searchField.getDocument().addDocumentListener(getSearchFieldListener());
        getActionContainerFactory().configureButton(matchCheck,
                (AbstractActionExt) getActionMap().get(PatternModel.MATCH_CASE_ACTION_COMMAND),
                null);

    }

    /**
     * Configures the searchLabel.
     * Here: sets text and mnenomic properties form ui values,
     * configures as label for searchField.
     */
    protected void bindSearchLabel(Locale locale) {
        searchLabel.setText(getUIString(SEARCH_FIELD_LABEL, locale));
          String mnemonic = getUIString(SEARCH_FIELD_MNEMONIC, locale);
          if (mnemonic != SEARCH_FIELD_MNEMONIC) {
              searchLabel.setDisplayedMnemonic(mnemonic.charAt(0));
          }
          searchLabel.setLabelFor(searchField);
    }

    /**
     * @return current <code>ActionContainerFactory</code>.
     * Will lazily create new factory if it does not exist
     */
    protected ActionContainerFactory getActionContainerFactory() {
        if (actionFactory == null) {
            actionFactory = new ActionContainerFactory(null);
        }
        return actionFactory;
    }

    /**
     * Initialize all the incorporated components and models
     */
    protected void initComponents() {
        searchLabel = new JLabel();
        searchField = new JTextField(getSearchFieldWidth()) {
            @Override
            public Dimension getMaximumSize() {
                Dimension superMax = super.getMaximumSize();
                superMax.height = getPreferredSize().height;
                return superMax;
            }
        };
        matchCheck = new JCheckBox();
    }

    /**
     * @return width in characters of the search field
     */
    protected int getSearchFieldWidth() {
        return 15;
    }
}
