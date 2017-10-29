package mtgpricer.rip.cardkingdom;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Builder for {@link SiteParserRules} instances
 * @author jared.pearson
 */
class SiteParserRulesBuilder {
	private Set<String> ignoredSets = new HashSet<>();
	private Map<String, String> setNameOverrides = new HashMap<>();
	private Map<String, CardParserRulesBuilder> sets = new HashMap<>();
	
	public void setIgnoredSets(Set<String> ignoredSets) {
		this.ignoredSets = ignoredSets;
	}
	
	public void setSetNameOverrides(Map<String, String> setNameOverrides) {
		this.setNameOverrides = setNameOverrides;
	}
	
	public void setSets(Map<String, CardParserRulesBuilder> sets) {
		this.sets = sets;
	}
	
	public SiteParserRules build() {
		final Map<String, CardParserRules> setModelMap = new HashMap<>();
		for (final Map.Entry<String, CardParserRulesBuilder> rulesBuilderEntry : sets.entrySet()) {
			setModelMap.put(rulesBuilderEntry.getKey(), rulesBuilderEntry.getValue().build());
		}
		return new SiteParserRules(
				this.ignoredSets,
				this.setNameOverrides,
				setModelMap);
	}
}