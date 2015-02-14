package mtgpricer.rip;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Gets the information from a site
 * @author jared.pearson
 */
public class PriceSiteInfo {
	private final Date retrieved;
	private final String url;
	private final List<CardSetPriceInfo> cardSets;
	
	public PriceSiteInfo(String url, Date retrieved, List<CardSetPriceInfo> cardSets) {
		this.url = url;
		this.retrieved = new Date(retrieved.getTime());
		this.cardSets = Collections.unmodifiableList(cardSets);
	}
	
	/**
	 * Gets the card sets on the site.
	 */
	public List<CardSetPriceInfo> getCardSets() {
		return cardSets;
	}
	
	/**
	 * Gets the date to which the site information was retrieved
	 */
	public Date getRetrieved() {
		return new Date(retrieved.getTime());
	}
	
	/**
	 * Gets the URL of the site
	 */
	public String getUrl() {
		return url;
	}
}