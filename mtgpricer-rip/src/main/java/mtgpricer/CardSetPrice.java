package mtgpricer;

import java.util.HashMap;
import java.util.Map;

import mtgpricer.rip.CardPriceInfo;
import mtgpricer.rip.CardSetPriceInfo;

/**
 * Price information for a card set
 * @author jared.pearson
 */
class CardSetPrice {
	private final CardSetPriceInfo cardSetPriceInfo;
	private final Map<String, CardPrice> numberToCardPrice;
	private final Map<Integer, CardPrice> multiverseIdToCardPrice;
	
	public CardSetPrice(final CardSetPriceInfo cardSetPriceInfo) {
		assert cardSetPriceInfo != null;
		
		this.cardSetPriceInfo = cardSetPriceInfo;
		this.numberToCardPrice = new HashMap<String, CardPrice>();
		this.multiverseIdToCardPrice = new HashMap<Integer, CardPrice>();
		for (final CardPriceInfo cardPriceInfo : cardSetPriceInfo.getCards()) {
			if (cardPriceInfo == null) {
				throw new IllegalStateException("CardSetPriceInfo for " + cardSetPriceInfo.getRawName() + " contains a null card");
			}
			final CardPrice cardPrice = new CardPrice(cardSetPriceInfo.getRetrieved(), cardPriceInfo);
			
			if (cardPriceInfo.getNumber() != null) {
				numberToCardPrice.put(cardPriceInfo.getNumber(), cardPrice);
			}
			
			if (cardPriceInfo.getMultiverseId() != null) {
				multiverseIdToCardPrice.put(cardPriceInfo.getMultiverseId(), cardPrice);
			}
		}
	}
	
	public String getCode() {
		return this.cardSetPriceInfo.getCode();
	}
	
	public CardPrice getCardPriceByNumber(String number) {
		return this.numberToCardPrice.get(number);
	}

	public CardPrice getCardPriceByMultiverseId(Integer multiverseId) {
		return this.multiverseIdToCardPrice.get(multiverseId);
	}
}