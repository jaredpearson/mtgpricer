package mtgpricer.bridge;

import mtgpricer.Display;

/**
 * Represents an operation that can be performed within the Bridge. 
 * @author jared.pearson
 */
public interface BridgeOperation {
	public void execute(Display display) throws Exception;
}
