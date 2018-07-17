package src.warships.server;


public class Game{

    private Field firstField;
    private Field secondField;

    private boolean endOfGame;

    private String nameOfFirstPlayer;
    private String nameOfSecondPlayer;

    public String[] playersInGame = new String[2];

    private boolean turnOfFirstPlayerToMove = true;
    private boolean turnOfSecondPlayerToMove = false;

    public Game(String firstPlayerName, String secondPlayerName) {
        nameOfFirstPlayer = firstPlayerName;
        nameOfSecondPlayer = secondPlayerName;

        playersInGame[0] = nameOfFirstPlayer;
        playersInGame[1] = nameOfSecondPlayer;

        firstField = new Field(nameOfFirstPlayer);
        secondField = new Field(nameOfSecondPlayer);
        endOfGame = false;
    }
    public Game(String firstPlayerName) {
        nameOfFirstPlayer = firstPlayerName;
        nameOfSecondPlayer = "BOT";

        playersInGame[0] = nameOfFirstPlayer;

        firstField = new Field(nameOfFirstPlayer);
        secondField = new Field("BOT");
        endOfGame = false;
    }

    public boolean isEndOfGame() {
        return endOfGame;
    }
    private void changeOfTurn(){
        if (turnOfFirstPlayerToMove == false && turnOfSecondPlayerToMove == true) {
            turnOfFirstPlayerToMove = true;
            turnOfSecondPlayerToMove = false;
        }
        else {
            turnOfFirstPlayerToMove = false;
            turnOfSecondPlayerToMove = true;
        }
    }

    public void makeMove(String nameOP, Commands command, Coord coord){
        if (nameOP==nameOfFirstPlayer){

        }
            /*System.out.println("Enemy field: ");
            secondField.print("");
            System.out.println("list of ships: ");
            for (Coord c : secondField.listOfShips)
                System.out.println(c.x + " " + c.y);
            System.out.println();
            System.out.println("Alli field:");
            firstField.print("ali");
            System.out.println(firstField.getFieldOwner() + " power = " + firstField.getPower());
            System.out.println(secondField.getFieldOwner() + " power = " + secondField.getPower());
            System.out.println("Enter coords [0-9], [0-9]: X, Y");
            checkWinStatus();*/
        }

    private void getShoot() {
        int x = (int) (Math.random() * 9);
        int y = (int) (Math.random() * 9);
        firstField.shoot(new Coord(x, y));
    }

    public void makeShoot(String nameOP, Coord coord){
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

    public void checkWinStatus(){
        boolean checkStatus = false;
        if (firstField.getPower() == 0){
            endOfGame = true;
        }
        else if (secondField.getPower() == 0){
            endOfGame = true;
        };
    }
    public boolean turnToMove(String name){
        if (nameOfFirstPlayer==name && turnOfFirstPlayerToMove == true){
            return true;
        }
        else if (nameOfSecondPlayer==name && turnOfSecondPlayerToMove == true){
            return true;
        }
        else{
            return false;
        }
    }
}