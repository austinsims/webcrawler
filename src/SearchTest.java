import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class SearchTest {
	Crawler crawler;
	Connection conn;
	Database db;
	
	private static final String SOLSYS_URL = "http://localhost/";
	private static final String VENUS_URL = "http://localhost/venus/index.html";
	private static final String EARTH_URL = "http://localhost/earth/index.html";
	private static final String MARS_URL = "http://localhost/mars/index.html";
	
	@Before
	public void setUp() throws IOException, SQLException {
		crawler = new Crawler("jdbc:mysql://localhost:3306", "test", "root", "", 100, "", "http://localhost");
		
		// Don't crawl mailto links
		crawler.addRule(new Rule() {
			@Override
			public boolean check(String url) {
				return !url.contains("mailto:");
			}
		});

		// Don't crawl anchor links
		crawler.addRule(new Rule() {
			@Override
			public boolean check(String url) {
				return !url.contains("#");
			}
		});
		crawler.crawl();
		conn = DriverManager.getConnection("jdbc:mysql://localhost:3306", "root", "");
		conn.createStatement().execute("USE test");
		db = new Database("jdbc:mysql://localhost:3306", "root", "", "test");
	}
	
	@After
	public void tearDown() throws SQLException {
		crawler.close();
		conn.createStatement().execute("DROP DATABASE test");
		conn.close();
		db.conn.close();
	}

	// no results
	@Test
	public void testNoResults() throws SQLException {
		Set<URL> result = db.search("kangaroo party in heaven");
		assertEquals(0, result.size());
	}
	
	// just the Solar System page
	@Test
	public void testSolSys() throws SQLException {
		Set<URL> set = db.search("melting points");
		Set<String> strings = new HashSet<String>();
		for (URL u : set) strings.add(u.toString());
		System.out.println(strings);
		assertTrue(strings.contains(SOLSYS_URL));
		assertFalse(strings.contains(VENUS_URL));
		assertFalse(strings.contains(EARTH_URL));
		assertFalse(strings.contains(MARS_URL));
	}
	
	// just the Earth page
	@Test
	public void testEarth() throws SQLException {
		Set<URL> set = db.search("densest");
		Set<String> strings = new HashSet<String>();
		for (URL u : set) strings.add(u.toString());
		System.out.println(strings);
		assertFalse(strings.contains(SOLSYS_URL));
		assertFalse(strings.contains(VENUS_URL));
		assertTrue(strings.contains(EARTH_URL));
		assertFalse(strings.contains(MARS_URL));
	}
	
	// just the Venus page
	@Test
	public void testVenus() throws SQLException {
		Set<URL> set = db.search("brightest natural object in the night sky");
		Set<String> strings = new HashSet<String>();
		for (URL u : set) strings.add(u.toString());
		System.out.println(strings);
		assertFalse(strings.contains(SOLSYS_URL));
		assertTrue(strings.contains(VENUS_URL));
		assertFalse(strings.contains(EARTH_URL));
		assertFalse(strings.contains(MARS_URL));
	}
	
	// just the Mars page
	@Test
	public void testMars() throws SQLException {
		Set<URL> set = db.search("second smallest planet");
		Set<String> strings = new HashSet<String>();
		for (URL u : set) strings.add(u.toString());
		System.out.println(strings);
		assertFalse(strings.contains(SOLSYS_URL));
		assertFalse(strings.contains(VENUS_URL));
		assertFalse(strings.contains(EARTH_URL));
		assertTrue(strings.contains(MARS_URL));
	}
	
	// both the Solar System and Earth page
	@Test
	public void testSolSysAndEarth() throws SQLException {
		Set<URL> set = db.search("Abracadabra");
		Set<String> strings = new HashSet<String>();
		for (URL u : set) strings.add(u.toString());
		System.out.println(strings);
		assertFalse(strings.contains(SOLSYS_URL));
		assertFalse(strings.contains(VENUS_URL));
		assertTrue(strings.contains(EARTH_URL));
		assertTrue(strings.contains(MARS_URL));
	}
	
	// both the Venus and Mars page
	@Test
	public void testVenusAndMars() throws SQLException {
		Set<URL> set = db.search("Febreeze");
		Set<String> strings = new HashSet<String>();
		for (URL u : set) strings.add(u.toString());
		System.out.println(strings);
		assertFalse(strings.contains(SOLSYS_URL));
		assertTrue(strings.contains(VENUS_URL));
		assertFalse(strings.contains(EARTH_URL));
		assertTrue(strings.contains(MARS_URL));
	}
	
	// All of the pages
	@Test
	public void testAllPages() throws SQLException {
		Set<URL> set = db.search("Sun");
		Set<String> strings = new HashSet<String>();
		for (URL u : set) strings.add(u.toString());
		System.out.println(strings);
		assertTrue(strings.contains(SOLSYS_URL));
		assertTrue(strings.contains(VENUS_URL));
		assertTrue(strings.contains(EARTH_URL));
		assertTrue(strings.contains(MARS_URL));
	}

}
