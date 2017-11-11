package mtgpricer.web.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;

@Configuration
@EnableWebMvc
public class WebMvcConfig extends WebMvcConfigurerAdapter {

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		super.addResourceHandlers(registry);
		registry.addResourceHandler("**.html").addResourceLocations("/");
		registry.addResourceHandler("/css/*").addResourceLocations("/css/");
		registry.addResourceHandler("/js/*").addResourceLocations("/js/");
	}
	
	@Bean
	public FreeMarkerConfigurer freeMarkerConfigurer() {
		final Properties settings = new Properties();
		settings.setProperty("url_escaping_charset", "UTF-8");
		
		final Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("_auth", new AuthVariable());
		
		final FreeMarkerConfigurer configurer = new FreeMarkerConfigurer();
		configurer.setTemplateLoaderPath("/WEB-INF/freemarker/");
		configurer.setDefaultEncoding("UTF-8");
		configurer.setFreemarkerSettings(settings);
		configurer.setFreemarkerVariables(variables);
		return configurer;
	}
	
	@Bean
	public FreeMarkerViewResolver freeMarkerViewResolver() {
		final FreeMarkerViewResolver resolver = new FreeMarkerViewResolver();
		resolver.setPrefix("");
		resolver.setSuffix(".ftl");
		resolver.setContentType("text/html; charset=UTF-8");
		return resolver;
	}
	
	/**
	 * Variables for Freemarker templates for authentication information. This can be found using "_auth".
	 * @author jared.pearson
	 */
	public static class AuthVariable {
		private static final GrantedAuthority ANONYMOUS = new SimpleGrantedAuthority("ROLE_ANONYMOUS");
		
		/**
		 * Determines if the user is authenticated.
		 */
		public boolean getIsAuthenticated() {
			return getAuth() != null && !getAuth().getAuthorities().contains(ANONYMOUS);
		}
		
		/**
		 * Gets the username of the of the authenticated user or null if not authenticated.
		 */
		public String getUsername() {
			return getIsAuthenticated() ? getAuth().getName() : null;
		}
		
		private Authentication getAuth() {
			return SecurityContextHolder.getContext().getAuthentication();
		}
	}
}
