package mtgpricer.rip;

import mtgpricer.bridge.Bridge;
import mtgpricer.rip.RipRequestProcessor.ProcessRipRequestListener;

/**
 * Factory for creating {@link RipRequestProcessor} instances
 * @author jared.pearson
 */
public class RipRequestProcessorFactory {
	private final RipProcessor ripProcessor;
	private final Bridge bridge;
	
	public RipRequestProcessorFactory(
			RipProcessor ripProcessor,
			Bridge bridge) {
		assert ripProcessor != null;
		assert bridge != null;
		this.ripProcessor = ripProcessor;
		this.bridge = bridge;
	}
	
	/**
	 * Creates a new rip request processor
	 * @param ripRequestId the id of the rip request to process
	 * @param listener a listener so that callers can be notified of changes
	 * @return the new rip request processor
	 */
	public RipRequestProcessor create(long ripRequestId, ProcessRipRequestListener listener) {
		return new RipRequestProcessor(
				ripProcessor, 
				bridge,
				ripRequestId,
				listener);
	}
}