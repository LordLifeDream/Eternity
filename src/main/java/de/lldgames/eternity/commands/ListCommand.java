package de.lldgames.eternity.commands;

import de.lldgames.eternity.App;
import de.lldgames.eternity.Eternity;

public class ListCommand extends Command{
    public ListCommand(){
        super("List", new String[]{"list", "listApps", "apps", "appList"});
        this.description = "lists all loaded apps.";
    }
    @Override
    public void execute(String[] params) {
        System.out.println("there are "+Eternity.loadedApps.size()+" loaded apps:");
        for(App app: Eternity.loadedApps){
            System.out.println("-"+app.name);
        }
    }
}
