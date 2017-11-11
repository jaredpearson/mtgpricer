package mtgpricer;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Spring configuration for the general utilities
 * @author jared.pearson
 */
@Configuration
@ComponentScan
public class UtilConfig {

	@Value("${db.url}")
	String dbUrl;
	
	@Value("${db.user}")
	String dbUser;
	
	@Value("${db.password}")
	String dbPassword;
	
	@Bean
	@Lazy
	public Gson standardGson() {
		return new GsonBuilder()
				.setDateFormat("yyyy/MM/dd kk:mm:ss Z")
				.create();
	}

	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
		final PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
		configurer.setIgnoreResourceNotFound(true);
		configurer.setLocations(new Resource[]{
			new ClassPathResource("application.properties"),
			new ClassPathResource("application-dev.properties")
		});
		return configurer;
	}
	
	@Bean
	@Lazy
	public DataSource dataSource() {
		ConfigPropertyUtils.assertNotEmpty("db.url", dbUrl);
		
		final BasicDataSource dataSource = new BasicDataSource();
		dataSource.setUrl(dbUrl);
		dataSource.setUsername(dbUser);
		dataSource.setPassword(dbPassword);
		return dataSource;
	}
}