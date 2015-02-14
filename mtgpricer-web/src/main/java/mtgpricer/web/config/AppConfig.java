package mtgpricer.web.config;

import java.io.IOException;

import javax.sql.DataSource;

import mtgpricer.bridge.Bridge;
import mtgpricer.rip.RipProcessor;
import mtgpricer.rip.RipRequestProcessorFactory;
import mtgpricer.rip.RipRequestQueue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.ContextStoppedEvent;

@Configuration
@ComponentScan(basePackages = "mtgpricer")
public class AppConfig {
	
	@Autowired
	RipProcessor ripProcessor;
	
	@Autowired
	Bridge bridge;
	
	@Autowired
	DataSource dataSource;
	
	@Bean
	@Lazy
	public RipRequestQueue ripRequestQueue() throws IOException {
		return new RipRequestQueue(dataSource, processRipRequestRunnableFactory());
	}
	
	@Bean
	@Lazy
	public RipRequestProcessorFactory processRipRequestRunnableFactory() {
		return new RipRequestProcessorFactory(ripProcessor, bridge);
	}
	
	@Bean
	public ApplicationListener<ContextStoppedEvent> ripQueueProcessorAppCloseListener() {
		return new ApplicationListener<ContextStoppedEvent>() {
			@Override
			public void onApplicationEvent(ContextStoppedEvent event) {
				try {
					ripRequestQueue().close();
				} catch(IOException exc) {
					System.err.println("Error occurred while closing RipRequestQueueProcessor");
					exc.printStackTrace(System.err);
				}
			}
		};
	}
	
}
