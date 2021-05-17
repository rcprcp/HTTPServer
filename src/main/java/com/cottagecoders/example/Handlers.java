package com.cottagecoders.example;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

class Handlers implements HttpHandler {
  private static final Logger LOG = LoggerFactory.getLogger(Handlers.class);
  private int pageNumber = 1;

  @Override
  public void handle(HttpExchange httpExchange) throws IOException {
    String requestParamValue;
    String method = httpExchange.getRequestMethod();
    if ("GET".equals(method)) {
      getRequest(httpExchange);
    } else if ("POST".equals(method)) {
      postRequestSOAP500(httpExchange);
    } else {
      LOG.error("unknown http verb {}", method);
      return;
    }
  }

  private String getRequest(HttpExchange httpExchange) {
    return httpExchange.getRequestURI().toString().split("\\?")[1].split("=")[1];
  }

  private void postRequestSOAP500(HttpExchange httpExchange) {
    BufferedReader br = new BufferedReader(new InputStreamReader(httpExchange.getRequestBody(),
        StandardCharsets.UTF_8
    ));

    String str = br.lines().collect(Collectors.joining(System.lineSeparator()));

    if (HTTPServer.DEBUG) {
      Headers h = httpExchange.getRequestHeaders();
      for (Map.Entry<String, List<String>> e : h.entrySet()) {
        LOG.debug("request header {} / {}", e.getKey(), e.getValue());
      }
    }

    final String SOAP = "<soap:Envelope xmlns:soap=http://schemas.xmlsoap.org/soap/envelope/>\n" + "    <soap:Body>\n"
        + "        " + "<soap:Fault>\n" + "            <faultcode>soap:1028</faultcode>\n" + "            " +
        "<faultstring>Response is " + "not available for version 20201006040025.</faultstring>\n" + "            " +
        "<detail>\n" + "                " + "<Code>1028</Code>\n" + "                <param>20201006040025</param>\n" + "            </detail>\n" + "    " + "    </soap:Fault>\n" + "    </soap:Body>\n" + "</soap:Envelope>\n\n";
    try {
      httpExchange.sendResponseHeaders(500, SOAP.length());
      LOG.info("payload length {}, payload: {}", SOAP.length(), SOAP);
      OutputStream outputStream = httpExchange.getResponseBody();
      outputStream.write(SOAP.getBytes(StandardCharsets.UTF_8));
      outputStream.flush();
      outputStream.close();

    } catch (IOException ex) {
      LOG.error("IOException on sendResponseHeaders {}", ex.getMessage(), ex);
    }
  }

  String arrayOfDummyData() {
    StringBuilder sb = new StringBuilder();
    if (HTTPServer.NULL && pageNumber == 5) {
      pageNumber = 1;
      return "{results: []}";
    }

    sb.append("{\"pageNumber\": ");
    sb.append(pageNumber);
    ++pageNumber;
    sb.append(", ");

    sb.append("\"names\":");
    sb.append("[");
    for (int i = 0; i <= 10; i++) {
      sb.append("\"" + UUID.randomUUID() + "\"");
      if (i < 10) {
        sb.append(", ");
      } else {
        sb.append(" ");
      }

    }
    sb.append("]}");
    return sb.toString();
  }

  String singleRecord() {
    StringBuilder sb = new StringBuilder();
    sb.append("{\"fname\": \"");
    sb.append(UUID.randomUUID());
    sb.append("\"}");
    return sb.toString();
  }

  private void postRequestJSON200(HttpExchange httpExchange) {
    BufferedReader br = new BufferedReader(new InputStreamReader(httpExchange.getRequestBody(),
        StandardCharsets.UTF_8
    ));

    String str = br.lines().collect(Collectors.joining(System.lineSeparator()));
    LOG.debug("request body '{}'", str);

    if (HTTPServer.DEBUG) {
      Headers h = httpExchange.getRequestHeaders();
      for (Map.Entry<String, List<String>> e : h.entrySet()) {
        LOG.debug("request header {} / {}", e.getKey(), e.getValue());
      }
    }

    // parse the POST body - pick out pageNumber
    JsonParser parser = new JsonParser();
    JsonElement element = parser.parse(str);
    JsonObject jsonObject = element.getAsJsonObject();

    String localPageNumber = jsonObject.get("pageNumber").toString();
    LOG.info("pageNumber {}", localPageNumber);
    pageNumber = Integer.parseInt(localPageNumber);

    StringBuilder jsonBuilder = new StringBuilder();
    jsonBuilder.append(arrayOfDummyData());
    String json = jsonBuilder.toString();

    try {
      // this line is a must
      httpExchange.sendResponseHeaders(200, jsonBuilder.length());
      OutputStream outputStream = httpExchange.getResponseBody();
      LOG.info("payload length {}, payload: {}", json.length(), json);

      outputStream.write(json.getBytes(StandardCharsets.UTF_8));

      outputStream.write("\n".getBytes(StandardCharsets.UTF_8));
      outputStream.flush();
      outputStream.close();

    } catch (IOException ex) {
      LOG.error("sendResponseHeaders exception: {} ", ex.getMessage(), ex);
    }
  }
}
