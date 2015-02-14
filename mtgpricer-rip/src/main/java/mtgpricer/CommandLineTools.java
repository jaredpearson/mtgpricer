package mtgpricer;

import org.springframework.context.ConfigurableApplicationContext;

/**
 * Utility methods for {@link CommandLineTool} instances
 * @author jared.pearson
 */
public class CommandLineTools {
	
	private CommandLineTools() {
	}
	
	/**
	 * Runs the command line tool of the given class with the given args.
	 * @param toolClass The class of the tool to execute. this should never be null
	 * @param args The arguments passed to the tool when executed
	 * @throws Exception thrown by the tool
	 */
	public static void run(Class<? extends CommandLineTool> toolClass, String[] args) throws Exception {
		assert toolClass != null;
		final ConfigurableApplicationContext context = SpringApplicationContextFactory.create();
		try {
			context.getBean(toolClass).run(args != null ? args : new String[0]);
		} finally {
			context.close();
		}
	}
}
