package mtgpricer.catalog;

/**
 * Exception thrown when loading the catalog cannot be loaded 
 * @author jared.pearson
 */
public class CatalogLoadFailureException extends RuntimeException {
	private static final long serialVersionUID = -2398980939325064044L;

	public CatalogLoadFailureException() {
		super();
	}

	public CatalogLoadFailureException(Throwable cause) {
		super(cause);
	}

	public CatalogLoadFailureException(String message) {
		super(message);
	}
	
	public CatalogLoadFailureException(String message, Throwable cause) {
		super(message, cause);
	}
}