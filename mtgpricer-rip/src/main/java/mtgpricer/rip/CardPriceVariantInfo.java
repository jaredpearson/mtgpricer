package mtgpricer.rip;

/**
 * Represents the information about a price variant on the card. A variant can be 
 * anything that may be different about the card, like condition (played, good, near mint)
 * or a limited printing (like foil).
 * <p>
 * Be aware that this instance is serialized to and read from a JSON file so all
 * properties will need to conform to that.
 * @author jared.pearson
 */
public class CardPriceVariantInfo {
	private final Double price;
	private final String priceRaw;
	private final String conditionRaw;
	
	public CardPriceVariantInfo(
			final Double price,
			final String priceRaw,
			final String conditionRaw) {
		this.price = price;
		this.priceRaw = priceRaw;
		this.conditionRaw = conditionRaw;
	}

	/**
	 * Gets the price value as a double. This will return null if the price provided was null or the price could not be converted
	 * to a double.
	 */
	public Double getPrice() {
		return this.price;
	}
	
	/**
	 * Gets the price value as it was provided.
	 */
	public String getPriceRaw() {
		return this.priceRaw;
	}
	
	/**
	 * Gets the condition as it was provided.
	 */
	public String getConditionRaw() {
		return this.conditionRaw;
	}
}
