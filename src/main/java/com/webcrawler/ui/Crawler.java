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

    // Максимальное количество раскрывающихся значений URL.
    private static final String[] MAX_URLS =
            {"50", "100", "500", "1000"};
    private static final String[] MAX_THREADS =
            {"5", "10", "50", "100"};
    // Кэш-память для списка ограничений робота.
    private HashMap disallowListCache = new HashMap();
    // Элементы управления графического интерфейса панели Search.
    private JTextField startTextField;
    private JComboBox maxComboBox;
    private JComboBox maxThreadComboBox;
    private JCheckBox limitCheckBox;
    private JTextField logTextField;
    private JTextField searchTextField;
    private JCheckBox caseCheckBox;
    private JButton searchButton;
    // Элементы управления графического интерфейса панели Stats.
    private JLabel crawlingLabel2;
    private JLabel crawledLabel2;
    private JLabel toCrawlLabel2;
    private JProgressBar progressBar;
    private JLabel matchesLabel2;
    private JLabel scanningLabel2;
    // Список соответствий.
    private JTable matchTable;
    private JTable watchedTable;
    private JTable scanningTable;
    // Флаг отображения состояния поиска.
    private boolean crawling;
    // Файл журнала для текстового вывода.
    private PrintWriter logFileWriter;

    // Конструктор для поискового червя.
    public Crawler() {
        // Установка заголовка приложения.
        setTitle("Search Crawler");
        // Установка размеров окна.
//        setSize(800, 600);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setUndecorated(true);
        // ОБработка событий по закрытию окна.
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                actionExit();
            }
        });
        // Установить меню “файл”.
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
        // Установить панель поиска.
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


        limitCheckBox =
                new JCheckBox("Limit crawling to Start URL site");
        constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(0, 10, 0, 0);
        layout.setConstraints(limitCheckBox, constraints);
        searchPanel.add(limitCheckBox);

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
        // Установить таблицу совпадений.
        matchTable = new JTable(new DefaultTableModel(new Object[][]{},
                new String[]{"URL"}) {
            public boolean isCellEditable(int row, int column) {
                return false;

            }
        });


        watchedTable = new JTable(new DefaultTableModel(new Object[][]{},
                new String[]{"URL Watched"}) {
            public boolean isCellEditable(int row, int column) {
                return false;

            }
        });
        scanningTable = new JTable(new DefaultTableModel(new Object[][]{},
                new String[]{"Scanning URLs"}) {
            public boolean isCellEditable(int row, int column) {
                return false;

            }
        });
        JPanel scanningPanel = new JPanel();
        scanningPanel.setBorder(BorderFactory.createTitledBorder("Scanning"));
        scanningPanel.setLayout(new BorderLayout());
        scanningPanel.add(new JScrollPane(scanningTable), BorderLayout.CENTER);

        // Установить панель совпадений.
        JPanel matchesPanel = new JPanel();
        matchesPanel.setBorder(BorderFactory.createTitledBorder("Matches"));
        matchesPanel.setLayout(new BorderLayout());

        matchesPanel.add(new JScrollPane(watchedTable), BorderLayout.EAST);
        matchesPanel.add(new JScrollPane(matchTable), BorderLayout.WEST);


        // Отобразить панели на дисплее.


        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(searchPanel, BorderLayout.NORTH);

        getContentPane().add(scanningPanel, BorderLayout.EAST);
        getContentPane().add(matchesPanel, BorderLayout.WEST);

    }

    // Выход из программы.
    private void actionExit() {
        System.exit(0);
    }


    // Обработать щелчок на кнопке search/stop.
    private void actionSearch() {
        // Если произведен щелчок на кнопке stop, сбросить флаг.
        if (crawling) {
            crawling = false;
            return;
        }
        ArrayList errorList = new ArrayList();
        // Проверить ввод начального адреса (URL).
        String startUrl = startTextField.getText().trim();
        if (startUrl.length() < 1) {
            errorList.add("Missing Start URL.");
        }
        // Проверить начальный URL.
        else if (verifyUrl(startUrl) == null) {
            errorList.add("Invalid Start URL.");
        }
 /* Проверить, что введено значение для максимально допустимого
 количества адресов и что это число. */
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

        // Проверить, что файл с журналом совпадений существует.
        String logFile = logTextField.getText().trim();
        if (logFile.length() < 1) {
            errorList.add("Missing Matches Log File.");
        }

        // Проверить, что введена стока для поиска.
        String searchString = searchTextField.getText().trim();
        if (searchString.length() < 1) {
            errorList.add("Missing Search String.");
        }
        // Показать ошибки, если они есть, и возврат.
        if (errorList.size() > 0) {
            StringBuffer message = new StringBuffer();
            // Объединить ошибки в одно сообщение.
            for (int i = 0; i < errorList.size(); i++) {
                message.append(errorList.get(i));
                if (i + 1 < errorList.size()) {
                    message.append("\n");
                }
            }
            showError(message.toString());
            return;
        }
        // Удалить символы "www" из начального URL, если они есть.
        startUrl = removeWwwFromUrl(startUrl);
        // Запустить поискового червя.
        search(logFile, startUrl, maxUrls, maxThreads, searchString);
    }


    private void search(final String logFile, final String startUrl,
                        final int maxUrls, final int maxThreads, final String searchString) {
        // Начать поиск в новом потоке.
        Thread thread = new Thread() {
            public void run() {
                // Отобразить песочные часы на время работы поискового червя.
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                // Заблокировать элементы управления поиска.
                startTextField.setEnabled(false);
                maxComboBox.setEnabled(false);
                limitCheckBox.setEnabled(false);
                logTextField.setEnabled(false);
                searchTextField.setEnabled(false);
                caseCheckBox.setEnabled(false);
                // Переключить кнопку поиска в состояние "Stop."
                searchButton.setText("Stop");
                // Переустановить панель Stats.
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
                // Открыть журнал совпадений.
                try {
                    logFileWriter = new PrintWriter(new FileWriter(logFile));
                } catch (Exception e) {

                    showError("Unable to open matches log file.");
                    return;
                }
                // Установить флаг поиска.
                crawling = true;
                // Выполнять реальный поиск.
                crawl(startUrl, maxUrls, maxThreads, limitCheckBox.isSelected(),
                        searchString, caseCheckBox.isSelected());
                // Сбросить флаг поиска.
                crawling = false;
                // Закрыть журнал совпадений.
                try {
                    logFileWriter.close();
                } catch (Exception e) {
                    showError("Unable to close matches log file.");
                }
                // Отметить окончание поиска.
                crawlingLabel2.setText("Done");
                // Разблокировать элементы контроля поиска.
                startTextField.setEnabled(true);
                maxComboBox.setEnabled(true);
                limitCheckBox.setEnabled(true);
                logTextField.setEnabled(true);
                searchTextField.setEnabled(true);
                caseCheckBox.setEnabled(true);
                // Переключить кнопку поиска в состояние "Search."
                searchButton.setText("Search");
                // Возвратить курсор по умолчанию.
                setCursor(Cursor.getDefaultCursor());
                // Отобразить сообщение, если строка не найдена.
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

    // Отобразить диалоговое окно с сообщением об ошибке.
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error",
                JOptionPane.ERROR_MESSAGE);
    }

    // Обновить панель stats.
    private synchronized void updateStats(
            String crawling, int crawled, int toCrawl, int maxUrls) {
        crawlingLabel2.setText(crawling);

        crawledLabel2.setText("" + crawled);
        toCrawlLabel2.setText("" + toCrawl);
        // Обновить индикатор выполнения.
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
                // Добавить URL в журнал совпадений.
                logFileWriter.println(url);
//                model.addRow(new Object[]{url});
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        model.addRow(new Object[]{url});

                    }
                });
            } catch (Exception e) {
                showError("Unable to log match.");
            }
        }
        final DefaultTableModel model = (DefaultTableModel) watchedTable.getModel();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                model.addRow(new Object[]{url});

            }
        });
//        model.addRow(new Object[]{url});


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

    // Проверить формат URL.
    private URL verifyUrl(String url) {
        // Разрешить только адреса HTTP.
        if (!url.toLowerCase().startsWith("https://"))
            return null;
        // Проверить формат URL.
        URL verifiedUrl = null;
        try {
            verifiedUrl = new URL(url);
        } catch (Exception e) {
            return null;
        }
        return verifiedUrl;
    }

    // Проверить, если робот разрешает доступ к данному URL.
    private boolean isRobotAllowed(URL url) {
        String host = url.getHost().toLowerCase();
        // Извлечь список ограничений сайта из кэш-памяти.
        ArrayList disallowList =
                (ArrayList) disallowListCache.get(host);
        // Если в кэш-памяти нет списка, загрузить его.
        if (disallowList == null) {
            disallowList = new ArrayList();
            try {
                URL robotsFileUrl =
                        new URL("http://" + host + "/robots.txt");
                // Открыть файл робота заданного URL для чтения.
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
                        robotsFileUrl.openStream()));
                // Прочитать файл робота, создать список запрещенных путей.
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    if (line.indexOf("Disallow:") == 0) {
                        String disallowPath =
                                line.substring("Disallow:".length());
 /* Просмотреть список запрещенных путей и удалить
 комментарии, если они есть. */
                        int commentIndex = disallowPath.indexOf("#");
                        if (commentIndex != -1) {
                            disallowPath =
                                    disallowPath.substring(0, commentIndex);
                        }
                        // Удалить начальные или конечные пробелы из
                        // запрещенных путей.
                        disallowPath = disallowPath.trim();
                        // Добавить запрещенные пути в список.
                        disallowList.add(disallowPath);
                    }
                }
                // Добавить новый список в кэш-память.
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

    // Загрузить страницу с заданным URL.
    private String downloadPage(URL pageUrl) {
        try {
            // Открыть соединение по заданному URL для чтения.
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(
                            pageUrl.openStream()));
            // Считать в буфер.
            String line;
            StringBuffer pageBuffer = new StringBuffer();
            while ((line = reader.readLine()) != null) {

                pageBuffer.append(line);
            }
            return pageBuffer.toString();
//            try {
//                URL yahoo = new URL("https://www.yahoo.com/");
//                DataInputStream dis = new DataInputStream(yahoo.openStream());
//                String inputLine;
//
//                while ((inputLine = dis.readLine()) != null) {
//                    System.out.println(inputLine);
//                }
//                dis.close();
//            } catch (MalformedURLException me) {
//                System.out.println("MalformedURLException: " + me);
//            } catch (IOException ioe) {
//                System.out.println("IOException: " + ioe);
//            }

        } catch (Exception e) {
        }
        return null;
    }

    // Удалить начальные символы "www" из адреса, если они присутствуют.
    private String removeWwwFromUrl(String url) {
        int index = url.indexOf("://www.");
        if (index != -1) {
            return url.substring(0, index + 3) +
                    url.substring(index + 7);
        }
        return (url);
    }

    // Произвести синтаксический анализ и возвратить ссылки.
    private ArrayList retrieveLinks(
            URL pageUrl, String pageContents, HashSet crawledList,
            boolean limitHost) {
        // Компилировать ссылки шаблонов совпадений.
        Pattern p =
                Pattern.compile("<a\\s+href\\s*=\\s*\"?(.*?)[\"|>]",
                        Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(pageContents);
        // Создать список совпадающих ссылок.
        ArrayList<String> linkList = new ArrayList();
        while (m.find()) {
            String link = m.group(1).trim();
            // Пропустить пустые ссылки.
            if (link.length() < 1) {
                continue;
            }
            // Пропустить ссылки, которые указывают на заданную страницу.
            if (link.charAt(0) == '#') {
                continue;
            }
            // Пропустить ссылки, которые используются
            // для почтовых отправлений.
            if (link.indexOf("mailto:") != -1) {
                continue;
            }
            // Пропустить ссылки на сценарии JavaScript.
            if (link.toLowerCase().indexOf("javascript") != -1) {
                continue;
            }
            // Восстановить префикс абсолютного или относительного URL.
            if (link.indexOf("://") == -1) {
                // Обработать абсолютный URL.
                if (link.charAt(0) == '/') {
                    link = "http://" + pageUrl.getHost() + link;

                    // Обработать относительный URL.
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
            // Удалить привязки из ссылок.
            int index = link.indexOf('#');
            if (index != -1) {
                link = link.substring(0, index);
            }
            // Удалить начальные символы "www" из URL, если они есть.
            link = removeWwwFromUrl(link);
            // Проверить ссылки и отбросить все неправильные.
            URL verifiedLink = verifyUrl(link);
            if (verifiedLink == null) {
                continue;
            }
 /* Если указано, то использовать только ссылки
 для сайта с начальным URL. */
            if (limitHost &&
                    !pageUrl.getHost().toLowerCase().equals(
                            verifiedLink.getHost().toLowerCase())) {
                continue;
            }
            // Отбросить ссылки, если они уже просмотрены.
            if (crawledList.contains(link)) {
                continue;
            }
            // Добавить ссылку в список.
            linkList.add(link);
        }
        return (linkList);
    }

    /* Определить, есть ли совпадения для строки поиска
    на данной странице. */
    private boolean searchStringMatches(
            String pageContents, String searchString,
            boolean caseSensitive) {
        String searchContents = pageContents;
 /* Если учитывается регистр клавиатуры, то преобразовать
 содержимое в нижний регистр для сравнения. */
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
//
//    // Выполнить просмотр, производя поиск для заданной строки.
//    public void crawl(
//            String startUrl, int maxUrls, boolean limitHost,
//            String searchString, boolean caseSensitive) {
//        // Установить список поиска.
//        HashSet crawledList = new HashSet();
//        LinkedList<String> toCrawlList = new LinkedList();
//        // Добавить начальный URL в список поиска.
//        toCrawlList.add(startUrl);
//         /* Выполнить поиск, последовательно просматривая
//         список поиска. */
//        while (crawling && toCrawlList.size() > 0) {
//         /* Проверить, не достигнуто ли максимально
//         число разрешенных URL, если это значение задано. */
//            if (maxUrls != -1) {
//                if (crawledList.size() == maxUrls) {
//                    break;
//                }
//            }
//            // Получить URL.
//            String url = toCrawlList.remove();
////            String url = (String) toCrawlList.iterator().next();
//            // Удалить URL из списка поиска.
////            toCrawlList.remove(url);
//            // Преобразовать строку url в объект URL.
//            URL verifiedUrl = verifyUrl(url);
//            // Пропустить URL, если по списку робота к нему нет доступа.
//            if (!isRobotAllowed(verifiedUrl)) {
//                continue;
//            }
//            // Обновить панель Stats.
//
//            updateStats(url, crawledList.size(), toCrawlList.size(),
//                    maxUrls);
//            // Добавить страницу в список поиска.
//            crawledList.add(url);
//            // Загрузить страницу с заданным url.
//            String pageContents = downloadPage(verifiedUrl);
// /* Если страница успешно загружена, извлечь из нее
// все ссылки и затем произвести поиск совпадающих строк. */
//            if (pageContents != null && pageContents.length() > 0) {
//                // Извлечь список допустимых ссылок из страницы.
//                ArrayList links =
//                        retrieveLinks(verifiedUrl, pageContents, crawledList,
//                                limitHost);
//                // Добавить ссылки в список поиска.
//                toCrawlList.addAll(links);
// /* Проверить на наличие совпадающей строки, и если
// совпадение есть, то записать совпадение. */
//                if (searchStringMatches(pageContents, searchString,
//                        caseSensitive)) {
//                    addMatch(url);
//                }
//            }
//            // Обновить панель Stats.
//            updateStats(url, crawledList.size(), toCrawlList.size(),
//                    maxUrls);
//        }
//    }
//
//

    public void crawl(String startUrl, int maxUrls, int maxThreads, boolean limitHost, String searchString, boolean caseSensitive) {
        HashSet crawledList = new HashSet();
        CopyOnWriteArrayList<String> toCrawlList = new CopyOnWriteArrayList();
//        CopyOnWriteArrayList<String> toCrawlOneStageNodes = new CopyOnWriteArrayList(toCrawlList);
        toCrawlList.add(startUrl);
        ThreadGroup nodesThreadGroup = new ThreadGroup("Children thread Group");
//        NodesStageInfo nodesStageInfo = new NodesStageInfo(new CopyOnWriteArrayList(toCrawlList));
        NodesStageInfo nodesStageInfo = new NodesStageInfo();

        while (crawling && toCrawlList.size() > 0) {

            ///////////////////////////////////////////////////
            if (!operateNextUrl(maxUrls, crawledList.size())) {
                break;
            }

            if (nodesStageInfo.isUpdateStageNeeded()) {
                nodesStageInfo.setStageCrawlList(new CopyOnWriteArrayList<>(toCrawlList));
                log.info("NEW STAGE CREATED! " + nodesStageInfo.getAllNodesCount() + " elements..");
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
                    log.info(this.getName() + " started");
                    addToScanningTable(url);
                    String pageContents = downloadPage(verifiedUrl);

                    log.info("Downloaded page content from: " + url);
//                    log.info("====================================== + \n" + pageContents);
                    if (pageContents != null && pageContents.length() > 0) {

                        ArrayList<String> links = retrieveLinks(verifiedUrl, pageContents, crawledList, limitHost);
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
                    System.out.println("Waiting... " + nodesThreadGroup.activeCount() + "/" + maxThreads + " are active");
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