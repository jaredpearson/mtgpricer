package mtgpricer.catalog;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import mtgpricer.CardPrice;
import mtgpricer.CardPriceVariant;
import mtgpricer.CommandLineTool;
import mtgpricer.CommandLineTools;
import mtgpricer.Money;
import mtgpricer.PriceService;
import mtgpricer.PriceServiceProvider;

/**
 * Command line tool for displaying card information from the catalog.
 * @author jared.pearson
 */
public class Main implements CommandLineTool {
	private PriceServiceProvider priceServiceProvider;
	private CardCatalogProvider cardCatalogProvider;
	
	@Autowired
	public void setPriceServiceProvider(PriceServiceProvider priceServiceProvider) {
		this.priceServiceProvider = priceServiceProvider;
	}
	
	@Autowired
	public void setCardCatalogProvider(CardCatalogProvider cardCatalogProvider) {
		this.cardCatalogProvider = cardCatalogProvider;
	}
	
	@Override
	public void run(String[] args) {
		if (args.length == 0) {
			System.out.println(Main.class.getName() + " [command]");
			System.exit(1);
		}
		
		final String command = args[0];
		final String[] commandArgs = args.length == 1 ? new String[0] : Arrays.copyOfRange(args, 1, args.length);
		
		if ("setlist".equals(command)) {
			final CardCatalog cardCatalog = cardCatalogProvider.getCardCatalog();
			for (CardSet set : cardCatalog.getCardSets()) {
				System.out.print(set.getCode());
				System.out.print("\t");
				System.out.print(set.getName());
				System.out.println();
			}
			
		} else if ("cardlist".equals(command)) {
			if (commandArgs.length != 1) {
				System.out.println(Main.class.getName() + " " + command + " [setcode]");
				System.exit(2);
			}
			
			final CardCatalog cardCatalog = cardCatalogProvider.getCardCatalog();
			final PriceService priceService = priceServiceProvider.getPriceService();

			final String setCode = commandArgs[0];
			final CardSet cardSet = cardCatalog.getCardSetByCode(setCode);
			if (cardSet == null) {
				System.err.println("Unknown card set specified: " + setCode);
				System.exit(3);
			}
			
			final Map<Card, List<CardPrice>> cardToCardPrices = priceService.getPriceHistoryForCards(cardSet);
			
			for (Card card : cardSet.getCards()) {
				final String number = card.getNumber() == null ? "-" : card.getNumber();
				final String multiverseId = card.getMultiverseId() == null ? "-" : card.getMultiverseId().toString();

				final Money price = CardPrice.selectFirstPrice(cardToCardPrices.get(card));
				final String priceAsString = price == null ? "-" : String.format("%.2f", price.doubleValue());
				
				System.out.println(String.format("%4s %6s %-50s %5s", number, multiverseId, card.getName(), priceAsString));
			}
		} else if ("card".equals(command)) {
			if (commandArgs.length < 2) {
				System.out.println(Main.class.getName() + " " + command + " [setcode] [number]");
				System.exit(2);
			}
			
			final CardCatalog cardCatalog = cardCatalogProvider.getCardCatalog();
			final PriceService priceService = priceServiceProvider.getPriceService();
			
			final String setCode = commandArgs[0];
			final CardSet cardSet = cardCatalog.getCardSetByCode(setCode);
			if (cardSet == null) {
				System.err.println("Unknown card set specified: " + setCode);
				System.exit(3);
			}
			
			final String cardNumber = commandArgs[1];
			final Card card = cardSet.getCardWithNumber(cardNumber);
			if (card == null) {
				System.err.println("Unable to find card with specified number: " + cardNumber);
				System.exit(4);
			}
			
			final List<CardPrice> history = priceService.getPriceHistoryForCard(card);
			for (final CardPrice priceInfo : history) {
				final CardPriceVariant variant = CardPriceVariant.selectFirstWithPrice(priceInfo.getVariants());
				final String priceAsString = variant == null ? "-" : String.format("%.2f", variant.getPrice());
				
				System.out.println(String.format("%tD %5s", priceInfo.getRetrieved(), priceAsString));
			}
		}
	}
	
	public static void main(String[] args) throws Exception {
		CommandLineTools.run(Main.class, args);
	}
}
