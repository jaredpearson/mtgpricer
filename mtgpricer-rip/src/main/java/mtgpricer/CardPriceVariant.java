package mtgpricer;

import java.util.List;

/**
 * Used to denote a variant in the price. This could be due to the condition of
 * a card (like played, good, near mint), a special version of a card (like foil),
 * etc.
 * @author jared.pearson
 *
 */
public class CardPriceVariant {
	private final Money price;
	private final String variant;
	
	public CardPriceVariant(Money price, String variant) {
		this.price = price;
		this.variant = variant;
	}
	
	public String getVariant() {
		return this.variant;
	}
	
	public Money getPrice() {
		return this.price;
	}
	
	/**
	 * Selects the first variant that has a non-null price property.
	 */
	public static CardPriceVariant selectFirstWithPrice(List<CardPriceVariant> variants) {
		if (variants == null || variants.isEmpty()) {
			return null;
		}
		for (final CardPriceVariant variant : variants) {
			if (variant.getPrice() != null) {
				return variant;
			}
		}
		return null;
	}
}
