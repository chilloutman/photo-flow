package ch.zhaw.photoflow.core.dao;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import com.google.common.io.Resources;

/**
 * Initializes a SQLite database.
 */
public class SQLiteInitialize {

	/**
	 * Initializes a SQLite database with the database script provided in Resources folder.
	 */
	public static void initialize(SQLiteConnectionProvider provider) {
		try {
			// Load db script
			URL scriptUrl = SQLiteInitialize.class.getResource("db_schema_script.db");
			String sqlCreateScript = Resources.toString(scriptUrl, StandardCharsets.UTF_8);

			// Execute db script
			try (Connection connection = provider.getConnection()) {
				Statement statement = connection.createStatement();
				statement.executeUpdate(sqlCreateScript);
				statement.close();
			}
			
			System.out.println("Database initialized");
		} catch (SQLException | IOException e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			throw new IllegalStateException(e);
		}
	}
}
