package mtgpricer.catalog;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Represents the catalog of cards
 * @author jared.pearson
 */
public class CardCatalog {
	private final Set<CardSet> cardSets;
	private final Map<String, CardSet> cardSetByName;
	private final Map<String, CardSet> cardSetByCode;
	
	public CardCatalog(
			final Collection<? extends CardSetInfo> cardSetInfos,
			final Map<String, Set<TournamentFormat>> setCodeToFormats) {
		assert cardSetInfos != null;
		final Set<CardSet> newCardSetsSet = new TreeSet<>();
		final Map<String, CardSet> cardSetByCodeCopy = new HashMap<>();
		final Map<String, CardSet> cardSetByName = new HashMap<>(cardSetByCodeCopy.size());
		
		for (final CardSetInfo cardSetInfo : cardSetInfos) {
			final String setCode = cardSetInfo.getCode();
			
			if (cardSetByCodeCopy.containsKey(setCode)) {
				throw new IllegalStateException("Multiple card sets found with same code: " + setCode);
			}
			
			final Set<TournamentFormat> validFormats;
			if (!setCodeToFormats.containsKey(setCode) || setCodeToFormats.get(setCode) == null) {
				validFormats = Collections.emptySet(); 
			} else {
				validFormats = setCodeToFormats.get(setCode);
			}

			// create the card set classes from the card set info classes
			final CardSet cardSet = new CardSet(cardSetInfo, validFormats);
			newCardSetsSet.add(cardSet);
			cardSetByCodeCopy.put(cardSet.getCode(), cardSet);
			cardSetByName.put(cardSet.getName(), cardSet);
		}
		
		this.cardSets = Collections.unmodifiableSet(newCardSetsSet);
		this.cardSetByCode = Collections.unmodifiableMap(cardSetByCodeCopy);
		this.cardSetByName = Collections.unmodifiableMap(cardSetByName);
	}

	public boolean containsCardSetWithName(String name) {
		return cardSetByName.containsKey(name);
	}

	public CardSet getCardSetByName(String name) {
		return cardSetByName.get(name);
	}

	public CardSet getCardSetByCode(String setCode) {
		return cardSetByCode.get(setCode);
	}
		
	public Set<CardSet> getCardSets() {
		return this.cardSets;
	}
	
	/**
	 * Gets the card with the specified multiverse ID. If the card is not found, then a null reference
	 * is returned.
	 */
	public Card getCardWithMultiverseId(int multiverseId) {
		final Map<Integer, Card> multiverseIdToCards = getCardsWithMultiverseIds(Arrays.asList(multiverseId));
		return multiverseIdToCards.get(multiverseId);
	}
	
	/**
	 * Gets the cards with the specified multiverse IDs. If a card is not found, it will not have a key in the
	 * returned map.
	 */
	public Map<Integer, Card> getCardsWithMultiverseIds(Collection<Integer> multiverseIds) {
		assert multiverseIds != null;
		
		final Map<Integer, Card> multiverseIdToCards = new HashMap<Integer, Card>(multiverseIds.size());
		for (Integer multiverseId : multiverseIds) {
			for (CardSet cardSet : this.cardSets) {
				final Card card = cardSet.getCardWithMultiverseId(multiverseId);
				if (card != null) {
					multiverseIdToCards.put(multiverseId, card);
					break;
				}
			}
		}
		return multiverseIdToCards;
	}
}