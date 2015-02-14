package mtgpricer;

/**
 * Provides the current instance of the {@link PriceServiceImpl}.
 * @author jared.pearson
 */
public interface PriceServiceProvider {
	/**
	 * Gets the current price service instance.
	 */
	public PriceService getPriceService();
}