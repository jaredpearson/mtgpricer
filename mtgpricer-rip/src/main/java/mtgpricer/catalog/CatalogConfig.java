package mtgpricer.catalog;

import java.io.File;

import mtgpricer.ConfigPropertyUtils;
import mtgpricer.Resource;
import mtgpricer.UtilConfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.google.gson.Gson;

@Configuration
@ComponentScan
public class CatalogConfig {
	
	@Autowired
	UtilConfig utilConfig;
	
	@Value("${catalog.catalogFilePath}")
	String catalogFilePath;
	
	@Value("${catalog.formatSetFilePath}")
	String formatSetFilePath;
	
	@Value("${catalog.imageCacheDir}")
	String imageCacheDir;
	
	@Bean
	@Lazy
	public mtgpricer.catalog.Main catalogMain() {
		return new mtgpricer.catalog.Main();
	}
	
	@Bean
	@Lazy
	public CardCatalogService cardCatalogService(Gson gson) {
		final Resource catalogResource = ConfigPropertyUtils.createResource("catalog.catalogFilePath", catalogFilePath);
		final Resource formatSetResource = ConfigPropertyUtils.createResource("catalog.formatSetFilePath", formatSetFilePath);
		return new CardCatalogService(gson, catalogResource, formatSetResource);
	}
	
	@Bean
	@Lazy
	public CardCatalogProvider cardCatalogProvider(CardCatalogService cardCatalogService) {
		return new CardCatalogProvider() {
			private CardCatalog cardCatalog = null;
			
			public CardCatalog getCardCatalog() {
				if (cardCatalog == null) {
					cardCatalog = cardCatalogService.loadCardCatalog();
				}
				return cardCatalog;
			}
		};
	}
	
	@Bean
	@Lazy 
	public CardImageService cardImageService() {
		final File imageCacheDirFile = ConfigPropertyUtils.createFile("catalog.imageCacheDir", imageCacheDir, false);
		return new CardImageServiceImpl(imageCacheDirFile);
	}
}