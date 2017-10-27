package mtgpricer.rip.cardkingdom;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;

import mtgpricer.Resource;

/**
 * Parser rules factory for the CardKingdom site
 * @author jared.pearson
 */
public class CardKingdomSiteParserRulesFactory implements SiteParserRulesFactory {
	private final Resource parserRulesResource;
	private final Gson gson;
	
	public CardKingdomSiteParserRulesFactory(final Resource parserRulesResource, final Gson gson) {
		assert parserRulesResource != null;
		assert gson != null;
		this.parserRulesResource = parserRulesResource;
		this.gson = gson;
	}
	
	/**
	 * Loads the site parser rules for the CardKingdom site
	 */
	@Override
	public SiteParserRules loadSiteParserRules() {
		final Map<String, CardParserRules> setCodeToCardParserRules;
		try {
			setCodeToCardParserRules = new CardParserRulesFileLoader(gson).loadFromResource(parserRulesResource);
		} catch(IOException exc) {
			throw new RuntimeException(exc);
		}
		
		// Card Kingdom has these listed as sets but they aren't in the catalog, so they need to be ignored
		final Set<String> cardSetsToIgnore = new HashSet<String>();
		cardSetsToIgnore.add("Deck Builder's Toolkit"); // not really a set
		cardSetsToIgnore.add("Collectors Ed"); // not sure why these are not in the catalog
		cardSetsToIgnore.add("Collectors Ed Intl");
		cardSetsToIgnore.add("Promotional"); // needs some work to split this up into the proper sets
		
		// Card Kingdom has different names for these than the catalog
		final Map<String, String> cardSetNameToCatalogName = new HashMap<String, String>();
		cardSetNameToCatalogName.put("3rd Edition", "Revised Edition");
		cardSetNameToCatalogName.put("4th Edition", "Fourth Edition");
		cardSetNameToCatalogName.put("5th Edition", "Fifth Edition");
		cardSetNameToCatalogName.put("6th Edition", "Classic Sixth Edition");
		cardSetNameToCatalogName.put("7th Edition", "Seventh Edition");
		cardSetNameToCatalogName.put("8th Edition", "Eighth Edition");
		cardSetNameToCatalogName.put("9th Edition", "Ninth Edition");
		cardSetNameToCatalogName.put("10th Edition", "Tenth Edition");
		cardSetNameToCatalogName.put("2010 Core Set", "Magic 2010");
		cardSetNameToCatalogName.put("2011 Core Set", "Magic 2011");
		cardSetNameToCatalogName.put("2012 Core Set", "Magic 2012");
		cardSetNameToCatalogName.put("2013 Core Set", "Magic 2013");
		cardSetNameToCatalogName.put("2014 Core Set", "Magic 2014 Core Set");
		cardSetNameToCatalogName.put("2015 Core Set", "Magic 2015 Core Set");
		cardSetNameToCatalogName.put("Alpha", "Limited Edition Alpha");
		cardSetNameToCatalogName.put("Battle Royale", "Battle Royale Box Set");
		cardSetNameToCatalogName.put("Beatdown", "Beatdown Box Set");
		cardSetNameToCatalogName.put("Beta", "Limited Edition Beta");
		cardSetNameToCatalogName.put("Commander", "Magic: The Gathering-Commander");
		cardSetNameToCatalogName.put("Commander 2013", "Commander 2013 Edition");
		cardSetNameToCatalogName.put("Conspiracy", "Magic: The Gathering—Conspiracy");
		cardSetNameToCatalogName.put("Deckmaster", "Deckmasters");
		cardSetNameToCatalogName.put("Duel Decks: Ajani Vs. Nicol Bolas", "Duel Decks: Ajani vs. Nicol Bolas");
		cardSetNameToCatalogName.put("Duel Decks: Divine Vs. Demonic", "Duel Decks: Divine vs. Demonic");
		cardSetNameToCatalogName.put("Duel Decks: Elspeth Vs. Tezzeret", "Duel Decks: Elspeth vs. Tezzeret");
		cardSetNameToCatalogName.put("Duel Decks: Elves Vs. Goblins", "Duel Decks: Elves vs. Goblins");
		cardSetNameToCatalogName.put("Duel Decks: Heroes Vs. Monsters", "Duel Decks: Heroes vs. Monsters");
		cardSetNameToCatalogName.put("Duel Decks: Izzet Vs. Golgari", "Duel Decks: Izzet vs. Golgari");
		cardSetNameToCatalogName.put("Duel Decks: Jace Vs. Chandra", "Duel Decks: Jace vs. Chandra");
		cardSetNameToCatalogName.put("Duel Decks: Jace Vs. Vraska", "Duel Decks: Jace vs. Vraska");
		cardSetNameToCatalogName.put("Duel Decks: Knights Vs. Dragons", "Duel Decks: Knights vs. Dragons");
		cardSetNameToCatalogName.put("Duel Decks: Garruk Vs. Liliana", "Duel Decks: Garruk vs. Liliana");
		cardSetNameToCatalogName.put("Duel Decks: Phyrexia Vs. The Coalition", "Duel Decks: Phyrexia vs. the Coalition");
		cardSetNameToCatalogName.put("Duel Decks: Venser Vs. Koth", "Duel Decks: Venser vs. Koth");
		cardSetNameToCatalogName.put("Duel Decks: Sorin Vs. Tibalt", "Duel Decks: Sorin vs. Tibalt");
		cardSetNameToCatalogName.put("Duel Decks: Speed Vs. Cunning", "Duel Decks: Speed vs. Cunning");
		cardSetNameToCatalogName.put("Duel Decks: Elspeth Vs. Kiora", "Duel Decks: Elspeth vs. Kiora");
		cardSetNameToCatalogName.put("From the Vault: Annihilation", "From the Vault: Annihilation (2014)");
		cardSetNameToCatalogName.put("Modern Event Deck", "Modern Event Deck 2014");
		cardSetNameToCatalogName.put("Modern Event Deck*", "Modern Event Deck 2014");
		cardSetNameToCatalogName.put("Premium Deck Series: Fire & Lightning", "Premium Deck Series: Fire and Lightning");
		cardSetNameToCatalogName.put("Planechase 2012", "Planechase 2012 Edition");
		cardSetNameToCatalogName.put("Portal 3K", "Portal Three Kingdoms");
		cardSetNameToCatalogName.put("Portal II", "Portal Second Age");
		cardSetNameToCatalogName.put("Ravnica", "Ravnica: City of Guilds");
		cardSetNameToCatalogName.put("Timeshifted", "Time Spiral \"Timeshifted\"");
		cardSetNameToCatalogName.put("Unlimited", "Unlimited Edition");
		
		return new SiteParserRules(cardSetsToIgnore, cardSetNameToCatalogName, setCodeToCardParserRules);
	}
}