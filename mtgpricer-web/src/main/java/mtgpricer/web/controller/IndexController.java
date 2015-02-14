package mtgpricer.web.controller;

import mtgpricer.PriceService;
import mtgpricer.catalog.CardCatalog;
import mtgpricer.catalog.CardCatalogProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class IndexController {
	
	@Autowired
	CardCatalogProvider cardCatalogProvider = null;
	
	@Autowired
	PriceService priceService = null;
	
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public ModelAndView showIndex() {
		final CardCatalog cardCatalog = cardCatalogProvider.getCardCatalog();
		final ModelAndView model = new ModelAndView("index");
		model.addObject("cardSets", cardCatalog.getCardSets());
		model.addObject("topPositiveCards", priceService.getTopPositiveCardPriceDiffSevenDays());
		model.addObject("topNegativeCards", priceService.getTopNegativeCardPriceDiffSevenDays());
		model.addObject("topPositiveCardsStandard", priceService.getTopPositiveCardPriceDiffSevenDaysStandard());
		return model;
	}
	
}
