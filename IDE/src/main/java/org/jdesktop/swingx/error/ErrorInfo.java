/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.jdesktop.swingx.error;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;

import javax.swing.SwingUtilities;

/**
 * <p>A simple class that encapsulates all the information needed
 * to report a problem using the automated report/processing system.</p>
 *
 * <p>All HTML referred to in this API refers to version 3.2 of the HTML
 * markup specification.</p>
 *
 * @status REVIEWED
 * @author Alexander Zuev
 * @author rbair
 */
public class ErrorInfo {
    /**
     * Short string that will be used as a error title
     */
    private String title;
    /**
     * Basic message that describes incident
     */
    private String basicErrorMessage;
    /**
     * Message that will fully describe the incident with all the
     * available details
     */
    private String detailedErrorMessage;
    /**
     * A category name, indicating where in the application this incident
     * occurred. It is recommended that this be the same value as you
     * would use when logging.
     */
    private String category;
    /**
     * Optional Throwable that will be used as a possible source for
     * additional information
     */
    private Throwable errorException;
    /**
     * Used to specify how bad this error was.
     */
    private Level errorLevel;
    /**
     *  A Map which captures the state of the application
     *  at the time of an exception. This state is then available for error
     *  reports.
     */
    private Map<String,String> state;

    /**
     * Creates a new ErrorInfo based on the provided data.
     *
     * @param title                 used as a quick reference for the
     *                              error (for example, it might be used as the
     *                              title of an error dialog or as the subject of
     *                              an email message). May be null.
     *
     * @param basicErrorMessage     short description of the problem. May be null.
     *
     * @param detailedErrorMessage  full description of the problem. It is recommended,
     *                              though not required, that this String contain HTML
     *                              to improve the look and layout of the detailed
     *                              error message. May be null.
     *
     * @param category              A category name, indicating where in the application
     *                              this incident occurred. It is recommended that
     *                              this be the same value as you would use when logging.
     *                              May be null.
     *
     * @param errorException        <code>Throwable</code> that can be used as a
     *                              source for additional information such as call
     *                              stack, thread name, etc. May be null.
     *
     * @param errorLevel            any Level (Level.SEVERE, Level.WARNING, etc).
     *                              If null, then the level will be set to SEVERE.
     *
     * @param state                 the state of the application at the time the incident occured.
     *                              The standard System properties are automatically added to this
     *                              state, and thus do not need to be included. This value may be null.
     *                              If null, the resulting map will contain only the System properties.
     *                              If there is a value in the map with a key that also occurs in the
     *                              System properties (for example: sun.java2d.noddraw), then the
     *                              developer supplied value will be used. In other words, defined
     *                              parameters override standard ones. In addition, the keys
     *                              "System.currentTimeMillis" and "isOnEDT" are both defined
     *                              automatically.
     */
    public ErrorInfo(String title, String basicErrorMessage, String detailedErrorMessage,
            String category, Throwable errorException, Level errorLevel, Map<String,String> state) {
        this.title = title;
        this.basicErrorMessage = basicErrorMessage;
        this.detailedErrorMessage = detailedErrorMessage;
        this.category = category;
        this.errorException = errorException;
        this.errorLevel = errorLevel == null ? Level.SEVERE : errorLevel;
        this.state = new HashMap<String,String>();

        //first add all the System properties
        try {
            //NOTE: This is not thread safe because System.getProperties() does not appear
            //to create a copy of the map. Thus, another thread could be modifying the System
            //properties and the "state" at the time of this exception may not be
            //accurate!
            Properties props = System.getProperties();
            for (Map.Entry<Object, Object> entry : props.entrySet()) {
                String key = entry.getKey() == null ? null : entry.getKey().toString();
                String val = entry.getKey() == null ? null : entry.getValue().toString();
                if (key != null) {
                    this.state.put(key, val);
                }
            }
        } catch (SecurityException e) {
            //probably running in a sandbox, don't worry about this
        }

        //add the automatically supported properties
        this.state.put("System.currentTimeMillis", "" + System.currentTimeMillis());
        this.state.put("isOnEDT", "" + SwingUtilities.isEventDispatchThread());

        //now add all the data in the param "state". Thus, if somebody specified a key in the
        //state map, it overrides whatever was in the System map
        if (state != null) {
            for (Map.Entry<String,String> entry : state.entrySet()) {
                this.state.put(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Gets the string to use for a dialog title or other quick reference. Used
     * as a quick reference for the incident. For example, it might be used as the
     * title of an error dialog or as the subject of an email message.
     *
     * @return quick reference String. May be null.
     */
    public String getTitle() {
        return title;
    }

    /**
     * <p>Gets the basic error message. This message should be clear and user oriented.
     * This String may have HTML formatting, but any such formatting should be used
     * sparingly. Generally, such formatting makes sense for making certain words bold,
     * but should not be used for page layout or other such things.</p>
     *
     * <p>For example, the following are perfectly acceptable basic error messages:
     * <pre>
     *      "Your camera cannot be located. Please make sure that it is powered on
     *       and that it is connected to this computer. Consult the instructions
     *       provided with your camera to make sure you are using the appropriate
     *       cable for attaching the camera to this computer"
     *
     *      "&lt;html&gt;You are running on &lt;b&gt;reserver&lt;/b&gt; battery
     *       power. Please plug into a power source immediately, or your work may
     *       be lost!&lt;/html&gt;"
     * </pre></p>
     *
     * @return basic error message or null
     */
    public String getBasicErrorMessage() {
        return basicErrorMessage;
    }

    /**
     * <p>Gets the detailed error message. Unlike {@link #getBasicErrorMessage},
     * this method may return a more technical message to the user. However, it
     * should still be user oriented. This String should be formatted using basic
     * HTML to improve readability as necessary.</p>
     *
     * <p>This method may return null.</p>
     *
     * @return detailed error message or null
     */
    public String getDetailedErrorMessage() {
        return detailedErrorMessage;
    }

    /**
     * Gets the category name. This value indicates where in the application
     * this incident occurred. It is recommended that this be the same value as
     * you would use when logging. This may be null.
     *
     * @return the category. May be null.
     */
    public String getCategory() {
        return category;
    }

    /**
     * Gets the actual exception that generated the error. If this returns a
     * non null value, then {@link #getBasicErrorMessage} may return a null value.
     * If this returns a non null value and {@link #getDetailedErrorMessage} returns
     * a null value, then this returned <code>Throwable</code> may be used as the
     * basis for the detailed error message (generally by showing the stack trace).
     *
     * @return exception or null
     */
    public Throwable getErrorException() {
        return errorException;
    }

    /**
     * Gets the severity of the error. The default level is <code>Level.SEVERE</code>,
     * but any {@link Level} may be specified when constructing an
     * <code>ErrorInfo</code>.
     *
     * @return the error level. This will never be null
     */
    public Level getErrorLevel() {
        return errorLevel;
    }

    /**
     * <p>Gets a copy of the application state at the time that the incident occured.
     * This map will never be null. If running with appropriate permissions the
     * map will contain all the System properties. In addition, it contains two
     * keys, "System.currentTimeMillis" and "isOnEDT".</p>
     *
     * <p>Warning: The System.properties <em>may not</em> contain the exact set
     * of System properties at the time the exception occured. This is due to the
     * nature of System.getProperties() and the Properties collection. While they
     * are property synchronized, it is possible that while iterating the set of
     * properties in the ErrorInfo constructor that some other code can change
     * the properties on another thread. This is unlikely to occur, but in some
     * applications <em>may</em> occur.</p>
     *
     * @return a copy of the application state. This will never be null.
     */
    public Map<String,String> getState() {
        return new HashMap<String,String>(state);
    }
}
