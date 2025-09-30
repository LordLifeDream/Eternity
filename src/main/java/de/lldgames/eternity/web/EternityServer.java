package de.lldgames.eternity.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;

public class EternityServer {
    private static String token;
    private static HttpServer server;

    public static void main(String[] args) {
        start();
    }

    public static void start(){
        try {
            File tokenFile = new File("./WEBTOKEN.txt");
            System.out.println( "[eternity SERVER] looking for token in "+tokenFile.getAbsolutePath());
            token = new BufferedReader(new FileReader("./WEBTOKEN.txt")).readLine();
            server = HttpServer.create(new InetSocketAddress(7713), -1);
            server.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void createEndpoints(){

    }

    private boolean checkAuth(HttpExchange e){
        return  e.getRequestHeaders().containsKey("authorization") &&
                e.getRequestHeaders().getFirst("authorization").equals(token);
    }
}
