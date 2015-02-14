package mtgpricer.catalog;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

import com.google.gson.Gson;

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
	 * Loads the card catalog from the specified file.
	 * @return the catalog instance from the given file.
	 * @throws IOException
	 */
	public Collection<CardSetInfo> loadFromFile(File file) throws IOException {
		assert file != null;
		final Map<String, CardSetInfo> cardSetByCode = loadCardSetFromCatalogFile(file);
		return cardSetByCode.values();
	}
	
	private Map<String, CardSetInfo> loadCardSetFromCatalogFile(File file) throws IOException {
		final Map<String, CardSetInfo> cardSetByCode;
		final Type type = new com.google.gson.reflect.TypeToken<Map<String, CardSetInfo>>() {}.getType();
		final FileReader fr = new FileReader(file);
		try {
			cardSetByCode = gson.fromJson(fr, type);
		} finally {
			fr.close();
		}
		return cardSetByCode;
	}
}