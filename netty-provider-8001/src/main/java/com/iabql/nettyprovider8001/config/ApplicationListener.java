package com.iabql.nettyprovider8001.config;

import org.springframework.context.annotation.Configuration;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
@Configuration
public class ApplicationListener implements ServletContextListener {

    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
        System.out.println("-------服务器停止-------");
    }
    @Override
    public void contextInitialized(ServletContextEvent arg0) {
        System.out.println("-------服务器启动-------");
    }
}

