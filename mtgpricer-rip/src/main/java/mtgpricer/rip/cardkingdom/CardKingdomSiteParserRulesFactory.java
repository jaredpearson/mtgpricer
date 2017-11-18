package mtgpricer.rip.cardkingdom;

import java.io.IOException;
import java.io.Reader;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import mtgpricer.Resource;

/**
 * Parser rules factory for the CardKingdom site
 * @author jared.pearson
 */
public class CardKingdomSiteParserRulesFactory implements SiteParserRulesFactory {
	private final Resource parserRulesResource;
	private final Gson gson;
	
	private final Lock lock = new ReentrantLock();
	private SiteParserRules siteParserRules;
	
	public CardKingdomSiteParserRulesFactory(
			final Resource parserRulesResource,
			final Supplier<GsonBuilder> gsonBuilderSupplier) {
		assert parserRulesResource != null;
		assert gsonBuilderSupplier != null;
		this.parserRulesResource = parserRulesResource;
		this.gson = gsonBuilderSupplier.get().create();
	}
	
	/**
	 * Loads the site parser rules for the CardKingdom site
	 */
	@Override
	public SiteParserRules loadSiteParserRules() {
		if (this.siteParserRules == null) {
			try (Reader reader = this.parserRulesResource.getReader()) {
				try {
					lock.lock();
					this.siteParserRules = gson.fromJson(reader, SiteParserRulesBuilder.class).build();
				} finally {
					lock.unlock();
				}
			} catch (IOException exc) {
				throw new RuntimeException(exc);
			}
		}
		return this.siteParserRules;
	}
	
	@Override
	public void saveSiteParserRules(SiteParserRules siteParserRules) {
		assert siteParserRules != null;
		try {
			if (lock.tryLock(5,  TimeUnit.SECONDS)) {
				try {
					// TODO write this to disk instead of just in memory
					this.siteParserRules = siteParserRules;
				} finally {
					lock.unlock();
				}
			}
		} catch (InterruptedException exc) {
			throw new RuntimeException(exc);
		}
	}
}