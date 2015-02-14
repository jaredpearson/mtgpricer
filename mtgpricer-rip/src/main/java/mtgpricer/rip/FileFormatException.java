package mtgpricer.rip;

/**
 * Exception thrown when the format of the price data file is not valid.
 * @author jared.pearson
 */
public class FileFormatException extends RuntimeException {
	private static final long serialVersionUID = 4458894490132520785L;

	public FileFormatException(String message) {
		super(message);
	}

	public FileFormatException(Throwable cause) {
		super(cause);
	}
	
	public FileFormatException(String message, Throwable cause) {
		super(message, cause);
	}
}