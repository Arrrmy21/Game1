package src.warships.client;


import src.warships.server.Field;

import java.io.*;
import java.net.Socket;

public class Player {

    private String nameOfPlayer;

    ObjectInputStream fileInputStream;
    BufferedReader reader;
    BufferedReader keyboard;
    PrintWriter writer;
    Socket sock;

    private Field playerField;
    private Field enemyField;

    public static void main(String[] args) {
        new Player().go();
    }

    private void go() {
        connect();
        setUpNetwork();

        //speaking();
        new Thread(new PlayerListener()).start();
        new Thread(new PlayerWriter()).start();
    }


    public String getNameOfPlayer() {
        return nameOfPlayer;
    }

    public void setNameOfPlayer(String name) {
        this.nameOfPlayer = name;
    }

    private void connect(){
        try {
            System.out.println("Attempt to connect...");
            sock = new Socket("127.0.0.1", 4949);
            //sock = new Socket("176.105.12.145", 4444);
        } catch (Exception ex) {
            System.out.println("Socket connection exception");
        }
    }

    private void setUpNetwork() {

        try {
            keyboard = new BufferedReader(new InputStreamReader(System.in));
            reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            //System.out.println("Reader created");
            writer = new PrintWriter( new OutputStreamWriter(sock.getOutputStream()));
            //System.out.println("Writer created");

            System.out.println("connections created.");
        } catch (Exception ex) {
            System.out.println("Reader / Writer creation failed.");
            ex.printStackTrace();
        }
    }
    private class PlayerListener implements Runnable {

        @Override
        public void run() {
            System.out.println("Starting listening...");
            while (true) {
                String receivedMessage = "";
                try {
                    receivedMessage = reader.readLine();
                    if (!receivedMessage.isEmpty()) {
                        System.out.println(receivedMessage);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class PlayerWriter implements Runnable {
            @Override
            public void run() {
                while (true) {
                    try {
                        String msgEntered = keyboard.readLine();

                        if (!msgEntered.isEmpty()) {
                            String firstWord = "";
                            String secondWord = "";
                            String msgEdited = msgEntered.replaceAll("\\s", "");

                            if (msgEdited.equalsIgnoreCase("exit")) {
                                closeConnection();
                                break;
                            }
                            /*
                            // Символ "=" означает присваивание имени
                             */
                            if (msgEdited.contains("=")) {
                                firstWord = (msgEdited.split("=")[0]);
                                secondWord = (msgEdited.split("=")[1]);
                                System.out.println("first word = " + firstWord);
                                System.out.println("second word = " + secondWord);
                                if (firstWord.equalsIgnoreCase("name")) {
                                    setNameOfPlayer(secondWord);
                                }
                                /*
                                //Если игрок не присваивает себе имя - идёт проверка на наличие имени
                                 */
                            } else if (getNameOfPlayer() == null) {
                                System.out.println("Enter your name first by [name = <your name>]");
                            } else {
                                System.out.println("name of player = " + getNameOfPlayer());
                                String msgToServer = getNameOfPlayer() + ":" + msgEdited;
                                System.out.println("Msg to server: " + msgToServer);
                                if (msgToServer != null) {
                                    writer.println(msgToServer);
                                    writer.flush();
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("Ошибка ввода");
                    }
                }
            }
        }


    /*public void speaking() {
        while (true) {
            try {
                String msgEntered = "";
                String msgFromServer = "";
                msgEntered = keyboard.readLine();

                if (!msgEntered.isEmpty()) {
                    String firstWord = "";
                    String secondWord = "";
                    String msgEdited = msgEntered.replaceAll("\\s", "");

                    if (msgEdited.equalsIgnoreCase("exit")) {
                        closeConnection();
                        break;
                    }
                    if (msgEdited.contains("=")) {
                        firstWord = (msgEdited.split("=")[0]);
                        secondWord = (msgEdited.split("=")[1]);
                        System.out.println("first word = " + firstWord);
                        System.out.println("second word = " + secondWord);
                        if (firstWord.equalsIgnoreCase("name")) {
                            setNameOfPlayer(secondWord);
                        }
                    } else if (getNameOfPlayer() == null) {
                        System.out.println("Enter your name first by [name = <your name>]");
                    } else {
                        System.out.println("name of player = " + getNameOfPlayer());
                        String msgToServer = getNameOfPlayer() + ":" + msgEdited;
                        System.out.println("Msg to server: " + msgToServer);
                        if (msgToServer != null) {
                            writer.println(msgToServer);
                            writer.flush();
                            while ((msgFromServer = reader.readLine()) != null) {
                                if (msgFromServer.equalsIgnoreCase("endMessage")) break;
                                System.out.println("Scan reply from server: " + msgFromServer);
                            }
                        }
                    }
                    msgEntered = "";
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Ошибка ввода");
            }
        }
    }*/

    public void closeConnection() {
        try {
            System.out.println("Trying to close");
            writer.close();
            System.out.println("Writer closed");
            reader.close();
            System.out.println("Reader closed");
            keyboard.close();
            System.out.println("Keyboard closed");
            sock.close();
            System.out.println("Socket closed");

        } catch (Exception e) {
            System.out.println("Failed to close connections");
        }
    }

    public void startGettigFiels(){
        try {
            fileInputStream = new ObjectInputStream(sock.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
