package de.lldgames.eternity.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Scanner;

public class EternityServer {
    private static String token;
    private static HttpServer server;
    private static final File webRootFile = new File("./webRoot").getAbsoluteFile();
    private static ArrayList<String> publicSites = new ArrayList<>();

    public static void main(String[] args) {
        start();
    }

    public static void start(){
        try {
            File tokenFile = new File("./WEBTOKEN.txt");
            System.out.println( "[eternity SERVER] looking for token in "+tokenFile.getAbsolutePath());
            token = new BufferedReader(new FileReader("./WEBTOKEN.txt")).readLine();
            File publicFile = new File("./WEBPUBLICS.txt");
            System.out.println("[eternity SERVER] looking for public sites in " + publicFile.getAbsolutePath());
            try(Scanner sc = new Scanner(new FileReader(publicFile))){
                while(sc.hasNext()){
                    publicSites.add(sc.nextLine());
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            server = HttpServer.create(new InetSocketAddress(7713), -1);
            server.start();
            createEndpoints();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void stop(){
        server.stop(0);
    }

    private static void createEndpoints(){
        server.createContext("/", EternityServer::handleWebRootReq);
    }

    private static void handleWebRootReq(HttpExchange ex){
        String path = ex.getRequestURI().getPath();
        if(path.endsWith("/")) path += "index.html";
        System.out.println("web root req for " +path);
        File f = new File("./webRoot"+path);
        if(!f.getAbsolutePath().startsWith(webRootFile.getAbsolutePath())){
            respond(ex, 423, "423 - Locked!");
            ex.close();
            return;
        }
        if(!publicSites.contains(path) && !checkAuth(ex)){
            respond(ex, 401, "401 - Unauthorized!");
            ex.close();
            return;
        }
        if(!f.exists()){
            respond(ex, 404, "404 - Not Found!");
            ex.close();
            return;
        }
        try(FileInputStream fis = new FileInputStream(f)){
            byte[] fileBytes = fis.readAllBytes();
            ex.sendResponseHeaders(200, fileBytes.length);
            ex.getResponseBody().write(fileBytes);
            ex.close();
        }catch (Exception e){
            e.printStackTrace();
            respond(ex, 500, "500 - Internal Server Error!");
        }
        ex.close();
    }

    private static void respond(HttpExchange e, int code, String body){
        try {
            e.sendResponseHeaders(code, body.length());
            e.getResponseBody().write(body.getBytes());
            e.close();
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private static boolean checkAuth(HttpExchange e){
        return  e.getRequestHeaders().containsKey("authorization") &&
                e.getRequestHeaders().getFirst("authorization").equals(token);
    }
}
