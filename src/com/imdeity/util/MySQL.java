package com.imdeity.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.ArrayList;
import java.sql.PreparedStatement;

import com.avaje.ebeaninternal.server.lib.sql.DataSourceException;

public class MySQL {
    private Connection conn;
    private String plugin = "[Lottery]";

    public MySQL() {

        // Load the driver instance
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
        } catch (Exception ex) {
            throw new DataSourceException(plugin
                    + " Failed to initialize JDBC driver");
        }

        // make the connection
        try {
            conn = DriverManager.getConnection(getConnectionString());
            System.out.println(plugin + " Connection success!");
        } catch (SQLException ex) {
            dumpSqlException(ex);
            throw new DataSourceException(plugin
                    + " Failed to create connection to Mysql database");
        }

        validateDatabaseTables();
    }

    /*
     * Creates the Database tables if none exist
     */
    public void validateDatabaseTables() {

        Write("CREATE TABLE IF NOT EXISTS " + tableName("players") + " ("
                + "`id` int(10) unsigned NOT NULL AUTO_INCREMENT,"
                + "`username` varchar(16) NOT NULL,"
                + "PRIMARY KEY (`id`)) ENGINE=MyISAM DEFAULT CHARSET=latin1 "
                + "COMMENT='Current lottery players log';");

        Write("CREATE TABLE IF NOT EXISTS " + tableName("winners") + " ("
                + "`id` int(10) unsigned NOT NULL AUTO_INCREMENT,"
                + "`username` VARCHAR( 16 ) NOT NULL ,"
                + "`winnings` INT( 10 ) NOT NULL DEFAULT  '0' ,"
                + "`time` TIMESTAMP NOT NULL,"
                + "PRIMARY KEY (`id`)) ENGINE=MyISAM DEFAULT CHARSET=latin1 "
                + "COMMENT='Past Lottery winners.';");
    }

    /*
     * Check if the connection is closed
     */
    private void reconnect() {
        System.out.println(plugin + " Reconnecting to MySQL...");
        try {
            conn = DriverManager.getConnection(getConnectionString());
            System.out.println(plugin + " Connection success!");
        } catch (SQLException ex) {
            System.out
                    .println(plugin
                            + " Connection to MySQL failed! Check status of MySQL server!");
            dumpSqlException(ex);
        }
    }

    /*
     * Prepares MySQL Statement to help catch SQL Injections
     */
    private PreparedStatement prepareSqlStatement(String sql, Object[] params)
            throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(sql);

        int counter = 1;

        for (Object param : params) {
            if (param instanceof Integer) {
                stmt.setInt(counter++, (Integer) param);
            } else if (param instanceof Long) {
                stmt.setLong(counter++, (Long) param);
            } else if (param instanceof Double) {
                stmt.setDouble(counter++, (Double) param);
            } else if (param instanceof String) {
                stmt.setString(counter++, (String) param);
            } else {
                System.out.printf(plugin
                        + " Database -> Unsupported data type %s", param
                        .getClass().getSimpleName());
            }
        }
        return stmt;
    }

    /*
     * write query
     */
    public boolean Write(String sql, Object... params) {
        try {
            ensureConnection();
            PreparedStatement stmt = prepareSqlStatement(sql, params);
            stmt.executeUpdate();
            return true;
        } catch (SQLException ex) {
            dumpSqlException(ex);
            return false;
        }
    }

    /*
     * Write query
     */
    public boolean WriteNoError(String sql) {
        try {
            PreparedStatement stmt = null;
            stmt = this.conn.prepareStatement(sql);
            stmt.executeUpdate();
            return true;
        } catch (SQLException ex) {
            return false;
        }
    }

    /*
     * Returns the Connection String
     */
    private String getConnectionString() {
        return "jdbc:mysql://" + Settings.getMySQLServerAddress() + ":"
                + Settings.getMySQLServerPort() + "/"
                + Settings.getMySQLDatabaseName() + "?user="
                + Settings.getMySQLUsername() + "&password="
                + Settings.getMySQLPassword();
    }

    /*
     * Returns formatted table names
     */
    public static String tableName(String nameOfTable) {
        return (String.format("`%s`.`%s`", Settings.getMySQLDatabaseName(),
                Settings.getMySQLDatabaseTablePrefix() + nameOfTable));
    }

    private void dumpSqlException(SQLException ex) {
        System.out.println(plugin + " SQLException: " + ex.getMessage());
        System.out.println(plugin + " SQLState: " + ex.getSQLState());
        System.out.println(plugin + " VendorError: " + ex.getErrorCode());
        ex.printStackTrace();
    }

    private void ensureConnection() {
        try {
            if (!conn.isValid(5)) {
                reconnect();
            }
        } catch (SQLException ex) {
            dumpSqlException(ex);
        }
    }

    /*
     * Get integer, Only return the first row/field
     */
    public Integer GetInt(String sql) {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Integer result = 0;

        /*
         * Double check connection to MySQL
         */
        try {
            if (!conn.isValid(5)) {
                reconnect();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            stmt = this.conn.prepareStatement(sql);
            if (stmt.executeQuery() != null) {
                stmt.executeQuery();
                rs = stmt.getResultSet();
                if (rs.next()) {
                    result = rs.getInt(1);
                } else {
                    result = 0;
                }
            }
        } catch (SQLException ex) {
            System.out.println(plugin + " SQLException: " + ex.getMessage());
            System.out.println(plugin + " SQLState: " + ex.getSQLState());
            System.out.println(plugin + " VendorError: " + ex.getErrorCode());
        }

        return result;
    }

    /*
     * Read query
     */
    public HashMap<Integer, ArrayList<String>> Read(String sql) {

        /*
         * Double check connection to MySQL
         */
        try {
            if (!conn.isValid(5)) {
                reconnect();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        PreparedStatement stmt = null;
        ResultSet rs = null;
        HashMap<Integer, ArrayList<String>> Rows = new HashMap<Integer, ArrayList<String>>();

        try {
            stmt = this.conn.prepareStatement(sql);
            if (stmt.executeQuery() != null) {
                stmt.executeQuery();
                rs = stmt.getResultSet();
                while (rs.next()) {
                    ArrayList<String> Col = new ArrayList<String>();
                    for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                        Col.add(rs.getString(i));
                    }
                    Rows.put(rs.getRow(), Col);
                }
            }
        } catch (SQLException ex) {
            System.out.println(plugin + " SQLException: " + ex.getMessage());
            System.out.println(plugin + " SQLState: " + ex.getSQLState());
            System.out.println(plugin + " VendorError: " + ex.getErrorCode());
        } finally {
            // release dataset
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException sqlEx) {
                } // ignore
                rs = null;
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException sqlEx) {
                } // ignore
                stmt = null;
            }
        }
        return Rows;
    }

}
