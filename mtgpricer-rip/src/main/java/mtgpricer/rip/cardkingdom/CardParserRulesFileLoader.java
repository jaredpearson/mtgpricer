package mtgpricer.rip.cardkingdom;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Loads the card parser rules file
 * @author jared.pearson
 */
public class CardParserRulesFileLoader {
	private final Gson gson;
	
	public CardParserRulesFileLoader(Gson gson) {
		assert gson != null;
		this.gson = gson;
	}
	
	/**
	 * Loads the card set parser rules from the given file in the class path.
	 */
	public Map<String, CardParserRules> loadFromClasspath(String resource) throws IOException {
		final ClassLoader classLoader = CardParserRulesFileLoader.class.getClassLoader();
		final File file = new File(classLoader.getResource(resource).getFile());
		return loadFromFile(file);
	}

	/**
	 * Loads the card set parser rules from the given file.
	 */
	public Map<String, CardParserRules> loadFromFile(File file) throws IOException {
		final Type type = new TypeToken<Map<String, CardParserRules>>(){}.getType();
		final FileReader fileReader = new FileReader(file);
		try {
			return gson.fromJson(fileReader, type);
		} finally {
			fileReader.close();
		}
	}
}