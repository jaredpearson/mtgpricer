package mtgpricer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import mtgpricer.catalog.Card;
import mtgpricer.catalog.CardSet;
import mtgpricer.rip.PriceDataLoader;
import mtgpricer.rip.PriceSiteInfo;

/**
 * Service for getting price information from in-memory representation of the 
 * card price information.
 * @author jared.pearson
 */
public class PriceServiceImpl implements PriceService {
	private final TreeMap<Date, PriceSite> dateRetrievedByPriceSite;
	
	public PriceServiceImpl(final PriceDataLoader priceDataLoader) {
		this.dateRetrievedByPriceSite = createDateRetrievedMap(priceDataLoader.loadPriceData());
	}
	
	public PriceServiceImpl(final Set<PriceSiteInfo> priceSiteInfos) {
		this.dateRetrievedByPriceSite = createDateRetrievedMap(priceSiteInfos);
	}

	@Override
	public CardPrice getCurrentPriceForCard(Card card) {
		assert card != null;
		
		if (dateRetrievedByPriceSite.isEmpty()) {
			return null;
		}
		
		final PriceSite priceSite = dateRetrievedByPriceSite.lastEntry().getValue();
		return priceSite.getCardPriceByMultiverseId(card.getMultiverseId());
	}
	
	@Override
	public List<CardPrice> getPriceHistoryForCard(Card card) {
		return getPriceHistoryForCard(card, new CardPriceQueryParams());
	}
	
	@Override
	public List<CardPrice> getPriceHistoryForCard(final Card card, final CardPriceQueryParams params) {
		assert card != null;
		assert params != null;
		
		if (dateRetrievedByPriceSite.isEmpty()) {
			return Collections.emptyList();
		}
		
		final ArrayList<CardPrice> history = new ArrayList<CardPrice>();
		for (final PriceSite priceSite : dateRetrievedByPriceSite.values()) {
			final CardPrice cardPrice = priceSite.getCardPriceByMultiverseId(card.getMultiverseId());
			if (cardPrice != null) {
				history.add(cardPrice);
			}
		}
		history.trimToSize();
		if (history.isEmpty()) {
			return Collections.emptyList();
		}
		
		Collections.sort(history, new CardPriceComparator(params.getOrder(), params.getOrderDirection()));
		
		return Collections.unmodifiableList(history.subList(0, Math.min(params.getLimit(), history.size())));
	}
	
	@Override
	public Map<Card, List<CardPrice>> getPriceHistoryForCards(CardSet cardSet) {
		final Map<Card, List<CardPrice>> priceHistories = new LinkedHashMap<Card, List<CardPrice>>();
		for (Card card : cardSet.getCards()) {
			priceHistories.put(card, getPriceHistoryForCard(card));
		}
		return priceHistories;
	}
	
	@Override
	public List<CardPriceDiff> getTopPositiveCardPriceDiffSevenDays() {
		// this is not implemented for the in-memory service
		return Collections.emptyList();
	}
	
	@Override
	public List<CardPriceDiff> getTopNegativeCardPriceDiffSevenDays() {
		// this is not implemented for the in-memory service
		return Collections.emptyList();
	}
	
	@Override
	public List<CardPriceDiff> getTopPositiveCardPriceDiffSevenDaysStandard() {
		// this is not implemented for the in-memory service
		return Collections.emptyList();
	}

	private static TreeMap<Date, PriceSite> createDateRetrievedMap(final Set<? extends PriceSiteInfo> priceSiteInfos) {
		final TreeMap<Date, PriceSite> dateRetrievedByPriceSite = new TreeMap<Date, PriceSite>();
		if (priceSiteInfos != null) {
			for (final PriceSiteInfo priceSiteInfo : priceSiteInfos) {
				if (priceSiteInfo.getRetrieved() != null) {
					dateRetrievedByPriceSite.put(priceSiteInfo.getRetrieved(), new PriceSite(priceSiteInfo));
				}
			}
		}
		return dateRetrievedByPriceSite;
	}
}
