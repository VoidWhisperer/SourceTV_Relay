package net.voksul.stvrelay;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DestroyRelayHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        HashMap<String, String> get_keyval = httpGet(httpExchange.getRequestURI().getQuery());
        if (get_keyval.containsKey("key") && get_keyval.containsKey("ip") && get_keyval.containsKey("port")) {
            if (get_keyval.get("key").equals(Main.getConfig().get("key"))) {
                //Check to make sure the server exists
                if (Main.getServerAndPid(get_keyval.get("ip"), Integer.parseInt(get_keyval.get("port"))) != null) {
                    //Kill the server, re-open the port, and output the success message
                    Map.Entry<Integer, ActiveServer> server = Main.getServerAndPid(get_keyval.get("ip"), Integer.parseInt(get_keyval.get("port")));
                    System.out.println("Destroying relay on port " + server.getValue().localPort + " that was attached to server " + server.getValue().ip + ":" + server.getValue().port);
                    Main.reOpenPort(server.getValue().localPort);
                    Process p = Runtime.getRuntime().exec("kill -9 " + server.getKey()); //Todo: more graceful shutdown.
                    Main.removeServer(server.getKey());
                    writeToPage("{\"success\":\"Server destroyed.\"}", 200, httpExchange);
                } else {
                    //Write error saying that the server already exists
                    writeToPage("{\"error\":\"Server doesn't exist for that ip and port.\"}", 400, httpExchange);
                }
            } else {
                //Write error saying that the key is incorrect
                writeToPage("{\"error\":\"Incorrect key\"}", 401, httpExchange);
            }
        } else {
            //write error saying that the request did not have the right values.
            writeToPage("{\"error\":\"Malformed request\"}", 400, httpExchange);
        }
    }

    /**
     * Utility method for writing text to the page
     *
     * @param s            Text to write
     * @param code         The response code
     * @param httpExchange the httpexchange to write the response to
     * @throws IOException
     */
    private void writeToPage(String s, int code, HttpExchange httpExchange) throws IOException {
        httpExchange.sendResponseHeaders(code, s.length());
        httpExchange.getResponseBody().write(s.getBytes());
    }

    /**
     * Returns the values of a get request in key-value form
     *
     * @param query the get request
     * @return
     */
    private HashMap<String, String> httpGet(String query) {
        HashMap<String, String> result = new HashMap<String, String>();
        for (String keyval : query.split("&")) {
            String[] split = keyval.split("=");
            if (split.length > 1) {
                result.put(split[0], split[1]);
            }
        }
        return result;
    }
}
