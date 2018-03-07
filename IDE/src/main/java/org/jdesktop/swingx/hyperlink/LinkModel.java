/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.jdesktop.swingx.hyperlink;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

/**
 * An bean which represents an URL link.
 *
 * Text, URL and visited are bound properties. Compares by Text.
 *
 * @author Mark Davidson
 * @author Jeanette Winzenburg
 */
public class LinkModel implements Comparable {

    private static final Logger LOG = Logger.getLogger(LinkModel.class
            .getName());

    private String text; // display text

    private URL url; // url of the link

    private String target; // url target frame

    private boolean visited = false;

    private PropertyChangeSupport propertyChangeSupport;

    public static final String VISITED_PROPERTY = "visited";

    // hack - this class assumes that the url always != null
    // need to cleanup
    private static String defaultURLString = "https://jdnc.dev.java.net";

    private static URL defaultURL;

    /**
     *
     * @param text
     * @param target
     * @param url
     */
    public LinkModel(String text, String target, URL url) {
        setText(text);
        setTarget(target);
        setURL(url != null ? url : getDefaultURL());
    }

    public LinkModel() {
        this(" ", null, null);
    }

    public LinkModel(String text) {
        this(text, null, null);
    }

    /**
     * @param text text to that a renderer would display
     * @param target the target that a URL should load into.
     * @param template a string that represents a URL with
     * &amp;{N} place holders for string substitution
     * @param args an array of strings which will be used for substitition
     */
    public LinkModel(String text, String target, String template, String[] args) {
        setText(text);
        setTarget(target);
        setURL(createURL(template, args));
    }

    /**
     * Set the display text.
     */
    public void setText(String text) {
        String old = getText();
        this.text = text;
        firePropertyChange("text", old, getText());
    }

    public String getText() {
        if (text != null) {
            return text;
        } else if (url != null) {
            return getURL().toString();
        }
        return null;
    }

    public void setURLString(String howToURLString) {
        URL url = null;
        try {
            url = new URL(howToURLString);
        } catch (MalformedURLException e) {
            url = getDefaultURL();
            LOG.warning("the given urlString is malformed: " + howToURLString +
                    "\n falling back to default url: " + url);
        }
        setURL(url);
    }

    private URL getDefaultURL() {
        if (defaultURL == null) {
            try {
                defaultURL = new URL(defaultURLString);
            } catch (MalformedURLException e) {
                LOG.fine("should not happen - defaultURL is wellFormed: "
                        + defaultURLString);
            }
        }
        return defaultURL;
    }

    /**
     * Set the url and resets the visited flag.
     *
     * Think: keep list of visited urls here?
     */
    public void setURL(URL url) {
        if (url == null) {
            throw new IllegalArgumentException("URL for link cannot be null");
        }
        if (url.equals(getURL()))
            return;
        URL old = getURL();
        this.url = url;
        firePropertyChange("URL", old, url);
        setVisited(false);
    }

    public URL getURL() {
        return url;
    }

    /**
     * Create a URL from a template string that has place holders and an array
     * of strings which will be substituted into the place holders. The place
     * holders are represented as
     *
     * @{N} where N = { 1..n }
     *      <p>
     *      For example, if the template contains a string like:
     *      http://bugz.sfbay/cgi-bin/showbug?cat=@{1}&sub_cat=@{2} and a two
     *      arg array contains: java, classes_swing The resulting URL will be:
     *      http://bugz.sfbay/cgi-bin/showbug?cat=java&sub_cat=classes_swing
     *      <p>
     * @param template a url string that contains the placeholders
     * @param args an array of strings that will be substituted
     */
    private URL createURL(String template, String[] args) {
        URL url = null;
        try {
            String urlStr = template;
            for (int i = 0; i < args.length; i++) {
                urlStr = urlStr.replaceAll("@\\{" + (i + 1) + "\\}", args[i]);
            }
            url = new URL(urlStr);
        } catch (MalformedURLException ex) {
            //
        }
        return url;
    }

    /**
     * Set the target that the URL should load into. This can be a uri
     * representing another control or the name of a window or special targets.
     * See: http://www.w3c.org/TR/html401/present/frames.html#adef-target
     */
    public void setTarget(String target) {
        this.target = target;
    }

    /**
     * Return the target for the URL.
     *
     * @return value of the target. If null then "_blank" will be returned.
     */
    public String getTarget() {
        if (target != null) {
            return target;
        } else {
            return "_blank";
        }
    }

    /**
     * Sets a flag to indicate if the link has been visited. The state of this
     * flag can be used to render the color of the link.
     */
    public void setVisited(boolean visited) {
        boolean old = getVisited();
        this.visited = visited;
        firePropertyChange(VISITED_PROPERTY, old, getVisited());
    }

    public boolean getVisited() {
        return visited;
    }

    // ---------------------- property change notification

    public void addPropertyChangeListener(PropertyChangeListener l) {
        getPropertyChangeSupport().addPropertyChangeListener(l);

    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        if (propertyChangeSupport == null)
            return;
        propertyChangeSupport.removePropertyChangeListener(l);
    }

    protected void firePropertyChange(String property, Object oldValue,
            Object newValue) {
        if (propertyChangeSupport == null)
            return;
        propertyChangeSupport.firePropertyChange(property, oldValue, newValue);
    }

    protected void firePropertyChange(String property, boolean oldValue,
            boolean newValue) {
        if (propertyChangeSupport == null)
            return;
        propertyChangeSupport.firePropertyChange(property, oldValue, newValue);

    }

    private PropertyChangeSupport getPropertyChangeSupport() {
        if (propertyChangeSupport == null) {
            propertyChangeSupport = new PropertyChangeSupport(this);
        }
        return propertyChangeSupport;
    }

    // Comparable interface for sorting.
    public int compareTo(Object obj) {
        if (obj == null) {
            return 1;
        }
        if (obj == this) {
            return 0;
        }
        return text.compareTo(((LinkModel) obj).text);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj != null && obj instanceof LinkModel) {
            LinkModel other = (LinkModel) obj;
            if (!getText().equals(other.getText())) {
                return false;
            }

            if (!getTarget().equals(other.getTarget())) {
                return false;
            }

            return getURL().equals(other.getURL());
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = 7;

        result = 37 * result + ((getText() == null) ? 0 : getText().hashCode());
        result = 37 * result
                + ((getTarget() == null) ? 1 : getTarget().hashCode());
        result = 37 * result + ((getURL() == null) ? 2 : getURL().hashCode());

        return result;
    }

    @Override
    public String toString() {

        StringBuffer buffer = new StringBuffer("[");
        // RG: Fix for J2SE 5.0; Can't cascade append() calls because
        // return type in StringBuffer and AbstractStringBuilder are different
        buffer.append("url=");
        buffer.append(url);
        buffer.append(", target=");
        buffer.append(target);
        buffer.append(", text=");
        buffer.append(text);
        buffer.append("]");

        return buffer.toString();
    }

}
