package warships.client;


import warships.server.Field;
import warships.server.Game;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicReference;

public class Player {

    private String nameOfPlayer;

    private BufferedReader keyboard;
    private PrintWriter writer;
    private ObjectInputStream ois;

    private Socket sock;

    Game currentGame= null;
    private int currentStep = 0;

    public static void main(String[] args) {
        new Player().go();
    }

    private void go() {
        connect();
        setUpNetwork();

        new Thread(new PlayerListener()).start();
        new Thread(new PlayerWriter()).start();
        new Thread(new Pinger()).start();
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

        @Override
        public void run() {
            try {
                ois = new ObjectInputStream(sock.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }

            while (true) {
                AtomicReference<String> receivedMessage;
                receivedMessage = new AtomicReference<>();
                try {
                    receivedMessage.set((String) ois.readObject());
                    /*
                    //После получения строки GameFile - получаем файл с игрой
                    //и обрабатываем его
                     */
                    if (receivedMessage.get().equalsIgnoreCase("GameFile")){
                        currentGame = (Game) ois.readObject();
                        System.out.println("Actual step= " + getCurrentStep());
                        setCurrentStep(currentGame.getStep());
                        System.out.println("New step= " + getCurrentStep());
                        /*
                        Если имя игрока в игре занесено как первый игрок.
                         */
                        Field playerField;
                        Field enemyField;
                        if(getNameOfPlayer().equals(getCurrentGame().playersInGame[0])) {
                            playerField = getCurrentGame().firstField;
                            enemyField = getCurrentGame().secondField;
                        }
                        /*
                        Если имя игрока в игре занесено как второй игрок
                         */
                        else {
                            playerField = getCurrentGame().secondField;
                            enemyField = getCurrentGame().firstField;
                        }
                        System.out.println("Player field:");
                        playerField.print(nameOfPlayer);
                        System.out.println("Enemy field:");
                        enemyField.print("");
                        currentGame = null;
                    }
                    else{
                        System.out.println("Received message: " + receivedMessage.get());
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
                writer = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
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
                            writer.println(msgToServer);
                        }
                        writer.flush();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Ошибка ввода");
                }
            }
        }
    }

    /*
    Класс каждые "seconds" секунд отправляет на сервер команду "Check" с актуальным номером хода для игрока.
    Если номер на сервере отличается, значит другой игрок походил и сервер отправит данные об игре.
     */
    public class Pinger implements Runnable{
        @Override
        public void run() {
            int seconds = 8;
            System.out.println("Pinger creating");
            try {
                Thread.sleep(seconds*1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            while (true){
                String checkMessage;
                try {
                    if (getNameOfPlayer()!=null) {
//                        System.out.println("i want to send 'check'");
                        checkMessage = (getNameOfPlayer() + ":Check:" + getCurrentStep());
//                        System.out.println("CCCCCCCCheck msg: " + checkMessage);
                        writer.println(checkMessage);
                        writer.flush();
                    }
                    Thread.sleep(seconds * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String getNameOfPlayer() {
        return nameOfPlayer;
    }

    private Game getCurrentGame() {
        return currentGame;
    }

//    public void setCurrentGame(Game currentGame) {
//        this.currentGame = currentGame;
//    }

    private synchronized int getCurrentStep() {
        return currentStep;
    }

    private void setCurrentStep(int curStep) {
        currentStep = curStep;
    }

    private void setNameOfPlayer(String name) {
        this.nameOfPlayer = name;
    }

    private void closeConnection() {
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
