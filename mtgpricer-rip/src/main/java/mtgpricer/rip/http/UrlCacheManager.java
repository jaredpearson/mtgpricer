package mtgpricer.rip.http;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * Manages cached URLs on disk
 * @author jared.pearson
 */
class UrlCacheManager implements Closeable {
	private static final Logger logger = Logger.getLogger(UrlCacheManager.class.getName());
	
	private CacheIndex cacheIndex;
	private final File cacheDir;
	private final File indexFile;
	private final Gson gson;
	
	public UrlCacheManager() {
		this(new File(".html_cache"), "index.json");
	}
	
	public UrlCacheManager(File cacheDir, String indexFileName) {
		this.cacheDir = cacheDir;
		this.indexFile = new File(cacheDir, indexFileName);
		this.gson = createGson();
	}
	
	/**
	 * Determines if the specified URL has been cached
	 */
	public boolean isCached(String url) throws IOException {
		assert url != null;
		
		if (!cacheDir.exists()) {
			return false;
		}

		final File cacheFile = getCacheFileForUrl(url);
		if (!cacheFile.exists()) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * Loads the text content of the URL from the cache
	 * @param url the URL to load
	 * @return the contents of the file from the cache or null if the file does not exist
	 */
	public String loadFromCache(String url) throws IOException {
		assert url != null;
		
		if (!cacheDir.exists()) {
			return null;
		}

		final File cacheFile = getCacheFileForUrl(url);
		if (!cacheFile.exists()) {
			return null;
		}
		
		logger.fine("Loading file from cache: " + cacheFile.getAbsolutePath());
		
		final StringBuilder sb = new StringBuilder();
		final FileReader fileReader = new FileReader(cacheFile);
		try {
			final BufferedReader bufferedReader = new BufferedReader(fileReader);
			try {
				String line;
				while ((line = bufferedReader.readLine()) != null) {
					sb.append(line);
					sb.append("\n");
				}
			} finally {
				bufferedReader.close();
			}
		} finally {
			fileReader.close();
		}
		return sb.toString();
	}

	/**
	 * Saves the specified text context to the cache.
	 * @param url the URL of the HTML
	 * @param html the HTML content to be cached
	 */
	public void saveToCache(String url, String html) throws IOException {
		assert url != null;
		
		ensureCacheDir();
		
		// save the file to the cache dir, writing over any existing files
		final File cacheFile = getCacheFileForUrl(url);
		if (!cacheFile.exists()) {
			logger.fine("Deleting existing file from cache: " + cacheFile.getAbsolutePath());
			cacheFile.delete();
		}
		try (final FileWriter fileWriter = new FileWriter(cacheFile)) {
			logger.fine("Writing file to cache: " + cacheFile.getAbsolutePath());
			fileWriter.write(html);
		}
		flushCacheIndexToFile();
	}
	
	public void close() throws IOException {
		if (cacheIndex != null) {
			this.flushCacheIndexToFile();
		}
	}

	private CacheIndex getCacheIndex() throws IOException {
		if (this.cacheIndex == null) {
			this.cacheIndex = loadCacheIndexFromFile(indexFile);
		}
		return this.cacheIndex;
	}
	
	private void ensureCacheDir() {
		if (!cacheDir.exists()) {
			cacheDir.mkdirs();
		}
	}
	
	private File getCacheFileForUrl(String url) throws IOException {
		final String filename = getCacheIndex().getFilenameForUrl(url);
		return new File(cacheDir, filename);
	}
	
	/**
	 * Loads the index file that holds all of the information about the files stored in the cache.
	 */
	private CacheIndex loadCacheIndexFromFile(File indexFile) throws IOException {
		if (!indexFile.exists()) {
			return new CacheIndex();
		}
		
		try (final FileReader fileReader = new FileReader(indexFile)) {
			return this.gson.fromJson(fileReader, CacheIndex.class);
		}
	}
	
	/**
	 * Writes the index file to the cache directory
	 */
	private void flushCacheIndexToFile() throws IOException {
		ensureCacheDir();
		
		try (final FileWriter fileWriter = new FileWriter(indexFile)) {
			this.gson.toJson(cacheIndex, fileWriter);
		}
	}
	
	private static Gson createGson() {
		final GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(CacheIndex.class, new CacheIndexSerializer());
		gsonBuilder.registerTypeAdapter(CacheIndex.class, new CacheIndexDeserializer());
		
		return gsonBuilder.create();
	}
	
	private static class CacheIndexSerializer implements JsonSerializer<CacheIndex> {
		public JsonElement serialize(CacheIndex src, Type typeOfSrc, JsonSerializationContext context) {
			final JsonArray jsonArray = new JsonArray();
			for (UrlEntry entry : src.getEntries()) {
				JsonObject jsonObject = new JsonObject();
				jsonObject.addProperty("url", entry.getUrl());
				jsonObject.addProperty("filename", entry.getFilename());
				jsonArray.add(jsonObject);
			}
			return jsonArray;
		}
	}
	
	private static class CacheIndexDeserializer implements JsonDeserializer<CacheIndex> {
		public CacheIndex deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			final CacheIndex cacheIndex = new CacheIndex();
			final JsonArray jsonArray = json.getAsJsonArray();
			for (JsonElement item : jsonArray) {
				final JsonObject jsonUrlEntry = item.getAsJsonObject();
				final String url = jsonUrlEntry.get("url").getAsString();
				final String filename = jsonUrlEntry.get("filename").getAsString();
				cacheIndex.add(new UrlEntry(url, filename));
			}
			return cacheIndex;
		}
	}
	
	/**
	 * Represents an index for the cache
	 * @author jared.pearson
	 */
	private static class CacheIndex {
		private final HashMap<String, UrlEntry> urlToEntry;
		private final TreeSet<UrlEntry> entries;
		
		public CacheIndex() {
			this.urlToEntry = new HashMap<String, UrlEntry>();
			this.entries = new TreeSet<UrlEntry>();
		}
		
		public String getFilenameForUrl(String url) {
			if (urlToEntry.containsKey(url)) {
				return urlToEntry.get(url).getFilename();
			} else {
				final String filename = UUID.randomUUID().toString();
				final UrlEntry urlEntry = new UrlEntry(url, filename);
				this.add(urlEntry);
				return filename;
			}
		}

		public void add(UrlEntry urlEntry) {
			assert urlEntry != null;
			entries.add(urlEntry);
			urlToEntry.put(urlEntry.getUrl(), urlEntry);
		}
		
		public Set<UrlEntry> getEntries() {
			return this.entries;
		}
	}
	
	/**
	 * Represents a URL entry
	 * @author jared.pearson
	 */
	private static class UrlEntry implements Comparable<UrlEntry> {
		private final String url;
		private final String filename;
		
		public UrlEntry(String url, String filename) {
			assert url != null;
			assert filename != null;
			this.url = url;
			this.filename = filename;
		}
		
		public String getFilename() {
			return filename;
		}
		
		public String getUrl() {
			return url;
		}
		
		public int compareTo(UrlEntry o) {
			if (o == null) {
				throw new IllegalStateException();
			}
			
			final int urlCompare = this.url.compareTo(o.url);
			if (urlCompare != 0) {
				return urlCompare;
			}
			return this.filename.compareTo(o.filename);
		}
	}
}