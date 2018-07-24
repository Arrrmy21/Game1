package warships.client;


import warships.server.Field;
import warships.server.Game;

import java.io.*;
import java.net.Socket;

public class Player {

    private String nameOfPlayer;

//    private BufferedReader reader;
    private BufferedReader keyboard;
    private PrintWriter writer;
    private ObjectInputStream ois;

    private Socket sock;
    protected Game currentGame;

    protected Field playerField;
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

            System.out.println("Connection with keyboard created.");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private class PlayerListener implements Runnable {

        int k = 0;
        @Override
        public void run() {
            try {
//                System.out.println("Reader loading...");
                ois = new ObjectInputStream(sock.getInputStream());
//                System.out.println("Reader created");
            } catch (IOException e) {
                e.printStackTrace();
            }

            while (true) {
                String receivedMessage;
                try {
//                    System.out.println("Reading object");
//                    System.out.println("Identifying object");
                    receivedMessage = (String)ois.readObject();
                    /*
                    //После получения строки GameFile - получаем файл с игрой
                    //и обрабатываем его
                     */
                    if (receivedMessage.equalsIgnoreCase("GameFile")){
//                        System.out.println("Object type = Game");
                        currentGame = (Game) ois.readObject();
                        playerField = currentGame.firstField;
                        enemyField = currentGame.secondField;
                        System.out.println("Your field");
                        playerField.print(nameOfPlayer);
                        System.out.println();
                        enemyField.print("BOT");
                        System.out.println("PLAYER FILES OWNER: " + playerField.getFieldOwner());
                    }
                    else{
                        System.out.println("Received message: " + receivedMessage);
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class PlayerWriter implements Runnable {
            @Override
            public void run() {
                try {
                    System.out.println("Writer loading...");
                    writer = new PrintWriter( new OutputStreamWriter(sock.getOutputStream()));
                    System.out.println("Writer created");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                while (true) {
                    try {
                        String msgEntered = keyboard.readLine();

                        if (!msgEntered.isEmpty()) {
                            String firstWord;
                            String secondWord;
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

    public String getNameOfPlayer() {
        return nameOfPlayer;
    }

    public void setNameOfPlayer(String name) {
        this.nameOfPlayer = name;
    }

    public void closeConnection() {
        try {
            System.out.println("Trying to close");
            writer.close();
            System.out.println("Writer closed");
//            reader.close();
//            System.out.println("Reader closed");
            keyboard.close();
            System.out.println("Keyboard closed");
            sock.close();
            System.out.println("Socket closed");

        } catch (Exception e) {
            System.out.println("Failed to close connections");
        }
    }



}
