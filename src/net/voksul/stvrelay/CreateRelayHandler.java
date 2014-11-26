package net.voksul.stvrelay;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.util.HashMap;

public class CreateRelayHandler implements HttpHandler {
    /**
     * Handles relay creation requests.
     *
     * @param httpExchange
     * @throws IOException
     */
    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        System.out.println("Request recieved on createrelay");
        HashMap<String, String> get_keyval = httpGet(httpExchange.getRequestURI().getQuery());
        if (get_keyval.containsKey("key") && get_keyval.containsKey("ip") && get_keyval.containsKey("port")) {
            if (get_keyval.get("key").equals(Main.getConfig().get("key"))) {
                if (Main.getServerAndPid(get_keyval.get("ip"), Integer.parseInt(get_keyval.get("port"))) == null) {
                    //Start the process on an open port
                    int port = Main.getOpenPort();
                    Process process = Runtime.getRuntime().exec(Main.config.get("pathtosrcds") + " -game tf -console +tv_enable \"1\" +tv_port " + port + " +tv_relay " + get_keyval.get("ip") + ":" + get_keyval.get("port"));
                    //Reflection hack to get the PID
                    try {
                        Field pid = process.getClass().getDeclaredField("pid");
                        pid.setAccessible(true);
                        Integer pidVal = (Integer) pid.get(process);
                        Main.servers.put(pidVal, new ActiveServer(get_keyval.get("ip"), Integer.parseInt(get_keyval.get("port")), port));
                        System.out.println("New STV relay started for " + get_keyval.get("ip") + ":" + get_keyval.get("port") + " on port " + port);
                        writeToPage("{\"success\":\"" + Main.getConfig().get("ip") + ":" + port + "\"}", 200, httpExchange);
                    } catch (NoSuchFieldException e) {
                        System.out.println(e.getMessage());
                    } catch (IllegalAccessException e) {
                        System.out.println(e.getMessage());
                    }
                } else {
                    //Write error saying that the server already exists
                    writeToPage("{\"error\":\"Server already exists for that ip and port.\"", 200, httpExchange);
                }
            } else {
                //Write error saying that the key is incorrect
                writeToPage("{\"error\":\"Incorrect key\"}", 201, httpExchange);
            }
        } else {
            //write error saying that the request did not have the right values.
            writeToPage("{\"error\":\"Malformed request\"}", 200, httpExchange);
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
        OutputStreamWriter osw = new OutputStreamWriter(httpExchange.getResponseBody());
        osw.write(s);
        osw.flush();
    }

    /**
     * Returns the values of a get request in key-value form
     *
     * @param query the get request
     * @return
     */
    private HashMap<String, String> httpGet(String query) {
        HashMap<String, String> result = new HashMap<String, String>();
        if (query != null) {
            for (String keyval : query.split("&")) {
                String[] split = keyval.split("=");
                if (split.length > 1) {
                    result.put(split[0], split[1]);
                }
            }
        }
        return result;
    }
}
