package mtgpricer.catalog;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import mtgpricer.Resource;

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
	 * Loads the tournament formats information from the given resource.
	 */
	public Map<String, Set<TournamentFormat>> loadFromResource(Resource resource) throws IOException {
		assert resource != null;
		
		final JsonObject rootObject;
		try (final Reader reader = resource.getReader()) {
			final JsonElement jsonElement = gson.fromJson(reader, JsonElement.class);
			rootObject = jsonElement.getAsJsonObject();
		}
		
		final Map<String, Set<TournamentFormat>> setCodeToFormats = new HashMap<>();
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
						setCodeToFormats.put(setCode, new HashSet<>(TournamentFormat.values().length));
					}
					
					setCodeToFormats.get(setCode).add(format);
				}
			}
		}
		return setCodeToFormats;
	}
}