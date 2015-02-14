package mtgpricer.catalog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

/**
 * Implementation of the {@link CardImageService} that retrieves the images from mtgimage.com.
 * @author jared.pearson
 */
public class CardImageServiceImpl implements CardImageService {
	private final static Logger logger = Logger.getLogger(CardImageServiceImpl.class.getName());
	private final File imageCacheDir;
	
	public CardImageServiceImpl(File imageCacheDir) {
		assert imageCacheDir != null;
		this.imageCacheDir = imageCacheDir;
	}

	public CardImage getCardImage(Card card) {
		assert card != null;
		
		if (card.getImageName() == null) {
			return null;
		}
		
		if (!imageCacheDir.exists()) {
			imageCacheDir.mkdirs();
		}
		
		final File cardSetDir = new File(imageCacheDir, card.getSetCode());
		if (!cardSetDir.exists()) {
			cardSetDir.mkdirs();
		}
		
		final File cardImageFile = new File(cardSetDir, card.getImageName() + ".hq.jpg");
		if (cardImageFile.exists()) {
			return new FileCardImage(cardImageFile);
		}
		
		try {
			final FileOutputStream outputStream = new FileOutputStream(cardImageFile);
			try {
				final boolean downloaded = writeCardImageFromHttp(card, outputStream);
				if (downloaded) {
					return new FileCardImage(cardImageFile);
				} else {
					return null;
				}
			} finally {
				outputStream.close();
			}
		} catch (IOException exc) {
			throw new RuntimeException("Unable to get card image", exc);
		}
	}
	
	private boolean writeCardImageFromHttp(Card card, OutputStream outputStream) {
		assert card != null;
		
		final String url;
		try {
			url = createImageUrl(card);
		} catch(IOException exc) {
			throw new RuntimeException("Error occurred while trying to build url for card", exc);
		}
		
		if (url == null) {
			logger.info("Card does not have a image on HTTP: " + card.toString());
			return false;
		}
		
		try {
			final CloseableHttpClient client = HttpClients.createDefault();
			try {
				final HttpGet get = new HttpGet(url);
				final CloseableHttpResponse response = client.execute(get);
				try {
					if (response.getStatusLine().getStatusCode() != 200) {
						logger.finest("Response code for " + url + ": " + response.getStatusLine());
						logger.info("Image not found for card: " + card.toString());
						return false;
					} else {
						response.getEntity().writeTo(outputStream);
					}
				} finally {
					response.close();
				}
			} finally {
				client.close();
			}
		} catch(IOException exc) {
			throw new RuntimeException("Error occurred while trying to retrieve card image from " + url, exc);
		}
		return true;
	}
	
	private static String createImageUrl(Card card) throws UnsupportedEncodingException {
		assert card != null;
		if (card.getImageName() == null) {
			return null;
		}
		
		final String imageName = URLEncoder.encode(card.getImageName(), "UTF-8").replace("+", "%20");
		final String setCode = URLEncoder.encode(card.getSetCode(), "UTF-8");
		final String uri = "http://mtgimage.com/set/" + setCode + "/" + imageName + ".hq.jpg";
		return uri;
	}
	
	private static class FileCardImage implements CardImage {
		private final File imageFile;
		
		public FileCardImage(File imageFile) {
			assert imageFile != null;
			this.imageFile = imageFile;
		}
		
		public long getFileSize() {
			return imageFile.length();
		}
		
		public long getLastModified() {
			return imageFile.lastModified();
		}
		
		public void writeTo(OutputStream output) throws IOException {
			final FileInputStream inputStream = new FileInputStream(imageFile);
			try {
				IOUtils.copy(inputStream, output);
			} finally {
				inputStream.close();
			}
		}
	}
}
