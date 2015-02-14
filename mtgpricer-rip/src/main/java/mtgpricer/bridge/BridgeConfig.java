package mtgpricer.bridge;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * Configuration for the bridge 
 * @author jared.pearson
 */
@Configuration
@ComponentScan
public class BridgeConfig {
	
	@Bean
	@Lazy
	public UpdatePriceTool updatePriceTool() {
		return new UpdatePriceTool();
	}
	
	@Bean
	@Lazy
	public UpdateStatsTool updateStatsTool() {
		return new UpdateStatsTool();
	}
	
	
	@Bean
	@Lazy
	public mtgpricer.bridge.Main bridgeMain() {
		return new mtgpricer.bridge.Main();
	}
	
	@Bean
	@Lazy
	public Bridge bridge() {
		return new Bridge(updatePriceTool(), updateStatsTool());
	}
}
