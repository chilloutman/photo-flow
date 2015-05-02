package ch.zhaw.photoflow.core;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class SQLiteInitialize {

	public static void initialize() {
		
	    Connection c = null;
	    Statement stmt = null;
	    try {
	      Class.forName("org.sqlite.JDBC");
	      FileHandler filehandler = new FileHandler();
	      filehandler.createSQLitePath();
	      String sqlitePath = filehandler.getSQLitePath();
	      c = DriverManager.getConnection("jdbc:sqlite:" + sqlitePath);
	      System.out.println("Opened database successfully");

	      stmt = c.createStatement();
	      URL dbScriptURL = SQLiteInitialize.class.getResource(("SQLite/db_schema_script.db"));
	      Path dbScriptPath = Paths.get(dbScriptURL.toURI());
	      String sqlCreateScript = new String(Files.readAllBytes(dbScriptPath));
	      stmt.executeUpdate(sqlCreateScript);
	      stmt.close();
	      c.close();
	    } catch ( Exception e ) {
	      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
	      System.exit(0);
	    }
	    System.out.println("Table created successfully");
	}
}
