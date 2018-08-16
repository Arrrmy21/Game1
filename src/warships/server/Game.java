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


    synchronized void makeMove(String nameOP, Commands command, Coord coord){
        if (nameOP.equals(turnToMove())){
            switch (command){
                case SHOOT:
                    makeShoot(nameOP, coord);
                    setStep(getStep() + 1);
                    break;
            }
        }
        checkWinStatus();
        }

    private void getShoot() {
        int x = (int) (Math.random() * 9);
        int y = (int) (Math.random() * 9);
        firstField.shoot(new Coord(x, y));
//        incrementStep();
    }

    private void makeShoot(String nameOP, Coord coord){
        if (nameOP.equalsIgnoreCase(nameOfFirstPlayer)){
            secondField.shoot(coord);
        }
        else {
            firstField.shoot(coord);
        }
    }

    public void putShip(String nameOP, Coord coord){
        if (nameOP.equalsIgnoreCase(nameOfFirstPlayer)){
            firstField.putShip(coord);
        }
        else {
            secondField.putShip(coord);
        }
    }

    public String getScore(){
        return nameOfFirstPlayer + " - " + firstField.getPower() + " : " + secondField.getPower() + " - " + nameOfSecondPlayer;
    }

    private void checkWinStatus(){
        if (firstField.getPower() == 0){
            endOfGame = true;
        }
        else if (secondField.getPower() == 0){
            endOfGame = true;
        }
    }

    String turnToMove(){
        if (getStep()%2!=0 ){
            return nameOfFirstPlayer;
        }
        else if (getStep()%2 == 0){
            return nameOfSecondPlayer;
        }
        else{
            return null;
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
