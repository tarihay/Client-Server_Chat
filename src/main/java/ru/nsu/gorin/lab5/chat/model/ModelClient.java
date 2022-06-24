package ru.nsu.gorin.lab5.chat.model;

import java.util.HashSet;
import java.util.Set;

public class ModelClient {
    private Set<String> users = new HashSet<>();

    public Set<String> getUsers() {
        return users;
    }

    public void addUser(String nameUser) {
        users.add(nameUser);
    }

    public void removeUser(String nameUser) {
        users.remove(nameUser);
    }

    public void setUsers(Set<String> users) {
        this.users = users;
    }
}
