package mtgpricer.bridge;

import static mtgpricer.bridge.RedisKeyFactory.cardKingdomCardPrice;
import static mtgpricer.bridge.RedisKeyFactory.cardList;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mtgpricer.Display;
import mtgpricer.redis.RedisConnectionProvider;
import mtgpricer.rip.CardPriceInfo;
import mtgpricer.rip.PriceDataLoader;
import mtgpricer.rip.PriceSiteInfo;

import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

/**
 * Transfers all of the prices retrieved during the ripping process into the Redis instance
 * @author jared.pearson
 */
public class UpdatePriceTool implements BridgeOperation {
	private RedisConnectionProvider redisConnectionProvider;
	private PriceDataLoader priceDataLoader = null;
	
	@Autowired
	public void setPriceDataLoader(PriceDataLoader priceDataLoader) {
		this.priceDataLoader = priceDataLoader;
	}
	
	@Autowired
	public void setRedisConnectionProvider(RedisConnectionProvider redisConnectionProvider) {
		this.redisConnectionProvider = redisConnectionProvider;
	}
	
	/**
	 * Processes all of the data within the machine and sends it to the Redis instance
	 */
	@Override
	public void execute(Display display) throws Exception {
		final Jedis jedis = redisConnectionProvider.getConnection();
		try {
			final Set<? extends PriceSiteInfo> priceData = priceDataLoader.loadPriceData();
			for (final PriceSiteInfo priceSite : priceData) {
				final Transaction t = jedis.multi();

				final Map<String, List<CardPriceInfo>> setCodeToCardPriceInfos = priceDataLoader.loadCardPriceInfos(priceSite);
				final Set<String> multiverseIdsToAdd = new HashSet<String>();
				final Date retrieved = priceSite.getRetrieved();
				for (final List<CardPriceInfo> cardSetPriceInfos : setCodeToCardPriceInfos.values()) {
					for (final CardPriceInfo cardPrice : cardSetPriceInfos) {
						final Integer multiverseId = cardPrice.getMultiverseId();
						if (multiverseId == null) {
							display.writeln("Skipping card with no multiverse ID: " + (cardPrice.getName() != null ? cardPrice.getName() : cardPrice.getRawName()));
							continue;
						}
						
						multiverseIdsToAdd.add(multiverseId.toString());
						
						final long time = retrieved.getTime();
						final String timeAsString = Long.toString(retrieved.getTime());
						final String priceAsString = Double.toString(cardPrice.getPrice());
						t.set(cardKingdomCardPrice(multiverseId, time), priceAsString);
						
						t.zadd(cardKingdomCardPrice(multiverseId), time, timeAsString);
					}
					
				}
				
				if (!multiverseIdsToAdd.isEmpty()) {
					t.sadd(cardList(), multiverseIdsToAdd.toArray(new String[multiverseIdsToAdd.size()]));
				}
				
				t.exec();
			}
		
		} finally {
			redisConnectionProvider.returnConnection(jedis);
		}
	}
}