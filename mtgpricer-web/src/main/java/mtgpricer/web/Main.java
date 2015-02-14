package mtgpricer.web;

import java.io.IOException;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

public class Main {
	private static final Logger logger = LoggerFactory.getLogger(Main.class);
	private static final int DEFAULT_PORT = 8080;
	
	public static void main(String[] args) throws Exception {
		final WebApplicationContext context = createApplicationContext();
		
		logger.debug("Starting server on port {}", DEFAULT_PORT);
		final Server server = new Server(DEFAULT_PORT); 
		server.setHandler(createServletContextHandler(context));
		server.start();
		logger.info("Started server on port {}", DEFAULT_PORT);
		server.join();
	}
	
	private static ServletContextHandler createServletContextHandler(WebApplicationContext context) throws IOException {
		final ServletContextHandler contextHandler = new ServletContextHandler();
		contextHandler.setErrorHandler(null);
        contextHandler.setContextPath("/");
        contextHandler.addServlet(new ServletHolder(new DispatcherServlet(context)), "/*");
        contextHandler.addEventListener(new ContextLoaderListener(context));
        contextHandler.setResourceBase(new ClassPathResource("webapp").getURI().toString());
		return contextHandler;
	}
	
	private static WebApplicationContext createApplicationContext() {
		final AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
		context.setConfigLocation("mtgpricer.web.config");
		return context;
	}
}