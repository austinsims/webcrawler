import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.UnknownServiceException;
import java.net.URL;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;

public class Crawler {
	
	Database db;
	Properties props;
	List<URL> urls;
	URL root;
	int max;
	
	public Crawler(String propFile) throws IOException, SQLException, MalformedURLException {
		props = new Properties();
		FileInputStream in = new FileInputStream("database.properties");
		props.load(in);
		in.close();
		db = new Database(propFile);
		urls = new LinkedList<URL>();
		root = new URL(props.getProperty("crawler.root"));
		urls.add(root);
		max = Integer.valueOf(props.getProperty("crawler.maxurls"));
	}

	public static boolean isURLAbsolute(String url) {
		return url.matches("^[a-zA-Z]+://.*");
	}
	
	private String makeAbsoluteURL(String relativeURL, String parentURL) {
		// TODO: implement
		return null;
	}
	
	public void fetch(URL url) {
		try {
			// Insert this URL into the database.
			int urlID = db.insertURL(url.toString());
			
			// Get the page contents.
			InputStreamReader in = null;
			try {
				in = new InputStreamReader(url.openStream());
			} catch (UnknownServiceException e) {
				return; // just don't deal with this url.
			}
			StringBuilder page = new StringBuilder();
			int c;
			while ((c = in.read()) != -1) {
				page.append((char)c);
			}
			
			// Link words in this page to its url.
			String text = Jsoup.parse(page.toString()).text();
			Pattern p = Pattern.compile("\\b\\w+\\b");
			Matcher m = p.matcher(text);
			while (m.find()) {
				String word = text.substring(m.start(), m.end());
				db.insertWord(word, urlID);
			}
			
			// Find URLs.
			Pattern pattern = Pattern.compile("<a\\s+href\\s*=\\s*(\"[^\"]*\"|[^\\s>]*)\\s*>", Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(page);
			while (matcher.find()) {
				String match = page.substring(matcher.start(), matcher.end());
				String foundAddress = matcher.group(1).replaceAll("\"", "");
				if (!db.hasURL(foundAddress)) {
					// Convert to absolute URL	
					if (isURLAbsolute(foundAddress)) {
						db.insertURL(foundAddress);
					} else {
						if (foundAddress.contains("#")) continue; // don't bother with anchors in same page
						URL foundURL = new URL(url, foundAddress);
						urls.add(foundURL);
						db.insertURL(foundURL.toString());
					}
					// TODO: Insert URL if it doesn't already exist
				}
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void crawl() throws MalformedURLException {
		int urlsCrawled = 0;
		while (urlsCrawled < max && urls.size() > 0) {
			fetch(urls.remove(0));
			urlsCrawled++;
		}
	}
	
	public static void main(String[] args) {
		Crawler crawler;
		try {
			crawler = new Crawler("database.properties");
			crawler.crawl();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
