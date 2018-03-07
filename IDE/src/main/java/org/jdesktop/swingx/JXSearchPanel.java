/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.jdesktop.swingx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;

import org.jdesktop.beans.JavaBean;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.renderer.DefaultListRenderer;
import org.jdesktop.swingx.renderer.LocalizableStringValue;
import org.jdesktop.swingx.renderer.StringValue;
import org.jdesktop.swingx.search.PatternMatcher;
import org.jdesktop.swingx.search.PatternModel;
/**
 * <p>
 * {@code JXSearchPanel} provides complex searching features. Users are able to
 * specify searching rules, enter searching text (including regular
 * expressions), and toggle case-sensitivity.
 * </p>
 * <p>
 * One of the main features that {@code JXSearchPanel} provides is the ability
 * to update {@link PatternMatcher}s. To highlight text with a
 * {@link Highlighter}, you need to update the highlighter via a pattern
 * matcher.
 * </p>
 * <pre>
 * public class PatternHandler implements PatternMatcher {
 *
 *     private Highlighter highlighter;
 *
 *     private Pattern pattern;
 *
 *     public void setPattern(Pattern pattern) {
 *         this.pattern = pattern;
 *         highlighter.setHighlightPredicate(new PatternPredicate(pattern));
 *     }
 *
 * }
 * </pre>
 * <p>
 * TODO: allow custom PatternModel and/or access to configuration of bound
 * PatternModel.
 * </p>
 * <p>
 * TODO: fully support control of multiple PatternMatchers.
 * </p>
 *
 * @author Ramesh Gupta
 * @author Jeanette Winzenburg
 */
@JavaBean
public class JXSearchPanel extends AbstractPatternPanel {
    /**
     * The action command key.
     */
    public static final String MATCH_RULE_ACTION_COMMAND = "selectMatchRule";

    private JXComboBox searchCriteria;

    private List<PatternMatcher> patternMatchers;

    /**
     * Creates a search panel.
     */
    public JXSearchPanel() {
        initComponents();
        build();
        initActions();
        bind();
        getPatternModel().setIncremental(true);
    }

//----------------- accessing public properties

    /**
     * Adds a pattern matcher.
     *
     * @param matcher
     *            the matcher to add.
     */
    public void addPatternMatcher(PatternMatcher matcher) {
        getPatternMatchers().add(matcher);
        updateFieldName(matcher);
    }

    /**
     * sets the PatternFilter control.
     *
     * PENDING: change to do a addPatternMatcher to enable multiple control.
     *
     */
//    public void setPatternFilter(PatternFilter filter) {
//        getPatternMatchers().add(filter);
//        updateFieldName(filter);
//    }

    /**
     * set the label of the search combo.
     *
     * @param name
     *            the label
     */
    public void setFieldName(String name) {
        String old = searchLabel.getText();
        searchLabel.setText(name);
        firePropertyChange("fieldName", old, searchLabel.getText());
    }

    /**
     * returns the label of the search combo.
     *
     */
    public String getFieldName() {
        return searchLabel.getText();
    }

    /**
     * returns the current compiled Pattern.
     *
     * @return the current compiled <code>Pattern</code>
     */
    public Pattern getPattern() {
        return patternModel.getPattern();
    }

    /**
     * @param matcher
     */
    protected void updateFieldName(PatternMatcher matcher) {

//        if (matcher instanceof PatternFilter) {
//            PatternFilter filter = (PatternFilter) matcher;
//            searchLabel.setText(filter.getColumnName());
//        } else {
            if (searchLabel.getText().length() == 0) { // ugly hack
                searchLabel.setText("Field");
                /** TODO: Remove this hack!!! */
//            }
        }
    }

    // ---------------- action callbacks

    /**
     * Updates the pattern matchers.
     */
    @Override
    public void match() {
        for (Iterator<PatternMatcher> iter = getPatternMatchers().iterator(); iter.hasNext();) {
            iter.next().setPattern(getPattern());

        }
    }

    /**
     * set's the PatternModel's MatchRule to the selected in combo.
     *
     * NOTE: this
     * is public as an implementation side-effect!
     * No need to ever call directly.
     */
    public void updateMatchRule() {
        getPatternModel().setMatchRule(
                (String) searchCriteria.getSelectedItem());
    }

    private List<PatternMatcher> getPatternMatchers() {
        if (patternMatchers == null) {
            patternMatchers = new ArrayList<PatternMatcher>();
        }
        return patternMatchers;
    }

    //---------------- init actions and model

    @Override
    protected void initExecutables() {
        super.initExecutables();
        getActionMap().put(MATCH_RULE_ACTION_COMMAND,
                createBoundAction(MATCH_RULE_ACTION_COMMAND, "updateMatchRule"));
    }

    //--------------------- binding support


    /**
     * bind the components to the patternModel/actions.
     */
    @Override
    protected void bind() {
        super.bind();
        List<?> matchRules = getPatternModel().getMatchRules();
        // PENDING: map rules to localized strings
        ComboBoxModel model = new DefaultComboBoxModel(matchRules.toArray());
        model.setSelectedItem(getPatternModel().getMatchRule());
        searchCriteria.setModel(model);
        searchCriteria.setAction(getAction(MATCH_RULE_ACTION_COMMAND));
        searchCriteria.setRenderer(new DefaultListRenderer(createStringValue(getLocale())));

    }

    private StringValue createStringValue(Locale locale) {
        // TODO Auto-generated method stub
        Map<Object, String> keys = new HashMap<Object, String>();
        keys.put(PatternModel.MATCH_RULE_CONTAINS,
                PatternModel.MATCH_RULE_CONTAINS);
        keys.put(PatternModel.MATCH_RULE_ENDSWITH,
                PatternModel.MATCH_RULE_ENDSWITH);
        keys.put(PatternModel.MATCH_RULE_EQUALS,
                PatternModel.MATCH_RULE_EQUALS);
        keys.put(PatternModel.MATCH_RULE_STARTSWITH,
                PatternModel.MATCH_RULE_STARTSWITH);
        return new LocalizableStringValue(keys, PatternModel.SEARCH_PREFIX, locale);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void updateLocaleState(Locale locale) {
        // TODO Auto-generated method stub
        super.updateLocaleState(locale);
        searchCriteria.setRenderer(new DefaultListRenderer(createStringValue(locale)));
    }

    //------------------------ init ui
    /**
     * build container by adding all components.
     * PRE: all components created.
     */
    private void build() {
        add(searchLabel);
        add(searchCriteria);
        add(searchField);
        add(matchCheck);
    }

    /**
     * create contained components.
     *
     *
     */
    @Override
    protected void initComponents() {
        super.initComponents();
        searchCriteria = new JXComboBox();
    }

}
