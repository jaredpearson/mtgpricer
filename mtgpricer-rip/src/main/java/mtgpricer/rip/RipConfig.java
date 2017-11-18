package mtgpricer.rip;

import java.io.File;
import java.util.function.Supplier;

import mtgpricer.ConfigPropertyUtils;
import mtgpricer.Resource;
import mtgpricer.UtilConfig;
import mtgpricer.catalog.CardCatalogService;
import mtgpricer.rip.cardkingdom.CardKingdomSiteFactory;
import mtgpricer.rip.cardkingdom.CardKingdomSiteParserRulesFactory;
import mtgpricer.rip.http.PageRequesterFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.google.gson.GsonBuilder;

@Configuration
@ComponentScan
public class RipConfig {
	
	@Autowired
	UtilConfig utilConfig;
	
	@Value("${rip.cardKingdom.parserRulesFilePath}")
	String cardKingdomParserRulesFilePath;
	
	@Value("${rip.outputDir}")
	String outputDir;
	
	@Bean
	@Lazy
	public File ripOutputDir() {
		return ConfigPropertyUtils.createFile("rip.outputDir", outputDir, false);
	}
	
	@Bean
	@Lazy
	public mtgpricer.rip.Main ripMain(RipProcessor ripProcessor, PageRequesterFactory pageRequesterFactory) {
		return new mtgpricer.rip.Main(ripProcessor, pageRequesterFactory);
	}
	
	@Bean
	@Lazy
	public UpdateRipFileTool updateRipFileTool(Supplier<GsonBuilder> gsonBuilderSupplier) {
		return new UpdateRipFileTool(gsonBuilderSupplier);
	}
	
	@Bean
	@Lazy
	public CardKingdomSiteFactory cardKingdomSiteFactory(CardKingdomSiteParserRulesFactory cardKingdomSiteParserRulesFactory) {
		return new CardKingdomSiteFactory(cardKingdomSiteParserRulesFactory);
	}
	
	@Bean
	@Lazy
	public CardKingdomSiteParserRulesFactory cardKingdomSiteParserRulesFactory(Supplier<GsonBuilder> gsonBuilderSupplier) {
		final Resource parserRulesResource = ConfigPropertyUtils.createResource("rip.cardKingdom.parserRulesFilePath", cardKingdomParserRulesFilePath);
		return new CardKingdomSiteParserRulesFactory(parserRulesResource, gsonBuilderSupplier);
	}

	@Bean
	@Lazy
	public PriceDataLoader priceDataLoader(Supplier<GsonBuilder> gsonBuilderSupplier) {
		return new FilePriceDataStore(ripOutputDir(), gsonBuilderSupplier);
	}
	
	@Bean
	@Lazy
	public PriceDataStore priceDataStore(PriceDataLoader priceDataLoader) {
		return (FilePriceDataStore) priceDataLoader;
	}
	
	@Bean
	@Lazy
	public RipProcessor ripProcessor(
			CardKingdomSiteFactory cardKingdomSiteFactory,
			PriceDataStore priceDataStore,
			PageRequesterFactory pageRequesterFactory,
			CardCatalogService cardCatalogService) {
		return new RipProcessor(
				pageRequesterFactory, 
				cardCatalogService, 
				cardKingdomSiteFactory, 
				priceDataStore);
	}
}