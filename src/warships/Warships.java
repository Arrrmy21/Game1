package warships;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Warships {

    ServerSocket serverSocket;



    public static void main(String[] args) throws IOException {

        new Warships().go();
    }


    private void go() {

        try{
            serverSocket = new ServerSocket(4949);

            while(true){
                Socket clientSocket = serverSocket.accept();
                System.out.println("Player connected to server.");

                Thread t = new Thread(new ClientHandler(clientSocket));
                t.start();

              //  Game game = new Game();
               // game.start();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public class ClientHandler implements Runnable {
        BufferedReader reader;
        PrintWriter writer;
        Socket sock;

        public ClientHandler(Socket clientSocket) {

            try {
                sock = clientSocket;

                reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                System.out.println("Input stream created");

                writer = new PrintWriter(clientSocket.getOutputStream());
                System.out.println("Output stream created");

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            String msgFromClient, msgToClient;
            try{
                /*
                while((msgFromClient = reader.readLine()) != null){

                    System.out.println("Got from client: " + msgFromClient);
                    writer.println("abaababab");
                }*/
                while (true){
                    if( (msgFromClient = reader.readLine()) != null){
                        System.out.println("msg from client: " + msgFromClient);
                        writer.print("print");
                        writer.println("println");
                        writer.write("write");
                        writer.flush();
                    }
                }

            }
            catch (Exception e){
                e.printStackTrace();
            }

        }
    }
}
