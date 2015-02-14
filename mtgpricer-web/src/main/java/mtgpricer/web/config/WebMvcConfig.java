package mtgpricer.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
	}
	
	@Bean
	public FreeMarkerConfigurer freeMarkerConfigurer() {
		final FreeMarkerConfigurer configurer = new FreeMarkerConfigurer();
		configurer.setTemplateLoaderPath("/WEB-INF/freemarker/");
		configurer.setDefaultEncoding("UTF-8");
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
	
}
