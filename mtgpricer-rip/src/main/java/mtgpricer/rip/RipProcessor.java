package mtgpricer.rip;

import java.io.IOException;

import mtgpricer.Display;
import mtgpricer.catalog.CardCatalog;
import mtgpricer.catalog.CardCatalogService;
import mtgpricer.rip.cardkingdom.CardKingdomSite;
import mtgpricer.rip.cardkingdom.CardKingdomSiteFactory;
import mtgpricer.rip.http.PageRequester;
import mtgpricer.rip.http.PageRequesterFactory;

/**
 * Processor for ripping price information from sites
 * @author jared.pearson
 */
public class RipProcessor {
	private final PageRequesterFactory pageRequesterFactory;
	private final CardCatalogService cardCatalogService;
	private final CardKingdomSiteFactory cardKingdomSiteFactory;
	private final PriceDataStore priceDataStore;
	
	public RipProcessor(PageRequesterFactory pageRequesterFactory,
			CardCatalogService cardCatalogService,
			CardKingdomSiteFactory cardKingdomSiteFactory,
			PriceDataStore priceDataStore) {
		assert pageRequesterFactory != null;
		assert cardCatalogService != null;
		assert cardKingdomSiteFactory != null;
		assert priceDataStore != null;
		
		this.pageRequesterFactory = pageRequesterFactory;
		this.cardCatalogService = cardCatalogService;
		this.cardKingdomSiteFactory = cardKingdomSiteFactory;
		this.priceDataStore = priceDataStore;
	}

	/**
	 * Rips the site information to the standard price data store.
	 */
	public void rip(Display display) throws IOException {
		final CardCatalog cardCatalog = cardCatalogService.loadCardCatalog();
		final PageRequester pageRequester = pageRequesterFactory.create();
		try {
			final CardKingdomSite site = cardKingdomSiteFactory.createSite(cardCatalog, pageRequester);
			assert site != null;
			
			final PriceSiteInfo siteInfo = site.requestSiteInfo();
			assert siteInfo != null;
			
			priceDataStore.persist(siteInfo);
		} finally {
			pageRequester.close();
		}
	}
}