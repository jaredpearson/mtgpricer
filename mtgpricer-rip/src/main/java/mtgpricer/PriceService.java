package mtgpricer;

import java.util.List;
import java.util.Map;
import mtgpricer.catalog.Card;
import mtgpricer.catalog.CardSet;

/**
 * Service for retrieving price information.
 * @author jared.pearson
 */
public interface PriceService {

	/**
	 * Gets the latest price information for the given card within the given card set.
	 * @returns the latest price for the card or null if one is not found
	 */
	CardPrice getCurrentPriceForCard(Card card);

	/**
	 * Gets the price history for specified card within the card set
	 * @param card the card to which the card belongs
	 * @return the list of price history for the specified card.
	 */
	List<CardPrice> getPriceHistoryForCard(Card card);

	/**
	 * Gets the price history for specified card within the card set
	 * @param card the card to which the card belongs
	 * @param params the parameters used to get the price history
	 * @return the list of price history for the specified card.
	 */
	List<CardPrice> getPriceHistoryForCard(Card card, CardPriceQueryParams params);
	
	/**
	 * Gets the price information for the card within the given card set.
	 * @param cardSet the card set to use to pull card price information
	 * @return the map of card to card price history. this method should never return null.
	 */
	Map<Card, List<CardPrice>> getPriceHistoryForCards(CardSet cardSet);
	
	/**
	 * Gets the top ten cards with the largest positive seven day diff.
	 */
	List<CardPriceDiff> getTopPositiveCardPriceDiffSevenDays();
	
	/**
	 * Gets the top ten cards with the largest negative seven day diff.
	 */
	List<CardPriceDiff> getTopNegativeCardPriceDiffSevenDays();

	/**
	 * Gets the top ten cards with the largest positive seven day diff for cards that are valid in the 
	 * Standard Tournament Format.
	 */
	List<CardPriceDiff> getTopPositiveCardPriceDiffSevenDaysStandard();
}