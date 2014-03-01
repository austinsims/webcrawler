import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Crawler
{
	Connection connection;
	int urlID;
	public Properties props;

	Crawler() {
		urlID = 0;
	}

	public void readProperties() throws IOException {
      		props = new Properties();
      		FileInputStream in = new FileInputStream("database.properties");
      		props.load(in);
      		in.close();
	}

	public void openConnection() throws SQLException, IOException
	{
		String drivers = props.getProperty("jdbc.drivers");
      		if (drivers != null) System.setProperty("jdbc.drivers", drivers);

      		String url = props.getProperty("jdbc.url");
      		String username = props.getProperty("jdbc.username");
      		String password = props.getProperty("jdbc.password");

		connection = DriverManager.getConnection( url, username, password);
   	}

	public void createDB() throws SQLException, IOException {
		openConnection();

         	Statement stat = connection.createStatement();
		
		// Delete the table first if any
		try {
			stat.executeUpdate("DROP TABLE URLS");
		}
		catch (Exception e) {
		}
			
		// Create the table
        	stat.executeUpdate("CREATE TABLE URLS (urlid INT, url VARCHAR(512), description VARCHAR(200))");
	}

	public boolean isURLInDB(String url) throws SQLException, IOException {
         	Statement stat = connection.createStatement();
		//ResultSet result = stat.executeQuery( "SELECT * FROM url WHERE url LIKE '"+urlFound+"'");
		ResultSet result = stat.executeQuery(String.format("SELECT COUNT(*) FROM url WHERE url LIKE '%s';", url));

		if (result.next()) {
	        	System.out.println("URL "+url+" already in DB");
			return true;
		}
	       // System.out.println("URL "+urlFound+" not yet in DB");
		return false;
	}

	public void insertURLInDB( String url) throws SQLException, IOException {
		Statement stat = connection.createStatement();
		String query = String.format("INSERT INTO url VALUES (NULL, '%s', NULL);", url);
		stat.executeUpdate( query );
	}

	/** Broken */
	public static String makeAbsoluteURL(String relativeURL, String parentURL) {
		StringBuilder absoluteURL = new StringBuilder();
		
		if (relativeURL.contains(":")) {
			// the protocol part is already there.
			return relativeURL;
		}

		if (relativeURL.length() > 0 && relativeURL.startsWith("/") ) {
			// It starts with '/'. Add only host part.
			int posHost = parentURL.indexOf("://");
			if (posHost <0) {
				return relativeURL;
			}
			int posAfterHost = relativeURL.indexOf("/", posHost+3);
			if (posAfterHost < 0) {
				posAfterHost = relativeURL.length();
			}
			String hostPart = relativeURL.substring(0, posAfterHost);
			absoluteURL.append(hostPart + "/" + relativeURL);
		} else {  // URL starts with a char other than "/"
			// below code is not complete and doesn't really make sense
			/*
			int pos = parentURL.lastIndexOf("/");
			int posHost = parentURL.indexOf("://");
			if (posHost <0) {
				return relativeURL;
			}
			*/
		}
		return absoluteURL.toString();
	}
	
    void startCrawl() {
        // TODO: Open the database
        
        //for every url in url-list..
        for (int i=0; true; i++){
        	// TODO: Insert into url table
           
        }
    }
    
//    void crawl() {
//    	while (NextURLIDScanned < NextURLID) {
//    		urlIndex = NextURLIDScanned;
//    		// TODO: Fetch the url1 entry in urlIndex
//    		
//    		NextURLIDScanned++;
//    		
//    		// TODO: Get the first 100 characters or less of the document from url1 without tags.
//    		// Add this description to the URL record in the URL table.
//    		
//    		// TODO: For each url2 in the links in the anchor tags of this document...
//    		for (/* anchor_href : tags_in_doc */) {
//    			// TODO: fetch anchor_href
//    			// TODO: If it is not html/text, continue
//    			if (/* not html/text*/ false) continue;
//    			
//    			if (NextURLID < MaxURLs && !urlInDB(anchor_href)) {
//    				// TODO: put (NextURLID, anchor_href) into url table
//    				
//    				NextURLID++;
//    			}
//    		}
//    		
//    		// TODO: Get the document in url1 without tags
//    		// for each different word in the document... 
//    		/* NOTE: remove words between...
//    			<script> ... </script>
//    			<!-- and -->
//    			& and ;
//    		*/
//    		for (/* String word : document.text.split(' ') */) {
//    			// TODO: In Word table, get the (word, URLList) for this word and append urlIndex at the end of URLList
//    			//       or create a new (word, URLList) if the entry does not exist
//    		}
//    	}
//    }

   	public void fetchURL(String urlScanned) {
		try {
			URL url = new URL(urlScanned);
			System.out.println("urlscanned="+urlScanned+" url.path="+url.getPath());
 
    			// open reader for URL
    			InputStreamReader in = 
       				new InputStreamReader(url.openStream());

    			// read contents into string builder
    			StringBuilder input = new StringBuilder();
    			int ch;
			while ((ch = in.read()) != -1) {
         			input.append((char) ch);
			}

     			// search for all occurrences of pattern
    			String patternString =  "<a\\s+href\\s*=\\s*(\"[^\"]*\"|[^\\s>]*)\\s*>";
    			Pattern pattern = 			
	     			Pattern.compile(patternString, 
	     			Pattern.CASE_INSENSITIVE);
    			Matcher matcher = pattern.matcher(input);
		
			while (matcher.find()) {
    				int start = matcher.start();
    				int end = matcher.end();
    				String match = input.substring(start, end);
				String urlFound = matcher.group(1);
				System.out.println(urlFound);

				// Check if it is already in the database
				if (!isURLInDB(urlFound)) {
					insertURLInDB(urlFound);
				}				
	
    				//System.out.println(match);
 			}

		}
      		catch (Exception e)
      		{
       			e.printStackTrace();
      		}
	}

   	public static void main(String[] args)
   	{
		Crawler crawler = new Crawler();

		try {
			crawler.readProperties();
			String root = crawler.props.getProperty("crawler.root");
			crawler.createDB();
			crawler.fetchURL(root);
		}
		catch( Exception e) {
         		e.printStackTrace();
		}
    }
}

