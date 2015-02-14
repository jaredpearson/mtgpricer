package mtgpricer;

import java.io.PrintStream;

/**
 * Factory for creating displays
 * @author jared.pearson
 */
public class Displays {
	
	private Displays() {
	}
	
	/**
	 * Creates a display that writes directly to the given {@link PrintStream}
	 */
	public static Display createForPrintStream(final PrintStream printStream) {
		assert printStream != null;
		return new Display() {
			public void writeln(String value) {
				printStream.println(value);
				printStream.flush();
			}
		};
	}
}