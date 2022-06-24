package ru.nsu.gorin.lab5.chat.serverView;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.nsu.gorin.lab5.chat.clientView.ViewGuiClient;
import ru.nsu.gorin.lab5.chat.serverController.AbstractServer;
import ru.nsu.gorin.lab5.chat.serverController.SimpleServer;
import ru.nsu.gorin.lab5.chat.serverController.ServerUsingJson;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


public class ViewGuiServer {
    private static final Logger logger = LogManager.getLogger(ViewGuiServer.class);

    private static final int TEXT_AREA_ROWS_AMOUNT = 10;
    private static final int TEXT_AREA_COLUMNS_AMOUNT = 10;

    private static final String LOGO_PATH = "NSU_logo.png";

    private static final String APP_NAME = "Server";
    private static final String RUN_BUTTON_NAME = "Run server";
    private static final String STOP_BUTTON_NAME = "Stop server";

    private JFrame frame = new JFrame(APP_NAME);
    private JTextArea dialogWindow = new JTextArea(TEXT_AREA_ROWS_AMOUNT, TEXT_AREA_COLUMNS_AMOUNT);
    private JButton startServerButton = new JButton(RUN_BUTTON_NAME);
    private JButton stopServerButton = new JButton(STOP_BUTTON_NAME);
    private JPanel panelButtons = new JPanel();

    private final AbstractServer server;

    public ViewGuiServer(SimpleServer server) {
        this.server = server;
    }

    public ViewGuiServer(ServerUsingJson server) {
        this.server = server;
    }

    /**
     * Метод инициализации графического интерфейса приложения сервера
     */
    public void initFrameServer() {
        dialogWindow.setEditable(false);
        dialogWindow.setLineWrap(true);
        frame.add(new JScrollPane(dialogWindow), BorderLayout.CENTER);
        panelButtons.add(startServerButton);
        panelButtons.add(stopServerButton);
        frame.add(panelButtons, BorderLayout.SOUTH);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        try {
            frame.setIconImage(ImageIO.read(ViewGuiClient.class.getResource(LOGO_PATH)));
        }
        catch(Exception e) {
            logger.error(e);
        }

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                server.stopServer();
                System.exit(0);
            }
        });
        frame.setVisible(true);

        startServerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int port = getPortFromOptionPane();
                server.startServer(port);
            }
        });
        stopServerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                server.stopServer();
            }
        });
    }


    /**
     * Метод добавляет в текстовое окно новое сообщение
     * @param serviceMessage сообщение для добавки
     */
    public void refreshDialogWindowServer(String serviceMessage) {
        dialogWindow.append(serviceMessage);
    }


    /**
     * Метод вызывает диалоговое окно для ввода порта сервера
     * @return возвращает введенный порт
     */
    protected int getPortFromOptionPane() {
        while (true) {
            String port = JOptionPane.showInputDialog(
                    frame, "Type server's port",
                    "Server port",
                    JOptionPane.QUESTION_MESSAGE
            );
            try {
                return Integer.parseInt(port.trim());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(
                        frame, "Error with typing port occurred",
                        "Wrong port typed", JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
}