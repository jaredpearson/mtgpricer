package mtgpricer.catalog.search;

import mtgpricer.catalog.Card;

/**
 * A single card result returned by the card search
 * @author jared.pearson
 */
public class CardSearchResult {
	private final Card card;
	
	public CardSearchResult(Card card) {
		assert card != null;
		this.card = card;
	}
	
	/**
	 * Gets the card
	 */
	public Card getCard() {
		return card;
	}
}