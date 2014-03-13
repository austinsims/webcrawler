/**
 * Determine whether or not a URL should be included in a crawl. 
 */
public abstract class Rule {
	public abstract boolean check(String url);
}
