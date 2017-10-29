package mtgpricer.rip;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import mtgpricer.Display;
import mtgpricer.Displays;
import mtgpricer.bridge.Bridge;

/**
 * Processes a rip request
 * @author jared.pearson
 */
public class RipRequestProcessor implements Runnable {
	private static final Logger logger = Logger.getLogger(RipRequestProcessor.class.getName());
	private final RipProcessor ripProcessor;
	private final Bridge bridge;
	private final long id;
	private final ProcessRipRequestListener listener;
	
	public RipRequestProcessor(
			RipProcessor ripProcessor, 
			Bridge bridge,
			long ripRequestId,
			ProcessRipRequestListener listener) {
		assert ripProcessor != null;
		assert bridge != null;
		this.ripProcessor = ripProcessor;
		this.bridge = bridge;
		this.id = ripRequestId;
		this.listener = listener != null ? listener : new ProcessRipRequestListenerBase(){};
	}
	
	@Override
	public void run() {
		final Display display = Displays.createForPrintStream(System.out);
		
		try {
			ripProcessor.rip(display);
		} catch(IOException exc) {
			logger.log(Level.WARNING, "Failed while ripping: " + id, exc);
			listener.onFailed("Failed while ripping: " + id, exc);
			return;
		}
		
		try {
			bridge.execute(display);
		} catch(Exception exc) {
			logger.log(Level.WARNING, "Failed while bridging: " + id, exc);
			listener.onFailed("Failed while bridging: " + id, exc);
			return;
		}
		
		listener.onFinished();
	}
	
	/**
	 * Listener for events that occur when processing the rip request
	 * @author jared.pearson
	 */
	public interface ProcessRipRequestListener {
		void onFinished();
		void onFailed(String message, Throwable throwable);
	}
	
	/**
	 * Base class that implements all methods of the {@link ProcessRipRequestListener}. All methods provide
	 * a no-op and can be safely overridden.
	 * @author jared.pearson
	 */
	public static abstract class ProcessRipRequestListenerBase implements ProcessRipRequestListener {
		/**
		 * Provides a no-op; feel free to override.
		 */
		public void onFinished() {
			// no-op
		}
		
		/**
		 * Provides a no-op; feel free to override.
		 */
		public void onFailed(String message, Throwable throwable) {
			// no-op
		}
	}
}