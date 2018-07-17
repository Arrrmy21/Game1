package src.warships.server;

import java.io.Serializable;
import java.util.ArrayList;

public class Field implements Serializable{

    private int width = 10;
    private int height = 10;
    private int power;
    private String fieldOwner;
    public ArrayList<Coord> listOfShips = new ArrayList<>();

    private int[][] matrix;


    protected Field(String name) {
        fieldOwner = name;

        matrix = new int[width][height];

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                matrix[i][j] = 0;
            }
        }

        putShip(new Coord(1 , 1));
        putShip(new Coord(4 , 4));
        putShip(new Coord(1 , 0));
        putShip(new Coord(0 , 8));


    }

    public int getPower() {
        return power;
    }

    public void setPower() {
        this.power = listOfShips.size();
    }

    private Integer getValue(Coord coord) {
        if (coord.x >= 0 && coord.x < width && coord.y >= 0 && coord.y < height)
            return matrix[coord.x][coord.y];
        return null;
    }

    private void setValue(Coord coord, int value) {
        if (coord.x >= 0 && coord.x < width && coord.y >= 0 && coord.y < height)
            matrix[coord.y][coord.x] = value;
    }
    public String getFieldOwner() {
        return fieldOwner;
    }

    protected void print(String str) {

        int[][] rezerv = new int[height][width];

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                rezerv[i][j] = matrix[i][j];
            }
        }
        if(str == "ali"){
            for(Coord coord : listOfShips){
                rezerv[coord.y][coord.x] = 1;
            }
        }
        /*
        //Состояние клеток:
        // "0" - неоткрытая пустая клетка
        // "-1" - открытая пустая клетка
        // "1" - открытая клетка с целым кораблем (для Alli поля)
        // "5" - открытая клетка с раненым кораблем
         */

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int index = rezerv[i][j];
                // 0 - пустя клетка
                if (index == 0)
                    System.out.print(" 0 ");
                // 1 - попадание в корабль
                else if (index == 5)
                    System.out.print(" X ");
                // -1 - промах
                else if (index == -1)
                    System.out.print(" - ");
                else if (index == 1)
                    System.out.print(" S ");
                else
                    System.out.print("?");
            }
            System.out.println();
        }
    }

    protected void putShip(Coord coord){
        int value = getValue(coord);
        if (value == 0 ) {
            listOfShips.add(coord);
            power++;
        }
        else
            System.out.println("try another one cell");
    }

    protected void shoot(Coord coord){

        int value = getValue(coord);
        if (listOfShips.removeIf(coord1 -> coord1.equals(coord))){
            setValue(coord, 5);
            power--;
        }
        else {
            if (value == 0)
                setValue(coord, -1);
        }
    }
}
