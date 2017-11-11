package mtgpricer.rip.cardkingdom;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.google.gson.annotations.SerializedName;

/**
 * Parser rules for when parsing
 * @author jared.pearson
 */
public class SiteParserRules {
	@SerializedName("ignoredSets")
	private final Set<String> cardSetNamesToIgnore;
	
	@SerializedName("setNameOverrides")
	private final Map<String, String> cardSetNameOverride;
	
	@SerializedName("sets")
	private final Map<String, CardParserRules> setCodeToParserRule;
	
	public SiteParserRules(
			final Set<String> cardSetNamesToIgnore,
			final Map<String, String> cardSetNameOverride,
			final Map<String, CardParserRules> setCodeToParserRule) {
		this.cardSetNamesToIgnore = (cardSetNamesToIgnore != null) ? cardSetNamesToIgnore : Collections.emptySet();
		this.cardSetNameOverride = (cardSetNameOverride != null) ? cardSetNameOverride : Collections.emptyMap();
		this.setCodeToParserRule = (setCodeToParserRule != null) ? setCodeToParserRule : Collections.emptyMap();
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

	/**
	 * Merges the given parser rules into a new parser rules instance. The given parser rules
	 * are not modified during this operation.
	 * @param newParserRules the parser rules to merge together. The order of the merge is in ascending order (higher indexed rules get priority over lower indexed rules).
	 * @return a new {@link SiteParserRules} instance that contains the rules from the given parser rules.
	 */
	public static SiteParserRules merge(SiteParserRules... parserRules) {
		final Set<String> cardSetNamesToIgnore = new TreeSet<>();
		final Map<String, String> cardSetNameOverride = new TreeMap<>();
		final Map<String, CardParserRules> setCodeToParserRule = new TreeMap<>();
		
		for (final SiteParserRules thisSiteParserRules : parserRules) {
			cardSetNamesToIgnore.addAll(thisSiteParserRules.cardSetNamesToIgnore);
			cardSetNameOverride.putAll(thisSiteParserRules.cardSetNameOverride);
			
			// merging the CardParserRules for each card set
			for (final Map.Entry<String, CardParserRules> thisSetCodeToParserRuleEntry : thisSiteParserRules.setCodeToParserRule.entrySet()) {
				final String setCode = thisSetCodeToParserRuleEntry.getKey();
				
				if (!setCodeToParserRule.containsKey(setCode)) {
					setCodeToParserRule.put(setCode, thisSetCodeToParserRuleEntry.getValue());
				} else {
					final CardParserRules existingCardParserRules = setCodeToParserRule.get(setCode);
					setCodeToParserRule.put(setCode, CardParserRules.merge(existingCardParserRules, thisSetCodeToParserRuleEntry.getValue()));
				}
			}
		}
		
		return new SiteParserRules(cardSetNamesToIgnore, cardSetNameOverride, setCodeToParserRule);
	}
}
