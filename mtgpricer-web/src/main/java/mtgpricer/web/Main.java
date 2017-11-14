package mtgpricer.web;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;

import javax.servlet.DispatcherType;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.session.HashSessionManager;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.DispatcherServlet;

public class Main {
	private static final Logger logger = LoggerFactory.getLogger(Main.class);
	private static final int DEFAULT_PORT = 8080;
	
	public static void main(String[] args) throws Exception {
		final Options options = parseArgs(args);
		final WebApplicationContext context = createApplicationContext();
		
		final int port = options.getPort() != null ? options.getPort() : DEFAULT_PORT; 
		logger.debug("Starting server on port {}", port);
		final Server server = new Server(port); 
		server.setHandler(createServletContextHandler(options, context));
		server.start();
		logger.info("Started server on port {}", port);
		server.join();
	}
	
	private static ServletContextHandler createServletContextHandler(final Options options, final WebApplicationContext context) throws IOException {
		final WebAppContext contextHandler = new WebAppContext();
		contextHandler.setErrorHandler(null);
        contextHandler.setContextPath("/");
        contextHandler.addServlet(new ServletHolder(new DispatcherServlet(context)), "/*");
        contextHandler.addFilter(new FilterHolder(new DelegatingFilterProxy("springSecurityFilterChain")), "/*", EnumSet.allOf(DispatcherType.class));
        contextHandler.addEventListener(new ContextLoaderListener(context));
        contextHandler.setResourceBase(new ClassPathResource("webapp").getURI().toString());
        if (options.getSessionStoreDirectory() != null) {
        	    contextHandler.setSessionHandler(createSessionHandler(options.getSessionStoreDirectory()));
        }
        return contextHandler;
	}
	
	private static WebApplicationContext createApplicationContext() {
		final AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
		context.setConfigLocation("mtgpricer.web.config");
		return context;
	}

	private static SessionHandler createSessionHandler(String sessionStoreDirectory) throws IOException {
		final HashSessionManager hashSessionManager = new HashSessionManager();
		hashSessionManager.setStoreDirectory(new File(sessionStoreDirectory));
		hashSessionManager.setSavePeriod(30);
		return new SessionHandler(hashSessionManager);
	}

	private static Options parseArgs(String[] args) {
		String sessionStoreDirectory = null;
		Integer port = null;
		for (int index = 0; index < args.length; index++) {
			final String arg = args[index];
			if ("--sessionStoreDirectory".equals(arg)) {
				if (index + 1 == args.length) {
					System.err.println("Argument sessionStoreDirectory should be followed by a directory");
					System.exit(1);
				}
				sessionStoreDirectory = args[++index];
			} else if ("--port".equals(arg)) {
				if (index + 1 == args.length) {
					System.err.println("Argument port should be followed by a number");
					System.exit(1);
				}
				final String portValue = args[++index];
				port = Integer.parseInt(portValue);
			} else {
				System.err.println("Unknown value specified on command line: " + arg);
				System.exit(1);
			}
		}
		return new Options(sessionStoreDirectory, port);
	}

	private static final class Options {
		private final String sessionStoreDirectory;
		private final Integer port;

		public Options(String sessionStoreDirectory, Integer port) {
			this.sessionStoreDirectory = sessionStoreDirectory;
			this.port = port;
		}

		public Integer getPort() {
			return port;
		}

		public String getSessionStoreDirectory() {
			return sessionStoreDirectory;
		}
	}
}