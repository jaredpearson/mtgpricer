package mtgpricer.rip;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import mtgpricer.rip.Deserialization.DeserializationType;

/**
 * Represents the information about set of cards.
 * <p>
 * Be aware that this instance is serialized to and read from a JSON file so all
 * properties will need to conform to that.
 * @author jared.pearson
 */
public class FileCardSetPriceInfo implements CardSetPriceInfo {
	private final String name;
	private final String rawName;
	private final String code;
	private final String url;
	private final Date retrieved;

	/**
	 * When saving the card set price information to disk, the cards are included but
	 * the cards property is optionally loaded on deserialization.
	 */
	@Deserialization(only=DeserializationType.CARDS)
	private final List<CardPriceInfo> cards;
	
	public FileCardSetPriceInfo(
			final String name,
			final String rawName,
			final String code,
			final String url,
			final Date retrieved,
			final List<? extends CardPriceInfo> cards) {
		assert cards != null;
		this.name = name;
		this.rawName = rawName;
		this.code = code;
		this.url = url;
		this.retrieved = new Date(retrieved.getTime());
		this.cards = Collections.unmodifiableList(cards);
	}

	@Override
	public String getCode() {
		return this.code;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getRawName() {
		return rawName;
	}

	@Override
	public String getUrl() {
		return url;
	}

	@Override
	public Date getRetrieved() {
		return retrieved;
	}

	public List<CardPriceInfo> getCards() {
		return cards;
	}
}