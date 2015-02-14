package mtgpricer.rip.cardkingdom;

import mtgpricer.catalog.CardCatalog;
import mtgpricer.rip.http.PageRequester;

/**
 * Factory for creating {@link CardKingdomSite} instances
 * @author jared.pearson
 */
public class CardKingdomSiteFactory {
	private final CardKingdomSiteParserRulesFactory siteParserRulesFactory;
	
	public CardKingdomSiteFactory(CardKingdomSiteParserRulesFactory siteParserRulesFactory) {
		assert siteParserRulesFactory != null;
		this.siteParserRulesFactory = siteParserRulesFactory;
	}
	
	public CardKingdomSite createSite(CardCatalog cardCatalog, PageRequester pageRequester) {
		return new CardKingdomSite(cardCatalog, pageRequester, siteParserRulesFactory);
	}
}