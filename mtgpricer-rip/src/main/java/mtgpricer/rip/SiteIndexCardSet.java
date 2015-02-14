package mtgpricer.rip;

/**
 * Represents a card set represented on a site index. This is a reference/link
 * to the card set, not the actual card set.
 * @author jared.pearson
 */
public class SiteIndexCardSet {
	private final String name;
	private final String rawName;
	private final String url;
	private final String setCode;
	
	public SiteIndexCardSet(String name, String rawName, String url, String setCode) {
		this.name = name;
		this.rawName = rawName;
		this.setCode = setCode;
		this.url = url;
	}
	
	/**
	 * Gets the name of the set or null when the name of the set is not found in the card catalog.
	 * <p>
	 * This name should be able to be translated to the name of the set in the card catalog. See {@link #getRawName()}
	 *  for the name as parsed from the price site.
	 * @see #getRawName()
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Gets the name of the set as parsed from the price site.
	 */
	public String getRawName() {
		return rawName;
	}
	
	/**
	 * Gets the name of the set code or null when the set is not found in the card catalog.
	 */
	public String getSetCode() {
		return setCode;
	}
	
	public String getUrl() {
		return url;
	}

	@Override
	public String toString() {
		return "SiteIndexSet [name=" + name + ", url=" + url + "]";
	}
}