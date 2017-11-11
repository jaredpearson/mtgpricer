package mtgpricer.rip.cardkingdom;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Represents some special rules for when parsing a list of cards
 * @author jared.pearson
 */
public class CardParserRules {
	private final Map<String, String> numberOverrides;
	private final Set<String> ignoredNames;
	private final Map<String, String> nameOverrides;
	private final Map<String, Integer> multiverseOverrides;
	
	public CardParserRules(Map<String, String> numberOverride, Set<String> ignoredNames, Map<String, String> nameOverrides, Map<String, Integer> multiverseOverrides) {
		this.numberOverrides = createImmutableOrEmpty(numberOverride);
		this.ignoredNames = (ignoredNames != null) ? Collections.unmodifiableSet(ignoredNames) : Collections.<String>emptySet();
		this.nameOverrides = createImmutableOrEmpty(nameOverrides);
		this.multiverseOverrides = createImmutableOrEmpty(multiverseOverrides);
	}
	
	public boolean isIgnored(String cardName) {
		return ignoredNames.contains(cardName);
	}
	
	/**
	 * Gets the card number to use for the given card name. 
	 * <p>
	 * Some card names specified on the price website don't match to that of the name in the card catalog. This
	 * override allows for the card number to be specified for the card name given on the price website.
	 * @param name
	 * @return
	 */
	public String getCardNumberOverrideForName(String name) {
		return numberOverrides.get(name);
	}

	/**
	 * Gets a new card name to use for the given card name.
	 */
	public String getCardNameOverrideForName(String name) {
		return nameOverrides.get(name);
	}

	/**
	 * Gets the Multiverse ID for the given card name.
	 */
	public Integer getMultiverseIdForName(String name) {
		return multiverseOverrides.get(name);
	}
	
	/**
	 * Creates an empty parser rule. This should be used instead of null parser rule.
	 * @return a new empty parser rule instance
	 */
	public static CardParserRules createEmpty() {
		return new CardParserRules(null, null, null, null);
	}
	
	/**
	 * Creates the parser rules containing all of the parser rules from instances given in the args. The {@link CardParserRules}
	 * are not modified during this operation.
	 * @param cardParserRules the parser rules to merge together. The order of the merge is in ascending order (higher indexed rules get priority over lower indexed rules).
	 * @return a new {@link CardParserRules} instance that contains the rules from the given parser rules.
	 */
	public static CardParserRules merge(CardParserRules... cardParserRules) {
		final Map<String, String> numberOverrides = new TreeMap<>();
		final Set<String> ignoredNames = new TreeSet<>();
		final Map<String, String> nameOverrides = new TreeMap<>();
		final Map<String, Integer> multiverseOverrides = new TreeMap<>();
		
		for (final CardParserRules thisCardParserRules : cardParserRules) {
			numberOverrides.putAll(thisCardParserRules.numberOverrides);
			ignoredNames.addAll(thisCardParserRules.ignoredNames);
			nameOverrides.putAll(thisCardParserRules.nameOverrides);
			multiverseOverrides.putAll(thisCardParserRules.multiverseOverrides);
		}
		
		return new CardParserRules(numberOverrides, ignoredNames, nameOverrides, multiverseOverrides);
	}

	private static <T, S> Map<T, S> createImmutableOrEmpty(Map<T, ? extends S> map) {
		if (map == null) {
			return Collections.emptyMap();
		} else {
			return Collections.unmodifiableMap(map);
		}
	}
}