package mtgpricer.rip.cardkingdom;

/**
 * Factory for creating {@link SiteParserRules} instances
 * @author jared.pearson
 */
public interface SiteParserRulesFactory {
	/**
	 * Loads a {@link SiteParserRules} instance.
	 */
	public SiteParserRules loadSiteParserRules();
}
