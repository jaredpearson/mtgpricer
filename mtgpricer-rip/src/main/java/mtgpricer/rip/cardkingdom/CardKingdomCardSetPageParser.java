package mtgpricer.rip.cardkingdom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mtgpricer.catalog.Card;
import mtgpricer.catalog.CardSet;
import mtgpricer.rip.CardPriceInfo;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Parser for card set pages from Card Kingdom
 * @author jared.pearson
 */
class CardKingdomCardSetPageParser {
	private static final Logger logger = Logger.getLogger(CardKingdomCardSetPageParser.class.getName());
	
	/**
	 * Parses the page of cards
	 * @param url the URL of the page
	 * @param html the HTML of the page to parse
	 * @param cardSet the card set corresponding to the page. this will be null when no card set is mapped to the page. this usually
	 * occurs when the cards are on the page but not in the catalog yet. 
	 * @param parserRule
	 * @return
	 */
	public CardKindgomCardSetPage parseHtml(
			final String url,
			final String html,
			final CardSet cardSet,
			final CardParserRules parserRule) {
		try {
			final Document doc = Jsoup.parse(html);
			
			final Set<String> unknownCardNames = new HashSet<>(); 
			
			final List<CardPriceInfo> cards = new ArrayList<>();
			final Elements itemElements = doc.select(".productCardWrapper .itemContentWrapper");
			for (final Element itemElement : itemElements) {
				final Element cardNameEl = itemElement.getElementsByClass("productDetailTitle").first();
				final String rawCardName = Strings.nullToEmpty(cardNameEl.text()).trim();
				if (rawCardName.length() == 0) {
					logger.finest("Skipping a card with no name");
					continue;
				}
				if (parserRule.isIgnored(rawCardName)) {
					logger.finer("Skipping \"" + rawCardName + "\" since it's in the skip list.");
					continue;
				}
	
				Card card = null;
				if (cardSet != null) {
					card = attemptToFindCard(cardSet, parserRule, rawCardName);
				}
				if (card == null) {
					unknownCardNames.add(rawCardName);
				}
				
				// for each card, there is a list containing condition and price
				for (final Element conditionEl : itemElement.getElementsByClass("itemAddToCart")) {
					
					final String conditionValue;
					if (conditionEl.classNames().contains("NM")) {
						conditionValue = "NM";
					} else if (conditionEl.classNames().contains("EX")) {
						conditionValue = "EX";
					} else if (conditionEl.classNames().contains("VG")) {
						conditionValue = "VG";
					} else if (conditionEl.classNames().contains("G")) {
						conditionValue = "G";
					} else {
						conditionValue = null;
						logger.warning("Unable to determine condition for " + rawCardName + ": " + conditionEl.className());
					}
					
					final Element priceEl = conditionEl.select(".stylePrice").first();
					final String rawPrice = Strings.nullToEmpty(priceEl.text()).trim();
					final Double price = parsePriceValue(rawPrice);
					if (price == null && rawPrice != null) {
						logger.warning("Unable to parse price for " + rawCardName + ": " + rawPrice);
					}
	
					final String cardNumber = card != null ? card.getNumber() : null;
					final String cardName = card != null ? card.getName() : null;
					final Integer multiverseId = card != null ? card.getMultiverseId() : null;
					
					cards.add(new CardPriceInfo(cardName, rawCardName, cardNumber, multiverseId, url, price, rawPrice, conditionValue));
				}
			}
			
			logUnknownCards(url, cardSet, unknownCardNames);

			final Set<String> referencedSetPageUrls = getSetPageUrls(doc);
			return new CardKindgomCardSetPage(url, referencedSetPageUrls, cards);
		} catch (Throwable t) {
			throw new RuntimeException("Exception thrown while parsing page: " + url, t);
		}
	}

	private void logUnknownCards(final String url, final CardSet cardSet, final Set<String> unknownCardNames) {
		if (unknownCardNames == null || unknownCardNames.isEmpty()) {
			return;
		}
		
		final StringBuilder sb = new StringBuilder();
		sb.append("Unknown card names found on set page: ");
		sb.append(url);
		sb.append("\n");
		if (cardSet != null) {
			sb.append(cardSet.getCode() + " - " + cardSet.getName());
			sb.append("\n");
		}
		for (String cardName : unknownCardNames) {
			sb.append("\t");
			sb.append(cardName);
			sb.append("\n");
		}

		logger.warning(sb.toString());
	}

	private Card attemptToFindCard(final CardSet cardSet, final CardParserRules parserRule, final String rawCardName) {
		Card card = null;
		
		// if there is a card number override for the card, lookup the card from catalog using it instead
		if (card == null) {
			final String cardNumberOverride = parserRule.getCardNumberOverrideForName(rawCardName);
			if (cardNumberOverride != null) {
				card = cardSet.getCardWithNumber(cardNumberOverride);
				if (card == null) {
					throw new IllegalStateException("Card number in card override is not found within set: " + rawCardName + " = " + cardNumberOverride);
				}
			}
		}
		
		// attempt to retrieve the card with the overridden name
		if (card == null) {
			final String cardNameOverride = parserRule.getCardNameOverrideForName(rawCardName);
			if (cardNameOverride != null) {
				card = cardSet.getCardWithName(cardNameOverride);
			}
		}
		
		// attempt to retrieve the card with the overridden multiverse ID
		if (card == null) {
			final Integer multiverseId = parserRule.getMultiverseIdForName(rawCardName);
			if (multiverseId != null) {
				card = cardSet.getCardWithMultiverseId(multiverseId);
			}
		}

		// attempt to retrieve the card using the raw name
		if (card == null) {
			card = cardSet.getCardWithName(rawCardName);
		}
		
		return card;
	}
	
	private Set<String> getSetPageUrls(final Document doc) {
		assert doc != null;

		final Elements nextPageElements = doc.select(".pagination li");
		if (nextPageElements.isEmpty()) {
			logger.warning("Page does not contain pagination: " + doc.location());
			return Collections.emptySet();
		}

		final Set<String> urls = new HashSet<>(nextPageElements.size());
		for (Element nextPageElement : nextPageElements) {
			
			// ignore "previous page", "next page" and "..." pagination elements
			final String text = nextPageElement.select("a").text().trim();
			if (!Pattern.matches("^[0-9]+$", text)) {
				continue;
			}
			
			urls.add(nextPageElement.select("a").attr("href"));
		}
		return urls;
	}
	
	private static Double parsePriceValue(String rawValue) {
		if (rawValue == null) {
			return null;
		}
		
		if (rawValue.startsWith("$")) {
			rawValue = rawValue.substring(1);
		}
		
		try {
			double valueAsDouble = Double.parseDouble(rawValue);
			return valueAsDouble;
		} catch(NumberFormatException exc) {
			// silently skip
		}

		// check for the sale pattern
		final Pattern salePattern = Pattern.compile("Sale $+([0-9,]*(\\.[0-9]{2})?)");
		Matcher m = salePattern.matcher(rawValue);
		if (m.find()) {
			try {
				return Double.parseDouble(m.group(1).replace(",", ""));
			} catch(NumberFormatException exc) {
				// silently skip
			}
		}
		return null;
	}
}