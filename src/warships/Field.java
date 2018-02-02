package warships;

import java.util.ArrayList;

public class Field {

    private int width = 10;
    private int height = 10;
    private int power = 4;
    private static String fieldOwner;
    public ArrayList<Coord> listOfShips = new ArrayList<>();

    private int[][] matrix;


    protected Field(String name) {
        matrix = new int[height][width];
        fieldOwner = name;

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                matrix[i][j] = 0;
            }
        }

        putShip(new Coord(1 , 1));
        putShip(new Coord(4 , 4));
        putShip(new Coord(9 , 9));
        putShip(new Coord(4 , 9));


    }

    protected int getPower() {
        return power;
    }

    private Integer getValue(Coord coord) {
        if (coord.x >= 0 && coord.x < width && coord.y >= 0 && coord.y < height)
            return matrix[coord.x][coord.y];
        return null;
    }

    private void setValue(Coord coord, int value) {
        if (coord.x >= 0 && coord.x < width && coord.y >= 0 && coord.y < height)
            matrix[coord.x][coord.y] = value;
    }

    protected void print() {

        int[][] rezerv = new int[height][width];


        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                rezerv[i][j] = matrix[i][j];
            }
        }
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
                else
                    System.out.print("?");
            }
            System.out.println();
        }
    }

    private void putShip(Coord coord){
        int value = getValue(coord);
        if (value == 0 ) {
            listOfShips.add(coord);
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
