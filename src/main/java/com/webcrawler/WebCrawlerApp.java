package com.webcrawler;

import javax.swing.JFrame;

import com.webcrawler.ui.Crawler;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;



import org.slf4j.Logger;

import org.slf4j.LoggerFactory;


@SpringBootApplication
public class WebCrawlerApp extends JFrame {
    private static final Logger log = LoggerFactory.getLogger(WebCrawlerApp.class);

    public static void main(String[] args) {

        ConfigurableApplicationContext context = new SpringApplicationBuilder(
                WebCrawlerApp.class).headless(false).run(args);

        Crawler crawlerFrame = context.getBean(Crawler.class);
        crawlerFrame.setVisible(true);
        log.info("==================STARTED==================");
    }

}