package de.lldgames.eternity.commands;

import de.lldgames.eternity.App;
import de.lldgames.eternity.Eternity;

public class StopCommand extends Command{
    public StopCommand(){
        super("Stop", new String[]{"stop", "stopApp"});
        this.description = "Stop an app. Usage: stop {name}";
    }

    @Override
    public void execute(String[] params) {
        App target = null;
        for(App app: Eternity.loadedApps)
            if(app.name.equalsIgnoreCase(params[0])) {
                target = app;
                break;
            }
        if (target == null) {
            System.out.println("app " + params[0] + " not found!");
            return;
        }
        System.out.println("shutting down" + target.name+ "!");
        target.stop();
    }
}
