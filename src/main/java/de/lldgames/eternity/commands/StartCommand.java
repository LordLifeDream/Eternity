package de.lldgames.eternity.commands;

import de.lldgames.eternity.App;
import de.lldgames.eternity.Eternity;

public class StartCommand extends Command{
    public StartCommand(){
        super("Start", new String[]{"start", "startApp"});
        this.description = "Start an app. Usage: start {name}";
    }

    @Override
    public void execute(String[] params) {
        App target = null;
        for(App app: Eternity.loadedApps)
            if(app.name.equalsIgnoreCase(params[1])) {
                target = app;
                break;
            }
        if (target == null) {
            System.out.println("app " + params[1] + " not found!");
            return;
        }
        System.out.println("starting down" + target.name+ "!");
        target.start();
    }
}
