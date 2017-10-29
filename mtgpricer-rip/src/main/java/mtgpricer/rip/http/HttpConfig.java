package mtgpricer.rip.http;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
@ComponentScan
public class HttpConfig {

	@Bean
	@Lazy
	public PageRequesterFactory pageRequesterFactory() {
		return new PageRequesterFactoryImpl();
	}
	
	private static final class PageRequesterFactoryImpl implements PageRequesterFactory {
		@Override
		public PageRequester create() {
			return new HttpPageRequester();
		}

		@Override
		public PageRequester createCachedPageRequester() {
			return new CachedPageRequestor(this.create());
		}
	}
}