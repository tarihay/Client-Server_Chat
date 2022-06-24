package ru.nsu.gorin.lab5.chat.connection;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Connection implements Closeable {
    private final Socket socket;
    private final ObjectOutputStream out;
    private final ObjectInputStream in;


    public Connection(Socket socket) throws IOException {
        this.socket = socket;
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());
    }


    /**
     * Метод отправляет сообщение через сокет
     * @param message сообщение
     * @throws IOException в случае ошибки записи выкидывает исключение
     */
    public void send(Message message) throws IOException {
        synchronized (out) {
            out.writeObject(message);
        }
    }

    /**
     * Метод отправляет сообщение через сокет
     * @param jsonObject сообщение в виде объекта JSON
     * @throws IOException в случае ошибки записи выкидывает исключение
     */
    public void send(String jsonObject) throws IOException {
        synchronized (out) {
            out.writeObject(jsonObject);
        }
    }

    /**
     * Метод принимает сообщение через сокет
     * @return Возвращает полученное сообщение
     * @throws IOException в случае чтение выбрает исключение
     * @throws ClassNotFoundException в случае ошибки класса выбрасывает исключение
     */
    public Message receive() throws IOException, ClassNotFoundException {
        synchronized (in) {
            Message message = (Message) in.readObject();
            return message;
        }
    }

    /**
     * Метод принимает сообщение через сокет
     * @return Возвращает полученное сообщение в виде JSON
     * @throws IOException в случае чтение выбрает исключение
     * @throws ClassNotFoundException в случае ошибки класса выбрасывает исключение
     */
    public String receiveJson() throws IOException, ClassNotFoundException {
        synchronized (in) {
            String jsonObject = (String) in.readObject();
            return jsonObject;
        }
    }


    /**
     * Метод закрывает потоки и сокет
     * @throws IOException выбрасывает исключение в случае ошибки закрытия
     */
    @Override
    public void close() throws IOException {
        in.close();
        out.close();
        socket.close();
    }
}
