package mtgpricer.rip;

import java.util.List;
import java.util.Map;
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
	 * @param priceSiteId the ID of the data to load.
	 * @return the price data corresponding to the given ID.
	 */
	PriceSiteInfo loadPriceDataById(long priceSiteId);

	/**
	 * Loads all of the card price info for the given site ID (and groups them by set code)
	 * @return map of set code to card price info
	 */
	Map<String, List<CardPriceInfo>> loadCardPriceInfos(PriceSiteInfo priceSiteInfo);
	
	/**
	 * Load the list of cards associated to the given card set from the given price site info.
	 */
	List<CardPriceInfo> loadCardPriceInfos(long priceSiteId, String setCode);

	/**
	 * Gets the price of a card with the given multiverse ID. If the card price is not
	 * found for the multiverse ID, then a null reference is returned.
	 */
	List<CardPriceInfo> loadCardPriceInfosByMultiverseId(long priceSiteId, int multiverseId);
}