package mtgpricer.rip;

import java.io.File;
import java.util.Set;

/**
 * Loads the price information.
 * @author jared.pearson
 */
public interface PriceDataLoader {

	/**
	 * Loads the price data from the data directory for all files
	 */
	public Set<? extends PriceSiteInfo> loadPriceData();

	/**
	 * Loads the price data from the single file
	 */
	public PriceSiteInfo loadPriceDataForSingleFile(File file);

}