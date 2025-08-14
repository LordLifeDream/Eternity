package de.lldgames.eternity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.function.Consumer;

public class AppIOReaderThread extends Thread{
    private final Consumer<String> onMessage;
    private boolean shouldBeRunning = true;
    private final InputStream inStream;
    public AppIOReaderThread(Consumer<String> onMessage, Process process, InputStream inStream){
        this.onMessage = onMessage;
        this.inStream = inStream;
        process.onExit().thenAccept((p)->{
            this.shouldBeRunning = false;
        });
    }

    @Override
    public void run() {
        try (InputStreamReader inputStreamReader = new InputStreamReader(inStream);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {

            String line;
            while (this.shouldBeRunning && (line = bufferedReader.readLine()) != null) {
                this.onMessage.accept(line);
            }
        } catch (IOException e) {
            //e.printStackTrace();
        }
    }
}
