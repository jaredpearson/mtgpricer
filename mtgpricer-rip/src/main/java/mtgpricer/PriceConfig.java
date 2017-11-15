package mtgpricer;

import mtgpricer.catalog.CatalogConfig;
import mtgpricer.redis.RedisConfig;
import mtgpricer.rip.PriceDataLoader;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * Spring configuration for the Price Services 
 * @author jared.pearson
 */
@Configuration
@ComponentScan
public class PriceConfig {
	
	@Bean
	@Lazy
	public RedisPriceService redisPriceService(RedisConfig redisConfig, CatalogConfig catalogConfig) {
		return new RedisPriceService(redisConfig.redisConnectionProvider(), catalogConfig.cardCatalogProvider());
	}
	
	@Bean
	@Lazy
	public PriceServiceProvider priceServiceProvider(PriceDataLoader priceDataLoader) {
		final PriceServiceImpl priceServiceImpl = new PriceServiceImpl(priceDataLoader);
		return new PriceServiceProvider() {
			public PriceService getPriceService() {
				return priceServiceImpl;
			}
		};
	}
}
