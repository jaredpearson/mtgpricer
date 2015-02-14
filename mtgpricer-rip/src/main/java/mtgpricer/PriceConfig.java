package mtgpricer;

import mtgpricer.catalog.CatalogConfig;
import mtgpricer.redis.RedisConfig;
import org.springframework.beans.factory.annotation.Autowired;
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
	
	@Autowired
	UtilConfig utilConfig;
	
	@Autowired
	RedisConfig redisConfig;
	
	@Autowired
	CatalogConfig catalogConfig;
	
	@Bean
	@Lazy
	public RedisPriceService redisPriceService() {
		return new RedisPriceService(redisConfig.redisConnectionProvider(), catalogConfig.cardCatalogProvider());
	}
	
	@Bean
	@Lazy
	public PriceServiceProvider priceServiceProvider() {
		return new PriceServiceProvider() {
			public PriceService getPriceService() {
				return redisPriceService();
			}
		};
	}
}
