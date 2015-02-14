package mtgpricer.rip.cardkingdom;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import mtgpricer.catalog.CardCatalog;
import mtgpricer.catalog.CardSet;
import mtgpricer.rip.CardPriceInfo;
import mtgpricer.rip.CardSetPriceInfo;
import mtgpricer.rip.SiteIndex;
import mtgpricer.rip.SiteIndexCardSet;
import mtgpricer.rip.PriceSiteInfo;
import mtgpricer.rip.http.PageRequester;

/**
 * Represents the CardKingdom site.
 * @author jared.pearson
 */
public class CardKingdomSite {
	private static final Logger logger = Logger.getLogger(CardKingdomSite.class.getName());
	private static final String cardKingdomUrl = "http://www.cardkingdom.com/";
	private final CardCatalog cardCatalog;
	private final PageRequester pageRequester;
	private final CardKingdomSiteIndexParser cardSetIndexParser;
	private final CardKingdomCardSetPageParser cardSetParser;
	private final SiteParserRules siteParserRules;
	
	public CardKingdomSite(CardCatalog cardCatalog, PageRequester pageRequester, SiteParserRulesFactory siteParserRulesFactory) {
		assert cardCatalog != null;
		assert pageRequester != null;
		assert siteParserRulesFactory != null;
		
		this.cardCatalog = cardCatalog;
		this.pageRequester = pageRequester;
		this.siteParserRules = siteParserRulesFactory.loadSiteParserRules();
		
		// create the parsers
		this.cardSetIndexParser = new CardKingdomSiteIndexParser(cardCatalog, siteParserRules);
		this.cardSetParser = new CardKingdomCardSetPageParser();
	}
	
	/**
	 * Requests the site info for all card sets.
	 */
	public PriceSiteInfo requestSiteInfo() throws IOException {
		final SiteIndex siteIndex = requestSiteIndex();
		logger.info("Site index retrieved. Getting set information for " + siteIndex.getCardSets().size() + " sets.");

		final List<CardSetPriceInfo> cardSets = new ArrayList<CardSetPriceInfo>();
		for (SiteIndexCardSet cardSetIndex : siteIndex.getCardSets()) {
			final CardSetPriceInfo cardSet = requestCardSetInfo(cardSetIndex);
			cardSets.add(cardSet);
		}
		
		return new PriceSiteInfo(cardKingdomUrl, new Date(), cardSets);
	}
	
	/**
	 * Request the site index from Card Kingdom.
	 */
	private SiteIndex requestSiteIndex() throws IOException {
		final String html = pageRequester.getHtml(cardKingdomUrl);
		final SiteIndex siteIndex = cardSetIndexParser.parseHtml(html);
		return siteIndex;
	}
	
	/**
	 * Request a Card Set from the card set reference.
	 */
	private CardSetPriceInfo requestCardSetInfo(SiteIndexCardSet cardSetIndex) throws IOException {
		assert cardSetIndex != null;

		logger.info("Requesting information for set " + cardSetIndex.getName());
		final List<CardKindgomCardSetPage> pages = requestPages(cardSetIndex);
		
		final List<CardPriceInfo> allCards = new ArrayList<CardPriceInfo>();
		for (CardKindgomCardSetPage page : pages) {
			allCards.addAll(page.getCards());
		}
		
		return new CardSetPriceInfo(cardSetIndex, new Date(), allCards);
	}

	/**
	 * Gets all of the pages of cards for the given set.
	 */
	private List<CardKindgomCardSetPage> requestPages(SiteIndexCardSet cardSet) throws IOException {
		assert cardSet != null;
		
		boolean visitedListPage = false;
		final List<CardKindgomCardSetPage> pages = new ArrayList<CardKindgomCardSetPage>();
		String url = cardSet.getUrl();
		while(url != null) {
			
			// CardKindom uses one URL for the "About" card set page and the list of cards within the set. The "About" page
			// has a link to the list that must be clicked after the loading the "About" page. This only has to be clicked
			// once per visit.
			if (!visitedListPage) {
				final String aboutTabUrl = cardSet.getUrl();
				final String setCategoryId = aboutTabUrl.substring(aboutTabUrl.lastIndexOf("/") + 1);
				final String cardTabUrl = "http://www.cardkingdom.com/catalog/set_model/mtg_card?category_id=" + setCategoryId;
				pageRequester.getHtml(cardTabUrl);
				visitedListPage = true;
			}
			
			// attempt to find the card set from the catalog
			final CardSet cardSetInfo;
			if (cardSet != null) { 
				cardSetInfo = this.cardCatalog.getCardSetByCode(cardSet.getSetCode());
			} else {
				cardSetInfo = null;
			}
			
			// attempt to find the parser rules corresponding to the card set
			final CardParserRules cardSetParserRule;
			if (cardSetInfo != null) { 
				cardSetParserRule = siteParserRules.getParserRuleForCardSetCode(cardSetInfo.getCode());
			} else {
				cardSetParserRule = CardParserRules.createEmpty();
			}
			
			// request the real page that we want
			final String cardSetHtml = pageRequester.getHtml(url);
			final CardKindgomCardSetPage page = cardSetParser.parseHtml(url, cardSetHtml, cardSetInfo, cardSetParserRule);
			pages.add(page);
			url = page.getNextPageUrl();
		}
		
		return pages;
	}
}