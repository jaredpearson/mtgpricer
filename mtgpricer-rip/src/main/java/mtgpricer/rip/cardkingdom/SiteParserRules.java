package mtgpricer.rip.cardkingdom;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Parser rules for when parsing
 * @author jared.pearson
 */
public class SiteParserRules {
	private final Set<String> cardSetNamesToIgnore;
	private final Map<String, String> cardSetNameOverride;
	private final Map<String, CardParserRules> setCodeToParserRule;
	
	public SiteParserRules(final Set<String> cardSetNamesToIgnore, final  Map<String, String> cardSetNameOverride, final Map<String, CardParserRules> setCodeToParserRule) {
		this.cardSetNamesToIgnore = (cardSetNamesToIgnore != null) ? cardSetNamesToIgnore : Collections.<String>emptySet();
		this.cardSetNameOverride = (cardSetNameOverride != null) ? cardSetNameOverride : Collections.<String, String>emptyMap();
		this.setCodeToParserRule = (setCodeToParserRule != null) ? setCodeToParserRule : Collections.<String, CardParserRules>emptyMap();
	}
	
	public boolean isCardSetNameIgnored(String name) {
		return this.cardSetNamesToIgnore.contains(name);
	}
	
	public String getCardSetNameOverride(String name) {
		return this.cardSetNameOverride.get(name);
	}

	/**
	 * Gets the parser rule for the given set code. If the set code is not found, then an empty parser rule is returned.
	 */
	public CardParserRules getParserRuleForCardSetCode(String setCode) {
		assert setCode != null;
		if (!setCodeToParserRule.containsKey(setCode)) {
			return CardParserRules.createEmpty();
		}
		return setCodeToParserRule.get(setCode);
	}
}
