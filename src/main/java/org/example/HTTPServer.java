package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.StringTokenizer;

public class HTTPServer implements Runnable {

  // port to listen connection
  private static final int PORT = 8085;

  private static final long NAPTIME = Long.parseLong(System.getProperty("naptime", "0"));
  private static final boolean CLOSE = System.getProperty("close", "true").equalsIgnoreCase("true");
  private static final boolean DEBUG = System.getProperty("debug", "false").equalsIgnoreCase("true");

  private static long sequence = 1;
  private final Socket connect;

  public HTTPServer(Socket c) {
    connect = c;
  }

  public static void main(String[] args) {
    try {

      System.out.println("Listening for connections on port : " + PORT);
      System.out.println("debug mode " + DEBUG);
      System.out.println("close connections? " + CLOSE);
      System.out.println("naptime " + NAPTIME);

      try (ServerSocket serverConnect = new ServerSocket(PORT)) {

        while (true) {   //NOSONAR: S2189      prevent infinite loop warning.
          HTTPServer myServer = new HTTPServer(serverConnect.accept());
          Thread thread = new Thread(myServer);
          thread.start();
        }
      }

    } catch (IOException e) {
      System.out.println("Server Connection error : " + e.getMessage());
    }
  }

  @Override
  public void run() {
    // we manage our particular client connection
    BufferedReader in = null;
    PrintWriter out = null;

    // don't use try-with-resources - we want to control closing (or not closing) the connection.
    try {
      in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
      out = new PrintWriter(connect.getOutputStream());
      if(in == null || out == null) {
        System.out.println("in or out is null.  fail.");
        System.exit(5);

      }

      String input = in.readLine();
      StringTokenizer parse = new StringTokenizer(input);
      String method = parse.nextToken().toUpperCase(); // we get the HTTP method of the client

      // we support only GET

      if (!method.equals("GET")) {
        String contentMimeType = "text/html";

        // we send HTTP Headers with data to client
        String NOT_IMPLEMENTED = "HTTP/1.1 501 Not Implemented";
        System.out.println("" + method + ": " + NOT_IMPLEMENTED);

        out.println(NOT_IMPLEMENTED);
        out.println("Server: Java HTTP Server from SSaurel : 1.0");
        out.println("Date: " + new Date());
        out.println("Content-type: " + contentMimeType);

        out.println("Content-length: " + NOT_IMPLEMENTED.length());
        out.println(); // blank line between headers and content, very important !
        out.flush(); // flush character output stream buffer

      } else {
        // GET or HEAD method

        String response = "{\"key\": " + sequence++ + ", \"value\": \"" + new Date() + "\"}\n";

        // send HTTP Headers
        out.println("HTTP/1.1 200 OK");
        out.println("Server: bob HTTP Server : 1.0");
        out.println("Date: " + new Date());
        out.println("Content-type: " + "application/json");
        out.println("Content-length: " + response.length());
        out.println(); // blank line between headers and content
        out.write(response);
        out.flush(); // flush character output stream buffer

        if (DEBUG) {
          System.out.println("" + response);
        }
      }
    } catch (IOException ex) {
      System.out.println("Exception: " + ex.getMessage());
      ex.printStackTrace();
      System.exit(3);

    } finally {
      try {
        Thread.sleep(NAPTIME);
        in.close();
        if (CLOSE) {
          out.close();
          connect.close();
        }
      } catch (IOException e) {
        System.out.println("IOException closing in, out or the connection : " + e.getMessage());

      } catch(InterruptedException ex) {
        System.out.println("InterruptedException before close");
        System.exit(7);
      }
    }
  }
}
