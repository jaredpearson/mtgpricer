package mtgpricer.rip.cardkingdom;

import java.io.IOException;
import java.io.Reader;

import com.google.gson.Gson;

import mtgpricer.Resource;

/**
 * Parser rules factory for the CardKingdom site
 * @author jared.pearson
 */
public class CardKingdomSiteParserRulesFactory implements SiteParserRulesFactory {
	private final Resource parserRulesResource;
	private final Gson gson;
	
	public CardKingdomSiteParserRulesFactory(final Resource parserRulesResource, final Gson gson) {
		assert parserRulesResource != null;
		assert gson != null;
		this.parserRulesResource = parserRulesResource;
		this.gson = gson;
	}
	
	/**
	 * Loads the site parser rules for the CardKingdom site
	 */
	@Override
	public SiteParserRules loadSiteParserRules() {
		try (Reader reader = this.parserRulesResource.getReader()) {
			return gson.fromJson(reader, SiteParserRulesBuilder.class).build();
		} catch (IOException exc) {
			throw new RuntimeException(exc);
		}
	}
}