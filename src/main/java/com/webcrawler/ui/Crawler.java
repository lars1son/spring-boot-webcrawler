package com.webcrawler.ui;


import com.webcrawler.model.NodesStageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.*;
import javax.swing.*;
import javax.swing.table.*;

/**
 * Created by Артем on 26.01.2019.
 */
@Component
public class Crawler extends JFrame {
    private static final Logger log = LoggerFactory.getLogger(Crawler.class);

     private static final String[] MAX_URLS =
            {"50", "100", "500", "1000"};
    private static final String[] MAX_THREADS =
            {"5", "10", "50", "100"};
     private HashMap disallowListCache = new HashMap();

    private JTextField startTextField;
    private JComboBox maxComboBox;
    private JComboBox maxThreadComboBox;

    private JTextField logTextField;
    private JTextField searchTextField;
    private JCheckBox caseCheckBox;
    private JButton searchButton;
     private JLabel crawlingLabel2;
    private JLabel crawledLabel2;
    private JLabel toCrawlLabel2;
    private JProgressBar progressBar;
    private JLabel matchesLabel2;

    // List of matches
    private JTable matchTable;
    // List of all urls were watched
    private JTable watchedTable;
    // List of url are scanning
    private JTable scanningTable;

    private boolean crawling;
    // File for additional urls output
    private PrintWriter logFileWriter;

     public Crawler() {

        setTitle("Search Crawler");

        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setUndecorated(true);
        //Operate window-closing action
         addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                actionExit();
            }
        });
        //add menu File
       JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        JMenuItem fileExitMenuItem = new JMenuItem("Exit",
                KeyEvent.VK_X);
        fileExitMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                actionExit();
            }
        });
        fileMenu.add(fileExitMenuItem);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);
        // add serch panel.
        JPanel searchPanel = new JPanel();
        GridBagConstraints constraints;
        GridBagLayout layout = new GridBagLayout();
        searchPanel.setLayout(layout);
        JLabel startLabel = new JLabel("Start URL:");
        constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.EAST;
        constraints.insets = new Insets(5, 5, 0, 0);
        layout.setConstraints(startLabel, constraints);
        searchPanel.add(startLabel);
        startTextField = new JTextField();
        constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.insets = new Insets(5, 5, 0, 5);
        layout.setConstraints(startTextField, constraints);
        searchPanel.add(startTextField);

        JLabel maxLabel = new JLabel("Max URLs to Crawl:");
        JLabel maxThreadLabel = new JLabel("Max threads to Crawl:");
        constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.EAST;
        constraints.insets = new Insets(5, 5, 0, 0);
        layout.setConstraints(maxLabel, constraints);
        searchPanel.add(maxLabel);

        maxComboBox = new JComboBox(MAX_URLS);
        maxComboBox.setEditable(true);
        constraints = new GridBagConstraints();
        constraints.insets = new Insets(5, 5, 0, 0);
        layout.setConstraints(maxComboBox, constraints);
        searchPanel.add(maxComboBox);



        constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(0, 10, 0, 0);

        layout.setConstraints(maxThreadLabel, constraints);
        searchPanel.add(maxThreadLabel);
        maxThreadComboBox = new JComboBox(MAX_THREADS);
        maxThreadComboBox.setEditable(true);
        constraints = new GridBagConstraints();
        constraints.insets = new Insets(5, 5, 0, 0);
        layout.setConstraints(maxThreadComboBox, constraints);
        searchPanel.add(maxThreadComboBox);


        JLabel blankLabel = new JLabel();
        constraints = new GridBagConstraints();
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        layout.setConstraints(blankLabel, constraints);
        searchPanel.add(blankLabel);
        JLabel logLabel = new JLabel("Matches Log File:");
        constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.EAST;
        constraints.insets = new Insets(5, 5, 0, 0);
        layout.setConstraints(logLabel, constraints);
        searchPanel.add(logLabel);
        String file =
                System.getProperty("user.dir") +
                        System.getProperty("file.separator") +
                        "crawler.log";
        logTextField = new JTextField(file);
        constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.insets = new Insets(5, 5, 0, 5);
        layout.setConstraints(logTextField, constraints);
        searchPanel.add(logTextField);
        JLabel searchLabel = new JLabel("Search String:");
        constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.EAST;
        constraints.insets = new Insets(5, 5, 0, 0);
        layout.setConstraints(searchLabel, constraints);
        searchPanel.add(searchLabel);
        searchTextField = new JTextField();
        constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(5, 5, 0, 0);
        constraints.gridwidth = 2;
        constraints.weightx = 1.0d;

        layout.setConstraints(searchTextField, constraints);
        searchPanel.add(searchTextField);
        caseCheckBox = new JCheckBox("Case Sensitive");
        constraints = new GridBagConstraints();
        constraints.insets = new Insets(5, 5, 0, 5);
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        layout.setConstraints(caseCheckBox, constraints);
        searchPanel.add(caseCheckBox);
        searchButton = new JButton("Search");
        searchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                actionSearch();
            }
        });
        constraints = new GridBagConstraints();
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.insets = new Insets(5, 5, 5, 5);
        layout.setConstraints(searchButton, constraints);
        searchPanel.add(searchButton);
        JSeparator separator = new JSeparator();
        constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.insets = new Insets(5, 5, 5, 5);
        layout.setConstraints(separator, constraints);
        searchPanel.add(separator);
        JLabel crawlingLabel1 = new JLabel("Crawling:");
        constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.EAST;
        constraints.insets = new Insets(5, 5, 0, 0);
        layout.setConstraints(crawlingLabel1, constraints);
        searchPanel.add(crawlingLabel1);
        crawlingLabel2 = new JLabel();
        crawlingLabel2.setFont(
                crawlingLabel2.getFont().deriveFont(Font.PLAIN));
        constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.insets = new Insets(5, 5, 0, 5);
        layout.setConstraints(crawlingLabel2, constraints);
        searchPanel.add(crawlingLabel2);
        JLabel crawledLabel1 = new JLabel("Crawled URLs:");
        constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.EAST;
        constraints.insets = new Insets(5, 5, 0, 0);
        layout.setConstraints(crawledLabel1, constraints);
        searchPanel.add(crawledLabel1);
        crawledLabel2 = new JLabel();
        crawledLabel2.setFont(
                crawledLabel2.getFont().deriveFont(Font.PLAIN));
        constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.insets = new Insets(5, 5, 0, 5);

        layout.setConstraints(crawledLabel2, constraints);
        searchPanel.add(crawledLabel2);
        JLabel toCrawlLabel1 = new JLabel("URLs to Crawl:");
        constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.EAST;
        constraints.insets = new Insets(5, 5, 0, 0);
        layout.setConstraints(toCrawlLabel1, constraints);
        searchPanel.add(toCrawlLabel1);
        toCrawlLabel2 = new JLabel();
        toCrawlLabel2.setFont(
                toCrawlLabel2.getFont().deriveFont(Font.PLAIN));
        constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.insets = new Insets(5, 5, 0, 5);
        layout.setConstraints(toCrawlLabel2, constraints);
        searchPanel.add(toCrawlLabel2);
        JLabel progressLabel = new JLabel("Crawling Progress:");
        constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.EAST;
        constraints.insets = new Insets(5, 5, 0, 0);
        layout.setConstraints(progressLabel, constraints);
        searchPanel.add(progressLabel);
        progressBar = new JProgressBar();
        progressBar.setMinimum(0);
        progressBar.setStringPainted(true);
        constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.insets = new Insets(5, 5, 0, 5);
        layout.setConstraints(progressBar, constraints);
        searchPanel.add(progressBar);
        JLabel matchesLabel1 = new JLabel("Search Matches:");
        constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.EAST;
        constraints.insets = new Insets(5, 5, 10, 0);
        layout.setConstraints(matchesLabel1, constraints);
        searchPanel.add(matchesLabel1);
        matchesLabel2 = new JLabel();
        matchesLabel2.setFont(
                matchesLabel2.getFont().deriveFont(Font.PLAIN));
        constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.insets = new Insets(5, 5, 10, 5);
        layout.setConstraints(matchesLabel2, constraints);
        searchPanel.add(matchesLabel2);
        // set up table of mathes
        matchTable = new JTable(new DefaultTableModel(new Object[][]{},
                new String[]{"URL"}) {
            public boolean isCellEditable(int row, int column) {
                return false;

            }
        });

// set up table of watched urls
        watchedTable = new JTable(new DefaultTableModel(new Object[][]{},
                new String[]{"URL Watched"}) {
            public boolean isCellEditable(int row, int column) {
                return false;

            }
        });
         // set up table of urls are scanning

         scanningTable = new JTable(new DefaultTableModel(new Object[][]{},
                new String[]{"Scanning URLs"}) {
            public boolean isCellEditable(int row, int column) {
                return false;

            }
        });

         // set up panels for tables

         JPanel scanningPanel = new JPanel();
        scanningPanel.setBorder(BorderFactory.createTitledBorder("Scanning"));
        scanningPanel.setLayout(new BorderLayout());
        scanningPanel.add(new JScrollPane(scanningTable), BorderLayout.CENTER);

        JPanel matchesPanel = new JPanel();
        matchesPanel.setBorder(BorderFactory.createTitledBorder("Matches"));
        matchesPanel.setLayout(new BorderLayout());

        matchesPanel.add(new JScrollPane(watchedTable), BorderLayout.EAST);
        matchesPanel.add(new JScrollPane(matchTable), BorderLayout.WEST);





        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(searchPanel, BorderLayout.NORTH);

        getContentPane().add(scanningPanel, BorderLayout.EAST);
        getContentPane().add(matchesPanel, BorderLayout.WEST);

        log.info("UI Created");
    }

    // Exit
    private void actionExit() {
        log.info("Exit");
        System.exit(0);
    }


    // operate click search/stop.
    private void actionSearch() {

         if (crawling) {
            log.info("Action: STOP");
            crawling = false;
            return;
        }
        log.info("Action: SEARCH");
        ArrayList errorList = new ArrayList();

        String startUrl = startTextField.getText().trim();
        if (startUrl.length() < 1) {
            errorList.add("Missing Start URL.");
        }
         else if (verifyUrl(startUrl) == null) {
            errorList.add("Invalid Start URL.");
        }

        int maxUrls = 0;
        String maxUrl = ((String) maxComboBox.getSelectedItem()).trim();
        int maxThreads = 0;
        String maxThread = ((String) maxThreadComboBox.getSelectedItem()).trim();
        if (maxUrl.length() > 0 || maxThread.length() > 0) {
            try {
                maxUrls = Integer.parseInt(maxUrl);
                maxThreads = Integer.parseInt(maxThread);
            } catch (NumberFormatException e) {
            }
            if (maxUrls < 1 || maxThreads < 1) {
                errorList.add("Invalid Max value.");
            }
        }

        // Check for file existing
        String logFile = logTextField.getText().trim();
        if (logFile.length() < 1) {
            errorList.add("Missing Matches Log File.");
        }

        // check "search" string
        String searchString = searchTextField.getText().trim();
        if (searchString.length() < 1) {
            errorList.add("Missing Search String.");
        }

        if (errorList.size() > 0) {
            StringBuffer message = new StringBuffer();
             for (int i = 0; i < errorList.size(); i++) {
                message.append(errorList.get(i));
                if (i + 1 < errorList.size()) {
                    message.append("\n");
                }
            }
            showError(message.toString());
            return;
        }

        startUrl = removeWwwFromUrl(startUrl);

        search(logFile, startUrl, maxUrls, maxThreads, searchString);
    }


    private void search(final String logFile, final String startUrl,
                        final int maxUrls, final int maxThreads, final String searchString) {
        log.info("Start search");

        Thread thread = new Thread() {
            public void run() {

                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

                startTextField.setEnabled(false);
                maxComboBox.setEnabled(false);

                logTextField.setEnabled(false);
                searchTextField.setEnabled(false);
                caseCheckBox.setEnabled(false);

                searchButton.setText("Stop");

                matchTable.setModel(new DefaultTableModel(new Object[][]{},
                        new String[]{"URL"}) {
                    public boolean isCellEditable(int row, int column) {
                        return false;
                    }
                });
                watchedTable.setModel(new DefaultTableModel(new Object[][]{},
                        new String[]{"Watched URLs"}) {
                    public boolean isCellEditable(int row, int column) {
                        return false;
                    }
                });
                updateStats(startUrl, 0, 0, maxUrls);

                try {
                    logFileWriter = new PrintWriter(new FileWriter(logFile));
                } catch (Exception e) {

                    showError("Unable to open matches log file.");
                    return;
                }

                crawling = true;

                crawl(startUrl, maxUrls, maxThreads,
                        searchString, caseCheckBox.isSelected());

                crawling = false;

                try {
                    logFileWriter.close();
                } catch (Exception e) {
                    showError("Unable to close matches log file.");
                }

                crawlingLabel2.setText("Done");
                 startTextField.setEnabled(true);
                maxComboBox.setEnabled(true);

                logTextField.setEnabled(true);
                searchTextField.setEnabled(true);
                caseCheckBox.setEnabled(true);
                 searchButton.setText("Search");
                 setCursor(Cursor.getDefaultCursor());
                 if (matchTable.getRowCount() == 0) {
                    JOptionPane.showMessageDialog(Crawler.this,
                            "Your Search String was not found. Please try another.",
                            "Search String Not Found",
                            JOptionPane.WARNING_MESSAGE);
                }
            }
        };


        thread.start();
    }

    // Show error message
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error",
                JOptionPane.ERROR_MESSAGE);
    }

    // update Stats
    private synchronized void updateStats(
            String crawling, int crawled, int toCrawl, int maxUrls) {
        log.info("Update stats");
        crawlingLabel2.setText(crawling);

        crawledLabel2.setText("" + crawled);
        toCrawlLabel2.setText("" + toCrawl);
         if (maxUrls == -1) {
            progressBar.setMaximum(crawled + toCrawl);
        } else {
            progressBar.setMaximum(maxUrls);
        }
        progressBar.setValue(crawled);
        matchesLabel2.setText("" + matchTable.getRowCount());
    }


    private synchronized void addRecordToTable(String url, boolean matched) {

        if (matched) {
            final DefaultTableModel  model = (DefaultTableModel) matchTable.getModel();
            try {
                // Add url to file
                logFileWriter.println(url);
                    addToModel(model,url);
            } catch (Exception e) {
                showError("Unable to log match.");
            }
        }
        final DefaultTableModel model = (DefaultTableModel) watchedTable.getModel();
        addToModel(model,url);


    }
    private void addToModel(DefaultTableModel  model, String url){
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                model.addRow(new Object[]{url});

            }
        });
    }

    private synchronized void addToScanningTable(String url) {
        DefaultTableModel model = (DefaultTableModel) scanningTable.getModel();
        boolean addRow = true;
        for (int i = 0; i < model.getRowCount(); i++) {
            if (((String) model.getValueAt(i, 0)).equals(url)) {
                model.removeRow(i);
                addRow = false;
            }
        }
        if (addRow) {
            model.addRow(new Object[]{url});
        }

    }

    // check format of  URL.
    private URL verifyUrl(String url) {
        // Allow only https
        if (!url.toLowerCase().startsWith("https://"))
            return null;

        URL verifiedUrl = null;
        try {
            verifiedUrl = new URL(url);
        } catch (Exception e) {
            return null;
        }
        return verifiedUrl;
    }

    // Check if robot allows access to URL.
    private boolean isRobotAllowed(URL url) {
        String host = url.getHost().toLowerCase();

        ArrayList disallowList =
                (ArrayList) disallowListCache.get(host);

        if (disallowList == null) {
            disallowList = new ArrayList();
            try {
                URL robotsFileUrl =
                        new URL("http://" + host + "/robots.txt");

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
                        robotsFileUrl.openStream()));

                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    if (line.indexOf("Disallow:") == 0) {
                        String disallowPath =
                                line.substring("Disallow:".length());

                        int commentIndex = disallowPath.indexOf("#");
                        if (commentIndex != -1) {
                            disallowPath =
                                    disallowPath.substring(0, commentIndex);
                        }

                        disallowPath = disallowPath.trim();

                        disallowList.add(disallowPath);
                    }
                }

                disallowListCache.put(host, disallowList);
            } catch (Exception e) {
                return true;
            }
        }
        String file = url.getFile();
        for (int i = 0; i < disallowList.size(); i++) {
            String disallow = (String) disallowList.get(i);
            if (file.startsWith(disallow)) {
                return false;
            }
        }
        return true;
    }

    // Download page
    private String downloadPage(URL pageUrl) {
        try {
             BufferedReader reader =
                    new BufferedReader(new InputStreamReader(
                            pageUrl.openStream()));

            String line;
            StringBuffer pageBuffer = new StringBuffer();
            while ((line = reader.readLine()) != null) {

                pageBuffer.append(line);
            }
            return pageBuffer.toString();

        } catch (Exception e) {
        }
        return null;
    }


    private String removeWwwFromUrl(String url) {
        int index = url.indexOf("://www.");
        if (index != -1) {
            return url.substring(0, index + 3) +
                    url.substring(index + 7);
        }
        return (url);
    }

    // Get links from page
    private ArrayList retrieveLinks(
            URL pageUrl, String pageContents, HashSet crawledList) {
         Pattern p =
                Pattern.compile("<a\\s+href\\s*=\\s*\"?(.*?)[\"|>]",
                        Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(pageContents);
         ArrayList<String> linkList = new ArrayList();
        while (m.find()) {
            String link = m.group(1).trim();
             if (link.length() < 1) {
                continue;
            }
             if (link.charAt(0) == '#') {
                continue;
            }
             if (link.indexOf("mailto:") != -1) {
                continue;
            }
             if (link.toLowerCase().indexOf("javascript") != -1) {
                continue;
            }
             if (link.indexOf("://") == -1) {
                 if (link.charAt(0) == '/') {
                    link = "http://" + pageUrl.getHost() + link;

                 } else {
                    String file = pageUrl.getFile();
                    if (file.indexOf('/') == -1) {
                        link = "http://" + pageUrl.getHost() + "/" + link;
                    } else {
                        String path =
                                file.substring(0, file.lastIndexOf('/') + 1);
                        link = "http://" + pageUrl.getHost() + path + link;
                    }
                }
            }
             int index = link.indexOf('#');
            if (index != -1) {
                link = link.substring(0, index);
            }
             link = removeWwwFromUrl(link);
             URL verifiedLink = verifyUrl(link);
            if (verifiedLink == null) {
                continue;
            }

             if (crawledList.contains(link)) {
                continue;
            }
             linkList.add(link);
        }
        return (linkList);
    }


    private boolean searchStringMatches(
            String pageContents, String searchString,
            boolean caseSensitive) {
        String searchContents = pageContents;
        if (!caseSensitive) {
            searchContents = pageContents.toLowerCase();
        }

        Pattern p = Pattern.compile("[\\s]+");
        String[] parts = p.split(searchString);

        for (int i = 0; i < parts.length; i++) {
            if (caseSensitive) {
                if (searchContents.indexOf(parts[i]) == -1) {
                    return false;
                }
            } else {
                if (searchContents.indexOf(parts[i].toLowerCase()) == -1) {
                    return false;
                }
            }
        }
        return true;
    }


    public void crawl(String startUrl, int maxUrls, int maxThreads,   String searchString, boolean caseSensitive) {
       log.info("Start crawling");
        HashSet crawledList = new HashSet();
        CopyOnWriteArrayList<String> toCrawlList = new CopyOnWriteArrayList();
        toCrawlList.add(startUrl);
        ThreadGroup nodesThreadGroup = new ThreadGroup("Children thread Group");
        NodesStageInfo nodesStageInfo = new NodesStageInfo();

        while (crawling && toCrawlList.size() > 0) {

            ///////////////////////////////////////////////////
            if (!operateNextUrl(maxUrls, crawledList.size())) {
                break;
            }

            if (nodesStageInfo.isUpdateStageNeeded()) {
                nodesStageInfo.setStageCrawlList(new CopyOnWriteArrayList<>(toCrawlList));
                log.info("New node-stage created! " + nodesStageInfo.getAllNodesCount() + " elements..");
            }
            String url = nodesStageInfo.getFirstNode();

            URL verifiedUrl = verifyUrl(url);
            if (!isRobotAllowed(verifiedUrl)) {
                continue;
            }
            updateStats(url, crawledList.size(), toCrawlList.size(), maxUrls);
            crawledList.add(url);

            Thread downloadThread = new Thread(nodesThreadGroup, "Name: " + url) {
                @Override
                public void run() {
                    log.info("THREAD " + this.getName() + " started");
                    addToScanningTable(url);
                    String pageContents = downloadPage(verifiedUrl);

                    log.info("Downloaded page content from: " + url);
//                    log.info("====================================== + \n" + pageContents);
                    if (pageContents != null && pageContents.length() > 0) {

                        ArrayList<String> links = retrieveLinks(verifiedUrl, pageContents, crawledList );
                        toCrawlList.remove(0);
                        toCrawlList.addAll(links);
                        log.info("New links were found in Thread: " + this.getName());
                        boolean matched = false;
                        if (searchStringMatches(pageContents, searchString, caseSensitive)) {
                            matched = true;
                            log.info("New match found, LINK: " + url);
                        }

                        addRecordToTable(url, matched);

                    }


                    updateStats(url, crawledList.size(), toCrawlList.size(), maxUrls);
                    nodesStageInfo.updateUsedNodesCount();
                    log.info("THREAD " + this.getName() + " finished!");
                    addToScanningTable(url);
                }
            };
            downloadThread.start();
            ///Ждать пока в очереди потоков не освободится место для нового потока или пока выполнится уровень нодов до конца
            while (nodesThreadGroup.activeCount() > 0 && (nodesThreadGroup.activeCount() == maxThreads || nodesStageInfo.waitForStageFinish(nodesThreadGroup.activeCount()))) {
                try {
                    log.info("Waiting... " + nodesThreadGroup.activeCount() + "/" + maxThreads + " are active");
                    Thread.currentThread().sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean operateNextUrl(int maxUrls, int crawledListSize) {
        if (maxUrls != -1) {
            if (crawledListSize == maxUrls) {
                return false;
            }
        }
        return true;
    }
}