package net.voksul.stvrelay;

/**
 * Created by Administrator on 11/24/2014.
 */
public class ActiveServer {
    String ip;
    int port;
    int localPort;
    public ActiveServer(String ip, int port, int localPort) {
        this.ip = ip;
        this.port = port;
        this.localPort = localPort;
    }
    //Utility class for tracking active servers
}
