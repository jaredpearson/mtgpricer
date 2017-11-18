package mtgpricer.rip.cardkingdom;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import mtgpricer.rip.CardPriceInfo;

/**
 * Represents a page of cards from Card Kingdom
 * @author jared.pearson
 */
class CardKingdomCardSetPage {
	private final String url;
	private final Set<String> referencedSetPageUrls;
	private final List<CardPriceInfo> cards;
	
	public CardKingdomCardSetPage(
			final String url,
			final Set<String> referencedSetPageUrls,
			final List<CardPriceInfo> cards) {
		this.url = url;
		this.referencedSetPageUrls = Collections.unmodifiableSet(referencedSetPageUrls);
		this.cards = Collections.unmodifiableList(cards);
	}
	
	/**
	 * Gets the URL that was used to create this page.
	 */
	public String getUrl() {
		return url;
	}
	
	/**
	 * Gets the list of cards found on the page.
	 */
	public List<CardPriceInfo> getCards() {
		return cards;
	}
	
	/**
	 * Gets the set pages that are referenced by this page from the pagination on the page.
	 * @return the URLs referenced in this page's pagination or empty set if no pages are referenced.
	 */
	public Set<String> getReferencedSetPageUrls() {
		return referencedSetPageUrls;
	}
}