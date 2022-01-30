package tech.nermindedovic.webserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;

public class MyListener {
    public static boolean controlSwitch = true;

    public static void main(String[] args) throws IOException {
        int q_len = 6; /* Number of requests for OpSys to queue */
        int port = 2540;
        Socket sock = null;


        try (ServerSocket servsock = new ServerSocket(port, q_len)) {
            System.out.println("Clark Elliott's Port listener running at 2540.\n");
            while (controlSwitch) {
                // wait for the next client connection:
                try {
                    sock = servsock.accept();
                    new ListenWorker (sock).start();
                } catch (IOException e) {
                    System.out.println("Caught exception : " + e);
                }
            }
        } finally {
            if (Objects.nonNull(sock)) sock.close();
        }
    }
}


class ListenWorker extends Thread {
    Socket sock;
    ListenWorker(Socket s) {sock = s;}

    @Override
    public void run(){
        // Get I/O streams from the socket:
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                PrintStream out = new PrintStream(sock.getOutputStream())
        ) {

            String sockdata;
            while (true) {
                sockdata = in.readLine ();
                if (sockdata != null) System.out.println(sockdata);
                System.out.flush ();
            }
        } catch (IOException x) {
            System.out.println("Connetion reset. Listening again...");
        } finally {
            System.out.println("Worker done with work...");
            try {
                sock.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

}
