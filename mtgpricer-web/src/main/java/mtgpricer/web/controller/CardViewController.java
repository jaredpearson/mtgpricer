package mtgpricer.web.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.servlet.http.HttpServletResponse;

import mtgpricer.CardPrice;
import mtgpricer.CardPriceComparator;
import mtgpricer.CardPriceOrder;
import mtgpricer.CardPriceQueryParams;
import mtgpricer.Money;
import mtgpricer.OrderDirection;
import mtgpricer.PriceService;
import mtgpricer.PriceServiceProvider;
import mtgpricer.catalog.Card;
import mtgpricer.catalog.CardCatalog;
import mtgpricer.catalog.CardCatalogProvider;
import mtgpricer.catalog.CardImage;
import mtgpricer.catalog.CardImageService;
import mtgpricer.catalog.CardSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class CardViewController {
	private CardCatalogProvider cardCatalogProvider = null;
	private CardImageService cardImageService = null;
	private PriceServiceProvider priceServiceProvider = null;
	
	@Autowired
	public void setCardCatalogProvider(CardCatalogProvider cardCatalogProvider) {
		this.cardCatalogProvider = cardCatalogProvider;
	}
	
	@Autowired
	public void setCardImageService(CardImageService cardImageService) {
		this.cardImageService = cardImageService;
	}
	
	@Autowired
	public void setPriceServiceProvider(PriceServiceProvider priceServiceProvider) {
		this.priceServiceProvider = priceServiceProvider;
	}
	
	@RequestMapping(value="/cards/{multiverseId}", method=RequestMethod.GET)
	public ModelAndView viewCard(@PathVariable int multiverseId) {
		final CardCatalog cardCatalog = cardCatalogProvider.getCardCatalog();
		final PriceService priceService = priceServiceProvider.getPriceService();
		
		final Card card = cardCatalog.getCardWithMultiverseId(multiverseId);
		if (card == null) {
			throw new ResourceNotFoundException();
		}
		
		final CardPriceQueryParams queryParams = new CardPriceQueryParams().limit(90);
		final List<CardPrice> priceHistory = priceService.getPriceHistoryForCard(card, queryParams);
		
		priceHistory.sort(new CardPriceComparator(CardPriceOrder.RETRIEVED, OrderDirection.DESC));
		
		// TODO selecting the first variant in the list may not always be the correct variant. change this
		// to be more deterministic.
		final Money latestPrice = CardPrice.selectFirstPrice(priceHistory);
		
		final CardSet cardSet;
		if (card.getSetCode() != null) {
			cardSet = cardCatalog.getCardSetByCode(card.getSetCode());
		} else {
			cardSet = null;
		}
		
		final ModelAndView model = new ModelAndView("cardView");
		model.addObject("card", card);
		model.addObject("cardSetName", (cardSet != null) ? cardSet.getName() : null);
		model.addObject("priceHistory", priceHistory);
		model.addObject("latestPrice", latestPrice);
		return model;
	}
	
	@RequestMapping(value="/cards/{multiverseId}/image.hq.jpg", method=RequestMethod.GET, produces=MediaType.IMAGE_JPEG_VALUE)
	public void viewCardImageHighQuality(@PathVariable final int multiverseId, 
			final HttpServletResponse response, 
			@RequestHeader(value = "If-Modified-Since", required = false) final Date ifModifiedSince) throws IOException {
		final CardCatalog cardCatalog = cardCatalogProvider.getCardCatalog();
		final Card card = cardCatalog.getCardWithMultiverseId(multiverseId);
		if (card == null) {
			throw new ResourceNotFoundException();
		}
		
		final CardImage cardImage = cardImageService.getCardImage(card);
		if (cardImage == null) {
			response.sendError(404);
		} else {
			
			response.setContentType(MediaType.IMAGE_JPEG_VALUE);
			if (cardImage.getFileSize() != -1) {
				response.setContentLength((int)cardImage.getFileSize());
			}
			response.setHeader("Cache-Control", "max-age=63072000");
			response.setHeader("Expires", getDateOneYearAheadForHeader());
			response.setHeader("Last-Modified", getFormattedDateForHeader(cardImage.getLastModified()));
			
			if (ifModifiedSince != null && cardImage.getLastModified() <= ifModifiedSince.getTime()) {
				response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
				return;
			}
			
			final OutputStream output = response.getOutputStream();
			try {
				cardImage.writeTo(output);
			} finally {
				output.close();
			}
		}
	}
	
	private static String getDateOneYearAheadForHeader() {
		final Calendar cal = Calendar.getInstance();
		cal.add(Calendar.YEAR, 1);
		return getFormattedDateForHeader(cal);
	}
	
	private static String getFormattedDateForHeader(long millis) {
		final Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(millis);
		return getFormattedDateForHeader(cal);
	}
	
	private static String getFormattedDateForHeader(Calendar cal) {
		final DateFormat df = new SimpleDateFormat("dd MMM yyyy kk:mm:ss z");
		df.setTimeZone(TimeZone.getTimeZone("GMT"));
		return df.format(cal.getTime());
	}
}
