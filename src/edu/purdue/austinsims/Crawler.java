package edu.purdue.austinsims;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownServiceException;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;

public class Crawler {

	Database db;
	Properties props;
	List<URL> urls;
	URL root;
	int max;
	private boolean restrict;
	private String domain;
	List<Rule> rules;

	/**
	 * Create a new crawler from a properties file
	 * 
	 * @param propFile
	 *            must contain jdbc.url, jdbc.db, jdbc.username, jdbc.password,
	 *            crawler.maxurls, crawler.domain and crawler.root
	 * @throws IOException
	 * @throws SQLException
	 * @throws MalformedURLException
	 */
	public Crawler(String propFile) throws IOException, SQLException, MalformedURLException {
		props = new Properties();
		FileInputStream in = new FileInputStream("database.properties");
		props.load(in);
		in.close();
		init();
	}

	/**
	 * Create a new crawler with manually set parameters
	 * 
	 * @param server
	 *            hostname or IP of MySQL server
	 * @param dbName
	 *            name of MySQL database to use
	 * @param user
	 *            MySQL username
	 * @param pass
	 *            MySQL password
	 * @param maxURLs
	 *            max number of pages to index for search
	 * @param domain
	 *            optional domain name (or path, like catb.org/jargon) to
	 *            restrict crawl to
	 * @param crawlRoot
	 *            first URL to visit
	 * @throws SQLException
	 * @throws IOException
	 */
	public Crawler(String server, String dbName, String user, String pass, int maxURLs, String domain, String crawlRoot)
			throws IOException, SQLException {
		props = new Properties();
		props.setProperty("jdbc.url", server);
		props.setProperty("jdbc.db", dbName);
		props.setProperty("jdbc.username", user);
		props.setProperty("jdbc.password", pass);
		props.setProperty("crawler.maxurls", String.format("%d", maxURLs));
		props.setProperty("crawler.domain", domain);
		props.setProperty("crawler.root", crawlRoot);
		init();
	}

	public void init() throws IOException, SQLException {
		db = new Database(props);
		urls = new LinkedList<URL>();
		root = new URL(props.getProperty("crawler.root"));
		urls.add(root);
		max = Integer.valueOf(props.getProperty("crawler.maxurls"));
		domain = props.getProperty("crawler.domain");
		rules = new LinkedList<Rule>();
	}

	public void close() throws SQLException {
		db.conn.close();
	}

	public static boolean isURLAbsolute(String url) { 
		return url.matches("^[a-zA-Z]+://.*");
	}

	/**
	 * Make the crawler stay on the domain specified in database.properties.
	 * 
	 * @param b
	 */
	public void restrictToDomain(boolean b) {
		restrict = b;
	}

	public void addRule(Rule r) {
		rules.add(r);
	}

	public void fetch(URL url) {
		try {
			// Get the page contents.
			InputStreamReader in = null;
			try {
				in = new InputStreamReader(url.openStream());
				URLConnection conn = url.openConnection();
				conn.connect();
				String contentType = conn.getContentType();
				if (!contentType.contains("text/html"))
					return;
			} catch (UnknownServiceException e) {
				System.out.println(url + " not found.");
				return;
			} catch (FileNotFoundException e) {
				System.out.println(url + " not found.");
				return;
			} catch (MalformedURLException e) {
				System.out.println(url + " is malformed.");
				return;
			} catch (IOException e) {
				System.out.println(url + " not crawled:" + e.getMessage());
			}
			StringBuilder page = new StringBuilder();
			int c;
			while ((c = in.read()) != -1) {
				page.append((char) c);
			}

			// Insert the URL into the database, along with its description
			String text = Jsoup.parse(page.toString()).text();
			String description = firstNCharRoundToWord(text.toString(), 100);
			int urlID = db.insertURL(url.toString(), description);
			System.out.printf("%d\t%s\n", urlID, url.toString());

			// Find URLs.
			Pattern pattern = Pattern
					.compile("<a\\s+href\\s*=\\s*(\"[^\"]*\"|[^\\s>]*)\\s*>", Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(page);
			while (matcher.find()) {
				String match = page.substring(matcher.start(), matcher.end());
				String foundAddress = matcher.group(1).replaceAll("\"", "");

				// If URL is not absolute, convert it
				if (!isURLAbsolute(foundAddress))
					foundAddress = (new URL(url, foundAddress)).toString();
				
				URL newURL = new URL(foundAddress);
				// Check against rules and insert if pass
				if (!urls.contains(newURL) && pass(foundAddress)) {
					urls.add(newURL);
				}

			}

			// Link words in this page to its url.
			Pattern p = Pattern.compile("\\b\\w+\\b");
			Matcher m = p.matcher(text);
			while (m.find()) {
				String word = text.substring(m.start(), m.end());
				db.insertWord(word, urlID);
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private String firstNCharRoundToWord(String string, int n) {
		StringBuilder sb = new StringBuilder();
		Scanner scan = new Scanner(string);
		int charCount = 0;
		while (scan.hasNext() && charCount <= n) {
			String next = scan.next();
			sb.append(next);
			sb.append(" ");
			charCount += next.length();
		}
		return sb.toString();
	}

	/**
	 * Determine whether this URL passes the rules of the crawler.
	 */
	private boolean pass(String url) {
		// TODO Auto-generated method stub
		if (rules == null)
			return true;
		boolean pass = true;
		for (Rule r : rules) {
			pass = pass && r.check(url);
		}
		return pass;
	}

	public void crawl() throws MalformedURLException {
		int urlsCrawled = 0;
		
		while (urlsCrawled < max && urls.size() > 0 && urlsCrawled < urls.size()) {
			//fetch(urls.remove(0));
			fetch(urls.get(urlsCrawled));
			urlsCrawled++;
		}
	}

	public static void main(String[] args) {
		Crawler crawler;
		try {
			crawler = new Crawler("database.properties");

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

			// Don't crawl javascript:... links
			crawler.addRule(new Rule() {
				@Override
				public boolean check(String url) {
					return !url.contains("javascript:");
				}
			});
			
			// Only crawl cs.purdue.edu links
			/*
			crawler.addRule(new Rule() {
				@Override
				public boolean check(String url) {
					return url.contains("cs.purdue.edu");
				}
			});
			*/
			
			crawler.addRule(new Rule() {
				@Override
				public boolean check(String url) {
					return !(
							url.endsWith(".jpg")||
							url.endsWith(".pdf")||
							url.endsWith(".gif")||
							url.endsWith(".png")||
							url.endsWith(".zip")
							);
				}
			});

			crawler.restrictToDomain(true);
			crawler.crawl();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
