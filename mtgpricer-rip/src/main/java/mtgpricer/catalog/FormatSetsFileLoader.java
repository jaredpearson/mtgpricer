package mtgpricer.catalog;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Loads the tournament formats information from the given file.
 * @author jared.pearson
 */
class FormatSetsFileLoader {
	private final Logger logger = Logger.getLogger(FormatSetsFileLoader.class.getName());
	private final Gson gson;
	
	public FormatSetsFileLoader(Gson gson) {
		assert gson != null;
		this.gson = gson;
	}

	/**
	 * Loads the tournament formats information from the given file.
	 */
	public Map<String, Set<TournamentFormat>> loadFromFile(File file) throws IOException {
		assert file != null;
		
		final JsonObject rootObject;
		final FileReader fileReader = new FileReader(file);
		try {
			final JsonElement jsonElement = gson.fromJson(fileReader, JsonElement.class);
			rootObject = jsonElement.getAsJsonObject();
		} finally {
			fileReader.close();
		}
		
		final Map<String, Set<TournamentFormat>> setCodeToFormats = new HashMap<String, Set<TournamentFormat>>();
		for (final Map.Entry<String, JsonElement> entry : rootObject.entrySet()) {
			final TournamentFormat format;
			if ("standard".equals(entry.getKey())) {
				format = TournamentFormat.STANDARD;
			} else {
				logger.warning("Skipping unknown tournament format: " + entry.getKey());
				continue;
			}
			
			final JsonElement value = entry.getValue();
			if (value.isJsonArray()) {
				final JsonArray valueArray = value.getAsJsonArray(); 
				for (final JsonElement item : valueArray) {
					final String setCode = item.getAsString();
					
					if (!setCodeToFormats.containsKey(setCode)) {
						setCodeToFormats.put(setCode, new HashSet<TournamentFormat>(TournamentFormat.values().length));
					}
					
					setCodeToFormats.get(setCode).add(format);
				}
			}
		}
		return setCodeToFormats;
	}
}