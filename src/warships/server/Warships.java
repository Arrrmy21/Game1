package warships.server;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import warships.DB.WarshipsPGDBConnector;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Warships {

    private ServerSocket serverSocket;
    private int playerID = 1;
    private static int gameID = 1;
    private WarshipsPGDBConnector connectorDB = new WarshipsPGDBConnector();


    private static ArrayList<ClientHandler> clients = new ArrayList<>();

//    private static HashMap<String, Integer> playerData = new HashMap<>();

//    static HashMap<Integer, Game> gameList = new HashMap<>();

    public static void main(String[] args) throws IOException, SQLException {

        WarshipsPGDBConnector clear = new WarshipsPGDBConnector();
        clear.clearData();
        Warships warships = new Warships();
        new Thread(new QueueOfPlayersHandler()).start();
        warships.go();
    }

    @SuppressWarnings("InfiniteLoopStatement")
    private void go() {

        try {
            serverSocket = new ServerSocket(4949);
            while (true) {
                System.out.println();
                printMessage("********************Server is waiting for client.");
                Socket clientSocket = serverSocket.accept();
                ClientHandler client = new ClientHandler(clientSocket, getPlayerAmountID());
                incrementPlayerAmountID();
                clients.add(client);
                new Thread(client).start();
                printMessage("Player connected to server.");
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
//    private synchronized Game checkGameStatus(String nameOP) {
//
//        Game gm = null;
//        for (Game gameValues : gameList.values()) {
//            for (int i = 0; i < 2; i++) {
//                if (gameValues.playersInGame[i].equalsIgnoreCase(nameOP)) {
//                    if (!gameValues.isEndOfGame()) {
//                        gm = gameValues;
//                    }
//                }
//            }
//        }
//        return gm;
//    }

    //    Получение игры из базы, если игра не закончилась.
    private synchronized Game getGame(int playerID) {
        Game currGame = null;

        try {
            String nameOP = connectorDB.returnPlayerName(playerID);
            int gameID = connectorDB.getGameID(nameOP);
            Game cg = connectorDB.getGameFromData(gameID);
            if (!cg.isEndOfGame())
                currGame = cg;
        } catch (SQLException e) {
            e.printStackTrace();
        }
//        for (Game game : gameList.values()) {
//            if (game.playersInGame[0].equalsIgnoreCase(nameOP) || game.playersInGame[1].equalsIgnoreCase(nameOP)) {
//                currGame = game;
//            } else {
//                System.out.println("Trouble with game in getGame method!!");
//            }
//        }
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

    static synchronized void incrementGameAmount() {
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
    private synchronized boolean checkPlayerNameInDB(String nameOP) {
        boolean isNameExist = false;
        try {
            if (connectorDB.isNameExist(nameOP)) {
                isNameExist = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
//        for (String name : playerData.keySet()) {
//            if (nameOP.equalsIgnoreCase(name))
//                isNameExist = true;
//        }
        return isNameExist;
    }

    //    Отправка сообщения клиенту, где message - сообщение, id- номер клиента в базе.
    static synchronized void writeMsgToClient(String message, int id) {
        try {
            for (ClientHandler client : clients) {
                if (id == client.pID) {
                    client.oos.writeObject(message);
                    client.oos.flush();
                    printMessage("Message [" + message + "] sent to client id: " + id);
                    break;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            printMessage("Sending error");
        }
    }

    //    Проверка наличия имени игрока в базе имён
    private synchronized boolean authorize(String nameOP, int playerID) {
        boolean auth = false;
        //Если в базе нет такого имени - заносим в базу.
//        boolean isNameExist;
        if (!checkPlayerNameInDB(nameOP)) {
//            playerData.put(nameOP, id);
            try {
                connectorDB.putPlayerNameToDB(playerID, nameOP);
                printMessage("Name [" + nameOP + "] added to data.");
                writeMsgToClient("Your name added to data.", playerID);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            auth = true;
        }
        //Если имя существует - проверяем совпадение id
        else {
//            System.out.println("playerData.get(nameOfPlayer)= " + playerData.get(nameOP));
//            System.out.println("pID = " + id);

//            if (playerData.get(nameOP) == id) {
            try {
                int idOfPlayerFromDB = connectorDB.getPlayerIDFromDB(nameOP);
                if (idOfPlayerFromDB == playerID) {
                    auth = true;
                } else {
                    writeMsgToClient("Name [" + nameOP + "] is already exist. Enter another name.", playerID);
                    printMessage("Name [" + nameOP + "] is already exist.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
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
                printMessage("Input stream created");
                oos = new ObjectOutputStream(sock.getOutputStream());
                printMessage("Object Output stream created");

            } catch (IOException e) {
                printMessage("Input stream reader failed.");
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
                        printMessage(".....Server got message......");
                        printMessage("Message from client: " + msgFromClient);
                        String[] getMsg = msgFromClient.split(":");
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
                            if (!getMsg[2].contains(",")) {
                                clientStepOfGame = Integer.parseInt(getMsg[2]);
                                printMessage("Step of game = " + clientStepOfGame);
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
                            if (authorize(nameOfPlayer, pID))
//                            writeMsgToClient("Authorize passed", pID);

                                switch (command) {
//                                case STARTSOLO:
//                                    Game soloGame = new Game(nameOfPlayer, playerID, getGameID());
//                                    System.out.println("New game was created");
////                                        ArrayList<String> playerList = new ArrayList<>();
////                                        playerList.add(nameOfPlayer);
////                                        gameData.put(currentGame, playerList);
////                                    gameList.put(getGameID(), soloGame);
//                                    connectorDB.putGameToData(getGameID(), soloGame);
//                                    incrementGameAmount();
//                                    writeMsgToClient("New game was created", pID);
//                                    break;
                                    case START:
                                        QueueOfPlayersHandler.queueOfPlayers.offer(nameOfPlayer);
                                        writeMsgToClient("Waiting for an opponent", pID);
                                        break;
                                    case CHECK:
                                        boolean isAvail = connectorDB.isGameAvailable(pID);
                                        if (isAvail) {
                                            int checkStep = connectorDB.getStepOfGame(nameOfPlayer);
                                            printMessage("Command: CHECK. ChecksStep = " + checkStep + ".");

                                            if (clientStepOfGame != checkStep || clientStepOfGame == 0) {
                                                sendGameToClient();
                                            }
                                        } else {
                                            writeMsgToClient("Game is not available", pID);
                                        }
                                        oos.writeObject("CHECKDONE");
                                        oos.flush();
                                        break;
                                    default:

                                        if (connectorDB.isGameAvailable(pID)) {
                                            Game currentGame = getGame(pID);

                                            if (currentGame.turnToMove().equals(nameOfPlayer)) {
                                                currentGame.makeMove(nameOfPlayer, command, coord);

                                                if (currentGame.getStep() == 0) {
                                                    connectorDB.putGameToData(currentGame.getGID(), currentGame);
                                                    printMessage("GAME SENDING TO DATA");
//                                                gameList.put(currentGame.getGID(), currentGame);
                                                } else {
                                                    connectorDB.updateGameToData(currentGame.getGID(), currentGame, nameOfPlayer, coord);
                                                    printMessage("GAME REWRITTEN TO DATA");
                                                }
                                            } else {
                                                writeMsgToClient("It's not your turn to move", pID);
                                            }
                                        } else {
                                            writeMsgToClient("Game is not available", pID);
                                        }

                                }


                            //Если авторизоваться не получилось:
                        } catch (NullPointerException ex) {
                            ex.printStackTrace();
                            printMessage("No Game Class get yet.");

                        } catch (Exception e) {
                            e.printStackTrace();
                            printMessage("Error 1");
                        }

                    }
                    Thread.sleep(100);
                }
            } catch (Exception e) {
                printMessage("beda-beda");
                closeClientConnections();
                e.printStackTrace();

            }
        }

        /*
        Отправление игры клиенту
         */
        private synchronized void sendGameToClient() throws IOException {
            printMessage("-----------> Sending game to client");
            oos.writeObject("GameFile");
            oos.flush();

            Game game = getGame(pID);

//            String gameString = toString(game);
//            oos.writeObject(gameString);

            StringWriter writer = new StringWriter();
            ObjectMapper mapper = new ObjectMapper();
//            String jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(game);
//            System.out.println(jsonString);
            mapper.writeValue(writer, game);
            oos.writeObject(writer.toString());
            oos.flush();
            writeMsgToClient("Turn to move of player: " + game.turnToMove(), pID);
        }

        /*
        Стандартная сериализация экземпляра класса Game в String
         */
//        private String toString(Serializable o) throws IOException {
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            ObjectOutputStream objectOutputStream = new ObjectOutputStream(baos);
//            objectOutputStream.writeObject(o);
//            objectOutputStream.close();
//            return Base64.getEncoder().encodeToString(baos.toByteArray());
//        }

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

    static void printMessage(String msg) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date();
        String time = sdf.format(date);
        System.out.println(time + ": " + msg);
    }

    /*
    При команде START игрок попадает в очередь. При количестве людей в очереди = 2 создается игра.
     */
    private static class QueueOfPlayersHandler implements Runnable {

        WarshipsPGDBConnector connector = new WarshipsPGDBConnector();
        //Очередь из игроков:
        private static Queue<String> queueOfPlayers = new LinkedList<>();

        @Override
        public void run() {
            //noinspection InfiniteLoopStatement
            while (true) {
                if (queueOfPlayers.size() > 1) {
                    String fistInQueue = queueOfPlayers.poll();
                    String secondInQueue = queueOfPlayers.poll();
                    int fpID = 0;
                    int spID = 0;
                    try {
                        fpID = connector.getPlayerIDFromDB(fistInQueue);
                        spID = connector.getPlayerIDFromDB(secondInQueue);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                    int gameID = getGameID();
                    Game game = new Game(fistInQueue, fpID, secondInQueue, spID, gameID);
//                    gameList.put(getGameID(), game);
                    printMessage("New game with players " + fistInQueue + " and " + secondInQueue + " created.");

                    writeMsgToClient("Your game with player " + secondInQueue + " is ready", fpID);
                    writeMsgToClient("Your game with player " + fistInQueue + " is ready", spID);
                    try {
                        connector.putGameToData(gameID, game);
                        printMessage("New game added to data.");
                    } catch (JsonProcessingException | SQLException e) {
                        e.printStackTrace();
                    }
                    incrementGameAmount();

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
}