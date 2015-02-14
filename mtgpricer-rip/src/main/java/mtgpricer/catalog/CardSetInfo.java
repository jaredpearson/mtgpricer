package mtgpricer.catalog;

import java.util.List;

/**
 * Represents the card set data that can be serialized/deserialized
 * @author jared.pearson
 */
class CardSetInfo implements Comparable<CardSetInfo> {
	private final String code;
	private final String name;
	private final List<CardInfo> cards;
	
	public CardSetInfo(String code, String name, List<CardInfo> cards) {
		this.code = code;
		this.name = name;
		this.cards = cards;
	}
	
	public String getCode() {
		return code;
	}
	
	public String getName() {
		return name;
	}
	
	public List<CardInfo> getCards() {
		return cards;
	}
	
	public int compareTo(CardSetInfo o) {
		if (o == null) {
			return -1;
		}
		
		return this.name.compareTo(o.name);
	}
}