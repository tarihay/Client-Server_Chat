package ru.nsu.gorin.lab5.chat.serverController;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.nsu.gorin.lab5.chat.connection.Connection;
import ru.nsu.gorin.lab5.chat.connection.Message;
import ru.nsu.gorin.lab5.chat.connection.MessageType;
import ru.nsu.gorin.lab5.chat.model.ModelServer;
import ru.nsu.gorin.lab5.chat.serverView.ViewGuiServer;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

/**
 * Класс сервера, который использует Json
 */
public class ServerUsingJson extends AbstractServer {
    private static final Logger logger = LogManager.getLogger(SimpleServer.class);

    private static final int MAX_NUM_OF_SAVED_MESSAGES = 7;

    private ServerSocket serverSocket;
    private ViewGuiServer gui;
    private ModelServer model;
    private volatile boolean isServerStart = false;

    private String lastMessages[] = new String[MAX_NUM_OF_SAVED_MESSAGES];
    private int numOfSavedMessages = 0;

    /**
     * Метод запускает работу сервера
     * Вызывается в ServerApplication
     * @see ServerApplication
     */
    @Override
    public void run() {
        gui = new ViewGuiServer(this);
        model = new ModelServer();
        gui.initFrameServer();
        while (true) {
            if (isServerStart) {
                acceptClient();
                isServerStart = false;
            }
        }
    }

    /**
     * Метод запускает сервер
     * @param port порт, по которому будет открыт сервер
     */
    @Override
    public void startServer(int port) {
        try {
            serverSocket = new ServerSocket(port);
            isServerStart = true;
            gui.refreshDialogWindowServer("Server started.\n");
        } catch (Exception e) {
            e.printStackTrace();
            gui.refreshDialogWindowServer("Server couldn't start for some reason.\n");
        }
    }

    /**
     * Метод останавливает работу сервера
     */
    @Override
    public void stopServer() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                for (Map.Entry<String, Connection> user : model.getAllUsers().entrySet()) {
                    user.getValue().close();
                }
                serverSocket.close();
                model.getAllUsers().clear();
                gui.refreshDialogWindowServer("Server stopped.\n");
            }
            else {
                gui.refreshDialogWindowServer("Server is not already working!\n");
            }
        } catch (Exception e) {
            gui.refreshDialogWindowServer("Error with stopping server occurred.\n");
            logger.error(e);
        }
    }

    /**
     * Метод принимает подключение клиента
     */
    @Override
    public void acceptClient() {
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                new ServerThread(socket).start();
            } catch (Exception e) {
                logger.error(e);
                gui.refreshDialogWindowServer("Connection lost\n");
                break;
            }
        }
    }


    /**
     * Метод преобразует сообщение в Json объект и рассылает всем участникам чата
     * @param message сообщение
     */
    @Override
    public void sendMessageAllUsers(Message message) {
        for (Map.Entry<String, Connection> user : model.getAllUsers().entrySet()) {
            try {
                Gson gson = new Gson();
                String jsonObject = gson.toJson(message);
                user.getValue().send(jsonObject);
            } catch (Exception e) {
                logger.error(e);
                gui.refreshDialogWindowServer("Error with sending the message occurred\n");
            }
        }
    }

    /**
     * Вспомогательный класс-поток, объект которого создается при подключении нового клиента
     */
    private class ServerThread extends Thread {
        private final Socket socket;

        public ServerThread(Socket socket) {
            this.socket = socket;
        }


        /**
         * Метод запрашивает имя у клиента и добавляет его в мапу
         * Использует Json объекты для обмена данными
         * @param connection объект соединения
         * @return возвращает никнейм клиента
         */
        private String addClient(Connection connection) {
            while (true) {
                try {
                    Gson gson = new Gson();
                    Message message = new Message(MessageType.REQUEST_NAME_USER);
                    String jsonObject = gson.toJson(message);
                    connection.send(jsonObject);

                    jsonObject = connection.receiveJson();
                    Message responseMessage = gson.fromJson(jsonObject, Message.class);
                    String userName = responseMessage.getTextMessage();

                    if (responseMessage.getTypeMessage() == MessageType.USER_NAME && userName != null
                            && !userName.isEmpty() && !model.getAllUsers().containsKey(userName)) {
                        model.addUser(userName, connection);
                        Set<String> listUsers = new HashSet<>();
                        for (Map.Entry<String, Connection> users : model.getAllUsers().entrySet()) {
                            listUsers.add(users.getKey());
                        }

                        Message nameAccepted = new Message(MessageType.NAME_ACCEPTED, listUsers);
                        jsonObject = gson.toJson(nameAccepted);
                        connection.send(jsonObject);

                        if (numOfSavedMessages > 0) {
                            for (int i = 0; i < numOfSavedMessages; i++) {
                                connection.send(lastMessages[i]);
                            }
                        }
                        System.out.println("1");

                        sendMessageAllUsers(new Message(MessageType.USER_ADDED, userName));

                        return userName;
                    }
                    else {
                        connection.send(new Message(MessageType.NAME_USED));
                    }


                } catch (Exception e) {
                    logger.error(e);
                    gui.refreshDialogWindowServer("Error with adding new client occurred\n");
                }
            }
        }


        /**
         * Метод реализует обмен сообщениями между клиентами
         * Использует Json объекты
         * @param connection объект соединения
         * @param userName никнейм текущего клиента
         */
        private void messagingBetweenUsers(Connection connection, String userName) {
            while (true) {
                try {
                    Gson gson = new Gson();
                    String jsonObject = connection.receiveJson();
                    Message message = gson.fromJson(jsonObject, Message.class);

                    String textMessage = null;
                    if (message.getTypeMessage() == MessageType.TEXT_MESSAGE) {
                        textMessage = String.format("%s:\n %s\n\n", userName, message.getTextMessage());
                        sendMessageAllUsers(new Message(MessageType.TEXT_MESSAGE, textMessage));
                    }

                    Message messageToSave = new Message(MessageType.TEXT_MESSAGE, textMessage);
                    String jsonToSave = gson.toJson(messageToSave);
                    if (numOfSavedMessages < MAX_NUM_OF_SAVED_MESSAGES) {
                        lastMessages[numOfSavedMessages] = jsonToSave;
                        numOfSavedMessages++;
                    }
                    else {
                        appendMessageWithMovingBuffer(jsonToSave);
                    }


                    if (message.getTypeMessage() == MessageType.DISABLE_USER) {
                        sendMessageAllUsers(new Message(MessageType.REMOVED_USER, userName));
                        model.removeUser(userName);
                        connection.close();
                        gui.refreshDialogWindowServer("User left the chat.\n");
                        break;
                    }
                } catch (Exception e) {
                    logger.error(e);
                    gui.refreshDialogWindowServer(String.format("Error with sending the messages from %s occurred\n", userName));
                    break;
                }
            }
        }

        private void appendMessageWithMovingBuffer(String jsonObject) {
            for (int i = 0; i < MAX_NUM_OF_SAVED_MESSAGES - 1; i++) {
                lastMessages[i] = lastMessages[i+1];
            }
            lastMessages[MAX_NUM_OF_SAVED_MESSAGES-1] = jsonObject;
        }

        @Override
        public void run() {
            gui.refreshDialogWindowServer("New participant joined the chat. Welcome!");
            try {
                Connection connection = new Connection(socket);
                String nameUser = addClient(connection);
                messagingBetweenUsers(connection, nameUser);
            } catch (Exception e) {
                logger.error(e);
                gui.refreshDialogWindowServer("Error with sending the messages from user occurred\n");
            }
        }
    }
}
