package warships.server;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonAutoDetect
public class Game {

    @JsonProperty("gameID")
    private int gID;

    @JsonProperty("endOfGame")
    private boolean endOfGame;

    @JsonProperty("nameOfFirstPlayer")
    private String nameOfFirstPlayer;
    @JsonProperty("firstPlayerID")
    private int firstPlayerID;

    @JsonProperty("nameOfSecondPlayer")
    private String nameOfSecondPlayer;
    @JsonProperty("secondPlayerID")
    private int secondPlayerID;

    @JsonProperty("firstField")
    private Field firstField;

    @JsonProperty("secondField")
    private Field secondField;

    @JsonProperty("playersInGame")
    public String[] playersInGame = new String[2];

    /*
    //При начале игры шаг будет равен 1. Значит ход первого игрока. Чётное число будет означать ход 2го игрока.
     */
    @JsonProperty("step")
    private int step;

    /*
    //Создание игры для 2-х игроков
     */
    public Game(String firstPlayerName, int fpid, String secondPlayerName, int spid, int gameID) {
        setNameOfFirstPlayer(firstPlayerName);
        setNameOfSecondPlayer(secondPlayerName);
        setFirstPlayerID(fpid);
        setSecondPlayerID(spid);
        setGID(gameID);
        playersInGame[0] = nameOfFirstPlayer;
        playersInGame[1] = nameOfSecondPlayer;

        firstField = new Field(getNameOfFirstPlayer(), getFirstPlayerID(), getGID() * 2 - 1);
        secondField = new Field(getNameOfSecondPlayer(), getSecondPlayerID(), getGID() * 2);

        setStep(1);
        setEndOfGame(false);
    }

    /*
    //Создание игры для одного игрока
     */
//    Game(String firstPlayerName, int playerID, int id) {
//        nameOfFirstPlayer = firstPlayerName;
//        nameOfSecondPlayer = nameOfFirstPlayer + "_OPPONENT";
//        firstPlayerID = playerID;
//        gID = id;
//
//        playersInGame[0] = nameOfFirstPlayer;
//        playersInGame[1] = nameOfSecondPlayer;
//
//        firstField = new Field(nameOfFirstPlayer, getFirstPlayerID(),gID * 2 - 1);
//        secondField = new Field(nameOfSecondPlayer, getSecondPlayerID(), gID * 2);
//
//        step = 1;
//        endOfGame = false;
//    }

    public Game() {
    }

    public synchronized void makeMove(String nameOP, Commands command, Coord coord) {
        if (nameOP.equals(turnToMove()) && isCoordInRange(coord)) {
            switch (command) {
                case SHOOT:
                    makeShoot(nameOP, coord);
                    setStep(getStep() + 1);
                    break;
            }
        }
        checkWinStatus();
    }

    private boolean isCoordInRange(Coord coord) {
        int x = coord.getX();
        int y = coord.getY();
        return x >= 0 && x < 10 && y >= 0 && y < 10;
    }

//    private void getShoot() {
//        int x = (int) (Math.random() * 9);
//        int y = (int) (Math.random() * 9);
//        firstField.shoot(new Coord(x, y));
//        incrementStep();
//    }

    private synchronized void makeShoot(String nameOP, Coord coord) {
        if (nameOP.equalsIgnoreCase(getNameOfFirstPlayer())) {
            secondField.shoot(coord);
        } else if (nameOP.equalsIgnoreCase(getNameOfSecondPlayer())) {
            firstField.shoot(coord);
        }
    }

    public String showScore() {
        return getNameOfFirstPlayer() + " - " + firstField.getPower() + " : " + secondField.getPower() + " - " + getNameOfSecondPlayer();
    }

    private void checkWinStatus() {
        if (firstField.getPower() == 0) {
            endOfGame = true;
        } else if (secondField.getPower() == 0) {
            endOfGame = true;
        }
    }

    String turnToMove() {
        if (getStep() % 2 != 0) {
            return nameOfFirstPlayer;
        } else if (getStep() % 2 == 0) {
            return nameOfSecondPlayer;
        } else {
            return null;
        }
    }

    int getGID() {
        return gID;
    }

    private void setGID(int id) {
        gID = id;
    }

    public void setFirstField(Field firstField) {
        this.firstField = firstField;
    }

    public void setSecondField(Field secondField) {
        this.secondField = secondField;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int st) {
        step = st;
    }

    public void setPlayersInGame(String[] playersInGame) {
        this.playersInGame = playersInGame;
    }

    public int getFirstPlayerID() {
        return firstPlayerID;
    }

    private void setFirstPlayerID(int fpid) {
        firstPlayerID = fpid;
    }

    public int getSecondPlayerID() {
        return secondPlayerID;
    }

    private void setSecondPlayerID(int spid) {
        secondPlayerID = spid;
    }

    public String getNameOfFirstPlayer() {
        return nameOfFirstPlayer;
    }

    public String getNameOfSecondPlayer() {
        return nameOfSecondPlayer;
    }

    public void setNameOfFirstPlayer(String nameOfFirstPlayer) {
        this.nameOfFirstPlayer = nameOfFirstPlayer;
    }

    public void setNameOfSecondPlayer(String nameOfSecondPlayer) {
        this.nameOfSecondPlayer = nameOfSecondPlayer;
    }

    public boolean isEndOfGame() {
        return endOfGame;
    }

    public Field getFirstField() {
        return firstField;
    }

    public Field getSecondField() {
        return secondField;
    }

    public void setEndOfGame(boolean isEnd) {
        endOfGame = isEnd;
    }

}