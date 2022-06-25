package ru.nsu.gorin.lab5.chat.clientView;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.nsu.gorin.lab5.chat.clientController.AbstractClient;
import ru.nsu.gorin.lab5.chat.clientController.SimpleClient;
import ru.nsu.gorin.lab5.chat.clientController.ClientUsingJson;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Set;

public class ViewGuiClient {
    private static final Logger logger = LogManager.getLogger(ViewGuiClient.class);

    private static final String CHAT_NAME = "NSU Chat";
    private static final String DISCONNECT_TEXT = "Disconnect";
    private static final String CONNECT_TEXT = "Connect";

    private static final String LOGO_PATH = "NSU_logo.png";

    private static final int USERS_AREA_ROWS_AMOUNT = 30;
    private static final int USERS_AREA_COLUMNS_AMOUNT = 20;
    private static final int MESSAGES_AREA_ROWS_AMOUNT = 30;
    private static final int MESSAGES_AREA_COLUMNS_AMOUNT = 15;

    private static final int TEXT_FIELD_COLUMNS_AMOUNT = 40;

    private final AbstractClient client;
    private final JFrame frame = new JFrame(CHAT_NAME);
    private final JTextArea messages = new JTextArea(MESSAGES_AREA_ROWS_AMOUNT, MESSAGES_AREA_COLUMNS_AMOUNT);
    private final JTextArea users = new JTextArea(USERS_AREA_ROWS_AMOUNT, USERS_AREA_COLUMNS_AMOUNT);
    private final JPanel panel = new JPanel();
    private final JTextField textField = new JTextField(TEXT_FIELD_COLUMNS_AMOUNT);
    private final JButton disconnectButton = new JButton(DISCONNECT_TEXT);
    private final JButton connectButton = new JButton(CONNECT_TEXT);

    public ViewGuiClient(SimpleClient simpleClient) {
        this.client = simpleClient;
    }

    public ViewGuiClient(ClientUsingJson client) {
        this.client = client;
    }

    /**
     * Метод инициализирует графический интерфейс клиента
     */
    public void initFrameClient() {
        messages.setEditable(false);
        users.setEditable(false);
        frame.add(new JScrollPane(messages), BorderLayout.CENTER);
        frame.add(new JScrollPane(users), BorderLayout.EAST);
        try {
            frame.setIconImage(ImageIO.read(ViewGuiClient.class.getResource(LOGO_PATH)));
        }
        catch(Exception e) {
            logger.error(e);
        }

        panel.add(textField);
        panel.add(connectButton);
        panel.add(disconnectButton);
        frame.add(panel, BorderLayout.SOUTH);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (client.isConnect()) {
                    client.disableClient();
                }
                System.exit(0);
            }
        });
        frame.setVisible(true);
        disconnectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                client.disableClient();
            }
        });
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                client.connectToServer();
            }
        });
        textField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                client.sendMessageOnServer(textField.getText());
                textField.setText("");
            }
        });
    }

    public void addMessage(String text) {
        messages.append(text);
    }


    /**
     * Метод обновляет список подключенных пользователей
     * @param listUsers список участников
     */
    public void refreshListUsers(Set<String> listUsers) {
        users.setText("");
        if (client.isConnect()) {
            StringBuilder text = new StringBuilder("Chat Participants: \n");
            for (String user : listUsers) {
                text.append(user + "\n");
            }
            users.append(text.toString());
        }
    }


    /**
     * Метод вызывает окно для ввода порта сервера
     * @return возвращает порт
     */
    public int getPortServerFromOptionPane() {
        while (true) {
            String port = JOptionPane.showInputDialog(
                    frame, "Type server's port",
                    "Server port",
                    JOptionPane.QUESTION_MESSAGE
            );
            try {
                return Integer.parseInt(port.trim());
            } catch (Exception e) {
                logger.error(e);
                JOptionPane.showMessageDialog(
                        frame, "Error with typing port occurred",
                        "Wrong port typed", JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    /**
     * Метод вызывает окно для ввода адреса сервера
     * @return возвращает порт
     */
    public String getAddressServerFromOptionPane() {
        while (true) {
            String address = JOptionPane.showInputDialog(
                    frame, "Type server's address",
                    "Server address",
                    JOptionPane.QUESTION_MESSAGE
            );
            try {
                return address;
            } catch (Exception e) {
                logger.error(e);
                JOptionPane.showMessageDialog(
                        frame, "Error with typing address occurred",
                        "Wrong address typed", JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }


    /**
     * Метод вызывает окно для ввода имени пользователя
     * @return возвращает имя пользователя
     */
    public String getNameUser() {
        return JOptionPane.showInputDialog(
                frame, "Type your nickname",
                "Nickname",
                JOptionPane.QUESTION_MESSAGE
        );
    }

    /**
     * Метод вызывает окно с ошибкой
     * @param text текст ошибки
     */
    public void errorDialogWindow(String text) {
        JOptionPane.showMessageDialog(
                frame, text,
                "ERROR", JOptionPane.ERROR_MESSAGE
        );
    }
}
