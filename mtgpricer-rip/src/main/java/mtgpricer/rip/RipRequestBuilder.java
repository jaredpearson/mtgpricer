package mtgpricer.rip;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Builder for instances of {@link RipRequest}
 * @author jared.pearson
 */
class RipRequestBuilder {
	private long id = -1;
	private Date startDate = null;
	private Date finishDate = null;
	private List<RipRequestLogLine> logLines = new ArrayList<RipRequestLogLine>();
	
	/**
	 * Creates a new {@link RipRequest} instance
	 */
	public RipRequest build() {
		return new RipRequest(id, startDate, finishDate, logLines);
	}
	
	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public void setStartDate(Date started) {
		this.startDate = started;
	}
	
	public void setFinishDate(Date finished) {
		this.finishDate = finished;
	}
	
	public void addLogLine(RipRequestLogLine logLine) {
		if (logLine != null) {
			this.logLines.add(logLine);
		}
	}
}