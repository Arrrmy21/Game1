package warships.server;


import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Warships {

    private ServerSocket serverSocket;
    private int playerID = 1;
    private static int gameID = 1;


    private static ArrayList<ClientHandler> clients = new ArrayList<>();

    private static HashMap<String, Integer> playerData = new HashMap<>();

    static HashMap<Integer, Game> gameList = new HashMap<>();

    public static void main(String[] args) throws IOException {

        Warships warships = new Warships();
        new Thread(new QueueOfPlayersHandler()).start();
        warships.go();
    }

    private void go() {

        try {
            serverSocket = new ServerSocket(4949);
            while (true) {
                System.out.println("----------Server is waiting for client.");
                Socket clientSocket = serverSocket.accept();
                ClientHandler client = new ClientHandler(clientSocket, getPlayerAmountID());
                incrementPlayerAmountID();
                clients.add(client);
                new Thread(client).start();
                System.out.println("Player connected to server.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            closeConnection();
        }
    }

    private void closeConnection() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //    Проверяем если ли игрок с таким именем в какой-то игре и активна ли игра.
    private synchronized Game checkGameStatus(String nameOP) {

        Game gm = null;
        for (Game gameValues : gameList.values()) {
            for (int i = 0; i < 2; i++) {
                if (gameValues.playersInGame[i].equalsIgnoreCase(nameOP)) {
                    if (!gameValues.isEndOfGame()) {
                        gm = gameValues;
                    }
                }
            }
        }
        return gm;
    }

    //    Получение игры из базы, если игра не закончилась.
    private synchronized Game getGame(String nameOP) {
        Game currGame = null;
        for (Game game : gameList.values()) {
            if (game.playersInGame[0].equalsIgnoreCase(nameOP) || game.playersInGame[1].equalsIgnoreCase(nameOP)) {
                currGame = game;
            } else {
                System.out.println("Trouble with game in getGame method!!");
            }
        }
        return currGame;
    }

    //    Вовращение количества подключенных игроков
    private synchronized int getPlayerAmountID() {
        return playerID;
    }

    private synchronized void incrementPlayerAmountID() {
        playerID++;
    }

    //    Возвращение количества созданных игр
    static synchronized int getGameID() {
        return gameID;
    }

    static synchronized void incrementGameID() {
        gameID++;
    }

    private synchronized Commands detectCommand(String stringFromUser) {
        Commands command;
        switch (stringFromUser.toLowerCase()) {
            case "shoot":
                command = Commands.SHOOT;
                break;
            case "put":
                command = Commands.PUT;
                break;
            case "start":
                command = Commands.START;
                break;
            case "startsolo":
                command = Commands.STARTSOLO;
                break;
            case "check":
                command = Commands.CHECK;
                break;
            default:
                command = Commands.HELP;
                break;
        }
        return command;
    }

    private synchronized Coord getCoodrinates(String stringFromClient) {

        String strCoords[] = stringFromClient.split(",");
        int[] intCoords = new int[2];
        for (int i = 0; i < 2; i++)
            intCoords[i] = Integer.parseInt(strCoords[i]);
        return new Coord(intCoords[0], intCoords[1]);
    }

    //    Проверка наличия имени в базе
    private synchronized boolean checkStatusOfPlayerName(String nameOP) {
        boolean isNameExist = false;
        for (String name : playerData.keySet()) {
            if (nameOP.equalsIgnoreCase(name))
                isNameExist = true;
        }
        return isNameExist;
    }

    //    Отправка сообщения клиенту, где message - сообщение, id- номер клиента в базе.
    static synchronized void writeMsgToClient(String message, int id) {
        try {
            for (ClientHandler client : clients) {
                if (id == client.pID) {
//                    client.writer.println(message);
                    client.oos.writeObject(message);
                    client.oos.flush();
                    System.out.println("Message [" + message + "] sent to client.");
                    break;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Sending error");
        }
    }

    //    Проверка наличия имени игрока в базе имён
    private synchronized boolean authorize(String nameOP, int id) {
        boolean auth = false;
        //Если в базе нет такого имени - заносим в базу.
        if (!checkStatusOfPlayerName(nameOP)) {
            playerData.put(nameOP, id);
            System.out.println("Name [" + nameOP + "] added to data.");
            writeMsgToClient("Your name added to data.", id);
            auth = true;
        }
        //Если имя существует - проверяем совпадение id
        else {
//            System.out.println("playerData.get(nameOfPlayer)= " + playerData.get(nameOP));
//            System.out.println("pID = " + id);
            if (playerData.get(nameOP) == id) {
                auth = true;
            } else {
                writeMsgToClient("Name [" + nameOP + "] is already exist. Enter another name.", id);
                System.out.println("Name [" + nameOP + "] is already exist.");
            }
        }
        return auth;

    }

    private void deleteClientFromList(int id) {
        for (ClientHandler client : clients) {
            if (client.pID == id) {
                clients.remove(client);
            }
        }
    }

    public class ClientHandler implements Runnable {

        int pID;
        BufferedReader reader;
        Socket sock;
        ObjectOutputStream oos;
        String nameOfPlayer;

        ClientHandler(Socket clientSocket, int id) {
            pID = id;
            this.sock = clientSocket;

            try {
                reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                System.out.println("Input stream created");
                oos = new ObjectOutputStream(sock.getOutputStream());
                System.out.println("Object Output stream created");

            } catch (IOException e) {
                System.out.println("Input stream reader failed.");
                e.printStackTrace();
                deleteClientFromList(pID);
            }
        }

        @Override
        public void run() {

            try {
                //noinspection InfiniteLoopStatement
                while (true) {
                    Commands command;
                    Coord coord = null;
                    String msgFromClient;
                    int clientStepOfGame = 0;
//на 2 команды
                    if (!(msgFromClient = reader.readLine()).isEmpty()) {
//         ===========================Parse_start=============================
                        System.out.println(".....Server got message......");
                        System.out.println("Message from client: " + msgFromClient);
                        String[] getMsg = msgFromClient.split(":");
//                        System.out.println("getMsg length = " + getMsg.length);
                        if (getMsg.length == 2) {
                            nameOfPlayer = getMsg[0];
                            command = detectCommand(getMsg[1]);
                        }
                            /*
                            *если в getMsg[2] 1 симаол - это № хода игры. Если >1 - это координата.
                             */
                        else if (getMsg.length == 3) {
                            nameOfPlayer = getMsg[0];
                            command = detectCommand(getMsg[1]);
                            if (getMsg[2].length() == 1) {
                                clientStepOfGame = Integer.parseInt(getMsg[2]);
                                System.out.println("Step of game = " + clientStepOfGame);
                            } else {
                                coord = getCoodrinates(getMsg[2]);
                            }
                        } else {
                            writeMsgToClient("Wrong command", pID);
                            break;
                        }
//         ===========================Parse_end=============================

//                        String enteredCommand = "Entered command : " + command;
//                        writeMsgToClient(enteredCommand, pID);

                        //Обработка сообщения от клиента
                        try {
                            //Проверка имени в базе
                            authorize(nameOfPlayer, pID);
//                            writeMsgToClient("Authorize passed", pID);
                            System.out.println("Trying to get Game from data");
                            Game currentGame = getGame(nameOfPlayer);

                            switch (command) {
                                case STARTSOLO:
                                    Game soloGame = new Game(nameOfPlayer, getGameID());
                                    System.out.println("New game was created");
//                                        ArrayList<String> playerList = new ArrayList<>();
//                                        playerList.add(nameOfPlayer);
//                                        gameData.put(currentGame, playerList);
                                    gameList.put(getGameID(), soloGame);
                                    incrementGameID();
                                    writeMsgToClient("New game was created", pID);
                                    break;
                                case START:
                                    QueueOfPlayersHandler.queueOfPlayers.offer(nameOfPlayer);
                                    writeMsgToClient("Waiting for an opponent", pID);
                                    break;
                                case CHECK:

                                    System.out.println("Command: Check. getGame(nameOfPlayer).getStep()= " + getGame(nameOfPlayer).getStep());
                                    if (clientStepOfGame != getGame(nameOfPlayer).getStep() || clientStepOfGame == 0) {
                                        sendGameToClient();
                                    }
                                    break;
                                default:
                                    if (currentGame.turnToMove().equals(nameOfPlayer)) {
                                        currentGame.makeMove(nameOfPlayer, command, coord);
                                        gameList.put(currentGame.getGID(), currentGame);
                                        System.out.println("GAME REWRITTEN TO DATA");
                                    } else {
                                        writeMsgToClient("It's not your turn to move", pID);
                                    }
                            }


                            //Если авторизоваться не получилось:
                        } catch (NullPointerException ex) {
                            ex.printStackTrace();
                            System.out.println("No Game Class get yet.");

                        } catch (Exception e) {
                            e.printStackTrace();
                            System.out.println("Error 1");
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("beda-beda");
                closeClientConnections();
                e.printStackTrace();

            }
        }

        /*
        Отправление игры клиенту
         */

        private synchronized void sendGameToClient() throws IOException {
            System.out.println("----> Sending game to client");
            oos.writeObject("GameFile");
            oos.flush();
            Game game = getGame(nameOfPlayer);
            String gameString = toString(game);
            oos.writeObject(gameString);
            oos.flush();
            writeMsgToClient("Turn to move of player: " + game.turnToMove(), pID);
        }

        /*
        Сериализация экземпляра класса Game в String
         */
        private String toString(Serializable o) throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(baos);
            objectOutputStream.writeObject(o);
            objectOutputStream.close();
            return Base64.getEncoder().encodeToString(baos.toByteArray());

        }

        void closeClientConnections() {
            try {
                reader.close();
                oos.close();
                sock.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /*
    При команде START игрок попадает в очередь. При количестве людей в очереди = 2 создается игра.
     */
    private static class QueueOfPlayersHandler implements Runnable {

        //Очередь из игроков:
        private static Queue<String> queueOfPlayers = new LinkedList<>();

        @Override
        public void run() {
            while (true) {
                if (queueOfPlayers.size() > 1) {
                    String fistInQueue = queueOfPlayers.poll();
                    String secondInQueue = queueOfPlayers.poll();

                    Game game = new Game(fistInQueue, secondInQueue, getGameID());
                    gameList.put(getGameID(), game);
                    incrementGameID();
                    System.out.println("New game with players " + fistInQueue + " and " + secondInQueue + " created");

                    writeMsgToClient("Your game with player " + secondInQueue + " is ready", playerData.get(fistInQueue));
                    writeMsgToClient("Your game with player " + fistInQueue + " is ready", playerData.get(secondInQueue));

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    //System.out.println("Players in queue = " + queueOfPlayers.size());
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /*
   //Класс для проверки изменений состяния игры на сервере и отправки её игрокам
    */


}
