package mtgpricer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
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

		// date should be in newest to oldest order
		final TreeMap<Date, PriceSite> dateRetrievedByPriceSite = createDateRetrievedMap(priceDataLoader.loadPriceData());
		if (dateRetrievedByPriceSite.isEmpty()) {
			return Collections.emptyList();
		}

		Integer count = 0;
		final ArrayList<CardPrice> history = new ArrayList<>();
		for (final PriceSite priceSite : dateRetrievedByPriceSite.values()) {
			final List<CardPriceInfo> cardPriceInfos = priceDataLoader.loadCardPriceInfosByMultiverseId(priceSite.getId(), card.getMultiverseId());
			if (!cardPriceInfos.isEmpty()) {
				final CardPrice cardPrice = new CardPrice(priceSite.getRetrieved(), cardPriceInfos);
				history.add(cardPrice);
			}

			// stop looking at histories if we have looked over the limit
			count++;
			if (count == params.getLimit()) {
				break;
			}
		}
		history.trimToSize();
		return history;
	}
	
	@Override
	public Map<Card, List<CardPrice>> getPriceHistoryForCards(CardSet cardSet) {
		assert cardSet != null;
		
		// date should be in newest to oldest order
		final TreeMap<Date, PriceSite> dateRetrievedByPriceSite = createDateRetrievedMap(priceDataLoader.loadPriceData());
		if (dateRetrievedByPriceSite.isEmpty()) {
			return Collections.emptyMap();
		}
		final CardPriceQueryParams params = new CardPriceQueryParams();
		
		Integer count = 0;
		final TreeMap<Integer, List<CardPrice>> multiverseIdToCardPrices = new TreeMap<>();
		for (final PriceSite priceSite : dateRetrievedByPriceSite.values()) {
			final List<CardPriceInfo> allCardPriceInfos = priceDataLoader.loadCardPriceInfos(priceSite.getId(), cardSet.getCode());

			// group each of the price entry (due to having multiple conditions) by the multiverse ID of the card associated to the variant
			final Map<Integer, List<CardPriceInfo>> multiverseIdToCardPriceInfos = new HashMap<>();
			for (final CardPriceInfo cardPriceInfo : allCardPriceInfos) {
				if (cardPriceInfo.getMultiverseId() == null) {
					continue;
				}
				
				if (!multiverseIdToCardPriceInfos.containsKey(cardPriceInfo.getMultiverseId())) {
					multiverseIdToCardPriceInfos.put(cardPriceInfo.getMultiverseId(), new ArrayList<>());
				}
				
				multiverseIdToCardPriceInfos.get(cardPriceInfo.getMultiverseId()).add(cardPriceInfo);
			}
			
			// convert each of the groupings into CardPrice entities
			for (Map.Entry<Integer, List<CardPriceInfo>> multiverseIdToCardPriceInfosEntry : multiverseIdToCardPriceInfos.entrySet()) {
				CardPrice cardPrice = new CardPrice(priceSite.getRetrieved(), multiverseIdToCardPriceInfosEntry.getValue());
				
				if (!multiverseIdToCardPrices.containsKey(multiverseIdToCardPriceInfosEntry.getKey())) {
					multiverseIdToCardPrices.put(multiverseIdToCardPriceInfosEntry.getKey(), new ArrayList<>());
				}
				multiverseIdToCardPrices.get(multiverseIdToCardPriceInfosEntry.getKey()).add(cardPrice);
			}
			
			// stop looking at histories if we have looked over the limit
			count++;
			if (count == params.getLimit()) {
				break;
			}
		}

		final Map<Card, List<CardPrice>> priceHistories = new HashMap<>();
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
		final TreeMap<Date, PriceSite> dateRetrievedByPriceSite = new TreeMap<Date, PriceSite>(new Comparator<Date>() {
			@Override
			public int compare(Date date1, Date date2) {
				return -date1.compareTo(date2);
			}
		});
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
