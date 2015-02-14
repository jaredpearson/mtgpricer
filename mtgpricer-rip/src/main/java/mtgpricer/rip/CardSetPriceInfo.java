package mtgpricer.rip;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Represents the information about set of cards.
 * <p>
 * Be aware that this instance is serialized to and read from a JSON file so all
 * properties will need to conform to that.
 * @author jared.pearson
 */
public class CardSetPriceInfo {
	private final String name;
	private final String rawName;
	private final String code;
	private final String url;
	private final Date retrieved;
	private final List<CardPriceInfo> cards;
	
	public CardSetPriceInfo(SiteIndexCardSet cardSetIndex, Date retrieved, List<? extends CardPriceInfo> cards) {
		assert cardSetIndex != null;
		assert cards != null;
		this.name = cardSetIndex.getName();
		this.rawName = cardSetIndex.getRawName();
		this.code = cardSetIndex.getSetCode();
		this.url = cardSetIndex.getUrl();
		this.cards = Collections.unmodifiableList(cards);
		this.retrieved = new Date(retrieved.getTime());
	}

	/* (non-Javadoc)
	 * @see mtgpricer.rip.CardSetPriceInfo#getCode()
	 */
	public String getCode() {
		return this.code;
	}
	
	/* (non-Javadoc)
	 * @see mtgpricer.rip.CardSetPriceInfo#getName()
	 */
	public String getName() {
		return name;
	}
	
	/* (non-Javadoc)
	 * @see mtgpricer.rip.CardSetPriceInfo#getRawName()
	 */
	public String getRawName() {
		return rawName;
	}
	
	/* (non-Javadoc)
	 * @see mtgpricer.rip.CardSetPriceInfo#getUrl()
	 */
	public String getUrl() {
		return url;
	}
	
	/* (non-Javadoc)
	 * @see mtgpricer.rip.CardSetPriceInfo#getRetrieved()
	 */
	public Date getRetrieved() {
		return retrieved;
	}

	/* (non-Javadoc)
	 * @see mtgpricer.rip.CardSetPriceInfo#getCards()
	 */
	public List<CardPriceInfo> getCards() {
		return cards;
	}
	
}