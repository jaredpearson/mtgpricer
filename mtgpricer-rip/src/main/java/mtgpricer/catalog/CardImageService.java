package mtgpricer.catalog;

/**
 * Service for working with {@link CardImage} instances
 * @author jared.pearson
 */
public interface CardImageService {
	/**
	 * Gets the card image for the given card or null if no card image is available.
	 */
	public CardImage getCardImage(Card card);
}
