package mtgpricer.bridge;

import mtgpricer.CommandLineTool;
import mtgpricer.CommandLineTools;
import mtgpricer.Display;
import mtgpricer.Displays;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Command line tool wrapper for the {@link Bridge}
 * @author jared.pearson
 */
public class Main implements CommandLineTool {
	private Bridge bridge;
	
	@Autowired
	public void setBridge(Bridge bridge) {
		this.bridge = bridge;
	}
	
	public void run(String[] args) throws Exception {
		final Display display = Displays.createForPrintStream(System.out);
		bridge.execute(display);
	}
	
	public static void main(String[] args) throws Exception {
		CommandLineTools.run(Main.class, args);
	}
}
