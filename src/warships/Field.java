package warships;

public class Field {

    private int width = 10;
    private int height = 10;
    private int power = 4;

    private int[][] matrix;


    protected Field() {
        matrix = new int[height][width];

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                matrix[i][j] = 0;
            }
        }

        putShip(1, 1);
        putShip(4,4);
        putShip(4,9);
        putShip(9,9);

    }

    protected int getPower() {
        return power;
    }

    private Integer getValue(int x, int y) {
        if (x >= 0 && x < width && y >= 0 && y < height)
            return matrix[y][x];
        return null;
    }

    private void setValue(int x, int y, int value) {
        if (x >= 0 && x < width && y >= 0 && y < height)
            matrix[y][x] = value;
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
                if (index == 0)
                    System.out.print(" 0 ");
                else if (index == 1)
                    System.out.print(" X ");
                else if (index == -1)
                    System.out.print(" - ");
                else if (index == 5)
                    System.out.print(" + ");
                else
                    System.out.print("?");
            }
            System.out.println();
        }
    }

    private void putShip(int x, int y){
        int value = getValue(x, y);
        if (value == 0 )
            setValue(x, y, 1);
        else
            System.out.println("try another one");
    }

    protected void shoot(int x, int y){
        int value = getValue(x, y);
        if (value == 1) {
            setValue(x, y, 5);
            power--;
        }
        else if (value == 0)
            setValue(x, y, -1);

    }
}
