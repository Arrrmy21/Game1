package warships;

import java.io.IOException;

public class Warships {

    public static void main(String[] args) throws IOException {

        Game game = new Game();
        game.start();

        //JFrame jFrame = getFrame();

    }

    /* static JFrame getFrame() {
        JFrame jFrame = new JFrame("Game");
        jFrame.setDefaultCloseOperation(jFrame.EXIT_ON_CLOSE);
        jFrame.setSize(700, 350);
        jFrame.setLocationRelativeTo(null);

        Object[][] data = {{1,2},{3,4}};
        JTable table = new JTable(data, new Object[]{"",""});
        table.setPreferredSize(new Dimension(70, 70));
        jFrame.add(table);
        jFrame.setVisible(true);

        return jFrame;
    }*/
}
