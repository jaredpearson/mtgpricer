package mtgpricer.rip.http;

import java.io.IOException;

/**
 * Requests pages from specified URL using first the cache, then
 * if not found, from the site using HTTP.
 * @author jared.pearson
 */
public class CachedPageRequestor implements PageRequester {
	private final UrlCacheManager cacheManager;
	private final PageRequester innerRequester;
	
	/**
	 * Create a new page requestor that caches the responses from the given page requestor
	 * <p>
	 * On close, the specified page requestor is also closed.
	 */
	public CachedPageRequestor(PageRequester pageRequester) {
		this.cacheManager = new UrlCacheManager();
		this.innerRequester = pageRequester;
	}
	
	public String getHtml(String url) throws IOException {
		final String html;
		if (cacheManager.isCached(url)) {
			html = cacheManager.loadFromCache(url);
		} else {
			// download the HTML
			html = innerRequester.getHtml(url);
			
			// save the HTML to a cache directory
			cacheManager.saveToCache(url, html);
		}
		return html;
	}
	
	/**
	 * Closes the cache requestor and the inner requestor.
	 */
	public void close() throws IOException {
		cacheManager.close();
		innerRequester.close();
	}
}