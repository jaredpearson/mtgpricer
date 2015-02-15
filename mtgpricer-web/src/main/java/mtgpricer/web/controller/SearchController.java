package mtgpricer.web.controller;

import mtgpricer.catalog.search.CardSearchResults;
import mtgpricer.catalog.search.CardSearchService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class SearchController {
	private CardSearchService cardSearchService;
	
	@Autowired
	public void setCardSearchService(CardSearchService cardSearchService) {
		this.cardSearchService = cardSearchService;
	}

	@RequestMapping(value = "/search", method = RequestMethod.GET)
	public ModelAndView viewResults(@RequestParam("q") String query, @RequestParam(value = "start", defaultValue = "0") Integer start) {
		if (start < 0) {
			start = 0;
		}
		
		final CardSearchResults results = cardSearchService.search(query, start);
		
		final int nextStart;
		if (start + 10 < results.getCount()) {
			nextStart = start + 10;
		} else {
			nextStart = -1;
		}
		
		final int previousStart;
		if (start != 0) {
			previousStart = (start > 10) ? start - 10 : 0;
		} else {
			previousStart = -1;
		}
		
		final ModelAndView modelAndView = new ModelAndView("searchResult");
		modelAndView.addObject("query", query);
		modelAndView.addObject("found", results.getCount());
		modelAndView.addObject("results", results.getCards());
		modelAndView.addObject("hasNext", nextStart != -1);
		modelAndView.addObject("nextStart", nextStart);
		modelAndView.addObject("hasPrevious", previousStart != -1);
		modelAndView.addObject("previousStart", previousStart);
		
		return modelAndView;
	}
}
