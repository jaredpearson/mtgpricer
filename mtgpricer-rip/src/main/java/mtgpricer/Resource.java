package mtgpricer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Represents resource that can be read. This encapsulates a file or a resource within the
 * classpath.
 * @author jared.pearson
 */
public abstract class Resource {
	public abstract Reader getReader() throws IOException;
	
	public static Resource createForFile(File file) {
		return new FileResource(file);
	}
	
	public static Resource createForClasspathResource(String resource) {
		return new ClasspathResource(resource);
	}
	
	private static class ClasspathResource extends Resource {
		private final String name;
		
		public ClasspathResource(String name) {
			assert name != null;
			this.name = name;
		}
		
		@Override
		public Reader getReader() throws IOException {
			final InputStream inputStream = getClass().getResourceAsStream(this.name);
			return new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
		}
	}
	
	private static class FileResource extends Resource {
		private final File file;
		
		public FileResource(File file) {
			assert file != null;
			this.file = file;
		}
		
		@Override
		public Reader getReader() throws IOException {
			return new InputStreamReader(new FileInputStream(this.file), "UTF-8");
		}
	}
}
