package mtgpricer.bridge;

/**
 * Factory for creating keys used in the Redis instance.
 * @author jared.pearson
 */
public class RedisKeyFactory {
	private static final String PRICER_PREFIX = "prcr";
	private static final String CARDS = PRICER_PREFIX + ":cards";
	
	private RedisKeyFactory() {
	}
	
	/**
	 * Sorted set that contains all of the times that the CardKingdom card price information was retrieved.
	 * @param multiverseId the multiverse ID of the card
	 */
	public static String cardKingdomCardPrice(int multiverseId) {
		return CARDS + ":" + multiverseId + ":prices:ck";
	}
	
	/**
	 * Simple value for storing the price of a single card at CardKingom that was retrieved at a specific time.
	 * @param multiverseId the multiverse ID of the card
	 * @param time the time in which the price was retrieved
	 */
	public static String cardKingdomCardPrice(int multiverseId, long time) {
		return CARDS + ":" + multiverseId + ":prices:ck:" + time;
	}
	
	/**
	 * Simple value for storing the 7 day price difference of a single card at CardKingdom.
	 * @param multiverseId the multiverse ID of the card
	 */
	public static String cardKingdomCardPriceDiff7(int multiverseId) {
		return CARDS + ":" + multiverseId + ":prices:ck:diff7";
	}
	
	/**
	 * Sorted set that contains a rank for the 7 day price difference of cards at CardKingdom.
	 */
	public static String cardKingdomCardPriceDiff7Rank() {
		return CARDS + ":ck:diff7Rank";
	}
	
	/**
	 * Sorted set that contains a rank for the 7 day price difference of cards at CardKingdom. This is a
	 * temporary key that only exists for the duration of the rank building algorithm. This variable is
	 * renamed to {@link #cardKingdomCardPriceDiff7Rank()} when the rank is completed.
	 */
	public static String cardKingdomCardPriceDiff7RankTemporary() {
		return CARDS + ":ck:diff7Rank.new";
	}
	
	/**
	 * Sorted set that contains a rank for the 7 day price difference of cards valid in the Standard Tournament format
	 * at CardKingdom.
	 */
	public static String cardKingdomCardPriceDiff7RankStandard() {
		return CARDS + ":ck:diff7Rank:standard";
	}

	/**
	 * Sorted set that contains a rank for the 7 day price difference of cards valid in the Standard Tournament format
	 * at CardKingdom.This is a temporary key that only exists for the duration of the rank building algorithm. This 
	 * variable is renamed to {@link #cardKingdomCardPriceDiff7RankStandard()} when the rank is completed.
	 */
	public static String cardKingdomCardPriceDiff7RankStandardTemporary() {
		return CARDS + ":ck:diff7Rank:standard.new";
	}
	
	/**
	 * Set that contains all of the multiverse IDs for cards
	 */
	public static String cardList() {
		return CARDS;
	}
}