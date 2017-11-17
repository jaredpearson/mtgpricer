package mtgpricer.rip.cardkingdom;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import mtgpricer.catalog.Card;
import mtgpricer.catalog.CardCatalog;
import mtgpricer.catalog.CardSet;
import mtgpricer.rip.CardSetPriceInfo;
import mtgpricer.rip.PriceSiteInfo;
import mtgpricer.rip.http.PageRequester;

/**
 * Tests for {@link CardKingdomSite}
 * @author jared.pearson
 */
@RunWith(MockitoJUnitRunner.class)
public class CardKingdomSiteTest {
	private static final String SITE_INDEX_URL = "https://www.cardkingdom.com/catalog/magic_the_gathering/by_az";
	private static final String SITE_INDEX_HTML = createSiteIndexHtml();
	
	@Mock CardCatalog cardCatalog;
	@Mock PageRequester pageRequester;
	@Mock SiteParserRules siteParserRules;
	@Mock SiteParserRulesFactory siteParserRulesFactory;
	
	@Before
	public void setup() throws Exception {
		when(siteParserRules.getParserRuleForCardSetCode(anyString())).thenReturn(CardParserRules.createEmpty());
		when(siteParserRulesFactory.loadSiteParserRules()).thenReturn(siteParserRules);
		
		when(pageRequester.getHtml(SITE_INDEX_URL)).thenReturn(SITE_INDEX_HTML);
		when(pageRequester.getHtml("/set1")).thenReturn(createCardSet1Page1Html());
		when(pageRequester.getHtml("/set1?page=1")).thenReturn(createCardSet1Page1Html());
		when(pageRequester.getHtml("/set1?page=2")).thenReturn(createCardSet1Page2Html());
		when(pageRequester.getHtml("/set2")).thenReturn(createCardSet2Page1Html());
		when(pageRequester.getHtml("/set2?page=1")).thenReturn(createCardSet2Page1Html());
		when(pageRequester.getHtml("/set2?page=2")).thenReturn(createCardSet2Page2Html());
		when(pageRequester.getHtml("/set2?page=3")).thenReturn(createCardSet2Page3Html());

		final List<Card> set1Cards = Arrays.asList(
				createCard("Set 1 Card 1", "set1"),
				createCard("Set 1 Card 2", "set1"),
				createCard("Set 1 Card 3", "set1"),
				createCard("Set 1 Card 4", "set1"));
		final CardSet cardSet1 = createCardSet("Set 1", "set1", set1Cards);

		final List<Card> set2Cards = Arrays.asList(
				createCard("Set 2 Card 1", "set2"),
				createCard("Set 2 Card 2", "set2"),
				createCard("Set 2 Card 3", "set2"),
				createCard("Set 2 Card 4", "set2"),
				createCard("Set 2 Card 5", "set2"));
		final CardSet cardSet2 = createCardSet("Set 2", "set2", set2Cards);
		
		final List<CardSet> cardSets = Arrays.asList(cardSet1, cardSet2);
		for (CardSet cardSet : cardSets) {
			when(cardCatalog.containsCardSetWithName(cardSet.getName())).thenReturn(true);
			when(cardCatalog.getCardSetByName(cardSet.getName())).thenReturn(cardSet);
			when(cardCatalog.getCardSetByCode(cardSet.getCode())).thenReturn(cardSet);
		}
	}

	/**
	 * Verifies that requesting site info will work in the happy path
	 */
	@Test
	public void testRequestSiteInfo() throws Exception {
		final CardKingdomSite site = new CardKingdomSite(cardCatalog, pageRequester, siteParserRulesFactory);
		final PriceSiteInfo priceSiteInfo = site.requestSiteInfo();
		assertNotNull("Expected requestSiteInfo to return a value (not null)", priceSiteInfo);
		assertNull("PriceSiteInfo has not been saved yet so ID should be null", priceSiteInfo.getId());
		assertNotNull("Expected retrieved to return a value", priceSiteInfo.getRetrieved());
		assertNotNull("Expected cardSets to return a value", priceSiteInfo.getCardSets());
		assertEquals("Unexpected number of card sets returned", 2, priceSiteInfo.getCardSets().size());
		
		Map<String, CardSetPriceInfo> codeToCardSetPriceInfos = new HashMap<>();
		for (CardSetPriceInfo cardSetPriceInfo : priceSiteInfo.getCardSets()) {
			if (codeToCardSetPriceInfos.containsKey(cardSetPriceInfo.getCode())) {
				fail("Duplicate CardSetPriceInfo instances returned: " + cardSetPriceInfo.getCode());
			}
			codeToCardSetPriceInfos.put(cardSetPriceInfo.getCode(), cardSetPriceInfo);
		}
		
		CardSetPriceInfo cardSetPriceInfo1 = codeToCardSetPriceInfos.get("set1");
		assertNotNull("Expected set1 to be returned", cardSetPriceInfo1);
		assertEquals("Unexpected name returned by rawName", "Set 1", cardSetPriceInfo1.getRawName());
		assertEquals("Unexpected number of cards within set", 4, cardSetPriceInfo1.getCards().size());

		CardSetPriceInfo cardSetPriceInfo2 = codeToCardSetPriceInfos.get("set2");
		assertNotNull("Expected set2 to be returned", cardSetPriceInfo2);
		assertEquals("Unexpected name returned by rawName", "Set 2", cardSetPriceInfo2.getRawName());
		assertEquals("Unexpected number of cards within set", 5, cardSetPriceInfo2.getCards().size());
	}
	
	private static String createSiteIndexHtml() {
		StringBuilder sb = new StringBuilder();
		sb.append("<html><body><div class=\"anchorList\"><table>");
		sb.append("<td><a href=\"/set1\">Set 1</a></td>");
		sb.append("<td><a href=\"/set2\">Set 2</a></td>");
		sb.append("</table></div></body></html>");
		return sb.toString();
	}
	
	private static String createCardSet1Page1Html() {
		return createCardSetPageHtml(new CardSetPageBuilder()
				.cardEntries(
						createCardEntryBuilder("Set 1 Card 1", "NM", "$1.01"),
						createCardEntryBuilder("Set 1 Card 2", "NM", "$1.02"))
				.pagination(new CardSetPagePaginationBuilder()
						.pageUrlFormat("/set1?page=%d")
						.numberOfPages(2)));
	}
	
	private static String createCardSet1Page2Html() {
		return createCardSetPageHtml(new CardSetPageBuilder()
				.cardEntries(
						createCardEntryBuilder("Set 1 Card 3", "NM", "$1.03"),
						createCardEntryBuilder("Set 1 Card 4", "NM", "$1.04"))
				.pagination(new CardSetPagePaginationBuilder()
						.pageUrlFormat("/set1?page=%d")
						.numberOfPages(2)));
	}
	
	private static String createCardSet2Page1Html() {
		return createCardSetPageHtml(new CardSetPageBuilder()
				.cardEntries(
						createCardEntryBuilder("Set 2 Card 1", "NM", "$2.01"),
						createCardEntryBuilder("Set 2 Card 2", "NM", "$2.02"))
				.pagination(new CardSetPagePaginationBuilder()
						.pageUrlFormat("/set2?page=%d")
						.numberOfPages(2)));
	}
	
	private static String createCardSet2Page2Html() {
		return createCardSetPageHtml(new CardSetPageBuilder()
				.cardEntries(
						createCardEntryBuilder("Set 2 Card 3", "NM", "$2.03"),
						createCardEntryBuilder("Set 2 Card 4", "NM", "$2.04"))
				.pagination(new CardSetPagePaginationBuilder()
						.pageUrlFormat("/set2?page=%d")
						.numberOfPages(3)));
	}
	
	private static String createCardSet2Page3Html() {
		return createCardSetPageHtml(new CardSetPageBuilder()
				.cardEntries(
						createCardEntryBuilder("Set 2 Card 5", "NM", "$2.05"))
				.pagination(new CardSetPagePaginationBuilder()
						.pageUrlFormat("/set2?page=%d")
						.numberOfPages(3)));
	}

	/**
	 * Creates the HTML for a card set page
	 */
	private static String createCardSetPageHtml(CardSetPageBuilder cardPage) {
		StringBuilder sb = new StringBuilder();
		sb.append("<html><body>");
		sb.append("<div class=\"productCardWrapper\">");
		for (CardEntryBuilder cardEntry : cardPage.cardEntries) {
			sb.append("<div class=\"itemContentWrapper\">");
			sb.append("<div class=\"productDetailTitle\">" + cardEntry.name + "</div>");
			for (CardVariantEntryBuilder cardVariant : cardEntry.variants) {
				sb.append("<div class=\"itemAddToCart " + cardVariant.condition + "\">");
				sb.append("<div class=\"stylePrice\">" + cardVariant.price + "</div>");
				sb.append("</div>");
			}
			sb.append("</div>");
		}
		sb.append("</table></div>");
		if (cardPage.pagination != null) {
			CardSetPagePaginationBuilder pagination = cardPage.pagination;
			sb.append("<ul class=\"pagination\">");
			for (int page = 1; page <= pagination.numberOfPages; page++) {
				String pageUrl = String.format(pagination.pageUrlFormat, page);
				sb.append("<li><a href=\"" + pageUrl + "\">" + page + "</a></li>");
			}
			sb.append("</ul>");
		}
		sb.append("</body></html>");
		return sb.toString();
	}
	
	/**
	 * Represents a page that contains cards
	 * @author jared.pearson
	 */
	private static class CardSetPageBuilder {
		public List<CardEntryBuilder> cardEntries = new ArrayList<>();
		public CardSetPagePaginationBuilder pagination;
		
		public CardSetPageBuilder pagination(CardSetPagePaginationBuilder pagination) {
			this.pagination = pagination;
			return this;
		}
		
		public CardSetPageBuilder cardEntries(CardEntryBuilder... cardEntries) {
			this.cardEntries = Arrays.asList(cardEntries);
			return this;
		}
	}
	
	/**
	 * Represents the pagination on a card set page
	 * @author jared.pearson
	 */
	private static class CardSetPagePaginationBuilder {
		public String pageUrlFormat;
		public int numberOfPages;
		
		public CardSetPagePaginationBuilder pageUrlFormat(String pageUrlFormat) {
			this.pageUrlFormat = pageUrlFormat;
			return this;
		}
		
		public CardSetPagePaginationBuilder numberOfPages(int numberOfPages) {
			this.numberOfPages = numberOfPages;
			return this;
		}
	}
	
	/**
	 * Represents an entry for a card on the page 
	 * @author jared.pearson
	 */
	private static class CardEntryBuilder {
		public String name;
		public List<CardVariantEntryBuilder> variants = new ArrayList<>();
		
		public CardEntryBuilder name(String name) {
			this.name = name;
			return this;
		}
		
		public CardEntryBuilder variant(CardVariantEntryBuilder variant) {
			this.variants.add(variant);
			return this;
		}
	}
	
	/**
	 * Represents a price variant within a entry for a card
	 * @author jared.pearson
	 */
	private static class CardVariantEntryBuilder {
		public String condition;
		public String price;
		
		public CardVariantEntryBuilder condition(String condition) {
			this.condition = condition;
			return this;
		}
		
		public CardVariantEntryBuilder price(String price) {
			this.price = price;
			return this;
		}
		
	}

	private static CardEntryBuilder createCardEntryBuilder(String name, String condition, String price) {
		return cardEntryBuilder().name(name)
			.variant(cardVariantEntryBuilder().condition(condition).price(price));
	}

	private static CardEntryBuilder cardEntryBuilder() {
		return new CardEntryBuilder();
	}
	
	private static CardVariantEntryBuilder cardVariantEntryBuilder() {
		return new CardVariantEntryBuilder();
	}
	
	private static Card createCard(String cardName, String setCode) {
		return new Card(cardName, setCode, null, null, null, null, false);
	}

	private static CardSet createCardSet(String name, String setCode, List<Card> cards) {
		final CardSet cardSet = mock(CardSet.class);
		when(cardSet.getName()).thenReturn(name);
		when(cardSet.getCode()).thenReturn(setCode);
		for (Card card : cards) {
			when(cardSet.getCardWithName(card.getName())).thenReturn(card);
		}
		return cardSet;
	}
}
