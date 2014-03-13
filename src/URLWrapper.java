import java.net.URL;

/**
 * Wrap URL class to allow for a description field.
 */
public class URLWrapper {
	private URL url;
	private String description;
	
	public URLWrapper(URL url) {
		this.url = url;
		description = null;
	}
	
	public URLWrapper(URL url, String description) {
		this.url = url;
		this.description = description;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public URL getURL() {
		return url;
	}
	
}
