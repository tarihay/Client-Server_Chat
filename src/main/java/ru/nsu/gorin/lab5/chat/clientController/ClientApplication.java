package ru.nsu.gorin.lab5.chat.clientController;

import java.util.Scanner;

/**
 * Класс-стартер клиентского приложения
 */
public class ClientApplication {
    private static final int SIMPLE_WORK = 1;
    private static final int WORK_WITH_JSON = 2;

    private static boolean startStatus = false;

    public static void main(String[] args) {
        AbstractClient client;

        Scanner scanner = new Scanner(System.in);
        while (!startStatus) {
            System.out.println("If you want to run Simple Client App, type 1. If you want to run Client App using JSON type 2");
            int value = scanner.nextInt();
            if (value == SIMPLE_WORK) {
                client = new SimpleClient();
                startStatus = true;
                client.run();
            }
            else if (value == WORK_WITH_JSON) {
                client = new ClientUsingJson();
                startStatus= true;
                client.run();
            }
            else {
                System.out.println("You typed wrong mode");
            }
        }
    }
}
