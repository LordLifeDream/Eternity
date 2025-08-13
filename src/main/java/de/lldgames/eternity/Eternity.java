package de.lldgames.eternity;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class Eternity {
    public static void main(String[] args) {
        ArrayList<App> loadedApps = new ArrayList<>();
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
            App app = new App(apps.getJSONObject(key), key);
            loadedApps.add(app);
            if(apps.getJSONObject(key).has("gui") && apps.getJSONObject(key).getBoolean("gui") && false) new ProcessOutputViewer(null).displayApp(app);
            System.out.println("loaded app " + key);
        }
    }
}
