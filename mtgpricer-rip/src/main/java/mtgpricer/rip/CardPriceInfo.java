package mtgpricer.rip;

/**
 * Represents the information about the card
 * <p>
 * Be aware that this instance is serialized to and read from a JSON file so all
 * properties will need to conform to that.
 * @author jared.pearson
 */
public class CardPriceInfo {
	private final String name;
	private final String rawName;
	private final String number;
	private final Integer multiverseId;
	private final String url;
	private final Double price;
	private final String priceRaw;
	private final String conditionRaw;
	
	public CardPriceInfo(String name, String rawName, String number, Integer multiverseId, String url, Double price, String priceRaw, String conditionRaw) {
		this.name = name;
		this.rawName = rawName;
		this.number = number;
		this.multiverseId = multiverseId;
		this.url = url;
		this.price = price;
		this.priceRaw = priceRaw;
		this.conditionRaw = conditionRaw;
	}
	
	/**
	 * Gets the name of the card. 
	 * <p>
	 * This will be null if the card information could not be found in the card
	 * catalog to the corresponding record on the price website.
	 * @see #getRawName()
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * Gets the name of the card as on the price website.
	 */
	public String getRawName() {
		return rawName;
	}
	
	/**
	 * Gets the number of the card within the card set.
	 * <p>
	 * This will be null when the set doesn't have numbers associated to the cards.
	 */
	public String getNumber() {
		return number;
	}
	
	/**
	 * Gets the ID of the card within the Multiverse. 
	 * <p>
	 * This will return null if the card is not found in the multiverse.
	 */
	public Integer getMultiverseId() {
		return this.multiverseId;
	}
	
	/**
	 * Gets the URL to which this card info was found
	 */
	public String getUrl() {
		return url;
	}
	
	/**
	 * Gets the price value as a double. This will return null if the price provided was null or the price could not be converted
	 * to a double.
	 */
	public Double getPrice() {
		return price;
	}
	
	/**
	 * Gets the price value as it was provided.
	 */
	public String getPriceRaw() {
		return priceRaw;
	}
	
	/**
	 * Gets the condition as it was provided.
	 */
	public String getConditionRaw() {
		return conditionRaw;
	}
}