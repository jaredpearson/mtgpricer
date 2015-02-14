package mtgpricer.catalog;

/**
 * Exception thrown when attempting to get an image when the image is not available.
 * @author jared.pearson
 */
public class ImageNotFoundException extends RuntimeException {
	private static final long serialVersionUID = -1135527397986370842L;

	public ImageNotFoundException() {
		super();
	}

	public ImageNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ImageNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public ImageNotFoundException(String message) {
		super(message);
	}

	public ImageNotFoundException(Throwable cause) {
		super(cause);
	}
	
	
}