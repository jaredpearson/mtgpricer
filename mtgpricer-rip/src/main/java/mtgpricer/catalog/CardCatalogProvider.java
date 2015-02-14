package mtgpricer.catalog;

public interface CardCatalogProvider {
	/**
	 * Gets the current card catalog instance
	 */
	public CardCatalog getCardCatalog();
}