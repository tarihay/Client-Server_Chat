package ru.nsu.gorin.lab5.chat.clientController;

/**
 * Абстрактный класс клиента. От него наследуются SimpleClient и ClientUsingJson
 * @see SimpleClient
 * @see ClientUsingJson
 */
public abstract class AbstractClient {

    public abstract void connectToServer();

    public abstract void disableClient();

    public abstract void run();

    public abstract void sendMessageOnServer(String text);

    public abstract void receiveMessageFromServer();

    public abstract void nameUserRegistration();

    public abstract boolean isConnect();
}
