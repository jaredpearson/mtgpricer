package mtgpricer.rip;

import java.io.Closeable;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.sql.DataSource;

import mtgpricer.rip.RipRequestProcessor.ProcessRipRequestListenerBase;

/**
 * Queue for the rip requests.
 * @author jared.pearson
 */
public class RipRequestQueue implements Closeable {
	public static final String QUEUE_NAME = "rip.request";
	private final DataSource dataSource;
	private final ExecutorService executorService;
	private final RipRequestProcessorFactory processRipRequestRunnableFactory;
	
	public RipRequestQueue(DataSource dataSource, RipRequestProcessorFactory processRipRequestRunnableFactory) throws IOException {
		assert dataSource != null;
		assert processRipRequestRunnableFactory != null;
		
		this.dataSource = dataSource;
		this.executorService = Executors.newSingleThreadExecutor();
		this.processRipRequestRunnableFactory = processRipRequestRunnableFactory;
	}
	
	/**
	 * Closes the processor
	 */
	public void close() throws IOException {
		executorService.shutdown();
	}
	
	/**
	 * Enqueues a request to rip the price site.
	 * @return the ID of the request to rip
	 */
	public long enqueue() {
		if (isProcessing()) {
			throw new IllegalStateException("Exceeded allowed number of requests. Only one request to rip can be processed at a time.");
		}
		final long ripRequestId = insertRipRequest();
		final Runnable processRipRequestRunnable = processRipRequestRunnableFactory.create(ripRequestId, new QueueProcessRipRequestListener(this, ripRequestId)); 
		executorService.submit(processRipRequestRunnable);
		return ripRequestId;
	}
	
	/**
	 * Determines if a request is currently being processing
	 */
	public boolean isProcessing() {
		try {
			try (final java.sql.Connection connection = dataSource.getConnection()) {
				try (final PreparedStatement stmt = connection.prepareStatement("SELECT count(*) FROM mtgpricer.rip_request WHERE finish_date IS NULL")) {
					try (final ResultSet rst = stmt.executeQuery()) {
						if (rst.next()) {
							return rst.getInt(1) > 0;
						} else {
							return false;
						}
					}
				}
			}
		} catch(SQLException exc) {
			throw new RuntimeException(exc);
		}
	}
	
	/**
	 * Gets the latest rip request
	 */
	public RipRequest getLatestRipRequest() {
		final List<RipRequest> requests = executeRipRequestQuery(buildSelectLatestQuery());
		if (requests.isEmpty()) {
			return null;
		} else {
			return requests.get(0);
		}
	}
	
	/**
	 * Gets the rip request with the given ID
	 */
	public RipRequest getRipRequestById(long id) {
		try {
			try (final java.sql.Connection connection = dataSource.getConnection()) {
				try (final PreparedStatement stmt = connection.prepareStatement(buildSelectByIdQuery())) {
					stmt.setLong(1, id);
					
					try (final ResultSet rst = stmt.executeQuery()) {
						final List<RipRequest> requests = processResultSet(rst);
						if (requests.isEmpty()) {
							return null;
						} else {
							return requests.get(0);
						}
					}
				}
			}
		} catch(SQLException exc) {
			throw new RuntimeException(exc);
		}
	}
	
	/**
	 * Executes the given query
	 */
	private List<RipRequest> executeRipRequestQuery(String query) {
		try {
			try (final java.sql.Connection connection = dataSource.getConnection()) {
				try (final PreparedStatement stmt = connection.prepareStatement(query)) {
					try (final ResultSet rst = stmt.executeQuery()) {
						return processResultSet(rst);
					}
				}
			}
		} catch(SQLException exc) {
			throw new RuntimeException(exc);
		}
	}
	
	/**
	 * Inserts a new rip request into the database.
	 * @return the ID of the new rip request
	 */
	private long insertRipRequest() {
		final Date startDate = Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTime();
		
		try {
			try (final java.sql.Connection connection = dataSource.getConnection()) {
				try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO mtgpricer.rip_request(start_date) values (?)", Statement.RETURN_GENERATED_KEYS)) {
					stmt.setTimestamp(1, new java.sql.Timestamp(startDate.getTime()));
					
					if (stmt.executeUpdate() != 1) {
						throw new IllegalStateException("Unable to create new rip request");
					}
					
					try (final ResultSet rst = stmt.getGeneratedKeys()) {
						if (rst.next()) {
							return rst.getLong(1);
						} else {
							throw new IllegalStateException("Failed to create rip_request; no ID returned");
						}
					}
					
				}
			}
		} catch(SQLException exc) {
			throw new IllegalStateException("Failed to create rip_request.", exc);
		}
	}
	
	/**
	 * Updates the finish date for the request at the given ID 
	 */
	private void updateRipRequestFinishDate(long ripRequestId, Date finishDate) {
		assert finishDate != null;
		try {
			try (final java.sql.Connection connection = dataSource.getConnection()) {
				try (final PreparedStatement stmt = connection.prepareStatement("UPDATE mtgpricer.rip_request SET finish_date = ? WHERE rip_request_id = ?");) {
					stmt.setTimestamp(1, new java.sql.Timestamp(finishDate.getTime()));
					stmt.setLong(2, ripRequestId);
					
					if (stmt.executeUpdate() != 1) {
						throw new IllegalStateException("Unable to update rip_request: " + ripRequestId);
					}
					
				}
			}
		} catch(SQLException exc) {
			throw new IllegalStateException("Failed to update rip_request: " + ripRequestId, exc);
		}
	}

	/**
	 * Updates the progress in the rip request record
	 */
	private void updateRipRequestProgress(long ripRequestId, int progress, int estimatedTotal) {
		try (final java.sql.Connection connection = dataSource.getConnection()) {
			try (final PreparedStatement stmt = connection.prepareStatement("UPDATE mtgpricer.rip_request SET progress = ?, estimated_total = ? WHERE rip_request_id = ?")) {
				stmt.setInt(1, progress);
				stmt.setInt(2, estimatedTotal);
				stmt.setLong(3, ripRequestId);
				
				if (stmt.executeUpdate() != 1) {
					throw new IllegalStateException("Unable to update rip_request: " + ripRequestId);
				}
			}
		} catch (SQLException exc) {
			throw new IllegalStateException("Failed to update rip_request: " + ripRequestId, exc);
		}
	}
	
	/**
	 * Processes the given result set into a list of {@link RipRequest} instances. This assumes that the 
	 * query executed used the standard select format as defined by {@link #getRipRequestById(long)}.
	 */
	private static List<RipRequest> processResultSet(ResultSet rst) throws SQLException {
		final List<RipRequest> ripRequests = new ArrayList<RipRequest>();
		RipRequestBuilder builder = null;
		
		while (rst.next()) {
			final long thisRequestId = rst.getLong("rip_request_id");
			if (builder != null && builder.getId() != thisRequestId) {
				ripRequests.add(builder.build());
				builder = null;
			}
			
			if (builder == null) {
				builder = new RipRequestBuilder();
				builder.setId(thisRequestId);
				builder.setStartDate(rst.getTimestamp("start_date"));
				builder.setFinishDate(rst.getTimestamp("finish_date"));
				builder.setProgress(getInteger(rst, "progress"));
				builder.setEstimatedTotal(getInteger(rst, "estimated_total"));
			}
			
			final long requestLogId = rst.getLong("rip_request_log_id");
			if (!rst.wasNull()) {
				final Date logDate = rst.getTimestamp("date");
				final String logText = rst.getString("value");
				
				final RipRequestLogLine logLine = new RipRequestLogLine(requestLogId, builder.getId(), logDate, logText);
				builder.addLogLine(logLine);
			}
		}
		
		
		// add the last builder row
		if (builder != null) {
			ripRequests.add(builder.build());
		}
		
		return ripRequests;
	}
	
	private static Integer getInteger(ResultSet rst, String column) throws SQLException {
		final int value = rst.getInt(column);
		if (rst.wasNull()) {
			return null;
		}
		return value;
	}
	
	/**
	 * Builds the query to select the rip request with a rip request ID.
	 * <p>
	 * Parameters
	 * <ol>
	 * <li>rip_request_id - ID of the rip request to retrieve
	 * </ol>
	 */
	private static String buildSelectByIdQuery() {
		final StringBuilder sql = new StringBuilder();
		sql.append("SELECT r.rip_request_id, r.start_date, r.finish_date, r.progress, r.estimated_total, l.rip_request_log_id, l.date, l.value ");
		sql.append("FROM mtgpricer.rip_request r LEFT OUTER JOIN mtgpricer.rip_request_log l ON (r.rip_request_id = l.rip_request_id) ");
		sql.append("WHERE r.rip_request_id = ?");
		return sql.toString();
	}

	/**
	 * Builds the query to select the latest rip request.
	 */
	private static String buildSelectLatestQuery() {
		final StringBuilder sql = new StringBuilder();
		sql.append("SELECT r.rip_request_id, r.start_date, r.finish_date, r.progress, r.estimated_total, l.rip_request_log_id, l.date, l.value ");
		sql.append("FROM mtgpricer.rip_request r LEFT OUTER JOIN mtgpricer.rip_request_log l ON (r.rip_request_id = l.rip_request_id) ");
		sql.append("ORDER BY r.start_date DESC ");
		sql.append("LIMIT 1");
		return sql.toString();
	}
	
	private static class QueueProcessRipRequestListener extends ProcessRipRequestListenerBase {
		private final long ripRequestId;
		private final RipRequestQueue ripRequestQueue;
		
		public QueueProcessRipRequestListener(RipRequestQueue ripRequestQueue, long ripRequestId) {
			assert ripRequestQueue != null;
			this.ripRequestQueue = ripRequestQueue;
			this.ripRequestId = ripRequestId;
		}
		
		@Override
		public void onFinished() {
			// update the rip request when finished
			ripRequestQueue.updateRipRequestFinishDate(ripRequestId, Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTime());
		}
		
		@Override
		public void onProgressUpdate(int progress, int estimatedTotal) {
			ripRequestQueue.updateRipRequestProgress(ripRequestId, progress, estimatedTotal);
		}
		
		@Override
		public void onFailed(String message, Throwable throwable) {
			// TODO: need to add a field in the rip request to store failed status and message
			this.onFinished();
		}
	}

}