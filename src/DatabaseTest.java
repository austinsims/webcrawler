import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DatabaseTest {
	
	final String JDBC_URL = "jdbc:mysql://localhost:3306";
	Connection conn;
	
	@Before
	public void setUp() throws Exception {
		conn = DriverManager.getConnection(JDBC_URL, "root", "");
	}

	@After
	public void tearDown() throws Exception {
		conn.close();
	}

	@Test
	public void testDoesDatabaseExist() {
		try {
			// Create a database and assert that it created its own database on the server
			Database db = new Database(JDBC_URL, "root", "", "test");
			assertTrue(db.doesDatabaseExist("crawler"));
			
			// drop it manually, then check to see that it is gone
			conn.createStatement().execute("DROP DATABASE crawler;");
			assertTrue(!db.doesDatabaseExist("crawler"));
			
		} catch (SQLException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testDatabaseConstructor() {
		try {
			Database db = new Database("jdbc:mysql://localhost:3306", "root", "", "test");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void testReset() {
		fail("Not yet implemented");
	}

	@Test
	public void testUrlInDB() {
		fail("Not yet implemented");
	}

	@Test
	public void testInsertURLWithoutDesc() {
		fail("Not yet implemented");
	}

	@Test
	public void testInsertURLWithDesc() {
		fail("Not yet implemented");
	}

}
