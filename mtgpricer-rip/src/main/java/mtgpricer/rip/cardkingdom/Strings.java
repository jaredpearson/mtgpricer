package mtgpricer.rip.cardkingdom;

/**
 * Utilities for handling String
 * @author jared.pearson
 */
class Strings {
	public static String nullToEmpty(String value) {
		if (value == null) {
			return "";
		} else {
			return value;
		}
	}
}