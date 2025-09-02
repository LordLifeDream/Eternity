package de.lldgames.eternity;

import de.lldgames.eternity.commands.Command;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Eternity {
    public static final ArrayList<App> loadedApps = new ArrayList<>();

    public static void main(String[] args) {
        File appsFile = new File("./apps.json");
        try{
            if(!appsFile.exists()){
                JSONObject dummy = new JSONObject();
                dummy.put("exampleApp", App.generateDummyJSON());
                try(FileOutputStream fos = new FileOutputStream(appsFile)){
                    fos.write(dummy.toString().getBytes());
                }
                System.exit(11);
            }
        }catch (Exception e){
            throw new RuntimeException(e);
        }
        String content;
        try(FileInputStream fis = new FileInputStream(appsFile)){
            content = new String(fis.readAllBytes());
        }catch (Exception e){
            throw new RuntimeException(e);
        }
        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            System.out.println("shutdown hook called!");
            for(App a: loadedApps){
                a.stop();
            }
        }));

        JSONObject apps = new JSONObject(content);
        for(String key: apps.keySet()){
            JSONObject appJson = apps.getJSONObject(key);
            //disabled check here so we don't even create the app object
            //if no enabled key present, it's probably supposed to be enabled so don't quit
            if(appJson.has("enabled") && !appJson.getBoolean("enabled")) continue;
            App app = new App(appJson, key);
            loadedApps.add(app);
            if(apps.getJSONObject(key).has("gui") && apps.getJSONObject(key).getBoolean("gui") && false) new ProcessOutputViewer(null).displayApp(app);
            System.out.println("loaded app " + key);
        }
        setupIn();
    }

    public static void setupIn(){
        Command.registerCMDs();
        try(Scanner sc = new Scanner(System.in)){
            while(sc.hasNext()){
                String line = sc.nextLine();
                String[] tokenized = line.split(" ");
                if(tokenized.length==0) continue;
                Command.callCmd(tokenized);
            }
        }
    }
}
