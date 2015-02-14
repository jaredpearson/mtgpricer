package mtgpricer.catalog;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;

/**
 * Service for handling the card catalog
 * @author jared.pearson
 */
public class CardCatalogService {
	private final Gson gson;
	private final File catalogFile;
	private final File formatSetFile;
	
	public CardCatalogService(Gson gson, File catalogFile, File formatSetFile) {
		assert gson != null;
		assert catalogFile != null;
		assert formatSetFile != null;
		this.gson = gson;
		this.catalogFile = catalogFile;
		this.formatSetFile = formatSetFile;
	}
	
	/**
	 * Gets the catalog from the specified location.
	 * @return the loaded catalog
	 * @throws CatalogLoadFailureException when the catalog could not be loaded.
	 */
	public CardCatalog loadCardCatalog() {
		try {
			if (!catalogFile.exists()) {
				throw new IllegalStateException("Expected the card catalog file to be found at " + catalogFile.getAbsolutePath());
			}
			final Collection<CardSetInfo> cardSetInfos = new MtgJsonAllSetsFileLoader(gson).loadFromFile(catalogFile);
			final Map<String, Set<TournamentFormat>> setCodeToFormats = new FormatSetsFileLoader(gson).loadFromFile(formatSetFile);
			return new CardCatalog(cardSetInfos, setCodeToFormats);
		} catch (IOException exc) {
			throw new CatalogLoadFailureException(exc);
		}
	}
}
