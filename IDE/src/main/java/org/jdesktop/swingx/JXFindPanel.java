/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.jdesktop.swingx;

import java.awt.Component;
import java.util.Locale;
import java.util.regex.Pattern;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import org.jdesktop.beans.JavaBean;
import org.jdesktop.swingx.search.PatternModel;
import org.jdesktop.swingx.search.Searchable;

/**
 * {@code JXFindPanel} is a basic find panel suitable for use in dialogs. It
 * offers case-sensitivity, wrapped searching, and reverse searching.
 *
 * @author unascribed from JDNC
 * @author Jeanette Winzenburg
 */
@JavaBean
public class JXFindPanel extends AbstractPatternPanel {

    public static final String FIND_NEXT_ACTION_COMMAND = "findNext";
    public static final String FIND_PREVIOUS_ACTION_COMMAND = "findPrevious";

    protected Searchable searchable;

    protected JCheckBox wrapCheck;
    protected JCheckBox backCheck;
    private boolean initialized;

    /**
     * Default constructor for the find panel. Constructs panel not targeted to
     * any component.
     */
    public JXFindPanel() {
        this(null);
    }

    /**
     * Construct search panel targeted to specific <code>Searchable</code> component.
     *
     * @param searchable Component where search widget will try to locate and select
     *                   information using methods of the <code>Searchable</code> interface.
     */
    public JXFindPanel(Searchable searchable) {
        setName(getUIString(SEARCH_TITLE));
        setSearchable(searchable);
        initActions();
    }

    /**
     * Sets the Searchable targeted of this find widget.
     * Triggers a search with null pattern to release the old
     * searchable, if any.
     *
     * @param searchable Component where search widget will try to locate and select
     *                   information using methods of the {@link Searchable Searchable} interface.
     */
    public void setSearchable(Searchable searchable) {
        if ((this.searchable != null) && this.searchable.equals(searchable)) return;
        Searchable old = this.searchable;
        if (old != null) {
            old.search((Pattern) null);
        }
        this.searchable = searchable;
        getPatternModel().setFoundIndex(-1);
        firePropertyChange("searchable", old, this.searchable);
    }

    /**
     * Notifies this component that it now has a parent component.
     * When this method is invoked, the chain of parent components is
     * set up with <code>KeyboardAction</code> event listeners.
     */
    @Override
    public void addNotify() {
        init();
        super.addNotify();
    }

   /**
    * Initializes component and its listeners and models.
    */
    protected void init() {
        if (initialized) return;
        initialized = true;
        initComponents();
        build();
        bind();
    }

    //------------------ support synch the model <--> components

    /**
     * Configure and bind components to/from PatternModel.
     */
    @Override
    protected void bind() {
        super.bind();
        getActionContainerFactory().configureButton(wrapCheck,
                getAction(PatternModel.MATCH_WRAP_ACTION_COMMAND),
                null);
        getActionContainerFactory().configureButton(backCheck,
                getAction(PatternModel.MATCH_BACKWARDS_ACTION_COMMAND),
                null);
    }

    /**
     * called from listening to empty property of PatternModel.
     *
     * this implementation calls super and additionally synchs the
     * enabled state of FIND_NEXT_ACTION_COMMAND, FIND_PREVIOUS_ACTION_COMMAND
     * to !empty.
     */
    @Override
    protected void refreshEmptyFromModel() {
        super.refreshEmptyFromModel();
        boolean enabled = !getPatternModel().isEmpty();
        getAction(FIND_NEXT_ACTION_COMMAND).setEnabled(enabled);
        getAction(FIND_PREVIOUS_ACTION_COMMAND).setEnabled(enabled);
    }

    //--------------------- action callbacks
    /**
     * Action callback for Find action.
     * Find next/previous match using current setting of direction flag.
     *
     */
    @Override
    public void match() {
        doFind();
    }

    /**
     * Action callback for FindNext action.
     * Sets direction flag to forward and calls find.
     */
    public void findNext() {
        getPatternModel().setBackwards(false);
        doFind();
    }

    /**
     * Action callback for FindPrevious action.
     * Sets direction flag to previous and calls find.
     */
    public void findPrevious() {
        getPatternModel().setBackwards(true);
        doFind();
    }

    /**
     * Common standalone method to perform search. Used by the action callback methods
     * for Find/FindNext/FindPrevious actions. Finds next/previous match using current
     * setting of direction flag. Result is being reporred using showFoundMessage and
     * showNotFoundMessage methods respectively.
     *
     * @see #match
     * @see #findNext
     * @see #findPrevious
     */
    protected void doFind() {
        if (searchable == null)
            return;
        int foundIndex = doSearch();
        boolean notFound = (foundIndex == -1) && !getPatternModel().isEmpty();
        if (notFound) {
            if (getPatternModel().isWrapping()) {
                notFound = doSearch() == -1;
            }
        }
        if (notFound) {
            showNotFoundMessage();
        } else {
            showFoundMessage();
        }
    }

    /**
     * Performs search and returns index of the next match.
     *
     * @return Index of the next match in document.
     */
    protected int doSearch() {
        int foundIndex = searchable.search(getPatternModel().getPattern(),
                getPatternModel().getFoundIndex(), getPatternModel().isBackwards());
        getPatternModel().setFoundIndex(foundIndex);
        return getPatternModel().getFoundIndex();
//         first try on #236-swingx - foundIndex wrong in backwards search.
//         re-think: autoIncrement in PatternModel?
//        return foundIndex;
    }

    /**
     * Report that suitable match is found.
     */
    protected void showFoundMessage() {

    }

    /**
     * Report that no match is found.
     */
    protected void showNotFoundMessage() {
        JOptionPane.showMessageDialog(this, getUIString("notFound"));
    }

//-------------- dynamic Locale support


    @Override
    protected void updateLocaleState(Locale locale) {
        super.updateLocaleState(locale);
        setName(getUIString(SEARCH_TITLE, locale));
    }

    //-------------------------- initial

    /**
     * creates and registers all "executable" actions.
     * Meaning: the actions bound to a callback method on this.
     */
    @Override
    protected void initExecutables() {
        getActionMap().put(FIND_NEXT_ACTION_COMMAND,
                createBoundAction(FIND_NEXT_ACTION_COMMAND, "findNext"));
        getActionMap().put(FIND_PREVIOUS_ACTION_COMMAND,
                createBoundAction(FIND_PREVIOUS_ACTION_COMMAND, "findPrevious"));
        super.initExecutables();
    }


//----------------------------- init ui

    /**
     * Create and initialize components.
     */
    @Override
    protected void initComponents() {
        super.initComponents();
        wrapCheck = new JCheckBox();
        backCheck = new JCheckBox();
    }


    /**
     * Compose and layout all the subcomponents.
     */
    protected void build() {
        Box lBox = new Box(BoxLayout.LINE_AXIS);
        lBox.add(searchLabel);
        lBox.add(new JLabel(":"));
        lBox.add(new JLabel("  "));
        lBox.setAlignmentY(Component.TOP_ALIGNMENT);
        Box rBox = new Box(BoxLayout.PAGE_AXIS);
        rBox.add(searchField);
        rBox.add(matchCheck);
        rBox.add(wrapCheck);
        rBox.add(backCheck);
        rBox.setAlignmentY(Component.TOP_ALIGNMENT);

        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

        add(lBox);
        add(rBox);
    }

}
