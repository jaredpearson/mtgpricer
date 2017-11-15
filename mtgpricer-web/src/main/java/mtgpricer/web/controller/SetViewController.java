package mtgpricer.web.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mtgpricer.CardPrice;
import mtgpricer.PriceService;
import mtgpricer.PriceServiceProvider;
import mtgpricer.catalog.Card;
import mtgpricer.catalog.CardCatalog;
import mtgpricer.catalog.CardCatalogProvider;
import mtgpricer.catalog.CardSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller for viewing a card set
 * @author jared.pearson
 */
@Controller
public class SetViewController {
	private CardCatalogProvider cardCatalogProvider;
	private PriceServiceProvider priceServiceProvider;
	
	@Autowired
	public void setCardCatalogProvider(CardCatalogProvider cardCatalogProvider) {
		this.cardCatalogProvider = cardCatalogProvider;
	}
	
	@Autowired
	public void setPriceServiceProvider(PriceServiceProvider priceServiceProvider) {
		this.priceServiceProvider = priceServiceProvider;
	}
	
	@RequestMapping(value="/sets/{setCode}", method=RequestMethod.GET)
	public ModelAndView showSetList(@PathVariable String setCode) {
		
		final CardCatalog cardCatalog = cardCatalogProvider.getCardCatalog();
		final CardSet cardSet = cardCatalog.getCardSetByCode(setCode);
		if (cardSet == null) {
			throw new ResourceNotFoundException();
		}
		
		final PriceService priceService = priceServiceProvider.getPriceService();
		final Map<Card, List<CardPrice>> cardPrices = priceService.getPriceHistoryForCards(cardSet);
		
		final ModelAndView model = new ModelAndView();
		model.setViewName("setView");
		model.addObject("cardSetName", cardSet.getName());
		model.addObject("cards", createModels(cardSet, cardPrices));
		return model;
	}
	
	public List<CardViewModel> createModels(CardSet cardSet, Map<Card, List<CardPrice>> cardPrices) {
		final List<CardViewModel> cardViewModels = new ArrayList<CardViewModel>(cardSet.getCards().size());
		for (final Card card : cardSet.getCards()) {
			final List<CardPrice> cardPriceHistory = cardPrices.get(card);
			final CardViewModel cardViewModel = new CardViewModel(card, cardPriceHistory);
			cardViewModels.add(cardViewModel);
		}
		return cardViewModels;
	}
	
	public static class CardViewModel {
		private final String name;
		private final Integer multiverseId;
		private final Double latestPrice;
		
		public CardViewModel(Card card, List<CardPrice> cardPrices) {
			this.name = card.getName();
			this.multiverseId = card.getMultiverseId();
			if (cardPrices != null && !cardPrices.isEmpty()) {
				// assume that card prices are ordered by retrieved in descending
				this.latestPrice = cardPrices.get(0).getPrice().doubleValue();
			} else {
				this.latestPrice = null;
			}
		}
		
		public Integer getMultiverseId() {
			return multiverseId;
		}
		
		public String getName() {
			return name;
		}
		
		public Double getLatestPrice() {
			return latestPrice;
		}
	}
}
