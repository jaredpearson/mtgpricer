package mtgpricer.catalog;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

import com.google.gson.Gson;

import mtgpricer.Resource;

/**
 * Loads the card information from a local AllSets-x.json file downloaded from MtgJson.com. 
 * @author jared.pearson
 */
class MtgJsonAllSetsFileLoader {
	private final Gson gson;
	
	public MtgJsonAllSetsFileLoader(Gson gson) {
		assert gson != null;
		this.gson = gson;
	}
	
	/**
	 * Loads the card catalog from the specified resource.
	 * @return the catalog instance from the given resource.
	 * @throws IOException
	 */
	public Collection<CardSetInfo> loadFromResource(Resource resource) throws IOException {
		assert resource != null;
		final Map<String, CardSetInfo> cardSetByCode = loadCardSetFromCatalogFile(resource);
		return cardSetByCode.values();
	}
	
	private Map<String, CardSetInfo> loadCardSetFromCatalogFile(Resource resource) throws IOException {
		final Map<String, CardSetInfo> cardSetByCode;
		final Type type = new com.google.gson.reflect.TypeToken<Map<String, CardSetInfo>>() {}.getType();
		final Reader reader = resource.getReader();
		try {
			cardSetByCode = gson.fromJson(reader, type);
		} finally {
			reader.close();
		}
		return cardSetByCode;
	}
}