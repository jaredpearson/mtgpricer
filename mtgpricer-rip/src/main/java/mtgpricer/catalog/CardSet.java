package mtgpricer.catalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents a card set from the catalog
 * @author jared.pearson
 */
public class CardSet implements Comparable<CardSet> {
	private final String code;
	private final String name;
	private final List<Card> cards;
	private final Map<String, Card> cardNumberToCard;
	private final Map<String, Card> cardNameToCard;
	private final Map<Integer, Card> multiverseIdToCard;
	private final Set<TournamentFormat> validFormats;
	
	CardSet(CardSetInfo cardSetInfo, Set<TournamentFormat> validFormats) {
		assert cardSetInfo != null;
		assert validFormats != null;
		final String setCode = cardSetInfo.getCode();
		
		this.code = setCode;
		this.name = cardSetInfo.getName();
		this.validFormats = Collections.unmodifiableSet(validFormats);
		
		final List<Card> cards = new ArrayList<Card>(cardSetInfo.getCards().size());
		final Map<String, Card> cardNumberToCard = new HashMap<String, Card>();
		final Map<String, Card> cardNameToCard = new HashMap<String, Card>();
		final Map<Integer, Card> multiverseIdToCard = new HashMap<Integer, Card>();
		for (final CardInfo cardInfo : cardSetInfo.getCards()) {
			final Card card = new Card(setCode, cardInfo, validFormats.contains(TournamentFormat.STANDARD));
			
			cards.add(card);
			
			if (card.getNumber() != null) {
				cardNumberToCard.put(card.getNumber(), card);
			}
			if (card.getName() != null) {
				cardNameToCard.put(card.getName(), card);
			}
			if (card.getMultiverseId() != null) {
				multiverseIdToCard.put(card.getMultiverseId(), card);
			}
		}
		
		this.cards = Collections.unmodifiableList(cards);
		this.cardNumberToCard = Collections.unmodifiableMap(cardNumberToCard);
		this.cardNameToCard = Collections.unmodifiableMap(cardNameToCard);
		this.multiverseIdToCard = Collections.unmodifiableMap(multiverseIdToCard);
	}
	
	/**
	 * Gets the code associated to the card set.
	 */
	public String getCode() {
		return code;
	}
	
	/**
	 * Gets the name of the card set.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Gets all of the cards associated to the card set.
	 */
	public List<Card> getCards() {
		return cards;
	}
	
	public int compareTo(CardSet o) {
		if (o == null) {
			return -1;
		}
		
		return this.code.compareTo(o.code);
	}

	/**
	 * Determines if the set is generally valid to be played in tournament format.
	 */
	public boolean isValidInStandardTournamentFormat() {
		return this.validFormats.contains(TournamentFormat.STANDARD);
	}
	
	public Card getCardWithNumber(String cardNumber) {
		return this.cardNumberToCard.get(cardNumber);
	}

	public Card getCardWithName(String cardName) {
		return this.cardNameToCard.get(cardName);
	}

	/**
	 * Gets the card with the specified multiverse ID. If no card corresponds to the 
	 * ID, then a null reference is returned.
	 */
	public Card getCardWithMultiverseId(Integer multiverseId) {
		if (multiverseId == null) {
			return null;
		}
		return this.multiverseIdToCard.get(multiverseId);
	}
}