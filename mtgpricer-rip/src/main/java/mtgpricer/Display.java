package mtgpricer;

import java.io.IOException;

/**
 * Used for displaying information to the user
 * @author jared.pearson
 */
public interface Display {
	/**
	 * Writes the value to the display followed by new line character
	 */
	public void writeln(String value) throws IOException;
}