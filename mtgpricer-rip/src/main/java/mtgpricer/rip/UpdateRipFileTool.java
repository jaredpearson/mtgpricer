package mtgpricer.rip;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import mtgpricer.CommandLineTool;
import mtgpricer.CommandLineTools;
import mtgpricer.catalog.Card;
import mtgpricer.catalog.CardCatalogProvider;
import mtgpricer.catalog.CardSet;
import mtgpricer.rip.cardkingdom.CardKingdomSiteParserRulesFactory;
import mtgpricer.rip.cardkingdom.CardParserRules;
import mtgpricer.rip.cardkingdom.SiteParserRules;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Processes each of the files in the output directory that have been ripped earlier. This allows
 * for the "upgrading" a file from an old schema to a new schema.
 * @author jared.pearson
 */
public class UpdateRipFileTool implements CommandLineTool {
	private CardCatalogProvider cardCatalogProvider;
	private CardKingdomSiteParserRulesFactory cardKingdomSiteParserRulesFactory;
	private File outputDir;
	private Gson gson;
	private Supplier<GsonBuilder> gsonBuilderSupplier;
	
	@Autowired
	public UpdateRipFileTool(Supplier<GsonBuilder> gsonBuilderSupplier) {
		this.gsonBuilderSupplier = gsonBuilderSupplier;
		this.gson = gsonBuilderSupplier.get().create();
	}
	
	@Autowired
	public void setCardCatalogProvider(CardCatalogProvider cardCatalogProvider) {
		this.cardCatalogProvider = cardCatalogProvider;
	}
	
	@Autowired
	public void setCardKingdomSiteParserRulesFactory(CardKingdomSiteParserRulesFactory cardKingdomSiteParserRulesFactory) {
		this.cardKingdomSiteParserRulesFactory = cardKingdomSiteParserRulesFactory;
	}
	
	@Autowired
	@Qualifier("ripOutputDir")
	public void setOutputDir(File outputDir) {
		this.outputDir = outputDir;
	}
	
	@Override
	public void run(String[] args) throws Exception {
		if (outputDir == null) {
			throw new IllegalStateException("outputDir must be set before invoking run");
		}
		
		final FilePriceDataStore priceDataLoader = new FilePriceDataStore(outputDir, gsonBuilderSupplier);
		for (final File file : priceDataLoader.getDataFiles()) {
			final JsonObject priceSiteJson = loadFile(file);
			if (processCardSet(priceSiteJson, file.getName())) {
				writeFile(file, priceSiteJson);
				System.out.println("Successfully updated " + file.getAbsolutePath());
			} else {
				System.out.println("No changes made to " + file.getAbsolutePath());
			}
		}
	}
	
	public static void main(String[] args) throws Exception {
		CommandLineTools.run(UpdateRipFileTool.class, args);
	}
	
	/**
	 * @param priceSiteJson
	 * @param filename
	 * @return true when the card set has been changed
	 */
	private boolean processCardSet(final JsonObject priceSiteJson, final String filename) {
		boolean changed = false;
		final JsonArray cardSetsJson = priceSiteJson.get("cardSets").getAsJsonArray();
		for (final JsonElement cardSetJsonEl : cardSetsJson) {
			final JsonObject cardSetJson = cardSetJsonEl.getAsJsonObject();
			if (updateCardSetName(cardSetJson)) {
				changed = true;
			}
			
			if (updateCardSetCode(cardSetJson)) {
				changed = true;
			}
			
			final Set<String> cardsWithoutMultiverseId = new HashSet<String>();
			for (JsonElement cardJson : cardSetJson.get("cards").getAsJsonArray()) {
				final JsonObject cardJsonObject = cardJson.getAsJsonObject();
				
				if (processCard(cardSetJson, cardJsonObject)) {
					changed = true;
				}
				
				if (!cardJsonObject.has("multiverseId")) {
					final String name = cardJsonObject.has("name") ? cardJsonObject.get("name").getAsString() : cardJsonObject.get("rawName").getAsString();
					cardsWithoutMultiverseId.add(name);
				}
			}
			
			if (!cardsWithoutMultiverseId.isEmpty()) {
				final String cardSetCode = getAsStringOrNull(cardSetJson, "code");
				final String cardSetName = getAsStringOrNull(cardSetJson, "name");
				final String cardSetRawName = getAsStringOrNull(cardSetJson, "rawName");
				final String id = (cardSetCode != null) ? cardSetCode : (cardSetName != null) ? cardSetName : cardSetRawName;
				
				System.out.println("Cards without multiverse ID in " + id + ":");
				for (String value : cardsWithoutMultiverseId) {
					System.out.println("\t" + value);
				}
			}
		}
		return changed;
	}
	
	private boolean processCard(final JsonObject cardSetJson, final JsonObject cardJson) {
		final CardSet cardSet = findCardSetByName(cardSetJson);
		if (cardSet == null) {
			return false;
		}
		
		// since some of the changes to the cardJson object affect other changes, we
		// reapply the changes 
		boolean anyChanges = false;
		boolean changesThisRound = false;
		
		do {
			changesThisRound = false;
		
			if (updateCardName(cardSet, cardJson)) {
				changesThisRound = true;
			}
			
			if (updateCard(cardSet, cardJson)) {
				changesThisRound = true;
			}
			
			if (updateCardPrice(cardSet, cardJson)) {
				changesThisRound = true;
			}
			
			if (changesThisRound) {
				anyChanges = true;
			}
		
		} while(changesThisRound);
		
		return anyChanges;
	}
	
	private SiteParserRules siteParserRules;
	private SiteParserRules getSiteParserRules() {
		if (siteParserRules == null) {
			siteParserRules = cardKingdomSiteParserRulesFactory.loadSiteParserRules();
		}
		return siteParserRules;
	}
	
	private String getAsStringOrNull(JsonObject json, String name) {
		return (json.has(name)) ? json.get(name).getAsString() : null;
	}
	
	private boolean updateCardSetName(final JsonObject cardSetJson) {
		if (cardSetJson == null) {
			return false;
		}
		
		// get the card set's raw name and skip it if it doesn't have one
		final String rawName = getAsStringOrNull(cardSetJson, "rawName");
		if (rawName == null) {
			return false;
		}
		
		// see if there is a name override in the parser rules
		final String nameOverride = getSiteParserRules().getCardSetNameOverride(rawName);
		if (nameOverride == null) {
			return false;
		}
		
		if (!cardSetJson.has("name") || !nameOverride.equals(cardSetJson.get("name").getAsString())) {
			cardSetJson.addProperty("name", nameOverride);
			System.out.println("Set name for card set: " + nameOverride);
			return true;
		} else {
			return false;
		}
	}

	private boolean updateCardSetCode(final JsonObject cardSetJson) {
		// skip any card set that doesn't have a code
		if (cardSetJson.has("code")) {
			return false;
		}
		
		final CardSet cardSet = findCardSetByName(cardSetJson);
		if (cardSet == null) {
			return false;
		}
		
		return setCardSetJsonWithCardProps(cardSetJson, cardSet);
	}
	
	private String getCardSetNameOrRawName(final JsonObject cardSetJson) {
		if (cardSetJson.has("name")) {
			return cardSetJson.get("name").getAsString();
		}
		if (cardSetJson.has("rawName")) {
			return cardSetJson.get("rawName").getAsString();
		}
		return null;
	}
	
	private boolean setCardSetJsonWithCardProps(final JsonObject cardSetJson, final CardSet cardSet) {
		if (cardSet == null) {
			return false;
		}

		boolean changed = false;
		
		final String cardSetName = getAsStringOrNull(cardSetJson, "name");
		if (!cardSet.getName().equals(cardSetName)) {
			cardSetJson.addProperty("name", cardSet.getName());
			System.out.println(cardSet.getName() + " set name to " + cardSet.getName());
			changed = true;
		}
		
		final String cardSetCode = getAsStringOrNull(cardSetJson, "code");
		if (!cardSet.getCode().equals(cardSetCode)) {
			cardSetJson.addProperty("code", cardSet.getCode());
			System.out.println(cardSet.getName() + " set code to " + cardSet.getCode());
			changed = true;
		}
		
		return changed;
	}
	
	private boolean updateCard(final CardSet cardSet, final JsonObject cardJson) { 
		final CardParserRules parserRules = getSiteParserRules().getParserRuleForCardSetCode(cardSet.getCode());
		final Card card = attemptToFindCard(cardSet, parserRules, cardJson);
		if (card == null || card.getMultiverseId() == null) {
			return false;
		}
		
		return setCardJsonWithCardProps(cardJson, card);
	}
	
	private boolean updateCardName(final CardSet cardSet, final JsonObject cardJson) {
		if (cardJson == null) {
			return false;
		}
		
		if (cardJson.has("name") && !cardJson.has("rawName")) {
			cardJson.add("rawName", cardJson.get("name"));
		}
		
		final String rawName = getAsStringOrNull(cardJson, "rawName");
		if (rawName == null) {
			return false;
		}
		
		final CardParserRules parserRules = getSiteParserRules().getParserRuleForCardSetCode(cardSet.getCode()); 
		final String nameOverride = parserRules.getCardNameOverrideForName(rawName);
		if (nameOverride == null) {
			return false;
		}
		
		final String cardName = getAsStringOrNull(cardJson, "name");
		if (!nameOverride.equals(cardName)) {
			cardJson.addProperty("name", nameOverride);
			System.out.println("Set name for card: " + nameOverride);
			return true;
		} else {
			return false;
		}
	}
	
	private Card attemptToFindCard(final CardSet cardSet, final CardParserRules parserRule, final JsonObject cardJson) {
		if (!cardJson.has("rawName")) {
			return null;
		}
		final String rawCardName = cardJson.get("rawName").getAsString();
		return attemptToFindCard(cardSet, parserRule, rawCardName);
	}

	private Card attemptToFindCard(final CardSet cardSet, final CardParserRules parserRule, final String rawCardName) {
		Card card = null;
		
		// attempt to retrieve the card with the overridden multiverse ID
		if (card == null) {
			final Integer multiverseId = parserRule.getMultiverseIdForName(rawCardName);
			if (multiverseId != null) {
				card = cardSet.getCardWithMultiverseId(multiverseId);
				if (card == null) {
					System.out.println("Multiverse ID in card override is not found within set: " + rawCardName + " = " + multiverseId);
				}
			}
		}
		
		// if there is a card number override for the card, lookup the card from catalog using it instead
		if (card == null) {
			final String cardNumberOverride = parserRule.getCardNumberOverrideForName(rawCardName);
			if (cardNumberOverride != null) {
				card = cardSet.getCardWithNumber(cardNumberOverride);
				if (card == null) {
					System.out.println("Card number in card override is not found within set: " + rawCardName + " = " + cardNumberOverride);
				}
			}
		}
		
		// attempt to retrieve the card with the overridden name
		if (card == null) {
			final String cardNameOverride = parserRule.getCardNameOverrideForName(rawCardName);
			if (cardNameOverride != null) {
				card = cardSet.getCardWithName(cardNameOverride);
				if (card == null) {
					System.out.println("Card name in card override is not found within set: " + rawCardName + " = " + cardNameOverride);
				}
			}
		}

		// attempt to retrieve the card using the raw name
		if (card == null) {
			card = cardSet.getCardWithName(rawCardName);
		}
		
		return card;
	}
	
	/**
	 * Copies the properties from the specified card into the JSON object
	 */
	private boolean setCardJsonWithCardProps(final JsonObject cardJson, final Card card) {
		if (card == null) {
			return false;
		}
		boolean changed = false;

		final String name = getAsStringOrNull(cardJson, "name");
		if (card.getName() != null && !card.getName().equals(name)) {
			cardJson.addProperty("name", card.getName());
			System.out.println("Set name to " + card.getName());
			changed = true;
		}

		final Integer multiverseId = cardJson.has("multiverseId") ? cardJson.get("multiverseId").getAsInt() : null;
		if (card.getMultiverseId() != null && !card.getMultiverseId().equals(multiverseId)) {
			cardJson.addProperty("multiverseId", card.getMultiverseId());
			System.out.println("Set multiverseId to " + card.getMultiverseId());
			changed = true;
		}

		final String cardNumber = getAsStringOrNull(cardJson, "number");
		if (card.getNumber() != null && !card.getNumber().equals(cardNumber)) {
			cardJson.addProperty("number", card.getNumber());
			System.out.println("Set number to " + card.getNumber());
			changed = true;
		}
		
		return changed;
	}
	
	/**
	 * Gets the price value from the priceRaw value only if the price is not already set on the card. This
	 * method ensures that priceRaw is a double before using it for the price value.
	 * @return true when a change has been made to the card
	 */
	private boolean updateCardPrice(final CardSet cardSet, final JsonObject cardJson) {
		if (cardJson.has("price")) {
			return false;
		}
		
		final String priceRaw = getAsStringOrNull(cardJson, "priceRaw");
		if (priceRaw == null) {
			return false;
		}
		
		try {
			double doubleValue = Double.parseDouble(priceRaw);
			cardJson.addProperty("price", doubleValue);
			return true;
		} catch (NumberFormatException exc) {
			System.out.println("Unable to convert priceRaw to double for " + cardJson.get("name").getAsString() + ": " + priceRaw);
			return false;
		}
	}
	
	private CardSet findCardSetByName(final JsonObject cardSetJson) {
		final String name = getCardSetNameOrRawName(cardSetJson);
		if (name == null) {
			return null;
		}
		
		final CardSet cardSet = cardCatalogProvider.getCardCatalog().getCardSetByName(name);
		if (cardSet == null) {
			System.out.println(name + " is not found");
			return null;
		}
		return cardSet;
	}

	private JsonObject loadFile(final File file) throws FileNotFoundException, IOException {
		final FileReader fileReader = new FileReader(file);
		try {
			return gson.fromJson(fileReader, JsonElement.class).getAsJsonObject();
		} finally {
			fileReader.close();
		}
	}
	
	private void writeFile(final File file, final JsonElement priceSiteJson) throws FileNotFoundException, IOException {
		if (file.exists()) {
			
			// find a file name that isn't already taken
			int index = 0;
			File backupFile = null;
			do {
				backupFile = new File(file.getAbsolutePath() + ".bak" + (index > 0 ? index : ""));
				index++;
			} while(backupFile.exists());
			
			file.renameTo(backupFile);
		}
		final FileWriter fileWriter = new FileWriter(file);
		try {
			gson.toJson(priceSiteJson, fileWriter);
		} finally {
			fileWriter.close();
		}
	}
}
