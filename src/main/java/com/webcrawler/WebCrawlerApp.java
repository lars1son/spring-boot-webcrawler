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
//    public WebCrawlerApp() {
//
//        initUI();
//    }
//
//    private void initUI() {
//
//        JButton quitButton = new JButton("Quit");
//
//        quitButton.addActionListener((ActionEvent event) -> {
//            System.exit(0);
//        });
//
//        createLayout(quitButton);
//
//        setTitle("Quit button");
//        setSize(300, 200);
//        setLocationRelativeTo(null);
//        setDefaultCloseOperation(EXIT_ON_CLOSE);
//    }
//
//    private void createLayout(JComponent... arg) {
//
//        Container pane = getContentPane();
//        GroupLayout gl = new GroupLayout(pane);
//        pane.setLayout(gl);
//
//        gl.setAutoCreateContainerGaps(true);
//
//        gl.setHorizontalGroup(gl.createSequentialGroup()
//                .addComponent(arg[0])
//        );
//
//        gl.setVerticalGroup(gl.createSequentialGroup()
//                .addComponent(arg[0])
//        );
//    }
//
//    public static void main(String[] args) {
//
//        ConfigurableApplicationContext ctx = new SpringApplicationBuilder(WebCrawlerApp.class)
//                .headless(false).run(args);
//
//        EventQueue.invokeLater(() -> {
//            WebCrawlerApp ex = ctx.getBean(WebCrawlerApp.class);
//            ex.setVisible(true);
//        });
//    }
}