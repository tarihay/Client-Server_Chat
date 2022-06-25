package ru.nsu.gorin.lab5.chat.clientController;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.nsu.gorin.lab5.chat.clientView.ViewGuiClient;
import ru.nsu.gorin.lab5.chat.connection.Connection;
import ru.nsu.gorin.lab5.chat.connection.Message;
import ru.nsu.gorin.lab5.chat.connection.MessageType;
import ru.nsu.gorin.lab5.chat.model.ModelClient;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Класс клиента
 */
public class SimpleClient extends AbstractClient {
    private static final Logger logger = LogManager.getLogger(SimpleClient.class);

    private static final int MAX_MESSAGE_LENGTH_ON_ONE_LINE = 70;

    private Connection connection;
    private ModelClient model;
    private ViewGuiClient gui;
    private volatile boolean isConnect = false;

    /**
     * Проверяет, подключен ли клиент к серверу или нет
     * @return возвращает true, если подключен, false в противном случае
     */
    public boolean isConnect() {
        return isConnect;
    }

    public void setConnect(boolean status) {
        isConnect = status;
    }

    /**
     * Метод запускает работу клиента
     * Вызывается в ClientApplication
     * @see ClientApplication
     */
    @Override
    public void run() {
        model = new ModelClient();
        gui = new ViewGuiClient(this);
        gui.initFrameClient();
        while (true) {
            if (isConnect) {
                nameUserRegistration();
                receiveMessageFromServer();
                isConnect = false;
            }
        }
    }

    /**
     * Метод создает соединение между сервером и клиентом
     */
    @Override
    public void connectToServer() {
        if (!isConnect) {
            while (true) {
                try {
                    InetAddress address = InetAddress.getByName(gui.getAddressServerFromOptionPane());
                    int port = gui.getPortServerFromOptionPane();

                    Socket socket = new Socket(address, port);
                    connection = new Connection(socket);
                    isConnect = true;
                    gui.addMessage("You connected to the server\n");
                    break;
                } catch (Exception e) {
                    gui.errorDialogWindow("You typed wrong port. Try another");
                    break;
                }
            }
        }
        else {
            gui.errorDialogWindow("You are already connected!");
        }
    }

    /**
     * Метод отключает текущего клиента от сервера
     * Отправляет сообщение на сервер об отключении в виде Message
     * @see Message
     */
    @Override
    public void disableClient() {
        try {
            if (isConnect) {
                connection.send(new Message(MessageType.DISABLE_USER));
                model.getUsers().clear();
                setConnect(false);
                gui.refreshListUsers(model.getUsers());
            }
            else {
                gui.errorDialogWindow("You are already disconnected");
            }
        } catch (Exception e) {
            gui.errorDialogWindow("Error with closing connection occurred");
        }
    }

    /**
     * Метод преобразует текст сообщение вида Message и отправляет его на сервер
     * @param text текст, хранящийся в сообщении
     */
    @Override
    public void sendMessageOnServer(String text) {
        try {
            connection.send(new Message(MessageType.TEXT_MESSAGE, text));
        } catch (Exception e) {
            gui.errorDialogWindow("Error with sending message occurred");
            logger.info("client couldn't send a message");
        }
    }

    /**
     * Метод принимает сообщения от других клиентов
     */
    @Override
    public void receiveMessageFromServer() {
        while (isConnect) {
            try {
                Message message = connection.receive();

                if (message.getTypeMessage() == MessageType.TEXT_MESSAGE) {
                    String stringMessage = message.getTextMessage();
                    if (stringMessage.length() > MAX_MESSAGE_LENGTH_ON_ONE_LINE) {
                        stringMessage = correctTheMessageLength(stringMessage);
                    }
                    gui.addMessage(stringMessage);
                }

                if (message.getTypeMessage() == MessageType.USER_ADDED) {
                    model.addUser(message.getTextMessage());
                    gui.refreshListUsers(model.getUsers());
                    gui.addMessage(String.format("We have new participant! %s joined to the chat.\n", message.getTextMessage()));
                }

                if (message.getTypeMessage() == MessageType.REMOVED_USER) {
                    model.removeUser(message.getTextMessage());
                    gui.refreshListUsers(model.getUsers());
                    gui.addMessage(String.format("We lost our participant! %s left the chat.\n", message.getTextMessage()));
                }
            } catch (Exception e) {
                gui.errorDialogWindow("Error in receiving message occurred");
                logger.error("Error in receiving message occurred: " + e);
                isConnect = false;
                gui.refreshListUsers(model.getUsers());
                break;
            }
        }
    }

    /**
     * Метод реализует регистрацию имени нового пользователя
     */
    @Override
    public void nameUserRegistration() {
        while (true) {
            try {
                Message message = connection.receive();

                if (message.getTypeMessage() == MessageType.REQUEST_NAME_USER) {
                    String nameUser = gui.getNameUser();
                    connection.send(new Message(MessageType.USER_NAME, nameUser));
                }

                if (message.getTypeMessage() == MessageType.NAME_USED) {
                    gui.errorDialogWindow("This name is already using. Try another");
                    String nameUser = gui.getNameUser();
                    connection.send(new Message(MessageType.USER_NAME, nameUser));
                }

                if (message.getTypeMessage() == MessageType.NAME_ACCEPTED) {
                    gui.addMessage("Name accepted\n");
                    model.setUsers(message.getListUsers());
                    break;
                }
            } catch (Exception e) {
                gui.errorDialogWindow("Something went wrong. Try to reconnect");
                try {
                    connection.close();
                    isConnect = false;
                    break;
                } catch (IOException e1) {
                    gui.errorDialogWindow("Error with closing connection occurred");
                    logger.error("An error with closing connection occured: " + e);
                }
            }
        }
    }

    /**
     * Метод расставляет переходы на новую строку в сообщении
     */
    private String correctTheMessageLength(String message) {
        int amountOfJumpsToTheNextLine = message.length() / MAX_MESSAGE_LENGTH_ON_ONE_LINE + 1;
        StringBuilder newMessage = new StringBuilder();

        for (int i = 0; i < amountOfJumpsToTheNextLine; i++) {
            int end = MAX_MESSAGE_LENGTH_ON_ONE_LINE * (i + 1);
            final int start = MAX_MESSAGE_LENGTH_ON_ONE_LINE * i;

            if (i == amountOfJumpsToTheNextLine-1) {
                end = message.length() - 1;
                newMessage.append(message, start, end);
            }
            else {
                newMessage.append(message, start, end);
            }
            if (message.charAt(end) != ' ' && message.charAt(end - 1) != ' '
                    && end != message.length() - 1) {
                newMessage.append("-\n");
            }
            else {
                newMessage.append("    \n");
            }
        }
        return newMessage.toString();
    }
}
