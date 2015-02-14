package mtgpricer;

import java.util.Date;

import mtgpricer.rip.CardPriceInfo;

/**
 * Represents the price information for one individual card 
 * @author jared.pearson
 */
public class CardPrice {
	private final Money price;
	private final Date retrieved;
	
	public CardPrice(Date retrieved, CardPriceInfo cardPriceInfo) {
		this.retrieved = retrieved;
		this.price = new Money(Double.toString(cardPriceInfo.getPrice()));
	}
	
	public CardPrice(Date retrieved, Double price) {
		this.retrieved = retrieved;
		this.price = new Money(Double.toString(price));
	}
	
	public Date getRetrieved() {
		return retrieved;
	}
	
	public Money getPrice() {
		return price;
	}
}