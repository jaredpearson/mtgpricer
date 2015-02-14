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
	private final Date retrieved;
	private final Map<String, CardSetPrice> codeToCardSet;
	
	public PriceSite(final PriceSiteInfo priceSiteInfo) {
		this.retrieved = priceSiteInfo.getRetrieved();
		this.codeToCardSet = new HashMap<String, CardSetPrice>();
		for (final CardSetPriceInfo cardSetPriceInfo : priceSiteInfo.getCardSets()) {
			final CardSetPrice cardSetPrice = new CardSetPrice(cardSetPriceInfo);
			if (cardSetPrice.getCode() != null) {
				this.codeToCardSet.put(cardSetPrice.getCode(), cardSetPrice);
			}
		}
	}
	
	/**
	 * Gets the card set price instance for the set with the given set code. If the 
	 * set code is unknown, then a null reference is returned.
	 */
	public CardSetPrice getCardSetPriceByCode(String code) {
		return this.codeToCardSet.get(code);
	}
	
	/**
	 * Gets the price of a card with the given multiverse ID. If the card price is not
	 * found for the multiverse ID, then a null reference is returned.
	 */
	public CardPrice getCardPriceByMultiverseId(int multiverseId) {
		for (CardSetPrice cardSetPrice : codeToCardSet.values()) {
			final CardPrice cardPrice = cardSetPrice.getCardPriceByMultiverseId(multiverseId);
			if (cardPrice != null) {
				return cardPrice;
			}
		}
		return null;
	}

	/**
	 * Gets the date this site information was retrieved.
	 */
	public Date getRetrieved() {
		return retrieved;
	}
}