package mtgpricer.catalog;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;

import mtgpricer.Resource;

/**
 * Service for handling the card catalog
 * @author jared.pearson
 */
public class CardCatalogService {
	private final Gson gson;
	private final Resource catalogResource;
	private final Resource formatSetResource;
	
	public CardCatalogService(Gson gson, Resource catalogResource, Resource formatSetResource) {
		assert gson != null;
		assert catalogResource != null;
		assert formatSetResource != null;
		this.gson = gson;
		this.catalogResource = catalogResource;
		this.formatSetResource = formatSetResource;
	}
	
	/**
	 * Gets the catalog from the specified location.
	 * @return the loaded catalog
	 * @throws CatalogLoadFailureException when the catalog could not be loaded.
	 */
	public CardCatalog loadCardCatalog() {
		try {
			final Collection<CardSetInfo> cardSetInfos = new MtgJsonAllSetsFileLoader(gson).loadFromResource(catalogResource);
			final Map<String, Set<TournamentFormat>> setCodeToFormats = new FormatSetsFileLoader(gson).loadFromResource(formatSetResource);
			return new CardCatalog(cardSetInfos, setCodeToFormats);
		} catch (IOException exc) {
			throw new CatalogLoadFailureException(exc);
		}
	}
}
