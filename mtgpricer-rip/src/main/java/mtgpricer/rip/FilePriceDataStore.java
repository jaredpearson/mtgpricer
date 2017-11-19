package mtgpricer.rip;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import mtgpricer.rip.Deserialization.DeserializationType;

/**
 * Manages the price data from JSON files within the data directory.
 * @author jared.pearson
 */
public class FilePriceDataStore implements PriceDataLoader, PriceDataStore {
	private static final Logger logger = Logger.getLogger(FilePriceDataStore.class.getName());
	private final File outputDir;
	private final Supplier<GsonBuilder> gsonBuilderSupplier;
	
	public FilePriceDataStore(File outputDir, Supplier<GsonBuilder> gsonBuilderSupplier) {
		assert outputDir != null;
		assert gsonBuilderSupplier != null;
		this.outputDir = outputDir;
		this.gsonBuilderSupplier = gsonBuilderSupplier;
	}
	
	@Override
	public Set<PriceSiteInfo> loadPriceData() {
		final Set<PriceSiteInfo> priceSites = new HashSet<>();
		for (final File file : getDataFiles()) {
			final PriceSiteInfo priceSite = loadPriceDataForSingleFile(file, this::loadPriceSiteInfoFromFileWithoutCards);
			if (priceSite != null) {
				priceSites.add(priceSite);
			}
		}
		return priceSites;
	}
	
	@Override
	public PriceSiteInfo loadPriceDataById(long priceSiteId) {
		final File outputFile = getPriceSiteFileFromId(priceSiteId);
		return this.loadPriceDataForSingleFile(outputFile, this::loadPriceSiteInfoFromFileWithoutCards);
	}

	@Override
	public List<CardPriceInfo> loadCardPriceInfos(final long priceSiteId, final String setCode) {
		assert setCode != null;
		final FilePriceSiteInfo fullPriceSiteInfo = loadFilePriceSiteInfoWithCards(priceSiteId);
		final FileCardSetPriceInfo fullCardSetPriceInfo = this.findCardSetPriceInfoByCode(fullPriceSiteInfo, setCode);
		if (fullCardSetPriceInfo == null) {
			return Collections.emptyList();
		}
		return fullCardSetPriceInfo.getCards();
	}
	
	@Override
	public Map<String, List<CardPriceInfo>> loadCardPriceInfos(final PriceSiteInfo priceSiteInfo) {
		assert priceSiteInfo != null;
		final FilePriceSiteInfo fullPriceSiteInfo = loadFilePriceSiteInfoWithCards(priceSiteInfo.getId());
		final Map<String, List<CardPriceInfo>> setCodeToCardPriceInfos = new HashMap<>();
		for (final FileCardSetPriceInfo cardSetPriceInfo : fullPriceSiteInfo.getCardSets()) {
			if (cardSetPriceInfo.getCode() == null) {
				continue;
			}
			
			setCodeToCardPriceInfos.put(cardSetPriceInfo.getCode(), new ArrayList<>(cardSetPriceInfo.getCards()));
		}
		return setCodeToCardPriceInfos;
	}
	
	@Override
	public CardPriceInfo loadCardPriceInfoByMultiverseId(long priceSiteId, int multiverseId) {
		final FilePriceSiteInfo fullPriceSiteInfo = loadFilePriceSiteInfoWithCards(priceSiteId);
		final List<CardPriceInfo> cardPriceInfos = new ArrayList<>();
		for (final FileCardSetPriceInfo cardSetPriceInfo : fullPriceSiteInfo.getCardSets()) {
			final List<CardPriceInfo> allCardPriceInfos = cardSetPriceInfo.getCards();
			if (allCardPriceInfos == null) {
				continue;
			}
			
			// we make the assumption that a multiverse ID can only be in one set so
			// if we found it in the set, we don't need to process the rest of the sets
			boolean foundInSet = false;
			for (final CardPriceInfo cardPriceInfo : allCardPriceInfos) {
				if (cardPriceInfo.getMultiverseId() != null &&
						multiverseId == cardPriceInfo.getMultiverseId()) {
					foundInSet = true;
					cardPriceInfos.add(cardPriceInfo);
				}
			}
			
			if (foundInSet) {
				break;
			}
		}
		
		// TODO: change the UI to show the different variants. return the first and ignore all of the others... :(
		return cardPriceInfos.isEmpty() ? null : cardPriceInfos.get(0);
	}

	/**
	 * Gets the list of data files found
	 */
	public File[] getDataFiles() {
		if (!outputDir.exists()) {
			throw new PriceDataNotFoundException("Price information does not exist.");
		}
		
		final File[] priceFiles = outputDir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".json");
			}
		});
		if (priceFiles.length == 0) {
			throw new PriceDataNotFoundException("Price information does not exist.");
		}
		return priceFiles;
	}

	@Override
	public long persist(PriceSiteInfo priceSiteInfo) throws IOException {
		assert priceSiteInfo != null;
		final FilePriceSiteInfo filePriceSiteInfo = (FilePriceSiteInfo) priceSiteInfo;
		
		// make sure the output directory exists
		outputDir.mkdirs();
		
		// for now, just use the epoch time for the ID. this will not be 
		// sufficient if there are ever multiples being retrieved at once due to possible
		// collisions.
		final long id = priceSiteInfo.getRetrieved().getTime();
		
		final PriceSiteInfo priceSiteInfoWithId = new FilePriceSiteInfoBuilder(filePriceSiteInfo)
				.setId(id)
				.build();
		
		// write the site to the output directory 
		final File outputFile = new File(outputDir, "cardkingdom-" + id + ".json");
		try (final FileWriter fileWriter = new FileWriter(outputFile)) {
			gsonBuilderSupplier.get().create().toJson(priceSiteInfoWithId, fileWriter);
		}
		return id;
	}

	private FilePriceSiteInfo loadFilePriceSiteInfoWithCards(long priceSiteId) {
		final File outputFile = getPriceSiteFileFromId(priceSiteId);
		final FilePriceSiteInfo fullPriceSiteInfo = this.loadPriceDataForSingleFile(outputFile, this::loadPriceSiteInfoFromFile);
		return fullPriceSiteInfo;
	}
	
	private FilePriceSiteInfo loadPriceDataForSingleFile(File file, Function<File, FilePriceSiteInfo> loadFunction) {
		assert file != null;
		try {
			final FilePriceSiteInfo priceSiteInfo = loadFunction.apply(file);
			if (priceSiteInfo == null) {
				throw new IllegalStateException("Loading the price site file returned null: " + file.getAbsolutePath());
			}
			
			final Date date = priceSiteInfo.getRetrieved();
			if (date == null) {
				logger.warning("No retrieved date found for " + file.getAbsolutePath());
				return null;
			}
			
			return priceSiteInfo;
		} catch (FileFormatException exc) {
			logger.log(Level.WARNING, "File format error in " + file.getAbsolutePath(), exc);
			return null;
		}
	}
	
	private FileCardSetPriceInfo findCardSetPriceInfoByCode(FilePriceSiteInfo priceSiteInfo, String cardSetCode) {
		if (cardSetCode == null) {
			return null;
		}
		for (final FileCardSetPriceInfo cardSetPriceInfo : priceSiteInfo.getCardSets()) {
			if (cardSetCode.equals(cardSetPriceInfo.getCode())) {
				return cardSetPriceInfo;
			}
		}
		return null;
	}
	
	private FilePriceSiteInfo loadPriceSiteInfoFromFile(final File file) {
		try (final FileReader fr = new FileReader(file);) {
			return gsonBuilderSupplier.get()
					.create()
					.fromJson(fr, FilePriceSiteInfo.class);
		} catch(JsonSyntaxException exc) {
			throw new FileFormatException(exc);
		} catch (IOException exc) {
			logger.log(Level.WARNING, "Failed to load " + file.getAbsolutePath(), exc);
			return null;
		}
	}

	private File getPriceSiteFileFromId(long priceSiteId) {
		final File outputFile = new File(outputDir, "cardkingdom-" + priceSiteId + ".json");
		if (!outputFile.exists()) {
			throw new PriceDataNotFoundException("Unable to find price data with ID: " + priceSiteId + ".\nFile not found: " + outputFile.getAbsolutePath());
		}
		return outputFile;
	}

	private FilePriceSiteInfo loadPriceSiteInfoFromFileWithoutCards(final File file) {
		try (final FileReader fr = new FileReader(file);) {
			return gsonBuilderSupplier.get()
					.addDeserializationExclusionStrategy(new DeserializationTypeExclusionStrategy())
					.create()
					.fromJson(fr, FilePriceSiteInfo.class);
		} catch(JsonSyntaxException exc) {
			throw new FileFormatException(exc);
		} catch (IOException exc) {
			logger.log(Level.WARNING, "Failed to load " + file.getAbsolutePath(), exc);
			return null;
		}
	}

	private static final class DeserializationTypeExclusionStrategy implements ExclusionStrategy {
		@Override
		public boolean shouldSkipClass(Class<?> clazz) {
			return false;
		}

		@Override
		public boolean shouldSkipField(FieldAttributes f) {
			return f.getAnnotation(Deserialization.class) != null &&
					DeserializationType.CARDS.equals(f.getAnnotation(Deserialization.class).only());
		}
	}
}