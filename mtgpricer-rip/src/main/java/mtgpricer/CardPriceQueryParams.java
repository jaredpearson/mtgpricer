package mtgpricer;

/**
 * Parameters for when querying for {@link CardPrice} instances
 * @author jared.pearson
 */
public class CardPriceQueryParams {
	private int limit = 10;
	private OrderDirection orderDirection = OrderDirection.DESC;
	private CardPriceOrder order = CardPriceOrder.RETRIEVED;
	
	public int getLimit() {
		return limit;
	}
	
	public CardPriceQueryParams limit(int value) {
		this.limit = value;
		return this;
	}
	
	public CardPriceOrder getOrder() {
		return order;
	}
	
	public CardPriceQueryParams order(CardPriceOrder order) {
		assert order != null;
		this.order = order;
		return this;
	}
	
	public OrderDirection getOrderDirection() {
		return orderDirection;
	}
	
	public CardPriceQueryParams orderDirection(OrderDirection orderDirection) {
		assert orderDirection != null;
		this.orderDirection = orderDirection;
		return this;
	}
}
