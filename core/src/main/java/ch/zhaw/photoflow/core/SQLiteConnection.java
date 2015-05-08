package ch.zhaw.photoflow.core;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.sqlite.SQLiteConfig;

public class SQLiteConnection {

	private static SQLiteConnection instance = new SQLiteConnection();

	public static final String DB_BASE_URL = "jdbc:sqlite:";
	public static final String DRIVER_CLASS = "org.sqlite.JDBC";

	private Connection connection = null;

	private SQLiteConnection() {
		try {
			Class.forName(DRIVER_CLASS);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	private Connection createConnection() throws SQLException {
		if (this.connection == null || this.connection.isClosed()) {
			SQLiteConfig config = new SQLiteConfig();
			config.enforceForeignKeys(true);
			
			try {
				this.connection = DriverManager.getConnection(DB_BASE_URL + FileHandler.sqliteFile(), config.toProperties());
			} catch (FileHandlerException e) {
				throw new RuntimeException(e);
			}
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
