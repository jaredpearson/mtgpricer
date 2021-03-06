package mtgpricer.rip;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * A request made to rip the price information from a site.
 * @author jared.pearson
 */
public class RipRequest {
	private final long id;
	private final Date startDate;
	private final Date finishDate;
	private final List<RipRequestLogLine> logLines;
	private final Integer progress;
	private final Integer estimatedTotal;
	
	public RipRequest(
			long id,
			Date startDate,
			Date finishDate,
			List<? extends RipRequestLogLine> logLines,
			Integer progress,
			Integer estimatedTotal) {
		assert startDate != null;
		this.id = id;
		this.startDate = copyDate(startDate);
		this.finishDate = copyDate(finishDate);
		this.logLines = (logLines == null) ? Collections.<RipRequestLogLine>emptyList() : Collections.unmodifiableList(logLines);
		this.progress = progress;
		this.estimatedTotal = estimatedTotal;
	}
	
	public long getId() {
		return id;
	}
	
	public Date getStartDate() {
		return copyDate(startDate);
	}

	public Date getFinishDate() {
		return copyDate(finishDate);
	}
	
	public List<RipRequestLogLine> getLogLines() {
		return logLines;
	}

	public Integer getProgress() {
		return progress;
	}

	public Integer getEstimatedTotal() {
		return estimatedTotal;
	}

	private static Date copyDate(Date finished) {
		return finished == null ? null : new Date(finished.getTime());
	}
	
}