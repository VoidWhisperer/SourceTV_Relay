package net.voksul.stvrelay;

import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.*;

public class Main {
    static Map<String, String> config = new HashMap<String, String>();
    static List<Integer> openPorts = new ArrayList<Integer>();
    static HashMap<Integer, ActiveServer> servers = new HashMap<Integer, ActiveServer>();

    public static void main(String[] args) throws IOException {
        //Load the config into a key-val hashmap
        loadConfig();
        System.out.println("Configuration loaded");
        //Grab the max and min tv ports out of the config and add them to list of ports to be used
        int minPort = Integer.parseInt(config.get("mintvport"));
        int maxPort = Integer.parseInt(config.get("maxtvport"));
        for (int i = minPort; i <= maxPort; i++) {
            openPorts.add(i);
        }
        System.out.println("Starting http server on " + config.get("ip") + ":" + config.get("port"));
        //Create the HTTP server and assign the handlers for the creation and destruction urls.
        HttpServer httpServer = HttpServer.create(new InetSocketAddress(config.get("ip"), Integer.parseInt(config.get("port"))), 0);
        httpServer.createContext("/createRelay", new CreateRelayHandler());
        httpServer.createContext("/destroyRelay", new DestroyRelayHandler());
        httpServer.start();
        System.out.println("HTTP server started.");
        //Start handling console input
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            String line = reader.readLine();
            handleConsoleInput(line);
        }
    }

    private static void handleConsoleInput(String line) {
        if (line.equalsIgnoreCase("list")) {
            if (servers.size() > 0) {
                int num = 1;
                for (Map.Entry<Integer, ActiveServer> server : servers.entrySet()) {
                    System.out.println(num + ". " + server.getValue().ip + ":" + server.getValue().port + " running on local port " + server.getValue().localPort + " PID " + server.getKey());
                }
            } else {
                System.out.println("There are no servers currently running.");
            }
        }
        if (line.toLowerCase().startsWith("destroy")) {
            if (line.split(" ").length > 1) {
                try {
                    Integer num = Integer.valueOf(line.split(" ")[1]);
                    if (servers.size() >= num) {
                        int i = 0;
                        Iterator<Map.Entry<Integer, ActiveServer>> iter = servers.entrySet().iterator();
                        while (iter.hasNext()) {
                            Map.Entry<Integer, ActiveServer> server = iter.next();
                            if (i == num - 1) {
                                reOpenPort(server.getValue().localPort);
                                Process p = Runtime.getRuntime().exec("kill -9 " + server.getKey()); //Todo: more graceful shutdown.
                                System.out.println("Killed server running on local port " + server.getValue().localPort);
                                removeServer(server.getKey());
                            }
                        }
                    } else {
                        System.out.println("That server does not exist.");
                    }

                } catch (Exception e) {
                    //Incase someone offers something that isn't a number to the console.
                    System.out.println("That is a not a valid number");
                }
            } else {
                System.out.println("Invalid command syntax. Try destroy [servernum] where you get the server number from the list command.");
            }
        }

    }

    private static void loadConfig() throws IOException {
        File configFile = new File(System.getProperty("user.dir") + "/config");
        FileReader fReader = new FileReader(configFile);
        BufferedReader reader = new BufferedReader(fReader);
        String line;
        int lineNum = 1;
        while ((line = reader.readLine()) != null) {
            String[] keyval = line.split("=");
            if (keyval.length == 2) {
                config.put(keyval[0].trim(), keyval[1].trim());
            } else {
                System.out.println("Malformed configuration value on line " + lineNum);
            }
            lineNum += 1;
        }
    }

    public static Map<String, String> getConfig() {
        return config;
    }

    public static int getOpenPort() {
        Integer port = openPorts.get(0);
        openPorts.remove(0);
        return port;
    }

    public static void reOpenPort(int port) {
        openPorts.add(port);
    }

    public static Map.Entry<Integer, ActiveServer> getServerAndPid(String ip, int port) {
        for (Map.Entry<Integer, ActiveServer> server : servers.entrySet()) {
            if (server.getValue().ip.equals(ip) && server.getValue().port == port) {
                return server;
            }
        }
        return null;
    }

    public static void removeServer(Integer key) {
        servers.remove(key);
    }
}
