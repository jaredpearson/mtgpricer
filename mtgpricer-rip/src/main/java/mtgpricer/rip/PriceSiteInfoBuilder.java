package mtgpricer.rip;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Builder for {@link PriceSiteInfo} instances
 * @author jared.pearson
 */
public class PriceSiteInfoBuilder {
	private Long id;
	private Date retrieved;
	private String url;
	private List<CardSetPriceInfo> cardSets = new ArrayList<>();
	
	public PriceSiteInfoBuilder() {
	}
	
	/**
	 * Creates a builder that is a copy of the fields in the given instance.
	 */
	public PriceSiteInfoBuilder(PriceSiteInfo priceSiteInfo) {
		this.id = priceSiteInfo.getId();
		this.retrieved = priceSiteInfo.getRetrieved();
		this.url = priceSiteInfo.getUrl();
		this.cardSets = new ArrayList<>(priceSiteInfo.getCardSets());
	}
	
	public PriceSiteInfo build() {
		return new PriceSiteInfo(
				this.id,
				this.url,
				this.retrieved,
				this.cardSets);
	}
	
	public PriceSiteInfoBuilder setId(Long id) {
		this.id = id;
		return this;
	}
	
	public PriceSiteInfoBuilder setRetrieved(Date retrieved) {
		this.retrieved = retrieved;
		return this;
	}
	
	public PriceSiteInfoBuilder setUrl(String url) {
		this.url = url;
		return this;
	}
	
	public PriceSiteInfoBuilder setCardSets(List<CardSetPriceInfo> cardSets) {
		this.cardSets = cardSets;
		return this;
	}
}
