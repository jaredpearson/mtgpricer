package mtgpricer;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Creates instances of {@link ConfigurableApplicationContext}
 * @author jared.pearson
 */
public class SpringApplicationContextFactory {
	
	private SpringApplicationContextFactory() {
	}
	
	/**
	 * Creates a new instance of the ApplicationContext for use in command line applications.
	 */
	public static ConfigurableApplicationContext create() {
		return new AnnotationConfigApplicationContext("mtgpricer");
	}
}
