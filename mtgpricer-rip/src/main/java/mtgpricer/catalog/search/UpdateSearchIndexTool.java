package mtgpricer.catalog.search;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import mtgpricer.CommandLineTool;
import mtgpricer.CommandLineTools;

/**
 * Updates the search server with the latest cards from the card catalog
 * @author jared.pearson
 */
@Component
public class UpdateSearchIndexTool implements CommandLineTool {
	private CardIndexService cardIndexService;
	
	@Autowired
	public void setCardIndexService(CardIndexService cardIndexService) {
		this.cardIndexService = cardIndexService;
	}
	
	@Override
	public void run(String[] args) throws Exception {
		cardIndexService.reindexCards();
	}
	
	public static void main(String[] args) throws Exception {
		CommandLineTools.run(UpdateSearchIndexTool.class, args);
	}
}
