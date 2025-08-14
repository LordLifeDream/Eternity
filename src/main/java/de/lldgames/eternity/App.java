package de.lldgames.eternity;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.merge.ContentMergeStrategy;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.json.JSONObject;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.net.http.HttpRequest;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class App {
    private String repoURL; //github.com/user/xx.git
    private String localLocation; //./xx
    private String gitToken; //gh user access token
    private String runCmd; // command to start the app. eg. java -jar x.jar TODO: make better tokenization
    private long pullInterval; //pull interval in minutes.
    private String mergeStrategy; // MergeStrategy.get()

    public final String name;

    private Git repo;
    public Process process;
    private AppIOHandler ioHandler;
    private boolean shouldBeRunning;
    private long startTime = -1;


    public App(JSONObject data, String name){
        this.repoURL = data.getString("repoURL");
        this.localLocation = data.getString("localLocation");
        this.gitToken = data.getString("gitToken");
        this.runCmd = data.getString("runCmd");
        this.pullInterval = data.getLong("pullInterval");
        this.mergeStrategy = data.has("mergeStrategy")? data.getString("mergeStrategy") : MergeStrategy.OURS.getName();
        this.name = name;
        this.ioHandler = new AppIOHandler(/*null, */new File(this.localLocation),
                data.has("io")? data.getJSONObject("io"):AppIOHandler.DEFAULT_CONFIG);
        this.init();

        this.start();
        this.runPullLoop();
    }

    public void runPullLoop(){
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(this::update, this.pullInterval, this.pullInterval, TimeUnit.MINUTES);
    }

    /**
     * pulls and restarts if pulled.
     */
    public void update(){
        if(this.pullRepo()){
            System.out.println("new changes pulled in app "+this.name +" @ "+ LocalDateTime.now());
            this.restart();
        }
    }

    //start/stop
    public void start(){
        this.stop();
        System.out.println(this.name+": start() @ "+ LocalDateTime.now());
        this.shouldBeRunning = true;
        try {
            boolean isWindows = System.getProperty("os.name").toLowerCase().contains("windows");
            //fix for npm on windows
            if(isWindows&& runCmd.contains("npm ")) runCmd= runCmd.replaceAll("npm ", "npm.cmd ");
            ProcessBuilder pb = new ProcessBuilder(runCmd.split(" "))
                    .directory(new File(this.localLocation));
                    //.inheritIO();
            this.process = pb.start();
            this.ioHandler.handleProcess(this.process);
            this.startTime = System.currentTimeMillis();
            this.process.onExit().thenAccept((p)->{
                if(this.shouldBeRunning && !this.isRunning()){ //make sure this isn't a stray process that's supposed to be long gone
                    long deltaTime = System.currentTimeMillis()-startTime;
                    System.out.println("process "+this.name+ " ended but should be running???");
                    System.out.println("time since start: " + deltaTime);
                    if(deltaTime>1000*60) this.start();
                    else System.err.println("did not restart process, since it has stopped less than a minute after launch.");
                }
            });
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
    public boolean isRunning(){
        return this.process !=null && this.process.isAlive();
    }
    public void stop(){
        if(!isRunning()) return;
        System.out.println(this.name+": stop() @ "+ LocalDateTime.now());
        this.shouldBeRunning = false;
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        service.schedule(()->{
            System.out.println("got tired of waiting for " + this.localLocation+" to destroy. forcing...");
            process.destroyForcibly();
        }, 10, TimeUnit.SECONDS);
        killChildren(process);
        process.destroy();
        //start timeout force destroy
        try{
            int exitVal = process.waitFor();
            System.out.println("APP "+this.name+" stopped process with exit val " + exitVal);
            //System.out.println(this.isRunning());
            //stop the force shutdown
            service.shutdownNow();
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
    public void restart(){
        this.stop();
        this.start();
    }

    private void killChildren(Process p){
        long pid = p.pid();
        //System.out.println("killing processes for "+pid);
        ProcessBuilder builder;

        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            builder = new ProcessBuilder("taskkill", "/F", "/T", "/PID", String.valueOf(pid));
        } else {
            builder = new ProcessBuilder("sh", "-c", "pkill -P " + pid);
        }

        try {
            //builder.inheritIO();
            builder.start().waitFor();
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    private void init(){
        File repoDir = new File(this.localLocation);
        try (Git git = Git.open(repoDir)) {
            this.repo = git;
        } catch (Exception e){
            System.out.println("failed to open repo " + this.localLocation + " (" + this.repoURL+"), cloning...");
            this.cloneRepo();
        }
    }

    /**
     * pulls the repo.
     * @return whether something changed.
     */
    private boolean pullRepo(){
        try{
            repo.stashCreate().call();
            PullResult pull= repo.pull()
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider("token", this.gitToken))
                    .setStrategy(MergeStrategy.get(this.mergeStrategy.toUpperCase()))
                    .setContentMergeStrategy(ContentMergeStrategy.OURS)
                    .call();
            boolean changed = pull.getFetchResult().getTrackingRefUpdates()!=null &&  !pull.getFetchResult().getTrackingRefUpdates().isEmpty();
            try{
                if(!repo.stashList().call().isEmpty())
                    repo.stashApply()
                        .setStrategy(MergeStrategy.get(this.mergeStrategy.toUpperCase()))
                        .setContentMergeStrategy(ContentMergeStrategy.OURS)
                        .call();
            }catch (Exception e){
                System.err.println("App " + this.name+" failed to apply stash. Stashed changes are not currently active.");
            }
            return changed;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public static JSONObject generateDummyJSON(){
        return new JSONObject()
                .put("repoURL", "https://github.com/user/xx.git")
                .put("localLocation", "./xx")
                .put("gitToken", "xxx")
                .put("runCmd", "java -jar xx.jar")
                .put("pullInterval", 5)
                .put("mergeStrategy", "OURS")
                //.put("gui", true);
                .put("io", AppIOHandler.DEFAULT_CONFIG);
        /*
        try(FileOutputStream fos = new FileOutputStream(f)){
            fos.write(data.toString().getBytes());
        }catch (Exception e){
            e.printStackTrace();
        }
        */
    }

    private void cloneRepo(){
        try {
            try(Git git = Git.cloneRepository()
                    .setURI(this.repoURL)
                    .setDirectory(new File(this.localLocation))
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider("token", this.gitToken))
                    .call()){
                this.repo = git;
            }
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
