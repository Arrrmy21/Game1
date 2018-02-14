package warships;

import java.io.*;
import java.net.Socket;

public class Player {

    BufferedReader reader;
    PrintWriter writer;
    Socket sock;

    public static void main(String[] args) {
        new Player().go();
    }

    public void go() {

        setUpNetwork();

    }

    private void setUpNetwork() {

        try {
            sock = new Socket("127.0.0.1", 4949);
            //sock = new Socket("176.105.12.145", 4444);
        }
        catch (Exception ex) {
            System.out.println("Socket connection exception");
        }

        try{
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

            reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(sock.getOutputStream())));

            System.out.println("connection to server applyed");

            String mslToServer, msgFromServer;

            while ((mslToServer = br.readLine()) != null) {
                writer.println(mslToServer);
                writer.flush();
                System.out.println("Client: " + mslToServer);
            }
            while (true){
                if((msgFromServer = reader.readLine()) != null){
                    System.out.println("server returns message:" + msgFromServer);
                }
            }
                //msgFromServer = reader.readLine();

        }
        catch (Exception e){
            e.printStackTrace();
            System.out.println("connection error. ");
        }
    }
}
