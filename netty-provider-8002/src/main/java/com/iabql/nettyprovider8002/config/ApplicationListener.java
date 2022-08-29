package com.iabql.nettyprovider8002.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

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
