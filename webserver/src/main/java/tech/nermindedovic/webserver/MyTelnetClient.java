package tech.nermindedovic.webserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

public class MyTelnetClient {
    public static void main (String[] args) {
        String serverName;
        if (args.length < 1) serverName = "localhost";
        else serverName = args[0];

        int port = 2540;

        String textFromServer;

        System.out.println("Clark Elliott's MyTelnet Client, 1.0.\n");
        System.out.println("Using server: " + serverName + ", Port: " + port);
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        try (
                // 80
                final Socket sock = new Socket(serverName, port);                                                         // change this to 2540 if you want to talk to MyListener at localhost!
                final BufferedReader fromServer = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                final PrintStream toServer = new PrintStream(sock.getOutputStream())
        ){
            String dataToSend;
            do {
                System.out.print("Enter text to send to the server, <stop> to end: ");
                System.out.flush ();
                dataToSend = in.readLine ();
                if (!dataToSend.contains("stop")){
                    toServer.println(dataToSend);
                    toServer.flush();
                }
            } while (!dataToSend.contains("stop"));
            for (int i = 1; i <= 20; i++){
                textFromServer = fromServer.readLine();
                if (textFromServer != null) System.out.println(textFromServer);
            }
        } catch (IOException x) {x.printStackTrace ();}
    }
}
