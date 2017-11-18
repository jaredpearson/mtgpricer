package mtgpricer.rip;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

/**
 * Manages the price data from JSON files within the data directory.
 * @author jared.pearson
 */
public class FilePriceDataStore implements PriceDataLoader, PriceDataStore {
	private static final Logger logger = Logger.getLogger(FilePriceDataStore.class.getName());
	private final File outputDir;
	private final Gson gson;
	
	public FilePriceDataStore(File outputDir, Supplier<GsonBuilder> gsonBuilderSupplier) {
		assert outputDir != null;
		assert gsonBuilderSupplier != null;
		this.outputDir = outputDir;
		this.gson = gsonBuilderSupplier.get().create();
	}
	
	@Override
	public Set<PriceSiteInfo> loadPriceData() {
		final Set<PriceSiteInfo> priceSites = new HashSet<>();
		for (final File file : getDataFiles()) {
			final PriceSiteInfo priceSite = loadPriceDataForSingleFile(file);
			if (priceSite != null) {
				priceSites.add(priceSite);
			}
		}
		return priceSites;
	}
	
	@Override
	public PriceSiteInfo loadPriceDataById(long id) {
		final File outputFile = new File(outputDir, "cardkingdom-" + id + ".json");
		if (!outputFile.exists()) {
			throw new PriceDataNotFoundException("Unable to find price data with ID: " + id + ".\nFile not found: " + outputFile.getAbsolutePath());
		}
		return this.loadPriceDataForSingleFile(outputFile);
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
	
	private PriceSiteInfo loadPriceDataForSingleFile(File file) {
		assert file != null;
		try {
			final PriceSiteInfo priceSiteInfo = loadPriceSiteInfoFromFile(file);
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
		} catch (IOException exc) {
			logger.log(Level.WARNING, "Failed to load " + file.getAbsolutePath(), exc);
			return null;
		}
	}
	
	@Override
	public long persist(PriceSiteInfo priceSiteInfo) throws IOException {
		assert priceSiteInfo != null;
		
		// make sure the output directory exists
		outputDir.mkdirs();
		
		// for now, just use the epoch time for the ID. this will not be 
		// sufficient if there are ever multiples being retrieved at once due to possible
		// collisions.
		final long id = priceSiteInfo.getRetrieved().getTime();
		
		final PriceSiteInfo priceSiteInfoWithId = new PriceSiteInfoBuilder(priceSiteInfo)
				.setId(id)
				.build();
		
		// write the site to the output directory 
		final File outputFile = new File(outputDir, "cardkingdom-" + id + ".json");
		try (final FileWriter fileWriter = new FileWriter(outputFile)) {
			gson.toJson(priceSiteInfoWithId, fileWriter);
		}
		return id;
	}

	private PriceSiteInfo loadPriceSiteInfoFromFile(final File file) throws IOException {
		final FileReader fr = new FileReader(file);
		try {
			return gson.fromJson(fr, PriceSiteInfo.class);
		} catch(JsonSyntaxException exc) {
			throw new FileFormatException(exc);
		} finally {
			fr.close();
		}
	}
	
}