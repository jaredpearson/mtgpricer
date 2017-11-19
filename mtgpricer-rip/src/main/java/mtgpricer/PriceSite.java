package mtgpricer;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import mtgpricer.rip.CardSetPriceInfo;
import mtgpricer.rip.PriceSiteInfo;

/**
 * Price information for a site
 * @author jared.pearson
 */
class PriceSite {
	private final Long id;
	private final Date retrieved;
	private final Map<String, CardSetPriceInfo> codeToCardSet;
	
	public PriceSite(final PriceSiteInfo priceSiteInfo) {
		this.id = priceSiteInfo.getId();
		this.retrieved = priceSiteInfo.getRetrieved();
		this.codeToCardSet = new HashMap<String, CardSetPriceInfo>();
		for (final CardSetPriceInfo cardSetPriceInfo : priceSiteInfo.getCardSets()) {
			if (cardSetPriceInfo.getCode() != null) {
				this.codeToCardSet.put(cardSetPriceInfo.getCode(), cardSetPriceInfo);
			}
		}
	}
	
	/**
	 * Gets the ID of the price site.
	 */
	public Long getId() {
		return id;
	}
	
	/**
	 * Gets the card set price instance for the set with the given set code. If the 
	 * set code is unknown, then a null reference is returned.
	 */
	public CardSetPriceInfo getCardSetPriceByCode(String code) {
		return this.codeToCardSet.get(code);
	}

	/**
	 * Gets the date this site information was retrieved.
	 */
	public Date getRetrieved() {
		return retrieved;
	}
}