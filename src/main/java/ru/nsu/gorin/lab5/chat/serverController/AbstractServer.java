package ru.nsu.gorin.lab5.chat.serverController;

import ru.nsu.gorin.lab5.chat.connection.Message;

/**
 * Абстрактный класс сервера. От него наследуются SimpleServer и ServerUsingJson
 * @see SimpleServer
 * @see ServerUsingJson
 */
public abstract class AbstractServer {
    public abstract void run();

    public abstract void startServer(int port);

    public abstract void stopServer();

    public abstract void acceptClient();

    public abstract void sendMessageAllUsers(Message message);
}
