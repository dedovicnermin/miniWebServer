package tech.nermindedovic.webserver;

import java.io.*;
import java.net.*;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class MiniWebserver {

    static final int CONCURRENT_REQ_LIMIT = 6;          // ServerSocket throws out 7th and onward request happening at the same time
    static final int SERVER_PORT = 2540;                // port
    static final AtomicBoolean SERVER_IS_RUNNING =
            new AtomicBoolean(true); // switch to turn of server

    public static void main(String[] args) throws IOException {
        try (final ServerSocket serverSocket = new ServerSocket(SERVER_PORT, CONCURRENT_REQ_LIMIT)) {
            System.out.println("Nermin Dedovic's MiniWebserver running at http://localhost:" + SERVER_PORT);
            System.out.println("Point Firefox browser to http://localhost:2540/abc.\n");

            while (SERVER_IS_RUNNING.get()) {
                // once client connection arrives, compose and delegate work to ServerWorker
                // accept more incoming requests
                final ServerWorker serverWorker = new ServerWorker(
                        serverSocket.accept()
                );
                serverWorker.start();
            }
        }
    }
}

class ServerWorker extends Thread {
    final Socket socket;

    ServerWorker (Socket socket) {
        this.socket = socket;
    }

    static final String DEFAULT_HTML = "<FORM method=\"GET\" action=\"http://localhost:2540/WebAdd.fake-cgi\">Enter your name and two numbers. My program will return the sum:<p><INPUT TYPE=\"text\" NAME=\"person\" size=20 placeholder=\"YourName\"><P><INPUT TYPE=\"text\" NAME=\"num1\" size=5 placeholder=\"x\"><br><INPUT TYPE=\"text\" NAME=\"num2\" size=5 placeholder=\"y\"><p><INPUT TYPE=\"submit\" VALUE=\"Submit Numbers\"></FORM></BODY></HTML>";
    static final int DEFAULT_HTML_LEN = DEFAULT_HTML.length();
    static final String TEMPLATE_HTML = "<FORM method=\"GET\" action=\"http://localhost:2540/WebAdd.fake-cgi\">%s<p><INPUT TYPE=\"text\" NAME=\"person\" size=20 placeholder=\"YourName\"><P><INPUT TYPE=\"text\" NAME=\"num1\" size=5 placeholder=\"x\"><br><INPUT TYPE=\"text\" NAME=\"num2\" size=5 placeholder=\"y\"><p><INPUT TYPE=\"submit\" VALUE=\"Submit Numbers\"></FORM></BODY></HTML>";
    private static final String STATUS_LINE = "HTTP/1.1 200 OK";
    private static final String CONTENT_TYPE = "Content-Type: text/html";
    private static final String CONNECTION_HEADER = "Connection: closed";



    @Override
    public void run() {
        // ensures resources are closed once code block processing complete
        // due to PrintStream and BufferedReader implementing Closable
        // measure taken to ensure no memory leaks / thread safety
        try (
                final PrintStream out = new PrintStream(socket.getOutputStream());
                final BufferedReader incoming = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {

            final String requestLine = incoming.readLine();
            if (Objects.nonNull(requestLine) && requestLine.contains("/favicon.ico")) return;

            if (ReqRespService.containsFormData(requestLine)) {
                final SumFormData sumFormData = ReqRespService.buildFormData(requestLine);
                final String responseBody = String.format(TEMPLATE_HTML, sumFormData.getFormattedOutput());
                final int responseBodyLength = responseBody.length();
                sendResponse(out, responseBody, responseBodyLength);
            } else {
                sendResponse(out, DEFAULT_HTML, DEFAULT_HTML_LEN);
            }
        } catch (IOException x) { System.out.println("Error: Connection reset. Listening again..."); }
        finally {                                                                     // So long as computer doesn't get nuked, close the socket once processing is complete
            try { socket.close(); }
            catch (IOException e) {System.out.println("ERROR (ServerWorker attempting to close socket): " + e);}
        }
    }

    // handles sending response to client with headers and response body
    private void sendResponse(final PrintStream out, final String responseBody, final int responseBodyLength) {
        out.println(STATUS_LINE);
        out.println(CONNECTION_HEADER);
        out.println(CONTENT_TYPE);
        out.println("Content-Length: " + responseBodyLength + "\r\n\r\n");                  // HTTP protocol and importance of the carriage return and how that signifies distinction between requestHeaders and the data/payload
        out.println(responseBody);
        out.flush();
    }
}

// DAO to make calculation and storing temporary fields easier
class SumFormData {
    private String name;
    private String num1;
    private String num2;

    public String getName() {
        return name;
    }

    public String getNum1() {
        return num1;
    }

    public String getNum2() {
        return num2;
    }

    // used to map query string key/val pairs onto SumFormData representation
    // helps keep track of user input and results for all scenarios (happy / sad path)
    public void mapToObject(final String keyAndValue) {
        final String[] split = keyAndValue.split("=");
        final String key = split[0];
        final boolean hasValue = split.length == 2;

        if ("person".equals(key)) {
            if (hasValue) {
                this.name = split[1].trim();
            } else {
                this.name = "Friend";               // if client submits form without a name, we call them Friend
            }
        } else if ("num1".equals(key)) {
            if (hasValue) {
                this.num1 = split[1].trim();
            } else {
                this.num1 = "UNSPECIFIED";        // client did not enter a value for num1 field
            }
        } else if ("num2".equals(key)) {
            if (hasValue) {
                this.num2 = split[1].trim();
            } else {
                this.num2 = "UNSPECIFIED";      // client did not enter a value for num2 field
            }
        }
    }

    // when a client submits the form, professor text replaced with formatted string
    public String getFormattedOutput() {
        return String.format("Hi, %s!%n Your previous request to retrieve the sum of %s and %s resulted with a sum of %s", getName(), getNum1(), getNum2(), getSum());
    }

    // calculation
    // handles invalid user input or no user input
    public String getSum() {
        int first;
        int second;
        try {
            first =  num1 == null || num1.isEmpty() ? 0 : Integer.parseInt(num1);
            second = num2 == null || num2.isEmpty() ? 0 : Integer.parseInt(num2);
        } catch (NumberFormatException e) {                                             // to take care of mischievous clients
            return "0";
        }
        return "" + (first + second);
    }
}

class ReqRespService {
    private ReqRespService() {}

    public static boolean containsFormData(final String requestLine) {
        if (Objects.isNull(requestLine)) return false;
        return requestLine.contains("?");
    }

    // created DAO from requestLine - ie GET <resource>?key1=value&key2=value2 HTTP/1.1
    // and map each keyValue pair with its respected 1:1 matching
    public static SumFormData buildFormData(final String requestLine) {
        final String[] methodResourceLang = requestLine.split(" ");                   // [GET , /resource/path, HTTP/1.1lang]
        final String resource = methodResourceLang[1];                                      // potential to have query params - ie. /resource?key=value&...
        final String resourceFormatted = resource.replace('?', ' ');       // replace the first question mark we see. Room for issues depending on resource path
        final String[] resourceAndData = resourceFormatted.split(" ");               // inorder to get characters from ? and onward
        final String data = resourceAndData[1];                                            // key1=value1&key2=value2 - only KeyValue pairs delimited by &

        final String[] keyValuePairsDelimited = data.split("&");
        final SumFormData sumFormData = new SumFormData();
        for (String keyValPair : keyValuePairsDelimited) {                                 // for each key value pair, map the value to DAO if the key matches its representation
            sumFormData.mapToObject(keyValPair);
        }
        return sumFormData;
    }

}
