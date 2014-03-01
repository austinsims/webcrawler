import java.io.*;
import java.net.*;
import java.util.regex.*;
import java.sql.*;
import java.util.*;

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

	public boolean urlInDB(String urlFound) throws SQLException, IOException {
         	Statement stat = connection.createStatement();
		ResultSet result = stat.executeQuery( "SELECT * FROM urls WHERE url LIKE '"+urlFound+"'");

		if (result.next()) {
	        	System.out.println("URL "+urlFound+" already in DB");
			return true;
		}
	       // System.out.println("URL "+urlFound+" not yet in DB");
		return false;
	}

	public void insertURLInDB( String url) throws SQLException, IOException {
         	Statement stat = connection.createStatement();
		String query = "INSERT INTO urls VALUES ('"+urlID+"','"+url+"','')";
		//System.out.println("Executing "+query);
		stat.executeUpdate( query );
		urlID++;
	}

/*
	public String makeAbsoluteURL(String url, String parentURL) {
		if (url.indexOf(":")<0) {
			// the protocol part is already there.
			return url;
		}

		if (url.length > 0 && url.charAt(0) == '/') {
			// It starts with '/'. Add only host part.
			int posHost = url.indexOf("://");
			if (posHost <0) {
				return url;
			}
			int posAfterHist = url.indexOf("/", posHost+3);
			if (posAfterHist < 0) {
				posAfterHist = url.Length();
			}
			String hostPart = url.substring(0, posAfterHost);
			return hostPart + "/" + url;
		} 

		// URL start with a char different than "/"
		int pos = parentURL.lastIndexOf("/");
		int posHost = parentURL.indexOf("://");
		if (posHost <0) {
			return url;
		}
		
		
		

	}
*/

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
				if (!urlInDB(urlFound)) {
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

