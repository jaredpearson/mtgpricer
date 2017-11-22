package mtgpricer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import mtgpricer.rip.CardPriceInfo;

/**
 * Represents the price information for one individual card 
 * @author jared.pearson
 */
public class CardPrice {
	private final Date retrieved;
	private final List<CardPriceVariant> variants;
	private final CardPriceVariant maxPriceVariant;
	
	public CardPrice(Date retrieved, List<CardPriceInfo> cardPriceInfos) {
		this.retrieved = retrieved;
		
		CardPriceVariant maxPriceVariant = null;
		final List<CardPriceVariant> variants = new ArrayList<>(cardPriceInfos.size());
		for (final CardPriceInfo cardPriceInfo : cardPriceInfos) {
			final Money price = cardPriceInfo.getPrice() == null ? null : new Money(Double.toString(cardPriceInfo.getPrice()));
			final CardPriceVariant variant = new CardPriceVariant(price, cardPriceInfo.getConditionRaw());
			variants.add(variant);
			
			if (price != null && (maxPriceVariant == null || price.doubleValue() > maxPriceVariant.getPrice().doubleValue())) {
				maxPriceVariant = variant;
			}
		}
		
		this.variants = variants;
		this.maxPriceVariant = maxPriceVariant;
	}
	
	public Date getRetrieved() {
		return this.retrieved;
	}
	
	public List<CardPriceVariant> getVariants() {
		return this.variants;
	}
	
	public CardPriceVariant getMaxPriceVariant() {
		return maxPriceVariant;
	}

	/**
	 * Given a list of card prices, this method will select the first price from among the variants associated to the card prices.
	 */
	public static Money selectFirstPrice(final List<CardPrice> priceHistory) {
		if (priceHistory == null || priceHistory.isEmpty()) {
			return null;
		}
		for (final CardPrice cardPrice : priceHistory) {
			if (cardPrice != null && cardPrice.getVariants() != null) {
				CardPriceVariant variant = CardPriceVariant.selectFirstWithPrice(cardPrice.getVariants());
				if (variant != null) {
					return variant.getPrice();
				}
			}
		}
		return null;
	}
}