package mtgpricer.rip;

import java.io.File;

import mtgpricer.ConfigPropertyUtils;
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

@Configuration
@ComponentScan
public class RipConfig {
	
	@Autowired
	UtilConfig utilConfig;
	
	@Value("${rip.cardKingdom.parserRulesFilePath}")
	String cardKingdomParserRulesFilePath;
	
	@Value("${rip.outputDir}")
	String outputDir;
	
	@Autowired
	PageRequesterFactory pageRequesterFactory;
	
	@Autowired
	CardCatalogService cardCatalogService;
	
	@Bean
	@Lazy
	public File ripOutputDir() {
		return ConfigPropertyUtils.createFile("rip.outputDir", outputDir, false);
	}
	
	@Bean
	@Lazy
	public mtgpricer.rip.Main ripMain() {
		return new mtgpricer.rip.Main(ripProcessor());
	}
	
	@Bean
	@Lazy
	public UpdateRipFileTool updateRipFileTool() {
		return new UpdateRipFileTool();
	}
	
	@Bean
	@Lazy
	public CardKingdomSiteFactory cardKingdomSiteFactory() {
		return new CardKingdomSiteFactory(cardKingdomSiteParserRulesFactory());
	}
	
	@Bean
	@Lazy
	public CardKingdomSiteParserRulesFactory cardKingdomSiteParserRulesFactory() {
		final File parserRulesFile = ConfigPropertyUtils.createFile("catalog.catalogFilePath", cardKingdomParserRulesFilePath);
		return new CardKingdomSiteParserRulesFactory(parserRulesFile, utilConfig.standardGson());
	}

	@Bean
	@Lazy
	public PriceDataLoader priceDataLoader() {
		return new FilePriceDataStore(ripOutputDir(), utilConfig.standardGson());
	}
	
	@Bean
	@Lazy
	public PriceDataStore priceDataStore() {
		return (FilePriceDataStore) priceDataLoader();
	}
	
	@Bean
	@Lazy
	public RipProcessor ripProcessor() {
		return new RipProcessor(
				pageRequesterFactory, 
				cardCatalogService, 
				cardKingdomSiteFactory(), 
				priceDataStore());
	}
}