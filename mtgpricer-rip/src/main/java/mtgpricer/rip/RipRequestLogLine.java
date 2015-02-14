package mtgpricer.rip;

import java.util.Date;

/**
 * A log line associated to a rip request
 * <p>
 * These are usually created when the request is being processed.
 * @author jared.pearson
 */
public class RipRequestLogLine {
	private final long id;
	private final long requestId;
	private final Date date;
	private final String text;
	
	public RipRequestLogLine(long id, long requestId, Date date, String text) {
		assert date != null;
		this.id = id;
		this.requestId = requestId;
		this.date = new Date(date.getTime());
		this.text = text;
	}
	
	public long getId() {
		return id;
	}
	
	public long getRequestId() {
		return requestId;
	}
	
	public Date getDate() {
		return date;
	}
	
	/**
	 * Gets the text that was written in the log line
	 */
	public String getText() {
		return text;
	}
}