package mtgpricer;

/**
 * Parameters for when querying for {@link CardPrice} instances
 * @author jared.pearson
 */
public class CardPriceQueryParams {
	private int limit = 10;
	
	public int getLimit() {
		return limit;
	}
	
	public CardPriceQueryParams limit(int value) {
		this.limit = value;
		return this;
	}
}
