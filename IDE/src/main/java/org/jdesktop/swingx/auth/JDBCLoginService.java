/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.auth;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.InitialContext;

import org.jdesktop.beans.JavaBean;
/**
 * A login service for connecting to SQL based databases via JDBC
 *
 * @author rbair
 */
@JavaBean
public class JDBCLoginService extends LoginService {
    private static final Logger LOG = Logger.getLogger(JDBCLoginService.class
            .getName());

    /**
     * The connection to the database
     */
    private Connection conn;
    /**
     * If used, defines the JNDI context from which to get a connection to
     * the data base
     */
    private String jndiContext;
    /**
     * When using the DriverManager to connect to the database, this specifies
     * any additional properties to use when connecting.
     */
    private Properties properties;

    /**
     * Create a new JDBCLoginService and initializes it to connect to a
     * database using the given params.
     * @param driver
     * @param url
     */
    public JDBCLoginService(String driver, String url) {
        super(url);
        try {
            Class.forName(driver);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "The driver passed to the " +
                    "JDBCLoginService constructor could not be loaded. " +
                    "This may be due to the driver not being on the classpath", e);
        }
        this.setUrl(url);
    }

    /**
     * Create a new JDBCLoginService and initializes it to connect to a
     * database using the given params.
     * @param driver
     * @param url
     * @param props
     */
    public JDBCLoginService(String driver, String url, Properties props) {
        super(url);
        try {
            Class.forName(driver);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "The driver passed to the " +
                    "JDBCLoginService constructor could not be loaded. " +
                    "This may be due to the driver not being on the classpath", e);
        }
        this.setUrl(url);
        this.setProperties(props);
    }

    /**
     * Create a new JDBCLoginService and initializes it to connect to a
     * database using the given params.
     * @param jndiContext
     */
    public JDBCLoginService(String jndiContext) {
        super(jndiContext);
        this.jndiContext = jndiContext;
    }

    /**
     * Default JavaBean constructor
     */
    public JDBCLoginService() {
        super();
    }

    /**
     * @return the JDBC connection url
     */
    public String getUrl() {
        return getServer();
    }

    /**
     * @param url set the JDBC connection url
     */
    public void setUrl(String url) {
        String old = getUrl();
        setServer(url);
        firePropertyChange("url", old, getUrl());
    }

    /**
     * @return JDBC connection properties
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * @param properties miscellaneous JDBC properties to use when connecting
     *        to the database via the JDBC driver
     */
    public void setProperties(Properties properties) {
        Properties old = getProperties();
        this.properties = properties;
        firePropertyChange("properties", old, getProperties());
    }

    public Connection getConnection() {
        return conn;
    }

    public void setConnection(Connection conn) {
        Connection old = getConnection();
        this.conn = conn;
        firePropertyChange("connection", old, getConnection());
    }

    /**
     * Attempts to get a JDBC Connection from a JNDI javax.sql.DataSource, using
     * that connection for interacting with the database.
     * @throws Exception
     */
    private void connectByJNDI(String userName, char[] password) throws Exception {
        InitialContext ctx = new InitialContext();
        javax.sql.DataSource ds = (javax.sql.DataSource)ctx.lookup(jndiContext);
        conn = ds.getConnection(userName, new String(password));
        conn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
    }

    /**
     * Attempts to get a JDBC Connection from a DriverManager. If properties
     * is not null, it tries to connect with those properties. If that fails,
     * it then attempts to connect with a user name and password. If that fails,
     * it attempts to connect without any credentials at all.
     * <p>
     * If, on the other hand, properties is null, it first attempts to connect
     * with a username and password. Failing that, it tries to connect without
     * any credentials at all.
     * @throws Exception
     */
    private void connectByDriverManager(String userName, char[] password) throws Exception {
        if (getProperties() != null) {
            try {
                conn = DriverManager.getConnection(getUrl(), getProperties());
                conn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            } catch (Exception e) {
                try {
                    conn = DriverManager.getConnection(getUrl(), userName, new String(password));
                    conn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
                } catch (Exception ex) {
                    conn = DriverManager.getConnection(getUrl());
                    conn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
                }
            }
        } else {
            try {
                conn = DriverManager.getConnection(getUrl(), userName, new String(password));
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Connection with properties failed. " +
                                "Tryint to connect without.", e);
                //try to connect without using the userName and password
                conn = DriverManager.getConnection(getUrl());

            }
        }
    }

    /**
     * @param name user name
     * @param password user password
     * @param server Must be either a valid JDBC URL for the type of JDBC driver you are using,
     * or must be a valid JNDIContext from which to get the database connection
     */
    @Override
    public boolean authenticate(String name, char[] password, String server) throws Exception {
        //try to form a connection. If it works, conn will not be null
        //if the jndiContext is not null, then try to get the DataSource to use
        //from jndi
        if (jndiContext != null) {
            try {
                connectByJNDI(name, password);
            } catch (Exception e) {
                try {
                    connectByDriverManager(name, password);
                } catch (Exception ex) {
                    LOG.log(Level.WARNING, "Login failed", ex);
                    //login failed
                    return false;
                }
            }
        } else {
            try {
                connectByDriverManager(name, password);
            } catch (Exception ex) {
                LOG.log(Level.WARNING, "", ex);
                return false;
            }
        }
        return true;
    }
}
