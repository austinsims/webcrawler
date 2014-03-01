import static org.junit.Assert.*;

import org.junit.Test;


public class CrawlerTest {

	@Test
	public void testUrlInDB() {
		fail("Not yet implemented");
	}

	@Test
	public void testInsertURLInDB() {
		fail("Not yet implemented");
	}

	@Test
	public void testMakeAbsoluteURL() {
		String parentURL = "http://www.example.com";
		String relativeURL = "/pictures/index.html";
		assertEquals(
				"http://www.example.com/pictures/index.html", 
				Crawler.makeAbsoluteURL(relativeURL, parentURL)
		);
	}

	@Test
	public void testFetchURL() {
		fail("Not yet implemented");
	}

}
