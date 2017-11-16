package mtgpricer.rip.cardkingdom;

import java.util.Collections;
import java.util.List;

/**
 * Represents the index of all of the card set pages within a site
 * @author jared.pearson
 */
public class SiteIndex {
	private final List<SiteIndexCardSet> sets;
	
	public SiteIndex(List<SiteIndexCardSet> sets) {
		this.sets = Collections.unmodifiableList(sets);
	}
	
	public List<SiteIndexCardSet> getCardSets() {
		return sets;
	}
}