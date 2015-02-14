package mtgpricer.redis;

import mtgpricer.ConfigPropertyUtils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
@ComponentScan
public class RedisConfig {
	
	@Value("${redis.host}")
	String redisHost;
	
	@Value("${redis.port}")
	int redisPort;
	
	@Bean
	@Lazy
	public RedisConnectionProvider redisConnectionProvider() {
		ConfigPropertyUtils.assertNotEmpty("redis.host", redisHost);
		return new RedisConnectionProviderImpl(redisHost, redisPort);
	}
	
}
