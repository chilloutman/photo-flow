package ch.zhaw.photoflow.core.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.sqlite.SQLiteConfig;

import ch.zhaw.photoflow.core.FileHandler;
import ch.zhaw.photoflow.core.FileHandlerException;

/**
 * Manages the connection pool.
 */
public class SQLiteConnectionProvider {

	public static final String DB_BASE_URL = "jdbc:sqlite:";
	public static final String DRIVER_CLASS = "org.sqlite.JDBC";

	private Connection connection = null;

	public SQLiteConnectionProvider() {
		try {
			Class.forName(DRIVER_CLASS);
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Creates a {@link Connection} to the photoflow sqlite database if necessary. Else the existing {@link Connection} will be returned. 
	 * @return {@link Connection}
	 * @throws SQLException
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
	 * @return A ready-to use {@link Connection} of the connection pool. The caller is responsible for closing this connection!
	 * @throws SQLException
	 */
	public Connection getConnection() throws SQLException {
		if (connection == null || connection.isClosed()) {
			connection = createConnection();
		}
		return connection;
	}

}
