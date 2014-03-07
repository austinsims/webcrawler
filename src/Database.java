import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class Database {
	Connection conn;

	/**
	 * Create a Database object from a properties file containing the info to
	 * connect to the database, namely jdbc.url, jdbc.username and
	 * jdbc.password. If the server does not already contain a database, create
	 * it and the needed tables.
	 * 
	 * @param propFile contains server info
	 * @throws IOException
	 */
	
	/**
	 * Does the database with the name exist?
	 * @param conn
	 * @param db Which database?
	 */
	public boolean doesDatabaseExist(String db) {
		try {
			Statement s = conn.createStatement();
			String SQL_CHECK_DB_EXIST = String.format("SELECT schema_name FROM information_schema.schemata WHERE schema_name = '%s'", db);
			ResultSet rs =  s.executeQuery(SQL_CHECK_DB_EXIST);
			return rs.first();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	Database(String propFile) throws IOException, SQLException {
		Properties props = new Properties();
		FileInputStream in = new FileInputStream(propFile);
		props.load(in);

		String url = props.getProperty("jdbc.url");
		String user = props.getProperty("jdbc.username");
		String password = props.getProperty("jdbc.password");

		conn = DriverManager.getConnection(url, user, password);
		
		// Set up schema if necessary
		if (!doesDatabaseExist("crawler")) {
			setupSchema();
		} else {
			conn.createStatement().execute("use crawler;");
		}
		
		in.close();
	}
	
	Database(String url, String user, String password) throws SQLException {
		conn = DriverManager.getConnection(url, user, password);
		
		// Set up schema if necessary
		if (!doesDatabaseExist("crawler")) {
			setupSchema();
		}
		conn.createStatement().execute("use database crawler;");
	}
	
	private void setupSchema() throws SQLException {
		Statement s = conn.createStatement();
		s.addBatch("CREATE DATABASE crawler;");
		s.addBatch("use crawler;");
		s.addBatch("CREATE TABLE url ( urlid INT NOT NULL AUTO_INCREMENT, url VARCHAR(255) UNIQUE NOT NULL, description TEXT, PRIMARY KEY (urlid));");
		s.addBatch("CREATE TABLE word ( word VARCHAR(50) NOT NULL, urlid INT NOT NULL, FOREIGN KEY (urlid)  REFERENCES url(urlid), PRIMARY KEY (word, urlid) );");
		s.executeBatch();
	}
	
	/**
	 * Delete the database and recreate the schema
	 * @throws SQLException 
	 */
	public void reset() throws SQLException {
		Statement s = conn.createStatement();
		s.execute("DROP DATABASE crawler");
		setupSchema();
	}
	
	public boolean hasURL(String url) throws SQLException {
		try {
			Statement s = conn.createStatement();
			ResultSet rs =  s.executeQuery(String.format("SELECT * FROM url WHERE url LIKE '%s'", url));
			return rs.first();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Insert new url without a description and return its id.
	 */
	public int insertURL(String url) throws SQLException {
		if (hasURL(url)) {
			Statement s = conn.createStatement();
			ResultSet rs = s.executeQuery(String.format("SELECT urlid FROM url WHERE url LIKE '%s'", url));
			rs.first();
			return rs.getInt(1);
		} else {
			Statement s = conn.createStatement();
			s.executeUpdate(String.format("INSERT INTO url VALUES (NULL, '%s', NULL)", url), Statement.RETURN_GENERATED_KEYS);
			ResultSet genKeys = s.getGeneratedKeys();
			genKeys.first();
			return genKeys.getInt(1);
		}
	}
	
	public void insertWord(String word, int urlID) throws SQLException {
		Statement s = conn.createStatement();
		s.executeUpdate(String.format("INSERT IGNORE INTO word VALUES ('%s', %d)", word, urlID));
	}
	
	public Set<URL> search(String keywords) throws SQLException {
		Set<URL> results = null;
		boolean firstWord = true;
		for (String word : keywords.split(" ")) {
			Statement st = conn.createStatement();
			String query = String.format(
					"SELECT url " + 
					"FROM url JOIN word ON (url.urlid = word.urlid) " +
					"WHERE word LIKE '%s';",
					word);
			ResultSet rs = st.executeQuery(query);
			Set<URL> s = new HashSet<URL>();
			while (rs.next()) {
				try {
					s.add(new URL(rs.getString(1)));
				} catch (MalformedURLException e) {
					continue;
				}
			}
			if (firstWord) {
				results = s;
				firstWord = false;
			} else {
				results.retainAll(s);
			}
		}
		return results;
	}
	
}
























