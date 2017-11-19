package mtgpricer.rip;

import java.util.Date;
import java.util.List;

/**
 * Gets the information from a site
 * @author jared.pearson
 */
public interface PriceSiteInfo {
	/**
	 * Gets the ID of this price site info instance.
	 * @return the ID or null if no persisted.
	 */
	Long getId();

	/**
	 * Gets the card sets on the site.
	 */
	List<? extends CardSetPriceInfo> getCardSets();

	/**
	 * Gets the date to which the site information was retrieved
	 */
	Date getRetrieved();

	/**
	 * Gets the URL of the site
	 */
	String getUrl();
}