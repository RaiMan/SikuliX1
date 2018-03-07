/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.jdesktop.swingx.search;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jdesktop.swingx.JXSearchField;
import org.jdesktop.swingx.plaf.UIManagerExt;

/**
 * Maintains a list of recent searches and persists this list automatically
 * using {@link Preferences}. A recent searches popup menu can be installed on
 * a {@link JXSearchField} using {@link #install(JXSearchField)}.
 *
 * @author Peter Weishapl <petw@gmx.net>
 *
 */
public class RecentSearches implements ActionListener {
	private Preferences prefsNode;

	private int maxRecents = 5;

	private List<String> recentSearches = new ArrayList<String>();

	private List<ChangeListener> listeners = new ArrayList<ChangeListener>();

	/**
	 * Creates a list of recent searches and uses <code>saveName</code> to
	 * persist this list under the {@link Preferences} user root node. Existing
	 * entries will be loaded automatically.
	 *
	 * @param saveName
	 *            a unique name for saving this list of recent searches
	 */
	public RecentSearches(String saveName) {
		this(null, saveName);
	}

	/**
	 * Creates a list of recent searches and uses <code>saveName</code> to
	 * persist this list under the <code>prefs</code> node. Existing entries
	 * will be loaded automatically.
	 *
	 * @param prefsNode
	 *            the preferences node under which this list will be persisted.
	 *            If prefsNode is <code>null</code> the preferences node will
	 *            be set to the user root node
	 * @param saveName
	 *            a unique name for saving this list of recent searches. If
	 *            saveName is <code>null</code>, the list will not be
	 *            persisted
	 */
	public RecentSearches(Preferences prefs, String saveName) {
		if (prefs == null) {
			try {
				prefs = Preferences.userRoot();
			} catch (AccessControlException ace) {
				// disable persistency, if we aren't allowed to access
				// preferences.
				Logger.getLogger(getClass().getName()).warning("cannot acces preferences. persistency disabled.");
			}
		}

		if (prefs != null && saveName != null) {
			this.prefsNode = prefs.node(saveName);
			load();
		}
	}

	private void load() {
		// load persisted entries
		try {
			String[] recent = new String[prefsNode.keys().length];
			for (String key : prefsNode.keys()) {
				recent[prefsNode.getInt(key, -1)] = key;
			}
			recentSearches.addAll(Arrays.asList(recent));
		} catch (Exception ex) {
			// ignore
		}
	}

	private void save() {
		if (prefsNode == null) {
			return;
		}

		try {
			prefsNode.clear();
		} catch (BackingStoreException e) {
			// ignore
		}

		int i = 0;
		for (String search : recentSearches) {
			prefsNode.putInt(search, i++);
		}
	}

	/**
	 * Add a search string as the first element. If the search string is
	 * <code>null</code> or empty nothing will be added. If the search string
	 * already exists, the old element will be removed. The modified list will
	 * automatically be persisted.
	 *
	 * If the number of elements exceeds the maximum number of entries, the last
	 * entry will be removed.
	 *
	 * @see #getMaxRecents()
	 * @param searchString
	 *            the search string to add
	 */
	public void put(String searchString) {
		if (searchString == null || searchString.trim().length() == 0) {
			return;
		}

		int lastIndex = recentSearches.indexOf(searchString);
		if (lastIndex != -1) {
			recentSearches.remove(lastIndex);
		}
		recentSearches.add(0, searchString);
		if (getLength() > getMaxRecents()) {
			recentSearches.remove(recentSearches.size() - 1);
		}
		save();
		fireChangeEvent();
	}

	/**
	 * Returns all recent searches in this list.
	 *
	 * @return the recent searches
	 */
	public String[] getRecentSearches() {
		return recentSearches.toArray(new String[] {});
	}

	/**
	 * The number of recent searches.
	 *
	 * @return number of recent searches
	 */
	public int getLength() {
		return recentSearches.size();
	}

	/**
	 * Remove all recent searches.
	 */
	public void removeAll() {
		recentSearches.clear();
		save();
		fireChangeEvent();
	}

	/**
	 * Returns the maximum number of recent searches.
	 *
	 * @see #put(String)
	 * @return the maximum number of recent searches
	 */
	public int getMaxRecents() {
		return maxRecents;
	}

	/**
	 * Set the maximum number of recent searches.
	 *
	 * @see #put(String)
	 * @param maxRecents
	 *            maximum number of recent searches
	 */
	public void setMaxRecents(int maxRecents) {
		this.maxRecents = maxRecents;
	}

	/**
	 * Add a change listener. A {@link ChangeEvent} will be fired whenever a
	 * search is added or removed.
	 *
	 * @param l
	 *            the {@link ChangeListener}
	 */
	public void addChangeListener(ChangeListener l) {
		listeners.add(l);
	}

	/**
	 * Remove a change listener.
	 *
	 * @param l
	 *            a registered {@link ChangeListener}
	 */
	public void removeChangeListener(ChangeListener l) {
		listeners.remove(l);
	}

	/**
	 * Returns all registered {@link ChangeListener}s.
	 *
	 * @return all registered {@link ChangeListener}s
	 */
	public ChangeListener[] getChangeListeners() {
		return listeners.toArray(new ChangeListener[] {});
	}

	private void fireChangeEvent() {
		ChangeEvent e = new ChangeEvent(this);

		for (ChangeListener l : listeners) {
			l.stateChanged(e);
		}
	}

	/**
	 * Creates the recent searches popup menu which will be used by
	 * {@link #install(JXSearchField)} to set a search popup menu on
	 * <code>searchField</code>.
	 *
	 * Override to return a custom popup menu.
	 *
	 * @param searchField
	 *            the search field the returned popup menu will be installed on
	 * @return the recent searches popup menu
	 */
	protected JPopupMenu createPopupMenu(JTextField searchField) {
		return new RecentSearchesPopup(this, searchField);
	}

	/**
	 * Install a recent the searches popup menu returned by
	 * {@link #createPopupMenu(JXSearchField)} on <code>searchField</code>.
	 * Also registers an {@link ActionListener} on <code>searchField</code>
	 * and adds the search string to the list of recent searches whenever a
	 * {@link ActionEvent} is received.
	 *
	 * Uses {@link NativeSearchFieldSupport} to achieve compatibility with the native
	 * search field support provided by the Mac Look And Feel since Mac OS 10.5.
	 *
	 * @param searchField
	 *            the search field to install a recent searches popup menu on
	 */
	public void install(JTextField searchField) {
		searchField.addActionListener(this);
		NativeSearchFieldSupport.setFindPopupMenu(searchField, createPopupMenu(searchField));
	}

	/**
	 * Remove the recent searches popup from <code>searchField</code> when
	 * installed and stop listening for {@link ActionEvent}s fired by the
	 * search field.
	 *
	 * @param searchField
	 *            uninstall recent searches popup menu
	 */
	public void uninstall(JXSearchField searchField) {
		searchField.removeActionListener(this);
		if (searchField.getFindPopupMenu() instanceof RecentSearchesPopup) {
			removeChangeListener((ChangeListener) searchField.getFindPopupMenu());
			searchField.setFindPopupMenu(null);
		}
	}

	/**
	 * Calls {@link #put(String)} with the {@link ActionEvent}s action command
	 * as the search string.
	 */
	@Override
    public void actionPerformed(ActionEvent e) {
		put(e.getActionCommand());
	}

	/**
	 * The popup menu returned by
	 * {@link RecentSearches#createPopupMenu(JXSearchField)}.
	 */
	public static class RecentSearchesPopup extends JPopupMenu implements ActionListener, ChangeListener {
		private RecentSearches recentSearches;

		private JTextField searchField;

		private JMenuItem clear;

		/**
		 * Creates a new popup menu based on the given {@link RecentSearches}
		 * and {@link JXSearchField}.
		 *
		 * @param recentSearches
		 * @param searchField
		 */
		public RecentSearchesPopup(RecentSearches recentSearches, JTextField searchField) {
			this.searchField = searchField;
			this.recentSearches = recentSearches;

			recentSearches.addChangeListener(this);
			buildMenu();
		}

		/**
		 * Rebuilds the menu according to the recent searches.
		 */
		private void buildMenu() {
			setVisible(false);
			removeAll();

			if (recentSearches.getLength() == 0) {
				JMenuItem noRecent = new JMenuItem(UIManagerExt.getString("SearchField.noRecentsText"));
				noRecent.setEnabled(false);
				add(noRecent);
			} else {
				JMenuItem recent = new JMenuItem(UIManagerExt.getString("SearchField.recentsMenuTitle"));
				recent.setEnabled(false);
				add(recent);

				for (String searchString : recentSearches.getRecentSearches()) {
					JMenuItem mi = new JMenuItem(searchString);
					mi.addActionListener(this);
					add(mi);
				}

				addSeparator();
				clear = new JMenuItem(UIManagerExt.getString("SearchField.clearRecentsText"));
				clear.addActionListener(this);
				add(clear);
			}
		}

		/**
		 * Sets {@link #searchField}s text to the {@link ActionEvent}s action
		 * command and call {@link JXSearchField#postActionEvent()} to fire an
		 * {@link ActionEvent}, if <code>e</code>s source is not the clear
		 * menu item. If the source is the clear menu item, all recent searches
		 * will be removed.
		 */
		@Override
        public void actionPerformed(ActionEvent e) {
			if (e.getSource() == clear) {
				recentSearches.removeAll();
			} else {
				searchField.setText(e.getActionCommand());
				searchField.postActionEvent();
			}
		}

		/**
		 * Every time the recent searches fires a {@link ChangeEvent} call
		 * {@link #buildMenu()} to rebuild the whole menu.
		 */
		@Override
        public void stateChanged(ChangeEvent e) {
			buildMenu();
		}
	}
}
