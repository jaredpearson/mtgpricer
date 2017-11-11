package mtgpricer.rip.cardkingdom;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Builder for {@link SiteParserRules} instances
 * @author jared.pearson
 */
public class SiteParserRulesBuilder {
	private TreeSet<String> ignoredSets = new TreeSet<>();
	private TreeMap<String, String> setNameOverrides = new TreeMap<>();
	private TreeMap<String, CardParserRulesBuilder> sets = new TreeMap<>();
	
	public void setIgnoredSets(Set<String> ignoredSets) {
		this.ignoredSets = new TreeSet<>(ignoredSets);
	}
	
	public void setSetNameOverrides(Map<String, String> setNameOverrides) {
		this.setNameOverrides = new TreeMap<>(setNameOverrides);
	}
	
	public void setSets(Map<String, CardParserRulesBuilder> sets) {
		this.sets = new TreeMap<>(sets);
	}
	
	public SiteParserRules build() {
		final Map<String, CardParserRules> setModelMap = new TreeMap<>();
		for (final Map.Entry<String, CardParserRulesBuilder> rulesBuilderEntry : sets.entrySet()) {
			setModelMap.put(rulesBuilderEntry.getKey(), rulesBuilderEntry.getValue().build());
		}
		return new SiteParserRules(
				this.ignoredSets,
				this.setNameOverrides,
				setModelMap);
	}
}