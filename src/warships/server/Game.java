package warships.server;


import java.io.Serializable;

public class Game implements Serializable {

    public Field firstField;
    public Field secondField;

    private final int gID;

    private boolean endOfGame;

    private String nameOfFirstPlayer;
    private String nameOfSecondPlayer;

    public String[] playersInGame = new String[2];

    /*
    //При начале игры шаг будет равен 1. Значит ход первого игрока. Чётное число будет означать ход 2го игрока.
     */
    private int step;

    /*
    //Создание игры для 2-х игроков
     */
    Game(String firstPlayerName, String secondPlayerName, int id) {
        nameOfFirstPlayer = firstPlayerName;
        nameOfSecondPlayer = secondPlayerName;
        gID = id;
        playersInGame[0] = nameOfFirstPlayer;
        playersInGame[1] = nameOfSecondPlayer;

        firstField = new Field(nameOfFirstPlayer);
        secondField = new Field(nameOfSecondPlayer);
        step = 1;
        endOfGame = false;
    }
    /*
    //Создание игры для одного игрока
     */
    Game(String firstPlayerName, int id) {
        nameOfFirstPlayer = firstPlayerName;
        nameOfSecondPlayer = "BOT";
        gID = id;

        playersInGame[0] = nameOfFirstPlayer;

        firstField = new Field(nameOfFirstPlayer);
        secondField = new Field("BOT");
        step = 1;
        endOfGame = false;
    }

    boolean isEndOfGame() {
        return endOfGame;
    }


    public void makeMove(String nameOP, Commands command, Coord coord){


            checkWinStatus();
        }

    private void getShoot() {
        int x = (int) (Math.random() * 9);
        int y = (int) (Math.random() * 9);
        firstField.shoot(new Coord(x, y));
//        incrementStep();
    }

    void makeShoot(String nameOP, Coord coord){
        if (nameOP.equalsIgnoreCase(nameOfFirstPlayer)){
            secondField.shoot(coord);
        }
        else {
            firstField.shoot(coord);
        }
        setStep(getStep() + 1);
    }

    public void putShip(String nameOP, Coord coord){
        if (nameOP.equalsIgnoreCase(nameOfFirstPlayer)){
            firstField.putShip(coord);
        }
        else {
            secondField.putShip(coord);
        }
    }

    private void checkWinStatus(){
        boolean checkStatus = false;
        if (firstField.getPower() == 0){
            endOfGame = true;
        }
        else if (secondField.getPower() == 0){
            endOfGame = true;
        }
    }
    boolean turnToMove(String name){
        if (name.equals(nameOfFirstPlayer) && getStep()%2!=0 ){
            return true;
        }
        else if (name.equals(nameOfSecondPlayer) && getStep()%2 == 0){
            return true;
        }
        else{
            return false;
        }
    }

    public int getStep(){
        return step;
    }

    private synchronized void setStep(int st) {
        step = st;
    }

//    void incrementStep(){
//        setStep(getStep() + 1);
//    }

    int getGID() {
        return gID;
    }


}
