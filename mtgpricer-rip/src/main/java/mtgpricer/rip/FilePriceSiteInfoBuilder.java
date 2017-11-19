package mtgpricer.rip;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Builder for {@link FilePriceSiteInfo} instances
 * @author jared.pearson
 */
public class FilePriceSiteInfoBuilder {
	private Long id;
	private Date retrieved;
	private String url;
	private List<FileCardSetPriceInfo> cardSets = new ArrayList<>();
	
	public FilePriceSiteInfoBuilder() {
	}
	
	/**
	 * Creates a builder that is a copy of the fields in the given instance.
	 */
	public FilePriceSiteInfoBuilder(FilePriceSiteInfo priceSiteInfo) {
		this.id = priceSiteInfo.getId();
		this.retrieved = priceSiteInfo.getRetrieved();
		this.url = priceSiteInfo.getUrl();
		this.cardSets = new ArrayList<>(priceSiteInfo.getCardSets());
	}
	
	public FilePriceSiteInfo build() {
		return new FilePriceSiteInfo(
				this.id,
				this.url,
				this.retrieved,
				this.cardSets);
	}
	
	public FilePriceSiteInfoBuilder setId(Long id) {
		this.id = id;
		return this;
	}
	
	public FilePriceSiteInfoBuilder setRetrieved(Date retrieved) {
		this.retrieved = retrieved;
		return this;
	}
	
	public FilePriceSiteInfoBuilder setUrl(String url) {
		this.url = url;
		return this;
	}
	
	public FilePriceSiteInfoBuilder setCardSets(List<FileCardSetPriceInfo> cardSets) {
		this.cardSets = cardSets;
		return this;
	}
}
