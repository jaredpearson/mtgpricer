package mtgpricer.rip;

import mtgpricer.CommandLineTool;
import mtgpricer.CommandLineTools;
import mtgpricer.Display;
import mtgpricer.Displays;

/**
 * Command line tool for getting the latest price information
 * @author jared.pearson
 */
public class Main implements CommandLineTool {
	private final RipProcessor ripProcessor;
	
	public Main(RipProcessor ripProcessor) {
		assert ripProcessor != null;
		this.ripProcessor = ripProcessor;
	}
	
	public void run(String[] args) throws Exception {
		final Display display = Displays.createForPrintStream(System.out);
		ripProcessor.rip(display);
	}
	
	public static void main(String[] args) throws Exception {
		CommandLineTools.run(Main.class, args);
	}
	
}
