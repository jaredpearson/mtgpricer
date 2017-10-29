package mtgpricer.rip.cardkingdom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Builder for {@link CardParserRules} instances
 * @author jared.pearson
 */
class CardParserRulesBuilder {
	private List<String> unknownNames = new ArrayList<>();
	private Set<String> ignoredNames = new HashSet<>();
	private Map<String, String> nameOverrides = new HashMap<>();
	private Map<String, String> numberOverrides = new HashMap<>();
	private Map<String, Integer> multiverseOverrides = new HashMap<>();

	public void setUnknownNames(List<String> unknownNames) {
		this.unknownNames = unknownNames;
	}
	
	public void setIgnoredNames(Set<String> ignoredNames) {
		this.ignoredNames = ignoredNames;
	}
	
	public void setNameOverrides(Map<String, String> nameOverrides) {
		this.nameOverrides = nameOverrides;
	}
	
	public void setNumberOverrides(Map<String, String> numberOverrides) {
		this.numberOverrides = numberOverrides;
	}

	public void setMultiverseOverrides(Map<String, Integer> multiverseOverrides) {
		this.multiverseOverrides = multiverseOverrides;
	}
	
	public CardParserRules build() {
		return new CardParserRules(
				this.numberOverrides,
				this.ignoredNames,
				this.nameOverrides,
				this.multiverseOverrides);
	}
}