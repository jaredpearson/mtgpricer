package mtgpricer;

/**
 * Represents the entry point of a command line application that can be executed.
 * @author jared.pearson
 */
public interface CommandLineTool {
	/**
	 * Runs the command line tool with the given arguments.
	 */
	public void run(String[] args) throws Exception;
}
