package mtgpricer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import mtgpricer.catalog.Card;
import mtgpricer.catalog.CardSet;
import mtgpricer.rip.CardPriceInfo;
import mtgpricer.rip.PriceDataLoader;
import mtgpricer.rip.PriceSiteInfo;

/**
 * Service for getting price information from in-memory representation of the 
 * card price information.
 * @author jared.pearson
 */
public class PriceServiceImpl implements PriceService {
	private final PriceDataLoader priceDataLoader;
	
	public PriceServiceImpl(final PriceDataLoader priceDataLoader) {
		this.priceDataLoader = priceDataLoader;
	}
	
	@Override
	public List<CardPrice> getPriceHistoryForCard(Card card) {
		return getPriceHistoryForCard(card, new CardPriceQueryParams());
	}
	
	@Override
	public List<CardPrice> getPriceHistoryForCard(final Card card, final CardPriceQueryParams params) {
		assert card != null;
		assert params != null;
		
		final TreeMap<Date, PriceSite> dateRetrievedByPriceSite = createDateRetrievedMap(priceDataLoader.loadPriceData());
		if (dateRetrievedByPriceSite.isEmpty()) {
			return Collections.emptyList();
		}
		final CardPriceComparator priceComparator = new CardPriceComparator(params.getOrder(), params.getOrderDirection());
		
		final ArrayList<CardPrice> history = new ArrayList<CardPrice>();
		for (final PriceSite priceSite : dateRetrievedByPriceSite.values()) {
			final CardPriceInfo cardPriceInfo = priceDataLoader.loadCardPriceInfoByMultiverseId(priceSite.getId(), card.getMultiverseId());
			if (cardPriceInfo != null) {
				final CardPrice cardPrice = new CardPrice(priceSite.getRetrieved(), cardPriceInfo);
				history.add(cardPrice);
			}
		}
		history.trimToSize();
		if (history.isEmpty()) {
			return Collections.emptyList();
		}
		
		Collections.sort(history, priceComparator);
		
		return Collections.unmodifiableList(history.subList(0, Math.min(params.getLimit(), history.size())));
	}
	
	@Override
	public Map<Card, List<CardPrice>> getPriceHistoryForCards(CardSet cardSet) {
		assert cardSet != null;
		
		final TreeMap<Date, PriceSite> dateRetrievedByPriceSite = createDateRetrievedMap(priceDataLoader.loadPriceData());
		if (dateRetrievedByPriceSite.isEmpty()) {
			return Collections.emptyMap();
		}
		final CardPriceQueryParams params = new CardPriceQueryParams();
		final CardPriceComparator priceComparator = new CardPriceComparator(params.getOrder(), params.getOrderDirection());

		final Map<Integer, LinkedList<CardPrice>> multiverseIdToCardPrices = new HashMap<>();
		for (final PriceSite priceSite : dateRetrievedByPriceSite.values()) {
			final List<CardPriceInfo> cardPriceInfos = priceDataLoader.loadCardPriceInfos(priceSite.getId(), cardSet.getCode());
			
			for (final CardPriceInfo cardPriceInfo : cardPriceInfos) {
				if (cardPriceInfo.getMultiverseId() == null) {
					continue;
				}
				
				final LinkedList<CardPrice> cardPrices;
				if (!multiverseIdToCardPrices.containsKey(cardPriceInfo.getMultiverseId())) {
					cardPrices = new LinkedList<>();
					multiverseIdToCardPrices.put(cardPriceInfo.getMultiverseId(), cardPrices);
				} else {
					cardPrices = multiverseIdToCardPrices.get(cardPriceInfo.getMultiverseId());
				}
				
				cardPrices.add(new CardPrice(priceSite.getRetrieved(), cardPriceInfo));
				cardPrices.sort(priceComparator);
				
				// we know that we only want a specific amount of history entries so remove any
				// that are not necessary
				if (cardPrices.size() > params.getLimit()) {
					cardPrices.removeLast();
				}
			}
		}

		final Map<Card, List<CardPrice>> priceHistories = new HashMap<Card, List<CardPrice>>();
		for (final Card card : cardSet.getCards()) {
			final List<CardPrice> history;
			if (card.getMultiverseId() == null || !multiverseIdToCardPrices.containsKey(card.getMultiverseId())) {
				history = Collections.emptyList();
			} else {
				history = new ArrayList<>(multiverseIdToCardPrices.get(card.getMultiverseId()));
			}
			priceHistories.put(card, history);
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
