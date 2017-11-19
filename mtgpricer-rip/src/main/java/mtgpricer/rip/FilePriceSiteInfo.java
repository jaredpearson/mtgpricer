package mtgpricer.rip;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Gets the information from a site
 * @author jared.pearson
 */
public class FilePriceSiteInfo implements PriceSiteInfo {
	private final Long id;
	private final Date retrieved;
	private final String url;
	private final List<FileCardSetPriceInfo> cardSets;
	
	public FilePriceSiteInfo(
			Long id,
			String url,
			Date retrieved,
			List<? extends FileCardSetPriceInfo> cardSets) {
		this.id = id;
		this.url = url;
		this.retrieved = new Date(retrieved.getTime());
		this.cardSets = Collections.unmodifiableList(cardSets);
	}

	@Override
	public Long getId() {
		return this.id;
	}

	@Override
	public List<FileCardSetPriceInfo> getCardSets() {
		return cardSets;
	}
	
	@Override
	public Date getRetrieved() {
		return new Date(retrieved.getTime());
	}

	@Override
	public String getUrl() {
		return url;
	}
}