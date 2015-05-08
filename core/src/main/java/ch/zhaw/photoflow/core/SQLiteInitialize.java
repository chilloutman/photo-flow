package ch.zhaw.photoflow.core;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Statement;

public class SQLiteInitialize {

	public static void initialize() {

		Connection c = null;
		Statement stmt = null;
		try {
			// Open Connection
			c = SQLiteConnection.getConnection();
			System.out.println("Opened database successfully");

			// Load dbscript
			URL dbScriptURL = SQLiteInitialize.class.getResource(("SQLite/db_schema_script.db"));
			Path dbScriptPath = Paths.get(dbScriptURL.toURI());
			String sqlCreateScript = new String(Files.readAllBytes(dbScriptPath));

			// Execute dbscript
			stmt = c.createStatement();
			stmt.executeUpdate(sqlCreateScript);
			stmt.close();

			// close connection
			c.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
		System.out.println("Table created successfully");
	}
}
