package mtgpricer.rip.cardkingdom;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import mtgpricer.catalog.CardCatalog;
import mtgpricer.catalog.CardSet;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Site index parser for Card Kingdom
 * @author jared.pearson
 */
class CardKingdomSiteIndexParser {
	private static final Logger logger = Logger.getLogger(CardKingdomSiteIndexParser.class.getName());
	private final CardCatalog cardCatalog;
	private final SiteParserRules parserRules;
	
	public CardKingdomSiteIndexParser(
			final CardCatalog cardCatalog, 
			final SiteParserRules siteParserRules) {
		assert cardCatalog != null;
		assert siteParserRules != null;
		this.cardCatalog = cardCatalog;
		this.parserRules = siteParserRules;
	}

	public SiteIndex parseHtml(final String html) {
		final Document doc = Jsoup.parse(html);
		final String selector = ".anchorList td a";
		final Elements headerAndLinkElements = doc.select(selector);
		if (headerAndLinkElements.isEmpty()) {
			throw new IllegalStateException(String.format("Expected \"%s\" to be found", selector));
		}
		
		final List<SiteIndexCardSet> sets = new ArrayList<>();
		for (final Element el : headerAndLinkElements) {
			final String realName = Strings.nullToEmpty(el.text()).trim();
			if (realName.length() == 0) {
				continue;
			}
			
			// skip any name in the ignore list
			if (parserRules.isCardSetNameIgnored(realName)) {
				logger.finer("Skipping \"" + realName + "\" since it's in the skip list.");
				continue;
			}
			
			// check the name translation map
			final String overrideName = parserRules.getCardSetNameOverride(realName);
			final String name;
			if (overrideName != null) {
				name = overrideName;
				logger.finer("Changing name from \"" + realName + "\" to \"" + name + "\" since it's in the translation.");
			} else {
				name = realName;
			}
			
			// attempt to map the card set from the catalog
			final CardSet cardSet;
			if (!cardCatalog.containsCardSetWithName(name)) {
				logger.warning(String.format("Unknown set with name \"%s\". To fix, either add it to the card catalog or add it to the ignore list.", name));
				cardSet = null;
			} else {
				cardSet = cardCatalog.getCardSetByName(name);
			}
			
			final String cardSetName = (cardSet != null) ? cardSet.getName() : null;
			final String cardSetCode = (cardSet != null) ? cardSet.getCode() : null;
			
			// Card Kingdom links to the set's about page from the index
			final String setAboutUrl = el.attr("href");
			
			sets.add(new SiteIndexCardSet(cardSetName, realName, setAboutUrl, cardSetCode));
		}
		return new SiteIndex(sets);
	}
}