package warships.client;


import com.fasterxml.jackson.databind.ObjectMapper;
import warships.server.Field;
import warships.server.Game;

import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

public class Player {

    private String nameOfPlayer;

    private BufferedReader keyboard;
    private PrintWriter writer;
    private ObjectInputStream ois;
    private Socket sock;

    private int currentStep = 0;

    private boolean check;

    public static void main(String[] args) {
        new Player().go();
    }

    private void go() {
        connect();
        keyboardListener();

        new Thread(new PlayerListener()).start();
        new Thread(new PlayerWriter()).start();
        new Thread(new Pinger()).start();

    }

    private void connect(){
        try {
            printMessage("Attempt to connect...");
            sock = new Socket("127.0.0.1", 4949);
            //sock = new Socket("176.105.12.145", 4444);
        } catch (Exception ex) {
            printMessage("Socket connection exception");
        }
    }

    private void keyboardListener() {
        try {
            keyboard = new BufferedReader(new InputStreamReader(System.in));
            printMessage("Connection with keyboard created.");
        } catch (Exception ex) {
            ex.printStackTrace();
            closeConnection();
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

            //noinspection InfiniteLoopStatement
            while (true) {
                String receivedMessage;
                try {
                    receivedMessage = (String) ois.readObject();
                    /*
                    //После получения строки GameFile - получаем файл с игрой
                    //и обрабатываем его
                     */
                    if (receivedMessage.equalsIgnoreCase("GameFile")){
//                        String gama = (String) ois.readObject();
//                        Game currentGame = (Game) fromString(gama);

                        StringReader reader = new StringReader((String) ois.readObject());
                        ObjectMapper mapper = new ObjectMapper();

//                        System.out.println("Получение игры");
                        Game currentGame = mapper.readValue(reader, Game.class);
//                        System.out.println("Класс игры получен");

                        if (currentGame != null) {
//                        System.out.println("Actual step= " + getCurrentStep());
                            setCurrentStep(currentGame.getStep());
//                        System.out.println("New step= " + getCurrentStep());
                        /*
                        Если имя игрока в игре занесено как первый игрок.
                         */
                            Field playerField;
                            Field enemyField;
                            if (getNameOfPlayer().equals(currentGame.playersInGame[0])) {
                                playerField = currentGame.getFirstField();
                                enemyField = currentGame.getSecondField();
                            }
                        /*
                        Если имя игрока в игре занесено как второй игрок
                         */
                            else {
                                playerField = currentGame.getSecondField();
                                enemyField = currentGame.getFirstField();
                            }
                            if (playerField != null && enemyField != null) {
                                printMessage("Player field:");
                                playerField.print(getNameOfPlayer());
                                printMessage("Enemy field:");
                                enemyField.print("");
                                printMessage("Step of game: " + currentGame.getStep() + ".");
                                printMessage(currentGame.showScore());
                            }
                        }
                    }
                    else if (receivedMessage.equals("CHECKDONE"))
                        setCheck(false);
                    else{
                        printMessage("Received message: " + receivedMessage);
                    }

                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                    closeConnection();
                }
            }

        }

//        private Object fromString(String gama) throws IOException, ClassNotFoundException {
//            byte [] gameBytes = Base64.getDecoder().decode(gama);
//            ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(gameBytes));
//            Object obj = objectInputStream.readObject();
//            objectInputStream.close();
//            return  obj;
//        }
    }

    private class PlayerWriter implements Runnable {
        @Override
        public void run() {
            try {
                printMessage("Writer loading.");
                writer = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
                printMessage("Writer created.");
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
//                            System.out.println("first word = " + firstWord);
//                            System.out.println("second word = " + secondWord);
                            if (firstWord.equalsIgnoreCase("name")) {
                                setNameOfPlayer(secondWord);
                            }
                                /*
                                //Если игрок не присваивает себе имя - идёт проверка на наличие имени
                                 */
                        } else if (getNameOfPlayer() == null) {
                            printMessage("Enter your name first by [name = <your name>]");
                        } else {
//                            System.out.println("name of player = " + getNameOfPlayer());
                            String msgToServer = getNameOfPlayer() + ":" + msgEdited;
//                            System.out.println("Msg to server: " + msgToServer);
                            writer.println(msgToServer);
                        }
                        writer.flush();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    printMessage("Ошибка ввода");
                    closeConnection();
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
            int seconds = 5;
            try {
                Thread.sleep(seconds*1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //noinspection InfiniteLoopStatement
            while (true){
                String checkMessage;
                try {
                    if (getNameOfPlayer()!=null && !isCheck()) {
//                        System.out.println("i want to send 'check'");
                        checkMessage = (getNameOfPlayer() + ":Check:" + getCurrentStep());
//                        System.out.println((getNameOfPlayer() + ":Check:" + getCurrentStep()));
                        writer.println(checkMessage);
                        writer.flush();
                        setCheck(true);
                    }
                    Thread.sleep(seconds * 1000);
                } catch (InterruptedException e) {
                    closeConnection();
                    e.printStackTrace();
                }
            }
        }
    }

    private String getNameOfPlayer() {
        return nameOfPlayer;
    }

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
            System.out.println("Trying to close connections:");
            writer.close();
            System.out.println("Writer closed");
            keyboard.close();
            System.out.println("Keyboard closed");
            ois.close();
            System.out.println("OIS closed");
            sock.close();
            System.out.println("Socket closed");

        } catch (Exception e) {
            System.out.println("Failed to close connections");
        }
    }

    private boolean isCheck() {
        return check;
    }

    private void setCheck(boolean check) {
        this.check = check;
    }

    private void printMessage(String msg){
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date();
        String time = sdf.format(date);
        System.out.println(time + ": " + msg);

    }

}
