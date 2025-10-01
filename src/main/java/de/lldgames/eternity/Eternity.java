package de.lldgames.eternity;

import de.lldgames.eternity.commands.Command;
import de.lldgames.eternity.selfUpdate.SelfUpdater;
import de.lldgames.eternity.web.EternityServer;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Eternity {
    public static final ArrayList<App> loadedApps = new ArrayList<>();
    public static final File appsFile = new File("./apps.json");
    public static String[] args;

    public static JSONObject loadAppsFromFile(){
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

        return new JSONObject(content);
    }

    public static void loadApps(JSONObject appsFile){
        //JSONObject apps = loadAppsFromFile();
        for(String key: appsFile.keySet()){
            JSONObject appJson = appsFile.getJSONObject(key);
            addApp(appJson, key);
        }
    }

    public static void addApp(JSONObject appJson, String name){
        //disabled check here so we don't even create the app object
        //if no enabled key present, it's probably supposed to be enabled so don't quit
        if(appJson.has("enabled") && !appJson.getBoolean("enabled")) return;
        App app = new App(appJson, name);
        loadedApps.add(app);
        //if(appsFile.getJSONObject(key).has("gui") && appsFile.getJSONObject(key).getBoolean("gui") && false) new ProcessOutputViewer(null).displayApp(app);
        System.out.println("loaded app " + name);
    }

    public static void main(String[] args) {
        Eternity.args = args;
        System.out.println("using arguments: " + Arrays.toString(args));
        if(!Arrays.stream(args).toList().contains("noSelfUpdate")){
            SelfUpdater.start();
            return;
        }


        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            System.out.println("shutdown hook called!");
            for(App a: loadedApps){
                a.stop();
                a.closeRepo();
            }
        }));

        //loadApps(loadAppsFromFile());

        EternityServer.start();

        //if(!Arrays.stream(args).toList().contains("noSelfUpdate"))SelfUpdater.start();


        try {
            TimeUnit.MILLISECONDS.sleep(5000);
            System.out.println("dsfdsfdfsdffdsdfsdsfsfdsfdsfsdf");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
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




    public static Process createEternityProcess(String[] args2){
        try {
            String[] cmd = new String[]{"java", "-jar", "./buildOut/eternity.jar"};
            ArrayList<String> ls = new ArrayList<>(Arrays.stream(cmd).toList());
            ls.addAll(Arrays.stream(args2).toList());
            return new ProcessBuilder(/*"java", "-jar", "./buildOut/eternity.jar" */ ls)
                    .directory(new File("./").getAbsoluteFile())
                    .inheritIO()
                    .start();
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @Deprecated
    public static void restart(){
        EternityServer.stop();
        System.out.println("ETERNITY RESTART ---- BEGIN NEW IO");
        String[] cmd = new String[]{"java", "-jar", "./buildOut/eternity.jar"};
        ArrayList<String> ls = new ArrayList<>(Arrays.stream(cmd).toList());
        ls.addAll(Arrays.stream(args).toList());
        try {
            Process p = new ProcessBuilder(/*"java", "-jar", "./buildOut/eternity.jar" */ ls)
                    .directory(new File("./").getAbsoluteFile())
                    .inheritIO()
                    .start();
            System.exit(0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
