package warships.server;


import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

public class Warships {

    ServerSocket serverSocket;
    private int playerID = 0;


    static ArrayList<ClientHandler> clients = new ArrayList<>();

    private static HashMap<String, Integer> playerData = new HashMap<>();

    private static HashMap<Game, ArrayList> gameData = new HashMap<>();


    public static void main(String[] args) throws IOException {

        Warships warships = new Warships();
        new Thread(new QueueOfPlayersHandler()).start();
        warships.go();
    }

    private void go() {

        try{
            serverSocket = new ServerSocket(4949);
            while(true){
                System.out.println("Server is waiting for client.");
                Socket clientSocket = serverSocket.accept();
                ClientHandler client = new ClientHandler(clientSocket, ++playerID);
                clients.add(client);
                Thread t = new Thread(client);
                t.start();
                System.out.println("Player connected to server.");
            }
        }
        catch (Exception e){
            e.printStackTrace();
            closeConnection();
        }
    }

    public void closeConnection() {
        try {

            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    Существует ли имя в базе игроков.


//    Проверяем если ли игрок с таким именем в какой-то игре и активна ли игра.
    public synchronized boolean checkGameStatus(String nameOP){
        boolean isGameAvailable = false;
        for (Game gameKeys : gameData.keySet()){
            for(int i = 0; i<2; i++){
                if (gameKeys.playersInGame[i].equalsIgnoreCase(nameOP)){
                    if(gameKeys.isEndOfGame() == false) {
                        isGameAvailable = true;
                        break;
                    }
                }
            }
        }
        return isGameAvailable;
    }

    public synchronized Game getGame(String nameOP){
        Game currGame = null;
        for(Game game : gameData.keySet()){
            if (game.playersInGame[0].equalsIgnoreCase(nameOP) || game.playersInGame[1].equalsIgnoreCase(nameOP)){
                currGame = game;
            }
        }
        return currGame;
    }

    public synchronized Commands detectCommand(String stringFromUser){
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
    public synchronized boolean checkStatusOfPlayerName(String nameOP) {
        boolean isNameExist = false;
            for (String name : playerData.keySet()) {
                if (nameOP.equalsIgnoreCase(name)) {
                    isNameExist = true;
                } else {
                    isNameExist = false;
                }
            }
        return isNameExist;
    }

//    Отправка сообщения клиенту, где message - сообщение, id- номер клиента в базе.
    public void writeMsgToClient(String message, int id){
        try {
            for (ClientHandler client : clients){
                if (id==client.pID){
//                    client.writer.println(message);
                    client.oos.writeObject(message);
                    client.oos.flush();
                    System.out.println("Message [" + message + "] sent to client.");
                    break;
                }
            }
        }
        catch (Exception ex){
            ex.printStackTrace();
            System.out.println("Sending error");
        }
    }

    public synchronized boolean authorize(String nameOP, int id){
        boolean auth = false;
        //Если нет - заносим в базу.
        if(checkStatusOfPlayerName(nameOP)==false){
            playerData.put(nameOP, id);
            System.out.println("Name [" + nameOP + "] added to data.");
            writeMsgToClient("Your name added to data.", id);
            auth = true;
        }
        //Если имя существует - проверяем совпадение id
        else {
            System.out.println("playerData.get(nameOfPlayer)= " + playerData.get(nameOP));
            System.out.println("pID = " + id);
            if(playerData.get(nameOP)==id) {
                auth = true;
            }
            else {
                writeMsgToClient("Name [" + nameOP + "] is already exist. Enter another name.", id);
                System.out.println("Name [" + nameOP + "] is already exist.");
            }
        }
        return auth;

    }

    public void deleteClientFromList(int id){
        for(ClientHandler client : clients){
            if(client.pID==id){
                clients.remove(client);
            }
        }
    }

    public class ClientHandler implements Runnable {

        int pID;
        BufferedReader reader;
        Socket sock;
        ObjectOutputStream oos;



        public ClientHandler(Socket clientSocket, int id) {
            pID = id;
            this.sock = clientSocket;

            try {
                reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                System.out.println("Input stream created");
//                writer = new PrintWriter(sock.getOutputStream());
//                System.out.println("Output stream created");

                oos = new ObjectOutputStream(sock.getOutputStream());
                System.out.println("Object Output stream created");

            } catch (IOException e) {
                System.out.println("Input stream reader failed.");
                e.printStackTrace();
            }
        }

        @Override
        public void run() {

            try {
                //noinspection InfiniteLoopStatement
                while (true) {
                    Game currentGame;
                    String nameOfPlayer;
                    Commands command;
                    Coord coord = null;
                    String msgFromClient;
                    int stepOfGame = 999;

                    while (!(msgFromClient = reader.readLine()).isEmpty()) {
                        //Parse
                        String[] getMsg = msgFromClient.split(":");
                        System.out.println(".....Server got message......");
                        System.out.println("getMsg length = " + getMsg.length);
                        if (getMsg.length == 2) {
                            nameOfPlayer = getMsg[0];
                            command = detectCommand(getMsg[1]);
                        } else if (getMsg.length == 3) {
                            nameOfPlayer = getMsg[0];
                            command = detectCommand(getMsg[1]);
                            /*
                            //если в getMsg[2] 1 симаол - это № хода игры. Если >1 - это координата.
                             */
                            if(getMsg[2].length()==1){
                                stepOfGame = Integer.parseInt(getMsg[2]);
//                                System.out.println("Step og game = " + stepOfGame);
                            }
                            else
                            coord = getCoodrinates(getMsg[2]);
                        } else {
                            writeMsgToClient("Wrong command", pID);
                            break;
                        }
                        String enteredCommand = "Entered command : " + command;
                        writeMsgToClient(enteredCommand, pID);

                        System.out.println("MMMsg from client: " + msgFromClient);

                        //Обработка сообщения от клиента

                        try {
                            //Авторизация
                            if (authorize(nameOfPlayer, pID)) {
                                writeMsgToClient("Authorize passed", pID);
                                //Существует ли игра
                                if (checkGameStatus(nameOfPlayer)) {
                                    writeMsgToClient("Your game is available", pID);
                                    currentGame = getGame(nameOfPlayer);
                                    //Не закончилась ли игра
                                    if (!currentGame.isEndOfGame()) {


                                        //Чья очередь ходить
                                        if (currentGame.turnToMove(nameOfPlayer)) {
                                            currentGame.makeMove(nameOfPlayer, command, coord);
                                            currentGame.checkWinStatus();
                                            //Реакция на команду игрока
                                            /*switch (command) {
                                                case SHOOT:
                                                    currentGame.makeShoot(nameOfPlayer, coord);
                                                    break;
                                                case PUT:
                                                    currentGame.putShip(nameOfPlayer, coord);
                                                     break;
                                            }*/
                                            writeMsgToClient("Govorit server: " + msgFromClient, pID);
                                        } else {
                                            writeMsgToClient("It's not your turn to move", pID);
                                        }
                                    } else writeMsgToClient("Game is over", pID);
                                } else {
                                    /*
                                    //Если игры не существует с даными игроками:
                                     */
                                    writeMsgToClient("You can start a new game by entering 'Start'", pID);
                                }
                                switch (command) {
                                    case STARTSOLO:
                                        currentGame = new Game(nameOfPlayer);
                                        System.out.println("New game was created");
                                        ArrayList<String> playerList = new ArrayList<>();
                                        playerList.add(nameOfPlayer);
                                        gameData.put(currentGame, playerList);
                                        writeMsgToClient("New game was created", pID);
                                        writeMsgToClient("GameFile", pID);
                                        oos.writeObject(currentGame);
                                        break;
                                    case START:
                                        QueueOfPlayersHandler.queueOfPlayers.offer(nameOfPlayer);
                                        writeMsgToClient("Waiting for an opponent", pID);
                                        break;
                                    case CHECK:
//                                        System.out.println("===== Step of game: " + stepOfGame);
//                                        writeMsgToClient("CHECK CHECK CHECK", pID);
                                        break;
                                }
                            }
                            //Если авторизоваться не получилось:
                            else {
                                writeMsgToClient("Enter new name", pID);
                            }
                            oos.flush();
                        } catch (Exception e) {
                            e.printStackTrace();
                            System.out.println("Error 1");
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("beda-beda");
                e.printStackTrace();
            }
        }

        public class GameSender implements Runnable{
            Socket sk;
            Game gm;
            int id;
            //ObjectOutputStream outStream;

            public GameSender(Socket client, String name, int id) {
                gm = new Game(name);
                sk = client;
                this.id = id;
            }

            @Override
            public void run() {

                    //System.out.println("Creating OUS");
                    //writeMsgToClient("Creating OUS", id);
                    //oos = new ObjectOutputStream(sk.getOutputStream());


                    try {
                        System.out.println("Writing game");
                        oos.writeObject(gm);
                        oos.flush();
                        System.out.println("Game sent");
                        oos.close();
                        System.out.println("!!!oos closing");


                    } catch (IOException e) {
                        e.printStackTrace();
                    }

            }
        }





    }

    private static class QueueOfPlayersHandler implements Runnable {
        //Очередь из игроков:
        private static Queue<String> queueOfPlayers = new LinkedList<>();
        @Override
        public void run() {
            while (true){
                if (queueOfPlayers.size()>1){
                    String fistInQueue = queueOfPlayers.poll();
                    String secondInQueue = queueOfPlayers.poll();

                    ArrayList players = new ArrayList();
                    players.add(fistInQueue);
                    players.add(secondInQueue);
                    Game game = new Game(fistInQueue, secondInQueue);
                    System.out.println("New game with players " + fistInQueue + " and " + secondInQueue + " created");
                    gameData.put(game, players);
//                    writeMsgToClient("Your game with player " + secondInQueue + " is ready", playerData.get(fistInQueue));
//                    writeMsgToClient("endMessage", playerData.get(fistInQueue));
//                    writeMsgToClient("Your game with player " + fistInQueue + " is ready", playerData.get(secondInQueue));
//                    writeMsgToClient("endMessage", playerData.get(secondInQueue));


                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                else {
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
