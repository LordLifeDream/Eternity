package de.lldgames.eternity.selfUpdate;

import de.lldgames.eternity.Eternity;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SelfUpdater {
    private static Git selfRepo;
    private static Process eternityProcess;
    private static boolean isRestarting = false;

    public static void start(){
        try {
            selfRepo = Git.open(new File("./"));
            eternityProcess = Eternity.createEternityProcess(new String[]{"noSelfUpdate"});
            eternityProcess.onExit().thenAccept(SelfUpdater::onProcessExit);
            ScheduledExecutorService ex = Executors.newSingleThreadScheduledExecutor();
            ex.scheduleAtFixedRate(SelfUpdater::pullLoop, 0, 2, TimeUnit.MINUTES);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void onProcessExit(Process p){
        if(!isRestarting){
            //either crash or user exit.
            System.out.println("[SELFUPDATER] restarting due to proxess termination. Exit code: " + p.exitValue());
            System.exit(0);
        }
    }


    private static void pullLoop(){
        try {
            PullResult res = selfRepo.pull().call();
            boolean changed = res.isSuccessful() && res.getFetchResult().getTrackingRefUpdates() != null && !res.getFetchResult().getTrackingRefUpdates().isEmpty();
            if (changed){
                System.out.println("ETERNITY: pulled new version. RESTARTING...");
                //Eternity.restart();
            }
        }catch (Exception e){

        }
    }

    private static void restart(){
        isRestarting = true;
        try{
            if(eternityProcess!=null&& eternityProcess.isAlive()){
                eternityProcess.destroy();
            }
            eternityProcess = Eternity.createEternityProcess(new String[]{"noSelfUpdate"});
        }catch (Exception e){
           e.printStackTrace();
        }
        isRestarting = false;
    }
}
