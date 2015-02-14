package mtgpricer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import mtgpricer.bridge.RedisKeyFactory;
import mtgpricer.catalog.Card;
import mtgpricer.catalog.CardCatalog;
import mtgpricer.catalog.CardCatalogProvider;
import mtgpricer.catalog.CardSet;
import mtgpricer.redis.RedisConnectionProvider;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.Tuple;

/**
 * Price service that uses Redis to provide price information.
 * @author jared.pearson
 */
public class RedisPriceService implements PriceService {
	private static final Logger logger = Logger.getLogger(RedisPriceService.class.getName());
	private final RedisConnectionProvider redisConnectionProvider;
	private final CardCatalogProvider cardCatalogProvider;
	
	public RedisPriceService(RedisConnectionProvider redisConnectionProvider, CardCatalogProvider cardCatalogProvider) {
		assert redisConnectionProvider != null;
		assert cardCatalogProvider != null;
		this.redisConnectionProvider = redisConnectionProvider;
		this.cardCatalogProvider = cardCatalogProvider;
	}
	
	public CardPrice getCurrentPriceForCard(Card card) {
		assert card != null;
		final Map<Card, List<CardPrice>> cardPrices = this.getPriceHistoryForCards(newArrayList(card), -1, -1, new CardPriceQueryParams());
		if (cardPrices.containsKey(card)) {
			return cardPrices.get(card).get(0);
		} else {
			return null;
		}
	}
	
	public List<CardPrice> getPriceHistoryForCard(Card card) {
		return getPriceHistoryForCard(card, new CardPriceQueryParams());
	}
	
	public List<CardPrice> getPriceHistoryForCard(Card card, CardPriceQueryParams params) {
		assert card != null;
		assert params != null;
		
		final Map<Card, List<CardPrice>> cardPrices = this.getPriceHistoryForCards(newArrayList(card), -params.getLimit(), -1, params);
		if (cardPrices.containsKey(card)) {
			return cardPrices.get(card);
		} else {
			return null;
		}
	}

	public Map<Card, List<CardPrice>> getPriceHistoryForCards(CardSet cardSet) {
		return getPriceHistoryForCards(cardSet.getCards(), -10, -1, new CardPriceQueryParams());
	}
	
	public List<CardPriceDiff> getTopPositiveCardPriceDiffSevenDays() {
		final String key = RedisKeyFactory.cardKingdomCardPriceDiff7Rank();
		final Set<Tuple> values = getZRevRangeWithScore(key);
		
		if (values == null) {
			logger.warning("Unable to find the positive diff rank for seven days: " + key);
			return Collections.emptyList();
		}
		
		return buildCardPriceDiffFromZRankTuple(values);
	}
	
	public List<CardPriceDiff> getTopNegativeCardPriceDiffSevenDays() {
		final String key = RedisKeyFactory.cardKingdomCardPriceDiff7Rank();
		final Set<Tuple> values = getZRangeWithScore(key);
		
		if (values == null) {
			logger.warning("Unable to find the negative diff rank for seven days: " + key);
			return Collections.emptyList();
		}
		
		return buildCardPriceDiffFromZRankTuple(values);
	}
	
	public List<CardPriceDiff> getTopPositiveCardPriceDiffSevenDaysStandard() {
		final String key = RedisKeyFactory.cardKingdomCardPriceDiff7RankStandard();
		final Set<Tuple> values = getZRevRangeWithScore(key);
		
		if (values == null) {
			logger.warning("Unable to find the positive diff rank for seven days: " + key);
			return Collections.emptyList();
		}
		
		return buildCardPriceDiffFromZRankTuple(values);
	}

	private Set<Tuple> getZRevRangeWithScore(final String key) {
		assert key != null;
		final Set<Tuple> values;
		final Jedis jedis = redisConnectionProvider.getConnection();
		try {
			values = jedis.zrevrangeWithScores(key, 0, 9);
		} finally {
			redisConnectionProvider.returnConnection(jedis);
		}
		return values;
	}
	
	private Set<Tuple> getZRangeWithScore(final String key) {
		assert key != null;
		final Set<Tuple> values;
		final Jedis jedis = redisConnectionProvider.getConnection();
		try {
			values = jedis.zrangeWithScores(key, 0, 9);
		} finally {
			redisConnectionProvider.returnConnection(jedis);
		}
		return values;
	}
	
	private List<CardPriceDiff> buildCardPriceDiffFromZRankTuple(Collection<? extends Tuple> tuples) {
		assert tuples != null;
		if (tuples.isEmpty()) {
			return Collections.emptyList();
		}
		
		final List<CardPriceDiffBuilder> priceDiffBuilders = new ArrayList<CardPriceDiffBuilder>(tuples.size());
		final Set<Integer> multiverseIds = new HashSet<Integer>(tuples.size());
		for (Tuple entry : tuples) {
			final int multiverseId;
			try {
				final String multiverseIdRaw = entry.getElement();
				multiverseId = Integer.parseInt(multiverseIdRaw);
			} catch(NumberFormatException exc) {
				logger.warning("Unable to convert rank value to long: " + entry.getElement());
				continue;
			}
			
			final double score = entry.getScore();
			
			final CardPriceDiffBuilder builder = new CardPriceDiffBuilder();
			builder.setMultiverseId(multiverseId);
			builder.setValue(new Money(Double.toString(score)));
			priceDiffBuilders.add(builder);
			
			multiverseIds.add(multiverseId);
		}
		
		final CardCatalog cardCatalog = cardCatalogProvider.getCardCatalog();
		final Map<Integer, Card> cards = cardCatalog.getCardsWithMultiverseIds(multiverseIds);
		final List<CardPriceDiff> priceDiffs = new ArrayList<CardPriceDiff>(priceDiffBuilders.size());
		for (CardPriceDiffBuilder builder : priceDiffBuilders) {
			if (!cards.containsKey(builder.getMultiverseId())) {
				logger.warning("Unable to find a card with multiverse ID: " + builder.getMultiverseId());
				continue;
			}
			
			builder.setCard(cards.get(builder.getMultiverseId()));
			
			priceDiffs.add(builder.build());
		}
		return priceDiffs;
	}
	
	
	private Map<Card, List<CardPrice>> getPriceHistoryForCards(Collection<? extends Card> cards, int start, int stop, CardPriceQueryParams params) {
		assert params != null;
		if (cards == null || cards.size() == 0) {
			return Collections.emptyMap();
		}
		
		final Comparator<CardPrice> cardPriceComparator = new CardPriceComparator(params.getOrder(), params.getOrderDirection());
		
		final Jedis jedis = redisConnectionProvider.getConnection();
		try {
			
			// load up the latest times for every card
			final Pipeline timesPipeline = jedis.pipelined();
			final Map<Integer, Response<Set<String>>> multiverseIdToTimeResponses = new HashMap<Integer, Response<Set<String>>>(cards.size());
			for (final Card card : cards) {
				final Integer multiverseId = card.getMultiverseId();
				if (multiverseId == null) {
					logger.warning("Skipping card with no multiverse ID: " + card.getName());
					continue;
				}
				
				final String cardPriceListKey = RedisKeyFactory.cardKingdomCardPrice(multiverseId);
				final Response<Set<String>> latestTimes = timesPipeline.zrange(cardPriceListKey, start, stop);
				multiverseIdToTimeResponses.put(multiverseId,	latestTimes);
			}
			timesPipeline.sync();
			
			// load up the prices associated to the latest times
			final Pipeline pricePipeline = jedis.pipelined();
			final Map<Integer, Set<RedisPriceService.CardPriceLoadContext>> cardToPriceResponses = new HashMap<Integer, Set<RedisPriceService.CardPriceLoadContext>>(multiverseIdToTimeResponses.size());
			for (final Map.Entry<Integer, Response<Set<String>>> entry : multiverseIdToTimeResponses.entrySet()) {
				final Integer multiverseId = entry.getKey();
				final Set<String> latestTimes = entry.getValue().get();
				
				final Set<RedisPriceService.CardPriceLoadContext> loadContexts = new LinkedHashSet<RedisPriceService.CardPriceLoadContext>(latestTimes.size());
				for (final String latestTime : latestTimes) {
					final long retrieved = Long.parseLong(latestTime);
					
					final String cardPriceKey = RedisKeyFactory.cardKingdomCardPrice(multiverseId, retrieved);
					final Response<String> priceResponse = pricePipeline.get(cardPriceKey);
					loadContexts.add(new CardPriceLoadContext(cardPriceKey, new Date(retrieved), priceResponse));
				}
				if (!loadContexts.isEmpty()) {
					cardToPriceResponses.put(multiverseId, loadContexts);
				}
			}
			pricePipeline.sync();
			
			// build the price history for every card specified
			final Map<Card, List<CardPrice>> cardToPrices = new LinkedHashMap<Card, List<CardPrice>>(cards.size());
			for (final Card card : cards) {
				final Set<RedisPriceService.CardPriceLoadContext> priceLoadContexts = cardToPriceResponses.get(card.getMultiverseId());
				if (priceLoadContexts == null || priceLoadContexts.isEmpty()) {
					cardToPrices.put(card, Collections.<CardPrice>emptyList());
					continue;
				}
				
				final List<CardPrice> prices = new ArrayList<CardPrice>(priceLoadContexts.size());
				for (final RedisPriceService.CardPriceLoadContext priceLoadContext : priceLoadContexts) {
					final String priceRaw = priceLoadContext.priceResponse.get();
					if (priceRaw == null) {
						continue;
					}
					
					final double price;
					try {
						price = Double.parseDouble(priceRaw);
					} catch(NumberFormatException exc) {
						System.out.println(String.format("Unable to parse double from price at %s: %s", priceLoadContext.key, priceRaw));
						continue;
					}
					
					final CardPrice cardPrice = new CardPrice(priceLoadContext.retrieved, price);
					prices.add(cardPrice);
				}
				Collections.sort(prices, cardPriceComparator);
				cardToPrices.put(card, Collections.unmodifiableList(prices));
			}
			return Collections.unmodifiableMap(cardToPrices);
			
		} finally {
			redisConnectionProvider.returnConnection(jedis);
		}
	}

	private static <T> List<T> newArrayList(T value) {
		final List<T> list = new ArrayList<T>(1);
		if (value != null) {
			list.add(value);
		}
		return list;
	}
	
	private static class CardPriceLoadContext {
		public final String key;
		public final Date retrieved;
		public final Response<String> priceResponse;
		
		public CardPriceLoadContext(String key, Date retrieved, Response<String> priceResponse) {
			this.key = key;
			this.retrieved = retrieved;
			this.priceResponse = priceResponse;
		}
	}
	
	private static class CardPriceDiffBuilder {
		private Integer multiverseId;
		private Card card;
		private Money value;
		
		public void setCard(Card card) {
			this.card = card;
		}
		
		public void setMultiverseId(Integer multiverseId) {
			this.multiverseId = multiverseId;
		}
		
		public Integer getMultiverseId() {
			return multiverseId;
		}
		
		public void setValue(Money value) {
			this.value = value;
		}
		
		public CardPriceDiff build() {
			if (multiverseId == null) {
				throw new IllegalStateException("MultiverseId should never be null");
			}
			
			return new CardPriceDiff(card, multiverseId, value);
		}
	}
}