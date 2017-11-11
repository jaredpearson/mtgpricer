package mtgpricer.rip;

import java.util.Set;

/**
 * Loads the price information.
 * @author jared.pearson
 */
public interface PriceDataLoader {

	/**
	 * Loads the price data from the data directory for all files
	 */
	Set<? extends PriceSiteInfo> loadPriceData();

	/**
	 * Loads the price data for the given ID. Throws an exception if the data doesn't
	 * exist for the given ID.
	 * @param id the ID of the data to load.
	 * @return the price data corresponding to the given ID.
	 */
	PriceSiteInfo loadPriceDataById(long id);

}