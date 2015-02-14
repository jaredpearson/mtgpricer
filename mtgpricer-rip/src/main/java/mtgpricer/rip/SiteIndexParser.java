package mtgpricer.rip;


/**
 * Parser for a site index
 * @author jared.pearson
 */
public interface SiteIndexParser {
	/**
	 * Parses the HTML page for the SiteIndex.
	 * @param html the HTML retrieved from the URL
	 * @return the site index parsed from the HTML
	 */
	public SiteIndex parseHtml(String html);
}