package ch.zhaw.photoflow.core;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLiteConnection {

	private static SQLiteConnection instance = new SQLiteConnection();
	
    public static final String URL = "jdbc:mysql://localhost/jdbcdb";
    public static final String USER = "YOUR_DATABASE_USERNAME";
    public static final String PASSWORD = " YOUR_DATABASE_PASSWORD";
    public static final String DRIVER_CLASS = "org.sqlite.JDBC";
    
    private Connection connection = null;
    private String sqlitePath;
	
    private SQLiteConnection() {
	    FileHandler filehandler = new FileHandler();
	    this.sqlitePath = filehandler.getSQLitePath();
	    
        try {
            Class.forName(DRIVER_CLASS);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    private Connection createConnection() throws SQLException {
    	if (this.connection == null) {
    		this.connection = DriverManager.getConnection("jdbc:sqlite:" + sqlitePath);
    	}
    	
   		return connection;
    }
    
    public static Connection getConnection() throws SQLException {
    	return instance.createConnection();
    }
    
    public static void close() throws SQLException {
    	instance.connection.close();
    }
    
}
