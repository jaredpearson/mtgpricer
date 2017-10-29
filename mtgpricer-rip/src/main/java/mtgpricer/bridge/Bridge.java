package mtgpricer.bridge;

import mtgpricer.Display;

/**
 * Processes and moves the price data information that is ripped into the storage that
 * is used by the website for information. This could be just updating values in the site or 
 * more complex operations like collecting statistics.
 * @author jared.pearson
 */
public class Bridge {
	private final BridgeOperation updatePriceTool;
	private final BridgeOperation updateStatsTool;
	
	public Bridge(BridgeOperation updatePriceTool, BridgeOperation updateStatsTool) {
		assert updatePriceTool != null;
		assert updateStatsTool != null;
		this.updatePriceTool = updatePriceTool;
		this.updateStatsTool = updateStatsTool;
	}
	
	/**
	 * Executes the operations configured for the bridge.
	 */
	public void execute(Display display) throws Exception {
		assert display != null;
		updatePriceTool.execute(display);
		updateStatsTool.execute(display);
	}
}
