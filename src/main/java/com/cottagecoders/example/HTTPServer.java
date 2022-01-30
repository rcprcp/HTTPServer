package com.cottagecoders.example;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class HTTPServer {
  private static final Logger LOG = LogManager.getLogger(HTTPServer.class);
  static boolean DEBUG = false;
  static boolean NULL = false;
  private static int PORT = 8085;
  private static long NAPTIME = 0;

  public static void main(String... args) throws IOException {
    ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
    LOG.info("Starting.");

    // handle args.
    // create Options object
    Options options = new Options();

    // add t option
    options.addOption("d", "debug", false, "enable debug code");
    options.addOption("p", "port", true, "specify port to use (default: 8085");
    options.addOption("u", "null", false, "null payload on 5th record");
    options.addOption("n", "naptime", true, "specify naptime durtion (in ms)");

    CommandLineParser parser = new DefaultParser();
    CommandLine cmd = null;
    try {
      cmd = parser.parse(options, args);
    } catch (ParseException ex) {
      LOG.error("Exception: {}", ex.getMessage(), ex);
      System.exit(5);
    }

    if (cmd.hasOption("d")) {
      DEBUG = true;
    }
    if (cmd.hasOption("u")) {
      NULL = true;
    }
    if (cmd.hasOption("n")) {
      try {
        NAPTIME = Integer.parseInt(cmd.getOptionValue("n"));
      } catch (NumberFormatException ex) {
        LOG.error("Exception: {}", ex.getMessage(), ex);
        System.exit(6);
      }
    }
    if (cmd.hasOption("p")) {
      try {
        PORT = Integer.parseInt(cmd.getOptionValue("p"));
      } catch (NumberFormatException ex) {
        LOG.error("Exception: {}", ex.getMessage(), ex);
        System.exit(6);
      }
    }
    com.sun.net.httpserver.HttpServer server = com.sun.net.httpserver.HttpServer.create(new InetSocketAddress(PORT), 0);
    server.createContext("/test", new Handlers());
    server.setExecutor(threadPoolExecutor);
    server.start();
    LOG.info(" Server started on port " + PORT);
  }

}
