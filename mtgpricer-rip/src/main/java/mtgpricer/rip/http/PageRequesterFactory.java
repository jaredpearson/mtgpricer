package mtgpricer.rip.http;


/**
 * Factory for creating {@link PageRequester} instances
 * @author jared.pearson
 */
public interface PageRequesterFactory {
	/**
	 * Creates a new page requester instance. Make sure to call the close method to release the resources
	 * held by the instance.
	 * @return a new page requester instance
	 */
	public PageRequester create();
}