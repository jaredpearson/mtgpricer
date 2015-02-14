package mtgpricer.rip.cardkingdom;

import java.util.Collections;
import java.util.List;

import mtgpricer.rip.CardPriceInfo;

/**
 * Represents a page of cards from Card Kingdom
 * @author jared.pearson
 */
class CardKindgomCardSetPage {
	private final String nextPageUrl;
	private final List<CardPriceInfo> cards;
	
	public CardKindgomCardSetPage(String nextPageUrl, List<CardPriceInfo> cards) {
		this.nextPageUrl = nextPageUrl;
		this.cards = Collections.unmodifiableList(cards);
	}
	
	/**
	 * Gets the list of cards found on the page.
	 */
	public List<CardPriceInfo> getCards() {
		return cards;
	}
	
	/**
	 * Gets the URL of the next page. If this is the last page, then a null
	 * reference is returned.
	 * @return the URL of the next page or null if on the last page.
	 */
	public String getNextPageUrl() {
		return nextPageUrl;
	}
}