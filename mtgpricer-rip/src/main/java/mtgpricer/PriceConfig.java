package mtgpricer;

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
	public PriceService priceService(PriceDataLoader priceDataLoader) {
		return new PriceServiceImpl(priceDataLoader);
	}
	
	@Bean
	@Lazy
	public PriceServiceProvider priceServiceProvider(final PriceService priceService) {
		return new PriceServiceProvider() {
			public PriceService getPriceService() {
				return priceService;
			}
		};
	}
}
