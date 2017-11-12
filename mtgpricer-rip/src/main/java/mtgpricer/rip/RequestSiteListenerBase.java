package mtgpricer.rip;

/**
 * Base class that implements all methods of the {@link RequestSiteListener}. All methods provide
 * a no-op and can be safely overridden.
 * @author jared.pearson
 */
public abstract class RequestSiteListenerBase implements RequestSiteListener {
	/**
	 * Provides a no-op; feel free to override.
	 */
	@Override
	public void onProgressUpdate(int progress, int estimatedTotal) {
		// no-op	
	}
}