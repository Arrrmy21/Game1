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
    ArrayList<ClientHandler> clients = new ArrayList<>();

    private HashMap<String, Integer> playerData = new HashMap<>();
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

    public class ClientHandler implements Runnable {

        int pID;
        BufferedReader reader;
        PrintWriter writer = null;
        Socket sock;
        DataOutputStream out = null;

        public ClientHandler(Socket clientSocket, int id) {
            pID = id;
            this.sock = clientSocket;

            try {
                reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                //System.out.println("Input stream created");
                writer = new PrintWriter(sock.getOutputStream());
                //System.out.println("Output stream created");

            } catch (IOException e) {
                System.out.println("Input stream reader failed.");
                e.printStackTrace();
            }
        }

        @Override
        public void run() {

            try {
                while (true) {
                    Game currentGame;
                    String nameOfPlayer = "";
                    Commands command = null;
                    Coord coord = null;
                    String msgFromClient = "";
                    while ((msgFromClient = reader.readLine()) != null) {
                        //Parse
                        String[] getMsg = msgFromClient.split(":");
                        System.out.println("getMsg length = " + getMsg.length);
                        if (getMsg.length==2){
                            nameOfPlayer = getMsg[0];
                            command = detectCommand(getMsg[1]);
                        }
                        else if(getMsg.length==3){
                            nameOfPlayer = getMsg[0];
                            command = detectCommand(getMsg[1]);
                            coord = getCoodrinates(getMsg[2]);
                        }
                        else {
                            writeMsgToClient("Wrong command", pID);
                            break;
                        }
                        String cccc = "Enered command : " + command;
                        writeMsgToClient(cccc, pID);

                        System.out.println("msg from client: " + msgFromClient);
                        //Обработка сообщения от клиента
                        try{
                            if(authorize(nameOfPlayer, pID)==true) {
                                writeMsgToClient("Authorize passed", pID);
                                //Существует ли игра
                                if(checkGameStatus(nameOfPlayer)== true) {
                                    writeMsgToClient("Your game is available", pID);
                                    currentGame = getGame(nameOfPlayer);
                                    //Не закончилась ли игра
                                    if (currentGame.isEndOfGame()==false) {
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
                                        }
                                        else {
                                            writeMsgToClient("It's not your turn to move", pID);
                                        }
                                    }
                                    else writeMsgToClient("Game is over", pID);
                                }
                                else{
                                    writeMsgToClient("You can start a new game by entering 'Start'", pID);
                                    switch (command) {
                                        case STARTSOLO:
                                            Game newGame = new Game(nameOfPlayer);
                                            ArrayList<String> playerList = new ArrayList<>();
                                            playerList.add(nameOfPlayer);
                                            gameData.put(newGame, playerList);
                                            System.out.println("new game was created");
                                            break;
                                        case START:
                                            QueueOfPlayersHandler.queueOfPlayers.offer(nameOfPlayer);
                                            writeMsgToClient("waiting for an opponent", pID);
                                            break;
                                    }

                                }

                            }
                            else{
                                writeMsgToClient("Enter new name", pID);
                            }
                            writeMsgToClient("endMessage", pID);
                            writer.flush();
                        }
                        catch (Exception e){
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
    }

    //Существует ли имя в базе игроков.
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

    //Проверяем если ли игрок с таким именем в какой-то игре и активна ли игра.
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
            default:
                command = Commands.HELP;
                break;
        }
        return command;
    }

    private synchronized Coord getCoodrinates(String stringFromClient){
        String strCoords[] = stringFromClient.split(",");
        int[] intCoords = new int[2];
        for (int i = 0; i < 2; i++)
            intCoords[i] = Integer.parseInt(strCoords[i]);
        return new Coord(intCoords[0], intCoords[1]);
    }
    //Проверка наличия имени в базе
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

    public synchronized void writeMsgToClient(String message, int id){
        try {
            for (ClientHandler client : clients){
                if (id==client.pID){
                    client.writer.println(message);
                    //client.out.flush();
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

    public void deleteClientFromList(int id){
        for(ClientHandler client : clients){
            if(client.pID==id){
                clients.remove(client);
            }
        }
    }

    private static class QueueOfPlayersHandler implements Runnable {
        //Очередь из игроков:
        private static Queue<String> queueOfPlayers = new LinkedList<>();
        @Override
        public void run() {
            while (true){
                if (queueOfPlayers.size()==2){
                    String fistInQueue = queueOfPlayers.poll();
                    String secondInQueue = queueOfPlayers.poll();

                    ArrayList players = new ArrayList();
                    players.add(fistInQueue);
                    players.add(secondInQueue);
                    Game game = new Game(fistInQueue, secondInQueue);
                    System.out.println("New game with players " + fistInQueue + " and " + secondInQueue + " created");
                    gameData.put(game, players);

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
}
