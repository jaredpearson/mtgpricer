package mtgpricer.rip.http;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

/**
 * Requests pages from the site using HTTP.
 * @author jared.pearson
 */
public class HttpPageRequester implements PageRequester {
	private final CloseableHttpClient client = HttpClients.createDefault();
	
	public String getHtml(String url) throws IOException {
		final HttpGet get = new HttpGet(url);
		final CloseableHttpResponse response = client.execute(get);
		try {
			HttpEntity entity = response.getEntity();
			return EntityUtils.toString(entity);
		} finally {
			response.close();
		}
	}
	
	public void close() throws IOException {
		client.close();
	}
}