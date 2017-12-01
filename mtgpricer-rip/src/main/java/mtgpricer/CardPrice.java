package mtgpricer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import mtgpricer.rip.CardPriceInfo;
import mtgpricer.rip.CardPriceVariantInfo;

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
			if (cardPriceInfo.getVariants() == null) {
				throw new IllegalStateException(cardPriceInfo.getRawName());
			}
			for (CardPriceVariantInfo cardPriceVariantInfo : cardPriceInfo.getVariants()) {
				final Money price = cardPriceVariantInfo.getPrice() == null ? null : new Money(Double.toString(cardPriceVariantInfo.getPrice()));
				final CardPriceVariant variant = new CardPriceVariant(price, cardPriceVariantInfo.getConditionRaw());
				variants.add(variant);
				
				if (price != null && (maxPriceVariant == null || price.doubleValue() > maxPriceVariant.getPrice().doubleValue())) {
					maxPriceVariant = variant;
				}
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