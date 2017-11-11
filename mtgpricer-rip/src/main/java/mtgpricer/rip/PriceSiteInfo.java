package mtgpricer.rip;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Gets the information from a site
 * @author jared.pearson
 */
public class PriceSiteInfo {
	private final Long id;
	private final Date retrieved;
	private final String url;
	private final List<CardSetPriceInfo> cardSets;
	
	public PriceSiteInfo(
			Long id,
			String url,
			Date retrieved,
			List<CardSetPriceInfo> cardSets) {
		this.id = id;
		this.url = url;
		this.retrieved = new Date(retrieved.getTime());
		this.cardSets = Collections.unmodifiableList(cardSets);
	}

	/**
	 * Gets the ID of this price site info instance.
	 * @return the ID or null if no persisted.
	 */
	public Long getId() {
		return this.id;
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