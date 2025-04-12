package com.example.config;

import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

public class AppInitializer implements WebApplicationInitializer {

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {

        // Root application context (equivalent to applicationContext.xml)
        AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
        rootContext.setConfigLocation("com.example.config"); // package with @Configuration classes

        servletContext.addListener(new ContextLoaderListener(rootContext));

        // Dispatcher servlet context (equivalent to dispatcher-servlet.xml)
        AnnotationConfigWebApplicationContext dispatcherContext = new AnnotationConfigWebApplicationContext();
        dispatcherContext.register(WebConfig.class); // your WebConfig class

        ServletRegistration.Dynamic dispatcher = servletContext.addServlet("dispatcher",
                new DispatcherServlet(dispatcherContext));

        dispatcher.setLoadOnStartup(1);
        dispatcher.addMapping("/");
    }
}
