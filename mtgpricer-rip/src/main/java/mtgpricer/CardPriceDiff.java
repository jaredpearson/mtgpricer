package mtgpricer;

import mtgpricer.catalog.Card;

/**
 * Represents a price difference for a card. The span of the values
 * and the reason for the diff are not known. This is usually used for
 * when determining how much a price has changed over a period of time.
 * @author jared.pearson
 */
public class CardPriceDiff {
	private final Card card;
	private final int multiverseId;
	private final Money value;
	
	public CardPriceDiff(Card card, int multiverseId, Money value) {
		this.card = card;
		this.multiverseId = multiverseId;
		this.value = value;
	}
	
	public Card getCard() {
		return card;
	}
	
	public int getMultiverseId() {
		return multiverseId;
	}
	
	public Money getValue() {
		return value;
	}
}