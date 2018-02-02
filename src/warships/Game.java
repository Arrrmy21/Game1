package warships;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Game {

    private Field alliField;
    private Field enemyField;
    private boolean endOfGame =false;

    public Game() {
        alliField = new Field("name1");
        enemyField = new Field("name2");
    }

    public void start() throws IOException {

        while (alliField.getPower() != 0 || enemyField.getPower() != 0) {

            alliField.print();
            System.out.println();
            System.out.println("list of ships");
            for (Coord c : alliField.listOfShips)
                System.out.println(c.x + " "  + c.y);
            System.out.println("power = " + alliField.getPower());
            System.out.println("Enter coords: X, Y");
            getShoot();
        }
    }

    public void getShoot() throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String read = br.readLine();
        String strCoords[] = read.split(",");
        int[] intCoords = new int[2];
        for (int i = 0; i < 2; i++)
            intCoords[i] = Integer.parseInt(strCoords[i]);
        alliField.shoot(new Coord(intCoords[0], intCoords[1]));
    }


}
