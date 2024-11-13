package server;

import framework.di.DIEngine;
import framework.di.DependencyContainer;
import framework.discovery.DiscoveryMechanism;
import framework.route.RouteHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
public class Server {
    public static final int TCP_PORT = 8080;
    private final RouteHandler routeHandler;
    private final DiscoveryMechanism discoveryMechanism;

    public Server(RouteHandler routeHandler, DiscoveryMechanism discoveryMechanism) {
        this.routeHandler = routeHandler;
        this.discoveryMechanism = discoveryMechanism;
    }

    public void start() {
        try {
            // Prvo skeniramo i inicijalizujemo sve
            discoveryMechanism.scan("example");  // ili koji god je root paket

            ServerSocket serverSocket = new ServerSocket(TCP_PORT);
            System.out.println("Server is running at http://localhost:" + TCP_PORT);

            while(true) {
                Socket socket = serverSocket.accept();
                new Thread(new ServerThread(socket, routeHandler)).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // Inicijalizacija framework komponenti
        DependencyContainer container = new DependencyContainer();
        DIEngine diEngine = new DIEngine(container);
        RouteHandler routeHandler = new RouteHandler(diEngine);
        DiscoveryMechanism discoveryMechanism = new DiscoveryMechanism(routeHandler, diEngine);

        // Kreiranje i pokretanje servera
        Server server = new Server(routeHandler, discoveryMechanism);
        server.start();
    }
}