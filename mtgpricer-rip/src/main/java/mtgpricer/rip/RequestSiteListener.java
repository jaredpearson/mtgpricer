package mtgpricer.rip;

/**
 * Listener used when requesting site information
 * @author jared.pearson
 */
public interface RequestSiteListener {
	void onProgressUpdate(int progress, int estimatedTotal);
}