package mtgpricer.catalog.search.solr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.annotation.PreDestroy;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.SolrQuery.SortClause;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import mtgpricer.catalog.Card;
import mtgpricer.catalog.CardCatalog;
import mtgpricer.catalog.CardCatalogProvider;
import mtgpricer.catalog.CardSet;
import mtgpricer.catalog.search.CardIndexService;
import mtgpricer.catalog.search.CardSearchResult;
import mtgpricer.catalog.search.CardSearchResults;
import mtgpricer.catalog.search.CardSearchService;
import mtgpricer.catalog.search.SearchFailedException;

@Component
public class SolrCardSearchService implements CardSearchService, CardIndexService {
	private static final Logger logger = Logger.getLogger(SolrCardSearchService.class.getName()); 
	private String solrUrl;
	private HttpSolrServer solrServer;
	private CardCatalogProvider cardCatalogProvider;
	
	@Autowired
	public void setCardCatalogProvider(CardCatalogProvider cardCatalogProvider) {
		this.cardCatalogProvider = cardCatalogProvider;
	}
	
	@Value("${solr.url}")
	public void setSolrUrl(String solrUrl) {
		this.solrUrl = solrUrl;
	}
	
	@PreDestroy
	public void shutdown() {
		if (solrServer != null) {
			solrServer.shutdown();
		}
	}
	
	@Override
	public CardSearchResults search(String query) {
		return this.search(query, 0);
	}
	
	@Override
	public CardSearchResults search(String query, int start) {
		try {
			final SolrServer solr = lazyGetSolrServer();
			
			final SolrQuery params = new SolrQuery(query);
			params.add("defType", "dismax");
			params.add("qf", "name^20");
			params.add("pf", "name^2");
			params.add("fl", "multiverseId, name, setCode");
			if (start > 0) {
				params.add("start", Integer.toString(start));
			}
			params.setSort(SortClause.desc("score"));
			
			// query solr
			final QueryResponse response = solr.query(params);
			final SolrDocumentList solrSearchResults = response.getResults();
			final List<Integer> multiverseIds = new ArrayList<Integer>(solrSearchResults.size());
			for (SolrDocument document : solrSearchResults) {
				final Object multiverseIdRaw = document.getFieldValue("multiverseId");
				if (multiverseIdRaw == null) {
					logger.warning("Search returned a result without a multiverse ID: " + document.toString() + "\nquery: " + query);
					continue;
				}
				multiverseIds.add(Integer.parseInt(multiverseIdRaw.toString()));
			}

			// fetch the cards that have the multiverse IDs
			final CardCatalog cardCatalog = cardCatalogProvider.getCardCatalog();
			final Map<Integer, Card> multiverseIdToCards = cardCatalog.getCardsWithMultiverseIds(multiverseIds);
			
			// build the search results
			final List<CardSearchResult> searchResults = new ArrayList<CardSearchResult>(multiverseIds.size());
			for (Integer multiverseId : multiverseIds) {
				final Card card = multiverseIdToCards.get(multiverseId);
				if (card == null) {
					logger.warning("Search returned a multiverse ID that was not found in the card catalog: " + multiverseId + "\nquery: " + query);
					continue;
				}
				searchResults.add(new CardSearchResult(card));
			}
			return new CardSearchResults(solrSearchResults.getNumFound(), searchResults);
		} catch (SolrServerException exc) {
			throw new SearchFailedException("Failed when trying to execute query: " + query, exc);
		}
	}
	
	@Override
	public void reindexCards() {
		try {
			final SolrServer solr = lazyGetSolrServer();
			
			final CardCatalog catalog = cardCatalogProvider.getCardCatalog();
			for (final CardSet cardSet : catalog.getCardSets()) {
				final List<SolrInputDocument> solrInputDocuments = new ArrayList<SolrInputDocument>(cardSet.getCards().size());
				for (final Card card : cardSet.getCards()) {
					if (card.getMultiverseId() == null) {
						continue;
					}
					
					final SolrInputDocument document = new SolrInputDocument();
					document.addField("multiverseId", card.getMultiverseId());
					document.addField("name", card.getName());
					document.addField("setCode", card.getSetCode());
					solrInputDocuments.add(document);
				}
				
				if (!solrInputDocuments.isEmpty()) {
					logger.fine("Indexing " + solrInputDocuments.size() + " documents for " + cardSet.getCode());
					solr.add(solrInputDocuments);
					solr.commit();
				}
			}
		} catch (SolrServerException | IOException exc) {
			throw new SearchFailedException("Failed to reindex", exc);
		}
	}
	
	private SolrServer lazyGetSolrServer() {
		if (solrServer == null) {
			solrServer = new HttpSolrServer(solrUrl);
		}
		return solrServer;
	}
}
