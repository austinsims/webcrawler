import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class CrawlerTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
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

}
