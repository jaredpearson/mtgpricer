package mtgpricer.rip;

import mtgpricer.CommandLineTool;
import mtgpricer.CommandLineTools;
import mtgpricer.Display;
import mtgpricer.Displays;
import mtgpricer.rip.http.PageRequester;
import mtgpricer.rip.http.PageRequesterFactory;

/**
 * Command line tool for getting the latest price information
 * @author jared.pearson
 */
public class Main implements CommandLineTool {
	private final RipProcessor ripProcessor;
	private final PageRequesterFactory pageRequesterFactory;
	
	public Main(RipProcessor ripProcessor, PageRequesterFactory pageRequesterFactory) {
		assert ripProcessor != null;
		assert pageRequesterFactory != null;
		this.ripProcessor = ripProcessor;
		this.pageRequesterFactory = pageRequesterFactory;
	}
	
	public void run(String[] args) throws Exception {
		boolean useCache = false;
		for (String arg : args) {
			if ("--cache".equals(arg)) {
				useCache = true;
			}
		}
		
		final Display display = Displays.createForPrintStream(System.out);
		try (final PageRequester pageRequester = createPageRequester(useCache)) {
			ripProcessor.rip(display, pageRequester, new RequestSiteListenerBase() {});
		}
	}
	
	public static void main(String[] args) throws Exception {
		CommandLineTools.run(Main.class, args);
	}
	
	private PageRequester createPageRequester(boolean useCache) {
		if (useCache) {
			return this.pageRequesterFactory.createCachedPageRequester();
		} else {
			return this.pageRequesterFactory.create();
		}
	}
}
