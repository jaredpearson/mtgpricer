package mtgpricer.catalog.search;


public interface CardSearchService {
	
	public CardSearchResults search(String query);
	public CardSearchResults search(String query, int start);
	
}
