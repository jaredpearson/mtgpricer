package mtgpricer.catalog.search;

import java.util.Collections;
import java.util.List;

/**
 * Results returned from the {@link CardSearchService}
 * @author jared.pearson
 */
public class CardSearchResults {
	private final long count;
	private final List<CardSearchResult> results;
	
	public CardSearchResults(long count, List<CardSearchResult> results) {
		this.count = count;
		this.results = (results == null) ? Collections.<CardSearchResult>emptyList() : Collections.unmodifiableList(results);
	}
	
	/**
	 * Gets the number of cards that match the query
	 */
	public long getCount() {
		return count;
	}
	
	/**
	 * Gets the cards that were found while searching.
	 */
	public List<? extends CardSearchResult> getCards() {
		return results;
	}
}