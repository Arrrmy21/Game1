package warships.DB;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import warships.server.Commands;
import warships.server.Coord;
import warships.server.Field;
import warships.server.Game;

import java.sql.*;
import java.util.ArrayList;

public class WarshipsPGDBConnector {
    private static String url = "jdbc:postgresql://localhost:5432/WarshipsDB";
    private static String user = "Army";
    private static String password = "123";

    public static void main(String[] args) throws JsonProcessingException, SQLException {


        {
            WarshipsPGDBConnector connector = new WarshipsPGDBConnector();
            connector.clearData();
            Connection connection = DriverManager.getConnection(url, user, password);

            Game gm = new Game("Alex", 1, "Oleg", 2, 1);

            gm.getFirstField().print("Alex");
            System.out.println(" ");
            gm.getSecondField().print(" ");
            System.out.println();
            System.out.println("Sending game to data");
            connector.putGameToData(1, gm);

            Coord coord1 = new Coord(0, 0);
            Coord coord2 = new Coord(0, 1);
            Coord coord3 = new Coord(2, 2);
            Coord coord4 = new Coord(3, 3);

            gm.makeMove("Alex", Commands.SHOOT, coord1);
            System.out.println("Updating game");
            connector.updateGameToData(1, gm, "Alex", coord1);

            gm.makeMove("Oleg", Commands.SHOOT, coord2);
            System.out.println("Updating game");
            connector.updateGameToData(1, gm, "Oleg", coord2);

            gm.makeMove("Alex", Commands.SHOOT, coord3);
            System.out.println("Updating game");
            connector.updateGameToData(1, gm, "Alex", coord3);

            gm.makeMove("Oleg", Commands.SHOOT, coord4);
            System.out.println("Updating game");
            connector.updateGameToData(1, gm, "Oleg", coord4);

            gm.getFirstField().print("Alex");
            System.out.println(" ");
            gm.getSecondField().print(" ");
            System.out.println();

            System.out.println("Getting game from data");
            Game gameFromData = connector.getGameFromData(1);
            gameFromData.getFirstField().print("Alex");
            System.out.println("");
            gameFromData.getSecondField().print(" ");

            connection.close();
        }

    }

    public synchronized boolean isGameAvailable(int playerID) throws SQLException {
        Connection connection = DriverManager.getConnection(url, user, password);

        boolean available = false;
        Statement statement = null;
        ResultSet rs = null;

        try {
            String request = "SELECT game_id FROM game_data WHERE first_player_id= " + playerID +
                    " OR second_player_id = " + playerID;
            statement = connection.createStatement();
            rs = statement.executeQuery(request);
            if (rs.next()) {
                int gameID = rs.getInt("game_id");
                if (gameID != 0) {
                    available = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            rs.close();
            statement.close();
        }

        connection.close();
        return available;
    }

    public synchronized int getStepOfGame(String nameOP) throws SQLException {
        Connection connection = DriverManager.getConnection(url, user, password);
//        connectToPGDB(connection);

        int step = 0;
        Statement st = null;
        ResultSet rs = null;
        try {
            int gameID = getGameID(nameOP);
            String request = "SELECT step FROM game_data WHERE game_id = " + gameID;
            st = connection.createStatement();
            rs = st.executeQuery(request);
            while (rs.next()) {
                step = rs.getInt("step");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            rs.close();
            st.close();
        }

//        closeConnection(connection);
        connection.close();
        return step;
    }

    public synchronized void putPlayerNameToDB(int id, String name) throws SQLException {
        Connection connection = DriverManager.getConnection(url, user, password);
//        connectToPGDB(connection);

        PreparedStatement ps;
        try {
            String reqest = "INSERT INTO player_data VALUES (?, ?)";
            ps = connection.prepareStatement(reqest);
            ps.setInt(1, id);
            ps.setString(2, name);
            ps.executeUpdate();
        } catch (SQLException e1) {
            e1.printStackTrace();
        }

//        closeConnection(connection);
        connection.close();
    }

    public synchronized int getPlayerIDFromDB(String nameOP) throws SQLException {
        Connection connection = DriverManager.getConnection(url, user, password);
//        connectToPGDB(connection);

        Statement statement = null;
        ResultSet resultSet = null;
        int idOfPlayer = 0;

        try {
            String request = "SELECT * FROM player_data";
            statement = connection.createStatement();
            resultSet = statement.executeQuery(request);
            while (resultSet.next()) {
                String name = resultSet.getString("player_name");
                if (name.equalsIgnoreCase(nameOP))
                    idOfPlayer = resultSet.getInt("player_id");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            resultSet.close();
            statement.close();
        }

//        closeConnection(connection);
        connection.close();
        return idOfPlayer;
    }

    public synchronized String returnPlayerName(int id) throws SQLException {
        Connection connection = DriverManager.getConnection(url, user, password);
//        connectToPGDB(connection);

        Statement st = null;
        ResultSet rs = null;

        String playerName = null;

        try {
            String request = "SELECT player_name FROM player_data WHERE player_id = " + id;
            st = connection.createStatement();
            rs = st.executeQuery(request);
            rs.next();
            playerName = rs.getString("player_name");

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (st != null) {
                    st.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
//        closeConnection(connection);
        connection.close();
        return playerName;
    }

    public synchronized boolean isNameExist(String name) throws SQLException {
        Connection connection = DriverManager.getConnection(url, user, password);
//        connectToPGDB(connection);

        Statement statement = null;
        ResultSet rs = null;
        boolean nameEx = false;

        try {
            String request = "SELECT player_name FROM player_data ";
            statement = connection.createStatement();
            rs = statement.executeQuery(request);
            while (rs.next()) {
                String nameOP = rs.getString("player_name");
                if (name.equalsIgnoreCase(nameOP)) {
                    nameEx = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
//            rs.close();
//            statement.close();
        }

//        closeConnection(connection);
        connection.close();
        return nameEx;
    }

    private void putShipsOfGameToData(Field field) throws SQLException {
        Connection connection = DriverManager.getConnection(url, user, password);

        PreparedStatement ps;
        String requestFieldShips = "INSERT INTO list_of_ships(field_id, ship_coord ) VALUES (?,?)";
        ps = connection.prepareStatement(requestFieldShips);

        int amountOfShips = field.listOfShips.size();

        for (int i = 0; i < amountOfShips; i++) {
            Coord coord = field.listOfShips.get(i);
            int coordID = getCoordID(coord.getX(), coord.getY());
            ps.setInt(1, field.getFieldID());
            ps.setInt(2, coordID);
            ps.executeUpdate();
        }

        connection.close();
    }

    private synchronized int getCoordID(int x, int y) throws SQLException {
        Connection connection = DriverManager.getConnection(url, user, password);

        int coordID = 0;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            String requestCoordID = "SELECT coord_id FROM coord_data WHERE coord_x = " + x + " AND coord_y = " + y;

            ps = connection.prepareStatement(requestCoordID);
            rs = ps.executeQuery();
            if (rs.next()) {
                coordID = rs.getInt("coord_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

//        closeConnection(connection);
        connection.close();
        return coordID;
    }

    public synchronized int getGameID(String nameOfPlayer) throws SQLException {
        Connection connection = DriverManager.getConnection(url, user, password);
//        connectToPGDB(connection);

        int id = 0;
        Statement statement = null;
        ResultSet resultSet = null;
        int playerID;

        try {
            playerID = getPlayerIDFromDB(nameOfPlayer);
            String requestGID = "SELECT game_id FROM game_data WHERE first_player_id = " + playerID +
                    " OR second_player_id= " + playerID;
            statement = connection.createStatement();
            resultSet = statement.executeQuery(requestGID);
            if (resultSet.next())
                id = resultSet.getInt("game_id");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                resultSet.close();
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }
//        closeConnection(connection);
        connection.close();
        return id;
    }

    public Coord getCoord(int coordValue) throws SQLException {
        Connection connection = DriverManager.getConnection(url, user, password);
//        connectToPGDB(connection);
        Statement statement = null;
        ResultSet resultSet = null;
        Coord coord = new Coord();
        try {
            statement = connection.createStatement();
            String coordRequest = "SELECT * FROM coord_data WHERE coord_id= " + coordValue;
            resultSet = statement.executeQuery(coordRequest);
            resultSet.next();
            int x_c = resultSet.getInt("coord_x");
            int y_c = resultSet.getInt("coord_y");
            coord.setX(x_c);
            coord.setY(y_c);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            resultSet.close();
            statement.close();
        }

//        closeConnection(connection);
        connection.close();
        return coord;
    }

    public synchronized Game getGameFromData(int gameId) throws SQLException {
        Connection connection = DriverManager.getConnection(url, user, password);

        Game gameFromData = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            statement = connection.createStatement();

            int firstFieldID = 0;
            int secondFieldID = 0;

            int firstPlayerID = 0;
            int secondPlayerID = 0;
            boolean isEndOfGame = false;
            int step = 0;

            String requestGame = "SELECT * FROM game_data WHERE game_id = " + gameId;
            resultSet = statement.executeQuery(requestGame);

            if (resultSet.next()) {
//                gameFromData.setGID(gameId);
                firstFieldID = resultSet.getInt("first_field_id");
                secondFieldID = resultSet.getInt("second_field_id");

                isEndOfGame = (resultSet.getBoolean("end_of_game"));
                step = (resultSet.getInt("step"));

                firstPlayerID = resultSet.getInt("first_player_id");
                secondPlayerID = resultSet.getInt("second_player_id");
//                gameFromData.setFirstPlayerID(firstPlayerID);
//                gameFromData.setSecondPlayerID(secondPlayerID);
            }

            String firstPlayerName = null;
            String secondPlayerName = null;

            String requestPlayerName = "SELECT player_name FROM player_data WHERE player_id = ";
            resultSet = statement.executeQuery(requestPlayerName + firstPlayerID);
            if (resultSet.next()) {
                firstPlayerName = resultSet.getString("player_name");
            }
            resultSet = statement.executeQuery(requestPlayerName + secondPlayerID);
            if (resultSet.next()) {
                secondPlayerName = resultSet.getString("player_name");
            }

            gameFromData = new Game(firstPlayerName, firstPlayerID, secondPlayerName, secondPlayerID, gameId);
            gameFromData.setEndOfGame(isEndOfGame);
            gameFromData.setStep(step);

            //---------------------1st Field-------------------------------------

            int field1Width = 0;
            int field1Hight = 0;
            String requestField = "SELECT * FROM  fields_data WHERE field_id = ";
            resultSet = statement.executeQuery(requestField + firstFieldID);
            Field firstField = new Field();
            if (resultSet.next()) {
                firstField.setFieldID(firstFieldID);
                field1Width = resultSet.getInt("field_width");
                field1Hight = resultSet.getInt("field_hight");
                firstField.setWidth(field1Width);
                firstField.setHeight(field1Hight);
                firstField.setPower(resultSet.getInt("field_power"));
                firstField.setFieldOwnerName(firstPlayerName);
                firstField.setFieldOwnerID(firstFieldID);
            }

            int firstMatrix[][] = new int[field1Width][field1Hight];
            fillInMatrix(firstMatrix, firstFieldID);
            firstField.setMatrix(firstMatrix);

            ArrayList<Coord> firstArray = new ArrayList<>();
            String shipsArray = "SELECT ship_coord FROM list_of_ships WHERE field_id= ";
            resultSet = statement.executeQuery(shipsArray + firstFieldID);
            while (resultSet.next()) {
                int coordID = resultSet.getInt("ship_coord");
                firstArray.add(getCoord(coordID));
            }
            firstField.setListOfShips(firstArray);
            gameFromData.setFirstField(firstField);

            //-------Second field---------

            int field2Width = 0;
            int field2Hight = 0;

            resultSet = statement.executeQuery(requestField + secondFieldID);
            Field secondField = new Field();
            if (resultSet.next()) {
                secondField.setFieldID(secondFieldID);
                field2Width = resultSet.getInt("field_width");
                field2Hight = resultSet.getInt("field_hight");
                secondField.setWidth(field2Width);
                secondField.setHeight(field2Hight);
                secondField.setPower(resultSet.getInt("field_power"));
                secondField.setFieldOwnerID(secondPlayerID);
                secondField.setFieldOwnerName(secondPlayerName);
            }

            int secondMatrix[][] = new int[field2Width][field2Hight];
            fillInMatrix(secondMatrix, secondFieldID);

            secondField.setMatrix(secondMatrix);

            ArrayList<Coord> secondArray = new ArrayList<>();
            shipsArray = "SELECT ship_coord FROM list_of_ships WHERE field_id= ";
            resultSet = statement.executeQuery(shipsArray + secondFieldID);
            while (resultSet.next()) {
                int coordID = resultSet.getInt("ship_coord");
                secondArray.add(getCoord(coordID));
            }
            secondField.setListOfShips(secondArray);
            gameFromData.setSecondField(secondField);

            String[] players = new String[2];
            players[0] = firstPlayerName;
            players[1] = secondPlayerName;
            gameFromData.setPlayersInGame(players);

            gameFromData.setNameOfFirstPlayer(firstPlayerName);
            gameFromData.setNameOfSecondPlayer(secondPlayerName);


        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            resultSet.close();
            statement.close();
        }


//        closeConnection(connection);
        connection.close();
        return gameFromData;
    }

    private synchronized void fillInMatrix(int[][] matrix, int fieldID) throws SQLException {
        Connection connection = DriverManager.getConnection(url, user, password);
        Statement st = connection.createStatement();
        String matrixRequest = "SELECT coord_value FROM field_matrix_data WHERE field_id = ";

        try {
            ResultSet resultSet = st.executeQuery(matrixRequest + fieldID + " ORDER BY coord_id ASC ");
            for (int x_c = 0; x_c < 10; x_c++) {
                for (int y_c = 0; y_c < 10; y_c++) {
                    if (resultSet.next()) {
                        int value = resultSet.getInt("coord_value");
                        matrix[x_c][y_c] = value;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        st.close();
        connection.close();


    }

    public synchronized void putGameToData(int gameID, Game game) throws JsonProcessingException, SQLException {

        Connection connection = DriverManager.getConnection(url, user, password);
        PreparedStatement ps = null;

        int firstFieldID = game.getFirstField().getFieldID();
        int secondFieldID = game.getSecondField().getFieldID();
        int[][] firstMartix = game.getFirstField().getMatrix();
        int[][] secondMartix = game.getSecondField().getMatrix();

//        String fillIDCoordData = "INSERT INTO coord_data (coord_id, coord_x, coord_y) VALUES(?, ?, ?)";

        String requestGameData = "INSERT INTO game_data (game_id, first_field_id, second_field_id, end_of_game, " +
                "step, first_player_id, second_player_id) VALUES (?, ?, ?, " +
                "?, ?, ?, ?)";

        String requestFieldsData = "INSERT INTO fields_data (field_id, field_width, field_hight, field_power, player_id)" +
                " VALUES(?, ?, ?, ?, ?)";


        synchronized (WarshipsPGDBConnector.class) {
            try {

                // ----------------------  Coord filling-----------
//            ps = connection.prepareStatement(fillIDCoordData);
//            for(int i = 1; i<100; i++){
//                for(int x_c = 0; x_c<10; x_c++){
//                    for(int y_c = 0; y_c < 10; y_c++){
//                        ps.setInt(1, i);
//                        ps.setInt(2, x_c);
//                        ps.setInt(3, y_c);
//                        ps.executeUpdate();
//                        i++;
//                    }
//                }
//            }
                //-------------------------------------------------

                //--------------------------Game filling-----------

                ps = connection.prepareStatement(requestGameData);
                ps.setInt(1, gameID);
                ps.setInt(2, firstFieldID);
                ps.setInt(3, secondFieldID);
                ps.setBoolean(4, game.isEndOfGame());
                ps.setInt(5, game.getStep());
                ps.setInt(6, game.getFirstPlayerID());
                ps.setInt(7, game.getSecondPlayerID());
                ps.executeUpdate();
                //-------------------------------------------------

                //-------------1st Field filling-------------------
                ps = connection.prepareStatement(requestFieldsData);
                ps.setInt(1, firstFieldID);
                ps.setInt(2, 10);
                ps.setInt(3, 10);
                ps.setInt(4, game.getFirstField().getPower());
                ps.setInt(5, game.getFirstField().getFieldOwnerID());
                ps.executeUpdate();
                //-------------------------------------------------
                //-------------2nd Field filling-------------------

                ps = connection.prepareStatement(requestFieldsData);
                ps.setInt(1, secondFieldID);
                ps.setInt(2, 10);
                ps.setInt(3, 10);
                ps.setInt(4, game.getSecondField().getPower());
                ps.setInt(5, game.getSecondField().getFieldOwnerID());
                ps.executeUpdate();

                if (game.getStep() == 1) {
                    putShipsOfGameToData(game.getFirstField());
                    putShipsOfGameToData(game.getSecondField());
                }
                //-------------------------------------------------

                //-----------Matrix filling------------------------
                String putCoordValuesOfMatrixToData = "INSERT INTO field_matrix_data (field_id, coord_id, coord_value)" +
                        " VALUES  (?, ?, ?)";
                ps = connection.prepareStatement(putCoordValuesOfMatrixToData);

                for (int x_c = 0; x_c < 10; x_c++) {
                    for (int y_c = 0; y_c < 10; y_c++) {
                        int valFF = firstMartix[x_c][y_c];
                        int valSF = secondMartix[x_c][y_c];
                        int coordID = getCoordID(x_c, y_c);
                        ps.setInt(1, firstFieldID);
                        ps.setInt(2, coordID);
                        ps.setInt(3, valFF);
                        ps.executeUpdate();
                        ps.setInt(1, secondFieldID);
                        ps.setInt(2, coordID);
                        ps.setInt(3, valSF);
                        ps.executeUpdate();
                    }
                }

                //-------------------------------------------------

            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

//        closeConnection(connection);
        connection.close();
    }

    public synchronized void updateGameToData(int gameID, Game game, String nameOP, Coord coord) throws SQLException {
        Connection connection = DriverManager.getConnection(url, user, password);

        int step = game.getStep();
        boolean endOfGame = game.isEndOfGame();

        Field field;

        if (nameOP.equalsIgnoreCase(game.getNameOfFirstPlayer())) {
            field = game.getSecondField();
        } else {
            field = game.getFirstField();
        }


        int fieldID = field.getFieldID();
        int fieldPower = field.getPower();

        int x_c = coord.getX();
        System.out.println("x_c= " + x_c);
        int y_c = coord.getY();
        System.out.println("y_c= " + y_c);
        int coordID = getCoordID(x_c, y_c);
        System.out.println("coodrdID = " + coordID);
        int[][] mat = field.getMatrix();
        int coordValue = mat[x_c][y_c];
        System.out.println("int coordValue = mat[x_c][y_c]; = " + coordValue);
        PreparedStatement ps = null;

        try {

            String updateGameData = "UPDATE game_data SET step = " + step +
                    ", end_of_game = " + endOfGame + " WHERE " +
                    " game_id = " + gameID;
            ps = connection.prepareStatement(updateGameData);
            ps.executeUpdate();


            String updateFieldData = "UPDATE fields_data SET field_power = " +
                    fieldPower + " WHERE field_id = " +
                    fieldID;
            ps = connection.prepareStatement(updateFieldData);
            ps.executeUpdate();


            //-----------Matrix updating------------------------
            String updateMatrixData = "UPDATE field_matrix_data SET coord_value = " + coordValue +
                    " WHERE field_id = " + fieldID + " AND coord_id = " + coordID;
            ps = connection.prepareStatement(updateMatrixData);
            ps.executeUpdate();

            //-------------------------------------------------

        } catch (SQLException e) {
            e.printStackTrace();
        }

        ps.close();
        connection.close();
    }

    public void clearData() throws SQLException {
        Connection connection = DriverManager.getConnection(url, user, password);
        PreparedStatement ps;

        try {
            String request1 = " DELETE FROM  fields_data";
            ps = connection.prepareStatement(request1);
            ps.executeUpdate();
            String request2 = " DELETE FROM  game_data";
            ps = connection.prepareStatement(request2);
            ps.executeUpdate();
            String request3 = " DELETE FROM  field_matrix_data";
            ps = connection.prepareStatement(request3);
            ps.executeUpdate();
            String request4 = " DELETE FROM  list_of_ships";
            ps = connection.prepareStatement(request4);
            ps.executeUpdate();

            String request5 = " DELETE FROM  player_data";
            ps = connection.prepareStatement(request5);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
        connection.close();
    }

}