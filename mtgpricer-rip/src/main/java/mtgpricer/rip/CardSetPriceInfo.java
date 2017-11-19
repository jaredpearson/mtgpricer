package mtgpricer.rip;

import java.util.Date;

/**
 * Represents the information about set of cards.
 * @author jared.pearson
 */
public interface CardSetPriceInfo {

	String getCode();
	String getName();
	String getRawName();
	String getUrl();
	Date getRetrieved();

}