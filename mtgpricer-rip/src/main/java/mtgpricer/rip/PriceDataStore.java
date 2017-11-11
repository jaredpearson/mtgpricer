package mtgpricer.rip;

import java.io.IOException;

/**
 * Controls how price data is stored. This class is associated to a {@link PriceDataLoader} that loads
 * the price information.
 * 
 * @author jared.pearson
 * @see PriceDataLoader
 */
public interface PriceDataStore {
	/**
	 * Persists the price information.
	 */
	long persist(PriceSiteInfo priceSiteInfo) throws IOException;
}