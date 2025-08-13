package de.lldgames.eternity;

import org.json.JSONObject;

import java.io.*;
import java.time.LocalDateTime;

public class AppIOHandler {
    public static final JSONObject DEFAULT_CONFIG = new JSONObject().put("ui", false).put("writeToFile", true);
    private Process process;
    private File workingDir;
    //config stuff
    private boolean writeToFile;
    private boolean ui;
    //---
    private BufferedWriter outWriter;
    private ProcessOutputViewer viewer;

    public AppIOHandler(/*Process process, */File workingDir, JSONObject config){
        //this.process = process;
        this.workingDir = workingDir;

        this.writeToFile = config.has("writeToFile") &&config.getBoolean("writeToFile");
        this.ui = config.has("ui") &&config.getBoolean("ui");
    }

    public void onMessage(String msg){
        if(this.writeToFile){
            try {
                outWriter.newLine();
                outWriter.write(LocalDateTime.now().toString()+": "+msg);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if(this.ui && this.viewer !=null){
            this.viewer.onMessage(msg);
        }
    }

    public void onError(String err){
        if(this.writeToFile){
            try {
                outWriter.newLine();
                outWriter.write("[-ERR-] "+LocalDateTime.now().toString()+": "+err);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if(this.ui && this.viewer !=null){
            this.viewer.onError(err);
        }
    }

    public void handleProcess(Process process){
        this.process = process;
        if(this.writeToFile){
            this.setupFileWriter();
        }
        if(this.ui){
            if(this.viewer == null) this.setupUI();
            else this.viewer.setProcess(this.process);
        }
        new Thread(() -> {
            try (InputStream inputStream = process.getInputStream();
                 InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                 BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {

                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    this.onMessage(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        new Thread(() -> {
            try (InputStream errorStream = process.getErrorStream();
                 InputStreamReader errorStreamReader = new InputStreamReader(errorStream);
                 BufferedReader bufferedReader = new BufferedReader(errorStreamReader)) {

                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    this.onError(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void setupUI(){
        this.viewer = new ProcessOutputViewer(this.process);
    }

    private void setupFileWriter(){
        try {
            String now = LocalDateTime.now().toString();
            this.outWriter = new BufferedWriter(new FileWriter(this.workingDir.getAbsolutePath()+"/LOG_"+now+".txt"));
            process.onExit().thenAccept((p)->{
                try {
                    outWriter.newLine();
                    outWriter.write("-------------");
                    outWriter.write(LocalDateTime.now().toString());
                    outWriter.write("-------------");
                    outWriter.newLine();
                    outWriter.write("process ended with exit code " + p.exitValue());
                    outWriter.close();
                }catch (Exception e){
                    throw new RuntimeException(e);
                }
            });
        }catch (Exception e){
            e.printStackTrace();
            System.err.println("something went horribly wrong. NO FILE LOGGING!");
            this.writeToFile = false;
        }
    }


}
