package mtgpricer;

import java.util.Comparator;

/**
 * Comparator for the {@link CardPrice} based on the order and order direction.
 * @author jared.pearson
 */
class CardPriceComparator implements Comparator<CardPrice> {
	private final CardPriceOrder order;
	private final OrderDirection orderDirection;
	
	public CardPriceComparator(CardPriceOrder order, OrderDirection orderDirection) {
		this.order = order;
		this.orderDirection = orderDirection;
	}

	public int compare(CardPrice cardPrice1, CardPrice cardPrice2) {
		final int value;
		
		switch (order) {
		case RETRIEVED:
			value = cardPrice1.getRetrieved().compareTo(cardPrice2.getRetrieved());
			break;
		default:
			throw new RuntimeException("Unknown order: " + order);
		}

		if (orderDirection.equals(OrderDirection.DESC)) {
			return -value;
		} else {
			return value;
		}
	}
}