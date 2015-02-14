package mtgpricer.catalog;

import java.io.IOException;
import java.io.OutputStream;

/**
 * The image printed on a card.
 * @author jared.pearson
 */
public interface CardImage {
	/**
	 * Gets the size of the card image file.
	 */
	public long getFileSize();
	
	/**
	 * Gets the time the card image file was last modified.
	 */
	public long getLastModified();
	
	/**
	 * Writes the card image to the output stream.
	 */
	public void writeTo(OutputStream output) throws IOException;
}