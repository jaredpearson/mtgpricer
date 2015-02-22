package mtgpricer.bridge;

import static mtgpricer.bridge.RedisKeyFactory.cardKingdomCardPrice;
import static mtgpricer.bridge.RedisKeyFactory.cardKingdomCardPriceDiff7;
import static mtgpricer.bridge.RedisKeyFactory.cardKingdomCardPriceDiff7Rank;
import static mtgpricer.bridge.RedisKeyFactory.cardKingdomCardPriceDiff7RankStandard;
import static mtgpricer.bridge.RedisKeyFactory.cardKingdomCardPriceDiff7RankStandardTemporary;
import static mtgpricer.bridge.RedisKeyFactory.cardKingdomCardPriceDiff7RankTemporary;
import static mtgpricer.bridge.RedisKeyFactory.cardList;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import mtgpricer.Display;
import mtgpricer.Money;
import mtgpricer.catalog.Card;
import mtgpricer.catalog.CardCatalog;
import mtgpricer.catalog.CardCatalogProvider;
import mtgpricer.redis.RedisConnectionProvider;

import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.ScanResult;

/**
 * Calculates all of the card price statistics and writes the information to the 
 * Redis server.
 * @author jared.pearson
 */
public class UpdateStatsTool implements BridgeOperation {
	private RedisConnectionProvider redisConnectionProvider;
	private CardCatalogProvider cardCatalogProvider = null;
	
	@Autowired
	public void setCardCatalogProvider(CardCatalogProvider cardCatalogProvider) {
		this.cardCatalogProvider = cardCatalogProvider;
	}
	
	@Autowired
	public void setRedisConnectionProvider(RedisConnectionProvider redisConnectionProvider) {
		this.redisConnectionProvider = redisConnectionProvider;
	}
	
	/**
	 * Updates the statistics for cards found within the price service.
	 */
	@Override
	public void execute(Display display) throws Exception {
		final CardCatalog cardCatalog = cardCatalogProvider.getCardCatalog();
		
		final Calendar cal = Calendar.getInstance();
		cal.clear(Calendar.HOUR_OF_DAY);
		cal.clear(Calendar.MINUTE);
		cal.clear(Calendar.SECOND);
		cal.clear(Calendar.MILLISECOND);
		
		final Date now = cal.getTime();
		
		cal.add(Calendar.DATE, -7);
		final Date sevenDaysAgo = cal.getTime();
		
		final Jedis jedis = redisConnectionProvider.getConnection(); 
		try {
			
			// remove the cache rank if it exists 
			jedis.del(cardKingdomCardPriceDiff7RankTemporary());
			jedis.del(cardKingdomCardPriceDiff7RankStandardTemporary());
			
			final Long numberOfCards = jedis.scard(cardList());
			if (numberOfCards == null) {
				return;
			}
			display.writeln("Processing " + numberOfCards + " cards");
			long processedCount = 0;
			
			String cardListCursor = "0";
			do {
				final ScanResult<String> cardListResults = jedis.sscan(cardList(), cardListCursor);
				cardListCursor = cardListResults.getStringCursor();
				
				final Map<Integer, Response<Set<String>>> multiverseIdToTimeResponses = new HashMap<Integer, Response<Set<String>>>();
				final Pipeline timesPipeline = jedis.pipelined();
				for (String multiverseIdString : cardListResults.getResult()) {
					final int multiverseId = Integer.parseInt(multiverseIdString);
					final Response<Set<String>> sevenDayTimes = timesPipeline.zrangeByScore(cardKingdomCardPrice(multiverseId), sevenDaysAgo.getTime(), now.getTime());
					multiverseIdToTimeResponses.put(multiverseId, sevenDayTimes);
				}
				timesPipeline.sync();
				
				final Map<Integer, Card> multiverseIdToCard = cardCatalog.getCardsWithMultiverseIds(multiverseIdToTimeResponses.keySet());

				final Map<Integer, UpdateStatsTool.RangeResponse> multiverseIdToPriceRangeResponses = new HashMap<Integer, UpdateStatsTool.RangeResponse>(multiverseIdToTimeResponses.size());
				final Set<Integer> multiverseIdsWithoutData = new HashSet<Integer>(); 
				final Pipeline pricePipeline = jedis.pipelined();
				for (Map.Entry<Integer, Response<Set<String>>> responseEntry : multiverseIdToTimeResponses.entrySet()) {
					final Integer multiverseId = responseEntry.getKey();
					final Set<String> sevenDayTimes = responseEntry.getValue().get();
					
					String firstTime = null;
					String latestTime = null;
					for (String time : sevenDayTimes) {
						if (firstTime == null) {
							firstTime = time;
						}
						latestTime = time;
					}
					
					if (firstTime == null || firstTime.equals(latestTime)) {
						multiverseIdsWithoutData.add(multiverseId);
						continue;
					}
					
					final Response<String> firstTimePrice = pricePipeline.get(cardKingdomCardPrice(multiverseId, Long.parseLong(firstTime)));
					final Response<String> latestTimePrice = pricePipeline.get(cardKingdomCardPrice(multiverseId, Long.parseLong(latestTime)));
					multiverseIdToPriceRangeResponses.put(multiverseId, new RangeResponse(firstTimePrice, latestTimePrice));
				}
				pricePipeline.sync();
				
				final Pipeline diffPipeline = jedis.pipelined();
				
				// if a card has incomplete data, make sure that the key is deleted
				for (Integer multiverseId : multiverseIdsWithoutData) {
					diffPipeline.del(cardKingdomCardPriceDiff7(multiverseId));
				}
				processedCount += multiverseIdsWithoutData.size();
				
				for (Map.Entry<Integer, UpdateStatsTool.RangeResponse> responseEntry : multiverseIdToPriceRangeResponses.entrySet()) {
					final Integer multiverseId = responseEntry.getKey();
					final UpdateStatsTool.RangeResponse response = responseEntry.getValue();
					
					final Money firstPrice = new Money(response.firstPrice.get());
					final Money lastPrice = new Money(response.lastPrice.get());
					
					final Money diffSevenDays = lastPrice.subtract(firstPrice);
					
					if (!firstPrice.equals(lastPrice)) {
						
						// add the price diff to the all card rank
						diffPipeline.zadd(cardKingdomCardPriceDiff7RankTemporary(), diffSevenDays.doubleValue(), multiverseId.toString());
						
						// if the card is valid in standard, then add it to the standard rank
						if (multiverseIdToCard.containsKey(multiverseId) && multiverseIdToCard.get(multiverseId).isValidInStandardTournamentFormat()) {
							diffPipeline.zadd(cardKingdomCardPriceDiff7RankStandardTemporary(), diffSevenDays.doubleValue(), multiverseId.toString());
						}
						
					}
					
					diffPipeline.set(cardKingdomCardPriceDiff7(multiverseId), diffSevenDays.toString());
					processedCount++;
				} 
				diffPipeline.sync();
				
				display.writeln("Processed " + processedCount + "/" + numberOfCards + " cards");
			} while(!cardListCursor.equals("0"));
			
			// copy the new rank to the existing location
			jedis.rename(cardKingdomCardPriceDiff7RankTemporary(), cardKingdomCardPriceDiff7Rank());
			jedis.rename(cardKingdomCardPriceDiff7RankStandardTemporary(), cardKingdomCardPriceDiff7RankStandard());
			
		} finally {
			redisConnectionProvider.returnConnection(jedis);
		}
	}
	
	private static class RangeResponse {
		public final Response<String> firstPrice;
		public final Response<String> lastPrice;
		
		public RangeResponse(Response<String> firstPrice, Response<String> lastPrice) {
			this.firstPrice = firstPrice;
			this.lastPrice = lastPrice;
		}
	}
	
}