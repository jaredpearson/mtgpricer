package mtgpricer.catalog.search;

/**
 * Exception that occurred during a search
 * @author jared.pearson
 */
public class SearchFailedException extends RuntimeException {
	private static final long serialVersionUID = 833856877207614204L;

	public SearchFailedException() {
		super();
	}

	public SearchFailedException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public SearchFailedException(String message, Throwable cause) {
		super(message, cause);
	}

	public SearchFailedException(String message) {
		super(message);
	}

	public SearchFailedException(Throwable cause) {
		super(cause);
	}
	
}
