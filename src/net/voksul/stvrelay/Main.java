package net.voksul.stvrelay;

import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    static Map<String,String> config = new HashMap<String,String>();
    static List<Integer> openPorts = new ArrayList<Integer>();
    static HashMap<Integer,ActiveServer> servers = new HashMap<Integer,ActiveServer>();
    public static void main(String[] args) throws IOException {
        //Load the config into a key-val hashmap
        loadConfig();
        System.out.println("Configuration loaded");
        //Grab the max and min tv ports out of the config and add them to list of ports to be used
        int minPort = Integer.parseInt(config.get("mintvport"));
        int maxPort = Integer.parseInt(config.get("maxtvport"));
        for (int i = minPort; i <= maxPort; i++)
        {
            openPorts.add(i);
        }
        System.out.println("Starting http server on " + config.get("ip") + ":" + config.get("port"));
        //Create the HTTP server and assign the handlers for the creation and destruction urls.
        HttpServer httpServer = HttpServer.create(new InetSocketAddress(config.get("ip"),Integer.parseInt(config.get("port"))),0);
        httpServer.createContext("/createRelay", new CreateRelayHandler());
        httpServer.createContext("/destroyRelay", new DestroyRelayHandler());
        httpServer.start();
        System.out.println("HTTP server started.");
        //Start handling console input
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while(true)
        {
            String line = reader.readLine();
            handleConsoleInput(line);
        }
    }

    private static void handleConsoleInput(String line) {
        if(line.equalsIgnoreCase("list"))
        {
            if(servers.size() > 0) {
                for (Map.Entry<Integer, ActiveServer> server : servers.entrySet()) {
                    System.out.println(server.getValue().ip + ":" + server.getValue().port + " running on local port " + server.getValue().localPort + " PID " + server.getKey());
                }
            }else{
                System.out.println("There are no servers currently running.");
            }
        }
    }

    private static void loadConfig() throws IOException {
        File configFile = new File(System.getProperty("user.dir") + "/config");
        FileReader fReader = new FileReader(configFile);
        BufferedReader reader = new BufferedReader(fReader);
        String line;
        int lineNum = 1;
        while((line = reader.readLine()) != null)
        {
            String[] keyval = line.split("=");
            if(keyval.length == 2)
            {
                config.put(keyval[0].trim(),keyval[1].trim());
            }else{
                System.out.println("Malformed configuration value on line " + lineNum);
            }
            lineNum+=1;
        }
    }

    public static Map<String,String> getConfig()
    {
        return config;
    }

    public static int getOpenPort()
    {
        Integer port = openPorts.get(0);
        openPorts.remove(0);
        return port;
    }

    public static void reOpenPort(int port)
    {
        openPorts.add(port);
    }

    public static Map.Entry<Integer,ActiveServer> getServerAndPid(String ip, int port)
    {
        for(Map.Entry<Integer,ActiveServer> server : servers.entrySet())
        {
            if(server.getValue().ip.equals(ip) && server.getValue().port == port)
            {
                return server;
            }
        }
        return null;
    }

    public static void removeServer(Integer key) {
        servers.remove(key);
    }
}
