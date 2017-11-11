package mtgpricer.web.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;

import mtgpricer.rip.CardPriceInfo;
import mtgpricer.rip.CardSetPriceInfo;
import mtgpricer.rip.PriceDataLoader;
import mtgpricer.rip.PriceSiteInfo;
import mtgpricer.rip.RipRequest;
import mtgpricer.rip.RipRequestQueue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ManageController {
	private static final long DAY_LENGTH_MILLIS = 86400000;
	
	private final RipRequestQueue ripRequestQueue;
	private final PriceDataLoader priceDataLoader;	
	
	@Autowired
	public ManageController(RipRequestQueue ripRequestQueue, PriceDataLoader priceDataLoader) {
		this.ripRequestQueue = ripRequestQueue;
		this.priceDataLoader = priceDataLoader;
	}
	
	@RequestMapping("/settings/manage")
	public ModelAndView showPage() {
		
		final RipRequest ripRequest = ripRequestQueue.getLatestRipRequest();
		
		final TimeZone timeZone = TimeZone.getTimeZone("PST");
		
		final ModelAndView modelAndView = new ModelAndView("settings/manage");
		modelAndView.addObject("canCreateNewRipRequest", ripRequest == null || ripRequest.getFinishDate() != null);
		modelAndView.addObject("ripInProgress", ripRequest != null && ripRequest.getFinishDate() == null);
		modelAndView.addObject("ripInProgressId", ripRequest != null ? ripRequest.getId() : null);
		modelAndView.addObject("latestRipStartDate", ripRequest != null && ripRequest.getStartDate() != null ? formatTimestampAsNaturalLanguage(ripRequest.getStartDate(), timeZone) : null);
		modelAndView.addObject("latestRipDate", ripRequest != null && ripRequest.getFinishDate() != null ? formatTimestampAsNaturalLanguage(ripRequest.getFinishDate(), timeZone) : null);
		modelAndView.addObject("priceSites", this.loadOrderedPriceSiteInfo());
		return modelAndView;
	}
	
	private List<PriceSiteInfoIndexEntry> loadOrderedPriceSiteInfo() {
		return this.priceDataLoader.loadPriceData().stream()
				.filter(p -> p.getRetrieved() != null)
				.sorted((p1, p2) -> p1.getRetrieved().compareTo(p2.getRetrieved()))
				.map(PriceSiteInfoIndexEntry::new)
				.collect(Collectors.toList());
	}
	
	private static String formatTimestampAsNaturalLanguage(Date value, TimeZone timeZone) {
		assert value != null;
		final long lastMidnight = getLastMidnight();
		final long valueMillis = value.getTime();
		final String timeSuffix = " at " + formatTime(value, timeZone);
		if (valueMillis > lastMidnight) {
			return "Today" + timeSuffix;
		} else if (valueMillis > lastMidnight - DAY_LENGTH_MILLIS) {
			return "Yesterday" + timeSuffix;
		} else {
			final DateFormat dateFormat = new SimpleDateFormat("M/d/y");
			return dateFormat.format(value) + timeSuffix;
		}
	}
	
	private static String formatTime(Date value, TimeZone timeZone) {
		final DateFormat dateFormat = new SimpleDateFormat("h:mm a zz");
		return dateFormat.format(value);
	}
	
	private static long getLastMidnight() {
		final Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTimeInMillis();
	}
	
	public static class PriceSiteInfoIndexEntry {
		private final PriceSiteInfo priceSiteInfo;
		private final int numberOfCards;

		public PriceSiteInfoIndexEntry(PriceSiteInfo priceSiteInfo) {
			this.priceSiteInfo = priceSiteInfo;

			Set<String> cards = new HashSet<>();
			for (final CardSetPriceInfo cardSetPriceInfo : priceSiteInfo.getCardSets()) {
				for (final CardPriceInfo cardPriceInfo : cardSetPriceInfo.getCards()) {
					if (cardPriceInfo.getName() != null) {
						cards.add(cardPriceInfo.getName());
					} else {
						cards.add(cardPriceInfo.getRawName());
					}
				}
			}

			this.numberOfCards = cards.size();
		}

		public Long getId() {
			return this.priceSiteInfo.getId(); 
		}

		public Date getRetrieved() {
			return this.priceSiteInfo.getRetrieved();
		}

		public int getNumberOfCardSets() {
			return this.priceSiteInfo.getCardSets().size();
		}
		
		public int getNumberOfCards() {
			return numberOfCards;
		}
	}
}
