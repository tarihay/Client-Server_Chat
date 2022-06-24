package ru.nsu.gorin.lab5.chat.model;

import ru.nsu.gorin.lab5.chat.connection.Connection;

import java.util.HashMap;
import java.util.Map;

public class ModelServer {
    private final Map<String, Connection> allUsers = new HashMap<>();


    public Map<String, Connection> getAllUsers() {
        return allUsers;
    }

    public void addUser(String nameUser, Connection connection) {
        allUsers.put(nameUser, connection);
    }

    public void removeUser(String nameUser) {
        allUsers.remove(nameUser);
    }

}