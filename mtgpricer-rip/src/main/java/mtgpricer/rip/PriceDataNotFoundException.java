package mtgpricer.rip;

/**
 * Exception thrown when the price data cannot be found
 * @author jared.pearson
 */
public class PriceDataNotFoundException extends RuntimeException {
	private static final long serialVersionUID = 9093427181511748676L;

	public PriceDataNotFoundException(String message) {
		super(message);
	}
}