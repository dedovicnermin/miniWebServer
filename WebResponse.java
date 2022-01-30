// basis for MiniWebserver.java
import java.io.*;  // Get the Input Output libraries
import java.net.*; // Get the Java networking libraries

class ListenWorker extends Thread {    // Class definition
    Socket sock;                   // Class member, socket, local to ListnWorker.
    ListenWorker (Socket s) {sock = s;} // Constructor, assign arg s to local sock
    public void run(){
        PrintStream out = null;   // Input from the socket
        BufferedReader in = null; // Output to the socket
        try {
            out = new PrintStream(sock.getOutputStream());
            in = new BufferedReader
                    (new InputStreamReader(sock.getInputStream()));

            System.out.println("Sending the HTML Reponse now: " +
                    Integer.toString(WebResponse.i) + "\n" );
            String HTMLResponse = "<html> <h1> Hello Browser World! " +
                    Integer.toString(WebResponse.i++) +  "</h1> <p><p> <hr> <p>";
            out.println("HTTP/1.1 200 OK");
            out.println("Connection: close"); // Can fool with this.
            // int Len = HTMLResponse.length();
            // out.println("Content-Length: " + Integer.toString(Len));
            out.println("Content-Length: 400"); // Lazy, so set high. Calculate later.
            out.println("Content-Type: text/html \r\n\r\n");
            out.println(HTMLResponse);

            for(int j=0; j<6; j++){ // Echo some of the request headers for fun
                out.println(in.readLine() + "<br>\n"); // Save and calculate length
            }                                        // ...if you care to.
            out.println("</html>");

            sock.close(); // close this connection, but not the server;
        } catch (IOException x) {
            System.out.println("Error: Connetion reset. Listening again...");
        }
    }
}

public class WebResponse {

    static int i = 0;

    public static void main(String a[]) throws IOException {
        int q_len = 6; /* Number of requests for OpSys to queue */
        int port = 2540;
        Socket sock;


        ServerSocket servsock = new ServerSocket(port, q_len);

        System.out.println("Clark Elliott's WebResponse running at 2540.");
        System.out.println("Point Firefox browser to http://localhost:2540/abc.\n");
        while (true) {
            // wait for the next client connection:
            sock = servsock.accept();
            new ListenWorker (sock).start();
        }
    }
}
