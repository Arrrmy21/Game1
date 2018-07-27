package warships.server;


import java.io.Serializable;

public class Game implements Serializable {

    public Field firstField;
    public Field secondField;

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
    public Game(String firstPlayerName, String secondPlayerName) {
        nameOfFirstPlayer = firstPlayerName;
        nameOfSecondPlayer = secondPlayerName;

        playersInGame[0] = nameOfFirstPlayer;
        playersInGame[1] = nameOfSecondPlayer;

        firstField = new Field(nameOfFirstPlayer);
        secondField = new Field(nameOfSecondPlayer);
        endOfGame = false;
    }
    /*
    //Создание игры для одного игрока
     */
    public Game(String firstPlayerName) {
        nameOfFirstPlayer = firstPlayerName;
        nameOfSecondPlayer = "BOT";

        playersInGame[0] = nameOfFirstPlayer;

        firstField = new Field(nameOfFirstPlayer);
        secondField = new Field("BOT");
        step = 1;
        endOfGame = false;
    }

    public boolean isEndOfGame() {
        return endOfGame;
    }


    public void makeMove(String nameOP, Commands command, Coord coord){


            checkWinStatus();
        }

    private void getShoot() {
        int x = (int) (Math.random() * 9);
        int y = (int) (Math.random() * 9);
        firstField.shoot(new Coord(x, y));
        incremetStep();
    }

    public void makeShoot(String nameOP, Coord coord){
        if (nameOP.equalsIgnoreCase(nameOfFirstPlayer)){
            secondField.shoot(coord);
        }
        else {
            firstField.shoot(coord);
        }
        incremetStep();
    }

    public void putShip(String nameOP, Coord coord){
        if (nameOP.equalsIgnoreCase(nameOfFirstPlayer)){
            firstField.putShip(coord);
        }
        else {
            secondField.putShip(coord);
        }
    }

    public void checkWinStatus(){
        boolean checkStatus = false;
        if (firstField.getPower() == 0){
            endOfGame = true;
        }
        else if (secondField.getPower() == 0){
            endOfGame = true;
        }
    }
    public boolean turnToMove(String name){
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
    public void setStep(int step) {
        this.step = step;
    }
    public void incremetStep(){
        setStep(getStep()+1);
    }


}
