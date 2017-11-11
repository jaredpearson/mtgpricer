package mtgpricer.rip.cardkingdom;

/**
 * Factory for creating {@link SiteParserRules} instances
 * @author jared.pearson
 */
public interface SiteParserRulesFactory {
	/**
	 * Loads a {@link SiteParserRules} instance.
	 */
	SiteParserRules loadSiteParserRules();
	
	/**
	 * Saves the {@link SiteParserRules} instance.
	 */
	void saveSiteParserRules(SiteParserRules siteParserRules);
}
