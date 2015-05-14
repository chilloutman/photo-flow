package ch.zhaw.photoflow.core.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.sqlite.SQLiteConfig;

import ch.zhaw.photoflow.core.FileHandler;
import ch.zhaw.photoflow.core.FileHandlerException;

/**
 * Manages SQL {@link Connection connections}.
 */
public class SQLiteConnectionProvider {

	private static final String DB_BASE_URL = "jdbc:sqlite:";
	private static final String DRIVER_CLASS = "org.sqlite.JDBC";

	private Connection connection = null;

	/**
	 * Prepare by loading the driver class.
	 */
	public SQLiteConnectionProvider() {
		try {
			Class.forName(DRIVER_CLASS);
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * @return A new connection to the Photo Flow SQLite database.
	 * @throws SQLException When a connection could not be created.
	 */
	private Connection createConnection() throws SQLException {
		SQLiteConfig config = new SQLiteConfig();
		config.enforceForeignKeys(true);

		try {
			this.connection = DriverManager.getConnection(DB_BASE_URL + FileHandler.sqliteFile(), config.toProperties());
		} catch (FileHandlerException e) {
			throw new RuntimeException(e);
		}
		return connection;
	}

	/**
	 * @return An existing open {@link Connection connection} or a new connection. The caller is responsible for closing this connection!
	 * @throws SQLException When a connection could not be created.
	 */
	public Connection getConnection() throws SQLException {
		if (connection == null || connection.isClosed()) {
			connection = createConnection();
		}
		return connection;
	}

}
