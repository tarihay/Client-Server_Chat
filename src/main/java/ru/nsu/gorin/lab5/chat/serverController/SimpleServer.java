package ru.nsu.gorin.lab5.chat.serverController;

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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Класс сервера
 */
public class SimpleServer extends AbstractServer {
    private static final Logger logger = LogManager.getLogger(SimpleServer.class);


    private ServerSocket serverSocket;
    private ViewGuiServer gui;
    private ModelServer model;
    private volatile boolean isServerStart = false;

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
     * Метод рассылает сообщения всем участникам чата
     * @param message сообщение
     */
    @Override
    public void sendMessageAllUsers(Message message) {
        for (Map.Entry<String, Connection> user : model.getAllUsers().entrySet()) {
            try {
                user.getValue().send(message);
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
         * @param connection объект соединения
         * @return возвращает никнейм клиента
         */
        private String addClient(Connection connection) {
            while (true) {
                try {
                    connection.send(new Message(MessageType.REQUEST_NAME_USER));
                    Message responseMessage = connection.receive();
                    String userName = responseMessage.getTextMessage();

                    if (responseMessage.getTypeMessage() == MessageType.USER_NAME && userName != null && !userName.isEmpty() && !model.getAllUsers().containsKey(userName)) {
                        model.addUser(userName, connection);
                        Set<String> listUsers = new HashSet<>();
                        for (Map.Entry<String, Connection> users : model.getAllUsers().entrySet()) {
                            listUsers.add(users.getKey());
                        }


                        connection.send(new Message(MessageType.NAME_ACCEPTED, listUsers));

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
         * @param connection объект соединения
         * @param userName никнейм текущего клиента
         */
        private void messagingBetweenUsers(Connection connection, String userName) {
            while (true) {
                try {
                    Message message = connection.receive();

                    if (message.getTypeMessage() == MessageType.TEXT_MESSAGE) {
                        String textMessage = String.format("%s: %s\n", userName, message.getTextMessage());
                        sendMessageAllUsers(new Message(MessageType.TEXT_MESSAGE, textMessage));
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