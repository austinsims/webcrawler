package edu.purdue.austinsims;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class CrawlerTest {
	Crawler crawler;
	Connection conn;

	@Before
	public void setUp() throws Exception {
		crawler = new Crawler("jdbc:mysql://localhost:3306", "test", "root", "", 100, "", "http://localhost");
		conn = DriverManager.getConnection("jdbc:mysql://localhost:3306", "root", "");
		conn.createStatement().execute("USE test");
	}

	@After
	public void tearDown() throws Exception {
		crawler.close();
		conn.createStatement().execute("DROP DATABASE test");
		conn.close();
	}

	@Test
	public void testIsURLAbsolute() {
		assertTrue(Crawler.isURLAbsolute("http://example.com"));
        assertTrue(Crawler.isURLAbsolute("HTTP://EXAMPLE.COM"));
        assertTrue(Crawler.isURLAbsolute("https://www.exmaple.com"));
        assertTrue(Crawler.isURLAbsolute("ftp://example.com/file.txt"));
        assertFalse(Crawler.isURLAbsolute("/myfolder/test.txt"));
        assertFalse(Crawler.isURLAbsolute("test"));
	}
	
	@Test
	public void testNoMailto() {
		try {
			crawler = new Crawler("jdbc:mysql://localhost:3306", "test", "root", "", 100, "", "http://localhost");
			crawler.addRule(new Rule() {
				@Override
				public boolean check(String url) {	
					//System.out.println("Checking " + url);
					return !url.contains("mailto");
				}
			});
			crawler.crawl();
			ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM url WHERE url LIKE '%mailto%';");
			int c = 0;
			while (rs.next()) c++;
			assertEquals(0,c);
		} catch (MalformedURLException e) {
			fail("Got exception: " + e);
			e.printStackTrace();
		} catch (SQLException e) {
			fail("Got exception: " + e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testNoAnchors() {
		try {
			crawler = new Crawler("jdbc:mysql://localhost:3306", "test", "root", "", 100, "", "http://localhost");
			crawler.addRule(new Rule() {
				@Override
				public boolean check(String url) {
					return !url.contains("#");
				}
			});
			crawler.crawl();
			ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM url WHERE url LIKE '%#%';");
			int c = 0;
			while (rs.next()) c++;
			assertEquals(0,c);
		} catch (MalformedURLException e) {
			fail("Got exception: " + e);
			e.printStackTrace();
		} catch (SQLException e) {
			fail("Got exception: " + e);
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testMaxURLs() {
		try {
			int maxURLs = 2;
			crawler = new Crawler("jdbc:mysql://localhost:3306", "test", "root", "", maxURLs, "", "http://localhost");
			crawler.crawl();
			ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM url;");
			rs.first();
			assertEquals(maxURLs, rs.getInt(1));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail(e.getMessage());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
}
