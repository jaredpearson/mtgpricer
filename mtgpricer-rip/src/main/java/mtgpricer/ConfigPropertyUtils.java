package mtgpricer;

import java.io.File;

import mtgpricer.catalog.CardCatalogService;

/**
 * Utilities for handling configuration properties
 * @author jared.pearson
 */
public class ConfigPropertyUtils {
	private ConfigPropertyUtils() {
	}
	
	/**
	 * Asserts that the value is not null or an empty string.
	 */
	public static void assertNotEmpty(final String propertyName, final String value) {
		if (value == null || value.trim().length() == 0) {
			throw new IllegalStateException("Missing configuration for property \"" + propertyName + "\"");
		}
	}
	
	/**
	 * Asserts that the file is not null and that it exists.
	 */
	public static void assertExists(final String propertyName, final File file) {
		if (file == null || !file.exists()) {
			throw new IllegalStateException("Configuration property \"" + propertyName + "\" references a file that does not exist.\n" + file.toString());
		}
	}
	
	/**
	 * Creates a {@link Resource} from the given property and value.
	 */
	public static Resource createResource(final String propertyName, final String value) {
		assertNotEmpty(propertyName, value);
		if (value.startsWith("classpath:")) {
			final String valueWithoutPrefix = value.substring(10);
			return Resource.createForClasspathResource(valueWithoutPrefix);
		} else {
			return Resource.createForFile(new File(value));
		}
	}

	/**
	 * Creates a {@link java.io.File} from the the given value
	 * @param exists
	 */
	public static File createFile(final String propertyName, final String value) {
		return createFile(propertyName, value, true);
	}
	
	/**
	 * Creates a {@link java.io.File} from the the given value
	 * @param assertExists makes sure that the file exists. if this is true and it doesn't exist then an 
	 * {@link IllegalStateException} is thrown.
	 */
	public static File createFile(final String propertyName, final String value, boolean assertExists) {
		assertNotEmpty(propertyName, value);
		
		final File file;
		if (value.startsWith("classpath:")) {
			file = new File(CardCatalogService.class.getResource(value.substring(10)).getFile());
		} else {
			file = new File(value);
		}
		
		if (assertExists) {
			assertExists(propertyName, file);
		}
		
		return file;
	}
}