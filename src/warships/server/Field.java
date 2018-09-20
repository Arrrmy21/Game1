package warships.server;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.ArrayList;
import java.util.Random;
import java.util.stream.IntStream;

@JsonAutoDetect
public class Field {

    @JsonProperty("field_id")
    private int fieldID;

    @JsonProperty("field_width")
    private int width = 10;
    @JsonProperty("field_height")
    private int height = 10;
    @JsonProperty("Power")
    private int power = 0;
    @JsonProperty("fieldOwnerName")
    private String fieldOwnerName;

    @JsonProperty("fieldOwnerID")
    private int fieldOwnerID;


    @JsonProperty("listOfShips")
    @JsonDeserialize(as = ArrayList.class)
    public ArrayList<Coord> listOfShips = new ArrayList<>();

    @JsonProperty("matrix")
    private int[][] matrix;

    public Field() {
    }

    /*
        //Для создания игрового поля используется имя игрока
         */
    public Field(String playerName, int playerID, int fieldID) {

        setFieldID(fieldID);
        setFieldOwnerName(playerName);
        setFieldOwnerID(playerID);

        int width = getWidth();
        int height = getHeight();
        matrix = new int[width][height];

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                matrix[i][j] = 0;
            }
        }

        fillFieldWithShips();
//        putShip(new Coord(1, 1));
//        putShip(new Coord(4, 4));
//        putShip(new Coord(0, 0));
//        putShip(new Coord(0, 5));

//        shoot(new Coord(0, 0));
//        shoot(new Coord(1, 1));
//        shoot(new Coord(2, 2));
//        shoot(new Coord(3, 3));


    }

    public int getPower() {
        return power;
    }

    public void setPower(int pow) {
        power = pow;
    }

    public int getFieldID() {
        return fieldID;
    }

    public void setFieldID(int id) {
        fieldID = id;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }


    private int getCoordValue(Coord coord) {
        int i = 0;
        if (coord.getX() >= 0 && coord.getX() < getWidth() && coord.getY() >= 0 && coord.getY() < getHeight())
            return matrix[coord.getX()][coord.getY()];
        return i;
    }

    private void setCoordValue(Coord coord, int value) {
        if (coord.getX() >= 0 && coord.getX() < getWidth() && coord.getY() >= 0 && coord.getY() < getHeight())
            matrix[coord.getX()][coord.getY()] = value;
    }

    public String getFieldOwnerName() {
        return fieldOwnerName;
    }

    public void setFieldOwnerName(String name) {
        fieldOwnerName = name;
    }

    public void setListOfShips(ArrayList<Coord> listOfShips) {
        this.listOfShips = listOfShips;
    }

    public void setMatrix(int[][] matrix) {
        this.matrix = matrix;
    }

    public int[][] getMatrix() {
        return matrix;
    }

    public int getFieldOwnerID() {
        return fieldOwnerID;
    }

    public void setFieldOwnerID(int fieldOwnerID) {
        this.fieldOwnerID = fieldOwnerID;
    }


    public void print(String name) {

        int[][] rezerv = new int[getHeight()][getWidth()];

        for (int i = 0; i < getWidth(); i++) {
            for (int j = 0; j < getHeight(); j++) {
                rezerv[i][j] = getMatrix()[i][j];
            }
        }

        if (name.equals(getFieldOwnerName())) {
            for (Coord coord : listOfShips) {
                if (rezerv[coord.getX()][coord.getY()] != 5)
                    rezerv[coord.getX()][coord.getY()] = 1;
            }
        }
        /*
        //Состояние клеток:
        // "0" - закрытая пустая клетка
        // "-1" - открытая пустая клетка
        // "1" - открытая клетка с целым кораблем (для Alli поля)
        // "5" - открытая клетка с раненым кораблем
         */

        for (int i = 0; i < getHeight(); i++) {
            for (int j = 0; j < getWidth(); j++) {
                int index = rezerv[i][j];
                // 0 - пустя клетка
                if (index == 0)
                    System.out.print(" 0 ");
                else if (index == 5)
                    System.out.print(" X ");
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

    private Coord getNewCoord() {
        Random random = new Random();
        return new Coord(random.nextInt(9), random.nextInt(9));
    }

    private void putShip(Coord coord) {
        listOfShips.add(coord);
        setPower(getPower() + 1);
    }

    private void fillFieldWithShips() {

        while (getPower() < 10) {

            Coord newCoord = getNewCoord();

            if (!listOfShips.contains(newCoord)) {

                for (Coord cd : listOfShips) {
                    if (cd.equals(newCoord))
                        break;
                }
                putShip(newCoord);
            }
        }
    }

    synchronized void shoot(Coord coord) {

        int value = getCoordValue(coord);

        if (listOfShips.contains(coord)) {
            setCoordValue(coord, 5);
            setPower(getPower() - 1);
        } else {
            if (value == 0)
                setCoordValue(coord, -1);
        }
    }

}