package mtgpricer.rip.http;

import java.io.Closeable;
import java.io.IOException;

/**
 * Implementing classes are in charge of getting the HTML text content from 
 * a URL.
 * @author jared.pearson
 */
public interface PageRequester extends Closeable {
	/**
	 * Gets the HTML text content from the specified URL.
	 * @param url the URL to be requested
	 * @return The content of the requested page.
	 */
	public String getHtml(String url) throws IOException;
}