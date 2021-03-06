package mtgpricer.rip.cardkingdom;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import com.google.common.base.Throwables;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import mtgpricer.catalog.CardCatalog;
import mtgpricer.catalog.CardSet;
import mtgpricer.rip.CardPriceInfo;
import mtgpricer.rip.FileCardSetPriceInfo;
import mtgpricer.rip.PriceSiteInfo;
import mtgpricer.rip.FilePriceSiteInfoBuilder;
import mtgpricer.rip.RequestSiteListener;
import mtgpricer.rip.RequestSiteListenerBase;
import mtgpricer.rip.http.PageRequester;

/**
 * Represents the CardKingdom site.
 * @author jared.pearson
 */
public class CardKingdomSite {
	private static final Logger logger = Logger.getLogger(CardKingdomSite.class.getName());
	private static final String cardKingdomUrl = "https://www.cardkingdom.com/";
	private final CardCatalog cardCatalog;
	private final PageRequester pageRequester;
	private final CardKingdomSiteIndexParser siteIndexParser;
	private final CardKingdomCardSetPageParser cardSetParser;
	private final SiteParserRules siteParserRules;
	
	public CardKingdomSite(CardCatalog cardCatalog, PageRequester pageRequester, SiteParserRulesFactory siteParserRulesFactory) {
		assert cardCatalog != null;
		assert pageRequester != null;
		assert siteParserRulesFactory != null;
		
		this.cardCatalog = cardCatalog;
		this.pageRequester = pageRequester;
		this.siteParserRules = siteParserRulesFactory.loadSiteParserRules();
		
		// create the parsers
		this.siteIndexParser = new CardKingdomSiteIndexParser(cardCatalog, siteParserRules);
		this.cardSetParser = new CardKingdomCardSetPageParser();
	}

	/**
	 * Requests the site info for all card sets.
	 */
	public PriceSiteInfo requestSiteInfo() throws IOException {
		return requestSiteInfo(new RequestSiteListenerBase() {});
	}
	
	/**
	 * Requests the site info for all card sets.
	 */
	public PriceSiteInfo requestSiteInfo(RequestSiteListener listener) throws IOException {
		try {
			final SiteIndex siteIndex = requestSiteIndex();
			logger.info("Site index retrieved. Getting set information for " + siteIndex.getCardSets().size() + " sets.");

			final Phaser phaser = new Phaser(1);
			final ListeningExecutorService executorService = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(10));
			final ProgressTrackingExecutorService progressTrackingExecutorService = new ProgressTrackingExecutorService(executorService, listener);
			
			// request each of the sets and their pages
			final List<ListenableFuture<RequestCardSetResult>> cardSetResultFutures = new ArrayList<>();
			final int numberOfCardSets = siteIndex.getCardSets().size();
			for (int index = 0; index < numberOfCardSets; index++) {
				final SiteIndexCardSet cardSetIndex = siteIndex.getCardSets().get(index);
				final ListenableFuture<RequestCardSetResult> cardSetResultFuture = progressTrackingExecutorService.submit(new RequestCardSetTask(
						phaser,
						progressTrackingExecutorService,
						cardCatalog,
						cardSetIndex));
				cardSetResultFutures.add(cardSetResultFuture);
			}
			phaser.arriveAndAwaitAdvance();
			executorService.shutdown();
			
			// convert all of the request models to the real models
			final List<FileCardSetPriceInfo> cardSetPriceInfos = new ArrayList<>();
			for (final ListenableFuture<RequestCardSetResult> cardSetResultFuture : cardSetResultFutures) {
				final FileCardSetPriceInfo cardSetPriceInfo = createCardSetPriceInfoFromRequest(cardSetResultFuture);
				cardSetPriceInfos.add(cardSetPriceInfo);
			}
			return new FilePriceSiteInfoBuilder()
					.setUrl(cardKingdomUrl)
					.setRetrieved(new Date())
					.setCardSets(cardSetPriceInfos)
					.build();

		} catch(ExecutionException | InterruptedException exc) {
			throw Throwables.propagate(exc);
		}
	}
	
	
	
	/**
	 * Request the site index from Card Kingdom.
	 */
	private SiteIndex requestSiteIndex() throws IOException {
		final String html = pageRequester.getHtml("https://www.cardkingdom.com/catalog/magic_the_gathering/by_az");
		return siteIndexParser.parseHtml(html);
	}

	private FileCardSetPriceInfo createCardSetPriceInfoFromRequest(
			final ListenableFuture<RequestCardSetResult> cardSetResultFuture) throws InterruptedException, ExecutionException {
		final RequestCardSetResult cardSetResult = cardSetResultFuture.get();
		final SiteIndexCardSet cardSetIndex1 = cardSetResult.getCardSetIndex();
		final List<CardPriceInfo> allCards = getCardsFromSetPages(cardSetResult.getPages());
		return new FileCardSetPriceInfo(
				cardSetIndex1.getName(),
				cardSetIndex1.getRawName(),
				cardSetIndex1.getSetCode(),
				cardSetIndex1.getUrl(),
				new Date(),
				allCards);
	}

	private List<CardPriceInfo> getCardsFromSetPages(
			final List<ListenableFuture<RequestCardSetPageResult>> taskResultFutures) throws InterruptedException, ExecutionException {
		final List<CardPriceInfo> cardPriceInfos = new ArrayList<>();
		final Set<String> visitedUrls = new HashSet<>();
		final List<RequestCardSetPageResult> taskResults = Futures.allAsList(taskResultFutures).get();
		final ArrayDeque<RequestCardSetPageResult> taskResultsToVisit = new ArrayDeque<>(taskResults);
		while (!taskResultsToVisit.isEmpty()) {
			final RequestCardSetPageResult taskResult = taskResultsToVisit.pop();
			
			// if we've already added this page to the result, then skip it
			if (visitedUrls.contains(taskResult.getUrl())) {
				continue;
			}
			
			visitedUrls.add(taskResult.getUrl());
			cardPriceInfos.addAll(taskResult.getCards());
			
			// add each of the referenced pages to the list of tasks to process
			taskResultsToVisit.addAll(Futures.allAsList(taskResult.getReferencedPages()).get());
		}
		return cardPriceInfos;
	}
	
	private class RequestCardSetTask implements Callable<RequestCardSetResult> {
		final Phaser phaser;
		final ProgressTrackingExecutorService executorService;
		final CardCatalog cardCatalog;
		final SiteIndexCardSet cardSetIndex;
		
		public RequestCardSetTask(
				final Phaser phaser,
				final ProgressTrackingExecutorService executorService,
				final CardCatalog cardCatalog,
				final SiteIndexCardSet cardSetIndex) {
			this.phaser = phaser;
			this.cardCatalog = cardCatalog;
			this.executorService = executorService;
			this.cardSetIndex = cardSetIndex;
			phaser.register();
		}
		
		@Override
		public RequestCardSetResult call() throws Exception {
			try {
				logger.info("Requesting information for set " + cardSetIndex.getName());
				
				// attempt to find the card set from the catalog
				final CardSet cardSetInfo;
				if (cardSetIndex != null) { 
					cardSetInfo = this.cardCatalog.getCardSetByCode(cardSetIndex.getSetCode());
				} else {
					cardSetInfo = null;
				}
				
				// attempt to find the parser rules corresponding to the card set
				final CardParserRules cardSetParserRules;
				if (cardSetInfo != null) { 
					cardSetParserRules = siteParserRules.getParserRuleForCardSetCode(cardSetInfo.getCode());
				} else {
					cardSetParserRules = CardParserRules.createEmpty();
				}
	
				// request the set index page to retrieve all of the referenced pages
				// CardKingdom's set index page includes links to different URLs than the first page URLs provided by the 
				// set pagination. We will fetch the page for the set index, ignore the cards but retrieve the pagination.
				final String cardSetIndexHtml = pageRequester.getHtml(cardSetIndex.getUrl());
				final CardKingdomCardSetPage indexPage = cardSetParser.parseHtml(cardSetIndex.getUrl(), cardSetIndexHtml, cardSetInfo, cardSetParserRules);
				
				final PageExecutorService pageExecutorService = new PageExecutorService(executorService, phaser);
				
				// create a task for retrieving  all of the pages retrieved from the card set index page
				final List<ListenableFuture<RequestCardSetPageResult>> cardSetPageResultFutures = new ArrayList<>();
				for (String url : indexPage.getReferencedSetPageUrls()) {
					final RequestCardSetPageTask task = new RequestCardSetPageTask(pageExecutorService, url, cardSetInfo, cardSetParserRules);
					final ListenableFuture<RequestCardSetPageResult> pageFuture = pageExecutorService.submit(url, task);
					cardSetPageResultFutures.add(pageFuture);
				}
				
				return new RequestCardSetResult(cardSetIndex, cardSetPageResultFutures);
			} finally {
				phaser.arrive();
			}
		}
	}
	
	private class RequestCardSetResult {
		private final SiteIndexCardSet cardSetIndex;
		private final List<ListenableFuture<RequestCardSetPageResult>> pages;
		
		public RequestCardSetResult(
				final SiteIndexCardSet cardSetIndex,
				final List<ListenableFuture<RequestCardSetPageResult>> pages) {
			this.cardSetIndex = cardSetIndex;
			this.pages = pages;
		}
		
		public SiteIndexCardSet getCardSetIndex() {
			return cardSetIndex;
		}
		
		public List<ListenableFuture<RequestCardSetPageResult>> getPages() {
			return pages;
		}
	}
	
	private class RequestCardSetPageTask implements Callable<RequestCardSetPageResult> {
		private final PageExecutorService pageExecutorService;
		private final String url;
		private final CardSet cardSetInfo;
		private final CardParserRules cardSetParserRules;
		
		public RequestCardSetPageTask(
				final PageExecutorService pageExecutorService,
				final String url,
				final CardSet cardSetInfo,
				final CardParserRules cardSetParserRules) {
			this.pageExecutorService = pageExecutorService;
			this.url = url;
			this.cardSetInfo = cardSetInfo;
			this.cardSetParserRules = cardSetParserRules;
		}
		
		@Override
		public RequestCardSetPageResult call() throws Exception {
			final String cardSetHtml = pageRequester.getHtml(url);
			final CardKingdomCardSetPage page = cardSetParser.parseHtml(url, cardSetHtml, cardSetInfo, cardSetParserRules);
			
			final List<ListenableFuture<RequestCardSetPageResult>> referencedPages = new ArrayList<>(page.getReferencedSetPageUrls().size());
			for (final String referencedSetPageUrl : page.getReferencedSetPageUrls()) {
				final RequestCardSetPageTask referencedSetPageTask = new RequestCardSetPageTask(pageExecutorService, referencedSetPageUrl, cardSetInfo, cardSetParserRules);
				final ListenableFuture<RequestCardSetPageResult> pageFuture = pageExecutorService.submit(referencedSetPageUrl, referencedSetPageTask);
				referencedPages.add(pageFuture);
			}
			
			return new RequestCardSetPageResult(page, referencedPages);
		}
	}
	
	/**
	 * ExecutorService wrapper that keeps track of the number of submits and the number of completed submits. Whenever
	 * a new task is submitted or a task completes, the listener is updated with the number of finished task and
	 * the total number of submits.
	 *
	 * @author jared.pearson
	 */
	private class ProgressTrackingExecutorService {
		private final ListeningExecutorService executorService;
		private final RequestSiteListener requestSiteListener;
		private final AtomicInteger finishedCount = new AtomicInteger();
		private final AtomicInteger totalCount = new AtomicInteger();
		
		public ProgressTrackingExecutorService(
				final ListeningExecutorService executorService,
				final RequestSiteListener requestSiteListener) {
			this.executorService = executorService;
			this.requestSiteListener = requestSiteListener;
		}
		
		public <T> ListenableFuture<T> submit(Callable<T> callable) {
			requestSiteListener.onProgressUpdate(finishedCount.get(), totalCount.incrementAndGet());
			final ListenableFuture<T> future = executorService.submit(callable);
			Futures.addCallback(future, new ProgressUpdateFutureCallback<T>(requestSiteListener, finishedCount, totalCount));
			return future;
		}
	}

	private static final class ProgressUpdateFutureCallback<T> implements FutureCallback<T> {
		private final RequestSiteListener listener;
		private final AtomicInteger finishedCount;
		private final AtomicInteger totalCount;

		private ProgressUpdateFutureCallback(RequestSiteListener listener, AtomicInteger finishedCount, AtomicInteger totalCount) {
			this.listener = listener;
			this.finishedCount = finishedCount;
			this.totalCount = totalCount;
		}

		@Override
		public void onFailure(Throwable t) {
			this.incrementAndUpdate();
		}

		@Override
		public void onSuccess(T result) {
			this.incrementAndUpdate();
		}
		
		private void incrementAndUpdate() {
			// after completion of the card set, increment the completion counter and then
			// tell the listener that we completed another task
			listener.onProgressUpdate(finishedCount.incrementAndGet(), totalCount.get());
		}
	}

	private class PageExecutorService {
		private final ProgressTrackingExecutorService executorService;
		private final Phaser phaser;
		private final Map<String, ListenableFuture<?>> submittedUrls = Collections.synchronizedMap(new HashMap<>());
		
		public PageExecutorService(
				final ProgressTrackingExecutorService executorService,
				final Phaser phaser) {
			this.executorService = executorService;
			this.phaser = phaser;
		}
		
		@SuppressWarnings("unchecked")
		public <T> ListenableFuture<T> submit(final String url, final Callable<T> callable) {
			if (submittedUrls.containsKey(url)) {
				return (ListenableFuture<T>)submittedUrls.get(url);
			}
			
			final ListenableFuture<T> future = executorService.submit(new Callable<T>() {
				{
					phaser.register();
				}
				
				@Override
				public T call() throws Exception {
					try {
						return callable.call();
					} finally {
						phaser.arrive();
					}
				}
			});
			submittedUrls.put(url, future);
			return future;
		}
	}
	
	private class RequestCardSetPageResult {
		private final List<CardPriceInfo> cards;
		private final String url;
		private final List<ListenableFuture<RequestCardSetPageResult>> referencedPages;

		public RequestCardSetPageResult(
				final CardKingdomCardSetPage page,
				final List<ListenableFuture<RequestCardSetPageResult>> referencedPages) {
			this.cards = page.getCards();
			this.url = page.getUrl();
			this.referencedPages = referencedPages;
		}
		
		public List<CardPriceInfo> getCards() {
			return this.cards;
		}

		public String getUrl() {
			return this.url;
		}
		
		public List<ListenableFuture<RequestCardSetPageResult>> getReferencedPages() {
			return this.referencedPages;
		}
	}
}