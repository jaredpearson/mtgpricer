package mtgpricer.web.controller;

import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import mtgpricer.catalog.Card;
import mtgpricer.catalog.CardCatalog;
import mtgpricer.catalog.CardCatalogProvider;
import mtgpricer.catalog.CardSet;
import mtgpricer.rip.CardPriceInfo;
import mtgpricer.rip.CardSetPriceInfo;
import mtgpricer.rip.PriceDataLoader;
import mtgpricer.rip.PriceDataNotFoundException;
import mtgpricer.rip.PriceSiteInfo;
import mtgpricer.rip.cardkingdom.CardParserRules;
import mtgpricer.rip.cardkingdom.SiteParserRules;
import mtgpricer.rip.cardkingdom.SiteParserRulesBuilder;
import mtgpricer.rip.cardkingdom.SiteParserRulesFactory;

/**
 * Controller for the PriceDataExplorer.
 * @author jared.pearson
 */
@Controller
public class PriceDataExplorerController {
	private final PriceDataLoader priceDataLoader;
	private final CardCatalogProvider cardCatalogProvider;
	private final SiteParserRulesFactory siteParserRulesFactory;
	private final Gson gson;
	
	@Autowired
	public PriceDataExplorerController(
			final PriceDataLoader priceDataLoader,
			final CardCatalogProvider cardCatalogProvider,
			final SiteParserRulesFactory siteParserRulesFactory) {
		this.priceDataLoader = priceDataLoader;
		this.cardCatalogProvider = cardCatalogProvider;
		this.siteParserRulesFactory = siteParserRulesFactory;
		this.gson = new GsonBuilder()
				.setPrettyPrinting()
				.disableHtmlEscaping()
				.create();
	}

	@RequestMapping("/settings/priceDataExplorer/{priceDataId}")
	public ModelAndView showPriceDataExplorer(
			@PathVariable("priceDataId") final long priceDataId) {
		
		// load the site from the URL
		final PriceSiteInfo priceSiteInfo;
		try {
			priceSiteInfo = priceDataLoader.loadPriceDataById(priceDataId);
		} catch (PriceDataNotFoundException exc) {
			throw new ResourceNotFoundException(exc);
		}
		
		// load the catalog and parser rules
		final CardCatalog catalog = cardCatalogProvider.getCardCatalog();
		final SiteParserRules parserRules = siteParserRulesFactory.loadSiteParserRules();
		
		// find all of the unknown cards within the card sets
		final PriorityQueue<CardSetEntry> cardSets = convertToCardSetEntries(priceSiteInfo, catalog, parserRules);
		
		ModelAndView modelAndView = new ModelAndView("settings/priceDataExplorer");
		modelAndView.addObject("priceSiteInfo", priceSiteInfo);
		modelAndView.addObject("cardSets", cardSets);
		return modelAndView;
	}

	@RequestMapping(value="/settings/priceDataExplorer/{priceDataId}/sets/{cardSetCode}.json", produces="application/json")
	@ResponseBody
	public CardSetEntry getCardsWithinSet(
			@PathVariable("priceDataId") final long priceDataId,
			@PathVariable("cardSetCode") final String cardSetCode) {

		// load the site from the URL
		final PriceSiteInfo priceSiteInfo;
		try {
			priceSiteInfo = priceDataLoader.loadPriceDataById(priceDataId);
		} catch (PriceDataNotFoundException exc) {
			throw new ResourceNotFoundException(exc);
		}
		
		// load the catalog and parser rules
		final CardCatalog catalog = cardCatalogProvider.getCardCatalog();
		final SiteParserRules parserRules = siteParserRulesFactory.loadSiteParserRules();
		
		// find all of the unknown cards within the card sets
		final PriorityQueue<CardSetEntry> cardSets = convertToCardSetEntries(priceSiteInfo, catalog, parserRules);
		return cardSets.stream()
			.filter(cardSetEntry -> cardSetEntry.getCode() != null && cardSetEntry.getCode().equals(cardSetCode))
			.findFirst()
			.orElseThrow(() -> new ResourceNotFoundException());
	}

	@RequestMapping(value="/settings/priceDataExplorer/cardkingdom/parserRules.json")
	public void downloadParserRules(
			final HttpServletResponse servletResponse) throws Exception {
		
		// load the existing parser rules
		final SiteParserRules loadedParserRules = siteParserRulesFactory.loadSiteParserRules();
		
		servletResponse.setContentType("application/json");
		servletResponse.setCharacterEncoding("UTF-8");
		servletResponse.setHeader("content-disposition", "attachment;filename=parserRules.json");
		try (final ServletOutputStream outputStream = servletResponse.getOutputStream()) {
			try (final OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8")) {
				// write the existing (or merged) file to the output stream
				gson.toJson(loadedParserRules, writer);
				writer.flush();
			}
			outputStream.flush();
		}
	}
	
	@RequestMapping(value="/settings/priceDataExplorer/cardkingdom/parserRules.json", method=RequestMethod.PATCH)
	public void patchParserRules(
			final HttpServletResponse servletResponse,
			@RequestBody final String newParserRulesValue) {
		
		final SiteParserRules newParserRules = (gson.fromJson(newParserRulesValue, SiteParserRulesBuilder.class)).build();

		// load the existing parser rules
		final SiteParserRules loadedParserRules = siteParserRulesFactory.loadSiteParserRules();
		
		// do the merge of the new rules with the existing rules
		final SiteParserRules outputParserRules = SiteParserRules.merge(loadedParserRules, newParserRules);
		
		siteParserRulesFactory.saveSiteParserRules(outputParserRules);
		
		servletResponse.setStatus(HttpServletResponse.SC_NO_CONTENT);
	}

	private PriorityQueue<CardSetEntry> convertToCardSetEntries(
			final PriceSiteInfo priceSiteInfo,
			final CardCatalog catalog,
			final SiteParserRules parserRules) {
		final Map<String, List<CardPriceInfo>> setCodeToCardPriceInfo = priceDataLoader.loadCardPriceInfos(priceSiteInfo);
		
		// using ordered collection to ensure that card sets are ordered in the UI
		final PriorityQueue<CardSetEntry> cardSets = new PriorityQueue<>((cs1, cs2) -> cs1.getName().compareTo(cs2.getName()));
		for (final CardSetPriceInfo cardSetPriceInfo : priceSiteInfo.getCardSets()) {
			final CardSet cardSet = cardSetPriceInfo.getCode() == null ? null : catalog.getCardSetByCode(cardSetPriceInfo.getCode());
			
			// if the card set isn't found, it means that code was wrong or not populated so just skip it
			// TODO the data explorer should show these and allow the user to select what to do
			if (cardSet == null) {
				continue;
			}
			
			// if the card set isn't found in the pricing information, then just skip it
			if (!setCodeToCardPriceInfo.containsKey(cardSetPriceInfo.getCode())) {
				continue;
			}
			
			final CardParserRules cardSetParserRules = parserRules.getParserRuleForCardSetCode(cardSet.getCode());
			
			// unused cards will be used in the UI for selection
			final Set<Card> unusedCards = new HashSet<>(cardSet.getCards());

			// using ordered collection to ensure that cards are ordered in the UI
			final UnknownCardEntryCollection unknownCardCollection = new UnknownCardEntryCollection();
			for (final CardPriceInfo cardPriceInfo : setCodeToCardPriceInfo.get(cardSetPriceInfo.getCode())) {
				final Card card = findCard(cardPriceInfo, cardSet, cardSetParserRules);
				if (card == null) {
					
					// check the latest parser rules to see if it's ignored
					if (cardSetParserRules.isIgnored(cardPriceInfo.getRawName())) {
						continue;
					}
					
					unknownCardCollection.add(cardPriceInfo);
				} else {
					unusedCards.remove(card);
				}
			}
			
			cardSets.add(new CardSetEntry(cardSetPriceInfo, unknownCardCollection.toCollection(), unusedCards));
		}
		return cardSets;
	}
	
	/**
	 * Attempt to find the card corresponding to the price information.
	 */
	private Card findCard(CardPriceInfo cardPriceInfo, CardSet cardSet, CardParserRules cardSetParserRules) {

		// if the card has a valid multiverse ID attempt to find the card in the catalog
		if (cardPriceInfo.getMultiverseId() != null) {
			final Card card = cardSet.getCardWithMultiverseId(cardPriceInfo.getMultiverseId());
			if (card != null) {
				return card;
			}
		}
		
		if (cardPriceInfo.getNumber() != null) {
			final Card card = cardSet.getCardWithNumber(cardPriceInfo.getNumber());
			if (card != null) {
				return card;
			}
		}
		
		// attempt to find the card within the latest card parser rules
		final String overrideCardNumber = cardSetParserRules.getCardNumberOverrideForName(cardPriceInfo.getRawName());
		if (overrideCardNumber != null) {
			final Card card = cardSet.getCardWithNumber(overrideCardNumber);
			if (card != null) {
				return card;
			}
		}

		// attempt to find the card within the latest card parser rules
		final Integer overrideMultiverseId = cardSetParserRules.getMultiverseIdForName(cardPriceInfo.getRawName());
		if (overrideMultiverseId != null) {
			final Card card = cardSet.getCardWithMultiverseId(overrideMultiverseId);
			if (card != null) {
				return card;
			}
		}
		
		return null;
	}
	
	private static List<Card> createSortedCardList(Collection<Card> cards) {
		if (cards == null) {
			return null;
		}
		final List<Card> unusedCards = new ArrayList<>(cards);
		unusedCards.sort((c1, c2) -> {
			int nameCompare = c1.getName().compareToIgnoreCase(c2.getName());
			if (nameCompare != 0) {
				return nameCompare;
			}
			
			if (c1.getNumber() != null && c2.getNumber() != null) {
				int numberCompare = c1.getNumber().compareTo(c2.getNumber());
				if (numberCompare != 0) {
					return numberCompare;
				}
			}
			
			if (c1.getMultiverseId() != null && c1.getMultiverseId() != null) {
				int multiverseCompare = c1.getMultiverseId().compareTo(c2.getMultiverseId());
				return multiverseCompare;
			}
			
			return 0;
		});
		return unusedCards;
	}
	
	public static class CardSetEntry {
		private final String name;
		private final String code;
		private final List<UnknownCardEntry> unknownCards;
		private final List<Card> unusedCards;
		
		public CardSetEntry(
				final CardSetPriceInfo cardSetPriceInfo,
				final Collection<UnknownCardEntry> unknownCards,
				final Collection<Card> unusedCards) {
			this.name = getName(cardSetPriceInfo);
			this.code = cardSetPriceInfo.getCode();
			this.unknownCards = unknownCards == null ? Collections.emptyList() : new ArrayList<>(unknownCards);
			this.unusedCards = unusedCards == null ? Collections.emptyList() : createSortedCardList(unusedCards);
		}
		
		public String getName() {
			return this.name;
		}
		
		public String getCode() {
			return this.code;
		}
		
		public List<UnknownCardEntry> getUnknownCards() {
			return this.unknownCards;
		}
		
		public List<Card> getUnusedCards() {
			return unusedCards;
		}
		
		private static String getName(CardSetPriceInfo cardSetPriceInfo) {
			if (cardSetPriceInfo.getName() != null) {
				return cardSetPriceInfo.getName();
			} else {
				return cardSetPriceInfo.getRawName();
			}
		}
	}
	
	public static class UnknownCardEntry {
		private final String rawName;
		
		public UnknownCardEntry(
				final String rawName) {
			this.rawName = rawName;
		}
		
		public String getRawName() {
			return this.rawName;
		}
		
	}
	
	private static class UnknownCardEntryCollection {
		private final PriorityQueue<UnknownCardEntry> unknownCards = new PriorityQueue<>((c1, c2) -> c1.rawName.compareTo(c2.rawName));
		private final Map<String, UnknownCardEntry> nameToUnknownCardMap = new HashMap<>();
		
		public void add(CardPriceInfo cardPriceInfo) {
			if (!nameToUnknownCardMap.containsKey(cardPriceInfo.getRawName())) {
				final UnknownCardEntry unknownCardEntry = new UnknownCardEntry(cardPriceInfo.getRawName());
				nameToUnknownCardMap.put(cardPriceInfo.getRawName(), unknownCardEntry);
				unknownCards.add(unknownCardEntry);
			}
		}
		
		public Collection<UnknownCardEntry> toCollection() {
			return this.unknownCards;
		}
		
		public int size() {
			return this.unknownCards.size();
		}
	}
	
	@ResponseStatus(value = HttpStatus.NOT_FOUND)
	public static class ResourceNotFoundException extends RuntimeException {
		private static final long serialVersionUID = -1562584394563577188L;

		public ResourceNotFoundException() {
			super();
		}

		public ResourceNotFoundException(String message, Throwable cause) {
			super(message, cause);
		}

		public ResourceNotFoundException(String message) {
			super(message);
		}

		public ResourceNotFoundException(Throwable cause) {
			super(cause);
		}
		
	}
}
